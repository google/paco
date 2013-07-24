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

- (NSArray *)notificationsForExperimentId:(NSString *)experimentId {
  NSMutableArray *array = [NSMutableArray array];
  for (UILocalNotification *notification in [UIApplication sharedApplication].scheduledLocalNotifications) {
    NSString *expId = [notification.userInfo objectForKey:@"experimentInstanceId"];
    if ([expId isEqualToString:experimentId]) {
      [array addObject:notification];
    }
  }
  return array;
}

- (void)cancelNotificationsForExperimentId:(NSString *)experimentId {
  NSArray *notifications = [self notificationsForExperimentId:experimentId];
  for (UILocalNotification *notification in notifications) {
    [[UIApplication sharedApplication] cancelLocalNotification:notification];
  }
}

- (void)handleLocalNotification:(UILocalNotification *)notification {
  NSLog(@"LOCAL NOTIFICATION INFO = %@", notification.userInfo);
  NSString *experimentId = [notification.userInfo objectForKey:@"experimentInstanceId"];
  PacoExperiment *experiment = [[PacoClient sharedInstance].model experimentForId:experimentId];
  NSArray *esmSchedule = [notification.userInfo objectForKey:@"esmSchedule"];
  if (esmSchedule) {
    experiment.schedule.esmSchedule = esmSchedule;
  }
  [self cancelNotificationsForExperimentId:experimentId];
  [self registerScheduleWithOS:experiment];
}

@end
