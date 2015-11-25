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

#import "PacoAppDelegate.h"

#import "GoogleAppEngineAuth.h"
#import "GTMOAuth2ViewControllerTouch.h"
#import "PacoExtendedClient.h"
#import "JCNotificationCenter.h"
#import "JCNotificationBannerPresenterSmokeStyle.h"
#import "UILocalNotification+PacoExteded.h"
#import "DDLog.h"
#import "DDASLLogger.h"
#import "DDFileLogger.h"
#import "DDTTYLogger.h"
#import "ScheduleTestViewController.h"
#import "PacoTableExperimentsController.h" 
#import "PacoMediator.h" 
#import "Paco-Swift.h" 
#import "PacoNetwork.h" 
 
#import "ExperimentHelper.h"
#import "java/util/ArrayList.h"
#import "java/util/List.h"
#import "Input2.h"

#import "TestUtil.h"





@implementation PacoAppDelegate


static NSString *dataSource = @"{\r\n  \"title\": \"For Inputing\",\r\n  \"description\": \"to view inputting controlls\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"self\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5726461709254656,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/11\/11\",\r\n  \"published\": true,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [\r\n    \"elasticsearch64@gmail.com\"\r\n  ],\r\n  \"version\": 30,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1446848310613,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"color\": 0,\r\n              \"dismissible\": true,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1446848310612,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 53700000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"only time\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1446848310614,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [\r\n        {\r\n          \"name\": \"apple\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"best fruit\",\r\n          \"likertSteps\": 5,\r\n          \"listChoices\": [\r\n            \"most intersting\",\r\n            \"pair\",\r\n            \"orange\",\r\n            \"plum\"\r\n          ],\r\n          \"multiselect\": true,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"uu\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"list\",\r\n          \"text\": \"how are you\",\r\n          \"likertSteps\": 5,\r\n          \"listChoices\": [\r\n            \"red\",\r\n            \"yello\",\r\n            \"green\"\r\n          ],\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"how great\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"list\",\r\n          \"text\": \"multiple selections\",\r\n          \"likertSteps\": 5,\r\n          \"listChoices\": [\r\n            \"good\",\r\n            \"great\",\r\n            \"better\",\r\n            \"even better\"\r\n          ],\r\n          \"multiselect\": true,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"howsmallvar\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"How small\",\r\n          \"likertSteps\": 5,\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        }\r\n      ],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>\\nNo need to do anything else for now.<br\/><br\/>\\nPaco will send you a notification when it is time to participate.<br\/><br\/>\\nBe sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";


/*
    Process ReceivedNotification
 */
- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification {
    [self processReceivedNotification:notification mustShowSurvey:NO];
}

/*
 
     if received notification not active check if there is an active notification.
      dray to show servuay for active notification
 
 */
- (void)processReceivedNotification:(UILocalNotification*)notification mustShowSurvey:(BOOL)mustShowSurvey {
  if (!notification) {
      }
    
  DDLogInfo(@"Detail: %@", [notification pacoDescriptionExt]);
  UILocalNotification* activeNotification = notification;
    
    
  if (![[PacoMediator sharedInstance].notificationManager isNotificationActive:activeNotification])
  {
 
    DDLogInfo(@"Notification is not active anymore, cancelling it from the tray...");
      
    [UILocalNotification pacoCancelLocalNotificationExt:activeNotification];
    activeNotification =
        [[PacoMediator sharedInstance].notificationManager activeNotificationForExperiment:[notification pacoExperimentIdExt]];
      
    if (activeNotification) {
      DDLogInfo(@"Active Notification Detected: %@", [activeNotification pacoDescriptionExt]);
    } else {
      DDLogInfo(@"No Active Notification Detected. ");
    }
  }
  
  UIApplicationState state = [[UIApplication sharedApplication] applicationState];
  if (activeNotification == nil) {
      
    [self showNoSurveyNeededForNotification:notification];
      
      
  } else {
      
    if (mustShowSurvey) {
        
      [self showSurveyForNotification:activeNotification];
        
    } else {
        
      if (state == UIApplicationStateInactive) {
        NSLog(@"UIApplicationStateInactive");
        [self showSurveyForNotification:activeNotification];
      } else if (state == UIApplicationStateActive) {
        NSLog(@"UIApplicationStateActive");
        [self presentForegroundNotification:activeNotification];
      }
    }
  }
}


- (void)showNoSurveyNeededForNotification:(UILocalNotification*)notification {
  DDLogInfo(@"Show a banner of No Survey Needed");
  [JCNotificationCenter sharedCenter].presenter = [JCNotificationBannerPresenterSmokeStyle new];
  NSString* format = @"This notification has expired.\n"
                     @"(It's notifications expire after %d minutes.)";
  NSString* message = [NSString stringWithFormat:format, [notification pacoTimeoutMinutesExt]];
  JCNotificationBanner* banner = [[JCNotificationBanner alloc] initWithTitle:[notification pacoExperimentTitleExt]
                                                                     message:message
                                                                     timeout:7.
                                                                  tapHandler:nil];
  [[JCNotificationCenter sharedCenter] enqueueNotification:banner];
}


/*
     fetches the experiment from the modal and displays notification
 */
- (void)showSurveyForNotification:(UILocalNotification*)notification {
  dispatch_async(dispatch_get_main_queue(), ^{

      
      // Defer to GUI implimentations.
      /*
           NSString *experimentId = [notification pacoExperimentIdExt];
    NSAssert(experimentId.length > 0, @"experimentId should be a valid string!");
    PacoExperiment *experiment = [[PacoClient sharedInstance].model experimentForId:experimentId];
    PacoQuestionScreenViewController *questions =
        [PacoQuestionScreenViewController controllerWithExperiment:experiment andNotification:notification];
    UINavigationController* navi = self.viewController.navigationController;
    [navi pushViewController:questions animated:NO];
       */
  });

}


/*
  ..., shows survay in async
 */
- (void)presentForegroundNotification:(UILocalNotification*)notification {
  NSAssert([notification pacoStatusExt] != PacoNotificationStatusTimeout, @"should not be timeout");
  [JCNotificationCenter sharedCenter].presenter = [JCNotificationBannerPresenterSmokeStyle new];
  [JCNotificationCenter enqueueNotificationWithTitle:@""
                                             message:notification.alertBody
                                          tapHandler:^{
                                            [self showSurveyForNotification:notification];
                                          }];
}

/*
   processes the notification from launch, mustShowSurvey== YES;
 
 */
- (void)processNotificationIfNeeded {
    
  if (self.notificationFromAppLaunch) {
    DDLogVerbose(@"Start processing notification received from app launch");
    [self processReceivedNotification:self.notificationFromAppLaunch mustShowSurvey:YES];
    self.notificationFromAppLaunch = nil;
  }
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {

      [[PacoNetwork sharedInstance] update];
    
    
    
    
     self.testViewController   = [[ScheduleTestViewController alloc]  initWithNibName:@"ScheduleTestViewController" bundle:nil];
    
     self.testTableViewController = [[PacoTableExperimentsController alloc] initWithNibName:@"PacoTableExperimentsController" bundle:nil];
    
    
    
  // per documents stir it is not required to invoke stir
  // arc4random_stir();
    
  [DDLog addLogger:[DDASLLogger sharedInstance]];
  [DDLog addLogger:[DDTTYLogger sharedInstance]];
  DDFileLogger* logger = [[DDFileLogger alloc] init];
  logger.rollingFrequency = 2 * 24 * 60 * 60; //48 hours rolling
  logger.logFileManager.maximumNumberOfLogFiles = 7;
  [DDLog addLogger:logger];
    
   _isFirstLaunch = [self markIsFirstLoaunched];
    
    
    /* register for notification */
  if (floor(NSFoundationVersionNumber) > NSFoundationVersionNumber_iOS_7_1 ||
      
        [UIApplication instancesRespondToSelector:@selector(registerUserNotificationSettings:)]) {
        UIUserNotificationType types = UIUserNotificationTypeBadge | UIUserNotificationTypeSound | UIUserNotificationTypeAlert;
      
        UIUserNotificationSettings *mySettings = [UIUserNotificationSettings settingsForTypes:types categories:nil];
        [application registerUserNotificationSettings:mySettings];
  }
  
    
    
    
    
     self.myExperiments =[[PacoMyExperiments alloc] initWithNibName:@"PacoMyExperiments" bundle:nil];
     self.hub  = [[PacoMyExperiments alloc] initWithNibName:@"PacoMyExperiments" bundle:nil];
    
    
    
    
    self.swiftViewController = [[PacoMainSwiftViewController alloc] initWithNibName:@"PacoMainSwiftViewController" bundle:nil];
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    
     [self makeTabBar];
 
    PAExperimentDAO * experiment =  [TestUtil buildExperiment:dataSource];

    
    
    
    id<JavaUtilList>  inputs =  [PAExperimentHelper getInputsWithPAExperimentDAO:experiment];
    PAInput2* input;
    for(input in inputs)
    {
        NSLog(@" name : %@", [input getName]);
        NSLog(@" name : %@", [input getResponseType]);
        
    }
    
    
   // PAExperimentGroup  group = experiment
    
    
    self.responseMessageController = [[PacoResponseTableViewController alloc] initWithNibName:@"PacoResponseTableViewController" bundle:nil  input:inputs];
   
   
    
      // self.window.rootViewController = self.responseMessageController ;
       self.window.rootViewController = self.tabBar ;
    
      [self.window makeKeyAndVisible];
  
  
  UILocalNotification *notification = launchOptions[UIApplicationLaunchOptionsLocalNotificationKey];

  if (notification) {
      
      /*  save notification to be handled later */
    self.notificationFromAppLaunch = notification;
  }
  return YES;
}





/*
     perform backgroun action. 
 
 
*/

- (void)application:(UIApplication *)application performFetchWithCompletionHandler:(void(^)(UIBackgroundFetchResult))completionHandler {

    [[PacoNetwork sharedInstance] update];

}

- (void) makeTabBar {
    
    
    self.joinedExperiment =
         [[PacoJoinedExperimentsController alloc] initWithNibName:@"PacoJoinedExperimentsController" bundle:nil];

    
    self.configController = [[PacoConfigController alloc] initWithNibName:@"PacoConfigController" bundle:nil];
    
    
     NSMutableArray *tabViewControllers = [[NSMutableArray alloc] init];
    [tabViewControllers addObject:[[UINavigationController alloc] initWithRootViewController:self.myExperiments]];
    [tabViewControllers addObject:[[UINavigationController alloc] initWithRootViewController:self.joinedExperiment]];
    [tabViewControllers addObject:[[UINavigationController alloc] initWithRootViewController:self.configController]];
    
    
    
    
    self.tabBar = [[UITabBarController alloc] init];
    

    
    
    self.myExperiments.title = @"Invitations";
    self.joinedExperiment.title =@"Joined";
    self.configController.title =@"Config";
     self.hub.title =@"Hub";
    
    [self.tabBar addChildViewController:[[UINavigationController alloc] initWithRootViewController:self.myExperiments]];
        [self.tabBar addChildViewController:[[UINavigationController alloc] initWithRootViewController:self.hub]];
    
    
    [self.tabBar addChildViewController:[[UINavigationController alloc] initWithRootViewController:self.joinedExperiment]];
      
    [self.tabBar addChildViewController:[[UINavigationController alloc] initWithRootViewController:self.configController]];
    
    
    
 
    
    self.myExperiments.tabBarItem.image = [UIImage imageNamed:@"animal-element-7.png" ];
    
    self.joinedExperiment.tabBarItem.image = [UIImage imageNamed:@"business-target-7.png" ];
     self.hub.tabBarItem.image = [UIImage imageNamed:@"gift-7.png" ];
 
    self.configController.tabBarItem.image =[UIImage imageNamed:@"gear-7.png" ];
    
    
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
  DDLogInfo(@"==========  Application applicationDidBecomeActive  ==========");
    
    
 // [[PacoClient sharedInstance] uploadPendingEventsInBackground];
  
  // [[NSNotificationCenter defaultCenter] postNotificationName:kPacoNotificationAppBecomeActive
      //                                                object:nil];
    
  // it doesn;t apper that didbecome active is used.
}

- (void)applicationWillResignActive:(UIApplication *)application {
    
    [[PacoMediator sharedInstance] cleanup];
    
   }

- (void)applicationWillTerminate:(UIApplication *)application {
 
}


// start background tasks
- (void)applicationDidEnterBackground:(UIApplication *)application {
    
  //http request will time out in 20 seconds, we need to request a little bit more time to allow
  //it finish, so we use UIBackgroundTaskIdentifier to request some more time to finish up
  __block UIBackgroundTaskIdentifier bgTask =
      [[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:^{
        // Clean up any unfinished task business by marking where you
        // stopped or ending the task outright.
        [[UIApplication sharedApplication] endBackgroundTask:bgTask];
        bgTask = UIBackgroundTaskInvalid;
      }];
  
  // Start the long-running task and return immediately.
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    // Do the work associated with the task, preferably in chunks.
    DDLogInfo(@"Waiting for possible http requests to be finished ...");
    sleep(30);
    DDLogInfo(@"Wake up and will end background task");
    [[UIApplication sharedApplication] endBackgroundTask:bgTask];
    bgTask = UIBackgroundTaskInvalid;
  });
    
    
    
}


/* 
   major tasks includes scheduling new tasks.
 
 */
- (void)applicationWillEnterForeground:(UIApplication *)application {
    
    
 // [[PacoClient sharedInstance] executeRoutineMajorTaskIfNeeded];
}


#pragma mark - Core Data stack

@synthesize managedObjectContext = _managedObjectContext;
@synthesize managedObjectModel = _managedObjectModel;
@synthesize persistentStoreCoordinator = _persistentStoreCoordinator;

- (NSURL *)applicationDocumentsDirectory {
    // The directory the application uses to store the Core Data store file. This code uses a directory named "paco.pacoModal" in the application's documents directory.
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
}

- (NSManagedObjectModel *)managedObjectModel {
    // The managed object model for the application. It is a fatal error for the application not to be able to find and load its model.
    if (_managedObjectModel != nil) {
        return _managedObjectModel;
    }
    NSURL *modelURL = [[NSBundle mainBundle] URLForResource:@"PacoModel" withExtension:@"momd"];
    _managedObjectModel = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];
    return _managedObjectModel;
}

- (NSPersistentStoreCoordinator *)persistentStoreCoordinator {
    // The persistent store coordinator for the application. This implementation creates and return a coordinator, having added the store for the application to it.
    if (_persistentStoreCoordinator != nil) {
        return _persistentStoreCoordinator;
    }
    
    // Create the coordinator and store
    
    _persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];
    NSURL *storeURL = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"PacoModel.sqlite"];
    NSError *error = nil;
    NSString *failureReason = @"There was an error creating or loading the application's saved data.";
    if (![_persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:nil error:&error]) {
        // Report any error we got.
        NSMutableDictionary *dict = [NSMutableDictionary dictionary];
        dict[NSLocalizedDescriptionKey] = @"Failed to initialize the application's saved data";
        dict[NSLocalizedFailureReasonErrorKey] = failureReason;
        dict[NSUnderlyingErrorKey] = error;
        error = [NSError errorWithDomain:@"YOUR_ERROR_DOMAIN" code:9999 userInfo:dict];
        // Replace this with code to handle the error appropriately.
        // abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
        abort();
    }
    
    return _persistentStoreCoordinator;
}


- (NSManagedObjectContext *)managedObjectContext {
    // Returns the managed object context for the application (which is already bound to the persistent store coordinator for the application.)
    if (_managedObjectContext != nil) {
        return _managedObjectContext;
    }
    
    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator];
    if (!coordinator) {
        return nil;
    }
    _managedObjectContext = [[NSManagedObjectContext alloc] init];
    [_managedObjectContext setPersistentStoreCoordinator:coordinator];
    return _managedObjectContext;
}

#pragma mark - Core Data Saving support

- (void)saveContext {
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil) {
        NSError *error = nil;
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error]) {
            // Replace this implementation with code to handle the error appropriately.
            // abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        }
    }
}


#pragma mark - run before flags 

- (BOOL) markIsFirstLoaunched  {
    
    BOOL retVal = NO;
    NSString* launchedKey = @"paco_launched";
    id value = [[NSUserDefaults standardUserDefaults] objectForKey:launchedKey];
    if (value == nil) {
        [[NSUserDefaults standardUserDefaults] setObject:@YES forKey:launchedKey];
        [[NSUserDefaults standardUserDefaults] synchronize];
        retVal = YES;
    }
    
    return retVal;
}

- (void)markIsFirstAuth2
{
    
    
    DDLogInfo(@"PacoClient-- checkIfUserFirstLaunchOAuth2 ");
    NSString* launchedKey = @"oauth2_launched";
    id value = [[NSUserDefaults standardUserDefaults] objectForKey:launchedKey];
    if (value == nil) { //first launch
        [[NSUserDefaults standardUserDefaults] setObject:@YES forKey:launchedKey];
        [[NSUserDefaults standardUserDefaults] synchronize];
        _isFirstOAuth2 = YES;
    } else {
        _isFirstOAuth2 = NO;
    }
}


@end
