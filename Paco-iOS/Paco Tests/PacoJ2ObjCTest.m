//
//  PacoJ2ObjCTest.m
//  Paco
//
//  Created by Northrop O'brien on 3/15/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "NSObject+J2objcKVO.h"
#import "ModelBase.h"
#import <objc/runtime.h>
#include "java/util/ArrayList.h"
#include "java/util/Iterator.h"
#include "java/lang/Boolean.h"
#include "java/lang/Long.h"
#include "java/lang/Integer.h"
#include "java/lang/Float.h"
#include "java/lang/Double.h"
#include "java/lang/Boolean.h"
#include "java/lang/Short.h"
#include "java/lang/Character.h"
#include "J2ObjC_header.h"

@interface PacoJ2ObjCTest : XCTestCase

@end

@implementation PacoJ2ObjCTest

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testExample {
    
 
 
    
    long long ll = 0;
    JavaLangLong* typedArg = create_JavaLangLong_initWithLong_(ll);
    
    
    
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
