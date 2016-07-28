//
//  PacoNotificationManager
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/13/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//
#import <UIKit/UIKit.h>

@protocol PacoNotificationManagerDelegate <NSObject>

@required
- (void)handleExpiredNotifications:(NSArray*)expiredNotifications;

@end



@interface PacoNotificationManager : NSObject

@property (atomic, assign, readonly) BOOL areNotificationsLoaded;

+ (PacoNotificationManager*)managerWithDelegate:(id<PacoNotificationManagerDelegate>)delegate
                                firstLaunchFlag:(BOOL)firstLaunchFlag;


- (void)scheduleNotifications:(NSArray*)newNotifications;

//notifications MUST be sorted already
- (void)cleanExpiredNotifications;

//call this when the user stops an experiment
//1. cancel all notifications from iOS for this expeirment
//2. clear this expeirment's notifications from notification tray
//3. delete all notifications from cache for this experiment
- (void)cancelNotificationsForExperiment:(NSString*)experimentId;

- (void)cancelNotificationsForExperiments:(NSArray*)experimentIds;

- (UILocalNotification*)activeNotificationForExperiment:(NSString*)experimentId;
- (BOOL)isNotificationActive:(UILocalNotification*)notification;

- (void)handleRespondedNotification:(UILocalNotification*)notification;

- (BOOL)saveNotificationsToCache;
- (BOOL)loadNotificationsFromCache;

- (NSUInteger)numOfScheduledNotifications;
- (BOOL)hasMaximumScheduledNotifications;

- (void)cancelAllPacoNotifications;


@end
