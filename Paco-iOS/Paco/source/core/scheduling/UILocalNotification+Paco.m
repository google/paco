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
#import "NSMutableArray+Paco.h"
#import "PacoExperiment.h"
#import "PacoExperimentSchedule.h"
#import "PacoDateUtility.h"
#import "PacoExperimentDefinition.h"

NSString* const kNotificationSoundName = @"deepbark_trial.mp3";

static int const kNumOfKeysInUserInfo = 3;
NSString* const kUserInfoKeyExperimentId = @"experimentInstanceId";
NSString* const kUserInfoKeyNotificationFireDate = @"notificationFireDate";
NSString* const kUserInfoKeyNotificationTimeoutDate = @"notificationTimeoutDate";

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


- (BOOL)isEqualToNotificationInfo:(PacoNotificationInfo*)info {
  if ([self.experimentId isEqualToString:info.experimentId] &&
      [self.fireDate isEqualToDate:info.fireDate] &&
      [self.timeOutDate isEqualToDate:info.timeOutDate]) {
    return YES;
  } else {
    return NO;
  }
}

@end



@implementation UILocalNotification (Paco)

+ (UILocalNotification*)pacoNotificationWithExperimentId:(NSString*)experimentId
                                         experimentTitle:(NSString*)experimentTitle
                                                fireDate:(NSDate*)fireDate
                                             timeOutDate:(NSDate*)timeOutDate {
  if (0 == [experimentId length] || 0 == [experimentTitle length] || fireDate == nil ||
      timeOutDate == nil ||[timeOutDate timeIntervalSinceDate:fireDate] <= 0) {
    return nil;
  }
  
  NSString* alertBody = [NSString stringWithFormat:@"[%@]%@",
                         [PacoDateUtility stringForAlertBodyFromDate:fireDate],
                         experimentTitle];
  
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


+ (NSArray*)pacoNotificationsForExperiment:(PacoExperiment*)experiment
                           datesToSchedule:(NSArray*)datesToSchedule {
  if (0 == [datesToSchedule count] || experiment == nil) {
    return nil;
  }
  
  NSTimeInterval timeoutInterval = experiment.schedule.timeout * 60;
  NSMutableArray* notifications = [NSMutableArray arrayWithCapacity:[datesToSchedule count]];
  for (NSDate* fireDate in datesToSchedule) {
    NSDate* timeOutDate = [fireDate dateByAddingTimeInterval:timeoutInterval];
    UILocalNotification* notification =
    [UILocalNotification pacoNotificationWithExperimentId:experiment.instanceId
                                          experimentTitle:experiment.definition.title
                                                 fireDate:fireDate
                                              timeOutDate:timeOutDate];
    [notifications addObject:notification];
  }
  return notifications;
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


- (NSString*)pacoStatusDescription {
  switch ([self pacoStatus]) {
    case PacoNotificationStatusFiredNotTimeout:
      return @"Fired, Not Timeout";
    case PacoNotificationStatusTimeout:
      return @"Fired, Timeout";
    case PacoNotificationStatusNotFired:
      return @"Not Fired";
    case PacoNotificationStatusUnknown:
      return @"Unknown";
    default:
      NSAssert(NO, @"should not happen");
      return @"Wrong";
  }
}


- (BOOL)pacoIsEqualTo:(UILocalNotification*)notification {
  if (![self.timeZone isEqualToTimeZone:notification.timeZone]) {
    return NO;
  }
  if (![self.fireDate isEqualToDate:notification.fireDate]) {
    return NO;
  }
  if (![self.alertBody isEqualToString:notification.alertBody]) {
    return NO;
  }
  if (![self.soundName isEqualToString:notification.soundName]) {
    return NO;
  }
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:self.userInfo];
  PacoNotificationInfo* another = [PacoNotificationInfo pacoInfoWithDictionary:notification.userInfo];
  if (![info isEqualToNotificationInfo:another]) {
    return NO;
  }
  return YES;
}


- (NSString*)pacoExperimentId {
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return info.experimentId;
}

- (NSDate*)pacoFireDate {
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return info.fireDate;
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
  NSArray* notifications = [UILocalNotification scheduledLocalNotificationsForExperiment:experimentInstanceId];
  return 0 < [notifications count];
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

+ (void)pacoCancelNotifications:(NSArray*)notifications {
  for (UILocalNotification* notification in notifications) {
    NSAssert([notification isKindOfClass:[UILocalNotification class]],
             @"should be a UILocalNotification!");
    [[UIApplication sharedApplication] cancelLocalNotification:notification];
  }
}

+ (void)pacoCancelLocalNotification:(UILocalNotification*)notification {
  if (notification != nil) {
    [[UIApplication sharedApplication] cancelLocalNotification:notification];
  }
}

+ (void)pacoProcessNotifications:(NSArray*)notifications withBlock:(NotificationProcessBlock)block {
  if (!block) {
    return;
  }
  int totalNumOfNotifications = [notifications count];
  if (0 == totalNumOfNotifications) {
    block(nil, nil, nil);
  }
  
  static int INVALID_INDEX = -1;
  NSInteger indexOfActiveNotification = INVALID_INDEX;
  NSInteger indexOfFirstNotFiredNotification = INVALID_INDEX;
  for (NSInteger index = 0; index < totalNumOfNotifications; index++) {
    UILocalNotification* notification = [notifications objectAtIndex:index];
    PacoNotificationStatus status = [notification pacoStatus];
    NSAssert(status != PacoNotificationStatusUnknown, @"status should be valid!");
    if (status == PacoNotificationStatusFiredNotTimeout) {
      indexOfActiveNotification = index;
      continue;
    }
    
    if (status == PacoNotificationStatusNotFired) {
      indexOfFirstNotFiredNotification = index;
      break;
    }
  }
  
  UILocalNotification* activeNotication = nil;
  NSArray* expiredNotifications = nil;
  NSArray* notFiredNotifications = nil;
  
  if (indexOfActiveNotification != INVALID_INDEX) {
    activeNotication = [notifications objectAtIndex:indexOfActiveNotification];
  }
  
  if (indexOfFirstNotFiredNotification != INVALID_INDEX) {
    NSRange notFiredRange = NSMakeRange(indexOfFirstNotFiredNotification,
                                        totalNumOfNotifications - indexOfFirstNotFiredNotification);
    notFiredNotifications = [notifications subarrayWithRange:notFiredRange];
  }
  
  NSInteger endIndexOfExpiredNotifications = INVALID_INDEX;
  if (indexOfActiveNotification != INVALID_INDEX) { //There is an active notification
    endIndexOfExpiredNotifications = indexOfActiveNotification;
  } else { //There isn't any active notification
    if (indexOfFirstNotFiredNotification != INVALID_INDEX) { //There are notifications that didn't fire yet
      endIndexOfExpiredNotifications = indexOfFirstNotFiredNotification;
    } else { //There aren't any non-fired notifications
      endIndexOfExpiredNotifications = totalNumOfNotifications;
    }
  }
  NSAssert(endIndexOfExpiredNotifications != INVALID_INDEX, @"should be an valid value!");
  if (endIndexOfExpiredNotifications > 0) { //There are expired notifications
    expiredNotifications = [notifications subarrayWithRange:NSMakeRange(0, endIndexOfExpiredNotifications)];
  }
  
  block(activeNotication, expiredNotifications, notFiredNotifications);
}


+ (void)pacoFetchExpiredNotificationsFrom:(NSArray*)notifications withBlock:(FetchExpiredBlock)block {
  if (!block) {
    return;
  }
  [self pacoProcessNotifications:notifications withBlock:^(UILocalNotification* activeNotification,
                                                           NSArray* expiredNotifications,
                                                           NSArray* notFiredNotifications) {
    NSMutableArray* nonExpiredNotifications = [NSMutableArray array];
    if (activeNotification) {
      [nonExpiredNotifications addObject:activeNotification];
    }
    if ([notFiredNotifications count] > 0) {
      [nonExpiredNotifications addObjectsFromArray:notFiredNotifications];
    }
    if (0 == [nonExpiredNotifications count]) {
      nonExpiredNotifications = nil;
    }
    if (0 == [expiredNotifications count]) {
      expiredNotifications = nil;
    }
    block(expiredNotifications, nonExpiredNotifications);
  }];
}



+ (NSDictionary*)sortNotificationsPerExperiment:(NSArray*)allNotifications {
  NSMutableDictionary* dict = [NSMutableDictionary dictionaryWithCapacity:[allNotifications count]];
  //create a dictionary from allNotifications
  for (UILocalNotification* notification in allNotifications) {
    NSString* experimentId = [notification pacoExperimentId];
    NSAssert(experimentId, @"experimentId should be valid!");
    NSMutableArray* notificationList = [dict objectForKey:experimentId];
    if (notificationList == nil) {
      notificationList = [NSMutableArray arrayWithCapacity:[allNotifications count]];
    }
    [notificationList addObject:notification];
    [dict setObject:notificationList forKey:experimentId];
  }
  
  //sort each array inside this dictionary
  for (NSString* experimentId in dict) {
    NSMutableArray* notificationList = [dict objectForKey:experimentId];
    NSAssert([notificationList isKindOfClass:[NSMutableArray class]],
             @"notificationList should be an array");
    [notificationList pacoSortLocalNotificationsByFireDate];
  }
  return dict;
}

@end
