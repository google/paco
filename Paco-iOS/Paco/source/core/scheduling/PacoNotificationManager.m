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
#import "PacoDate.h"
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
  @synchronized(self) {
    NSAssert(notification != nil, @"notification should be valid!");
    NSAssert(hashKey.length > 0, @"hashKey should be valid!");
    
    BOOL success = NO;
    if ([self.notificationDict objectForKey:hashKey] == nil) {
      success = YES;
    }
//    NSLog(@"%@: ADD key:%@, notification:%@", success?@"Success":@"Fail",
//          hashKey, [PacoDate pacoStringForDate:[notification.userInfo objectForKey:@"experimentFireDate"]]);
    [self.notificationDict setObject:notification forKey:hashKey];
    return success;
  }
}

- (BOOL)deleteNotificationWithHashKey:(NSString*)hashKey {
  @synchronized(self) {
    NSAssert(hashKey.length > 0, @"hashKey should be valid!");
    UILocalNotification* noti = [self.notificationDict objectForKey:hashKey];
    if (noti == nil) {
//      NSLog(@"Fail: DELETE key:%@, notification:%@",
//            hashKey, [PacoDate pacoStringForDate:[noti.userInfo objectForKey:@"experimentFireDate"]]);
      return NO;
    } else {
//      NSLog(@"Success: DELETE key:%@, notification:%@",
//            hashKey, [PacoDate pacoStringForDate:[noti.userInfo objectForKey:@"experimentFireDate"]]);
      [self.notificationDict removeObjectForKey:hashKey];
      return YES;
    }    
  }
}


- (NSTimeInterval)nearestTimerInterval {
  @synchronized(self) {
    NSTimeInterval interval = MAXFLOAT;
    
    for (NSString* notificationHash in self.notificationDict) {
      UILocalNotification* noti = [self.notificationDict objectForKey:notificationHash];
      NSDate* fireDate = [noti.userInfo objectForKey:@"experimentFireDate"];
      NSTimeInterval timerInterval = [fireDate timeIntervalSinceNow];
      //notification fired, fetch the time out interval
      if (timerInterval <= 0) {
        NSDate* timeOutDate = [noti.userInfo objectForKey:@"experimentTimeOutDate"];
        NSAssert(timeOutDate != nil, @"");
        NSTimeInterval timerInterval = [timeOutDate timeIntervalSinceNow];
        
        if (timerInterval > 0) {
          if (interval > timerInterval) {
            interval = timerInterval;
          }
        } else {
          NSLog(@"ERROR: timerInterval should probably be larger than 0, maybe a bug!");
        }
      } else {
        if (interval > timerInterval) {
          interval = timerInterval;
        }
      }
    }
    
    if (interval == MAXFLOAT) {
      return 0;
    } else {
      NSTimeInterval offset = 1;
      return interval + offset;
      
    }
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
