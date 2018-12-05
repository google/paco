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
