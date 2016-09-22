/* Copyright 2015  Google
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
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
#import "PacoConfigurationViewController.h"
#import "PacoTestCustomFeedBackViewController.h" 
#import "PacoExperiment.h" 


//#import <GoogleSignIn/GoogleSignIn.h>
#import "HubPaginatedTableViewController.h"

/* lets pull in all the swift code */
#import "Paco-Swift.h"

#import "PacoJoinSummary.h"
#import "NSObject+J2objcKVO.h"





@implementation PacoAppDelegate




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
      
       NSString *experimentId = [notification pacoExperimentIdExt];
       NSAssert(experimentId.length > 0, @"experimentId should be a valid string!");

     // [[PacoMediator sharedInstance] refreshRunningExperiments];
      
      PAExperimentDAO* dao =  [[PacoMediator sharedInstance ] experimentForId:experimentId];
      PacoExperiment * experiment =  [PacoExperiment experimentWithExperimentDao:dao];
      
      
       NSString* groupName = notification.userInfo[kNotificationGroupName];
      
      
       PAExperimentGroup* group =  [dao getGroupByNameWithNSString:groupName];

       PacoQuestionScreenViewController *questions =
       [PacoQuestionScreenViewController controllerWithExperiment:experiment  group:group andNotification:notification];
      
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
    
    
    
 
    
    self.alertTester = [[PacoNotificationTester alloc] initWithNibName:@"PacoNotificationTester" bundle:nil];
    
    
    self.swiftViewController = [[PacoMainSwiftViewController alloc] initWithNibName:@"PacoMainSwiftViewController" bundle:nil];
    
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    [self makeTabBar];
    
    
    _pacoView = [[UIImageView alloc] init];
    
 
    
   // let height = UIApplication.sharedApplication().statusBarFrame.height +
   // self.navigationController!.navigationBar.frame.height;
 
    
    self.window.rootViewController = [[UINavigationController alloc] initWithRootViewController: self.tabBar];
    
 
    
    self.viewController =  self.tabBar;
    

    
    
    
    [self.window makeKeyAndVisible];
    [[PacoNetwork sharedInstance] update];
    
    
    _pacoView.image =  [UIImage imageNamed:@"paco32"];
    
    
    CGRect rect =  CGRectMake(32+43,  20+ (44-32)/2 , 32, 32);
    _pacoView.frame = rect;
    
    //_pacoView.center = self.viewController.navigationController.view.center;
    
    
    [self.window addSubview:_pacoView];
    
   int existingCount = [PacoMediator sharedInstance].startedExperiments.count;
    
    if(existingCount > 0)
    {
        
        [self.tabBar setSelectedIndex:2];
        
    }
    
    
    
  
  
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
    self.configurationManager = [[PacoConfigurationViewController  alloc] initWithNibName:@"PacoConfigurationViewController" bundle:nil];
    self.hub  = [[PacoMyExperiments alloc] initWithNibName:@"PacoMyExperiments" bundle:nil];
    self.pg = [[HubPaginatedTableViewController alloc] initWithNibName:@"HubPaginatedTableViewController" bundle:nil];
    
    self.myExperiments =[[PacoMyExperiments alloc] initWithNibName:@"PacoMyExperiments" bundle:nil];
    self.publicExperiments  =[[PacoHubExperiments alloc] initWithNibName:@"PacoHubExperiments" bundle:nil];
    
    
     self.tabBar = [[UITabBarController alloc] init];

    [self.tabBar addChildViewController:self.myExperiments];
    [self.tabBar addChildViewController:self.pg];
    [self.tabBar addChildViewController:self.joinedExperiment];
    [self.tabBar addChildViewController:self.configurationManager];
    
    
    self.myExperiments.tabBarItem.title = @"Invitations";
    self.joinedExperiment.tabBarItem.title = @"Joined";
    self.configurationManager.tabBarItem.title = @"Config";
    self.pg.tabBarItem.title = @"Hub";
    

 
    
   // self.myExperiments.tabBarItem.image = [UIImage imageNamed:@"animal-element-7.png" ];
    
    //self.myExperiments.tabBarItem.image = [UIImage imageNamed:@"animal-element-7.png" ];
    
    
     self.myExperiments.tabBarItem.image = [UIImage imageNamed:@"invitations.png" ];
     self.joinedExperiment.tabBarItem.image = [UIImage imageNamed:@"joined.png" ];
    
    // self.joinedExperiment.tabBarItem.image = [UIImage imageNamed:@"business-target-7.png" ];
    
    self.joinedExperiment.tabBarItem.image = [UIImage imageNamed:@"joined.png" ];
    self.pg.tabBarItem.image = [UIImage imageNamed:@"gift-7.png" ];
    
    self.pg.tabBarItem.image = [UIImage imageNamed:@"hub.png" ];
   // self.configurationManager.tabBarItem.image =[UIImage imageNamed:@"gear-7.png" ];
    
    self.configurationManager.tabBarItem.image =[UIImage imageNamed:@"configuration.png" ];
    
    
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
  DDLogInfo(@"==========  Application applicationDidBecomeActive  ==========");
    
 
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
