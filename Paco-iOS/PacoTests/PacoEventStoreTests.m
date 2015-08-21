//
//  PacoEventStoreTests.m
//  Paco
//
//  Created by northropo on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import  "PacoEventImpl.h"
#include "TimeUtil.h"
#include "java/lang/Boolean.h"
#include "java/lang/IllegalStateException.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/Collections.h"
#include "java/util/List.h"
#import  "PacoEventStore.h"




@interface PacoEventStoreTests : XCTestCase

@end

@implementation PacoEventStoreTests

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}


/*
- (id<PAEventInterface>)getEventWithJavaLangLong:(JavaLangLong *)experimentId
                         withOrgJodaTimeDateTime:(OrgJodaTimeDateTime *)scheduledTime
                                    withNSString:(NSString *)groupName
                                withJavaLangLong:(JavaLangLong *)actionTriggerId
                                withJavaLangLong:(JavaLangLong *)scheduleId
 */

- (void)testFetchFromEventStore
{
    // This is an example of a functional test case.
    
    OrgJodaTimeDateTime *time = [[OrgJodaTimeDateTime alloc] initWithInt:2012 withInt:3 withInt:23 withInt:0 withInt:0 withInt:0 withInt:0];
    
    
    PacoEventImpl* eventImpl = [[PacoEventImpl alloc] init:time  withResponseTime:time   GroupName:@"the group" ExperimentId:[JavaLangLong valueOfWithLong:123]  ActionTriggerId:[JavaLangLong valueOfWithLong:1234]  ScheduleId:[JavaLangLong valueOfWithLong:1235] ];
    
    
    PacoEventStore * eventStore = [[PacoEventStore alloc] init];
    [eventStore insertEventWithPAEventInterface:eventImpl];
    
    id<PAEventInterface> event = [eventStore getEventWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withOrgJodaTimeDateTime:time withNSString:@"the group" withJavaLangLong:[JavaLangLong valueOfWithLong:1234] withJavaLangLong:[JavaLangLong valueOfWithLong:1235]];

    XCTAssert(event !=nil, @"Pass");
}


- (void)testFetchFromEventStore2
{
    OrgJodaTimeDateTime *time = [[OrgJodaTimeDateTime alloc] initWithInt:2012 withInt:3 withInt:23 withInt:0 withInt:0 withInt:0 withInt:0];
    
    PacoEventImpl* eventImpl = [[PacoEventImpl alloc] init:time  withResponseTime:time   GroupName:@"the group" ExperimentId:[JavaLangLong valueOfWithLong:123]  ActionTriggerId:[JavaLangLong valueOfWithLong:1234]  ScheduleId:[JavaLangLong valueOfWithLong:1235] ];
    
    
    PacoEventStore * eventStore = [[PacoEventStore alloc] init];
    [eventStore insertEventWithPAEventInterface:eventImpl];
    
    eventImpl.groupName=@"New Name";
    [eventStore updateEventWithPAEventInterface:eventImpl];
    
    
    
    id<PAEventInterface> event = [eventStore getEventWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withOrgJodaTimeDateTime:time withNSString:@"the group" withJavaLangLong:[JavaLangLong valueOfWithLong:1234] withJavaLangLong:[JavaLangLong valueOfWithLong:1235]];
    
    
    
    
    XCTAssert(event !=nil, @"Pass");
    
    
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
