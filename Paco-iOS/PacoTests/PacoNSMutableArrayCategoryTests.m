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
#import "NSMutableArray+Paco.h"
#import "UILocalNotification+Paco.h"
#import "PacoDateUtility.h"

@interface PacoNSMutableArrayCategoryTests : XCTestCase

@end

@implementation PacoNSMutableArrayCategoryTests

- (void)setUp {
    [super setUp];
    // Put setup code here; it will be run once, before the first test case.
}

- (void)tearDown {
    // Put teardown code here; it will be run once, after the last test case.
    [super tearDown];
}

- (void)testAddObjectsFromNilArray {
  NSArray* arrayToAdd = nil;
  NSMutableArray* testArr = [NSMutableArray arrayWithCapacity:0];
  [testArr addObjectsFromArray:arrayToAdd];
  XCTAssertEqual([testArr count], (NSUInteger)0, @"should have 0 elements");
  
  arrayToAdd = @[@1, @2];
  [testArr addObjectsFromArray:arrayToAdd];
  XCTAssertEqual([testArr count], (NSUInteger)2, @"should have 2 elements");
}

- (void)testSortDatesToSchedule {
  NSDate* date1 = [NSDate dateWithTimeIntervalSinceNow:-10];
  NSDate* date2 = [NSDate date];
  NSDate* date3 = [NSDate dateWithTimeInterval:10 sinceDate:date2];
  
  NSMutableArray* dates = [NSMutableArray arrayWithObjects:date3, date2, date1,nil];
  [dates pacoSortDatesToSchedule];
  NSArray* expect = @[date1, date2, date3];
  XCTAssertEqualObjects(dates,expect, @"dates should be sorted correctly");
}

- (void)testSortLocalNotificationsByFireDate {
  NSString* experimentId = @"12345";
  NSTimeInterval timeoutInterval = 400 * 60;//400 minutes
  NSDate* date1 = [NSDate dateWithTimeIntervalSinceNow:-10];
  NSDate* date2 = [NSDate date];
  NSDate* date3 = [NSDate dateWithTimeInterval:10 sinceDate:date2];
  
  NSDate* timeout1 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1];
  NSDate* timeout2 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date2];
  NSDate* timeout3 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date3];
  
  NSString* title = @"Experiment";
  NSString* alertBody1 = [NSString stringWithFormat:@"[%@]%@",
                          [PacoDateUtility stringForAlertBodyFromDate:date1],
                          title];
  NSString* alertBody2 = [NSString stringWithFormat:@"[%@]%@",
                          [PacoDateUtility stringForAlertBodyFromDate:date2],
                          title];
  NSString* alertBody3 = [NSString stringWithFormat:@"[%@]%@",
                          [PacoDateUtility stringForAlertBodyFromDate:date3],
                          title];
  UILocalNotification* noti1 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                           experimentTitle:alertBody1
                                                                            fireDate:date1
                                                                         timeOutDate:timeout1];
  UILocalNotification* noti2 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                           experimentTitle:alertBody2
                                                                            fireDate:date2
                                                                         timeOutDate:timeout2];
  UILocalNotification* noti3 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                           experimentTitle:alertBody3
                                                                            fireDate:date3
                                                                         timeOutDate:timeout3];
  XCTAssertNotNil(noti1, @"notification should be valid");
  XCTAssertNotNil(noti2, @"notification should be valid");
  XCTAssertNotNil(noti3, @"notification should be valid");
  NSMutableArray* notifications = [NSMutableArray arrayWithObjects:noti3, noti2, noti1, nil];
  [notifications pacoSortLocalNotificationsByFireDate];
  NSArray* expect = @[noti1, noti2, noti3];
  XCTAssertEqualObjects(notifications, expect, @"notifications should be sorted by fire date");
}


- (void)testSortAndRemoveDuplicatesWithOneNotifications {
  NSString* experimentId = @"12345";
  NSTimeInterval timeoutInterval = 400 * 60;//400 minutes
  NSDate* date1 = [NSDate dateWithTimeIntervalSinceNow:-10];
  NSDate* timeout1 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1];
  NSString* title = @"Experiment";
  NSString* alertBody1 = [NSString stringWithFormat:@"[%@]%@",
                          [PacoDateUtility stringForAlertBodyFromDate:date1],
                          title];
  UILocalNotification* noti1 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                     experimentTitle:alertBody1
                                                                            fireDate:date1
                                                                         timeOutDate:timeout1];
  XCTAssertNotNil(noti1, @"notification should be valid");
  NSMutableArray* notifications = [NSMutableArray arrayWithObjects:noti1, nil];
  NSMutableArray* result = [notifications pacoSortLocalNotificationsAndRemoveDuplicates];
  XCTAssertTrue([result isKindOfClass:[NSMutableArray class]], @"should be mutable array");
  XCTAssertTrue(notifications != result, @"should be different objects");
  XCTAssertEqualObjects(notifications, result, @"should have the same objects");
}

- (void)testSortAndRemoveDuplicatesForEmptyArray {
  NSMutableArray* notifications = [NSMutableArray arrayWithCapacity:10];
  NSMutableArray* result = [notifications pacoSortLocalNotificationsAndRemoveDuplicates];
  XCTAssertTrue([result isKindOfClass:[NSMutableArray class]], @"should be mutable array");
  XCTAssertEqual((int)[result count], 0, @"should be empty");
}


- (void)testSortAndRemoveDuplicatesWithTwoNonDuplicateNotifications {
  NSString* experimentId = @"12345";
  NSTimeInterval timeoutInterval = 400 * 60;//400 minutes
  NSDate* date1 = [NSDate dateWithTimeIntervalSinceNow:-10];
  NSDate* date2 = [NSDate date];
  
  NSDate* timeout1 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1];
  NSDate* timeout2 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date2];
  
  NSString* title = @"Experiment";
  NSString* alertBody1 = [NSString stringWithFormat:@"[%@]%@",
                          [PacoDateUtility stringForAlertBodyFromDate:date1],
                          title];
  NSString* alertBody2 = [NSString stringWithFormat:@"[%@]%@",
                          [PacoDateUtility stringForAlertBodyFromDate:date2],
                          title];
  UILocalNotification* noti1 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                     experimentTitle:alertBody1
                                                                            fireDate:date1
                                                                         timeOutDate:timeout1];
  UILocalNotification* noti2 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                     experimentTitle:alertBody2
                                                                            fireDate:date2
                                                                         timeOutDate:timeout2];
  XCTAssertNotNil(noti1, @"notification should be valid");
  XCTAssertNotNil(noti2, @"notification should be valid");
  NSMutableArray* notifications = [NSMutableArray arrayWithObjects:noti2, noti1, nil];
  NSMutableArray* result = [notifications pacoSortLocalNotificationsAndRemoveDuplicates];
  XCTAssertTrue([result isKindOfClass:[NSMutableArray class]], @"should be mutable array");
  NSArray* expect = @[noti1, noti2];
  XCTAssertEqualObjects(result, expect, @"notifications should be sorted by fire date");
}

- (void)testSortAndRemoveDuplicatesWithTwoDuplicateNotifications {
  NSString* experimentId = @"12345";
  NSTimeInterval timeoutInterval = 400 * 60;//400 minutes
  NSDate* date1 = [NSDate dateWithTimeIntervalSinceNow:-10];
  
  NSDate* timeout1 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1];
  
  NSString* title = @"Experiment";
  NSString* alertBody1 = [NSString stringWithFormat:@"[%@]%@",
                          [PacoDateUtility stringForAlertBodyFromDate:date1],
                          title];
  UILocalNotification* noti1 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                     experimentTitle:alertBody1
                                                                            fireDate:date1
                                                                         timeOutDate:timeout1];
  UILocalNotification* noti2 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                     experimentTitle:alertBody1
                                                                            fireDate:date1
                                                                         timeOutDate:timeout1];
  XCTAssertNotNil(noti1, @"notification should be valid");
  XCTAssertNotNil(noti2, @"notification should be valid");
  XCTAssertTrue(noti1 != noti2, @"should be two different objects");
  XCTAssertEqualObjects([noti1 pacoFireDate], [noti2 pacoFireDate], @"fireDate should be duplicate");
  NSMutableArray* notifications = [NSMutableArray arrayWithObjects:noti2, noti1, nil];
  NSMutableArray* result = [notifications pacoSortLocalNotificationsAndRemoveDuplicates];
  XCTAssertTrue([result isKindOfClass:[NSMutableArray class]], @"should be mutable array");
  XCTAssertEqual((int)[result count], 1, @"should contain only 1 object");
  UILocalNotification* noti = [result firstObject];
  XCTAssertTrue(noti == noti2, @"should only contain noti2");
}

//noti2 and noti3 have the same fire date
- (void)testSortAndRemoveDuplicatesWithMultipleDuplicateNotifications {
  NSString* experimentId = @"12345";
  NSTimeInterval timeoutInterval = 400 * 60;//400 minutes
  NSDate* date1 = [NSDate dateWithTimeIntervalSinceNow:-10];
  NSDate* date2 = [NSDate date];
  NSDate* date3 = date2;
  NSDate* date4 = [NSDate dateWithTimeInterval:10 sinceDate:date3];
  
  NSDate* timeout1 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1];
  NSDate* timeout2 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date2];
  NSDate* timeout3 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date3];
  NSDate* timeout4 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date4];
  
  NSString* title = @"Experiment";
  NSString* alertBody1 = [NSString stringWithFormat:@"[%@]%@",
                          [PacoDateUtility stringForAlertBodyFromDate:date1],
                          title];
  NSString* alertBody2 = [NSString stringWithFormat:@"[%@]%@",
                          [PacoDateUtility stringForAlertBodyFromDate:date2],
                          title];
  NSString* alertBody3 = [NSString stringWithFormat:@"[%@]%@",
                          [PacoDateUtility stringForAlertBodyFromDate:date3],
                          title];
  NSString* alertBody4 = [NSString stringWithFormat:@"[%@]%@",
                          [PacoDateUtility stringForAlertBodyFromDate:date4],
                          title];
  UILocalNotification* noti1 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                     experimentTitle:alertBody1
                                                                            fireDate:date1
                                                                         timeOutDate:timeout1];
  UILocalNotification* noti2 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                     experimentTitle:alertBody2
                                                                            fireDate:date2
                                                                         timeOutDate:timeout2];
  UILocalNotification* noti3 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                     experimentTitle:alertBody3
                                                                            fireDate:date3
                                                                         timeOutDate:timeout3];
  UILocalNotification* noti4 = [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                                                     experimentTitle:alertBody4
                                                                            fireDate:date4
                                                                         timeOutDate:timeout4];
  XCTAssertNotNil(noti1, @"notification should be valid");
  XCTAssertNotNil(noti2, @"notification should be valid");
  XCTAssertNotNil(noti3, @"notification should be valid");
  XCTAssertNotNil(noti4, @"notification should be valid");
  XCTAssertEqualObjects([noti2 pacoFireDate], [noti3 pacoFireDate], @"fireDate should be duplicate");

  NSMutableArray* notifications = [NSMutableArray arrayWithObjects:noti4, noti3, noti2, noti1, nil];
  NSMutableArray* result = [notifications pacoSortLocalNotificationsAndRemoveDuplicates];
  XCTAssertTrue([result isKindOfClass:[NSMutableArray class]], @"should be mutable array");
  NSArray* expect = @[noti1, noti2, noti4];
  XCTAssertEqualObjects(result, expect, @"notifications should be sorted by fire date");
}


- (void)testPacoIsNotEmpty {
  NSArray* arr = @[];
  XCTAssertFalse([arr pacoIsNotEmpty], @"should be empty");
  arr = @[@"hello"];
  XCTAssertTrue([arr pacoIsNotEmpty], @"should not be empty");
  arr = nil;
  XCTAssertFalse([arr pacoIsNotEmpty], @"nil should be empty");
  
  NSMutableArray* mutableArr = [NSMutableArray array];
  XCTAssertFalse([mutableArr pacoIsNotEmpty], @"should be empty");
  [mutableArr addObject:@"hello"];
  XCTAssertTrue([mutableArr pacoIsNotEmpty], @"should not be empty");
  [mutableArr removeAllObjects];
  XCTAssertFalse([mutableArr pacoIsNotEmpty], @"should be empty");
  mutableArr = nil;
  XCTAssertFalse([mutableArr pacoIsNotEmpty], @"nil should be empty");
}

- (void)testPacoDescriptionForDates {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:10];
  [comp setDay:16];
  [comp setHour:8];
  [comp setMinute:54];
  [comp setSecond:34];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  NSDate* date1 = [gregorian dateFromComponents:comp];
  [comp setMonth:10];
  [comp setDay:25];
  [comp setHour:22];
  [comp setMinute:10];
  [comp setSecond:00];
  NSDate* date2 = [gregorian dateFromComponents:comp];
  NSArray* arr = @[date1, date2];

  NSString* descript = [arr pacoDescriptionForDates];
  NSString* expect = @"(\n"
                     @"2013/10/16 08:54:34-0700\n"
                     @"2013/10/25 22:10:00-0700\n"
                     @")";
  XCTAssertEqualObjects(descript, expect, @"should be correct");
}

- (void)testGetFirstOrLastObjectFromEmptyArray {
  NSArray* testArr = nil;
  XCTAssertNil([testArr firstObject], @"should be nil");
  XCTAssertNil([testArr lastObject], @"should be nil");
  
  testArr = @[];
  XCTAssertNil([testArr firstObject], @"should be nil");
  XCTAssertNil([testArr lastObject], @"should be nil");
}


@end
