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

static NSString* kNotificationPlistName = @"notificationDictionary.plist";

@interface PacoNotificationManager ()
@property (atomic, retain, readwrite) NSMutableDictionary* notificationDict;
@property (nonatomic, weak, readwrite) id<PacoNotificationManagerDelegate> delegate;

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


- (void)cancelAlliOSNotifications {
  NSLog(@"Cancel All Local Notifications!");
  [[UIApplication sharedApplication] cancelAllLocalNotifications];
}

//when notification system needs to be shut down, Paco needs to:
//a. cancel all notifications from iOS system
//b. check if there are any expired notifications, and save survey missing events for them
//c. set notification dictionary to be empty
- (void)cancelAllPacoNotifications {
  [self cancelAlliOSNotifications];

  [self processCachedNotificationsWithBlock:^(NSMutableDictionary* newNotificationDict,
                                              NSArray* expiredNotifications,
                                              NSArray* notFiredNotifications) {
    if (expiredNotifications) {
      [self.delegate handleExpiredNotifications:expiredNotifications];
    }
  }];

  //reset notification dictionary
  self.notificationDict = [NSMutableDictionary dictionary];
  [self saveNotificationsToCache];
}

- (void)handleRespondedNotification:(UILocalNotification*)notification {
  if (notification == nil) {
    return;
  }
  //Since this notification is responded successfully, cancelling it will clear it from the notification tray
  [[UIApplication sharedApplication] cancelLocalNotification:notification];

  //remove this notification from local cache
  NSString* experimentId = [notification pacoExperimentId];
  NSAssert(experimentId, @"experimentId should be valid");
  NSMutableArray* notifications = [self.notificationDict objectForKey:experimentId];
  if (0 == [notifications count]) {
    return;
  }
  [notifications removeObject:notification];
  [self.notificationDict setObject:notifications forKey:experimentId];
  [self saveNotificationsToCache];
}


- (void)processCachedNotificationsWithBlock:(void(^)(NSMutableDictionary*, NSArray*, NSArray*))block {
  NSMutableDictionary* newDict = [NSMutableDictionary dictionary];
  if (0 == [self.notificationDict count]) {
    block(newDict, nil, nil);
    return;
  }
  
  NSMutableArray* allExpiredNotifications = [NSMutableArray array];
  NSMutableArray* allNotFiredNotifications = [NSMutableArray array];
  
  for (NSString* experimentId in self.notificationDict) {
    NSArray* notifications = [self.notificationDict objectForKey:experimentId];
    if (0 == [notifications count]) {
      continue;
    }
    NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                       NSArray* expiredNotifications,
                                       NSArray* notFiredNotifications) {
      if (activeNotification != nil) {
        [newDict setObject:[NSMutableArray arrayWithObject:activeNotification] forKey:experimentId];
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
  [self processCachedNotificationsWithBlock:^(NSMutableDictionary* newNotificationDict,
                                              NSArray* expiredNotifications,
                                              NSArray* notFiredNotifications) {
    NSAssert(newNotificationDict, @"newNotificationDict should not be nil!");
    self.notificationDict = newNotificationDict;
    if (expiredNotifications) {
      [UILocalNotification pacoCancelNotifications:expiredNotifications];
      [self.delegate handleExpiredNotifications:expiredNotifications];
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
      NSArray* sortedNotifications = [sortedNotificationDict objectForKey:experimentId];
      NSMutableArray* currentNotifications = [self.notificationDict objectForKey:experimentId];
      if (currentNotifications == nil) {
        currentNotifications = [NSMutableArray arrayWithCapacity:[sortedNotifications count]];
      }
      NSAssert([currentNotifications isKindOfClass:[NSMutableArray class]],
               @"currentNotifications should be an array!");
      //clean duplicate notification with the same fireDate just to be safe
      [currentNotifications addObjectsFromArray:sortedNotifications];
      NSMutableArray* nonDuplicate = [currentNotifications pacoSortLocalNotificationsAndRemoveDuplicates];
      [self.notificationDict setObject:nonDuplicate forKey:experimentId];
    }
  }
}

- (BOOL)hasMaximumScheduledNotifications {
  return (kTotalNumOfNotifications == [[UIApplication sharedApplication].scheduledLocalNotifications count]);
}

- (void)schedulePacoNotifications:(NSArray*)notifications {
  @synchronized(self) {
    [self purgeCachedNotifications];
    
    if ([notifications count] > 0) {
      [self addNotifications:notifications];
    }
    //save the new notifications
    [self saveNotificationsToCache];
    
    //schedule the new notifications
    [UIApplication sharedApplication].scheduledLocalNotifications = notifications;
  }
}

- (void)cancelNotificationsForExperiment:(NSString*)experimentId {
  NSAssert([experimentId length] > 0, @"experimentId should be valid");
  @synchronized(self) {
    NSMutableArray* notifications = [self.notificationDict objectForKey:experimentId];
    if (notifications != nil) {
      NSAssert([notifications isKindOfClass:[NSMutableArray class]], @"should be NSMutableArray object");
      [UILocalNotification pacoCancelNotifications:notifications];
      [self.notificationDict removeObjectForKey:experimentId];
      //save the new notifications
      [self saveNotificationsToCache];
    }
    //Just in case, remove any notificaiton that still exists in OS system
    [UILocalNotification cancelScheduledNotificationsForExperiment:experimentId];
  }
}

- (void)checkCorrectnessForExperiment:(NSString*)instanceIdToCheck {
  //check cached notifications
  BOOL hasScheduledNotification = NO;
  for(NSString* notificationHash in self.notificationDict) {
    UILocalNotification* notification = [self.notificationDict objectForKey:notificationHash];
    NSString* experimentInstanceId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    NSAssert(experimentInstanceId.length > 0, @"experimentInstanceId should be valid!");
    if ([experimentInstanceId isEqualToString:instanceIdToCheck]) {
      hasScheduledNotification = YES;
      break;
    }
  }
  NSAssert(!hasScheduledNotification, @"shouldn't have any scheduled notifications!");
}

- (NSString*)notificationPlistPath {
  return [NSString pacoDocumentDirectoryFilePathWithName:kNotificationPlistName];
}

- (BOOL)saveNotificationsToCache {
  NSData* data = [NSKeyedArchiver archivedDataWithRootObject:self.notificationDict];
  return [data writeToFile:[self notificationPlistPath] atomically:YES];
}

- (BOOL)loadNotificationsFromCache {
  NSError* error = nil;
  NSData* data = [NSData dataWithContentsOfFile:[self notificationPlistPath]
                                        options:NSDataReadingMappedIfSafe
                                          error:&error];
  if (error == nil) {
    self.notificationDict = (NSMutableDictionary*)[NSKeyedUnarchiver unarchiveObjectWithData:data];
  } else {
    self.notificationDict = [NSMutableDictionary dictionary];
  }
  if (error != nil && ![error pacoIsFileNotExistError]) {
    return NO;
  } else {
    return YES;
  }
}


@end
