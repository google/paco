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
#import "PacoNetwork.h"
#import "ExperimentHelper.h"
#import "java/util/ArrayList.h"
#import "java/util/List.h"
#import "Input2.h"
#import "TestUtil.h"
#import "PacoTestViewer.h"
 #import "PacoNotificationTester.h"
#import  "PacoQuestionScreenViewController.h"
#import "PacoMediator.h"
#import "PacoExperiment.h"


//#import <GoogleSignIn/GoogleSignIn.h>
#import "HubPaginatedTableViewController.h"

/* lets pull in all the swift code */
#import "Paco-Swift.h"

#import "PacoJoinSummary.h"






@implementation PacoAppDelegate



static NSString *dataSource = @"{\r\n  \"title\": \"Everything Demo New\",\r\n  \"description\": \"\",\r\n  \"creator\": \"bobevans999@gmail.com\",\r\n  \"contactEmail\": \"bobevans999@gmail.com\",\r\n  \"id\": 6139552436584448,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2016\/04\/21\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"bobevans999@gmail.com\",\r\n    \"rbe5000@gmail.com\",\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 5,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"default\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 1640220000,\r\n              \"timeout\": 59,\r\n              \"delay\": 5000,\r\n              \"color\": 0,\r\n              \"dismissible\": true,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 27337,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 43200000,\r\n                  \"basis\": 0,\r\n                  \"offsetTimeMillis\": 1800000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [\r\n        {\r\n          \"name\": \"smiley\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"likert_smileys\",\r\n          \"text\": \"How happy are you?\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"likert\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"likert\",\r\n          \"text\": \"How frequently do you feel this way?\",\r\n          \"likertSteps\": 5,\r\n          \"leftSideLabel\": \"Never\",\r\n          \"rightSideLabel\": \"Always\",\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"color\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"What is your favorite color?\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"slist\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"list\",\r\n          \"text\": \"Choose one\",\r\n          \"listChoices\": [\r\n            \"Almond\",\r\n            \"Macadamia\",\r\n            \"Pecan\"\r\n          ],\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"multi\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"list\",\r\n          \"text\": \"Choose 1 or more\",\r\n          \"listChoices\": [\r\n            \"Vodka\",\r\n            \"Beer\",\r\n            \"Wine\"\r\n          ],\r\n          \"multiselect\": true,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"num\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"number\",\r\n          \"text\": \"Pick a number\",\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"location\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"location\",\r\n          \"text\": \"\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": true,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"photo\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"photo\",\r\n          \"text\": \"Take a photo\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": true,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"scale_for_conditional\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"likert\",\r\n          \"text\": \"This is a scale. Pick a value > 3 to trigger conditional question.\",\r\n          \"likertSteps\": 5,\r\n          \"leftSideLabel\": \"Not at all\",\r\n          \"rightSideLabel\": \"totally\",\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"conditioned_on_scale\",\r\n          \"required\": false,\r\n          \"conditional\": true,\r\n          \"conditionExpression\": \"scale_for_conditional > 3\",\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"Why did you pick a value greater than 3?\",\r\n          \"likertSteps\": 5,\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        }\r\n      ],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 1,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 1,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";


static NSString * const kClientID =
      @"795968041191-j3suruarvos24eusa1ls0af9thb11b7s.apps.googleusercontent.com";



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
     
       NSString *experimentId = [notification pacoExperimentIdExt];
       NSAssert(experimentId.length > 0, @"experimentId should be a valid string!");

      [[PacoMediator sharedInstance] refreshRunningExperiments];
      
     PAExperimentDAO* dao =  [[PacoMediator sharedInstance ] experimentForId:experimentId];
 
      
     PacoExperiment * experiment =  [PacoExperiment experimentWithExperimentDao:dao];
 
     // [PacoExperiment experimentWithExperimentDao:dao];
     //  PacoExperiment *experiment = [[PacoClient sharedInstance].model experimentForId:experimentId];
      
      
      
    PacoQuestionScreenViewController *questions =
        [PacoQuestionScreenViewController controllerWithExperiment:experiment andNotification:notification];
      
    UINavigationController* navi = self.viewController.navigationController;
      
    [navi  pushViewController:questions animated:NO];
      
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

    
    
    NSError* configureError;
    
    
 
    NSAssert(!configureError, @"Error configuring Google services: %@", configureError);
    
      self.scheduleEditor      =  [[ScheduleEditor alloc] initWithNibName:@"ScheduleEditor"  bundle:nil];
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
  
 
    
     self.testPagination = [[PacoTestViewer alloc] initWithNibName:@"PacoTestViewer" bundle:nil];
     self.swiftTest = [[PacoTestingControllerTableViewController alloc] initWithNibName:@"PacoTestingControllerTableViewController" bundle:nil];
    
    
    
     self.myExperiments =[[PacoMyExperiments alloc] initWithNibName:@"PacoMyExperiments" bundle:nil];
     self.publicExperiments  =[[PacoHubExperiments alloc] initWithNibName:@"PacoHubExperiments" bundle:nil];
    
    
     self.hub  = [[PacoMyExperiments alloc] initWithNibName:@"PacoMyExperiments" bundle:nil];
    
      self.pg = [[HubPaginatedTableViewController alloc] initWithNibName:@"HubPaginatedTableViewController" bundle:nil];
    
   self.alertTester = [[PacoNotificationTester alloc] initWithNibName:@"PacoNotificationTester" bundle:nil];
    
    
    self.swiftViewController = [[PacoMainSwiftViewController alloc] initWithNibName:@"PacoMainSwiftViewController" bundle:nil];
    
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    
     [self makeTabBar];
 
    //PAExperimentDAO * experiment =  [TestUtil buildExperiment:dataSource];

    
    
//    
//    //id<JavaUtilList>  inputs =  [PAExperimentHelper getInputsWithPAExperimentDAO:experiment];
//   // PAInput2* input;
//    for(input in inputs)
//    {
//        NSLog(@" name : %@", [input getName]);
//        NSLog(@" name : %@", [input getResponseType]);
//        
//    }
    
    
   // PAExperimentGroup  group = experiment
    
//    
//    self.responseMessageController = [[PacoResponseTableViewController alloc] initWithNibName:@"PacoResponseTableViewController" bundle:nil  input:inputs];
   
   
    self.goController  = [[PacoJoinSummary alloc] initWithNibName:@"PacoJoinSummary" bundle:nil];
    
    
    
       // self.window.rootViewController = self.responseMessageController ;
      //  self.window.rootViewController = self.goController ;
    
    // self.window.rootViewController = self.tabBar ;
    
      // self.window.rootViewController = self.testViewController;
     self.window.rootViewController = [[UINavigationController alloc] initWithRootViewController:self.alertTester];
    
   
    
     self.viewController =  self.alertTester;
    
    
    [self.window makeKeyAndVisible];
    
    
       PAExperimentDAO * experiment =  [TestUtil buildExperiment:dataSource];
      self.scheduleEditor.cells = [experiment getTableCellModelObjects];
    
     // self.window.rootViewController = self.scheduleEditor;
    
     //
    
     [[PacoNetwork sharedInstance] update];
  
  
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
    

    self.pg.title = @"Hub";
    self.myExperiments.title = @"Invitations";
    self.joinedExperiment.title =@"Joined";
    self.configController.title =@"Config";
    self.hub.title =@"Hub";
    self.testPagination.title=@"pagination";
    
    self.swiftTest.title  =@"THE HUB";
    
    [self.tabBar addChildViewController:[[UINavigationController alloc] initWithRootViewController:self.myExperiments]];
    [self.tabBar addChildViewController:[[UINavigationController alloc] initWithRootViewController:self.pg]];
    
    
    [self.tabBar addChildViewController:[[UINavigationController alloc] initWithRootViewController:self.joinedExperiment]];
    [self.tabBar addChildViewController:[[UINavigationController alloc] initWithRootViewController:self.configController]];
    
    
    
 
    
    self.myExperiments.tabBarItem.image = [UIImage imageNamed:@"animal-element-7.png" ];
    
    self.joinedExperiment.tabBarItem.image = [UIImage imageNamed:@"business-target-7.png" ];
     self.pg.tabBarItem.image = [UIImage imageNamed:@"gift-7.png" ];
 
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
