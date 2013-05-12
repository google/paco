//
//  AppDelegate.m
//  LocalNotification
//
//  Created by Tom Pennings on 12/3/12.
//  Copyright (c) 2012 Lautumar. All rights reserved.
//

#import "AppDelegate.h"
#import "LocalNotificationsViewController.h"

@implementation AppDelegate {
    NSMutableArray *localNotifications;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    NSLog(@"didFinishLaunchingWithOptions...");
    
    // handle the case of launching the App
    UILocalNotification *localNotification = [launchOptions objectForKey:UIApplicationLaunchOptionsLocalNotificationKey];
    if (localNotification) {
        NSString *key1Value = [localNotification.userInfo objectForKey:@"key1"];
        NSString *key2Value = [localNotification.userInfo objectForKey:@"key2"];
        NSLog(@"Receive Local Notification while the app is NOT in the foreground...");
        NSLog(@"Value1: %@", key1Value);
        NSLog(@"Value2: %@", key2Value);
        //[viewController displayItem:itemName];  // custom method
        application.applicationIconBadgeNumber = localNotification.applicationIconBadgeNumber - 1;
    }
    
    // Override point for customization after application launch.
    return YES;
}

- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notif {
    NSString *key1Value = [notif.userInfo objectForKey:@"key1"];
    NSString *key2Value = [notif.userInfo objectForKey:@"key2"];
    NSLog(@"Receive Local Notification while the app is in the foreground...");
    NSLog(@"Value1: %@", key1Value);
    NSLog(@"Value2: %@", key2Value);
    //[viewController displayItem:itemName];  // custom method
    application.applicationIconBadgeNumber = notif.applicationIconBadgeNumber - 1;
}
							
- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    NSLog(@"applicationDidEnterBackground...");
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    NSLog(@"applicationWillEnterForeground...");
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

@end
