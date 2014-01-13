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
#import "PacoDateUtility.h"
#import "PacoScheduleGenerator.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperiment.h"
#import "PacoNotificationManager.h"
#import "UILocalNotification+Paco.h"

NSString* const kExperimentHasFiredKey = @"experimentHasFired";
NSInteger const kTotalNumOfNotifications = 60;


@interface PacoNotificationManager ()
- (void)checkCorrectnessForExperiment:(NSString*)instanceIdToCheck;

@end


@interface PacoScheduler () <PacoNotificationManagerDelegate>
@property (nonatomic, assign) id<PacoSchedulerDelegate> delegate;
@property (atomic, retain) PacoNotificationManager* notificationManager;

@end

@implementation PacoScheduler

#pragma mark Object Lifecycle
- (id)initWithFirstLaunchFlag:(BOOL)firstLaunch {
  self = [super init];
  if (self) {
    _notificationManager = [PacoNotificationManager managerWithDelegate:self
                                                        firstLaunchFlag:firstLaunch];
  }
  return self;
}

+ (PacoScheduler*)schedulerWithDelegate:(id<PacoSchedulerDelegate>)delegate
                        firstLaunchFlag:(BOOL)firstLaunch {
  PacoScheduler* scheduler = [[PacoScheduler alloc] initWithFirstLaunchFlag:firstLaunch];
  scheduler.delegate = delegate;
  return scheduler;
}


- (void)initializeNotifications {
  BOOL success = [self.notificationManager loadNotificationsFromCache];
  if (!success) {
    NSLog(@"Serious Error: failed to load notifications!");
  }
  
  if (![self.delegate needsNotificationSystem]) {
    [self.notificationManager cancelAllPacoNotifications];
  } else {
    [self executeRoutineMajorTask];
  }
}


- (BOOL)saveNotificationsToFile {
  return [self.notificationManager saveNotificationsToCache];
}


#pragma mark Public Methods
-(void)startSchedulingForExperimentIfNeeded:(PacoExperiment*)experiment {
  if (![experiment shouldScheduleNotifications]) {
    NSLog(@"Skip scheduling for newly-joined expeirment: %@", experiment.instanceId);
    return;
  }
  
  BOOL hasScheduledNotifications =
      [UILocalNotification hasLocalNotificationScheduledForExperiment:experiment.instanceId];
  NSAssert(!hasScheduledNotifications, @"There should be 0 notfications scheduled!");
  [self.notificationManager checkCorrectnessForExperiment:experiment.instanceId];
  
  [self executeMajorTaskForChangedExperimentModel];
}


- (void)stopSchedulingForExperimentIfNeeded:(PacoExperiment*)experiment {
  if (experiment == nil || [experiment isSelfReportExperiment]) {
    return;
  }
  NSLog(@"Stop scheduling notifications for experiment: %@", experiment.instanceId);
  [self.notificationManager cancelNotificationsForExperiment:experiment.instanceId];
}

- (void)stopSchedulingForAllExperiments {
  NSLog(@"stop scheduling for all experiments");
  [self.notificationManager cancelAllPacoNotifications];
}

- (void)stopSchedulingForExperiments:(NSArray*)experimentIds {
  if ([experimentIds count] > 0) {
    NSLog(@"stop scheduling for experiments: %@", experimentIds);
    [self.notificationManager cancelNotificationsForExperiments:experimentIds];
  }
}

- (void)executeRoutineMajorTask {
  [self executeMajorTask:NO];
}

- (void)executeMajorTaskForChangedExperimentModel {
  [self executeMajorTask:YES];
}

/*
 experimentModelChanged:
 YES: whenever a schedule experiment is joined or stopped
 NO: no schedule experiment is joined or stopped
 **/
//YMZ:TODO: this method can be improved to be more efficient
- (void)executeMajorTask:(BOOL)experimentModelChanged {
  NSLog(@"Executing Major Task...");
  BOOL needToScheduleNewNotifications = YES;
  NSArray* notificationsToSchedule = nil;
  
  if (!experimentModelChanged && [self.notificationManager hasMaximumScheduledNotifications]) {
    needToScheduleNewNotifications = NO;
    NSLog(@"No need to schedule new notifications, there are 60 notifications already.");
  }
  if (needToScheduleNewNotifications) {
    notificationsToSchedule = [self.delegate nextNotificationsToSchedule];
  }
  if (!experimentModelChanged &&
      needToScheduleNewNotifications &&
      [self.notificationManager numOfScheduledNotifications] == [notificationsToSchedule count]) {
      NSLog(@"There are already %d notifications scheduled, skip scheduling new notifications.", [notificationsToSchedule count]);
      needToScheduleNewNotifications = NO;
  }
  if (needToScheduleNewNotifications) {
    NSLog(@"Schedule %d new notifications ...",[notificationsToSchedule count]);
    [self.notificationManager schedulePacoNotifications:notificationsToSchedule];
  } else {
    [self.notificationManager cleanExpiredNotifications];
  }
  [self.delegate updateNotificationSystem];
}

- (void)restartNotificationSystem {
  NSArray* notificationsToSchedule = [self.delegate nextNotificationsToSchedule];
  [self.notificationManager resetWithPacoNotifications:notificationsToSchedule];
  [self.delegate updateNotificationSystem];
}

- (void)cleanExpiredNotifications {
  [self.notificationManager cleanExpiredNotifications];
}

- (void)handleRespondedNotification:(UILocalNotification *)notification {
  [self.notificationManager handleRespondedNotification:notification];
}

- (UILocalNotification*)activeNotificationForExperiment:(NSString*)experimentId {
  return [self.notificationManager activeNotificationForExperiment:experimentId];
}

- (BOOL)isNotificationActive:(UILocalNotification*)notification {
  return [self.notificationManager isNotificationActive:notification];
}

#pragma mark PacoNotificationManagerDelegate
- (void)handleExpiredNotifications:(NSArray*)expiredNotifications {
  [self.delegate handleExpiredNotifications:expiredNotifications];
}

@end
