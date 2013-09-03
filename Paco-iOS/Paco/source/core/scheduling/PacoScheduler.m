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

NSString* const kExperimentHasFiredKey = @"experimentHasFired";

@interface PacoScheduler ()
@property (nonatomic, assign) id<PacoSchedulerDelegate> delegate;
@property (atomic, retain, readwrite) NSMutableDictionary* iOSLocalNotifications;
@end

@implementation PacoScheduler

#pragma mark Object Lifecycle
- (id)init {
  self = [super init];
  if (self) {
    _iOSLocalNotifications = [[NSMutableDictionary alloc] init];

    // After a reboot or restart of our application we clear all notifications
    NSLog(@"Cancel All LocalNotifications!");
    [self cancelAlliOSLocalNotifications];
    // Load notifications from the plist
    
    NSMutableArray* notificationArray = [self readScheduleFromFile];
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
  NSMutableArray* notificationArray = [[NSMutableArray alloc] init];
  NSDictionary* iosLocalNotifications = [self.iOSLocalNotifications copy];
  
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

#pragma mark Public Methods
-(void)startSchedulingForExperiment:(PacoExperiment*)experiment {
  //check schedule notifications
  NSArray* scheduledArr = [self getiOSLocalNotifications:experiment.instanceId];
  NSAssert([scheduledArr count] == 0, @"There should be 0 notfications scheduled!");
  
  //check cached notifications
  BOOL hasScheduledNotification = NO;
  for(NSString* notificationHash in self.iOSLocalNotifications) {
    UILocalNotification* notification = [self.iOSLocalNotifications objectForKey:notificationHash];
    NSString* experimentInstanceId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    NSAssert(experimentInstanceId.length > 0, @"experimentInstanceId should be valid!");
    if ([experimentInstanceId isEqualToString:experiment.instanceId]) {
      hasScheduledNotification = YES;
      break;
    }
  }
  NSAssert(!hasScheduledNotification, @"shouldn't have any scheduled notifications!");
  
  NSLog(@"Start scheduling notifications for newly joined experiment: %@", experiment.instanceId);
  [self registeriOSNotificationForExperiment:experiment];
}


- (void)stopSchedulingForExperiment:(PacoExperiment*)experiment {
  NSDictionary* iosLocalNotifications = [self.iOSLocalNotifications copy];
  
  for(NSString* notificationHash in iosLocalNotifications) {
    UILocalNotification* notification = [iosLocalNotifications objectForKey:notificationHash];
    NSString* experimentInstanceId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    
    if ([experiment.instanceId isEqualToString:experimentInstanceId]) {
      NSLog(@"Paco removing iOS notification for %@", experimentInstanceId);
      [[UIApplication sharedApplication] cancelLocalNotification:notification];
      // the firedate + timeout falls before now, so the notification has expired and should be deleted
      [self.iOSLocalNotifications removeObjectForKey:notificationHash];
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
    experiment.schedule.esmSchedule = esmSchedule;
  }
  
  [self stopSchedulingForExperiment:experiment];
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
  
  NSAssert(experimentFireDate != nil, @"experimentFireDate should NOT be nil!");
  NSString* alertBody = [NSString stringWithFormat:@"Paco experiment %@ at %@.",
                         experiment.instanceId,
                         [self getTimeZoneFormattedDateString:experimentFireDate]];
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
  [self.iOSLocalNotifications setObject:notificationObject forKey:notificationKey];
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
      NSLog(@"Registering iOS notification for %@", experiment.instanceId);
      [self registeriOSNotificationForExperiment:experiment];
    } else {
      NSLog(@"Skip registering iOS notification for %@, since it has a notification scheduled.",
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
      NSLog(@"Paco clearing 1 fired iOS notification from tray for %@", experimentInstanceId);
      [[UIApplication sharedApplication] cancelLocalNotification:notification];
      [self.iOSLocalNotifications removeObjectForKey:notificationHash];
      [self.delegate handleNotificationTimeOut:experimentInstanceId
                            experimentFireDate:[notification.userInfo objectForKey:@"experimentFireDate"]];
    }
  }

}

// This will clear iOS Local Notifications that have fired from Notification Center and notify the deligate that they've timed out
- (void)cancelExpirediOSLocalNotifications: (NSArray *)experiments {
  NSDictionary* iosLocalNotifications = [self.iOSLocalNotifications copy];
  NSMutableDictionary* firedNotificationsInTrayDict = [NSMutableDictionary dictionary];
  
  for(NSString* notificationHash in iosLocalNotifications) {
    UILocalNotification* notification = [iosLocalNotifications objectForKey:notificationHash];
    NSDate* timeOutDate = [notification.userInfo objectForKey:@"experimentTimeOutDate"];
    NSString* experimentInstanceId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    
    //clear time out notification
    if ([timeOutDate timeIntervalSinceNow] < 0) {
      NSLog(@"Paco cancelling iOS notification for %@", experimentInstanceId);
      [[UIApplication sharedApplication] cancelLocalNotification:notification];
      // the firedate + timeout falls before now, so the notification has expired and should be deleted
      [self.iOSLocalNotifications removeObjectForKey:notificationHash];
      // Let the server know about this Missed Responses/Signals
      [self.delegate handleNotificationTimeOut:experimentInstanceId experimentFireDate:[notification.userInfo objectForKey:@"experimentFireDate"]];
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
