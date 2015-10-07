//
//  PacoEventManagerTests.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/17/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "PacoSerializer.h"
#import "PacoExtendedClient.h"
#import "ActionScheduleGenerator.h"
#import "NSObject+J2objcKVO.h"

#include "ExperimentDAO.h"
#include "ExperimentDAOCore.h"
#include "ExperimentGroup.h"
#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "ListMaker.h"
#include "Validator.h"
#include "java/lang/Boolean.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/List.h"
#import "ExperimentDAO.h"
#import <objc/runtime.h>
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
#include "org/joda/time/Hours.h"
#include "org/joda/time/Duration.h"
#include "EsmGenerator2.h"
#include  "PacoEventManagerExtended.h"
#include "PacoEventExtended.h"

@interface PacoEventExtended (Testing)
@property (nonatomic, readwrite, copy) NSString *appId;
@property (nonatomic, readwrite, copy) NSString *pacoVersion;

- (NSArray*) allPendingEvents;
@end


@interface PacoEventManagerExtended (Testing)

- (NSArray*) allPendingEvents;
@end



@interface PacoEventManagerTests : XCTestCase
{
  PacoEventManagerExtended* _eventManager;
    
    
}
@end

@implementation PacoEventManagerTests

- (void)setUp {
    [super setUp];
    
    
    _eventManager= [ PacoEventManagerExtended  defaultManager];
 
}

- (void)tearDown {
    [super tearDown];
}


-(void) testSaveEvent
{
    
    PacoEventExtended *event = [[PacoEventExtended alloc] init];
    event.who = @"robeto duran";
    event.when = [NSDate date];
    event.latitude = 1234;
    event.longitude = 2345;
    event.responseTime = [NSDate date];
    event.scheduledTime = [NSDate date];
    event.appId =  @"app id";
    [event setValue:@"app Id" forKey:@"appId"];
    [event setValue:@"version 1" forKey:@"pacoVersion"];
    [event setValue:@"experimentId " forKey:@"experimentId"];
    [event setValue:@"ex version" forKey:@"experimentVersion"];
    [event setValue:@"the name" forKey:@"experimentName"];
    [event setValue:@"1.2" forKey:@"experimentVersion"];
     event.responses = @[@"oh yeah", @"oh no", @"oh maybe"];
    
    [_eventManager saveEvent:event];
    
    
   NSArray * array =   [_eventManager allPendingEvents];
    
   PacoEventExtended  * event2 = array[0];
    
    
    XCTAssert([event2.appId isEqualToString:event.appId], @"Pass");XCTAssert([event2.who isEqualToString:event.who], @"Pass");
    XCTAssert([event2.who isEqualToString:event.who], @"Pass");XCTAssert([event2.who isEqualToString:event.who], @"Pass");
    
    
}

-(void) testSaveEvents
{
    
   
    
}

- (void)testExample {
    // This is an example of a functional test case.
    XCTAssert(YES, @"Pass");
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
