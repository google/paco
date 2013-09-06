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

@interface PacoNotificationManager ()
@property (atomic, retain, readwrite) NSMutableDictionary* notificationDict;
@end

@implementation PacoNotificationManager

- (id)init {
  self = [super init];
  if (self) {
    _notificationDict = [[NSMutableDictionary alloc] init];
  }
  return self;
}

- (NSDictionary*)copyOfNotificationDictionary {
  return [self.notificationDict copy];
}

- (BOOL)addNotification:(UILocalNotification*)notification withHashKey:(NSString*)hashKey {
  NSAssert(notification != nil, @"notification should be valid!");
  NSAssert(hashKey.length > 0, @"hashKey should be valid!");
  
  BOOL success = NO;
  if ([self.notificationDict objectForKey:hashKey] == nil) {
    success = YES;
  }
  [self.notificationDict setObject:notification forKey:hashKey];
  return success;
}

- (BOOL)deleteNotificationWithHashKey:(NSString*)hashKey {
  NSAssert(hashKey.length > 0, @"hashKey should be valid!");
  if ([self.notificationDict objectForKey:hashKey] == nil) {
    return NO;
  } else {
    [self.notificationDict removeObjectForKey:hashKey];
    return YES;
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
    if ([notification.userInfo objectForKey:@"experimentEsmSchedule"] != nil) {
      [saveDict setValue:[notification.userInfo objectForKey:@"experimentEsmSchedule"] forKey:@"experimentEsmSchedule"];
    }
    
    [notificationArray addObject:saveDict];
  }
  
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *documentsDirectory = [paths objectAtIndex:0];
  NSString *filePath = [documentsDirectory stringByAppendingString:@"/notifications.plist"];
  return [notificationArray writeToFile:filePath atomically:YES];
}

@end
