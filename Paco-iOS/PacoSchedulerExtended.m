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

#import "PacoSchedulerExtended.h"
#import "PacoExtendedClient.h"
#import "PacoConfig.h"
#import "PacoDateUtility.h"
#import "PacoScheduleGenerator.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperimentExtended.h"
#import "PacoNotificationManager.h"
#import "UILocalNotification+Paco.h"

int const kTotalNumOfNotificationsExtended = 60;


@interface PacoNotificationManager ()
- (void)checkCorrectnessForExperiment:(NSString*)instanceIdToCheck;

@end


@interface PacoSchedulerExtended () <PacoNotificationManagerDelegate>
@property (nonatomic, assign) id<PacoSchedulerDelegate> delegate;
@property (atomic, retain) PacoNotificationManager* notificationManager;
@property (atomic, assign) BOOL isExecutingRoutineMajorTask;
@end

@implementation PacoSchedulerExtended


#pragma mark Object Lifecycle
- (id)initWithFirstLaunchFlag:(BOOL)firstLaunch {
    self = [super init];
    if (self) {
        NSLog (@"PacoScheduler initializing...");
        _notificationManager = [PacoNotificationManager managerWithDelegate:self
                                                            firstLaunchFlag:firstLaunch];
    }
    return self;
}

+ (PacoSchedulerExtended*)schedulerWithDelegate:(id<PacoSchedulerDelegate>)delegate
                        firstLaunchFlag:(BOOL)firstLaunch {
    PacoSchedulerExtended* scheduler = [[PacoSchedulerExtended alloc] initWithFirstLaunchFlag:firstLaunch];
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
        NSLog(@"Finished initializing notifications, start executing routine major task.");
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
-(void)startSchedulingForExperimentIfNeeded:(PacoExperimentExtended  *)experiment {
    if (![experiment shouldScheduleNotificationsFromNow]) {
        
        return;
    }
    
    BOOL hasScheduledNotifications =
    [UILocalNotification hasLocalNotificationScheduledForExperiment:[experiment.instanceId stringValue]];
    NSAssert(!hasScheduledNotifications, @"There should be 0 notfications scheduled!");
    [self.notificationManager checkCorrectnessForExperiment:[experiment.instanceId stringValue]];
    
    [self executeMajorTaskForChangedExperimentModel];
}


- (void)stopSchedulingForExperimentIfNeeded:(PacoExperimentExtended*)experiment {
    if (experiment == nil || [experiment isSelfReportExperiment]) {
        return;
    }
    NSLog (@"Stop scheduling notifications for experiment: %@", experiment.instanceId);
    
    
    [self.notificationManager cancelNotificationsForExperiment:[experiment.instanceId stringValue]];
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
    @synchronized(self){
        if (self.isExecutingRoutineMajorTask) {
            NSLog(@"Already executing routine major task, skip it!");
            return;
        }
        self.isExecutingRoutineMajorTask = YES;
        [self executeMajorTask:NO];
        self.isExecutingRoutineMajorTask = NO;
    }
}

- (void)executeMajorTaskForChangedExperimentModel {
    NSLog(@"Execute major task for changed model.");
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
            NSLog(@"Skip executing major task: PacoClient isn't ready");
            return;
        }
        
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
            NSLog(@"There are already %lu notifications scheduled, skip scheduling new notifications.", (unsigned long)[notificationsToSchedule count]);
            needToScheduleNewNotifications = NO;
        }
        if (needToScheduleNewNotifications) {
            NSLog(@"Schedule %lu new notifications ...",(unsigned long)[notificationsToSchedule count]);
            [self.notificationManager scheduleNotifications:notificationsToSchedule];
        } else {
            [self.notificationManager cleanExpiredNotifications];
        }
        [self.delegate updateNotificationSystem];
        NSLog(@"Finished major task.");
    }
    
}


//keep all active notifications
//cancel all scheduled notifications
//schedule new notifications
//adjust badge number
//adjust notification system
- (void)restartNotificationSystem {
    NSLog(@"restart notification system...");
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
       NSLog(@"PacoScheduler's delegate should be a valid PacoClient object!");
    }
    [self.delegate handleExpiredNotifications:expiredNotifications];
}

@end