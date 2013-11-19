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
#import "PacoLocation.h"
#import "JCNotificationCenter.h"
#import "JCNotificationBannerPresenterSmokeStyle.h"
#import "PacoEventManager.h"
#import "UILocalNotification+Paco.h"

@implementation PacoAppDelegate

- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification {
  NSLog(@"==========  Application didReceiveLocalNotification  ==========");
  [self processReceivedNotification:notification];
}

- (void)processReceivedNotification:(UILocalNotification*)notification {
  NSLog(@"Detail: %@", [notification pacoDescription]);
  
  UILocalNotification* activeNotification = notification;
  if (![[PacoClient sharedInstance].scheduler isNotificationActive:activeNotification]) {
    NSLog(@"Notification is not active anymore, cancelling it from the tray...");
    [UILocalNotification pacoCancelLocalNotification:activeNotification];
    activeNotification =
        [[PacoClient sharedInstance].scheduler activeNotificationForExperiment:[notification pacoExperimentId]];
    if (activeNotification) {
      NSLog(@"Active Notification Detected: %@", [activeNotification pacoDescription]);
    } else {
      NSLog(@"No Active Notification Detected. ");
    }
  }
  
  UIApplicationState state = [[UIApplication sharedApplication] applicationState];
  if (activeNotification == nil) {
    if (state == UIApplicationStateInactive) {
      [self showNoSurveyNeeded];
    } else {
      NSLog(@"Ignore this notfication");
    }
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


- (void)showNoSurveyNeeded {
  [JCNotificationCenter sharedCenter].presenter = [JCNotificationBannerPresenterSmokeStyle new];
  NSString* message = @"No need to fill out any survey at this moment for this experiment.";
  [JCNotificationCenter enqueueNotificationWithTitle:@""
                                             message:message
                                          tapHandler:nil];
}

- (void)showSurveyForNotification:(UILocalNotification*)notification {
  //If there is any view popped up, dismiss it and show a question view 
  UINavigationController* navi = self.viewController.navigationController;
  [navi popToRootViewControllerAnimated:NO];
  
  NSString *experimentId = [notification pacoExperimentId];
  NSAssert(experimentId.length > 0, @"experimentId should be a valid string!");
  PacoExperiment *experiment = [[PacoClient sharedInstance].model experimentForId:experimentId];
  PacoQuestionScreenViewController *questions =
      [PacoQuestionScreenViewController controllerWithExperiment:experiment andNotification:notification];
  [navi pushViewController:questions animated:NO];
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

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  // Stir!
  arc4random_stir();
  
  //YMZ:TODO: set the badge number to 0 may clear all notifications in the tray
  //http://stackoverflow.com/questions/7773584/can-i-programmatically-clear-my-apps-notifications-from-the-ios-5-notification
  // Clear all Application Badges  
//  application.applicationIconBadgeNumber = 0;
  
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
    NSLog(@"==========  Application didFinishLaunchingWithOptions: One Notification ==========");
    [self processReceivedNotification:notification];
  } else {
    NSLog(@"==========  Application didFinishLaunchingWithOptions: No Notification ==========");
  }
  return YES;
}

- (void)application:(UIApplication *)application performFetchWithCompletionHandler:(void(^)(UIBackgroundFetchResult))completionHandler {
  NSLog(@"==========  Application Background Fetch Working ==========");
  
  [[PacoClient sharedInstance] executeRoutineMajorTaskIfNeeded];
  
  UIBackgroundFetchResult result = UIBackgroundFetchResultNewData;
  completionHandler(result);
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
  NSLog(@"==========  Application applicationDidBecomeActive  ==========");
  [[PacoClient sharedInstance] uploadPendingEventsInBackground];
}

- (void)applicationWillResignActive:(UIApplication *)application {
  NSLog(@"==========  Application applicationWillResignActive  ==========");

  BOOL success = [[PacoClient sharedInstance].model saveToFile];
  if (success) {
    NSLog(@"Successfully saved model!");
  } else {
    NSLog(@"Failed to save model!");
  }
}

- (void)applicationWillTerminate:(UIApplication *)application {
  NSLog(@"==========  Application applicationWillTerminate  ==========");
  
  BOOL success = [[PacoClient sharedInstance].model saveToFile];
  if (success) {
    NSLog(@"Successfully saved model!");
  } else {
    NSLog(@"Failed to save model!");
  }
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
  NSLog(@"==========  Application applicationDidEnterBackground  ==========");
  if ([PacoClient sharedInstance].location != nil) {
    [[PacoClient sharedInstance].location enableLocationService];
  }
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
  NSLog(@"==========  Application applicationWillEnterForeground  ==========");
  [[PacoClient sharedInstance] executeRoutineMajorTaskIfNeeded];
}

@end
