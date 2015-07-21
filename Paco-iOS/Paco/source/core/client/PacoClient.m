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

static NSString* const kPacoNotificationSystemTurnedOn = @"paco_notification_system_turned_on";
static NSString* const kPacoServerConfigAddress = @"paco_server_configuration_address";
static NSString* const kPacoProductionServerAddress = @"quantifiedself.appspot.com";
static NSString* const kPacoLocalServerAddress = @"127.0.0.1";
static NSString* const kPacoStagingServerAddress = @"quantifiedself-staging.appspot.com";

@interface PacoModel ()
- (BOOL)loadExperimentDefinitionsFromFile;
- (NSError*)loadExperimentInstancesFromFile;
- (void)deleteExperimentInstance:(PacoExperiment*)experiment;
- (BOOL)hasRunningExperiments;
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
    [self checkIfUserFirstLaunchOAuth2];
    
    self.authenticator = [[PacoAuthenticator alloc] initWithFirstLaunchFlag:_firstLaunch|_firstOAuth2];
    self.scheduler = [PacoScheduler schedulerWithDelegate:self firstLaunchFlag:_firstLaunch];
    self.service = [[PacoService alloc] init];
    _reachability = [Reachability reachabilityWithHostname:@"www.google.com"];
    // Start the notifier, which will cause the reachability object to retain itself!
    [_reachability startNotifier];

    self.model = [[PacoModel alloc] init];
    
    _eventManager = [PacoEventManager defaultManager];
    
    [self setupServerDomain];
    
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

- (void)checkIfUserFirstLaunchOAuth2 {
  NSString* launchedKey = @"oauth2_launched";
  id value = [[NSUserDefaults standardUserDefaults] objectForKey:launchedKey];
  if (value == nil) { //first launch
    [[NSUserDefaults standardUserDefaults] setObject:@YES forKey:launchedKey];
    [[NSUserDefaults standardUserDefaults] synchronize];
    _firstOAuth2 = YES;
  } else {
    _firstOAuth2 = NO;
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
  [self showLoginScreenWithCompletionBlock:^(NSError *error) {
     if (!error) {
       [self startWorkingAfterLogIn];
     }
   }];
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

- (void)setupServerDomain {
  NSString* serverAddress = [[NSUserDefaults standardUserDefaults] objectForKey:kPacoServerConfigAddress];
  if (!serverAddress) {
    switch (SERVER_DOMAIN_FLAG) {
      case 0: //production server
        serverAddress = kPacoProductionServerAddress;
        break;
        
      case 1: //local server
        serverAddress = kPacoLocalServerAddress;
        break;
        
      case 2: //staging server
        serverAddress = kPacoStagingServerAddress;
        break;
        
      default:
        NSAssert(NO, @"wrong server address");
        break;
    }
  }
  [self updateServerDomainWithAddress:serverAddress];
}

- (void)updateServerDomainWithAddress:(NSString*)serverAddress {
  NSString* prefix = [serverAddress isEqualToString:kPacoLocalServerAddress] ? @"http://" : @"https://";
  self.serverDomain = [NSString stringWithFormat:@"%@%@", prefix, serverAddress];
}

- (void)configurePacoServerAddress:(NSString *)serverAddress {
  [self updateServerDomainWithAddress:serverAddress];
  [[NSUserDefaults standardUserDefaults] setObject:serverAddress forKey:kPacoServerConfigAddress];
  [[NSUserDefaults standardUserDefaults] synchronize];
}

- (NSString*)serverAddress {
  NSString* endOfPrefix = @"//";
  NSRange range = [self.serverDomain rangeOfString:endOfPrefix];
  NSUInteger index = range.location + [endOfPrefix length];
  return [self.serverDomain substringFromIndex:index];
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
  if (![self.model hasLoadedRunningExperiments]) {
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
  NSUInteger numOfRunningExperiments = [self.model.runningExperiments count];
  NSMutableArray* allNotifications =
      [NSMutableArray arrayWithCapacity:numOfRunningExperiments * kTotalNumOfNotifications];
  
  NSDate* now = [NSDate date];
  for (PacoExperiment* experiment in [self.model runningExperiments]) {
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
- (void)showLoginScreenWithCompletionBlock:(LoginCompletionBlock)block {  
  [[PacoClient sharedInstance] loginWithOAuth2CompletionHandler:block];
}

- (void)startWorkingAfterLogIn {
  // Authorize the service.
  self.service.authenticator = self.authenticator;
  
  // Fetch the experiment definitions and the events of joined experiments.
  [self prefetchInBackground];
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
    [self prefetchInBackground];
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
  if ([self isLoggedIn]) {
    if (block) {
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
        [self prefetchInBackground];
        completionHandler(nil);
      } else {
        completionHandler(error);
      }
    }];
  }
}


- (void)uploadPendingEventsInBackground {
  [self.eventManager startUploadingEvents];
}


//refreshing all definitions published to the current user is a full refreshing
//refreshing only running experiments' definitions is a partial refreshing
- (void)refreshSucceedWithDefinitions:(NSArray*)newDefinitions isPartialUpdate:(BOOL)isPartial{
  DDLogInfo(@"Fetched %lu definitions from server", (unsigned long)[newDefinitions count]);

  //save survey missing events
  [self.scheduler cleanExpiredNotifications];
  
  if (isPartial) {
    [self.model partiallyUpdateDefinitionList:newDefinitions];
  } else {
    [self.model fullyUpdateDefinitionList:newDefinitions];
  }
  
  if (![self.model hasRunningExperiments]) {
    return;
  }
  
  BOOL shouldRefreshSchedules = [self.model refreshExperimentsWithDefinitionList:newDefinitions];
  if (shouldRefreshSchedules) { //reset notification system
    [self.scheduler restartNotificationSystem];
  }
}

- (void)refreshMyDefinitionsWithBlock:(PacoRefreshCompletionBlock)completionBlock {
  @synchronized(self) {
    DDLogInfo(@"Start refreshing definitions...");
    [self.service loadMyFullDefinitionListWithBlock:^(NSArray* definitions, NSError *error) {
      if (!error) {
        DDLogInfo(@"Succeeded to refreshing definitions.");
        [self refreshSucceedWithDefinitions:definitions isPartialUpdate:NO];
      } else {
        DDLogError(@"Failed to refresh definitions: %@", [error description]);
      }
      if (completionBlock) {
        completionBlock(error);
      }
    }];
  }
}


- (void)refreshRunningExperimentsWithBlock:(PacoRefreshCompletionBlock)completionBlock {
  @synchronized(self) {
    if (![self.model hasRunningExperiments]) {
      if (completionBlock) {
        completionBlock(nil);
      }
      return;
    }
    
    NSArray* definitionIdList = [self.model runningExperimentIdList];
    void(^finishBlock)(NSArray* , NSError*) = ^(NSArray* definitions, NSError* error) {
      if (!error) {
        [self refreshSucceedWithDefinitions:definitions isPartialUpdate:YES];
      }
      if (completionBlock) {
        completionBlock(error);
      }
    };
    [self.service loadFullDefinitionListWithIDs:definitionIdList andBlock:finishBlock];
  }
}

#pragma mark Private methods
- (void)prefetchInBackground {
  @synchronized(self) {
    NSError* error = [self.model loadExperimentInstancesFromFile];
    [[NSNotificationCenter defaultCenter] postNotificationName:kPacoNotificationLoadedRunningExperiments
                                                        object:error];
    [self setUpNotificationSystem];
    // Load the experiment definitions.
    BOOL success = [self.model loadExperimentDefinitionsFromFile];
    if (success) {
      [[NSNotificationCenter defaultCenter] postNotificationName:kPacoNotificationLoadedMyDefinitions
                                                          object:nil];
    } else {
      [self.service loadMyFullDefinitionListWithBlock:^(NSArray* definitions, NSError* error) {
        if (!error) {
          [self.model fullyUpdateDefinitionList:definitions];
        } else {
          DDLogError(@"Failed to prefetch definitions: %@", [error description]);
        }
        [[NSNotificationCenter defaultCenter] postNotificationName:kPacoNotificationLoadedMyDefinitions
                                                            object:error];
      }];
    }
  }
}


#pragma mark join an experiment
- (void)joinExperimentWithDefinition:(PacoExperimentDefinition*)definition
                            schedule:(PacoExperimentSchedule*)schedule
                     completionBlock:(void(^)())completionBlock {
  NSAssert(definition, @"definition should not be nil");
  [self.eventManager saveJoinEventWithDefinition:definition withSchedule:schedule];
  //create a new experiment and save it to cache
  PacoExperiment *experiment = [self.model addExperimentWithDefinition:definition
                                                              schedule:schedule];
  DDLogInfo(@"Experiment Joined with schedule: %@", [experiment.schedule description]);
  //start scheduling notifications for this joined experiment
  [self.scheduler startSchedulingForExperimentIfNeeded:experiment];
  
  if (completionBlock) {
    completionBlock();
  }
}

#pragma mark modify a running experiment's schedule
- (void)changeScheduleForExperiment:(PacoExperiment*)experiment
                        newSchedule:(PacoExperimentSchedule*)newSchedule
                    completionBlock:(void(^)())completionBlock {
  if ([newSchedule isExactlyEqualToSchedule:experiment.schedule]) {
    if (completionBlock) {
      completionBlock();
    }
    return;
  }
  DDLogInfo(@"Change schedule for experiment ...");
  [self.model configureExperiment:experiment withSchedule:newSchedule];
  [self.scheduler restartNotificationSystem];
  if (completionBlock) {
    completionBlock();
  }
}

#pragma mark stop an experiment
- (void)stopExperiment:(PacoExperiment*)experiment withBlock:(void(^)())completionBlock {
  if (!experiment) {
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
  
  if (completionBlock) {
    completionBlock();
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
