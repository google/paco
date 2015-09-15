//
//  PacoScheduleCallbackHandler.m
//  Paco
//
//  Created by northropo on 9/8/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoScheduleDelegate.h"
#import "PacoSchedulingUtil.h"
#import "PacoData.h"

@implementation PacoScheduleDelegate


- (void)handleExpiredNotifications:(NSArray*)expiredNotifications
{
    
    
    
}
- (BOOL)isDoneInitializationForMajorTask
{
    
    return YES;
    
}
- (BOOL)needsNotificationSystem
{
    return NO;
    
}
- (void)updateNotificationSystem
{
 
    
}
- (NSArray*)nextNotificationsToSchedule;
{
    
    NSArray* newActionSpecifications  = [PacoSchedulingUtil calculateActionSpecifications];
    [PacoData sharedInstance].oldActionSpecifications  = [PacoData sharedInstance].actionSpecifications;
    [PacoData sharedInstance].actionSpecifications = [[NSMutableArray alloc] initWithArray:newActionSpecifications];
    return newActionSpecifications;
    
    
}

@end
