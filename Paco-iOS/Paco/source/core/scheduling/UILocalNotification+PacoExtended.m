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

#import "UILocalNotification+PacoExteded.h"
#import "NSMutableArray+Paco.h"
#import "PacoExperiment.h"
#import "PacoExperimentSchedule.h"
#import "PacoDateUtility.h"
#import "PacoExperimentDefinition.h"
#import "PacoClient.h"
#import "PacoNotificationConstants.h"
#import "PacoExtendedNotificationInfo.h"
#import "ExperimentDAO.h"
#import "ActionSpecification.h"
#import "ExperimentGroup.h"
#import "ExperimentDAO.h"
#import "ActionTrigger.h"
#import "PacoNotificationAction.h"
#import "NSObject+J2objcKVO.h"
#import "java/lang/long.h"
#import "PAExperimentDAO+Helper.h"
#import "OrgJodaTimeDateTime+PacoDateHelper.h"
#import "ModelBase+PacoAssociatedId.h"



extern NSString* const kNotificationGroupId;
extern NSString* const kNotificationGroupName;
extern NSString* const kUserInfoKeyActionTriggerId;
extern NSString* const kUserInfoKeyNotificationActionId;
extern NSString* const kUserInfoKeyActionTriggerSpecId;



@implementation UILocalNotification (Paco)

+ (UILocalNotification*)pacoNotificationWithExperimentId:(NSString*)experimentId
                                         experimentTitle:(NSString*)experimentTitle
                                                fireDate:(NSDate*)fireDate
                                             timeOutDate:(NSDate*)timeOutDate
                                                 groupId:(NSString*) groupId
                                               groupName:(NSString*) groupName
                                               triggerId:(NSString*) triggerId
                                    notificationActionId:(NSString*) notificationActionId
                                    actionTriggerSpecId:(NSString*) actionTriggerSpecId
{
  if (0 == [experimentId length] || 0 == [experimentTitle length] || fireDate == nil ||
      timeOutDate == nil ||[timeOutDate timeIntervalSinceDate:fireDate] <= 0 || [groupId length] !=0
    ||[groupName length] !=0 || [triggerId length] != 0 || [notificationActionId length] ==0 || [actionTriggerSpecId length] ==0)
  {
    return nil;
  }
  
  NSString* alertBody = [NSString stringWithFormat:@"%@\nTime to participate!",experimentTitle];
  UILocalNotification *notification = [[UILocalNotification alloc] init];
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.fireDate = fireDate;
  notification.alertBody = alertBody;
  notification.soundName = kNotificationSoundName;
  notification.applicationIconBadgeNumber = 1;
  notification.userInfo = [PacoExtendedNotificationInfo userInfoDictionaryWithExperimentId:experimentId experimentTitle:experimentTitle fireDate:fireDate timeOutDate:timeOutDate groupId:groupId groupName:groupName actionTriggerId:triggerId notificationActionId:notificationActionId actionTriggerSpecId:actionTriggerSpecId];
    
  return notification;
}


+ (NSArray*)pacoNotificationsForExperimentSpecifications:(NSArray*) specifications
                            {
  if ( 0 == [specifications count]) {
    return nil;
  }
                                
  NSMutableArray* notifications = [NSMutableArray arrayWithCapacity:[specifications count]];
                                
  for (PAActionSpecification  * spec   in specifications) {
      
      
      PAExperimentDAO   *   dao = [spec valueForKey:@"experiment_"];
      PAExperimentGroup *   group = [spec valueForKey:@"experimentGroup_"];
      PAActionTrigger*   actionTrigger = [spec valueForKey:@"actionTrigger_"];
      PAPacoNotificationAction *   notificationAction = [spec valueForKey:@"action_"];
      NSString* experimentId =  [dao instanceId];
      NSString* experimentTitle = [dao valueForKeyEx:@"title"];
      NSDate * fireDate = [[spec valueForKey:@"time_"] nsDateValue];
      long     timeoutDate =  [[notificationAction valueForKeyEx:@"timeout"]  longValue];
      NSDate * timeOutDate =  [fireDate dateByAddingTimeInterval:timeoutDate];
      NSString* groupId = [group getUuid];
      NSString* groupName = [group valueForKeyEx:@"name"];
      NSString* triggerId = [[notificationAction valueForKeyEx:@"id"] stringValue];
      NSString* notificationActionId = [notificationAction  getUuid];
      NSString *   notifiationSpecId  =  [((JavaLangLong*)  [spec valueForKey:@"actionTriggerSpecId_"] ) stringValue];
      

      UILocalNotification* notification =   [UILocalNotification pacoNotificationWithExperimentId:experimentId experimentTitle:experimentTitle   fireDate:fireDate timeOutDate:timeOutDate groupId:groupId  groupName:groupName  triggerId:triggerId  notificationActionId:notificationActionId  actionTriggerSpecId:notifiationSpecId];
      
      
    [notifications addObject:notification];
  }
  return notifications;
}

- (PacoNotificationStatus)pacoStatus {
  if (self.userInfo == nil) {
    return PacoNotificationStatusUnknown;
  }
    
  PacoExtendedNotificationInfo * info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:self.userInfo];
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

/*
  two alerts that fire at exactly the same time and have exactly the same alert body and sound will be considered to be equal.
   enhancement: make sure they have the same id.
 */
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
  PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return info.experimentId;
}

- (NSString*)pacoExperimentTitle {
  PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return info.experimentTitle;
}

- (NSDate*)pacoFireDate {
  PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return info.fireDate;
}

- (NSDate*)pacoTimeoutDate {
  PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return info.timeOutDate;
}

- (long)pacoTimeoutMinutes {
  PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:self.userInfo];
  return [info timeoutMinutes];
}

- (long)pacoGroupId {
    PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:self.userInfo];
    return [info groupId];
}


- (long)pacoGroupName {
    PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:self.userInfo];
    return [info groupName];
}

- (long)pacoActionTriggerId {
    PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:self.userInfo];
    return [info actionTriggerId];
}

- (long)pacoActionNotificationActionId {
    PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:self.userInfo];
    return [info notificationActionId];
}


- (long)pacoActionNotificationActionSpecId{
    PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:self.userInfo];
    return [info actionTriggerSpecId];
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

+ (NSArray*)scheduledLocalNotificationsForGroupId:(NSString*) groupId
{
    NSAssert([groupId length] > 0, @"id should be valid!");
    
    NSMutableArray* result = [NSMutableArray array];
    NSArray* allScheduledNotifications = [UIApplication sharedApplication].scheduledLocalNotifications;
    for (UILocalNotification* notification in allScheduledNotifications) {
        PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:notification.userInfo];
        NSAssert([info.groupId   length] > 0, @"experimentId should be valid!");
        if ([info.groupId isEqualToString:groupId]) {
            [result addObject:notification];
        }
    }
    return result;
}

+ (NSArray*)scheduledLocalNotificationsForTriggerId:(NSString*) actionTriggerId
{
    NSAssert([actionTriggerId length] > 0, @"id should be valid!");
    
    NSMutableArray* result = [NSMutableArray array];
    NSArray* allScheduledNotifications = [UIApplication sharedApplication].scheduledLocalNotifications;
    for (UILocalNotification* notification in allScheduledNotifications) {
        PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:notification.userInfo];
        NSAssert([info.actionTriggerId   length] > 0, @"experimentId should be valid!");
        if ([info.actionTriggerId isEqualToString:actionTriggerId]) {
            [result addObject:notification];
        }
    }
    return result;
}

+ (NSArray*)scheduledLocalNotificationsForNotificationActionId:(NSString*) notificationActionId
{
    NSAssert([notificationActionId length] > 0, @"id should be valid!");
    
    NSMutableArray* result = [NSMutableArray array];
    NSArray* allScheduledNotifications = [UIApplication sharedApplication].scheduledLocalNotifications;
    for (UILocalNotification* notification in allScheduledNotifications) {
        PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:notification.userInfo];
        NSAssert([info.notificationActionId   length] > 0, @"experimentId should be valid!");
        if ([info.notificationActionId isEqualToString:notificationActionId]) {
            [result addObject:notification];
        }
    }
    return result;
}




+ (BOOL)hasLocalNotificationScheduledForExperiment:(NSString*)experimentInstanceId {
  NSArray* notifications = [UILocalNotification scheduledLocalNotificationsForExperiment:experimentInstanceId];
  return 0 < [notifications count];
}



+ (BOOL)hasLocalNotificationScheduledForGroup:(NSString*)groupId {
    NSArray* notifications = [UILocalNotification scheduledLocalNotificationsForGroupId:groupId];
    return 0 < [notifications count];
}

+ (BOOL)hasLocalNotificationScheduledForTrigger:(NSString*)actionTrigger {
    NSArray* notifications = [UILocalNotification scheduledLocalNotificationsForTriggerId:actionTrigger];
    return 0 < [notifications count];
}


+ (BOOL)hasLocalNotificationScheduledForNotificationActionId:(NSString*)actionId {
    NSArray* notifications = [UILocalNotification scheduledLocalNotificationsForNotificationActionId:actionId];
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


+ (void)cancelScheduledNotificationsForGroupId:(NSString*)groupId {
    NSAssert([groupId length] > 0, @"id should be valid!");
    
    NSArray* scheduledArr = [[UIApplication sharedApplication] scheduledLocalNotifications];
    for (UILocalNotification* noti in scheduledArr) {
        PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:noti.userInfo];
        NSAssert([info.groupId length] > 0, @"experimentId should be valid!");
        
        if ([info.groupId  isEqualToString:groupId]) {
            [UILocalNotification pacoCancelLocalNotification:noti];
        }
    }
}



+ (void)cancelScheduledNotificationsForActionTrigger:(NSString*)actionTriggerId{
    NSAssert([actionTriggerId length] > 0, @"id should be valid!");
    
    NSArray* scheduledArr = [[UIApplication sharedApplication] scheduledLocalNotifications];
    for (UILocalNotification* noti in scheduledArr) {
        PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:noti.userInfo];
        NSAssert([info.actionTriggerId length] > 0, @"experimentId should be valid!");
        
        if ([info.actionTriggerId  isEqualToString:actionTriggerId]) {
            [UILocalNotification pacoCancelLocalNotification:noti];
        }
    }
}

+ (void)cancelScheduledNotificationsForNotificationAction:(NSString*)notificationActionId{
    NSAssert([notificationActionId length] > 0, @"id should be valid!");
    
    NSArray* scheduledArr = [[UIApplication sharedApplication] scheduledLocalNotifications];
    for (UILocalNotification* noti in scheduledArr) {
        PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:noti.userInfo];
        NSAssert([info.notificationActionId length] > 0, @"experimentId should be valid!");
        
        if ([info.notificationActionId  isEqualToString:notificationActionId]) {
            [UILocalNotification pacoCancelLocalNotification:noti];
        }
    }
}


+ (void)pacoCancelLocalNotification:(UILocalNotification*)notification {
  if (notification != nil) {
    //NSLog(@"UIApplication is cancelling a notification: %@", [notification pacoDescription]);
    [[UIApplication sharedApplication] cancelLocalNotification:notification];
  }
}

+ (void)pacoCancelNotifications:(NSArray*)notifications {
  DDLogInfo(@"iOS Cancelling %lu notifications.", (unsigned long)[notifications count]);
  for (UILocalNotification* notification in notifications) {
    NSAssert([notification isKindOfClass:[UILocalNotification class]],
             @"should be a UILocalNotification!");
    [UILocalNotification pacoCancelLocalNotification:notification];
  }
}

/*
  NOTE: Don't use the following code to set local notifications:

  [UIApplication sharedApplication].scheduledLocalNotifications = notifications;

  This API will not only schedule the future notifications,
  but also clear all notifications in the notification center:
 **/
+ (void)pacoScheduleNotifications:(NSArray*)notifications {
  DDLogInfo(@"iOS Scheduling %lu notifications.", (unsigned long)[notifications count]);
  for (UILocalNotification* notification in notifications) {
    [[UIApplication sharedApplication] scheduleLocalNotification:notification];
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

+ (void)pacoReplaceCurrentNotifications:(NSArray*)currentNotifications
                   withNewNotifications:(NSArray*)newNotifications
                               andBlock:(NotificationReplaceBlock)block{
  NotificationProcessBlock processBlock = ^(UILocalNotification* activeNotification,
                                            NSArray* expiredNotifications,
                                            NSArray* notFiredNotifications) {
    NSMutableArray *toCancel = [NSMutableArray array];
    NSMutableArray *toSchedule = [NSMutableArray array];
      
    for (UILocalNotification *newNotification in newNotifications) {
      if (![notFiredNotifications containsObject:newNotification]) {
        [toSchedule addObject:newNotification];
      }
    }
    for (UILocalNotification *oldNotification in notFiredNotifications) {
      if (![newNotifications containsObject:oldNotification]) {
        [toCancel addObject:oldNotification];
      }
    }
    block(activeNotification, expiredNotifications, toCancel, toSchedule);
  };

  [UILocalNotification pacoProcessNotifications:currentNotifications withBlock:processBlock];
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


//{ NSString : NSMutableArray }
+ (NSDictionary*)pacoDictionaryFromNotifications:(NSArray*)notifications {
  NSMutableDictionary* dict = [NSMutableDictionary dictionaryWithCapacity:[notifications count]];
  for (UILocalNotification* notification in notifications) {
    NSString* experimentId = [notification pacoExperimentId];
    NSMutableArray* notificationList = dict[experimentId];
    if (!notificationList) {
      notificationList = [NSMutableArray arrayWithCapacity:[notifications count]];
    }
    [notificationList addObject:notification];
    dict[experimentId] = notificationList;
  }
  return [NSDictionary dictionaryWithDictionary:dict];
}


//{ NSString : NSMutableArray }
+ (NSDictionary*)pacoSortedDictionaryFromNotifications:(NSArray*)notifications {
  NSDictionary *dict = [self pacoDictionaryFromNotifications:notifications];
  //sort each array inside this dictionary
  for (NSString* experimentId in dict) {
    NSMutableArray* notificationList = dict[experimentId];
    NSAssert([notificationList isKindOfClass:[NSMutableArray class]],
             @"notificationList should be NSMutableArray");
    [notificationList pacoSortLocalNotificationsByFireDate];
  }
  return dict;
}

@end
