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

int const kTotalNumOfNotifications = 60;


@interface PacoNotificationManager ()
- (void)checkCorrectnessForExperiment:(NSString*)instanceIdToCheck;

@end


@interface PacoScheduler () <PacoNotificationManagerDelegate>
@property (nonatomic, assign) id<PacoSchedulerDelegate> delegate;
@property (atomic, retain) PacoNotificationManager* notificationManager;
@property (atomic, assign) BOOL isExecutingRoutineMajorTask;
@end

@implementation PacoScheduler

#pragma mark Object Lifecycle
- (id)initWithFirstLaunchFlag:(BOOL)firstLaunch {
  self = [super init];
  if (self) {
    DDLogInfo(@"PacoScheduler initializing...");
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
    DDLogInfo(@"Serious Error: failed to load notifications!");
  }
  
  if (![self.delegate needsNotificationSystem]) {
    [self.notificationManager cancelAllPacoNotifications];
  } else {
    DDLogInfo(@"Finished initializing notifications, start executing routine major task.");
    [self executeRoutineMajorTask];
  }
}


- (BOOL)saveNotificationsToFile {
  return [self.notificationManager saveNotificationsToCache];
}

- (BOOL)isDoneLoadingNotifications {
  return self.notificationManager.areNotificationsLoaded;
}

#pragma mark Public Methods
-(void)startSchedulingForExperimentIfNeeded:(PacoExperiment*)experiment {
  if (![experiment shouldScheduleNotificationsFromNow]) {
    DDLogInfo(@"Skip scheduling for newly-joined expeirment: %@", experiment.instanceId);
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
  DDLogInfo(@"Stop scheduling notifications for experiment: %@", experiment.instanceId);
  [self.notificationManager cancelNotificationsForExperiment:experiment.instanceId];
}

- (void)stopSchedulingForAllExperiments {
  DDLogInfo(@"stop scheduling for all experiments");
  [self.notificationManager cancelAllPacoNotifications];
}

- (void)stopSchedulingForExperiments:(NSArray*)experimentIds {
  if ([experimentIds count] > 0) {
    DDLogInfo(@"stop scheduling for experiments: %@", experimentIds);
    [self.notificationManager cancelNotificationsForExperiments:experimentIds];
  }
}

- (void)executeRoutineMajorTask {
  @synchronized(self){
    if (self.isExecutingRoutineMajorTask) {
      DDLogInfo(@"Already executing routine major task, skip it!");
      return;
    }
    self.isExecutingRoutineMajorTask = YES;
    [self executeMajorTask:NO];
    self.isExecutingRoutineMajorTask = NO;
  }
}

- (void)executeMajorTaskForChangedExperimentModel {
  DDLogInfo(@"Execute major task for changed model.");
  [self executeMajorTask:YES];
}

/*
 experimentModelChanged:
 YES: whenever a schedule experiment is joined or stopped
 NO: no schedule experiment is joined or stopped
 **/
//YMZ:TODO: this method can be improved to be more efficient
- (void)executeMajorTask:(BOOL)experimentModelChanged {
  @synchronized(self) {
    if (![self.delegate isDoneInitializationForMajorTask]) {
      DDLogInfo(@"Skip executing major task: PacoClient isn't ready");
      return;
    }
    
    DDLogInfo(@"Executing Major Task...");
    BOOL needToScheduleNewNotifications = YES;
    NSArray* notificationsToSchedule = nil;
    
    if (!experimentModelChanged && [self.notificationManager hasMaximumScheduledNotifications]) {
      needToScheduleNewNotifications = NO;
      DDLogInfo(@"No need to schedule new notifications, there are 60 notifications already.");
    }
    if (needToScheduleNewNotifications) {
      notificationsToSchedule = [self.delegate nextNotificationsToSchedule];
    }
    if (!experimentModelChanged &&
        needToScheduleNewNotifications &&
        [self.notificationManager numOfScheduledNotifications] == [notificationsToSchedule count]) {
      DDLogInfo(@"There are already %lu notifications scheduled, skip scheduling new notifications.", (unsigned long)[notificationsToSchedule count]);
      needToScheduleNewNotifications = NO;
    }
    if (needToScheduleNewNotifications) {
      DDLogInfo(@"Schedule %lu new notifications ...",(unsigned long)[notificationsToSchedule count]);
      [self.notificationManager scheduleNotifications:notificationsToSchedule];
    } else {
      [self.notificationManager cleanExpiredNotifications];
    }
    [self.delegate updateNotificationSystem];
    DDLogInfo(@"Finished major task.");
  }
  
}


//keep all active notifications
//cancel all scheduled notifications
//schedule new notifications
//adjust badge number
//adjust notification system
- (void)restartNotificationSystem {
  DDLogInfo(@"restart notification system...");
  NSArray* notificationsToSchedule = [self.delegate nextNotificationsToSchedule];
  [self.notificationManager scheduleNotifications:notificationsToSchedule];
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

- (BOOL)hasActiveNotificationForExperiment:(NSString*)experimentId {
  if (0 == [experimentId length]) {
    return NO;
  }
  return [self activeNotificationForExperiment:experimentId] != nil;
}

- (BOOL)isNotificationActive:(UILocalNotification*)notification {
  return [self.notificationManager isNotificationActive:notification];
}

#pragma mark PacoNotificationManagerDelegate
- (void)handleExpiredNotifications:(NSArray*)expiredNotifications {
  if (!self.delegate) {
    DDLogError(@"PacoScheduler's delegate should be a valid PacoClient object!");
  }
  [self.delegate handleExpiredNotifications:expiredNotifications];
}

@end
