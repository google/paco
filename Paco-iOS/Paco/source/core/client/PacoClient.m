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

static NSString* const RunningExperimentsKey = @"has_running_experiments";
static NSString* const kPacoNotificationSystemTurnedOn = @"paco_notification_system_turned_on";

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


typedef void(^BackgroundFetchCompletionBlock)(UIBackgroundFetchResult result);

@interface PacoClient () <PacoSchedulerDelegate>
@property (nonatomic, retain) PacoAuthenticator *authenticator;
@property (nonatomic, retain) PacoModel *model;
@property (nonatomic, strong) PacoEventManager* eventManager;
@property (nonatomic, retain) PacoScheduler *scheduler;
@property (nonatomic, retain) PacoService *service;
@property (nonatomic, strong) Reachability* reachability;
@property (nonatomic, retain) NSString *serverDomain;
@property (nonatomic, retain) PacoPrefetchState *prefetchState;
@property (nonatomic, assign) BOOL firstLaunch;
@property (nonatomic, copy) BackgroundFetchCompletionBlock backgroundFetchBlock;

@end

@implementation PacoClient

#pragma mark Object Life Cycle
+ (PacoClient *)sharedInstance {
  static PacoClient *client = nil;
  
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    client = [[PacoClient alloc] init];
  });
  return client;
}

- (id)init {
  self = [super init];
  if (self) {
    [self checkIfUserFirstLaunchPaco];
    
    self.authenticator = [[PacoAuthenticator alloc] initWithFirstLaunchFlag:_firstLaunch];
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
    DDLogInfo(@"PacoClient initializing...");
  }
  return self;
}

- (void)dealloc {
  DDLogInfo(@"PacoClient deallocating...");
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
  BOOL turnedOn = [[NSUserDefaults standardUserDefaults] boolForKey:kPacoNotificationSystemTurnedOn];
  return turnedOn;
}

- (void)triggerNotificationSystemIfNeeded {
  @synchronized(self) {
    if (![self isNotificationSystemOn]) {
      DDLogInfo(@"Turn on notification system.");
      [[NSUserDefaults standardUserDefaults] setBool:YES forKey:kPacoNotificationSystemTurnedOn];
      [[NSUserDefaults standardUserDefaults] synchronize];
      //set background fetch min internval to be 15 minutes
      [[UIApplication sharedApplication] setMinimumBackgroundFetchInterval:15 * 60];
    }
  }
}

- (void)shutDownNotificationSystemIfNeeded {
  @synchronized(self) {
    if ([self isNotificationSystemOn]) {
      DDLogInfo(@"Shut down notification system.");
      [[NSUserDefaults standardUserDefaults] setBool:NO forKey:kPacoNotificationSystemTurnedOn];
      [[NSUserDefaults standardUserDefaults] synchronize];
      
      [self.scheduler stopSchedulingForAllExperiments];
      [self disableBackgroundFetch];
    }
  }
}


#pragma mark PacoSchedulerDelegate
- (void)handleExpiredNotifications:(NSArray*)expiredNotifications {
  if (0 == [expiredNotifications count]) {
    return;
  }
  NSArray* eventList = [self eventsFromExpiredNotifications:expiredNotifications];
  NSAssert([eventList count] == [expiredNotifications count], @"should have correct number of elements");
  DDLogInfo(@"Save %lu notification expired events", (unsigned long)[eventList count]);
  [self.eventManager saveEvents:eventList];
}

//return YES if Paco finishes loading both running experiments and also notifications
- (BOOL)isDoneInitializationForMajorTask {
  if (![self.model areRunningExperimentsLoaded]) {
    DDLogInfo(@"PacoClient: running experiments are not loaded yet!");
    return NO;
  }
  if (![self.scheduler isDoneLoadingNotifications]) {
    DDLogInfo(@"PacoClient: notifications are not loaded yet!");
    return NO;
  }
  return YES;
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
  NSUInteger numOfRunningExperiments = [self.model.experimentInstances count];
  NSMutableArray* allNotifications =
      [NSMutableArray arrayWithCapacity:numOfRunningExperiments * kTotalNumOfNotifications];
  
  NSDate* now = [NSDate date];
  for (PacoExperiment* experiment in [self.model experimentInstances]) {
    if (![experiment shouldScheduleNotificationsFromNow]) {
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
  
  NSUInteger numOfNotifications = [allNotifications count];
  if (0 == numOfNotifications) {
    return nil;
  }
  //sort all dates and return the first 60
  [allNotifications pacoSortLocalNotificationsByFireDate];
  NSUInteger endIndex = kTotalNumOfNotifications;
  if (numOfNotifications < kTotalNumOfNotifications) {
    endIndex = numOfNotifications;
  }
  return [allNotifications subarrayWithRange:NSMakeRange(0, endIndex)];
}

#pragma mark set up notification system
//After data model is loaded successfully, Paco needs to
//a. load notifications from cache
//b. perform major task if needed
//c. trigger or shutdown the notifications system
- (void)setUpNotificationSystem {
  DDLogInfo(@"Setting up notification system...");
  [self.scheduler initializeNotifications];
  DDLogInfo(@"Finish initializing notifications");
  [(PacoAppDelegate*)[UIApplication sharedApplication].delegate processNotificationIfNeeded];
  [self updateNotificationSystem];
  
  //if the setup finishes during Paco launch caused by background fetch API,
  //then need to trigger the background fetch completion handler to finish up
  if (self.backgroundFetchBlock) {
    [self.eventManager startUploadingEventsInBackgroundWithBlock:self.backgroundFetchBlock];
    self.backgroundFetchBlock = nil;
    DDLogInfo(@"backgroundFetchBlock is set to nil!");
  }
}

- (void)executeRoutineMajorTaskIfNeeded {
  if ([self isNotificationSystemOn]) {
    [self.scheduler executeRoutineMajorTask];
  } else {
    DDLogInfo(@"Skip Executing Major Task, notification system is off");
  }
}

- (void)backgroundFetchStartedWithBlock:(void(^)(UIBackgroundFetchResult))completionBlock {
  if (![self isNotificationSystemOn]) {
    DDLogInfo(@"Skip Executing Major Task, notification system is off");
    [self disableBackgroundFetch];
    if (completionBlock) {
      DDLogInfo(@"UIBackgroundFetchResultNoData");
      completionBlock(UIBackgroundFetchResultNoData);
    }
    DDLogInfo(@"Background fetch finished!");
  } else {
    if ([self isDoneInitializationForMajorTask]) {
      DDLogInfo(@"PacoClient finished initialization, start routine major task.");
      [self.scheduler executeRoutineMajorTask];
      [self.eventManager startUploadingEventsInBackgroundWithBlock:completionBlock];
    } else {
      //if Paco is launched by background fetch API, we need to keep the background fetch completion
      //handler, and trigger it when PacoClient finishes setting up notication system, see setUpNotificationSystem
      DDLogInfo(@"PacoClient isn't done with initialization, waiting...");
      self.backgroundFetchBlock = completionBlock;
    }
  }
}

- (void)disableBackgroundFetch {
  DDLogInfo(@"Disable background fetch");
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
        DDLogError(@"[ERROR]: failed to re-authenticate user!!!");
        [self showLoginScreenWithCompletionBlock:nil];
      }
    }];
    
    DDLogWarn(@"[Reachable]: Online Now!");
  }else {
    DDLogWarn(@"[Reachable]: Offline Now!");
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
    DDLogInfo(@"Valid cookie detected, no need to log in!");
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

- (BOOL)hasRunningExperiments {
  return [[NSUserDefaults standardUserDefaults] boolForKey:RunningExperimentsKey];
}

- (void)applyDefinitionsFromServer:(NSArray*)definitions {
  DDLogInfo(@"Fetched %lu definitions from server", (unsigned long)[definitions count]);
  [self.model applyDefinitionJSON:definitions];
  [self.model saveExperimentDefinitionListJson:definitions];
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
    DDLogInfo(@"Start refreshing definitions...");
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
      [self.service loadMyFullDefinitionListWithBlock:^(NSArray* definitions, NSError *error) {
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
          if (!error) {
            DDLogInfo(@"Succeeded to refreshing definitions.");
            [self refreshSucceedWithDefinitions:definitions];
          } else {
            DDLogError(@"Failed to refresh definitions: %@", [error description]);
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
        DDLogError(@"Failed to prefetch definitions: %@", [error description]);
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
  DDLogInfo(@"Experiment Joined with schedule: %@", [experiment.schedule description]);
  //start scheduling notifications for this joined experiment
  [self.scheduler startSchedulingForExperimentIfNeeded:experiment];

  [[NSUserDefaults standardUserDefaults] setBool:YES forKey:RunningExperimentsKey];
  [[NSUserDefaults standardUserDefaults] synchronize];
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
  }
  //remove experiment from local cache, this needs to be done after stopSchedulingForExperimentIfNeeded
  //is called, since we may need to store missing survey events, which needs the experiment from model
  [self.model deleteExperimentInstance:experiment];

  //shut down notification if needed after experiment is deleted from model
  if ([experiment isScheduledExperiment] && ![self needsNotificationSystem]) {
    [self shutDownNotificationSystemIfNeeded];
  }
  if (![self.model hasRunningExperiments]) {
    [[NSUserDefaults standardUserDefaults] setBool:NO forKey:RunningExperimentsKey];
    [[NSUserDefaults standardUserDefaults] synchronize];
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
