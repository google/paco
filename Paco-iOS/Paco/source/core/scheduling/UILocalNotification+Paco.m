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

static int const kNumOfKeysInUserInfo = 4;
NSString* const kUserInfoKeyExperimentId = @"id";
NSString* const kUserInfoKeyExperimentTitle = @"title";
NSString* const kUserInfoKeyNotificationFireDate = @"fireDate";
NSString* const kUserInfoKeyNotificationTimeoutDate = @"timeoutDate";

@interface PacoNotificationInfo ()
@property(nonatomic, copy) NSString* experimentId;
@property(nonatomic, copy) NSString* experimentTitle;
@property(nonatomic, strong) NSDate* fireDate;
@property(nonatomic, strong) NSDate* timeOutDate;
@end

@implementation PacoNotificationInfo

+ (PacoNotificationInfo*)pacoInfoWithDictionary:(NSDictionary*)infoDict {
  if ([[infoDict allKeys] count] != kNumOfKeysInUserInfo) {
    return nil;
  }
  
  NSString* experimentId = infoDict[kUserInfoKeyExperimentId];
  NSString* experimentTitle = infoDict[kUserInfoKeyExperimentTitle];
  NSDate* fireDate = infoDict[kUserInfoKeyNotificationFireDate];
  NSDate* timeOutDate = infoDict[kUserInfoKeyNotificationTimeoutDate];
  if (0 == [experimentId length] || 0 == [experimentTitle length] ||
      fireDate == nil || timeOutDate == nil || [timeOutDate timeIntervalSinceDate:fireDate] <= 0) {
    return nil;
  }
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  info.experimentId = experimentId;
  info.experimentTitle = experimentTitle;
  info.fireDate = fireDate;
  info.timeOutDate = timeOutDate;
  return info;
}

+ (NSDictionary*)userInfoDictionaryWithExperimentId:(NSString*)experimentId
                                    experimentTitle:(NSString*)experimentTitle
                                           fireDate:(NSDate*)fireDate
                                        timeOutDate:(NSDate*)timeOutDate {
  if (0 == [experimentId length] || 0 == [experimentTitle length] ||
      fireDate == nil || timeOutDate == nil || [timeOutDate timeIntervalSinceDate:fireDate] <= 0) {
    return nil;
  }
  NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
  userInfo[kUserInfoKeyExperimentId] = experimentId;
  userInfo[kUserInfoKeyExperimentTitle] = experimentTitle;
  userInfo[kUserInfoKeyNotificationFireDate] = fireDate;
  userInfo[kUserInfoKeyNotificationTimeoutDate] = timeOutDate;
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

- (long)timeoutMinutes {
  return [self.timeOutDate timeIntervalSinceDate:self.fireDate] / 60;
}

- (NSString*)description {
  NSString* description = @"{";
  description = [description stringByAppendingString:[NSString stringWithFormat:@"%@:%@; ",
                                                      kUserInfoKeyExperimentId, self.experimentId]];
  description = [description stringByAppendingString:[NSString stringWithFormat:@"%@:%@; ",
                                                      kUserInfoKeyExperimentTitle, self.experimentTitle]];
  description = [description stringByAppendingString:[NSString stringWithFormat:@"%@:%@; ",
                                                      kUserInfoKeyNotificationFireDate,
                                                      [PacoDateUtility pacoStringForDate:self.fireDate]]];
  description = [description stringByAppendingString:[NSString stringWithFormat:@"%@:%@; ",
                                                      kUserInfoKeyNotificationTimeoutDate,
                                                      [PacoDateUtility pacoStringForDate:self.timeOutDate]]];
  description = [description stringByAppendingString:@"}"];
  return description;
}

- (BOOL)isEqualToNotificationInfo:(PacoNotificationInfo*)info {
  if ([self.experimentId isEqualToString:info.experimentId] &&
      [self.experimentTitle isEqualToString:info.experimentTitle] &&
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
  
  NSString* alertBody = [NSString stringWithFormat:@"%@\nTime to participate!",experimentTitle];
  UILocalNotification *notification = [[UILocalNotification alloc] init];
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.fireDate = fireDate;
  notification.alertBody = alertBody;
  notification.soundName = kNotificationSoundName;
  notification.applicationIconBadgeNumber = 1;
  notification.userInfo = [PacoNotificationInfo userInfoDictionaryWithExperimentId:experimentId
                                                                   experimentTitle:experimentTitle
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

- (NSString*)pacoDescription {
  return [NSString stringWithFormat:@"<%@,%p: fireDate=%@, status=(%@), userInfo=%@>",
          NSStringFromClass([self class]),
          self,
          [PacoDateUtility pacoStringForDate:self.fireDate],
          [self pacoStatusDescription],
          [[PacoNotificationInfo pacoInfoWithDictionary:self.userInfo] description]
          ];
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

- (NSString*)pacoExperimentTitle {
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return info.experimentTitle;
}

- (NSDate*)pacoFireDate {
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return info.fireDate;
}

- (NSDate*)pacoTimeoutDate {
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return info.timeOutDate;
}

- (long)pacoTimeoutMinutes {
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return [info timeoutMinutes];
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
      [UILocalNotification pacoCancelLocalNotification:noti];
    }
  }
}

+ (void)pacoCancelNotifications:(NSArray*)notifications {
  for (UILocalNotification* notification in notifications) {
    NSAssert([notification isKindOfClass:[UILocalNotification class]],
             @"should be a UILocalNotification!");
    [UILocalNotification pacoCancelLocalNotification:notification];
  }
}

+ (void)pacoCancelLocalNotification:(UILocalNotification*)notification {
  if (notification != nil) {
    //NSLog(@"UIApplication is cancelling a notification: %@", [notification pacoDescription]);
    [[UIApplication sharedApplication] cancelLocalNotification:notification];
  }
}

+ (void)pacoProcessNotifications:(NSArray*)notifications withBlock:(NotificationProcessBlock)block {
  if (!block) {
    return;
  }
  NSUInteger totalNumOfNotifications = [notifications count];
  if (0 == totalNumOfNotifications) {
    block(nil, nil, nil);
  }
  
  static int INVALID_INDEX = -1;
  NSInteger indexOfActiveNotification = INVALID_INDEX;
  NSInteger indexOfFirstNotFiredNotification = INVALID_INDEX;
  for (NSInteger index = 0; index < totalNumOfNotifications; index++) {
    UILocalNotification* notification = notifications[index];
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
    activeNotication = notifications[indexOfActiveNotification];
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
    NSMutableArray* notificationList = dict[experimentId];
    if (notificationList == nil) {
      notificationList = [NSMutableArray arrayWithCapacity:[allNotifications count]];
    }
    [notificationList addObject:notification];
    dict[experimentId] = notificationList;
  }
  
  //sort each array inside this dictionary
  for (NSString* experimentId in dict) {
    NSMutableArray* notificationList = dict[experimentId];
    NSAssert([notificationList isKindOfClass:[NSMutableArray class]],
             @"notificationList should be an array");
    [notificationList pacoSortLocalNotificationsByFireDate];
  }
  return dict;
}

@end
