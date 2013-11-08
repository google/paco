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
  NSLog(@"Detail: %@", [notification description]);
  
  PacoNotificationStatus status = [notification pacoStatus];
  if (status == PacoNotificationStatusTimeout) {
    NSLog(@"Warning: A time out notification was received!");
    return;
  }
  NSLog(@"Notification Status: %@", [notification pacoStatusDescription]);
  
  UIApplicationState state = [application applicationState];
  if (state == UIApplicationStateInactive) {
    //if this is called when application is in background, we should show the question view directly.
    NSLog(@"UIApplicationStateInactive");
    [self showSurveyForNotification:notification];
  } else if (state == UIApplicationStateActive) {
    NSLog(@"UIApplicationStateActive");
    [self presentForegroundNotification:notification];
  }
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
  PacoNotificationStatus status = [notification pacoStatus];
  if (status == PacoNotificationStatusTimeout) {
    NSLog(@"Warning: A time out notification was received!");
    return;
  }
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
    NSLog(@"Detail: %@", [notification description]);
    [self presentForegroundNotification:notification];
  } else {
    NSLog(@"==========  Application didFinishLaunchingWithOptions: No Notification ==========");
  }
  return YES;
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
  NSLog(@"==========  Application applicationDidBecomeActive  ==========");
}

- (void)applicationWillResignActive:(UIApplication *)application {
  NSLog(@"==========  Application applicationWillResignActive  ==========");

  BOOL success = [[PacoClient sharedInstance].model saveToFile];
  success = success && [[PacoClient sharedInstance].scheduler saveNotificationsToFile];
  if (success) {
    NSLog(@"SUCCESSFULLY SAVED TO FILE");
  } else {
    NSLog(@"FAILED TO SAVE TO FILE");
  }
  [[PacoClient sharedInstance].eventManager saveDataToFile];
}

- (void)applicationWillTerminate:(UIApplication *)application {
  NSLog(@"==========  Application applicationWillTerminate  ==========");
  
  BOOL success = [[PacoClient sharedInstance].model saveToFile];
  if (success) {
    NSLog(@"Successfully saved model!");
  } else {
    NSLog(@"Failed to save model!");
  }
  success = [[PacoClient sharedInstance].scheduler saveNotificationsToFile];
  if (success) {
    NSLog(@"Successfully saved notifications!");
  } else {
    NSLog(@"Failed to save notifications!");
  }
  [[PacoClient sharedInstance].eventManager saveDataToFile];
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
  NSLog(@"==========  Application applicationDidEnterBackground  ==========");
  if ([PacoClient sharedInstance].location != nil) {
    [[PacoClient sharedInstance].location enableLocationService];
  }
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
  NSLog(@"==========  Application applicationWillEnterForeground  ==========");
}

@end
