//
//  PacoSchedulingUtil.h
//  Paco
//
//  Created by northropo on 9/9/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PacoSchedulingUtil : NSObject


+(NSArray*) calculateActionSpecifications;


- (void)handleExpiredNotifications:(NSArray*)expiredNotifications;

- (BOOL)isDoneInitializationForMajorTask;

- (BOOL)needsNotificationSystem;

- (void)updateNotificationSystem;

- (NSArray*)nextNotificationsToSchedule;

@end
