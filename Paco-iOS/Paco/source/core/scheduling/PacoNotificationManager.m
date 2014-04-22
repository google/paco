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


- (void)purgeCachedNotifications;

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
  int numOfActiveNotifications = [self totalNumberOfActiveNotifications];
  DDLogInfo(@"There are %d active notifications", numOfActiveNotifications);
  int badgeNumber = numOfActiveNotifications > 0 ? 1 : 0;
  DDLogInfo(@"Badge number set to %d", badgeNumber);
  [UIApplication sharedApplication].applicationIconBadgeNumber = badgeNumber;
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
 
 - For all scheduled but not fired notifications:
 a. cancel them from iOS
 b. delete them from the local cache
 **/
- (void)purgeCachedNotifications {
  DDLogInfo(@"Purge cached notifications...");
  [self processCachedNotificationsWithBlock:^(NSMutableDictionary* newNotificationDict,
                                              NSArray* expiredNotifications,
                                              NSArray* notFiredNotifications) {
    NSAssert(newNotificationDict, @"newNotificationDict should not be nil!");
    DDLogInfo(@"There are %lu expired notifications.", (unsigned long)[expiredNotifications count]);
    DDLogInfo(@"There are %lu not fired notifications.", (unsigned long)[notFiredNotifications count]);
    
    int numOfActiveNotifications = 0;
    for (NSString* experimentId in newNotificationDict) {
      numOfActiveNotifications += [newNotificationDict[experimentId] count];
    }
    DDLogInfo(@"There are %d active notifications.", numOfActiveNotifications);
    
    self.notificationDict = newNotificationDict;
    if (expiredNotifications) {
      [UILocalNotification pacoCancelNotifications:expiredNotifications];
      [self handleExpiredNotifications:expiredNotifications];
    }
    if (notFiredNotifications) {
      [UILocalNotification pacoCancelNotifications:notFiredNotifications];
    }
  }];
}

- (void)addNotifications:(NSArray*)allNotifications {
  if (0 == [allNotifications count]) {
    return;
  }
  
  @synchronized(self) {
    NSDictionary* sortedNotificationDict = [UILocalNotification sortNotificationsPerExperiment:allNotifications];
    for (NSString* experimentId in sortedNotificationDict) {
      NSArray* sortedNotifications = sortedNotificationDict[experimentId];
      NSMutableArray* currentNotifications = (self.notificationDict)[experimentId];
      if (currentNotifications == nil) {
        currentNotifications = [NSMutableArray arrayWithCapacity:[sortedNotifications count]];
      }
      NSAssert([currentNotifications isKindOfClass:[NSMutableArray class]],
               @"currentNotifications should be an array!");
      //clean duplicate notification with the same fireDate just to be safe
      [currentNotifications addObjectsFromArray:sortedNotifications];
      NSMutableArray* nonDuplicate = [currentNotifications pacoSortLocalNotificationsAndRemoveDuplicates];
      (self.notificationDict)[experimentId] = nonDuplicate;
    }
  }
}

- (NSUInteger)numOfScheduledNotifications {
  return [[UIApplication sharedApplication].scheduledLocalNotifications count];
}

- (BOOL)hasMaximumScheduledNotifications {
  return (kTotalNumOfNotifications == [self numOfScheduledNotifications]);
}

- (void)schedulePacoNotifications:(NSArray*)notifications {
  @synchronized(self) {
    [self purgeCachedNotifications];
    
    if ([notifications count] > 0) {
      [self addNotifications:notifications];
    }
    //save the new notifications
    [self saveNotificationsToCache];
    
    DDLogInfo(@"%@", [self.notificationDict pacoDescriptionForNotificationDict]);
    
    /*
     schedule the new notifications, and don't use the following code to set local notifications,
     since it will clear all notifications in the notification center:
     [UIApplication sharedApplication].scheduledLocalNotifications = notifications;
     **/
    for (UILocalNotification* notification in notifications) {
      [[UIApplication sharedApplication] scheduleLocalNotification:notification];
    }
    
    [self adjustBadgeNumber];
  }
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
    if (activeNotification && [activeNotification pacoIsEqualTo:notification]) {
      return YES;
    } else {
      return NO;
    }
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
    
    if (error != nil && ![error pacoIsFileNotExistError]) {
      return NO;
    } else {
      return YES;
    }
  }
}


@end
