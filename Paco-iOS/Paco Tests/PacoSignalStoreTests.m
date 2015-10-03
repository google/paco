//
//  PacoSignalStoreTests.m
//  Paco
//
//  Created by northropo on 10/2/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "PacoSignalStore.h"
#import "java/lang/Long.h"
#include <stdlib.h>





@interface PacoSignalStoreTests : XCTestCase

@property (nonatomic,strong) PacoSignalStore* signalStore;

@end

@implementation PacoSignalStoreTests

- (void)setUp {
    [super setUp];
    _signalStore = [[PacoSignalStore alloc] init];
    [_signalStore deleteAll];
}




/*
 
 -(NSArray*)  matchRecords:(JavaLangLong *)date
 withJavaLangLong:(JavaLangLong *)experimentId
 withJavaLangLong:(JavaLangLong *)alarmTime
 withNSString:(NSString *)groupName
 withJavaLangLong:(JavaLangLong *)actionTriggerId
 withJavaLangLong:(JavaLangLong *)scheduleId;
 
 
 */

-(void) testCreateRecord
{
    JavaLangLong* jll =  [JavaLangLong valueOfWithLong:arc4random_uniform(74)];
    
    
    [_signalStore   storeSignalWithJavaLangLong:jll  withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withNSString:@"mygroupname"  withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withJavaLangLong:[JavaLangLong valueOfWithLong:12345]];
    
    /* create duplicate record but with different experimentid*/
    [_signalStore   storeSignalWithJavaLangLong:[JavaLangLong valueOfWithLong:123457] withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withNSString:@"mygroupname"  withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withJavaLangLong:[JavaLangLong valueOfWithLong:12345]];
    
    NSArray * signals  = [_signalStore  matchRecords:jll   withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withNSString:@"mygroupname" withJavaLangLong:[JavaLangLong valueOfWithLong:12345]withJavaLangLong:[JavaLangLong valueOfWithLong:12345]];
    XCTAssert([signals count] ==1 , @"one and only one record should exist");
    
    
    
}

-(void) testEqualsInArrayUsingJavaLangLong
{
    JavaLangLong * numberOne =   [JavaLangLong valueOfWithLong:12345];
    JavaLangLong * numberTwo =   [JavaLangLong valueOfWithLong:7890];
    JavaLangLong * compareOne =   [JavaLangLong valueOfWithLong:12345];
    JavaLangLong * compareTwo =   [JavaLangLong valueOfWithLong:7890];
    JavaLangLong * controlNumber  =   [JavaLangLong valueOfWithLong:1111];
    NSMutableArray* numbers = [[NSMutableArray alloc] init];
    
    [numbers addObject:numberOne];
    [numbers addObject:numberTwo];

    XCTAssert([numbers containsObject:compareOne] , @"success");
    XCTAssert([numbers containsObject:compareTwo] , @"success");
    XCTAssert(![numbers containsObject:controlNumber] , @"success");
    
    
    
}

-(void) testDeleteAll
{
    
      JavaLangLong* jll =  [JavaLangLong valueOfWithLong:arc4random_uniform(74)];
    
        [_signalStore   storeSignalWithJavaLangLong:jll withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withNSString:@"mygroupname"  withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withJavaLangLong:[JavaLangLong valueOfWithLong:12345]];
    
         NSArray * signals  = [_signalStore  matchRecords:jll  withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withNSString:@"mygroupname" withJavaLangLong:[JavaLangLong valueOfWithLong:12345]withJavaLangLong:[JavaLangLong valueOfWithLong:12345]];
    
        XCTAssert([signals count] ==1 , @"Fail");
      [_signalStore deleteAll];
    
   signals  = [_signalStore  matchRecords:[JavaLangLong valueOfWithLong:123456]  withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withJavaLangLong:[JavaLangLong valueOfWithLong:12345] withNSString:@"mygroupname" withJavaLangLong:[JavaLangLong valueOfWithLong:12345]withJavaLangLong:[JavaLangLong valueOfWithLong:12345]];
    XCTAssert([signals count] ==0 , @"Fail");
}

-(void) testDeleteAllSignalsWithExperimentId
{
    
    
    
}

-(void) tearDown
{
    
    [_signalStore deleteAll];
    
    
}


@end
