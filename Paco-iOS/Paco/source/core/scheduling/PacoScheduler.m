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

#import "PacoScheduler.h"

#import "PacoClient.h"
#import "PacoConfig.h"
#import "PacoDate.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperiment.h"

@interface PacoScheduler ()
@property (retain, readwrite) NSMutableDictionary* iOSLocalNotifications;
@end

@implementation PacoScheduler

#pragma mark Object Lifecycle
- (id)init {
  self = [super init];
  if (self) {
    _iOSLocalNotifications = [[NSMutableDictionary alloc] init];

    // After a reboot or restart of our application we clear all notifications
    [self cancelAlliOSLocalNotifications];
    // Load notifications from the plist
    NSMutableArray* notificationArray = [self readScheduleFromFile];
    // Reschedule the ones that have already fired
    [self rescheduleLocalNotifications:notificationArray];
  }
  return self;
}

-(bool) writeEventsToFile {
  NSMutableArray* notificationArray = [[NSMutableArray alloc] init];
  NSDictionary* iosLocalNotifications = [_iOSLocalNotifications copy];
  
  for(NSString* notificationHash in iosLocalNotifications) {
    UILocalNotification* notification = [iosLocalNotifications objectForKey:notificationHash];
    
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

-(NSMutableArray*) readScheduleFromFile {
  NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString* documentsDirectory = [paths objectAtIndex:0];
  NSString* filePath = [documentsDirectory stringByAppendingString:@"/notifications.plist"];

  return [NSMutableArray arrayWithContentsOfFile:filePath];
}

-(void) rescheduleLocalNotifications:(NSMutableArray*) schedule {
  for(NSDictionary* notificationDictionary in schedule) {
    NSString* experimentInstanceId = [notificationDictionary valueForKey:@"experimentInstanceId"];
    NSDate* experimentFireDate =[notificationDictionary valueForKey:@"experimentFireDate"];
    NSDate* experimentTimeOutDate =[notificationDictionary valueForKey:@"experimentTimeOutDate"];
    NSString* experimentAlertBody = [notificationDictionary valueForKey:@"experimentAlertBody"];
    NSArray* experimentEsmSchedule = [notificationDictionary valueForKey:@"experimentEsmSchedule"];
    
    // if this notification has already timed out, we should let the deligate know so he can notify the server
    if (([experimentTimeOutDate timeIntervalSinceNow] <= 0)) {
      [_delegate handleEventTimeOut:experimentInstanceId experimentFireDate:experimentFireDate];
    } else {
      [self registeriOSNotification:experimentInstanceId experimentFireDate:experimentFireDate experimentTimeOutDate:experimentTimeOutDate experimentEsmSchedule:experimentEsmSchedule experimentAlertBody:experimentAlertBody];
    }
  }
}

#pragma mark Public Methods
-(void)addEvent:(PacoExperiment*) experiment
    experiments:(NSArray*) experiments {
  [self update:experiments];
}

-(void)removeEvent:(PacoExperiment*) experiment
            experiments:(NSArray*) experiments {
  NSDictionary* iosLocalNotifications = [_iOSLocalNotifications copy];
  
  for(NSString* notificationHash in iosLocalNotifications) {
    UILocalNotification* notification = [iosLocalNotifications objectForKey:notificationHash];
    NSString* experimentInstanceId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    
    if ([experiment.instanceId isEqualToString:experimentInstanceId]) {
      NSLog(@"Paco removing iOS notification for %@", experimentInstanceId);
      [[UIApplication sharedApplication] cancelLocalNotification:notification];
      // the firedate + timeout falls before now, so the notification has expired and should be deleted
      [_iOSLocalNotifications removeObjectForKey:notificationHash];
    }
  }
}

-(void)update:(NSArray *)experiments {
  [self cancelExpirediOSLocalNotifications:experiments];
  [self registerUpcomingiOSNotifications:experiments];
}

- (void)handleEvent:(UILocalNotification *)notification
         experiments:(NSArray*) experiments {
  NSLog(@"Paco handling an iOS notification = %@", notification.userInfo);
  
  // make sure to decrement the Application Badge Number
  UIApplication *application = [UIApplication sharedApplication];
  application.applicationIconBadgeNumber = notification.applicationIconBadgeNumber - 1;
  
  NSString *experimentId = [notification.userInfo objectForKey:@"experimentInstanceId"];
  PacoExperiment *experiment = [[PacoClient sharedInstance].model experimentForId:experimentId];
  NSArray *esmSchedule = [notification.userInfo objectForKey:@"esmSchedule"];
  if (esmSchedule) {
    experiment.schedule.esmSchedule = esmSchedule;
  }
  
  [self removeEvent:experiment experiments:experiments];
  [self registerUpcomingiOSNotifications:experiments];
}

- (void)canceliOSNotificationsForExperimentId:(NSString *)experimentId {
  NSArray *notifications = [self getiOSLocalNotifications:experimentId];
  for (UILocalNotification *notification in notifications) {
    [[UIApplication sharedApplication] cancelLocalNotification:notification];
  }
}

- (void)registeriOSNotificationForExperiment:(PacoExperiment *)experiment {
  NSDate* now = [NSDate dateWithTimeIntervalSinceNow:0];
  NSDate* experimentFireDate = [PacoDate nextScheduledDateForExperiment:experiment fromThisDate:now];
  NSDate* experimentTimeOutDate = [experimentFireDate dateByAddingTimeInterval:(experiment.schedule.timeout * 60)];
  assert(experiment.instanceId.length);
  
  // put the esm schedule in the notification dictionary
  NSArray *scheduleDates = nil;
  if (experiment.schedule.scheduleType == kPacoScheduleTypeESM) {
    scheduleDates = experiment.schedule.esmSchedule;
    if (!scheduleDates.count) {
      scheduleDates = [PacoDate createESMScheduleDates:experiment fromThisDate:now];
      experiment.schedule.esmSchedule = scheduleDates;
    }
  }
  
  [self registeriOSNotification:experiment.instanceId experimentFireDate:experimentFireDate experimentTimeOutDate:experimentTimeOutDate experimentEsmSchedule:scheduleDates experimentAlertBody:[NSString stringWithFormat:@"Paco experiment %@ at %@.", experiment.instanceId, [self getTimeZoneFormattedDateString:experimentFireDate]]];
}

- (void)registeriOSNotification:(NSString*) experimentInstanceId
             experimentFireDate:(NSDate*) experimentFireDate
          experimentTimeOutDate:(NSDate*) experimentTimeOutDate
          experimentEsmSchedule:(NSArray*) experimentEsmSchedule
            experimentAlertBody:(NSString*) experimentAlertBody {
  UILocalNotification *notification = [[UILocalNotification alloc] init];
  
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.alertBody = experimentAlertBody;
  notification.soundName = @"deepbark_trial.mp3";
  notification.applicationIconBadgeNumber += 1;

  NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
  [userInfo setObject:experimentInstanceId forKey:@"experimentInstanceId"];
  [userInfo setObject:experimentFireDate forKey:@"experimentFireDate"];
  [userInfo setObject:experimentTimeOutDate forKey:@"experimentTimeOutDate"];
  if (experimentEsmSchedule) {
    [userInfo setObject:experimentEsmSchedule forKey:@"experimentEsmSchedule"];
  }
  
  // this logic is for when we're loading notifications from a file that should have fired
  // in the past: we want them to fire right away (so they show up in Notification Center),
  // but by setting the hasFired object in userInfo object we make sure that the UI doesn't show them
  if (([experimentFireDate timeIntervalSinceNow] <= 0)) {
    notification.fireDate = [[NSDate date] dateByAddingTimeInterval:5];
    [userInfo setObject:@"true" forKey:@"experimentHasFired"];
  } else {
    notification.fireDate = experimentFireDate;
    [userInfo setObject:@"false" forKey:@"experimentHasFired"];
  }
  notification.userInfo = userInfo;
 [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  
  // now that it's scheduled we need to start bookkeeping it
  UILocalNotification* notificationObject = [self getiOSLocalNotification:experimentInstanceId fireDate:notification.fireDate];
  NSString* notificationKey = [NSString stringWithFormat:@"%@%.0f", experimentInstanceId, [notification.fireDate timeIntervalSince1970]];
  NSAssert(notificationObject != nil, @"notification shouldn't be nil");
  [_iOSLocalNotifications setObject:notificationObject forKey:notificationKey];
}

- (NSString*)getTimeZoneFormattedDateString:(NSDate*)date {
  NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
  [dateFormat setDateFormat:@"MMM dd, YYYY HH:mm:ssZZZ"];
  [dateFormat setTimeZone:[NSTimeZone systemTimeZone]];
  
  return [dateFormat stringFromDate:date];
}

#pragma mark functions for interacting with iOS's LocalNotification System
- (void)cancelAlliOSLocalNotifications {
  [[UIApplication sharedApplication] cancelAllLocalNotifications];
}

- (UILocalNotification*)getiOSLocalNotification:(NSString*)experimentInstanceId fireDate:(NSDate*)fireDate {
  NSArray* notificationArray = [self getiOSLocalNotifications:experimentInstanceId];
  for (UILocalNotification* notification in notificationArray) {
    if (([notification.fireDate timeIntervalSinceDate:fireDate] >= 0) && ([notification.fireDate timeIntervalSinceDate:fireDate] < 60)) {
      return notification;
    }
  }
  return nil;
}

- (NSArray*)getiOSLocalNotifications:(NSString*)experimentInstanceId {
  NSMutableArray *array = [NSMutableArray array];
  for (UILocalNotification *notification in [UIApplication sharedApplication].scheduledLocalNotifications) {
    NSString *expId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    if ([expId isEqualToString:experimentInstanceId]) {
      [array addObject:notification];
    }
  }
  return array;
}

- (void)registerUpcomingiOSNotifications: (NSArray *)experiments {
  // go through all experiments, see if a notification is already scheduled, and if not add it to the schedule
  for (PacoExperiment *experiment in experiments) {
    if ([self getiOSLocalNotifications:experiment.instanceId].count == 0) {
      // ok, so no notification exists, so schedule one
      NSLog(@"Paco registering iOS notification for %@", experiment.instanceId);
      [self registeriOSNotificationForExperiment:experiment];
    }
  }
}

// This will clear iOS Local Notifications that have fired from Notification Center and notify the deligate that they've timed out
- (void)cancelExpirediOSLocalNotifications: (NSArray *)experiments {
  NSDictionary* iosLocalNotifications = [_iOSLocalNotifications copy];
  
  for(NSString* notificationHash in iosLocalNotifications) {
    UILocalNotification* notification = [iosLocalNotifications objectForKey:notificationHash];
    NSDate* timeOutDate = [notification.userInfo objectForKey:@"experimentTimeOutDate"];
    NSString* experimentInstanceId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    
    if ([timeOutDate timeIntervalSinceNow] < 0) {
      NSLog(@"Paco cancelling iOS notification for %@", experimentInstanceId);
      [[UIApplication sharedApplication] cancelLocalNotification:notification];
      // the firedate + timeout falls before now, so the notification has expired and should be deleted
      [_iOSLocalNotifications removeObjectForKey:notificationHash];
      // Let the server know about this Missed Responses/Signals
      [_delegate handleEventTimeOut:experimentInstanceId experimentFireDate:[notification.userInfo objectForKey:@"experimentFireDate"]];
    }
  }
}
@end
