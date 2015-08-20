//
//  PacoExtendedClient.m
//  Paco
//
//  Created by northropo on 8/12/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoExtendedClient.h"
#import "PacoSchedulerExtended.h"
#import "ExperimentDAO.h"
#import "Schedule.h" 
#import "PacoModelExtended.h"
#import "PacoEventManagerExtended.h" 
#import "PacoAuthenticator.h" 


@interface PacoExtendedClient () <PacoSchedulerDelegate>

@property (nonatomic, retain) PacoSchedulerExtended *scheduler;
@property (nonatomic, retain) PacoModelExtended *model;
@property (nonatomic, retain) PacoAuthenticator *authenticator;

@end

@implementation PacoExtendedClient


+ (PacoExtendedClient *)sharedInstance {
    
    static PacoExtendedClient *client = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        client = [[PacoExtendedClient alloc] init];
    });
    return client;
}


- (id)init {
    self = [super init];
    if (self) {
        
          self.authenticator = [[PacoAuthenticator alloc] initWithFirstLaunchFlag:_firstLaunch | _firstOAuth2];
          [self checkIfUserFirstLaunchPaco];
          self.scheduler = [PacoSchedulerExtended schedulerWithDelegate:self firstLaunchFlag:_firstLaunch];
          self.model = [[PacoModelExtended alloc] init];
      
    }
    return self;
}


- (void) joinExperimentWithDefinition:(PAExperimentDAO*)definition
                            schedule:(PASchedule*) schedule
                     completionBlock:(void(^)())completionBlock
{
    
     NSAssert(definition, @"definition should not be nil");
     [self.eventManager saveJoinEventWithDefinition:definition withSchedule:schedule];
      PacoExperimentExtended  *experiment = [self.model addExperimentWithDefinition:definition
                                                                schedule:schedule];

     NSLog(@"Experiment Joined with schedule: %@", [experiment.schedule description]);
    
    //start scheduling notifications for this joined experiment
    [self.scheduler startSchedulingForExperimentIfNeeded:experiment];
    
    
    if (completionBlock) {
        completionBlock();
    }
    
}


#pragma mark join an experiment
/*
- (void)joinExperimentWithDefinition:(PacoExperimentDefinition*)definition
                            schedule:(PacoExperimentSchedule*)schedule
                     completionBlock:(void(^)())completionBlock {
    
    DDLogInfo(@"PacoClient-- Refresh joinExperimentWithDefinition ");
    NSAssert(definition, @"definition should not be nil");
    [self.eventManager saveJoinEventWithDefinition:definition withSchedule:schedule];
    //create a new experiment and save it to cache
    PacoExperiment *experiment = [self.model addExperimentWithDefinition:definition
                                                                schedule:schedule];
    DDLogInfo(@"Experiment Joined with schedule: %@", [experiment.schedule description]);
    //start scheduling notifications for this joined experiment
    [self.scheduler startSchedulingForExperimentIfNeeded:experiment];
    
    if (completionBlock) {
        completionBlock();
    }
}

*/


- (void)checkIfUserFirstLaunchPaco {
    
    NSString* launchedKey = @"paco_launched";
    id value = [[NSUserDefaults standardUserDefaults] objectForKey:launchedKey];
    if (value == nil) { //first launch
        [[NSUserDefaults standardUserDefaults] setObject:@YES forKey:launchedKey];
        [[NSUserDefaults standardUserDefaults] synchronize];
        _firstLaunch = YES;
    } else {
        _firstLaunch = NO;
    }
}


#pragma mark - scheduler delegate methods

- (void)handleExpiredNotifications:(NSArray*)expiredNotifications
{
    
    
    
}
- (BOOL)isDoneInitializationForMajorTask
{
    
     return TRUE;
}
- (BOOL)needsNotificationSystem
{
    return TRUE;
}
- (void)updateNotificationSystem
{
 
}
- (NSArray*)nextNotificationsToSchedule
{
    return nil;
}

@end
