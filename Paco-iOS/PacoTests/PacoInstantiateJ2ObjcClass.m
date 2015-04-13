#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "TimeUtil.h"
#import "DateTimeConstants.h"

@interface PacoInstantiateJ2ObjcClass : XCTestCase

@end

@implementation PacoInstantiateJ2ObjcClass

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testExample {
    // This is an example of a functional test case.
  int d = OrgJodaTimeDateTimeConstants_SATURDAY;
  XCTAssertTrue([PATimeUtil isWeekendWithInt:d], @"Pass");
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
