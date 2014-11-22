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

#import "PacoEventManager.h"
#import "PacoEvent.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"

@interface PacoEventManagerTests : XCTestCase

@end

@implementation PacoEventManagerTests
{
    PacoEventManager *fakePacoEventManager;
    PacoEvent *pacoEvent;
    PacoExperimentDefinition *pacoExperimentDefinition;
}

- (void)setUp {
    
    [super setUp];
    
    fakePacoEventManager = [PacoEventManager defaultManager];
    
    pacoExperimentDefinition = [PacoExperimentDefinition testPacoExperimentDefinition];
    
    pacoEvent = [PacoEvent pacoEventForIOS];
    pacoEvent.experimentId = pacoExperimentDefinition.experimentId;
    pacoEvent.responseTime = [NSDate date];
}

- (void)tearDown {
    
    fakePacoEventManager = nil;
    pacoEvent = nil;
    pacoExperimentDefinition = nil;
    
    [super tearDown];
}

- (void)testCoreDataCleared
{
    // NOTE: Because Core Data is not automatically cleared in tearDown, results vary if Core Data is not manually cleared or if individual tests are run versus running whole PacoEventManagerTests suite
    
    BOOL iClearedCoreData = NO;
    
    XCTAssertTrue(iClearedCoreData, @"Core Data should be cleared to acheive consistent test results.");
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
    
    XCTAssertEqual(testParticipateStatus.numberOfNotifications, 1);
    XCTAssertEqual(testParticipateStatus.numberOfParticipations, 1);
    XCTAssertEqual(testParticipateStatus.numberOfSelfReports, 1);
    XCTAssertEqual(testParticipateStatus.percentageOfParticipation, 1);
    XCTAssertTrue([testParticipateStatus.percentageText isEqualToString:@"100%"]);
    
    // PacoEventTypeMiss
    pacoEvent.responseTime = nil;
    
    [fakePacoEventManager saveEvent:pacoEvent];
    
    testParticipateStatus = [fakePacoEventManager statsForExperiment:pacoExperimentDefinition.experimentId];
    
    XCTAssertEqual(testParticipateStatus.numberOfNotifications, 1);
    XCTAssertEqual(testParticipateStatus.numberOfParticipations, 0);
    XCTAssertEqual(testParticipateStatus.numberOfSelfReports, 1);
    XCTAssertEqual(testParticipateStatus.percentageOfParticipation, 0);
    XCTAssertTrue([testParticipateStatus.percentageText isEqualToString:@"0%"]);
    
    // PacoEventType
    pacoEvent.responseTime = [NSDate date];
    pacoEvent.scheduledTime = nil;
    
    [fakePacoEventManager saveEvent:pacoEvent];
    
    testParticipateStatus = [fakePacoEventManager statsForExperiment:pacoExperimentDefinition.experimentId];
    
    XCTAssertEqual(testParticipateStatus.numberOfNotifications, 0);
    XCTAssertEqual(testParticipateStatus.numberOfParticipations, 0);
    XCTAssertEqual(testParticipateStatus.numberOfSelfReports, 2);
    XCTAssertEqual(testParticipateStatus.percentageOfParticipation, 0);
    XCTAssertNil(testParticipateStatus.percentageText);
}

@end