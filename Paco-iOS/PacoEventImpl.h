//
//  PacoEvent.h
//  Paco
//
//  Created by northropo on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EventInterface.h" 

#include "J2ObjC_header.h"
#include "java/lang/Boolean.h"
#include "java/lang/IllegalStateException.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/Collections.h"
#include "java/util/List.h"
#include "java/lang/Long.h"
#include "DateTime.h"


@interface PacoEventImpl : NSObject<PAEventInterface>


@property(strong,nonatomic)  OrgJodaTimeDateTime* scheduled_time;
@property(strong,nonatomic)  OrgJodaTimeDateTime* response_time;
@property(strong,nonatomic)  JavaLangLong *   experimentId;
@property(strong,nonatomic)  OrgJodaTimeDateTime *  scheduledTime;
@property(strong,nonatomic)  NSString *  groupName;
@property(strong,nonatomic)  JavaLangLong *  actionTriggerId;
@property(strong,nonatomic)  JavaLangLong *  scheduleId;



- (instancetype)init:(OrgJodaTimeDateTime *) scheduledTime withResponseTime:(OrgJodaTimeDateTime *) responseTime
           GroupName:(NSString*) groupName
        ExperimentId:(JavaLangLong*)experimentId
     ActionTriggerId:(JavaLangLong *)  actionTriggerId
          ScheduleId:(JavaLangLong * )  scheduleId;
@end
