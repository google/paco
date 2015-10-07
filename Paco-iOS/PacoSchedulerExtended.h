//
//  PacoSchedulerExtended.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/18/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PacoExperimentExtended.h"

@protocol PacoSchedulerDelegate
@required
- (void)handleExpiredNotifications:(NSArray*)expiredNotifications;
- (BOOL)isDoneInitializationForMajorTask;
- (BOOL)needsNotificationSystem;
- (void)updateNotificationSystem;
- (NSArray*)nextNotificationsToSchedule;
@end


@interface PacoSchedulerExtended : NSObject


+ (PacoSchedulerExtended*) schedulerWithDelegate:(id<PacoSchedulerDelegate>)delegate
                        firstLaunchFlag:(BOOL)firstLaunch;

- (void)handleRespondedNotification:(UILocalNotification *)notification;

- (UILocalNotification*)activeNotificationForExperiment:(NSString*)experimentId;
- (BOOL)hasActiveNotificationForExperiment:(NSString*)experimentId;

- (BOOL)isNotificationActive:(UILocalNotification*)notification;

- (void)executeRoutineMajorTask;

// call this when joining an experiment
-(void)startSchedulingForExperimentIfNeeded:(PacoExperimentExtended*)experiment;

// call this when leaving an experiment
- (void)stopSchedulingForExperimentIfNeeded:(PacoExperimentExtended *)experiment;

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
