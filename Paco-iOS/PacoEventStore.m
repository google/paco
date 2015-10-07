//
//  PacoEventStore.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoEventStore.h"
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
#include "EventInterface.h"
#include "EventStore.h"
#import  "Consts.h"


@interface PacoEventStore()<PAEventStore>
@property (strong,nonatomic) NSMutableArray* events;
@end



@implementation PacoEventStore


- (instancetype)init
{
    self = [super init];
    if (self) {
        _events = [NSMutableArray array];
    }
    return self;
}


- (id<PAEventInterface>)getEventWithJavaLangLong:(JavaLangLong *)experimentId
                         withOrgJodaTimeDateTime:(OrgJodaTimeDateTime *)scheduledTime
                                    withNSString:(NSString *)groupName
                                withJavaLangLong:(JavaLangLong *)actionTriggerId
                                withJavaLangLong:(JavaLangLong *)scheduleId
{
    
       NSArray *filteredArray = [_events filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"(experimentId==%@) AND (scheduledTime==%@) AND   (groupName LIKE %@)   AND   (actionTriggerId==%@) AND (scheduleId==%@)",experimentId,scheduledTime,groupName,actionTriggerId,scheduleId ]];
    // assert array contains at most one element.
    
     id<PAEventInterface> event = [filteredArray firstObject];
     return event;
    
    
    
    
}

- (void)updateEventWithPAEventInterface:(id<PAEventInterface>)correspondingEvent
{
    //needs to be unique. How do we fetch the corresponding event
 /*
    PacoEventImpl * e = (PacoEventImpl*)correspondingEvent;
    id<PAEventInterface> event = [self getEventWithJavaLangLong:e.experimentId withOrgJodaTimeDateTime:e.scheduled_time  withNSString:e.groupName withJavaLangLong:e.actionTriggerId   withJavaLangLong:e.scheduleId];
    int index = [_events  indexOfObject:event];
    [_events replaceObjectAtIndex:index withObject:correspondingEvent];
  */
    
}

- (void)insertEventWithPAEventInterface:(id<PAEventInterface>)event
{
    [_events addObject:event];
}






@end
