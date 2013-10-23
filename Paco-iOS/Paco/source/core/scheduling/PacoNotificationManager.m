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

@interface PacoNotificationManager ()
@property (atomic, retain, readwrite) NSMutableDictionary* notificationDict;
@property (nonatomic, weak, readwrite) id<PacoNotificationManagerDelegate> delegate;

- (void)purgeCachedNotifications;

@end

@implementation PacoNotificationManager

- (id)init {
  self = [super init];
  if (self) {
    _notificationDict = [[NSMutableDictionary alloc] init];
  }
  return self;
}

+ (PacoNotificationManager*)managerWithDelegate:(id)delegate {
  PacoNotificationManager* manager = [[PacoNotificationManager alloc] init];
  manager.delegate = delegate;
  return manager;
}

- (NSDictionary*)copyOfNotificationDictionary {
  return [self.notificationDict copy];
}

- (BOOL)addNotification:(UILocalNotification*)notification withHashKey:(NSString*)hashKey {
  @synchronized(self) {
    NSAssert(notification != nil, @"notification should be valid!");
    NSAssert(hashKey.length > 0, @"hashKey should be valid!");
    
    BOOL success = NO;
    if ([self.notificationDict objectForKey:hashKey] == nil) {
      success = YES;
    }
    [self.notificationDict setObject:notification forKey:hashKey];
    return success;
  }
}

- (BOOL)deleteNotificationWithHashKey:(NSString*)hashKey {
  @synchronized(self) {
    NSAssert(hashKey.length > 0, @"hashKey should be valid!");
    UILocalNotification* noti = [self.notificationDict objectForKey:hashKey];
    if (noti == nil) {
      return NO;
    } else {
      [self.notificationDict removeObjectForKey:hashKey];
      return YES;
    }    
  }
}

- (void)handleExpiredNotifications:(NSArray*)expiredNotifications {
  [UILocalNotification pacoCancelNotifications:expiredNotifications];
  [self.delegate handleExpiredNotifications:expiredNotifications];
}

- (void)processCachedNotificationsWithBlock:(void(^)(NSMutableDictionary*, NSArray*, NSArray*))block {
  NSMutableDictionary* newDict = [NSMutableDictionary dictionaryWithCapacity:[self.notificationDict count]];
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
    self.notificationDict = newNotificationDict;
    if (expiredNotifications) {
      [self handleExpiredNotifications:expiredNotifications];
    }
    if (notFiredNotifications) {
      [UILocalNotification pacoCancelNotifications:notFiredNotifications];
    }
  }];
}

- (void)addNotifications:(NSArray*)allNotifications {
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
      [currentNotifications addObjectsFromArray:sortedNotifications];
      [self.notificationDict setObject:currentNotifications forKey:experimentId];
    }
  }
}

- (void)scheduleNotifications:(NSArray*)notifications {
  @synchronized(self) {
    [self purgeCachedNotifications];
    [self addNotifications:notifications];
    //save the new notifications
    [self saveNotificationsToFile];
    //schedule the new notifications
    [UIApplication sharedApplication].scheduledLocalNotifications = notifications;
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

- (NSMutableArray*)loadNotificationsFromFile {
  NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString* documentsDirectory = [paths objectAtIndex:0];
  NSString* filePath = [documentsDirectory stringByAppendingString:@"/notifications.plist"];
  
  return [NSMutableArray arrayWithContentsOfFile:filePath];
}

- (BOOL)saveNotificationsToFile {
  NSMutableArray* notificationArray = [[NSMutableArray alloc] init];
  NSDictionary* notificationDict = [self copyOfNotificationDictionary];
  
  for(NSString* notificationHash in notificationDict) {
    UILocalNotification* notification = [notificationDict objectForKey:notificationHash];
    
    NSMutableDictionary *saveDict = [NSMutableDictionary dictionary];
    [saveDict setValue:[notification.userInfo objectForKey:@"experimentInstanceId"] forKey:@"experimentInstanceId"];
    [saveDict setValue:[notification.userInfo objectForKey:@"experimentFireDate"] forKey:@"experimentFireDate"];
    [saveDict setValue:[notification.userInfo objectForKey:@"experimentTimeOutDate"] forKey:@"experimentTimeOutDate"];
    [saveDict setValue:notification.alertBody forKey:@"experimentAlertBody"];
    
    [notificationArray addObject:saveDict];
  }
  
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *documentsDirectory = [paths objectAtIndex:0];
  NSString *filePath = [documentsDirectory stringByAppendingString:@"/notifications.plist"];
  return [notificationArray writeToFile:filePath atomically:YES];
}

@end
