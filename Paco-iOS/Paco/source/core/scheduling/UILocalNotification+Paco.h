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

typedef enum {
  PacoNotificationStatusUnknown = 0,      //unknown
  PacoNotificationStatusNotFired,         //not fired yet
  PacoNotificationStatusFiredNotTimeout,  //fired, but not timed out
  PacoNotificationStatusTimeout,          //fired, and timed out
} PacoNotificationStatus;



@interface PacoNotificationInfo : NSObject
@property(nonatomic, copy, readonly) NSString* experimentId;
@property(nonatomic, strong, readonly) NSDate* fireDate;
@property(nonatomic, strong, readonly) NSDate* timeOutDate;
@end



@interface UILocalNotification (Paco)

- (PacoNotificationStatus)pacoStatus;

+ (UILocalNotification*)pacoNotificationWithExperimentId:(NSString*)experimentId
                                               alertBody:(NSString*)alertBody
                                                fireDate:(NSDate*)fireDate
                                             timeOutDate:(NSDate*)timeOutDate;
- (NSString*)pacoExperimentId;
- (NSDate*)pacoTimeoutDate;
+ (NSArray*)scheduledLocalNotificationsForExperiment:(NSString*)experimentInstanceId;
+ (BOOL)hasLocalNotificationScheduledForExperiment:(NSString*)experimentInstanceId;
+ (void)cancelScheduledNotificationsForExperiment:(NSString*)experimentInstanceId;
@end
