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

#import "UILocalNotification+Paco.h"

static NSString* const kNotificationSoundName = @"deepbark_trial.mp3";

static int const kNumOfKeysInUserInfo = 3;
static NSString* const kUserInfoKeyExperimentId = @"experimentInstanceId";
static NSString* const kUserInfoKeyNotificationFireDate = @"notificationFireDate";
static NSString* const kUserInfoKeyNotificationTimeoutDate = @"notificationTimeoutDate";

@interface PacoNotificationInfo ()
@property(nonatomic, copy) NSString* experimentId;
@property(nonatomic, strong) NSDate* fireDate;
@property(nonatomic, strong) NSDate* timeOutDate;
@end

@implementation PacoNotificationInfo

+ (PacoNotificationInfo*)pacoInfoWithDictionary:(NSDictionary*)infoDict {
  if ([[infoDict allKeys] count] != kNumOfKeysInUserInfo) {
    return nil;
  }
  
  NSString* experimentId = [infoDict objectForKey:kUserInfoKeyExperimentId];
  NSDate* fireDate = [infoDict objectForKey:kUserInfoKeyNotificationFireDate];
  NSDate* timeOutDate = [infoDict objectForKey:kUserInfoKeyNotificationTimeoutDate];
  if (0 == [experimentId length] || fireDate == nil || timeOutDate == nil ||
      [timeOutDate timeIntervalSinceDate:fireDate] <= 0) {
    return nil;
  }
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  info.experimentId = experimentId;
  info.fireDate = fireDate;
  info.timeOutDate = timeOutDate;
  return info;
}

+ (NSDictionary*)userInfoDictionaryWithExperimentId:(NSString*)experimentId
                                           fireDate:(NSDate*)fireDate
                                        timeOutDate:(NSDate*)timeOutDate {
  if (0 == [experimentId length] || fireDate == nil || timeOutDate == nil ||
      [timeOutDate timeIntervalSinceDate:fireDate] <= 0) {
    return nil;
  }
  NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
  [userInfo setObject:experimentId forKey:kUserInfoKeyExperimentId];
  [userInfo setObject:fireDate forKey:kUserInfoKeyNotificationFireDate];
  [userInfo setObject:timeOutDate forKey:kUserInfoKeyNotificationTimeoutDate];
  return userInfo;
}

- (PacoNotificationStatus)status {
  if ([self.fireDate timeIntervalSinceNow] > 0) {
    NSAssert([self.timeOutDate timeIntervalSinceDate:self.fireDate] > 0,
             @"timeout data should always be later than fire date");
    return PacoNotificationStatusNotFired;
  } else {
    if ([self.timeOutDate timeIntervalSinceNow] > 0) {
      return PacoNotificationStatusFiredNotTimeout;
    } else {
      return PacoNotificationStatusTimeout;
    }
  }
}

@end



@implementation UILocalNotification (Paco)

+ (UILocalNotification*)pacoNotificationWithExperimentId:(NSString*)experimentId
                                               alertBody:(NSString*)alertBody
                                                fireDate:(NSDate*)fireDate
                                             timeOutDate:(NSDate*)timeOutDate {
  if (0 == [experimentId length] || 0 == [alertBody length] || fireDate == nil ||
      timeOutDate == nil ||[timeOutDate timeIntervalSinceDate:fireDate] <= 0) {
    return nil;
  }

  UILocalNotification *notification = [[UILocalNotification alloc] init];
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.fireDate = fireDate;
  notification.alertBody = alertBody;
  notification.soundName = kNotificationSoundName;
  notification.userInfo = [PacoNotificationInfo userInfoDictionaryWithExperimentId:experimentId
                                                                          fireDate:fireDate
                                                                       timeOutDate:timeOutDate];
  return notification;
}

- (PacoNotificationStatus)pacoStatus {
  if (self.userInfo == nil) {
    return PacoNotificationStatusUnknown;
  }
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:self.userInfo];
  if (info == nil) {
    return PacoNotificationStatusUnknown;
  }
  return [info status];
}

- (NSString*)pacoExperimentId {
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return info.experimentId;
}

- (NSDate*)pacoTimeoutDate {
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return info.timeOutDate;
}


+ (NSArray*)scheduledLocalNotificationsForExperiment:(NSString*)experimentInstanceId {
  NSAssert([experimentInstanceId length] > 0, @"id should be valid!");
  
  NSMutableArray* result = [NSMutableArray array];
  NSArray* allScheduledNotifications = [UIApplication sharedApplication].scheduledLocalNotifications;
  for (UILocalNotification* notification in allScheduledNotifications) {
    PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:notification.userInfo];
    NSAssert([info.experimentId length] > 0, @"experimentId should be valid!");
    if ([info.experimentId isEqualToString:experimentInstanceId]) {
      [result addObject:notification];
    }
  }
  return result;
}

+ (BOOL)hasLocalNotificationScheduledForExperiment:(NSString*)experimentInstanceId {
  return 0 < [UILocalNotification scheduledLocalNotificationsForExperiment:experimentInstanceId];
}

+ (void)cancelScheduledNotificationsForExperiment:(NSString*)experimentInstanceId {
  NSAssert([experimentInstanceId length] > 0, @"id should be valid!");

  NSArray* scheduledArr = [[UIApplication sharedApplication] scheduledLocalNotifications];
  for (UILocalNotification* noti in scheduledArr) {
    PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:noti.userInfo];
    NSAssert([info.experimentId length] > 0, @"experimentId should be valid!");

    if ([info.experimentId isEqualToString:experimentInstanceId]) {
      [[UIApplication sharedApplication] cancelLocalNotification:noti];
    }
  }
}

@end
