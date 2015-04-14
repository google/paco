#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "Integer.h"
#import "java/util/ArrayList.h"
#import "TimeUtil.h"
#import "SignalTime.h"
#import "InterruptTrigger.h"
#import "ExperimentValidator.h"
#import "InterruptCue.h"
#import "PacoAction.h"
#import "PacoNotificationAction.h"
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

- (void)testSimpleStaticCallIntoGeneratedCode {
  int d = OrgJodaTimeDateTimeConstants_SATURDAY;
  XCTAssertTrue([PATimeUtil isWeekendWithInt:d], @"Pass");
}

- (void)testSimpleObjectInstantationCallIntoGeneratedCode {
  PASignalTime *st = [PASignalTime new];
  XCTAssertNotNil(st, @"should have instantiated object");
  int type = (int)[st getType];
  XCTAssertEqual((int)PASignalTime_FIXED_TIME_, (int)type, @"should have returned FIXED_TIME type");
}

- (void)testSimpleObjectCtorWParamsInstantationCallIntoGeneratedCode {
  JavaLangInteger *fixedTimeMillisFromMidnight = JavaLangInteger_valueOfWithInt_(8 * 60 * 60 * 1000);
  PASignalTime *st = [[PASignalTime alloc ]initWithJavaLangInteger:PASignalTime_FIXED_TIME_
                                        withJavaLangInteger:PASignalTime_OFFSET_BASIS_RESPONSE_TIME_
                                        withJavaLangInteger:fixedTimeMillisFromMidnight
                                        withJavaLangInteger:PASignalTime_MISSED_BEHAVIOR_USE_SCHEDULED_TIME_
                                        withJavaLangInteger:nil
                                                       withNSString:@"Morning signal"];
  XCTAssertNotNil(st, @"should have instantiated object");
  int type = (int)[st getType];
  XCTAssertEqual((int)PASignalTime_FIXED_TIME_, (int)type, @"should have returned FIXED_TIME type");
  XCTAssertEqual(@"Morning signal", st.getLabel, @"should have returned Morning signal as label");
}

- (void)testSubtypeObjectInstantationCallIntoGeneratedCode {
  PAInterruptTrigger *st = [PAInterruptTrigger new];
  PAInterruptCue *cue = [PAInterruptCue new];
  JavaUtilArrayList *cueList = [JavaUtilArrayList new];
  [cueList addWithId:cue];
  [st setCuesWithJavaUtilList:cueList];
  
  
  XCTAssertNotNil(st, @"should have instantiated object");
  XCTAssertNotNil(cue, @"should have instantiated object");

  XCTAssertEqual(1, [[st getCues] size], @"should have returned proper numer of cues");
  XCTAssertEqualObjects(@"interruptTrigger", [st getType], @"should have returned correct type");
  
  PAExperimentValidator *validator = [PAExperimentValidator new];
  [st validateActionsWithPAValidator:validator];
  NSString *results = [validator stringifyResults];
  XCTAssertEqualObjects(@"ERROR: ActionTrigger actions should contain at least one action\n", results);
  
  PAPacoNotificationAction *action = [PAPacoNotificationAction new];
  JavaUtilArrayList *actionList = [JavaUtilArrayList new];
  [actionList addWithId:action];
  [st setActionsWithJavaUtilList:actionList];
  
  PAExperimentValidator *validator2 = [PAExperimentValidator new];
  [st validateActionsWithPAValidator:validator2];
  NSString *results2 = [validator2 stringifyResults];
  XCTAssertEqualObjects(@"", results2);

}


@end
