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
#import "PacoNotificationManager.h"

NSString* const kExperimentHasFiredKey = @"experimentHasFired";


@interface PacoNotificationManager ()
- (void)checkCorrectnessForExperiment:(NSString*)instanceIdToCheck;

@end


@interface PacoScheduler ()
@property (nonatomic, assign) id<PacoSchedulerDelegate> delegate;
@property (atomic, retain) PacoNotificationManager* notificationManager;

@end

@implementation PacoScheduler

#pragma mark Object Lifecycle
- (id)init {
  self = [super init];
  if (self) {
    _notificationManager = [[PacoNotificationManager alloc] init];

    // After a reboot or restart of our application we clear all notifications
    NSLog(@"Cancel All LocalNotifications!");
    [self cancelAlliOSLocalNotifications];
    
    // Load notifications from the plist
    NSMutableArray* notificationArray = [_notificationManager loadNotificationsFromFile];
    NSLog(@"Reschedule %d LocalNotifications from file!", [notificationArray count]);
    // Reschedule the ones that have already fired
    [self rescheduleLocalNotifications:notificationArray];
  }
  return self;
}

+ (PacoScheduler*)schedulerWithDelegate:(id<PacoSchedulerDelegate>)delegate {
  PacoScheduler* scheduler = [[PacoScheduler alloc] init];
  scheduler.delegate = delegate;
  return scheduler;
}

- (BOOL)saveNotificationsToFile {
  return [self.notificationManager saveNotificationsToFile];
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
      [self.delegate handleNotificationTimeOut:experimentInstanceId experimentFireDate:experimentFireDate];
    } else {
      [self registeriOSNotification:experimentInstanceId
                 experimentFireDate:experimentFireDate
              experimentTimeOutDate:experimentTimeOutDate
              experimentEsmSchedule:experimentEsmSchedule
                experimentAlertBody:experimentAlertBody];
    }
  }
}


- (void)updateTimer {
  NSTimeInterval timerInterval = [self.notificationManager nearestTimerInterval];
  [self.delegate updateTimerInterval:timerInterval];
}


#pragma mark Public Methods
-(void)startSchedulingForExperimentIfNeeded:(PacoExperiment*)experiment {
  if (![experiment shouldScheduleNotifications]) {
    NSLog(@"Skip scheduling for newly-joined self-report or advanced expeirment: %@", experiment.instanceId);
    return;
  }
  
  //check schedule notifications
  NSArray* scheduledArr = [self getiOSLocalNotifications:experiment.instanceId];
  NSAssert([scheduledArr count] == 0, @"There should be 0 notfications scheduled!");
  [self.notificationManager checkCorrectnessForExperiment:experiment.instanceId];
  
  NSLog(@"Start scheduling notifications for newly joined experiment: %@", experiment.instanceId);
  [self registeriOSNotificationForExperiment:experiment];
  
  [self updateTimer];
}


- (void)stopSchedulingForExperiment:(PacoExperiment*)experiment {
  NSLog(@"Stop scheduling notifications for experiment: %@", experiment.instanceId);
  [self deleteAllNotificationsForExperiment:experiment];
  [self updateTimer];
}

- (void)deleteAllNotificationsForExperiment:(PacoExperiment*)experiment {
  NSLog(@"Delete all notifications for experiment: %@", experiment.instanceId);
  
  NSDictionary* iosLocalNotifications = [self.notificationManager copyOfNotificationDictionary];
  
  for(NSString* notificationHash in iosLocalNotifications) {
    UILocalNotification* notification = [iosLocalNotifications objectForKey:notificationHash];
    NSString* experimentInstanceId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    
    NSDate* firedDate = [notification.userInfo objectForKey:@"experimentFireDate"];
    if ([experiment.instanceId isEqualToString:experimentInstanceId]) {
      NSLog(@"Paco removing iOS notification fire at %@ for %@",
            [PacoDate pacoStringForDate:firedDate], experimentInstanceId);
      [[UIApplication sharedApplication] cancelLocalNotification:notification];
      // the firedate + timeout falls before now, so the notification has expired and should be deleted
      [self.notificationManager deleteNotificationWithHashKey:notificationHash];
    }
  }
  //Just in case, remove any notificaiton that still exists in OS system
  NSArray* scheduledArr =
      [NSArray arrayWithArray:[[UIApplication sharedApplication] scheduledLocalNotifications]];
  for (UILocalNotification* noti in scheduledArr) {
    NSString* experimentInstanceId = [noti.userInfo objectForKey:@"experimentInstanceId"];
    NSAssert(experimentInstanceId.length > 0, @"experimentInstanceId should be valid!");
    if ([experimentInstanceId isEqualToString:experiment.instanceId]) {
      NSAssert(NO, @"There shouldn't be notification existing!");
      [[UIApplication sharedApplication] cancelLocalNotification:noti];
    }
  }
}

-(void)update:(NSArray *)experiments {
  [self cancelExpirediOSLocalNotifications:experiments];
  [self registerUpcomingiOSNotifications:experiments];
  
  [self updateTimer];
}

- (void)handleNotification:(UILocalNotification *)notification
               experiments:(NSArray*) experiments {
  NSLog(@"Paco handling an iOS notification = %@", notification.userInfo);
  
  // make sure to decrement the Application Badge Number
//  UIApplication *application = [UIApplication sharedApplication];
//  application.applicationIconBadgeNumber = notification.applicationIconBadgeNumber - 1;
  
  //Since this notification is responded successfully, cancelling it will clear it from the notification tray
  [[UIApplication sharedApplication] cancelLocalNotification:notification];
  
  NSString *experimentId = [notification.userInfo objectForKey:@"experimentInstanceId"];
  PacoExperiment *experiment = [[PacoClient sharedInstance].model experimentForId:experimentId];
  NSArray *esmSchedule = [notification.userInfo objectForKey:@"esmSchedule"];
  if (esmSchedule) {
    experiment.schedule.esmScheduleList = esmSchedule;
  }
  
  [self deleteAllNotificationsForExperiment:experiment];
  [self registerUpcomingiOSNotifications:experiments];
}

- (void)canceliOSNotificationsForExperimentId:(NSString *)experimentId {
  NSArray *notifications = [self getiOSLocalNotifications:experimentId];
  for (UILocalNotification *notification in notifications) {
    [[UIApplication sharedApplication] cancelLocalNotification:notification];
  }
}

- (void)registeriOSNotificationForExperiment:(PacoExperiment *)experiment {
  NSAssert([experiment shouldScheduleNotifications], @"experiment shouldScheduleNotifications!");
  
  NSDate* experimentFireDate = [PacoDate nextScheduledDateFromNow:experiment];
  NSDate* experimentTimeOutDate = [experimentFireDate dateByAddingTimeInterval:(experiment.schedule.timeout * 60)];
  assert(experiment.instanceId.length);
  
  // put the esm schedule in the notification dictionary
  NSArray *scheduleDates = nil;
  if (experiment.schedule.scheduleType == kPacoScheduleTypeESM) {
    scheduleDates = experiment.schedule.esmScheduleList;
    NSAssert([scheduleDates count] > 0 && [scheduleDates count] == experiment.schedule.esmFrequency,
             @"nextScheduledDateFromNow should always create valid dates");
  }
  
  NSAssert(experimentFireDate != nil, @"experimentFireDate should NOT be nil!");
  NSString* alertBody = [NSString stringWithFormat:@"Paco experiment %@ at %@.",
                         experiment.instanceId,
                         [self getTimeZoneFormattedDateString:experimentFireDate]];
  if (DEBUG) {
    alertBody = [NSString stringWithFormat:@"[%@]%@",
                 experiment.instanceId, [PacoDate debugStringForDate:experimentFireDate]];
  }
  
  [self registeriOSNotification:experiment.instanceId
             experimentFireDate:experimentFireDate
          experimentTimeOutDate:experimentTimeOutDate
          experimentEsmSchedule:scheduleDates
            experimentAlertBody:alertBody];
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
//  notification.applicationIconBadgeNumber += 1;

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
    NSLog(@"Notification scheduled in 5 seconds!!!");
    notification.fireDate = [[NSDate date] dateByAddingTimeInterval:5]; 
    [userInfo setObject:[NSNumber numberWithBool:YES] forKey:kExperimentHasFiredKey];
    
    if (DEBUG) {
      notification.alertBody = [NSString stringWithFormat:@"[Rescheduled]%@", experimentAlertBody];
    }
  } else {
    notification.fireDate = experimentFireDate;
    [userInfo setObject:[NSNumber numberWithBool:NO] forKey:kExperimentHasFiredKey];
  }
  notification.userInfo = userInfo;
  NSLog(@"Notification Scheduled for %@ at %@",
        experimentInstanceId, [PacoDate pacoStringForDate:notification.fireDate]);
  NSLog(@"Detail: %@", [notification description]);
 [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  
  // now that it's scheduled we need to start bookkeeping it
  UILocalNotification* notificationObject = [self getiOSLocalNotification:experimentInstanceId fireDate:notification.fireDate];
  NSString* notificationKey = [NSString stringWithFormat:@"%@%.0f", experimentInstanceId, [notification.fireDate timeIntervalSince1970]];
  NSAssert(notificationObject != nil, @"notification shouldn't be nil");
  [self.notificationManager addNotification:notificationObject withHashKey:notificationKey];
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
    if ([experiment shouldScheduleNotifications]) {
      if ([self getiOSLocalNotifications:experiment.instanceId].count == 0) {
          NSLog(@"Registering iOS notification for %@", experiment.instanceId);
          [self registeriOSNotificationForExperiment:experiment];
      } else {
        NSLog(@"Skip registering iOS notification for %@, since it has a notification scheduled.",
              experiment.instanceId);
      }
    } else {
      NSLog(@"Skip registering notification for %@, it's a self-report or advanced experiment.",
            experiment.instanceId);
    }
  }
}


- (void)clearOldNotificationsFromTray:(NSMutableDictionary*)firedNotificationsInTrayDict {
  for (NSString* experimentInstanceId in firedNotificationsInTrayDict) {
    NSMutableArray* notificationsFired = [firedNotificationsInTrayDict objectForKey:experimentInstanceId];
    //If there is only one fired notification in the tray for the same experiment, we don't need
    //to clear anything
    if ([notificationsFired count] <= 1) {
      continue;
    }
    
    //experiment has more than one notification that fired and thus are currently showing up in the tray,
    //so we need to cancel old notifications and only leave the latest fired one in the tray    
    [notificationsFired sortUsingComparator:^NSComparisonResult(id obj1, id obj2) {
      NSAssert([obj1 isKindOfClass:[NSArray class]] && [obj2 isKindOfClass:[NSArray class]],
               @"obj1 and obj2 should be NSArray!");
      UILocalNotification* notificationObject1 = [(NSArray*)obj1 objectAtIndex:1];
      UILocalNotification* notificationObject2 = [(NSArray*)obj2 objectAtIndex:1];
      NSAssert([notificationObject1 isKindOfClass:[UILocalNotification class]] &&
               [notificationObject2 isKindOfClass:[UILocalNotification class]],
               @"notificationObject1 and notificationObject2 should be UILocalNotification!");
      
      NSDate* fireDate1 = [notificationObject1.userInfo objectForKey:@"experimentFireDate"];
      NSDate* fireDate2 = [notificationObject2.userInfo objectForKey:@"experimentFireDate"];
      NSAssert(fireDate1 != nil && fireDate2 != nil, @"fireDate1 and fireDate2 should be valid!");
      return [fireDate1 compare:fireDate2];
    }];
  
    NSLog(@"There are %d fired notifications in the tray for %@",[notificationsFired count],experimentInstanceId);
    for (int index=0; index < [notificationsFired count]-1; index++) {
      NSArray* notificationObjectArr = [notificationsFired objectAtIndex:index];
      
      NSString* notificationHash = [notificationObjectArr objectAtIndex:0];
      UILocalNotification* notification = [notificationObjectArr objectAtIndex:1];
      
      NSDate* firedDate = [notification.userInfo objectForKey:@"experimentFireDate"];
      NSLog(@"Paco clearing 1 iOS notification fired at %@ from tray for %@",
            [PacoDate pacoStringForDate:firedDate], experimentInstanceId);
      [[UIApplication sharedApplication] cancelLocalNotification:notification];
      [self.notificationManager deleteNotificationWithHashKey:notificationHash];
      [self.delegate handleNotificationTimeOut:experimentInstanceId
                            experimentFireDate:firedDate];
    }
  }

}

// This will clear iOS Local Notifications that have fired from Notification Center and notify the deligate that they've timed out
- (void)cancelExpirediOSLocalNotifications: (NSArray *)experiments {
  NSDictionary* iosLocalNotifications = [self.notificationManager copyOfNotificationDictionary];
  NSMutableDictionary* firedNotificationsInTrayDict = [NSMutableDictionary dictionary];
  
  for(NSString* notificationHash in iosLocalNotifications) {
    UILocalNotification* notification = [iosLocalNotifications objectForKey:notificationHash];
    NSDate* timeOutDate = [notification.userInfo objectForKey:@"experimentTimeOutDate"];
    NSString* experimentInstanceId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    
    //clear time out notification
    if ([timeOutDate timeIntervalSinceNow] < 0) {
      NSDate* fireDate = [notification.userInfo objectForKey:@"experimentFireDate"];
      NSLog(@"Paco cancelling iOS notification fired at %@ for %@",
            [PacoDate pacoStringForDate:fireDate], experimentInstanceId);
      [[UIApplication sharedApplication] cancelLocalNotification:notification];
      // the firedate + timeout falls before now, so the notification has expired and should be deleted
      [self.notificationManager deleteNotificationWithHashKey:notificationHash];
      // Let the server know about this Missed Responses/Signals
      [self.delegate handleNotificationTimeOut:experimentInstanceId experimentFireDate:fireDate];
    }

    NSDate* fireDate = [notification.userInfo objectForKey:@"experimentFireDate"];
    NSAssert(fireDate != nil, @"fireDate should not be nil!");
    NSTimeInterval intervalSinceFiredDate = [fireDate timeIntervalSinceNow];
    if (intervalSinceFiredDate <= 0) {
      NSMutableArray* firedNotifications = [firedNotificationsInTrayDict objectForKey:experimentInstanceId];
      if (firedNotifications == nil) {
        firedNotifications = [NSMutableArray array];
      }
      [firedNotifications addObject:@[notificationHash, notification]];
      [firedNotificationsInTrayDict setObject:firedNotifications forKey:experimentInstanceId];
    }
  }
  
  [self clearOldNotificationsFromTray:firedNotificationsInTrayDict];
}
@end
