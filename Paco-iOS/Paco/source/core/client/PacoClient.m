/* Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "PacoClient.h"

#import "PacoAuthenticator.h"
#import "PacoLocation.h"
#import "PacoModel.h"
#import "PacoScheduler.h"
#import "PacoService.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperiment.h"
#import "PacoEvent.h"
#import "Reachability.h"
#import "PacoEventManager.h"
#import "PacoAppDelegate.h"
#import "NSError+Paco.h"
#import "PacoDateUtility.h"
#import "UILocalNotification+Paco.h"
#import "PacoScheduleGenerator.h"
#import "NSMutableArray+Paco.h"
#import "PacoExperimentSchedule.h"



@interface PacoPrefetchState : NSObject
@property(atomic, readwrite, assign) BOOL finishLoadingDefinitions;
@property(atomic, readwrite, strong) NSError* errorLoadingDefinitions;

@property(atomic, readwrite, assign) BOOL finishLoadingExperiments;
@property(atomic, readwrite, strong) NSError* errorLoadingExperiments;
@end

@implementation PacoPrefetchState


- (BOOL)succeedToLoadDefinitions {
  return self.finishLoadingDefinitions && self.errorLoadingDefinitions == nil;
}


- (BOOL)succeedToLoadExperiments {
  return self.finishLoadingExperiments && self.errorLoadingExperiments == nil;
}

- (BOOL)succeedToLoadAll {
  return [self succeedToLoadDefinitions] && [self succeedToLoadExperiments];
}

- (BOOL)finishLoadingAll {
  return self.finishLoadingDefinitions && self.finishLoadingExperiments;
}

- (void)reset
{
  self.finishLoadingDefinitions = NO;
  self.errorLoadingDefinitions = NO;
  
  self.finishLoadingExperiments = NO;
  self.errorLoadingExperiments = nil;
}
@end

@interface PacoModel ()
- (BOOL)loadExperimentDefinitionsFromFile;
- (NSError*)loadExperimentInstancesFromFile;
- (void)applyDefinitionJSON:(id)jsonObject;
- (void)deleteExperimentInstance:(PacoExperiment*)experiment;
@end

@interface PacoClient () <PacoLocationDelegate, PacoSchedulerDelegate>
@property (nonatomic, retain, readwrite) PacoAuthenticator *authenticator;
@property (nonatomic, retain, readwrite) PacoLocation *location;
@property (nonatomic, retain, readwrite) PacoModel *model;
@property (nonatomic, strong, readwrite) PacoEventManager* eventManager;
@property (nonatomic, retain, readwrite) PacoScheduler *scheduler;
@property (nonatomic, retain, readwrite) PacoService *service;
@property (nonatomic, strong) Reachability* reachability;
@property (nonatomic, retain, readwrite) NSString *serverDomain;
@property (nonatomic, retain, readwrite) PacoPrefetchState *prefetchState;
@property (nonatomic, assign, readwrite) BOOL firstLaunch;
@end

@implementation PacoClient

#pragma mark Object Life Cycle
+ (PacoClient *)sharedInstance {
  static PacoClient *client = nil;
  if (!client) {
    client = [[PacoClient alloc] init];
  }
  return client;
}

- (id)init {
  self = [super init];
  if (self) {
    [self checkIfUserFirstLaunchPaco];
    
    self.authenticator = [[PacoAuthenticator alloc] initWithFirstLaunchFlag:_firstLaunch];
    self.location = nil;//[[PacoLocation alloc] init];
    self.scheduler = [PacoScheduler schedulerWithDelegate:self firstLaunchFlag:_firstLaunch];
    self.service = [[PacoService alloc] init];
    _reachability = [Reachability reachabilityWithHostname:@"www.google.com"];
    // Start the notifier, which will cause the reachability object to retain itself!
    [_reachability startNotifier];

    self.model = [[PacoModel alloc] init];
    
    _eventManager = [PacoEventManager defaultManager];
    
    self.prefetchState = [[PacoPrefetchState alloc] init];
    
    if (SERVER_DOMAIN_FLAG == 0) {//production
      self.serverDomain = @"https://quantifiedself.appspot.com";
    }else{//localserver
      self.serverDomain = @"http://127.0.0.1";
    }
    NSLog(@"PacoClient initializing...");
  }
  return self;
}

- (void)dealloc {
  NSLog(@"PacoClient deallocating...");
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)checkIfUserFirstLaunchPaco {
  NSString* launchedKey = @"paco_launched";
  id value = [[NSUserDefaults standardUserDefaults] objectForKey:launchedKey];
  if (value == nil) { //first launch
    [[NSUserDefaults standardUserDefaults] setObject:@YES forKey:launchedKey];
    [[NSUserDefaults standardUserDefaults] synchronize];
    _firstLaunch = YES;
  } else {
    _firstLaunch = NO;
  }
}

#pragma mark Public methods
- (BOOL)isLoggedIn {
  return [self.authenticator isLoggedIn];
}

- (BOOL)isUserAccountStored {
  return [self.authenticator isUserAccountStored];
}

- (BOOL)hasJoinedExperimentWithId:(NSString*)definitionId {
  return [self.model isExperimentJoined:definitionId];
}

- (NSString*)userEmail {
  if (SKIP_LOG_IN) {
    return @"test@gmail.com";
  }
  return [self.authenticator userEmail];
}

- (NSString*)userName {
  NSString* email = [self userEmail];
  NSArray* components = [email componentsSeparatedByString:@"@"];
  NSAssert([components count] == 2, @"should be a valid email");
  NSString* name = [[components firstObject] capitalizedString];
  return name;
}

- (void)invalidateUserAccount {
  [self.authenticator invalidateCurrentAccount];
  [self showLoginScreenWithCompletionBlock:nil];
}

- (NSArray*)eventsFromExpiredNotifications:(NSArray*)expiredNotifications {
  NSAssert([[self userEmail] length] > 0, @"userEmail should be valid");
  NSMutableArray* eventList = [NSMutableArray arrayWithCapacity:[expiredNotifications count]];
  for (UILocalNotification* notification in expiredNotifications) {
    NSString* experimentId = [notification pacoExperimentId];
    NSAssert([experimentId length] > 0, @"id should be valid");
    PacoExperiment* experiment = [self.model experimentForId:experimentId];
    NSAssert(experiment, @"experiment should be valid");
    NSDate* fireDate = [notification pacoFireDate];
    NSAssert(fireDate, @"fireDate");
    PacoEvent* event = [PacoEvent surveyMissedEventForDefinition:experiment.definition
                                               withScheduledTime:fireDate
                                                      userEmail:[self userEmail]];
    [eventList addObject:event];
  }
  return eventList;
}

- (BOOL)isNotificationSystemOn {
  return self.location != nil;
}

- (void)triggerNotificationSystemIfNeeded {
  @synchronized(self) {
    if (self.location != nil) {
      return;
    }
    //NOTE:CLLocationManager need to be initialized in the main thread to work correctly
    //http://stackoverflow.com/questions/7857323/ios5-what-does-discarding-message-for-event-0-because-of-too-many-unprocessed-m
    dispatch_async(dispatch_get_main_queue(), ^{
      if (self.location == nil) {
        NSLog(@"***********  PacoLocation is allocated ***********");
        self.location = [[PacoLocation alloc] init];
        self.location.delegate = self;
        [self.location enableLocationService];
        
        //set background fetch min internval to be 15 minutes
        [[UIApplication sharedApplication] setMinimumBackgroundFetchInterval:15 * 60];
      }
    });
  }
}

- (void)shutDownNotificationSystemIfNeeded {
  @synchronized(self) {
    if (self.location == nil) {
      return;
    }
    NSLog(@"Shut down notification system ...");
    [self.scheduler stopSchedulingForAllExperiments];
    
    [self disableBackgroundFetch];
    self.location.delegate = nil;
    [self.location disableLocationService];
    self.location = nil;
  }
}

#pragma mark PacoLocationDelegate
- (void)locationChangedSignificantly {
  UIApplicationState state = [[UIApplication sharedApplication] applicationState];
  if (state != UIApplicationStateBackground) {
    return;
  }
  BOOL shouldUpdate = YES;
  static NSString* lastUpdateDateKey = @"last_update_key";
  NSDate* lastUpdateDate  = [[NSUserDefaults standardUserDefaults] objectForKey:lastUpdateDateKey];
  NSDate* now = [NSDate date];
  if (lastUpdateDate != nil && [now timeIntervalSinceDate:lastUpdateDate] < 15 * 60) {//less than 15 minutes
    shouldUpdate = NO;
  }
  if (shouldUpdate) {
    [self executeRoutineMajorTaskIfNeeded];
    [[NSUserDefaults standardUserDefaults] setObject:now forKey:lastUpdateDateKey];
    [[NSUserDefaults standardUserDefaults] synchronize];
  }
}


#pragma mark PacoSchedulerDelegate
- (void)handleExpiredNotifications:(NSArray*)expiredNotifications {
  if (0 == [expiredNotifications count]) {
    return;
  }
  NSArray* eventList = [self eventsFromExpiredNotifications:expiredNotifications];
  NSAssert([eventList count] == [expiredNotifications count], @"should have correct number of elements");
  NSLog(@"Save %d notification expired events", [eventList count]);
  [self.eventManager saveEvents:eventList];
}

- (BOOL)needsNotificationSystem {
  return [self.model shouldTriggerNotificationSystem];
}

- (void)updateNotificationSystem {
  @synchronized(self) {
    if ([self needsNotificationSystem]) {
      [self triggerNotificationSystemIfNeeded];
    } else {
      [self shutDownNotificationSystemIfNeeded];
    }
  }
}

- (NSArray*)nextNotificationsToSchedule {
  int numOfRunningExperiments = [self.model.experimentInstances count];
  NSMutableArray* allNotifications =
      [NSMutableArray arrayWithCapacity:numOfRunningExperiments * kTotalNumOfNotifications];
  
  NSDate* now = [NSDate date];
  for (PacoExperiment* experiment in [self.model experimentInstances]) {
    if (![experiment shouldScheduleNotifications]) {
      continue;
    }
    NSArray* dates = [PacoScheduleGenerator nextDatesForExperiment:experiment
                                                        numOfDates:kTotalNumOfNotifications
                                                          fromDate:now];
    NSArray* notifications = [UILocalNotification pacoNotificationsForExperiment:experiment
                                                                 datesToSchedule:dates];
    [allNotifications addObjectsFromArray:notifications];
  }
  
  //we need to store generated esm schedules inside experiment plist
  [self.model saveExperimentInstancesToFile];
  
  int numOfNotifications = [allNotifications count];
  if (0 == numOfNotifications) {
    return nil;
  }
  //sort all dates and return the first 60
  [allNotifications pacoSortLocalNotificationsByFireDate];
  int endIndex = kTotalNumOfNotifications;
  if (numOfNotifications < kTotalNumOfNotifications) {
    endIndex = numOfNotifications;
  }
  return [allNotifications subarrayWithRange:NSMakeRange(0, endIndex)];
}

#pragma mark set up notification system
//After date model is loaded successfully, Paco needs to
//a. load notifications from cache
//b. perform major task if needed
//c. trigger or shutdown the notifications system
- (void)setUpNotificationSystem {
  [self.scheduler initializeNotifications];
  [self updateNotificationSystem];
}

- (void)executeRoutineMajorTaskIfNeeded {
  if ([self isNotificationSystemOn]) {
    [self.scheduler executeRoutineMajorTask];
  } else {
    NSLog(@"Skip Executing Major Task, notification system is off");
  }
}

- (void)backgroundFetchStarted {
  if (![self isNotificationSystemOn]) {
    [self disableBackgroundFetch];
  } else {
    [self executeRoutineMajorTaskIfNeeded];
  }
}

- (void)disableBackgroundFetch {
  NSLog(@"Disable background fetch");
  [[UIApplication sharedApplication] setMinimumBackgroundFetchInterval:UIApplicationBackgroundFetchIntervalNever];
}

#pragma mark bring up login flow if necessary
- (void)showLoginScreenWithCompletionBlock:(LoginCompletionBlock)block
{
  if (SKIP_LOG_IN) {
    [self prefetchInBackgroundWithBlock:^{
      [self setUpNotificationSystem];
    }];
    return;
  }
  
  UINavigationController* navi = (UINavigationController*)
      ((PacoAppDelegate*)[UIApplication sharedApplication].delegate).window.rootViewController;
  if (![navi.visibleViewController isKindOfClass:[PacoLoginScreenViewController class]]) {
    PacoLoginScreenViewController *loginViewController =
        [PacoLoginScreenViewController controllerWithCompletionBlock:block];
    [navi presentViewController:loginViewController animated:YES completion:nil];
  }  
}

- (void)startWorkingAfterLogIn {
  // Authorize the service.
  self.service.authenticator = self.authenticator;
  
  // Fetch the experiment definitions and the events of joined experiments.
  [self prefetchInBackgroundWithBlock:^{
    [self setUpNotificationSystem];
  }];
  
  [self uploadPendingEventsInBackground];
}


- (void)reachabilityChanged:(NSNotification*)notification {
  Reachability* reach = (Reachability*)[notification object];
  if ([reach isReachable]) {      
    [self.authenticator reAuthenticateWithBlock:^(NSError* error) {
      [[NSNotificationCenter defaultCenter] removeObserver:self];

      if (error == nil) {
        // Authorize the service.
        self.service.authenticator = self.authenticator;
        [self uploadPendingEventsInBackground];
      } else {
        NSLog(@"[ERROR]: failed to re-authenticate user!!!");
        [self showLoginScreenWithCompletionBlock:nil];
      }
    }];
    
    NSLog(@"[Reachable]: Online Now!");
  }else {
    NSLog(@"[Reachable]: Offline Now!");
  }
}

- (void)reAuthenticateUserWithBlock:(LoginCompletionBlock)block {
  //If there is an account stored, and the internet is offline, then we should allow user to use
  //our app, so we need to prefetch definitions and experiments. When the internet is reacheable,
  //we will re-authenticate user
  if (!self.reachability.isReachable) {
    [self prefetchInBackgroundWithBlock:^{
      [self setUpNotificationSystem];
    }];
    if (block != nil) {
      block(nil);
    }
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(reachabilityChanged:)
                                                 name:kReachabilityChangedNotification
                                               object:nil];
  } else {
    [self.authenticator reAuthenticateWithBlock:^(NSError* error) {
      if (error == nil) {
        [self startWorkingAfterLogIn];
        if (block != nil) {
          block(nil);
        }
      } else {
        [self showLoginScreenWithCompletionBlock:block];
      }
    }];
  }
}


- (void)loginWithCompletionBlock:(LoginCompletionBlock)block {
  if (SKIP_LOG_IN) {
    [self startWorkingAfterLogIn];
    if (block != nil) {
      block(nil);
    }
    return;
  }
  
  
  if ([self isLoggedIn]) {
    if (block) {
      block(nil);
    }
    return;
  }

  
  if ([self.authenticator setupWithCookie]) {
    NSLog(@"Valid cookie detected, no need to log in!");
    [self startWorkingAfterLogIn];
    
    if (block != nil) {
      block(nil);
    }
    return;
  }
  
  if (![self isUserAccountStored]) {
    [self showLoginScreenWithCompletionBlock:block];
  } else {
    [self reAuthenticateUserWithBlock:block];
  }
}


- (void)loginWithClientLogin:(NSString *)email
                    password:(NSString *)password
           completionHandler:(void (^)(NSError *))completionHandler {
  NSAssert(![self isLoggedIn], @"user should not be logged in!");
  [self.authenticator authenticateWithClientLogin:email
                                         password:password
                                completionHandler:^(NSError *error) {
                                  if (!error) {
                                    [self startWorkingAfterLogIn];
                                    completionHandler(nil);
                                  } else {
                                    completionHandler(error);
                                  }
                                }];    
}


- (void)loginWithOAuth2CompletionHandler:(void (^)(NSError *))completionHandler {
  if ([self isLoggedIn]) {
    if (completionHandler != nil) {
      completionHandler(nil);
    }
  }else{
    [self.authenticator authenticateWithOAuth2WithCompletionHandler:^(NSError *error) {
      if (!error) {
        // Authorize the service.
        self.service.authenticator = self.authenticator;
        // Fetch the experiment definitions and the events of joined experiments.
        [self prefetchInBackgroundWithBlock:^{
          // let's handle setting up the notifications after that thread completes
          NSLog(@"Paco loginWithOAuth2CompletionHandler experiments load has completed.");
          [self setUpNotificationSystem];
        }];
        completionHandler(nil);
      } else {
        completionHandler(error);
      }
    }];
  }
}


- (void)uploadPendingEventsInBackground {
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    [self.eventManager startUploadingEvents];
  });
}

- (BOOL)prefetchedDefinitions
{
  return self.prefetchState.finishLoadingDefinitions;
}

- (NSError*)errorOfPrefetchingDefinitions
{
  return self.prefetchState.errorLoadingDefinitions;
}

- (BOOL)prefetchedExperiments
{
  return self.prefetchState.finishLoadingExperiments;
}

- (NSError*)errorOfPrefetchingexperiments
{
  return self.prefetchState.errorLoadingExperiments;
}

- (void)applyDefinitionsFromServer:(NSArray*)definitions {
  NSLog(@"Fetched %d definitions from server", [definitions count]);
  [self.model applyDefinitionJSON:definitions];
  [self.model saveExperimentDefinitionsToFile];
}


- (void)refreshSucceedWithDefinitions:(NSArray*)newDefinitions {
  //save survey missing events
  [self.scheduler cleanExpiredNotifications];
  
  [self applyDefinitionsFromServer:newDefinitions];
  
  if (![self.model hasRunningExperiments]) {
    return;
  }
  
  BOOL shouldRefreshSchedules = [self.model refreshExperiments];
  if (shouldRefreshSchedules) { //reset notification system
    [self.scheduler restartNotificationSystem];
  }
}

- (void)refreshDefinitions {
  @synchronized(self) {
    if (![self.prefetchState finishLoadingAll]) {
      return;
    }
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
      [self.service loadMyFullDefinitionListWithBlock:^(NSArray* definitions, NSError *error) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
          if (!error) {
            [self refreshSucceedWithDefinitions:definitions];
          } else {
            NSLog(@"Failed to refresh definitions: %@", [error description]);
          }
          self.prefetchState.finishLoadingDefinitions = YES;
          self.prefetchState.finishLoadingExperiments = YES;
          //if it succeeded this time or last time
          if (error == nil || (error != nil && [self.prefetchState succeedToLoadDefinitions])) {
            self.prefetchState.errorLoadingDefinitions = nil;
            self.prefetchState.errorLoadingExperiments = nil;
            [[NSNotificationCenter defaultCenter] postNotificationName:PacoFinishRefreshing
                                                                object:nil];
          } else {
            self.prefetchState.errorLoadingDefinitions = error;
            self.prefetchState.errorLoadingExperiments = error;
            [[NSNotificationCenter defaultCenter] postNotificationName:PacoFinishRefreshing
                                                                object:error];
          }
        });
      }];
    });


  }
}

#pragma mark Private methods
- (void)definitionsLoadedWithError:(NSError*)error
{
  if (ADD_TEST_DEFINITION) {
    // for testing purposes let's load a sample experiment
    //[self.model addExperimentDefinition:[PacoExperimentDefinition testPacoExperimentDefinition]];
    [self.model addExperimentDefinition:[PacoExperimentDefinition testDefinitionWithId:@"999999999"]];
  }
  self.prefetchState.finishLoadingDefinitions = YES;
  self.prefetchState.errorLoadingDefinitions = error;
  [[NSNotificationCenter defaultCenter] postNotificationName:PacoFinishLoadingDefinitionNotification object:error];
}

- (void)prefetchInBackgroundWithBlock:(void (^)(void))completionBlock {
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    if ([self.prefetchState succeedToLoadAll]) {
      return;
    }

    [self.prefetchState reset];
    
    if (SKIP_LOG_IN) {
      [self definitionsLoadedWithError:nil];
      [self prefetchExperimentsWithBlock:completionBlock];
      return;
    }

    // Load the experiment definitions.
    BOOL success = [self.model loadExperimentDefinitionsFromFile];
    if (success) {
      [self definitionsLoadedWithError:nil];
      [self prefetchExperimentsWithBlock:completionBlock];
      return;
    }
    
    [self.service loadMyFullDefinitionListWithBlock:^(NSArray* definitions, NSError* error) {
      if (error) {
        NSLog(@"Failed to prefetch definitions: %@", [error description]);
        [self definitionsLoadedWithError:error];
        if (completionBlock) {
          completionBlock();
        }
        return;
      }
      [self applyDefinitionsFromServer:definitions];
      [self definitionsLoadedWithError:nil];
      [self prefetchExperimentsWithBlock:completionBlock];
    }];
  });
}


- (void)experimentsLoadedWithError:(NSError*)error
{
  self.prefetchState.finishLoadingExperiments = YES;
  self.prefetchState.errorLoadingExperiments = error;
  
  [[NSNotificationCenter defaultCenter] postNotificationName:PacoFinishLoadingExperimentNotification object:error];
}


- (void)prefetchExperimentsWithBlock:(void (^)(void))completionBlock {
  NSError* error = [self.model loadExperimentInstancesFromFile];
  [self experimentsLoadedWithError:error];
  if (completionBlock) {
    completionBlock();
  }
}

#pragma mark join an experiment
- (void)joinExperimentWithDefinition:(PacoExperimentDefinition*)definition
                         andSchedule:(PacoExperimentSchedule*)schedule {
  if (definition == nil) {
    return;
  }
  [self.eventManager saveJoinEventWithDefinition:definition withSchedule:schedule];
  //create a new experiment and save it to cache
  PacoExperiment *experiment = [self.model addExperimentWithDefinition:definition
                                                              schedule:schedule];
  NSLog(@"Experiment Joined with schedule: %@", [experiment.schedule description]);
  //start scheduling notifications for this joined experiment
  [self.scheduler startSchedulingForExperimentIfNeeded:experiment];
}

#pragma mark stop an experiment
- (void)stopExperiment:(PacoExperiment*)experiment {
  if (experiment == nil) {
    return;
  }
  [self.eventManager saveStopEventWithExperiment:experiment];

  if ([experiment isScheduledExperiment]) {
    //clear all scheduled notifications and notifications in the tray for the stopped experiment
    [self.scheduler stopSchedulingForExperimentIfNeeded:experiment];
    if ([experiment.schedule isESMSchedule]) {
      [experiment.definition clearEsmScheduleList];
    }
  }
  //remove experiment from local cache, this needs to be done after stopSchedulingForExperimentIfNeeded
  //is called, since we may need to store missing survey events, which needs the experiment from model
  [self.model deleteExperimentInstance:experiment];

  //shut down notification if needed after experiment is deleted from model
  if ([experiment isScheduledExperiment] && ![self needsNotificationSystem]) {
    [self shutDownNotificationSystemIfNeeded];
  }
}

#pragma mark submit a survey
- (void)submitSurveyWithDefinition:(PacoExperimentDefinition*)definition
                      surveyInputs:(NSArray*)surveyInputs
                      notification:(UILocalNotification*)notification {
  if (notification) {
    [self.eventManager saveSurveySubmittedEventForDefinition:definition
                                                  withInputs:surveyInputs
                                            andScheduledTime:[notification pacoFireDate]];
    [self.scheduler handleRespondedNotification:notification];
  } else {
    [self.eventManager saveSelfReportEventWithDefinition:definition andInputs:surveyInputs];
  }
}


@end
