//
//  PacoJAVA2OBJCTest.m
//  Paco
//
//  Created by northropo on 8/26/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#include "java/lang/Boolean.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/List.h"
#import  "SignalTime.h"
@interface PacoJAVA2OBJCTest : XCTestCase

@end

@implementation PacoJAVA2OBJCTest

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}


- (void)testIntegerEquality {
    
    JavaLangInteger *i = [JavaLangInteger valueOfWithInt:3];
    
    JavaLangInteger *i2 = [JavaLangInteger valueOfWithInt:0];
    
    XCTAssert(i == i2, @"should be equal due to unboxing"); // works
    
    BOOL btst= (i == 3);

    XCTAssert(i == 3, @"should be equal due to unboxing"); // works
    
    XCTAssert(i == PASignalTime_get_FIXED_TIME_(), @"should it work?"); // works
    
    
    
    PASignalTime *p = [PASignalTime alloc];
    
    PASignalTime_initWithJavaLangInteger_withJavaLangInteger_withJavaLangInteger_withJavaLangInteger_withJavaLangInteger_withNSString_(p,                                                                                                                                                        [JavaLangLong valueOfWithLong:1], // tests below will pass with either this value of 0
                                                                                                                                       
                                                                                                                                       //[JavaLangInteger valueOfWithInt:0], // they also pass with this value of 0 instead
                                                                                                                                       
                                                                                                                                       PASignalTime_OFFSET_BASIS_SCHEDULED_TIME_,
                                                                                                                                       
                                                                                                                                       [JavaLangInteger valueOfWithInt:1000 * 60 * 60 * 10],
                                                                                                                                       
                                                                                                                                       PASignalTime_MISSED_BEHAVIOR_USE_SCHEDULED_TIME_,
                                                                                                                                       
                                                                                                                                       [JavaLangInteger valueOfWithInt:0],
                                                                                                                                       
                                                                                                                                       @"First time");
    BOOL b1 = [p getType] == PASignalTime_get_OFFSET_TIME_() ;
    BOOL b2 = [p getType] ==  [NSNumber numberWithInt:1];
     BOOL b3 = [p getType] ==  [NSNumber numberWithInt:0];
    
    XCTAssert(b1, @"should work"); // works
    XCTAssert(b2, @"should work"); // works
    XCTAssert(b3, @"should work"); // works
    
   // XCTAssert([p getType] ==         [JavaLangInteger valueOfWithInt:1], @"should work");  //works
    
    //XCTAssert([p getType] == [NSNumber numberWithInt:0], @"should work"); / /does not work
    
}

- (void)testExample {
    
    JavaLangLong * pt1 = [[JavaLangLong alloc] initWithLong:12345];
    JavaLangLong * pt2 = [[JavaLangLong alloc] initWithLong:12345];
    
    
    JavaLangInteger *  pt11 = [[JavaLangInteger   alloc] initWithInt:12345];
    JavaLangInteger  * pt22 = [[JavaLangInteger  alloc]  initWithInt:12345];
    
    char* c = [pt11 objCType];
    char* cc = [pt1 objCType];
    
    JavaLangInteger *  pt111 = [JavaLangInteger valueOfWithInt:1234];
    JavaLangInteger  * pt222 = [JavaLangInteger valueOfWithInt:1234];
    
    
    JavaLangInteger *  ptzero1 = [JavaLangInteger valueOfWithInt:0];
    JavaLangInteger  * ptzero2 = [JavaLangInteger valueOfWithInt:0];
    
    BOOL b = (pt1 == pt2);
    BOOL b0 =(ptzero1==ptzero2);
    BOOL b1 = (pt11==pt22);
    BOOL b2 = (pt111==pt222);
    
    XCTAssert(b, @"Pass");
    XCTAssert(b0, @"Pass");
    XCTAssert(b1, @"Pass");
    XCTAssert(b2, @"Pass");
}





- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
