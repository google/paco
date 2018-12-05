////
////  PacoMediatorTests.m
////  Paco
////
////  Authored by  Tim N. O'Brien on 9/25/15.
////  Copyright (c) 2015 Paco. All rights reserved.
////
//
//#import <UIKit/UIKit.h>
//#import <XCTest/XCTest.h>
//#import "PacoTests.h"
//
//#import <XCTest/XCTest.h>
//#import <XCTest/XCTest.h>
//#import "ExperimentDAO.h"
//#import "PacoNotificationConstants.h"
//#import "PacoSerializer.h"
//#import "java/util/ArrayList.h"
//#import "ExperimentDAO.h"
//#import "DateTime.h"
//#include "EsmSignalStore.h"
//#include "EventStore.h"
//#import "ActionSpecification.h"
//#import "ActionScheduleGenerator.h"
//#import "PacoEventStore.h"
//#import "PacoSignalStore.h"
//#import "java/lang/Long.h"
//#import "NSObject+J2objcKVO.h"
//#import "NSDate+Paco.h"
//#import "OrgJodaTimeDateTime+PacoDateHelper.h"
//#import "PacoNotificationConstants.h"
//#import "PacoExtendedNotificationInfo.h"
//#import "UILocalNotification+PacoExteded.h"
//#import "PacoMediator.h"
//#import "ActionSpecification.h"
//#import "PacoDateUtility.h"
//#import "PacoMediator.h"
//#import "PAExperimentDAO+Helper.h"
//#import "PacoSchedulingUtil.h"
//#import "PacoMediator.h"
//#import "PAExperimentDAO+Helper.h"
//
///* validators */
//
//#import "PacoHasRelevantActionSpecifications.h"
//#import "PacoWasAbleToSaveState.h"
//#import "PacoExperimentIsNotNil.h"
//#import "PacoIsSelfReport.h"
//#import "PacoExperimentIsNotNil.h"
//
///* reporters */
//
//#import "PacoPrintGroupNamesReporter.h"
//#import "PacoExperimentTitleReporter.h" 
//
//@interface PacoMediator ()
//
//@property (strong,nonatomic ) NSMutableArray* allExperiments;
//@property (strong,nonatomic)   NSMutableArray* runningExperiments;
//@property (strong,nonatomic)  NSMutableArray* actionSpecifications;
//@property (strong,nonatomic ) NSMutableArray* oldActionSpecifications;
//
//
///* verifitcation protocols */
//@property (strong,nonatomic ) NSMutableArray* willStartVerifiers;
//@property (strong,nonatomic ) NSMutableArray*  didStartNotifiers;
//@property (strong,nonatomic ) NSMutableArray*  willStopVerifiers;
//@property (strong,nonatomic ) NSMutableArray*  didStopNotifiers;
//
//@end
//
//
//@interface PacoMediatorTests : XCTestCase
//
//@property (nonatomic,strong) PAExperimentDAO* experiment;
//@property (nonatomic,strong) PacoMediator * mediator;
//@end
//
//@implementation PacoMediatorTests
//
//
//static NSString * JSON_DATA= @"{\r\n  \"title\": \"EG^2\",\r\n  \"creator\": \"Timo@google.com\",\r\n  \"contactEmail\": \"Timo@google.com\",\r\n  \"id\": 5629998891270144,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/09\/25\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"Timo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 3,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"One Notification\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/9\/25\",\r\n      \"endDate\": \"2015\/9\/30\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 2,\r\n              \"id\": 1443218396188,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1443218396187,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1443218396189,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    },\r\n    {\r\n      \"name\": \"Four Notifications\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 2,\r\n              \"id\": 1443218396191,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 2,\r\n              \"snoozeTime\": 7380000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 123,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1443218396190,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 32400000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 39600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"pm\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 54000000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 61200000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1443218396192,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";
//
//
//
//- (void)setUp {
//    [super setUp];
//    
//    
//    self.experiment = [self buildExpeiment:JSON_DATA];
//    
//    
//    
//    
// 
//    
//    
//    [[PacoMediator  sharedInstance]
//         registerDidStartNotifiers:@[[PacoExperimentTitleReporter new],  [PacoPrintGroupNameReporter new] ] ];
// 
//    [[PacoMediator sharedInstance] registerWillStartValidators:@[
//                                                               
//                                                               [PacoExperimentIsNotNil new],
//                                                               [PacoWasAbleToSaveState new],
//                                                               [PacoHasRelevantActionSpecifications new],
//                                                               [PacoIsSelfReport new]
//                                                               ]
//     ];
//    
//}
//
//
//
//-(void) testAddToAllExperiments
//{
//    
//    [[PacoMediator sharedInstance] addExperimentToAvailableStore:self.experiment];
//    
//    XCTAssertTrue([[PacoMediator sharedInstance].allExperiments count] ==1);
//    
//    
//}
//
//-(void) testIsActiveFalse
//{
//    
//    [[PacoMediator sharedInstance] addExperimentToAvailableStore:self.experiment];
//    
//    XCTAssertTrue([[PacoMediator sharedInstance] isExperimentLive:self.experiment] == NO);
//    
//}
//
//-(void) testIsActiveIDLiveFalse
//{
//    
//    [[PacoMediator sharedInstance] addExperimentToAvailableStore:self.experiment];
//    
//    XCTAssertTrue([[PacoMediator sharedInstance] isExperimentIdLive:[self.experiment instanceId]] == NO);
//    
//}
//
//
//
//-(void) testStartRunning
//{
//    [[PacoMediator sharedInstance] clearRunningExperiments];
//    [[PacoMediator sharedInstance] addExperimentToAvailableStore:self.experiment];
//    [[PacoMediator sharedInstance] startRunningExperiment:[self.experiment instanceId]];
//    
//    XCTAssertTrue( [[PacoMediator sharedInstance].runningExperiments  count] == 1);
//      [[PacoMediator sharedInstance] clearRunningExperiments];
//    XCTAssertTrue( [[PacoMediator sharedInstance].runningExperiments  count] == 0);
//}
//
//
//-(void) testIsExperimentLiveYES
//{
//    
//    [[PacoMediator sharedInstance] clearRunningExperiments];
//    [[PacoMediator sharedInstance] addExperimentToAvailableStore:self.experiment];
//    [[PacoMediator sharedInstance] startRunningExperiment:[self.experiment instanceId]];
//    
//    XCTAssertTrue( [[PacoMediator sharedInstance] isExperimentIdLive:[self.experiment instanceId]]);
//}
//
//-(void) testStopRunningExperiment
//{
//    
//    [[PacoMediator sharedInstance] clearRunningExperiments];
//    [[PacoMediator sharedInstance] addExperimentToAvailableStore:self.experiment];
//    [[PacoMediator sharedInstance] startRunningExperiment:[self.experiment instanceId]];
//    
//    XCTAssertTrue( [[PacoMediator sharedInstance] isExperimentIdLive:[self.experiment instanceId]]);
//    
//    [[PacoMediator sharedInstance] stopRunningExperiment:[self.experiment instanceId]];
//      XCTAssertTrue( [[PacoMediator sharedInstance] isExperimentIdLive:[self.experiment instanceId]] == NO) ;
//}
//
//
//
//- (void)tearDown {
//    // Put teardown code here. This method is called after the invocation of each test method in the class.
//    [super tearDown];
//}
//
//- (void)testExample {
//    // This is an example of a functional test case.
//    XCTAssert(YES, @"Pass");
//}
//
//- (void)testPerformanceExample {
//    // This is an example of a performance test case.
//    [self measureBlock:^{
//        // Put the code you want to measure the time of here.
//    }];
//}
//
//
//
//#pragma mark - helper methods
//-(PAExperimentDAO*) buildExpeiment:(NSString*) json
//{
//    
//    NSData* data =nil;
//    data=  [json dataUsingEncoding:NSUTF8StringEncoding];
//
//    PacoSerializer* serializer =
//    [[PacoSerializer alloc] initWithArrayOfClasses:nil
//                          withNameOfClassAttribute:@"nameOfClass"];
//    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
//    IOSObjectArray * iosArray = [resultArray toArray];
//    PAExperimentDAO * dao =  [iosArray objectAtIndex:0];
//    return dao;
//    
//}
//
//
//
//
//
///*
// 
// return a unique id for an object
// */
//-(NSValue*) uniqueId:(NSObject*) actionSpecification
//{
//    return [NSValue valueWithPointer:(__bridge const void *)(actionSpecification)];
//}
//
//
//@end
