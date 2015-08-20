//
//  PacoEvent.m
//  Paco
//
//  Created by northropo on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoEventImpl.h"
#include "J2ObjC_header.h"

@implementation PacoEventImpl





- (instancetype)init:(OrgJodaTimeDateTime *) scheduledTime withResponseTime:(OrgJodaTimeDateTime *) responseTime
                    
                                            GroupName:(NSString*) groupName
                                            ExperimentId:(JavaLangLong*)experimentId
                                            ActionTriggerId:(JavaLangLong *)  actionTriggerId
                                            ScheduleId:(JavaLangLong * )  scheduleId
{
    self = [super init];
    if (self) {
        self.scheduled_time = scheduledTime;
        self.response_time = responseTime;
        self.groupName = groupName;
        self.experimentId = experimentId;
        self.actionTriggerId = actionTriggerId;
        self.scheduleId = scheduleId;
    }
    return self;
}
- (OrgJodaTimeDateTime *)getScheduledTime
{
    return  _scheduled_time;
}

- (OrgJodaTimeDateTime *)getResponseTime
{
     return  _response_time;
}

@end
