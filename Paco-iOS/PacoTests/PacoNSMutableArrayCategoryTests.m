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

#import <SenTestingKit/SenTestingKit.h>
#import "NSMutableArray+Paco.h"
#import "UILocalNotification+Paco.h"
#import "PacoDateUtility.h"

@interface PacoNSMutableArrayCategoryTests : SenTestCase

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

- (void)testSortDatesToSchedule {
  NSDate* date1 = [NSDate dateWithTimeIntervalSinceNow:-10];
  NSDate* date2 = [NSDate date];
  NSDate* date3 = [NSDate dateWithTimeInterval:10 sinceDate:date2];
  
  NSMutableArray* dates = [NSMutableArray arrayWithObjects:date3, date2, date1,nil];
  [dates pacoSortDatesToSchedule];
  NSArray* expect = @[date1, date2, date3];
  STAssertEqualObjects(dates,expect, @"dates should be sorted correctly");
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
  STAssertNotNil(noti1, @"notification should be valid");
  STAssertNotNil(noti2, @"notification should be valid");
  STAssertNotNil(noti3, @"notification should be valid");
  NSMutableArray* notifications = [NSMutableArray arrayWithObjects:noti3, noti2, noti1, nil];
  [notifications pacoSortLocalNotificationsByFireDate];
  NSArray* expect = @[noti1, noti2, noti3];
  STAssertEqualObjects(notifications, expect, @"notifications should be sorted by fire date");
}

@end
