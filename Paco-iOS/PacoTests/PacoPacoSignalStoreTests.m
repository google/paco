//
//  PacoPacoSignalStore.m
//  Paco
//
//  Created by northropo on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
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
#include "PacoSignalStore.h" 



@interface PacoPacoSignalStore : XCTestCase

@end

@implementation PacoPacoSignalStore

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}


/*
 
 - (void)storeSignalWithJavaLangLong:(JavaLangLong *)date
 withJavaLangLong:(JavaLangLong *)experimentId
 withJavaLangLong:(JavaLangLong *)alarmTime
 withNSString:(NSString *)groupName
 withJavaLangLong:(JavaLangLong *)actionTriggerId
 withJavaLangLong:(JavaLangLong *)scheduleId;
 
 
 */

- (void)testSignalStoreTwoObjectsWithDifferentExperimentId {
 
 
    PacoSignalStore * signalStore = [[PacoSignalStore alloc] init];
    
    [signalStore storeSignalWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    [signalStore storeSignalWithJavaLangLong:[JavaLangLong valueOfWithLong:1234567]  withJavaLangLong:[JavaLangLong valueOfWithLong:222]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
     JavaUtilArrayList * list =  (JavaUtilArrayList*)  [signalStore getSignalsWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    int size = [list  size];
     
    
    
    XCTAssert(size ==1 , @"Pass");
}


- (void)testSignalStoreTwoObjectsWithSameExperimentId {
    
    
    PacoSignalStore * signalStore = [[PacoSignalStore alloc] init];
    
    [signalStore storeSignalWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    [signalStore storeSignalWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    JavaUtilArrayList * list =  (JavaUtilArrayList*)  [signalStore getSignalsWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    int size = [list  size];
    
    
    
    XCTAssert(size ==2 , @"Pass");
}


- (void)testSignalStoreAndDelete {
    
    
    PacoSignalStore * signalStore = [[PacoSignalStore alloc] init];
    
    [signalStore storeSignalWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    [signalStore storeSignalWithJavaLangLong:[JavaLangLong valueOfWithLong:1234567]  withJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    [signalStore deleteAll];
    
    JavaUtilArrayList * list =  (JavaUtilArrayList*)  [signalStore getSignalsWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    int size = [list  size];
    XCTAssert(size ==0 , @"Pass");
}





@end
