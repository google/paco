//
//  PAExperimentDAO+Helper.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/11/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PAExperimentDAO+Helper.h"
#import "java/lang/Long.h"
#import "ActionScheduleGenerator.h" 
#include "DateTime.h"
#import "OrgJodaTimeDateTime+PacoDateHelper.h"
#import "NSDate+PacoTimeZoneHelper.h"
#import "OrgJodaTimeDateMidnight+PacoDateHelper.h"
#import "PacoScheduleUtil.h"


@implementation PAExperimentDAO (Helper)

-(NSString*) instanceId
{
    NSString* retValue = nil;
    
    if(self->id__ !=nil)
    {
      retValue = [self->id__ stringValue];
    }
    
    return  retValue;
    
}



-(NSString*) scheduleString
{

    NSString * schedulingString =  [PacoScheduleUtil buildScheduleString:self];
    return schedulingString;
    
}

-(NSString*) lastEndDate
{
    
    OrgJodaTimeDateTime * joda =  [PAActionScheduleGenerator getLastEndTimeWithPAExperimentDAO:self];
    NSDate* date = [joda nsDateValue];
    NSString* dateString = [date dateToStringLocalTimezone];
    return dateString;
    
}


-(NSString*) earliestStartDate
{
    
    OrgJodaTimeDateMidnight * joda =  [PAActionScheduleGenerator getEarliestStartDateWithPAExperimentDAO:self];
    NSDate* date = [joda nsDateValue];
    NSString* dateString = [date dateToStringLocalTimezone];
    return dateString;
    
}


/*
   to do, should return true if the experiment contains no scheduling information.
 
 */
-(BOOL) isSelfReport
{
    
    return NO;
    
}

@end
