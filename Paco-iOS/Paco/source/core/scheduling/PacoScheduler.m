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

@interface PacoScheduleUnit : NSObject
@property (nonatomic, retain) PacoExperiment *experiment;
@property (nonatomic, retain) NSArray *notifications;  // UILocalNotification
@property (nonatomic, retain) NSDictionary *lastPayload;
@end

@implementation PacoScheduleUnit
@end

@interface PacoScheduler ()
@property (nonatomic, retain) NSMutableDictionary *scheduledExperiments;  // <experimentId, PacoScheduleUnit>
@end

@implementation PacoScheduler

- (void)registerSchedulesWithOS:(NSArray *)experiments {
  for (PacoExperiment *experiment in experiments) {
    [self registerScheduleWithOS:experiment];
  }
}

- (void)registerNotificationForExperiment:(PacoExperiment *)experiment
                                  forDate:(NSDate *)scheduledFireTime {
  UILocalNotification *notification = [[UILocalNotification alloc] init];
  NSDate *now = [NSDate dateWithTimeIntervalSinceNow:0];
  notification.fireDate = [PacoDate nextScheduledDateForExperiment:experiment fromThisDate:now];

  NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
  assert(experiment.instanceId.length);
  [userInfo setObject:experiment.instanceId forKey:@"experimentInstanceId"];
  
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
  notification.alertBody = [NSString stringWithFormat:@"PACO Experiment Time!"];
  notification.applicationIconBadgeNumber = 1;
  notification.soundName = @"deepbark_trial.mp3";
  [[UIApplication sharedApplication] cancelAllLocalNotifications];
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  
}

- (NSDate *)localTime:(NSDate *)date {
  NSTimeZone *timeZone = [NSTimeZone defaultTimeZone];
  NSInteger seconds = [timeZone secondsFromGMTForDate:date];
  return [NSDate dateWithTimeInterval:seconds sinceDate:date];
}

- (void)registerScheduleWithOS:(PacoExperiment *)experiment {
//  assert(experiment);
  if (!experiment) {
    NSLog(@"FAILED TO REGIStER EXPERIMENT SCHEDULE, NIL");
    return;
  }
  // Schedule 3 non-repeating events (64 / 3 = 21.333) so at max can have 20
  // current experiments.  If the user misses 3 events in a row the local
  // notifications will stop until the next time they open the app.
  NSDate *now = [NSDate dateWithTimeIntervalSinceNow:60];
  NSDate *fireDate1 = [PacoDate nextScheduledDateForExperiment:experiment fromThisDate:now];
  NSDate *fireDate1a = [fireDate1 dateByAddingTimeInterval:60];
  NSDate *fireDate2 = [PacoDate nextScheduledDateForExperiment:experiment fromThisDate:fireDate1a];
  NSDate *fireDate2a = [fireDate2 dateByAddingTimeInterval:60];
  NSDate *fireDate3 = [PacoDate nextScheduledDateForExperiment:experiment fromThisDate:fireDate2a];

#if PACO_TEST_MAKE_SOON_EVENT_ON_JOIN
  NSDate *soon = [NSDate dateWithTimeIntervalSinceNow:5];
  UILocalNotification *notification = [[UILocalNotification alloc] init];
  notification.fireDate = soon;

  NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
  assert(experiment.instanceId.length);
  [userInfo setObject:experiment.instanceId forKey:@"experimentInstanceId"];
  
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
  notification.alertBody = [NSString stringWithFormat:@"PACO Experiment Time!"];
  notification.applicationIconBadgeNumber = 1;
  [[UIApplication sharedApplication] cancelAllLocalNotifications];
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];
#endif

  [self registerNotificationForExperiment:experiment forDate:fireDate1];
  [self registerNotificationForExperiment:experiment forDate:fireDate2];
  [self registerNotificationForExperiment:experiment forDate:fireDate3];
  NSLog(@"REGISTERING LOCAL NOTIFICATIONS\n%@\n%@\n%@\n", [self localTime:fireDate1], [self localTime:fireDate2], [self localTime:fireDate3]);
 

  /*
  
  NSTimeZone *tz = [NSTimeZone defaultTimeZone];
  NSInteger seconds = [tz secondsFromGMTForDate: self];
  return [NSDate dateWithTimeInterval: seconds sinceDate: self];
}
  
  */
  
}

- (void)registeriOSNotificationForExperiment:(PacoExperiment *)experiment {
  NSDate *now = [NSDate dateWithTimeIntervalSinceNow:0];
  UILocalNotification *notification = [[UILocalNotification alloc] init];
  
  notification.fireDate = [PacoDate nextScheduledDateForExperiment:experiment fromThisDate:now];
  
  NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
  assert(experiment.instanceId.length);
  [userInfo setObject:experiment.instanceId forKey:@"experimentInstanceId"];
  
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
  notification.alertBody = [NSString stringWithFormat:@"PACO Experiment Time!"];
  notification.applicationIconBadgeNumber = 1;
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];  
}

- (NSArray *)getiOSNotificationsForExperimentId:(NSString *)experimentId {
  NSMutableArray *array = [NSMutableArray array];
  for (UILocalNotification *notification in [UIApplication sharedApplication].scheduledLocalNotifications) {
    NSString *expId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    if ([expId isEqualToString:experimentId]) {
      [array addObject:notification];
    }
  }
  return array;
}

- (void)canceliOSNotificationsForExperimentId:(NSString *)experimentId {
  NSArray *notifications = [self getiOSNotificationsForExperimentId:experimentId];
  for (UILocalNotification *notification in notifications) {
    [[UIApplication sharedApplication] cancelLocalNotification:notification];
  }
}

- (void)cancelExpirediOSNotifications: (NSArray *)experiments {
  NSDate *now = [NSDate date];
  
  // get all current scheduled notifications for this app
  for (UILocalNotification *notification in [UIApplication sharedApplication].scheduledLocalNotifications) {
    NSString *expId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    NSDate *fireDate = notification.fireDate;
    
    // get the timeout for the experiment
    for (PacoExperiment *experiment in experiments) {
      if ([expId isEqualToString:experiment.instanceId]) {
        if ([now compare:[fireDate dateByAddingTimeInterval:experiment.schedule.timeout * 60]] == NSOrderedDescending) {
          // the firedate + timeout falls before now, so the notification has expired and should be deleted
          NSLog(@"Paco cancelling iOS notification for %@", expId);
          [[UIApplication sharedApplication] cancelLocalNotification:notification];
          // Let the server know about this Missed Responses/Signals
          // TODO TPE: let the server know about the Missed Responses/Signals
          // [[PacoClient sharedInstance].service
        }
        break;
      }
    }
  }
}

- (void)registerUpcomingiOSNotifications: (NSArray *)experiments {
  // go through all experiments, see if a notification is already scheduled, and if not add it to the schedule
  for (PacoExperiment *experiment in experiments) {
    if ([self getiOSNotificationsForExperimentId:experiment.instanceId].count == 0) {
      // ok, so no notification exists, so schedule one
      NSLog(@"Paco registering iOS notification for %@", experiment.instanceId);
      [self registeriOSNotificationForExperiment:experiment];
    }
  }
}

- (void)updateiOSNotifications: (NSArray *)experiments {
  [self cancelExpirediOSNotifications:experiments];
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
  [self registerScheduleWithOS:experiment];
}

@end
