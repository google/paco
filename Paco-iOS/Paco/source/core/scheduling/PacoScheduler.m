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
//designated initializer
- (id)init {
  self = [super init];
  if (self) {
    [self cancelAlliOSLocalNotifications];
    _iOSLocalNotifications = [[NSMutableDictionary alloc] init];
  }
  return self;
}

- (void)registerSchedulesWithOS:(NSArray *)experiments {
  for (PacoExperiment *experiment in experiments) {
    [self registeriOSNotificationForExperiment:experiment];
  }
}

- (void)canceliOSNotificationsForExperimentId:(NSString *)experimentId {
  NSArray *notifications = [self getiOSLocalNotifications:experimentId];
  for (UILocalNotification *notification in notifications) {
    [[UIApplication sharedApplication] cancelLocalNotification:notification];
  }
}

- (void)registeriOSNotificationForExperiment:(PacoExperiment *)experiment {
  NSDate *now = [NSDate dateWithTimeIntervalSinceNow:0];
  UILocalNotification *notification = [[UILocalNotification alloc] init];
  
  notification.fireDate = [PacoDate nextScheduledDateForExperiment:experiment fromThisDate:now];
  
  NSLog(@"Paco iOS notification with ExperimentId=%@ on fireDate=%@ is scheduled in iOS", experiment.instanceId, [self getTimeZoneFormattedDateString:notification.fireDate]);
  
  NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
  assert(experiment.instanceId.length);
  [userInfo setObject:experiment.instanceId forKey:@"experimentInstanceId"];
  [userInfo setObject:[notification.fireDate dateByAddingTimeInterval:(experiment.schedule.timeout * 60)] forKey:@"experimentTimeOutDate"];
  
  // put the esm schedule in the notification dictionary
  NSArray *scheduleDates = nil;
  if (experiment.schedule.scheduleType == kPacoScheduleTypeESM) {
    scheduleDates = experiment.schedule.esmSchedule;
    if (!scheduleDates.count) {
      scheduleDates = [PacoDate createESMScheduleDates:experiment fromThisDate:now];
      experiment.schedule.esmSchedule = scheduleDates;
    }
    [userInfo setObject:scheduleDates forKey:@"esmSchedule"];
  }
  
  notification.userInfo = userInfo;
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.alertBody = [NSString stringWithFormat:@"Paco experiment %@ at %@.", experiment.instanceId, [self getTimeZoneFormattedDateString:notification.fireDate]];
  notification.soundName = @"deepbark_trial.mp3";
  notification.applicationIconBadgeNumber += 1;
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  
  // now that it's scheduled we need to start bookkeeping it
  UILocalNotification* notificationObject = [self getiOSLocalNotification:experiment.instanceId fireDate:notification.fireDate];
  NSString* notificationKey = [NSString stringWithFormat:@"%@%.0f", experiment.instanceId, [notification.fireDate timeIntervalSince1970]];
  NSAssert(notificationObject != nil, @"notification shouldn't be nil");
  [_iOSLocalNotifications setObject:notificationObject forKey:notificationKey];
}

- (NSString*)getTimeZoneFormattedDateString:(NSDate*)date {
  NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
  [dateFormat setDateFormat:@"MMM dd, YYYY HH:mm:ssZZZ"];
  [dateFormat setTimeZone:[NSTimeZone systemTimeZone]];
  
  return [dateFormat stringFromDate:date];
}

#pragma mark functions for interacting with iOSs LocalNotification System
- (void)cancelAlliOSLocalNotifications {
  [[UIApplication sharedApplication] cancelAllLocalNotifications];
}

// This will clear previous iOS Local Notifications from Notification Center
- (void)cancelExpirediOSLocalNotifications: (NSArray *)experiments {
  NSDictionary* iosLocalNotifications = [_iOSLocalNotifications copy];
  
  for(NSString* notificationHash in iosLocalNotifications) {
    UILocalNotification* notification = [iosLocalNotifications objectForKey:notificationHash];
    NSDate* timeOutDate = [notification.userInfo objectForKey:@"experimentTimeOutDate"];
    NSString* experimentInstanceId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    
    if ([timeOutDate timeIntervalSinceNow] < 0) {
      NSLog(@"Paco cancelling iOS notification for %@", experimentInstanceId);
      [[UIApplication sharedApplication] cancelLocalNotification:notification];
      // remove it from our notifications
      [_iOSLocalNotifications removeObjectForKey:notificationHash];
      // the firedate + timeout falls before now, so the notification has expired and should be deleted
      // Let the server know about this Missed Responses/Signals
      // TODO TPE: let the server know about the Missed Responses/Signals
      // [[PacoClient sharedInstance].service
    }
  }
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

- (void)updateiOSNotifications: (NSArray *)experiments {
  [self cancelExpirediOSLocalNotifications:experiments];
  [self registerUpcomingiOSNotifications:experiments];
}

- (void)handleLocalNotification:(UILocalNotification *)notification {
  NSLog(@"LOCAL NOTIFICATION INFO = %@", notification.userInfo);

  // make sure to decrement the Application Badge Number
  UIApplication *application = [UIApplication sharedApplication];
  application.applicationIconBadgeNumber = notification.applicationIconBadgeNumber - 1;
  
  NSString *experimentId = [notification.userInfo objectForKey:@"experimentInstanceId"];
  PacoExperiment *experiment = [[PacoClient sharedInstance].model experimentForId:experimentId];
  NSArray *esmSchedule = [notification.userInfo objectForKey:@"esmSchedule"];
  if (esmSchedule) {
    experiment.schedule.esmSchedule = esmSchedule;
  }
  [self canceliOSNotificationsForExperimentId:experimentId];
  [self registeriOSNotificationForExperiment:experiment];
}

@end
