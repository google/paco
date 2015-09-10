//
//  PacoExperimentExtended.m
//  Paco
//
//  Created by northropo on 8/12/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoExperimentExtended.h"
#import "PacoExtendedClient.h"
#import "PacoScheduler.h"
#import "Schedule.h"
#import "ExperimentDAO.h"
#import "PacoExperimentExtended.h"
#import "NSObject+J2objcKVO.h" 
#import "PacoDateUtility.h" 
#import "PacoSerializeUtil.h" 


@interface PacoExperimentExtended()

@property(nonatomic, retain, readwrite) NSDate* joinTime;

@end

@implementation PacoExperimentExtended


+ (PacoExperimentExtended*)experimentWithDefinition:(PAExperimentDAO*)definition
                                   schedule:(PASchedule*)schedule
                                   joinTime:(NSDate*)joinTime
{
    
    PacoExperimentExtended* experimentInstance = [[PacoExperimentExtended alloc] init];
    experimentInstance.schedule = schedule;
    experimentInstance.definition = definition;
    experimentInstance.instanceId = [definition valueForKeyPathEx:@"id"];
    experimentInstance.lastEventQueryTime = joinTime;
    experimentInstance.joinTime = joinTime;
     return experimentInstance;
    
    
}


- (NSString *)description {
    return [NSString stringWithFormat:@"<PacoExperiment:%p - "
            @"joinTime=%@\n"
            @"schedule=%@\n"
            @"definition=%@>",
            self,
            [PacoDateUtility pacoStringForDate:self.joinTime],
            self.schedule,
            self.definition];
}

- (id)serializeToJSON {
    id  jsonSchedule =  [PacoSerializeUtil  jsonFromSchedule:self.schedule];
    id jsonDefinition = [PacoSerializeUtil jsonFromDefinition:self.definition];
    id jsonJoinTime = [PacoDateUtility pacoStringForDate:self.joinTime];
    
    return @{@"experimentId": [self.definition valueForKeyEx:@"id"],
             @"joinTime": jsonJoinTime,
             @"instanceId": self.instanceId,
             @"schedule": jsonSchedule,
             @"definition": jsonDefinition};
  
    return nil;
    
}

- (BOOL)shouldScheduleNotificationsFromNow
{
    return YES;
}
- (BOOL)shouldScheduleNotificationsFromDate:(NSDate*)fromDate
{
    return NO;
    
}

- (BOOL)isSelfReportExperiment
{
    return NO;
    
}
- (BOOL)isScheduledExperiment
{
    return NO;
    
}

- (BOOL)isExperimentValidSinceDate:(NSDate*)fromDate
{
    return NO;
}

- (BOOL)isFixedLength
{
    return NO;
}
- (BOOL)isOngoing
{
    return NO;
}

//return all esm dates generated and stored for an esm experiment that are later than fromDate,
//if there aren't any esm dates generated yet, return nil
- (NSArray*)ESMSchedulesFromDate:(NSDate*)fromDate
{
    
    return nil;
}

//The last ESM schedule date generated and stored for an esm experiment
//we use it to determine the next esm cycle start date to generate future esm schedule dates
//if there aren't any esm dates generated yet, return nil
- (NSDate*)lastESMScheduleDate
{
    return nil;
    
}

- (NSDate*)startDate
{
    return nil;
    
}
- (NSDate*)endDate
{
    return nil;
}

//return the midnight of joinTime
- (NSDate*)joinDate
{
    return nil;
}

//when definition is refreshed, refresh experiment's schedule
//but keep the esmStartHour, esmEndHour, or times configured specifically by user
//return YES if the schedule is changed, NO if the schedule doesn't need to be updated
- (BOOL)refreshSchedule:(PASchedule*) newSchedule
{
    return NO;
    
}

- (void)configureSchedule:(PASchedule*)newSchedule
{
    
}


@end
