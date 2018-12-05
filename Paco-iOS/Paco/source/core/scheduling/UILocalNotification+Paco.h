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

#import <UIKit/UIKit.h>
#import "PacoNotificationConstants.h" 

@class PacoExperiment;





    
typedef void(^NotificationProcessBlock)(UILocalNotification* activeNotification,
                                        NSArray* expiredNotifications,
                                        NSArray* notFiredNotifications);

typedef void(^NotificationReplaceBlock)(UILocalNotification* active,
                                        NSArray* expired,
                                        NSArray* toBeCanceled,
                                        NSArray* toBeScheduled);

typedef void(^FetchExpiredBlock)(NSArray* expiredNotifications, NSArray* nonExpiredNotifications);

@interface UILocalNotification (Paco)

- (PacoNotificationStatus)pacoStatus;
- (NSString*)pacoStatusDescription;
- (NSString*)pacoDescription;

+ (UILocalNotification*)pacoNotificationWithExperimentId:(NSString*)experimentId
                                         experimentTitle:(NSString*)experimentTitle
                                                fireDate:(NSDate*)fireDate
                                             timeOutDate:(NSDate*)timeOutDate;

//datesToSchedule Must be sorted already
+ (NSArray*)pacoNotificationsForExperiment:(PacoExperiment*)experiment
                           datesToSchedule:(NSArray*)datesToSchedule;

- (NSString*)pacoExperimentId;
- (NSString*)pacoExperimentTitle;
- (NSDate*)pacoFireDate;
- (NSDate*)pacoTimeoutDate;
- (long)pacoTimeoutMinutes;

+ (NSArray*)scheduledLocalNotificationsForExperiment:(NSString*)experimentInstanceId;
+ (BOOL)hasLocalNotificationScheduledForExperiment:(NSString*)experimentInstanceId;
+ (void)cancelScheduledNotificationsForExperiment:(NSString*)experimentInstanceId;

+ (void)pacoCancelLocalNotification:(UILocalNotification*)notification;
+ (void)pacoCancelNotifications:(NSArray*)notifications;
+ (void)pacoScheduleNotifications:(NSArray*)notifications;

//notifications MUST be sorted already
+ (void)pacoProcessNotifications:(NSArray*)notifications withBlock:(NotificationProcessBlock)block;
+ (void)pacoFetchExpiredNotificationsFrom:(NSArray*)notifications withBlock:(FetchExpiredBlock)block;

//{ NSString : NSMutableArray }
+ (NSDictionary*)pacoSortedDictionaryFromNotifications:(NSArray*)notifications;

- (BOOL)pacoIsEqualTo:(UILocalNotification*)notification;

+ (void)pacoReplaceCurrentNotifications:(NSArray*)currentNotifications
                   withNewNotifications:(NSArray*)newNotifications
                               andBlock:(NotificationReplaceBlock)block;

@end
