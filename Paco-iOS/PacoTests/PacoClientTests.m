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

#import <XCTest/XCTest.h>
#import "PacoClient.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"
#import "PacoModel.h"
#import "PacoAuthenticator.h"
#import "UILocalNotification+Paco.h"
#import "PacoDateUtility.h"
#import "PacoEvent.h"

//id: 10948007
//title: Notification - ESM Test
static NSString* esmDefinitionJson = @"{\"title\":\"Notification - ESM Test\",\"description\":\"te\",\"informedConsentForm\":\"test\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":false,\"id\":10948007,\"questionsChange\":false,\"modifyDate\":\"2013/09/05\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"hello\",\"mandatory\":false,\"responseType\":\"likert\",\"likertSteps\":5,\"leftSideLabel\":\"q\",\"rightSideLabel\":\"f\",\"name\":\"hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":9001,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":10,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":50,\"minimumBuffer\":5,\"id\":1,\"scheduleType\":4,\"esmFrequency\":10,\"esmPeriodInDays\":0,\"esmStartHour\":57600000,\"esmEndHour\":61200000,\"times\":[46800000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":50,\"minimumBuffer\":5,\"id\":1,\"scheduleType\":4,\"esmFrequency\":10,\"esmPeriodInDays\":0,\"esmStartHour\":57600000,\"esmEndHour\":61200000,\"times\":[46800000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";


@interface PacoClient ()
- (NSArray*)eventsFromExpiredNotifications:(NSArray*)expiredNotifications;
@end

@interface PacoClientTests : XCTestCase

@property(nonatomic, strong) PacoClient* client;

@end

@implementation PacoClientTests

- (void)setUp {
  [super setUp];
  // Put setup code here; it will be run once, before the first test case.
  self.client = [[PacoClient alloc] init];
  [self.client.authenticator setValue:@"testuser@gmail.com" forKey:@"accountEmail"];
  
  NSError* error = nil;
  NSData* data = [esmDefinitionJson dataUsingEncoding:NSUTF8StringEncoding];
  id definitionDict = [NSJSONSerialization JSONObjectWithData:data
                                                      options:NSJSONReadingAllowFragments
                                                        error:&error];
  XCTAssertTrue(error == nil && [definitionDict isKindOfClass:[NSDictionary class]],
               @"esmExperimentTemplate should be successfully serialized!");
  PacoExperimentDefinition* definition = [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:definitionDict];
  XCTAssertNotNil(definition, @"definition should not be nil!");
  
  PacoExperiment* experimentInstance = [[PacoExperiment alloc] init];
  experimentInstance.schedule = definition.schedule;
  experimentInstance.definition = definition;
  experimentInstance.instanceId = definition.experimentId;
  XCTAssertNotNil(experimentInstance, @"experimentInstance should not be nil!");
  
  NSMutableArray* definitionList = [NSMutableArray arrayWithObject:definition];
  NSMutableArray* experimentList = [NSMutableArray arrayWithObject:experimentInstance];
  [self.client.model setValue:definitionList forKey:@"myDefinitions"];
  [self.client.model setValue:experimentList forKey:@"runningExperiments"];
}

- (void)tearDown {
  // Put teardown code here; it will be run once, after the last test case.
  self.client = nil;
  [super tearDown];
}

- (void)testOSVersion
{
  NSString* version = @"7.0.0";
  float floatVersion = [version floatValue];
  XCTAssertEqual(floatVersion, (float)7.0, @"version number should be correct.");
  XCTAssertTrue(floatVersion >= 7.0, @"version number should be correct.");

  version = @"7.0.1";
  floatVersion = [version floatValue];
  XCTAssertEqual(floatVersion, (float)7.0, @"version number should be correct.");

  version = @"7.2.1";
  floatVersion = [version floatValue];
  XCTAssertEqual(floatVersion, (float)7.2, @"version number should be correct.");
  XCTAssertTrue(floatVersion >= 7.0, @"version number should be correct.");

  version = @"7.2.0";
  floatVersion = [version floatValue];
  XCTAssertEqual(floatVersion, (float)7.2, @"version number should be correct.");
  
  version = @"6.2.3";
  floatVersion = [version floatValue];
  XCTAssertEqual(floatVersion, (float)6.2, @"version number should be correct.");
  XCTAssertFalse(floatVersion >= 7.0, @"version number should be correct.");
  
  version = @"6.0.0";
  floatVersion = [version floatValue];
  XCTAssertEqual(floatVersion, (float)6.0, @"version number should be correct.");
  XCTAssertFalse(floatVersion >= 7.0, @"version number should be correct.");
  
  version = @"6.3.3.6";
  floatVersion = [version floatValue];
  XCTAssertEqual(floatVersion, (float)6.3, @"version number should be correct.");
}

//id: 10948007
//title: Notification - ESM Test
- (void)testEventsFromExpiredNotifications {
  NSString* experimentId = @"10948007";
  NSString* experimentTitle = @"Notification - ESM Test";
  NSTimeInterval timeoutInterval = 20;//20 seconds
  
  NSDate* now = [NSDate date];
  NSDate* date1 = [NSDate dateWithTimeInterval:-25 sinceDate:now]; //timeout
  NSDate* date2 = [NSDate dateWithTimeInterval:-15 sinceDate:now]; //obsolete
  
  UILocalNotification* timeoutNoti =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                        experimentTitle:experimentTitle
                                               fireDate:date1
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1]];
  
  UILocalNotification* obsoleteNoti =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                        experimentTitle:experimentTitle
                                               fireDate:date2
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date2]];
  NSArray* allNotifications = @[timeoutNoti, obsoleteNoti];
  NSArray* events = [self.client eventsFromExpiredNotifications:allNotifications];
  XCTAssertEqual([events count], (NSUInteger)2, @"should have 2 events");
  PacoEvent* first = [events firstObject];
  XCTAssertEqualObjects(first.who, @"testuser@gmail.com", @"should be correct");
  XCTAssertEqualObjects(first.experimentId, @"10948007", @"should be correct");
  XCTAssertEqualObjects(first.experimentName, @"Notification - ESM Test", @"should be correct");
  XCTAssertEqual(first.experimentVersion, 10, @"should be correct");
  XCTAssertNil(first.responseTime, @"should be nil");
  XCTAssertEqualObjects(first.scheduledTime, date1, @"should be correct");
  
  PacoEvent* last = [events lastObject];
  XCTAssertEqualObjects(last.who, @"testuser@gmail.com", @"should be correct");
  XCTAssertEqualObjects(last.experimentId, @"10948007", @"should be correct");
  XCTAssertEqualObjects(last.experimentName, @"Notification - ESM Test", @"should be correct");
  XCTAssertEqual(last.experimentVersion, 10, @"should be correct");
  XCTAssertNil(last.responseTime, @"should be nil");
  XCTAssertEqualObjects(last.scheduledTime, date2, @"should be correct");
}


@end
