//
//  PacoScheduler.h
//  Paco
//
//  Created by Northrop O'brien on 4/15/16.
//  Copyright © 2016 Paco. All rights reserved.


#import <Foundation/Foundation.h>

#import "PacoModel.h"


extern int const kTotalNumOfNotifications;

@class PacoExperiment;

@protocol PacoSchedulerDelegate
@required
- (void)handleExpiredNotifications:(NSArray*)expiredNotifications;
- (BOOL)isDoneInitializationForMajorTask;
- (BOOL)needsNotificationSystem;
- (void)updateNotificationSystem;
- (NSArray*)nextNotificationsToSchedule;
@end

// The PacoScheduler schedules local notifications via UILocalNotification.  The
// experiment schedule is used to decide when to fire local notifications.  The
// local notification system can have at most 64 scheduled notifications per
// app. This means that there is a limit in how many experiments can be
// scheduled at once.
@interface PacoScheduler : NSObject

+ (PacoScheduler*)schedulerWithDelegate:(id<PacoSchedulerDelegate>)delegate
                        firstLaunchFlag:(BOOL)firstLaunch;

- (void)handleRespondedNotification:(UILocalNotification *)notification;

- (UILocalNotification*)activeNotificationForExperiment:(NSString*)experimentId;
- (BOOL)hasActiveNotificationForExperiment:(NSString*)experimentId;

- (BOOL)isNotificationActive:(UILocalNotification*)notification;

- (void)executeRoutineMajorTask;

// call this when joining an experiment
-(void)startSchedulingForExperimentIfNeeded:(PacoExperiment*)experiment;

// call this when leaving an experiment
- (void)stopSchedulingForExperimentIfNeeded:(PacoExperiment*)experiment;

// call this when shutting down the notification system
- (void)stopSchedulingForAllExperiments;

- (void)stopSchedulingForExperiments:(NSArray*)experimentIds;

// call this when the application goes to InActive to make sure
// we can persist the notifications state
- (BOOL)saveNotificationsToFile;

//return YES if notification plist is loaded, otherwise return NO
- (BOOL)isDoneLoadingNotifications;
// call this AFTER running experiments are loaded
- (void)initializeNotifications;

- (void)cleanExpiredNotifications;

- (void)restartNotificationSystem;

@end

