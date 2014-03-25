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
#import "PacoClient.h"
#import "PacoColor.h"
#import "PacoMainViewController.h"
#import "PacoQuestionScreenViewController.h"
#import "PacoScheduler.h"
#import "JCNotificationCenter.h"
#import "JCNotificationBannerPresenterSmokeStyle.h"
#import "PacoEventManager.h"
#import "UILocalNotification+Paco.h"
#import "DDLog.h"
#import "DDASLLogger.h"
#import "DDFileLogger.h"
#import "DDTTYLogger.h"

@implementation PacoAppDelegate

- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification {
  DDLogInfo(@"==========  Application didReceiveLocalNotification  ==========");
  [self processReceivedNotification:notification mustShowSurvey:NO];
}

- (void)processReceivedNotification:(UILocalNotification*)notification mustShowSurvey:(BOOL)mustShowSurvey {
  if (!notification) {
    DDLogWarn(@"Ignore a nil notification");
    return;
  }
  DDLogInfo(@"Detail: %@", [notification pacoDescription]);
  UILocalNotification* activeNotification = notification;
  if (![[PacoClient sharedInstance].scheduler isNotificationActive:activeNotification]) {
    DDLogInfo(@"Notification is not active anymore, cancelling it from the tray...");
    [UILocalNotification pacoCancelLocalNotification:activeNotification];
    activeNotification =
        [[PacoClient sharedInstance].scheduler activeNotificationForExperiment:[notification pacoExperimentId]];
    if (activeNotification) {
      DDLogInfo(@"Active Notification Detected: %@", [activeNotification pacoDescription]);
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
  JCNotificationBannerPresenterSmokeStyle* style = [[JCNotificationBannerPresenterSmokeStyle alloc] initWithMessageFont:[UIFont fontWithName:@"HelveticaNeue" size:14]];
  [JCNotificationCenter sharedCenter].presenter = style;
  
  NSString* format = @"This notification has expired.\n"
                     @"(It's notifications expire after %d minutes.)";
  NSString* message = [NSString stringWithFormat:format, [notification pacoTimeoutMinutes]];
  JCNotificationBanner* banner = [[JCNotificationBanner alloc] initWithTitle:[notification pacoExperimentTitle]
                                                                     message:message
                                                                     timeout:7.
                                                                  tapHandler:nil];
  [[JCNotificationCenter sharedCenter] enqueueNotification:banner];
}

- (void)showSurveyForNotification:(UILocalNotification*)notification {
  dispatch_async(dispatch_get_main_queue(), ^{
    NSString *experimentId = [notification pacoExperimentId];
    NSAssert(experimentId.length > 0, @"experimentId should be a valid string!");
    PacoExperiment *experiment = [[PacoClient sharedInstance].model experimentForId:experimentId];
    PacoQuestionScreenViewController *questions =
        [PacoQuestionScreenViewController controllerWithExperiment:experiment andNotification:notification];
    UINavigationController* navi = self.viewController.navigationController;
    [navi pushViewController:questions animated:NO];
  });

}

- (void)presentForegroundNotification:(UILocalNotification*)notification {
  NSAssert([notification pacoStatus] != PacoNotificationStatusTimeout, @"should not be timeout");
  [JCNotificationCenter sharedCenter].presenter = [JCNotificationBannerPresenterSmokeStyle new];
  [JCNotificationCenter enqueueNotificationWithTitle:@""
                                             message:notification.alertBody
                                          tapHandler:^{
                                            [self showSurveyForNotification:notification];
                                          }];
}


- (void)processNotificationIfNeeded {
  if (self.notificationFromAppLaunch) {
    DDLogVerbose(@"Start processing notification received from app launch");
    [self processReceivedNotification:self.notificationFromAppLaunch mustShowSurvey:YES];
    self.notificationFromAppLaunch = nil;
  }
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  // Stir!
  arc4random_stir();
  
  [DDLog addLogger:[DDASLLogger sharedInstance]];
  [DDLog addLogger:[DDTTYLogger sharedInstance]];
  DDFileLogger* logger = [[DDFileLogger alloc] init];
  logger.rollingFrequency = 2 * 24 * 60 * 60; //48 hours rolling
  logger.logFileManager.maximumNumberOfLogFiles = 7;
  [DDLog addLogger:logger];
  
  // Override the navigation bar and item tint color globally across the app.
  [[UINavigationBar appearance] setTintColor:[PacoColor pacoBlue]];

  self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
  // Override point for customization after application launch.
  if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
    self.viewController = [[PacoMainViewController alloc] initWithNibName:nil bundle:nil];
  } else {
    self.viewController = [[PacoMainViewController alloc] initWithNibName:nil bundle:nil];
  }

  self.window.rootViewController = [[UINavigationController alloc] initWithRootViewController:self.viewController];
  [self.window makeKeyAndVisible];
  
  
  UILocalNotification *notification = [launchOptions objectForKey:UIApplicationLaunchOptionsLocalNotificationKey];
  if (notification) {
    DDLogInfo(@"==========  Application didFinishLaunchingWithOptions: One Notification ==========");
    DDLogVerbose(@"The following notification will be processed after notification system is initialized:\n%@",
                 [notification pacoDescription]);
    self.notificationFromAppLaunch = notification;
  } else {
    DDLogInfo(@"==========  Application didFinishLaunchingWithOptions: No Notification ==========");
  }
  
  return YES;
}

- (void)application:(UIApplication *)application performFetchWithCompletionHandler:(void(^)(UIBackgroundFetchResult))completionHandler {
  DDLogInfo(@"==========  Application Background Fetch Working ==========");
  
  [[PacoClient sharedInstance] backgroundFetchStartedWithBlock:completionHandler];
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
  DDLogInfo(@"==========  Application applicationDidBecomeActive  ==========");
  [[PacoClient sharedInstance] uploadPendingEventsInBackground];
  
  [[NSNotificationCenter defaultCenter] postNotificationName:PacoAppBecomeActive object:nil];
}

- (void)applicationWillResignActive:(UIApplication *)application {
  DDLogInfo(@"==========  Application applicationWillResignActive  ==========");
}

- (void)applicationWillTerminate:(UIApplication *)application {
  DDLogInfo(@"==========  Application applicationWillTerminate  ==========");
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
  DDLogInfo(@"==========  Application applicationDidEnterBackground  ==========");
  
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

- (void)applicationWillEnterForeground:(UIApplication *)application {
  DDLogInfo(@"==========  Application applicationWillEnterForeground, start executing routine major task if needed  ==========");
  [[PacoClient sharedInstance] executeRoutineMajorTaskIfNeeded];
}

@end
