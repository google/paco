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

#import "PacoEvent.h"
#import "PacoClient.h"
#import "PacoDateUtility.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperimentInput.h"
#import "PacoExperimentSchedule.h"

@interface PacoEventTests : XCTestCase
{
    PacoEvent *pacoEventForIOS;
    NSString *testJSONString;
    NSDictionary *testJSONDictionary;
    PacoExperimentDefinition *pacoExperimentDefinition;
    NSArray *pacoExperimentInputs;
    NSDate *pacoEventScheduledTime;
}
@end

@implementation PacoEventTests

- (void)setUp
{
    [super setUp];
    
    pacoEventForIOS = [PacoEvent pacoEventForIOS];
    pacoEventForIOS.who = @"Test who";
    pacoEventForIOS.when = [PacoDateUtility pacoDateForString:@"1970/11/15 00:10:50.25Z"];
    pacoEventForIOS.latitude = (long long)37.789836;
    pacoEventForIOS.longitude = (long long)-122.390581;
    pacoEventForIOS.responseTime = [PacoDateUtility pacoDateForString:@"1970/11/15 00:15:50.25Z"];
    pacoEventForIOS.scheduledTime = [PacoDateUtility pacoDateForString:@"1970/11/15 00:20:50.25Z"];
    pacoEventForIOS.experimentId = @"Test experimentId";
    pacoEventForIOS.experimentName = @"Test experimentName";
    pacoEventForIOS.experimentVersion = 10;
    pacoEventForIOS.responses = @[
                                    @{
                                        @"name" : @"Test name",
                                        @"answer" : @"false",
                                        @"inputId" : @"-1"
                                    }
                                  ];
    
    testJSONString =
    @"["
        @"{"
            @"\"who\": \"Test who\","
            @"\"when\": \"1970/11/15 00:10:50.25Z\","
            @"\"lat\": 37.789836,"
            @"\"long\": -122.390581,"
            @"\"responseTime\": \"1970/11/15 00:15:50.25Z\","
            @"\"appId\": \"iOS\","
            @"\"scheduledTime\": \"1970/11/15 00:20:50.25Z\","
            @"\"pacoVersion\": \"1.1.3\","
            @"\"experimentId\": \"Test experimentId\","
            @"\"experimentName\": \"Test experimentName\","
            @"\"experimentVersion\": 10,"
            @"\"responses\": ["
                @"{"
                    @"\"name\": \"Test name\","
                    @"\"answer\": \"false\","
                    @"\"inputId\": \"-1\""
                @"}"
            @"]"
        @"}"
    @"]";
    
    NSData *jsonData = [testJSONString dataUsingEncoding:NSUTF8StringEncoding];
    id jsonObject = [NSJSONSerialization JSONObjectWithData:jsonData options:0 error:nil];
    testJSONDictionary = (id)jsonObject[0];
    
    pacoExperimentDefinition = [PacoExperimentDefinition testPacoExperimentDefinition];
    
    pacoExperimentInputs = @[ [[PacoExperimentInput alloc] init] ];
    
    pacoEventScheduledTime = [NSDate date];
}

- (void)tearDown
{
    pacoEventForIOS = nil;
    testJSONString = nil;
    testJSONDictionary = nil;
    pacoExperimentDefinition = nil;
    pacoExperimentInputs = nil;
    pacoEventScheduledTime = nil;
    
    [super tearDown];
}

- (void)testPacoEventCanBeCreated
{
    PacoEvent *testEvent = [[PacoEvent alloc] init];
    
    XCTAssertNotNil(testEvent, @"Should be able to create a PacoEvent instance.");
}

- (void)testPacoEventForIOSCanBeCreated
{
    XCTAssertNotNil(pacoEventForIOS, @"Should be able to create a PacoEvent instance via pacoEventForIOS");
}

- (void)testPacoEventCanBeCreatedFromJSON
{
    PacoEvent *testEvent = [PacoEvent pacoEventFromJSON:testJSONDictionary];
    
    XCTAssertNotNil(testEvent, @"Should be able to create a PacoEvent instance from JSON");
    XCTAssertEqualObjects(testEvent.who, @"Test who", @"Should be able to set 'who' from JSON");
    XCTAssertEqual(testEvent.when, [PacoDateUtility pacoDateForString:@"1970/11/15 00:10:50.25Z"], @"Should be able to set 'when' from JSON");
    XCTAssertEqual(testEvent.latitude, [[NSNumber numberWithDouble:37.789836] longLongValue], @"Should be able to set 'latitude from JSON'");
    XCTAssertEqual(testEvent.longitude, [[NSNumber numberWithDouble:-122.390581] longLongValue], @"Should be able to set 'longitude from JSON'");
    XCTAssertEqual(testEvent.responseTime, [PacoDateUtility pacoDateForString:@"1970/11/15 00:15:50.25Z"], @"Should be able to set 'responseTime from JSON'");
    XCTAssertEqual(testEvent.scheduledTime, [PacoDateUtility pacoDateForString:@"1970/11/15 00:20:50.25Z"], @"Should be able to set 'scheduledTime from JSON'");
    XCTAssertEqualObjects(testEvent.appId, @"iOS", @"Should be able to set 'appId' from JSON");
    XCTAssertEqualObjects(testEvent.pacoVersion, @"1.1.3", @"Should be able to set 'pacoVersion from JSON'");
    XCTAssertEqualObjects(testEvent.experimentId, @"Test experimentId", @"Should be able to set 'experimentId' from JSON");
    XCTAssertEqualObjects(testEvent.experimentName, @"Test experimentName", @"Should be able to set 'experimentName' from JSON");
    XCTAssertEqual(testEvent.experimentVersion, 10, @"Should be able to set 'experimentVersion' from JSON");
    
    NSDictionary *response = @{
                                @"name" : @"Test name",
                                @"answer" : @"false",
                                @"inputId" : @"-1"
                              };
    
    XCTAssertEqualObjects(testEvent.responses, @[ response ], @"Should be able to set 'responses' from JSON");
}

- (void)testEventTypeReturnedCorrectly
{
    pacoEventForIOS.responses = @[
                                    @{
                                        @"name" : @"joined",
                                        @"answer" : @"true",
                                        @"inputId" : @"-1"
                                    }
                                ];
    
    XCTAssertEqual([pacoEventForIOS type], PacoEventTypeJoin, @"Type should be correctly returned");
    
    pacoEventForIOS.responses = @[
                                  @{
                                      @"name" : @"joined",
                                      @"answer" : @"false",
                                      @"inputId" : @"-1"
                                      }
                                  ];
    
    XCTAssertEqual([pacoEventForIOS type], PacoEventTypeStop, @"Type should be correctly returned");
    
    pacoEventForIOS.responses = @[
                                  @{
                                      @"name" : @"not joined",
                                      @"answer" : @"false",
                                      @"inputId" : @"-1"
                                      }
                                  ];
    
    pacoEventForIOS.responseTime = [NSDate date];
    pacoEventForIOS.scheduledTime = [NSDate date];
    
    XCTAssertEqual([pacoEventForIOS type], PacoEventTypeSurvey, @"Type should be correctly returned");
    
    pacoEventForIOS.responseTime = nil;
    
     XCTAssertEqual([pacoEventForIOS type], PacoEventTypeMiss, @"Type should be correctly returned");
    
    pacoEventForIOS.responseTime = [NSDate date];
    pacoEventForIOS.scheduledTime = nil;
    
    XCTAssertEqual([pacoEventForIOS type], PacoEventTypeSelfReport, @"Type should be correctly returned");
}

- (void)testDescriptionReturnedCorrectly
{
    // NOTE: At time of writing tests, PacoDateUtility dateFromString is returning nil; set here explicitly to explain test results
    pacoEventForIOS.responseTime = nil;
    pacoEventForIOS.scheduledTime = nil;
    
    NSString *description = [pacoEventForIOS description];
    NSString* expectedDescription = [NSString stringWithFormat:@"<PacoEvent, %p: id=Test experimentId,name=Test experimentName,version=10,responseTime=(null),who=Test who,when=(null),response=\r[{answer:false,name:Test name,inputId:-1}]>", pacoEventForIOS];
    
    XCTAssertNotNil(description, @"Should be able to create a description");
    XCTAssertTrue([description isEqualToString:expectedDescription], @"Description should be set properly");
}

- (void)testJsonObjectCanBeGeneratedFromPacoEvent
{
    id jsonObject = [pacoEventForIOS generateJsonObject];
    
    XCTAssertNotNil(jsonObject, @"Should be able to create jsonObject from event");
    XCTAssertEqualObjects(jsonObject[@"who"], pacoEventForIOS.who, @"Value for 'who' should match in event and JSON");
    XCTAssertEqualObjects([PacoDateUtility pacoDateForString:jsonObject[@"when"]], pacoEventForIOS.when, @"Value for 'when' should match in event and JSON");
    XCTAssertEqual([jsonObject[@"lat"] longLongValue], pacoEventForIOS.latitude, @"Value for 'latitude' should match in event and JSON");
    XCTAssertEqual([jsonObject[@"long"] longLongValue], pacoEventForIOS.longitude, @"Value for 'longitude' should match in event and JSON");
    XCTAssertEqual([PacoDateUtility pacoDateForString:jsonObject[@"responseTime"]], pacoEventForIOS.responseTime, @"Value for 'responseTime' should match in event and JSON");
    XCTAssertEqual([PacoDateUtility pacoDateForString:jsonObject[@"scheduledTime"]], pacoEventForIOS.scheduledTime, @"Value for 'scheduledTime' should match in event and JSON");
    XCTAssertEqual(jsonObject[@"appId"], pacoEventForIOS.appId, @"Value for 'appId' should match in event and JSON");
    XCTAssertEqual(jsonObject[@"pacoVersion"], pacoEventForIOS.pacoVersion, @"Value for 'pacoVersion' should match in event and JSON");
    XCTAssertEqual(jsonObject[@"experimentId"], pacoEventForIOS.experimentId, @"Value for 'experimentId' should match in event and JSON");
    XCTAssertEqual(jsonObject[@"experimentName"], pacoEventForIOS.experimentName, @"Value for 'experimentName' should match in event and JSON");
    XCTAssertEqual([jsonObject[@"experimentVersion"] intValue], pacoEventForIOS.experimentVersion, @"Value for 'experimentVersoin' should match in event and JSON");
    XCTAssertEqual(jsonObject[@"responses"], pacoEventForIOS.responses, @"Value for 'responses' should match in event and JSON");
}

- (void)testCorrectJsonWithImagePayloadReturnedForZeroOrMoreResponses
{
    pacoEventForIOS.responses = nil;
    
    id jsonObjectWithoutImagePayload = [pacoEventForIOS generateJsonObject];
    
    XCTAssertEqualObjects([pacoEventForIOS payloadJsonWithImageString], jsonObjectWithoutImagePayload, @"Events with no responses should not have an image payload");
    
    // TODO: Test for event with responses and valid image name
}

- (void)testEventJoinedCorrectlyGivenDefinitionAndSchedule
{
    PacoExperimentDefinition *experimentDefinition = [PacoExperimentDefinition testPacoExperimentDefinition];
    
    XCTAssertNotNil(experimentDefinition, @"Definition used for testing should not be nil");
    
    PacoEvent *eventGivenDefinition = [PacoEvent joinEventForDefinition:experimentDefinition withSchedule:nil];
    
    XCTAssertNotNil(eventGivenDefinition, @"Should be able to create event from definition");
    
    // Check response indicates user has joined
    // NOTE: special values may change
    NSDictionary *response = eventGivenDefinition.responses[0];
    
    XCTAssertTrue([response[@"answer"] boolValue], @"Should have special values for event responses to indicate user has joined experiment");
    XCTAssertTrue([response[@"inputId"] isEqualToString:@"-1"], @"Should have special values for event responses to indicate user has joined experiment");
    XCTAssertTrue([response[@"name"] isEqualToString:@"joined"], @"Should have special values for event responses to indicate user has joined experiment");
}

- (void)testEventStoppedCorrectlyGivenExperiment
{
    XCTAssertNotNil(pacoExperimentDefinition, @"Definition used for testing should not be nil");
    
    PacoExperiment *experiment = [PacoExperiment experimentWithDefinition:pacoExperimentDefinition schedule:nil joinTime:[NSDate date]];
    
    XCTAssertNotNil(experiment, @"Experiment used for testing should not be nil");

    PacoEvent *testEvent = [PacoEvent stopEventForExperiment:experiment];
    
    // Check response indicates event has stopped
    // NOTE: special values may change
    
    XCTAssertTrue([testEvent.who isEqualToString:[[PacoClient sharedInstance] userEmail]], @"Event details should be set");
    XCTAssertEqual(testEvent.experimentId, pacoExperimentDefinition.experimentId, @"Event details should be set");
    XCTAssertEqual(testEvent.experimentName, pacoExperimentDefinition.title, @"Event details should be set");
    XCTAssertEqual(testEvent.experimentVersion, pacoExperimentDefinition.experimentVersion, @"Event details should be set");
    
    NSDictionary *response = testEvent.responses[0];
    
    XCTAssertFalse([response[@"answer"] boolValue], @"Should have special values for event responses to indicate user has joined experiment");
    XCTAssertTrue([response[@"inputId"] isEqualToString:@"-1"], @"Should have special values for event responses to indicate user has joined experiment");
}

- (void)testEventSelfReportedCorrectlyGivenDefinitionAndInputs
{
    XCTAssertNotNil(pacoExperimentDefinition, @"Definition used for testing should not be nil");
    
    XCTAssertNotNil(pacoExperimentInputs, @"Inputs used for testing should not be nil");
    
    // Check event indicates its been self reported
    // NOTE: special values may change
    PacoEvent *testEvent = [PacoEvent selfReportEventForDefinition:pacoExperimentDefinition withInputs:pacoExperimentInputs];
    
    XCTAssertEqual(testEvent.who, [[PacoClient sharedInstance] userEmail], @"Should have special values to indicate event is self reported");
    XCTAssertNil(testEvent.scheduledTime, @"Should have special values to indicate event is self reported");
    XCTAssertNotNil(testEvent.responseTime, @"Should have special values to indicate event is self reported");
}

- (void)testSurveySubmittedCorrectlyGivenDefinitionAndInputsAndScheduledTime
{
    XCTAssertNotNil(pacoExperimentDefinition, @"Definition used for testing should not be nil");
    
    XCTAssertNotNil(pacoEventScheduledTime, @"Scheduled time used for testing should not be nil");
    
    // Check event indicates its been self reported
    // NOTE: special values may change
    PacoEvent *testEvent = [PacoEvent surveySubmittedEventForDefinition:pacoExperimentDefinition withInputs:pacoExperimentInputs andScheduledTime:pacoEventScheduledTime];
    
    XCTAssertNotNil(testEvent.responseTime, @"Should have special values to indicate survey is submitted");
    XCTAssertEqual(testEvent.scheduledTime, pacoEventScheduledTime, @"Should have special values to indicate survey is submitted");
}

- (void)testSurveyMissedCorrectlyGivenDefinitionAndScheduledTime
{
    XCTAssertNotNil(pacoExperimentDefinition, @"Definition used for testing should not be nil");
    
    XCTAssertNotNil(pacoEventScheduledTime, @"Scheduled time used for testing should not be nil");
    
    NSString *testEmail = @"test@email.com";
    
    // Check event indicates its been missed
    // NOTE: special values may change
    PacoEvent *testEvent = [PacoEvent surveyMissedEventForDefinition:pacoExperimentDefinition withScheduledTime:pacoEventScheduledTime userEmail:testEmail];
    
    XCTAssertTrue([testEvent.who isEqualToString:testEmail], @"Event details should be set");
    XCTAssertEqual(testEvent.experimentId, pacoExperimentDefinition.experimentId, @"Event details should be set");
    XCTAssertEqual(testEvent.experimentName, pacoExperimentDefinition.title, @"Event details should be set");
    XCTAssertEqual(testEvent.experimentVersion, pacoExperimentDefinition.experimentVersion, @"Event details should be set");
    XCTAssertNil(testEvent.responseTime, @"Event details should be set");
    XCTAssertEqualObjects(testEvent.scheduledTime, pacoEventScheduledTime, @"Event details should be set");
}

- (void)testEventWhoIsAssignedCorrectlyWhenNotProvidedForMissedSurvey
{
    XCTAssertNotNil(pacoExperimentDefinition, @"Definition used for testing should not be nil");
    
    PacoEvent *testEvent = [PacoEvent surveyMissedEventForDefinition:pacoExperimentDefinition withScheduledTime:pacoEventScheduledTime];
    
    XCTAssertNotNil(testEvent.who, @"Event 'who' should not be nil");
    XCTAssertTrue([testEvent.who isEqualToString:[[PacoClient sharedInstance] userEmail]], @"Event 'who' should default to user email");
}

@end