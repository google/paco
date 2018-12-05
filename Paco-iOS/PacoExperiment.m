/* Copyright 2015  Google
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

#import "PacoExperiment.h"

@implementation PacoExperiment






+ (PacoExperiment*)experimentWithExperimentDao:(PAExperimentDAO*)definition
{
    
    PacoExperiment * experiment = [[PacoExperiment alloc] init];
    experiment.experimentDao = definition;
    return experiment;
 
}


- (id)serializeToJSON
{
    return nil;
    
}
- (void)deserializeFromJSON:(id)json
{
    
    
}

- (BOOL)shouldScheduleNotificationsFromNow
{
    return YES;
    
    
}
- (BOOL)shouldScheduleNotificationsFromDate:(NSDate*)fromDate
{
    return YES;
    
}

- (BOOL)isSelfReportExperiment
{
    
    return YES;
}
- (BOOL)isScheduledExperiment
{
    return YES;
    
}

- (BOOL)isExperimentValidSinceDate:(NSDate*)fromDate
{
    return YES;
}

- (BOOL)isFixedLength
{
    return YES;
}
- (BOOL)isOngoing
{
    return YES;
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
- (BOOL)refreshSchedule:(PacoExperimentSchedule*)newSchedule
{
    return YES;
}


- (void)configureSchedule:(PacoExperimentSchedule*)newSchedule
{
    
}



@end
