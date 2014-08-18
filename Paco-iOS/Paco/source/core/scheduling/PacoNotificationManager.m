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

#import "PacoNotificationManager.h"
#import "PacoDateUtility.h"
#import "UILocalNotification+Paco.h"
#import "PacoScheduler.h"
#import "NSError+Paco.h"
#import "NSString+Paco.h"
#import "NSMutableArray+Paco.h"
#import "PacoClient.h"

static NSString* kNotificationPlistName = @"notificationDictionary.plist";

@interface PacoNotificationManager ()
@property (atomic, retain) NSMutableDictionary* notificationDict;
@property (nonatomic, weak) id<PacoNotificationManagerDelegate> delegate;
@property (atomic, assign) BOOL areNotificationsLoaded;


@end

@implementation PacoNotificationManager

+ (PacoNotificationManager*)managerWithDelegate:(id<PacoNotificationManagerDelegate>)delegate
                                firstLaunchFlag:(BOOL)firstLaunch {
  PacoNotificationManager* manager = [[PacoNotificationManager alloc] init];
  manager.delegate = delegate;
  
  if (firstLaunch) {
    [manager cancelAlliOSNotifications];
  }
  return manager;
}

- (void)adjustBadgeNumber {
  [self updateBadgeNumber:[self totalNumberOfActiveNotifications]];
}

- (void)updateBadgeNumber:(NSUInteger)numOfActiveNotifications {
  DDLogInfo(@"There are %lu active notifications", (unsigned long)numOfActiveNotifications);

  int badgeNumber = numOfActiveNotifications > 0 ? 1 : 0;
  [UIApplication sharedApplication].applicationIconBadgeNumber = badgeNumber;

  DDLogInfo(@"Badge number set to %d", badgeNumber);
}

- (void)handleExpiredNotifications:(NSArray*)expiredNotifications {
  if (!self.delegate) {
    DDLogError(@"PacoNotificationManager's delegate should be a valid PacoScheduler's object!");
  }
  [self.delegate handleExpiredNotifications:expiredNotifications];
}

- (void)cancelAlliOSNotifications {
  DDLogInfo(@"Cancel All Local Notifications!");
  [[UIApplication sharedApplication] cancelAllLocalNotifications];
  DDLogInfo(@"Badge number set to 0");
  [UIApplication sharedApplication].applicationIconBadgeNumber = 0;
}

//when notification system needs to be shut down, Paco needs to:
//a. cancel all notifications from iOS system
//b. check if there are any expired notifications, and save survey missing events for them
//c. set notification dictionary to be empty
- (void)cancelAllPacoNotifications {
  @synchronized (self) {
    [self cancelAlliOSNotifications];
    
    [self processCachedNotificationsWithBlock:^(NSMutableDictionary* newNotificationDict,
                                                NSArray* expiredNotifications,
                                                NSArray* notFiredNotifications) {
      if (expiredNotifications) {
        [self handleExpiredNotifications:expiredNotifications];
      }
    }];
    
    //reset notification dictionary
    self.notificationDict = [NSMutableDictionary dictionary];
    DDLogInfo(@"New Notification Dict: %@", [self.notificationDict pacoDescriptionForNotificationDict]);
    [self saveNotificationsToCache];
  }
}

- (void)handleRespondedNotification:(UILocalNotification*)notification {
  @synchronized (self) {
    if (notification == nil) {
      return;
    }
    DDLogInfo(@"Handling responded notification...");
    //Since this notification is responded successfully, cancelling it will clear it from the notification tray
    [UILocalNotification pacoCancelLocalNotification:notification];
    
    //remove this notification from local cache
    NSString* experimentId = [notification pacoExperimentId];
    NSAssert(experimentId, @"experimentId should be valid");
    NSMutableArray* notifications = (self.notificationDict)[experimentId];
    if (0 == [notifications count]) {
      return;
    }
    [notifications removeObject:notification];
    (self.notificationDict)[experimentId] = notifications;
    DDLogInfo(@"New Notification Dict: %@", [self.notificationDict pacoDescriptionForNotificationDict]);
    [self saveNotificationsToCache];
    
    [self adjustBadgeNumber];
  }
}


- (void)processCachedNotificationsWithBlock:(void(^)(NSMutableDictionary*, NSArray*, NSArray*))block {
  @synchronized (self) {
    NSMutableDictionary* newDict = [NSMutableDictionary dictionary];
    if (0 == [self.notificationDict count]) {
      block(newDict, nil, nil);
      return;
    }
    
    NSMutableArray* allExpiredNotifications = [NSMutableArray array];
    NSMutableArray* allNotFiredNotifications = [NSMutableArray array];
    
    for (NSString* experimentId in self.notificationDict) {
      NSArray* notifications = (self.notificationDict)[experimentId];
      if (0 == [notifications count]) {
        continue;
      }
      NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                         NSArray* expiredNotifications,
                                         NSArray* notFiredNotifications) {
        if (activeNotification != nil) {
          newDict[experimentId] = [NSMutableArray arrayWithObject:activeNotification];
        }
        
        if ([expiredNotifications count] > 0) {
          [allExpiredNotifications addObjectsFromArray:expiredNotifications];
        }
        if ([notFiredNotifications count] > 0) {
          [allNotFiredNotifications addObjectsFromArray:notFiredNotifications];
        }
      };
      
      [UILocalNotification pacoProcessNotifications:notifications withBlock:block];
    }
    
    if (0 == [allExpiredNotifications count]) {
      allExpiredNotifications = nil;
    }
    
    if (0 == [allNotFiredNotifications count]) {
      allNotFiredNotifications = nil;
    }
    block(newDict, allExpiredNotifications, allNotFiredNotifications);
  }
}

/*
 - Keep the active notifications

 - For all expired notifications:
 a. cancel them from iOS
 b. save survey-missed events
 c. delete them from the local cache

 - For a scheduled but not fired notification in local cache:
 a. if it is in the new notification list, then don't do anything
 b. if it isn't in the new notification list, then cancel it from iOS and delete it from local cache

 - For a notification in the new notification list
 a. if it is in local cache, meaning it's scheduled already, don't do anything
 b. if it isn't in local cache, schedule it and save it in cache
 **/
- (void)scheduleNotifications:(NSArray*)newNotifications {
  @synchronized (self) {
    NSMutableArray *allActive = [NSMutableArray array];
    NSMutableArray *allExpired = [NSMutableArray array];
    NSMutableArray *allToCancel = [NSMutableArray array];
    NSMutableArray *allToSchedule = [NSMutableArray array];
    NSMutableDictionary *resultDict = [NSMutableDictionary dictionary];

    //generate a dictionary from the new list of notifcations
    NSDictionary *newNotificationDict = [UILocalNotification pacoSortedDictionaryFromNotifications:newNotifications];
    for (NSString* experimentId in self.notificationDict) {
      NSArray *currentNotifications = self.notificationDict[experimentId];
      NSArray *newNotifications = newNotificationDict[experimentId];

      NotificationReplaceBlock block = ^(UILocalNotification *active,
                                         NSArray *expired,
                                         NSArray *toBeCanceled,
                                         NSArray *toBeScheduled) {
        NSMutableArray *results = [NSMutableArray arrayWithArray:newNotifications];
        if (active) {
          [allActive addObject:active];
          [results addObject:active];
        }
        resultDict[experimentId] = [results pacoSortLocalNotificationsAndRemoveDuplicates];

        [allExpired addObjectsFromArray:expired];
        [allToCancel addObjectsFromArray:toBeCanceled];
        [allToSchedule addObjectsFromArray:toBeScheduled];
      };
      [UILocalNotification pacoReplaceCurrentNotifications:currentNotifications
                                      withNewNotifications:newNotifications
                                                  andBlock:block];
    }

    for (NSString *experimentId in newNotificationDict) {
      BOOL isNewExperiment = (self.notificationDict[experimentId] == nil);
      if (isNewExperiment) {
        NSMutableArray *notifications = newNotificationDict[experimentId];
        resultDict[experimentId] = [notifications pacoSortLocalNotificationsAndRemoveDuplicates];
        [allToSchedule addObjectsFromArray:notifications];
      }
    }

    DDLogInfo(@"%lu active: %@", (unsigned long)[allActive count], [allActive pacoDescriptionForNotifications]);
    DDLogInfo(@"%lu expired: %@", (unsigned long)[allExpired count], [allExpired pacoDescriptionForNotifications]);
    DDLogInfo(@"%lu to be canceled: %@",
              (unsigned long)[allToCancel count], [allToCancel pacoDescriptionForNotifications]);
    DDLogInfo(@"%lu new to be scheduled: %@",
              (unsigned long)[allToSchedule count], [allToSchedule pacoDescriptionForNotifications]);

    self.notificationDict = resultDict;
    [self saveNotificationsToCache];
    DDLogInfo(@"%@", [self.notificationDict pacoDescriptionForNotificationDict]);

    [self handleExpiredNotifications:allExpired];
    [allToCancel addObjectsFromArray:allExpired];
    [UILocalNotification pacoCancelNotifications:allToCancel];
    [UILocalNotification pacoScheduleNotifications:allToSchedule];
    [self updateBadgeNumber:[allActive count]];
  }
}

- (NSUInteger)numOfScheduledNotifications {
  return [[UIApplication sharedApplication].scheduledLocalNotifications count];
}

- (BOOL)hasMaximumScheduledNotifications {
  return (kTotalNumOfNotifications == [self numOfScheduledNotifications]);
}



/*
 - Keep the following notifications: 
 a. the active notifications
 b. all scheduled but not fired notifications
 
 - Clean all expired notifications:
 a. cancel them from iOS
 b. save survey-missed events
 c. delete them from the local cache
 **/
- (void)cleanExpiredNotifications {
  @synchronized(self) {
    if (0 == [self.notificationDict count]) {
      return;
    }
    NSMutableDictionary* newNotificationDict = [NSMutableDictionary dictionary];
    NSMutableArray* allExpiredNotifications = [NSMutableArray array];
    for (NSString* experimentId in self.notificationDict) {
      NSArray* notifications = (self.notificationDict)[experimentId];
      if (0 == [notifications count]) {
        continue;
      }
      FetchExpiredBlock block = ^(NSArray* expiredNotifications,
                                  NSArray* nonExpiredNotifications) {
        if ([expiredNotifications count] > 0) {
          [allExpiredNotifications addObjectsFromArray:expiredNotifications];
        }
        if ([nonExpiredNotifications count] > 0) {
          newNotificationDict[experimentId] = [NSMutableArray arrayWithArray:nonExpiredNotifications];
        }
      };
      [UILocalNotification pacoFetchExpiredNotificationsFrom:notifications withBlock:block];
    }
    DDLogInfo(@"Clean %lu expired notifications...", (unsigned long)[allExpiredNotifications count]);
    //handle the expired notifications
    if ([allExpiredNotifications count] > 0) {
      [UILocalNotification pacoCancelNotifications:allExpiredNotifications];
      [self handleExpiredNotifications:allExpiredNotifications];
      DDLogInfo(@"New Notification Dict: %@", [newNotificationDict pacoDescriptionForNotificationDict]);
    }
    //set the new notification dict, and save it to cache
    self.notificationDict = newNotificationDict;
    [self saveNotificationsToCache];
    [self adjustBadgeNumber];
  }
}

- (int)totalNumberOfActiveNotifications {
  @synchronized(self) {
    if (0 == [self.notificationDict count]) {
      return 0;
    }
    __block int totalNumber = 0;
    for (NSString* experimentId in self.notificationDict) {
      NSArray* notifications = (self.notificationDict)[experimentId];
      if (0 == [notifications count]) {
        continue;
      }
      [UILocalNotification pacoProcessNotifications:notifications
                                          withBlock:^(UILocalNotification* activeNotification,
                                                      NSArray* expiredNotifications,
                                                      NSArray* notFiredNotifications) {
                                            if (activeNotification) {
                                              totalNumber++;
                                            }
                                          }];
    }
    return totalNumber;
  }
}


- (void)cancelNotificationsForExperiment:(NSString*)experimentId {
  NSAssert([experimentId length] > 0, @"experimentId should be valid");
  @synchronized(self) {
    NSMutableArray* notifications = (self.notificationDict)[experimentId];
    if (notifications != nil) {
      NSAssert([notifications isKindOfClass:[NSMutableArray class]], @"should be NSMutableArray object");
      [UILocalNotification pacoCancelNotifications:notifications];
      [self.notificationDict removeObjectForKey:experimentId];
      DDLogInfo(@"New Notification Dict: %@", [self.notificationDict pacoDescriptionForNotificationDict]);
      //save the new notifications
      [self saveNotificationsToCache];
    }
    //Just in case, remove any notificaiton that still exists in OS system
    [UILocalNotification cancelScheduledNotificationsForExperiment:experimentId];
  }
}

- (void)cancelNotificationsForExperiments:(NSArray*)experimentIds {
  @synchronized(self) {
    if (0 == [experimentIds count]) {
      return;
    }
    for (NSString* experimentId in experimentIds) {
      NSAssert([experimentId isKindOfClass:[NSString class]], @"should be a valid ID");
      
      NSMutableArray* notifications = (self.notificationDict)[experimentId];
      if (notifications != nil) {
        NSAssert([notifications isKindOfClass:[NSMutableArray class]], @"should be NSMutableArray object");
        [UILocalNotification pacoCancelNotifications:notifications];
        [self.notificationDict removeObjectForKey:experimentId];
      }
      //Just in case, remove any notificaiton that still exists in OS system
      [UILocalNotification cancelScheduledNotificationsForExperiment:experimentId];
    }

    DDLogInfo(@"Finish Cancel Notifications for experiments: %@", experimentIds);
    DDLogInfo(@"New Notification Dict: %@", [self.notificationDict pacoDescriptionForNotificationDict]);
    //save the new notifications
    [self saveNotificationsToCache];
  }
}


- (UILocalNotification*)activeNotificationForExperiment:(NSString*)experimentId {
  NSAssert([experimentId length] > 0, @"experimentId should be valid");
  @synchronized(self) {
    NSMutableArray* notifications = (self.notificationDict)[experimentId];
    if (0 == [notifications count]) {
      return nil;
    }
    __block UILocalNotification* result = nil;
    [UILocalNotification pacoProcessNotifications:notifications
                                        withBlock:^(UILocalNotification* activeNotification,
                                                    NSArray* expiredNotifications,
                                                    NSArray* notFiredNotifications) {
                                          result = activeNotification;
    }];
    return result;
  }
}

- (BOOL)isNotificationActive:(UILocalNotification*)notification {
  @synchronized(self) {
    if (notification == nil) {
      return NO;
    }
    if ([notification pacoStatus] == PacoNotificationStatusTimeout) {
      return NO;
    }
    NSString* experimentId = [notification pacoExperimentId];
    NSAssert([experimentId length] > 0, @"experimentId should be nil");
    UILocalNotification* activeNotification = [self activeNotificationForExperiment:experimentId];
    BOOL isActive = activeNotification && [activeNotification pacoIsEqualTo:notification];
    return isActive;
  }
}

- (void)checkCorrectnessForExperiment:(NSString*)instanceIdToCheck {
  NSAssert([instanceIdToCheck length] > 0, @"instanceIdToCheck should be valid");
  NSArray* notifications = (self.notificationDict)[instanceIdToCheck];
  NSAssert([notifications count] == 0, @"shouldn't have any notifications!");
}

- (NSString*)notificationPlistPath {
  return [NSString pacoDocumentDirectoryFilePathWithName:kNotificationPlistName];
}

- (BOOL)saveNotificationsToCache {
  @synchronized (self) {
    NSData* data = [NSKeyedArchiver archivedDataWithRootObject:self.notificationDict];
    BOOL success = [data writeToFile:[self notificationPlistPath] atomically:YES];
    if (success) {
      DDLogInfo(@"Successfully saved notifications!");
    } else {
      DDLogInfo(@"Failed to save notifications!");
    }
    return success;
  }
}

- (BOOL)loadNotificationsFromCache {
  @synchronized (self) {
    NSError* error = nil;
    NSData* data = [NSData dataWithContentsOfFile:[self notificationPlistPath]
                                          options:NSDataReadingMappedIfSafe
                                            error:&error];
    if (error == nil) {
      self.notificationDict = (NSMutableDictionary*)[NSKeyedUnarchiver unarchiveObjectWithData:data];
    } else {
      self.notificationDict = [NSMutableDictionary dictionary];
    }
    self.areNotificationsLoaded = YES;

    BOOL hasError = (error != nil && ![error pacoIsFileNotExistError]);
    return !hasError;
  }
}


@end
