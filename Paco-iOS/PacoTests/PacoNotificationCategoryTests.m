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
#import "UILocalNotification+Paco.h"


@interface PacoNotificationInfo()
+ (PacoNotificationInfo*)pacoInfoWithDictionary:(NSDictionary*)infoDict;
+ (NSDictionary*)userInfoDictionaryWithExperimentId:(NSString*)experimentId
                                           fireDate:(NSDate*)fireDate
                                        timeOutDate:(NSDate*)timeOutDate;
- (PacoNotificationStatus)status;

@end


@interface PacoNotificationCategoryTests : SenTestCase
@property(nonatomic, strong) NSString* testID;
@property(nonatomic, strong) NSString* testAlertBody;
@property(nonatomic, strong) NSDate* testFireDate;
@property(nonatomic, strong) NSDate* testTimeoutDate;
@end

@implementation PacoNotificationCategoryTests

- (void)setUp {
  self.testID = @"12345";
  self.testAlertBody = @"Paco Notification One";
  self.testFireDate = [NSDate date];
  self.testTimeoutDate = [NSDate dateWithTimeInterval:10 sinceDate:self.testFireDate];
  
  [super setUp];
}

- (void)tearDown {
  self.testID = nil;
  self.testFireDate = nil;
  self.testTimeoutDate = nil;
  [super tearDown];
}

- (void)testCreatePacoInfo {
  NSDictionary* dict = @{@"experimentInstanceId":self.testID,
                         @"notificationFireDate":self.testFireDate,
                         @"notificationTimeoutDate":self.testTimeoutDate};
  
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:dict];
  STAssertTrue(info != nil, @"should have a valid info");
  STAssertEqualObjects(info.experimentId, @"12345", @"should have a valid experiment id");
  STAssertEqualObjects(info.fireDate, self.testFireDate, @"should have a valid fire date");
  STAssertEqualObjects(info.timeOutDate, self.testTimeoutDate, @"should have a valid timeout date");
}

- (void)testCreatePacoInfo1 {
  NSDictionary* dict = @{@"experimentInstanceId":self.testID};
  STAssertEqualObjects([PacoNotificationInfo pacoInfoWithDictionary:dict], nil,
                       @"missing information will get a nil result");
}

- (void)testCreatePacoInfo2 {
  NSDictionary* dict = @{@"notificationFireDate":self.testFireDate};
  STAssertEqualObjects([PacoNotificationInfo pacoInfoWithDictionary:dict], nil,
                       @"missing information will get a nil result");
}

- (void)testCreatePacoInfo3 {
  NSDictionary* dict = @{@"id":self.testID,
                         @"notificationFireDate":self.testFireDate,
                         @"notificationTimeoutDate":self.testTimeoutDate};
  STAssertEqualObjects([PacoNotificationInfo pacoInfoWithDictionary:dict], nil,
                       @"wrong dict key will get a nil result");
}

- (void)testCreateUserInfoDictionary {
  NSDictionary* dict = [PacoNotificationInfo userInfoDictionaryWithExperimentId:self.testID
                                                                       fireDate:self.testFireDate
                                                                    timeOutDate:self.testTimeoutDate];
  NSDictionary* expected = @{@"experimentInstanceId":self.testID,
                             @"notificationFireDate":self.testFireDate,
                             @"notificationTimeoutDate":self.testTimeoutDate};
  STAssertEqualObjects(dict, expected, @"should have a valid result");
}

- (void)testCreateUserInfoDictionary1 {
  NSDictionary* dict = [PacoNotificationInfo userInfoDictionaryWithExperimentId:@""
                                                                       fireDate:self.testFireDate
                                                                    timeOutDate:self.testTimeoutDate];
  STAssertEqualObjects(dict, nil, @"should have a nil result for an empty experiment id");
}

- (void)testCreateUserInfoDictionary2 {
  NSDictionary* dict = [PacoNotificationInfo userInfoDictionaryWithExperimentId:self.testID
                                                                       fireDate:nil
                                                                    timeOutDate:self.testTimeoutDate];
  STAssertEqualObjects(dict, nil, @"should have a nil result for a nil fireDate");
}

- (void)testCreateUserInfoDictionary3 {
  NSDictionary* dict = [PacoNotificationInfo userInfoDictionaryWithExperimentId:self.testID
                                                                       fireDate:self.testFireDate
                                                                    timeOutDate:nil];
  STAssertEqualObjects(dict, nil, @"should have a nil result for a nil timeOutDate");
}

- (void)testCreateUserInfoDictionary4 {
  self.testTimeoutDate = [NSDate dateWithTimeInterval:-1 sinceDate:self.testFireDate];
  NSDictionary* dict = [PacoNotificationInfo userInfoDictionaryWithExperimentId:self.testID
                                                                       fireDate:self.testFireDate
                                                                    timeOutDate:self.testTimeoutDate];
  STAssertEqualObjects(dict, nil, @"should have a nil result for an invalid timeOutDate");
}

- (void)testStatusNotFired {
  self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:1];//1 seconds after now
  self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:10*60]; //10 minutes after now
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  [info setValue:self.testID forKey:@"experimentId"];
  [info setValue:self.testFireDate forKey:@"fireDate"];
  [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
  STAssertEquals([info status], PacoNotificationStatusNotFired, @"should be not fired");
}

- (void)testStatusFiredNotTimeout {
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  [info setValue:self.testID forKey:@"experimentId"];
  [info setValue:[NSDate dateWithTimeIntervalSinceNow:0] forKey:@"fireDate"];
  [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
  STAssertEquals([info status], PacoNotificationStatusFiredNotTimeout, @"should be fired but not timeout");
}

- (void)testStatusFiredNotTimeout2 {
  self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:-2*60];//two minutes before now
  self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:1]; //one second after now
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  [info setValue:self.testID forKey:@"experimentId"];
  [info setValue:self.testFireDate forKey:@"fireDate"];
  [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
  STAssertEquals([info status], PacoNotificationStatusFiredNotTimeout, @"should be fired but not timeout");
}

- (void)testStatusFiredNotTimeout3 {
  self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:0];//right now
  self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:1]; //one second after now
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  [info setValue:self.testID forKey:@"experimentId"];
  [info setValue:self.testFireDate forKey:@"fireDate"];
  [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
  STAssertEquals([info status], PacoNotificationStatusFiredNotTimeout, @"should be fired but not time out");
}


- (void)testStatusTimeout {
  self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:-2*60];//two minutes before now
  self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:0]; //right now
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  [info setValue:self.testID forKey:@"experimentId"];
  [info setValue:self.testFireDate forKey:@"fireDate"];
  [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
  STAssertEquals([info status], PacoNotificationStatusTimeout, @"should be time out");
}


- (void)testCreateNotification {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                          alertBody:self.testAlertBody
                                                                           fireDate:self.testFireDate
                                                                        timeOutDate:self.testTimeoutDate];
  STAssertEqualObjects(noti.timeZone, [NSTimeZone systemTimeZone], @"should be system timezone");
  STAssertEqualObjects(noti.fireDate, self.testFireDate, @"firedate should be valid");
  STAssertEqualObjects(noti.alertBody, self.testAlertBody, @"alert body should be valid");
  STAssertEqualObjects(noti.soundName, @"deepbark_trial.mp3", @"sound name should be valid");
  NSDictionary* userInfo = @{@"experimentInstanceId":self.testID,
                             @"notificationFireDate":self.testFireDate,
                             @"notificationTimeoutDate":self.testTimeoutDate};
  STAssertEqualObjects(noti.userInfo, userInfo, @"userInfo should be valid");
}

- (void)testCreateNotification2 {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:@""
                                                                          alertBody:self.testAlertBody
                                                                           fireDate:self.testFireDate
                                                                        timeOutDate:self.testTimeoutDate];
  STAssertEqualObjects(noti, nil, @"noti should be invalid with an empty experiment id");
}

- (void)testCreateNotification3 {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                          alertBody:@""
                                                                           fireDate:self.testFireDate
                                                                        timeOutDate:self.testTimeoutDate];
  STAssertEqualObjects(noti, nil, @"noti should be invalid with an empty alert body");
}

- (void)testCreateNotification4 {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                          alertBody:self.testAlertBody
                                                                           fireDate:nil
                                                                        timeOutDate:self.testTimeoutDate];
  STAssertEqualObjects(noti, nil, @"noti should be invalid with a nil fireDate");
}

- (void)testCreateNotification5 {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                          alertBody:self.testAlertBody
                                                                           fireDate:self.testFireDate
                                                                        timeOutDate:nil];
  STAssertEqualObjects(noti, nil, @"noti should be invalid with a nil fireDate");
}

- (void)testCreateNotification6 {
  UILocalNotification* noti =
      [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                  alertBody:self.testAlertBody
                                                   fireDate:self.testFireDate
                                                timeOutDate:self.testFireDate];
  STAssertEqualObjects(noti, nil, @"noti should be invalid with an invalid timeout date");
}

- (void)testCreateNotification7 {
  self.testTimeoutDate = [NSDate dateWithTimeInterval:-1 sinceDate:self.testFireDate];
  UILocalNotification* noti =
      [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                  alertBody:self.testAlertBody
                                                   fireDate:self.testFireDate
                                                timeOutDate:self.testTimeoutDate];
  STAssertEqualObjects(noti, nil, @"noti should be invalid with an invalid timeout date");
}

- (void)testPacoStatus {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:@""
                                                                          alertBody:self.testAlertBody
                                                                           fireDate:self.testFireDate
                                                                        timeOutDate:self.testTimeoutDate];
  STAssertEqualObjects(noti, nil, @"noti should be nil if experiment id is not valid");
  STAssertEquals([noti pacoStatus], PacoNotificationStatusUnknown, @"a nil notification should be unknown status");
}



@end
