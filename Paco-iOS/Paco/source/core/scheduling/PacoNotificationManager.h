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
#import <Foundation/Foundation.h>


@protocol PacoNotificationManagerDelegate <NSObject>

@required
- (void)handleExpiredNotifications:(NSArray*)expiredNotifications;

@end



@interface PacoNotificationManager : NSObject

@property (atomic, assign, readonly) BOOL areNotificationsLoaded;

+ (PacoNotificationManager*)managerWithDelegate:(id<PacoNotificationManagerDelegate>)delegate
                                firstLaunchFlag:(BOOL)firstLaunchFlag;


- (void)scheduleNotifications:(NSArray*)newNotifications;

//notifications MUST be sorted already
- (void)cleanExpiredNotifications;

//call this when the user stops an experiment
//1. cancel all notifications from iOS for this expeirment
//2. clear this expeirment's notifications from notification tray
//3. delete all notifications from cache for this experiment
- (void)cancelNotificationsForExperiment:(NSString*)experimentId;

- (void)cancelNotificationsForExperiments:(NSArray*)experimentIds;

- (UILocalNotification*)activeNotificationForExperiment:(NSString*)experimentId;
- (BOOL)isNotificationActive:(UILocalNotification*)notification;

- (void)handleRespondedNotification:(UILocalNotification*)notification;

- (BOOL)saveNotificationsToCache;
- (BOOL)loadNotificationsFromCache;

- (NSUInteger)numOfScheduledNotifications;
- (BOOL)hasMaximumScheduledNotifications;

- (void)cancelAllPacoNotifications;


@end
