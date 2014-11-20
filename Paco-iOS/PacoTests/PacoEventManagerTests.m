/* Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>

#import "FakePacoEventManager.h"
#import "PacoEvent.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"

@interface PacoEventManagerTests : XCTestCase

@end

@implementation PacoEventManagerTests
{
    FakePacoEventManager *fakePacoEventManager;
    PacoEvent *pacoEvent;
    PacoExperimentDefinition *pacoExperimentDefinition;
}

- (void)setUp {
    
    [super setUp];
    
    fakePacoEventManager = [FakePacoEventManager defaultManager];
    
    pacoExperimentDefinition = [PacoExperimentDefinition testPacoExperimentDefinition];
    
    pacoEvent = [[PacoEvent alloc] init];
    
    pacoEvent = [PacoEvent pacoEventForIOS];
    pacoEvent.experimentId = pacoExperimentDefinition.experimentId;
}

- (void)tearDown {
    
    fakePacoEventManager = nil;
    pacoEvent = nil;
    pacoExperimentDefinition = nil;
    
    [super tearDown];
}

- (void)testParticipateStatusCanBeCreated
{
    PacoParticipateStatus *testParticipateStatus = [fakePacoEventManager statsForExperiment:pacoExperimentDefinition.experimentId];
    
    XCTAssertNotNil(testParticipateStatus, @"Participate status should not be nil");
}

- (void)testParticiateStatusDetailsAssignedCorrectly
{
    // PacoEventTypeJoin
    pacoEvent.responses = @[
                            @{
                                @"name" : @"joined",
                                @"answer" : @"true",
                                @"inputId" : @"-1"
                             }
                           ];
    
    [fakePacoEventManager saveEvent:pacoEvent];
    
    PacoParticipateStatus *testParticipateStatus = [fakePacoEventManager statsForExperiment:pacoExperimentDefinition.experimentId];
    
    XCTAssertEqual(testParticipateStatus.numberOfNotifications, 0);
    XCTAssertEqual(testParticipateStatus.numberOfParticipations, 0);
    XCTAssertEqual(testParticipateStatus.numberOfSelfReports, 0);
    XCTAssertEqual(testParticipateStatus.percentageOfParticipation, 0);
    XCTAssertNil(testParticipateStatus.percentageText);
    
    // PacoEventTypeStop
    pacoEvent.responses = @[
                             @{
                                 @"name" : @"joined",
                                 @"answer" : @"false",
                                 @"inputId" : @"-1"
                                 }
                             ];
    
    [fakePacoEventManager saveEvent:pacoEvent];
    
    testParticipateStatus = [fakePacoEventManager statsForExperiment:pacoExperimentDefinition.experimentId];
    
    XCTAssertEqual(testParticipateStatus.numberOfNotifications, 0);
    XCTAssertEqual(testParticipateStatus.numberOfParticipations, 0);
    XCTAssertEqual(testParticipateStatus.numberOfSelfReports, 0);
    XCTAssertEqual(testParticipateStatus.percentageOfParticipation, 0);
    XCTAssertNil(testParticipateStatus.percentageText);
    
    // PacoEventTypeSurvey
    pacoEvent.responses = @[
                             @{
                                 @"name" : @"not joined",
                                 @"answer" : @"false",
                                 @"inputId" : @"-1"
                                 }
                           ];
    
    pacoEvent.responseTime = [NSDate date];
    pacoEvent.scheduledTime = [NSDate date];
    
    [fakePacoEventManager saveEvent:pacoEvent];
    
    testParticipateStatus = [fakePacoEventManager statsForExperiment:pacoExperimentDefinition.experimentId];
    
    XCTAssertEqual(testParticipateStatus.numberOfNotifications, 2);
    XCTAssertEqual(testParticipateStatus.numberOfParticipations, 2);
    XCTAssertEqual(testParticipateStatus.numberOfSelfReports, 0);
    XCTAssertEqual(testParticipateStatus.percentageOfParticipation, 1);
    XCTAssertTrue([testParticipateStatus.percentageText isEqualToString:@"100%"]);
    
    // PacoEventTypeMiss
    pacoEvent.responseTime = nil;
    
    [fakePacoEventManager saveEvent:pacoEvent];
    
    testParticipateStatus = [fakePacoEventManager statsForExperiment:pacoExperimentDefinition.experimentId];
    
    XCTAssertEqual(testParticipateStatus.numberOfNotifications, 3);
    XCTAssertEqual(testParticipateStatus.numberOfParticipations, 0);
    XCTAssertEqual(testParticipateStatus.numberOfSelfReports, 0);
    XCTAssertEqual(testParticipateStatus.percentageOfParticipation, 0);
    XCTAssertTrue([testParticipateStatus.percentageText isEqualToString:@"0%"]);
    
    // PacoEventType
    pacoEvent.responseTime = [NSDate date];
    pacoEvent.scheduledTime = nil;
    
    [fakePacoEventManager saveEvent:pacoEvent];
    
    testParticipateStatus = [fakePacoEventManager statsForExperiment:pacoExperimentDefinition.experimentId];
    
    XCTAssertEqual(testParticipateStatus.numberOfNotifications, 0);
    XCTAssertEqual(testParticipateStatus.numberOfParticipations, 0);
    XCTAssertEqual(testParticipateStatus.numberOfSelfReports, 4);
    XCTAssertEqual(testParticipateStatus.percentageOfParticipation, 0);
    XCTAssertNil(testParticipateStatus.percentageText);
}


//@property(nonatomic, readonly) NSUInteger numberOfNotifications;
//@property(nonatomic, readonly) NSUInteger numberOfParticipations;
//@property(nonatomic, readonly) NSUInteger numberOfSelfReports;
//@property(nonatomic, readonly) float percentageOfParticipation; //0.867
//@property(nonatomic, copy, readonly) NSString *percentageText; //87%
//
//@end
//
//
////YMZ:TODO: fully testing
////YMZ:TODO: thread safe
////YMZ:TODO: use async design
////YMZ:TODO: use core data
////YMZ:TODO: error handling of file operation
//@interface PacoEventManager : NSObject
//
//+ (PacoEventManager*)defaultManager;
//
//- (void)saveEvent:(PacoEvent*)event;
//- (void)saveEvents:(NSArray*)events;
//
//- (void)startUploadingEvents;
//
////When background fetch API triggers or location significantly changes, call this method
////to upload events in a limited time frame, we are allowed to finish our tasks in 30 seconds.
//- (void)startUploadingEventsInBackgroundWithBlock:(void(^)(UIBackgroundFetchResult))completionBlock;
//
//- (void)stopUploadingEvents;
//
//
//- (void)saveJoinEventWithDefinition:(PacoExperimentDefinition*)definition
//                       withSchedule:(PacoExperimentSchedule*)schedule;
//- (void)saveStopEventWithExperiment:(PacoExperiment*)experiment;
//- (void)saveSelfReportEventWithDefinition:(PacoExperimentDefinition*)definition
//                                andInputs:(NSArray*)visibleInputs;
//- (void)saveSurveySubmittedEventForDefinition:(PacoExperimentDefinition*)definition
//                                   withInputs:(NSArray*)inputs
//                             andScheduledTime:(NSDate*)scheduledTime;
//
//- (PacoParticipateStatus*)statsForExperiment:(NSString*)experimentId;


@end
