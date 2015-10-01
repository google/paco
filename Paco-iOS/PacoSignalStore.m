//
//  PacoSignalStore.m
//  Paco
//
//  Created by northropo on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoSignalStore.h"

#include "ActionScheduleGenerator.h"
#include "ActionSpecification.h"
#include "ActionTrigger.h"
#include "DateMidnight.h"
#include "DateTime.h"
#include "EsmGenerator2.h"
#include "EsmSignalStore.h"
#include "EventStore.h"
#include "ExperimentDAO.h"
#include "ExperimentGroup.h"
#include "Interval.h"
#include "J2ObjC_source.h"
#include "NonESMSignalGenerator.h"
#include "PacoAction.h"
#include "PacoNotificationAction.h"
#include "Schedule.h"
#include "ScheduleTrigger.h"
#include "SignalTime.h"
#include "TimeUtil.h"
#include "java/lang/Boolean.h"
#include "java/lang/IllegalStateException.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/Collections.h"
#include "java/util/List.h"
#include  "Consts.h"





@interface PacoSignalStore()

@property(nonatomic, retain, readwrite) NSMutableArray* signals;

@end

@implementation PacoSignalStore

- (instancetype)init
{
    self = [super init];
    if (self) {
        _signals =  [NSMutableArray array];
    }
    return self;
}
- (void)storeSignalWithJavaLangLong:(JavaLangLong *)date
                   withJavaLangLong:(JavaLangLong *)experimentId
                   withJavaLangLong:(JavaLangLong *)alarmTime
                       withNSString:(NSString *)groupName
                   withJavaLangLong:(JavaLangLong *)actionTriggerId
                   withJavaLangLong:(JavaLangLong *)scheduleId
{
     NSDictionary * dictionary
              =     @{SIGNAL_PERIOD_START_DATE:date,
                      SIGNAL_EXPERIMENT_ID:experimentId,
                      SIGNAL_ALARM_TIME:alarmTime,
                      SIGNAL_GROUP_NAME:groupName,
                      SIGNAL_ACTION_TRIGGER_ID:actionTriggerId,
                      SIGNAL_SCHEDULE_ID:scheduleId};
    
    [_signals addObject:dictionary];
}



- (id<JavaUtilList>)getSignalsWithJavaLangLong:(JavaLangLong *)experimentId
                              withJavaLangLong:(JavaLangLong *)periodStart
                                  withNSString:(NSString *)groupName
                              withJavaLangLong:(JavaLangLong *)actionTriggerId
                              withJavaLangLong:(JavaLangLong *)scheduleId
{
 
   NSArray *filteredArray = [_signals filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"(experimentId==%@) AND (periodStart==%@) AND   (groupName LIKE %@)   AND   (actionTriggerId==%@) AND (scheduleId==%@)",experimentId,periodStart,groupName,actionTriggerId,scheduleId ]];
 
    JavaUtilArrayList  * arrayList = [[JavaUtilArrayList alloc] initWithInt:[filteredArray count]];
    NSDictionary * dictionary;
 
    // need to convert the dictionary into a signal object and store it.
    for(dictionary in filteredArray)
    {
        
        OrgJodaTimeDateTime* dateTime = [[OrgJodaTimeDateTime  alloc] initWithLong:[dictionary[SIGNAL_ALARM_TIME] longLongValue]];
        [arrayList addWithId:dateTime];
    }
    return arrayList;
}

- (void)deleteAll
{
    [_signals removeAllObjects];
}

- (void)deleteAllSignalsForSurveyWithJavaLangLong:(JavaLangLong *)experimentId
{
    [_signals removeAllObjects];
}

- (void)deleteSignalsForPeriodWithJavaLangLong:(JavaLangLong *)experimentId
                              withJavaLangLong:(JavaLangLong *)periodStart
                                  withNSString:(NSString *)groupName
                              withJavaLangLong:(JavaLangLong *)actionTriggerId
                              withJavaLangLong:(JavaLangLong *)scheduleId
{
    
   NSArray *filteredArray = [_signals filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"(experimentId==%@) AND (periodStart==%@) AND   (groupName LIKE %@)   AND   (actionTriggerId==%@) AND (scheduleId==%@)",experimentId,periodStart,groupName,actionTriggerId,scheduleId ]];
    
    [_signals removeObjectsInArray:filteredArray];
    
}


@end
