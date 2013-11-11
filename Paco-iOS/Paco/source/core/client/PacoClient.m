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
    
  }
  return self;
}

- (void)dealloc {
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

- (void)invalidateUserAccount {
  [self.authenticator invalidateCurrentAccount];
  [self showLoginScreenWithCompletionBlock:nil];
}

- (NSArray*)eventsFromExpiredNotifications:(NSArray*)expiredNotifications {
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

- (void)triggerNotificationSystemIfNeeded {
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
    }
  });
}

- (void)shutDownNotificationSystemIfNeeded {
  if (self.location == nil) {
    return;
  }
  NSLog(@"Shut down notification system ...");
  [self.location disableLocationService];
  self.location = nil;
}

#pragma mark PacoSchedulerDelegate
- (void)handleNotificationTimeOut:(NSString*)experimentInstanceId
               experimentFireDate:(NSDate*)scheduledTime {
  if (!ADD_TEST_DEFINITION) {
    NSLog(@"Save experiment missed event for experiment %@ with scheduledTime %@",
          experimentInstanceId, [PacoDateUtility pacoStringForDate:scheduledTime]);
    PacoExperimentDefinition* definition = [self.model experimentForId:experimentInstanceId].definition;
    NSAssert(definition != nil, @"definition should not be nil!");
    [self.eventManager saveSurveyMissedEventForDefinition:definition withScheduledTime:scheduledTime];
  }
}

- (void)handleExpiredNotifications:(NSArray*)expiredNotifications {
  if (0 == [expiredNotifications count]) {
    return;
  }
  NSArray* eventList = [self eventsFromExpiredNotifications:expiredNotifications];
  NSAssert([eventList count] == [expiredNotifications count], @"should have correct number of elements");
  [self.eventManager saveEvents:eventList];
}

- (BOOL)needsNotificationSystem {
  return [self.model shouldTriggerNotificationSystem];
}

- (void)updateNotificationSystem {
  if ([self needsNotificationSystem]) {
    [self triggerNotificationSystemIfNeeded];
  } else {
    [self shutDownNotificationSystemIfNeeded];
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
    [self setUpNotificationSystem ];
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
    // Load the experiment definitions.
    
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
    
    [self.service loadMyFullDefinitionListWithBlock:^(NSArray *experiments, NSError *error) {
      if (error) {
        NSLog(@"Failed to prefetch definitions: %@", [error description]);
        [self definitionsLoadedWithError:error];
        if (completionBlock) {
          completionBlock();
        }
        return;
      }
      
      NSLog(@"Loaded %d experiments", [experiments count]);
      // Convert the JSON response into an object model.
      [self.model applyDefinitionJSON:experiments];
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
- (void)joinExperimentWithDefinition:(PacoExperimentDefinition*)definition {
  if (definition == nil) {
    return;
  }
  [self.eventManager saveJoinEventWithDefinition:definition withSchedule:nil];
  //create a new experiment and save it to cache
  PacoExperiment *experiment = [[PacoClient sharedInstance].model
                                addExperimentInstance:definition
                                schedule:definition.schedule
                                events:nil]; //TODO: events will be removed from this method
  //start scheduling notifications for this joined experiment
  [self.scheduler startSchedulingForExperimentIfNeeded:experiment];
}

#pragma mark stop an experiment
- (void)stopExperiment:(PacoExperiment*)experiment {
  if (experiment == nil) {
    return;
  }
  [self.eventManager saveStopEventWithExperiment:experiment];
  //remove experiment from local cache
  [self.model deleteExperimentInstance:experiment];
  
  //if experiment is self-report, no need to touch notification system
  if ([experiment isSelfReportExperiment]) {
    return;
  }
  if ([self needsNotificationSystem]) {
    //clear all scheduled notifications and notifications in the tray for the stopped experiment
    [self.scheduler stopSchedulingForExperimentIfNeeded:experiment];
  } else {
    [self.scheduler stopSchedulingForAllExperiments];
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
