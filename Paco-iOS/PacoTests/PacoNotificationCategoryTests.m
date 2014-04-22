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
#import "UILocalNotification+Paco.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperimentSchedule.h"
#import "PacoDateUtility.h"

//Ongoing, Fixed interval, Daily, Repeat every 1 day, timeout 479 minutes
//9:30 am, 12:50 pm, 6:11 pm
static NSString* DEFINITION_JSON = @"{\"title\":\"NotificationTest-FixInterval-2\",\"description\":\"test\",\"informedConsentForm\":\"test\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":false,\"id\":10451001,\"questionsChange\":false,\"modifyDate\":\"2013/08/23\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"hello\",\"mandatory\":false,\"responseType\":\"likert\",\"likertSteps\":5,\"leftSideLabel\":\"left\",\"rightSideLabel\":\"right\",\"name\":\"hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":20001,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":22,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[34200000,46200000,65460000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[34200000,46200000,65460000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";


@interface PacoNotificationInfo()
+ (PacoNotificationInfo*)pacoInfoWithDictionary:(NSDictionary*)infoDict;
+ (NSDictionary*)userInfoDictionaryWithExperimentId:(NSString*)experimentId
                                           fireDate:(NSDate*)fireDate
                                        timeOutDate:(NSDate*)timeOutDate;
- (PacoNotificationStatus)status;

@end


@interface PacoNotificationCategoryTests : XCTestCase
@property(nonatomic, strong) NSString* testID;
@property(nonatomic, strong) NSString* testTitle;
@property(nonatomic, strong) NSDate* testFireDate;
@property(nonatomic, strong) NSDate* testTimeoutDate;

@property(nonatomic, strong) PacoExperiment* testExperiment;
@property(nonatomic, strong) NSArray* testDatesToSchedule;
@end

@implementation PacoNotificationCategoryTests

- (void)setUp {
  [super setUp];

  self.testID = @"12345";
  self.testTitle = @"Paco Notification One";
  self.testFireDate = [NSDate date];
  self.testTimeoutDate = [NSDate dateWithTimeInterval:10 sinceDate:self.testFireDate];
  
  NSError* error = nil;
  NSData* data = [DEFINITION_JSON dataUsingEncoding:NSUTF8StringEncoding];
  id definitionDict = [NSJSONSerialization JSONObjectWithData:data
                                                      options:NSJSONReadingAllowFragments
                                                        error:&error];
  XCTAssertTrue(error == nil && [definitionDict isKindOfClass:[NSDictionary class]],
               @"DEFINITION_JSON should be successfully serialized!");
  PacoExperimentDefinition* definition = [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:definitionDict];
  XCTAssertTrue(definition != nil, @"definition should not be nil!");
  PacoExperiment* experimentInstance = [[PacoExperiment alloc] init];
  experimentInstance.schedule = definition.schedule;
  experimentInstance.definition = definition;
  experimentInstance.instanceId = definition.experimentId;
  XCTAssertNotNil(experimentInstance, @"experimentInstance should be valid!");
  self.testExperiment = experimentInstance;
  
  NSDate* date1 = [NSDate dateWithTimeIntervalSinceNow:10];
  NSDate* date2 = [NSDate dateWithTimeInterval:10 sinceDate:date1];
  self.testDatesToSchedule = @[date1, date2];
}

- (void)tearDown {
  self.testID = nil;
  self.testFireDate = nil;
  self.testTimeoutDate = nil;
  self.testExperiment = nil;
  self.testDatesToSchedule = nil;
  [super tearDown];
}

- (void)testCreatePacoInfo {
  NSDictionary* dict = @{@"experimentInstanceId":self.testID,
                         @"notificationFireDate":self.testFireDate,
                         @"notificationTimeoutDate":self.testTimeoutDate};
  
  PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:dict];
  XCTAssertTrue(info != nil, @"should have a valid info");
  XCTAssertEqualObjects(info.experimentId, @"12345", @"should have a valid experiment id");
  XCTAssertEqualObjects(info.fireDate, self.testFireDate, @"should have a valid fire date");
  XCTAssertEqualObjects(info.timeOutDate, self.testTimeoutDate, @"should have a valid timeout date");
}

- (void)testCreatePacoInfo1 {
  NSDictionary* dict = @{@"experimentInstanceId":self.testID};
  XCTAssertEqualObjects([PacoNotificationInfo pacoInfoWithDictionary:dict], nil,
                       @"missing information will get a nil result");
}

- (void)testCreatePacoInfo2 {
  NSDictionary* dict = @{@"notificationFireDate":self.testFireDate};
  XCTAssertEqualObjects([PacoNotificationInfo pacoInfoWithDictionary:dict], nil,
                       @"missing information will get a nil result");
}

- (void)testCreatePacoInfo3 {
  NSDictionary* dict = @{@"id":self.testID,
                         @"notificationFireDate":self.testFireDate,
                         @"notificationTimeoutDate":self.testTimeoutDate};
  XCTAssertEqualObjects([PacoNotificationInfo pacoInfoWithDictionary:dict], nil,
                       @"wrong dict key will get a nil result");
}

- (void)testCreateUserInfoDictionary {
  NSDictionary* dict = [PacoNotificationInfo userInfoDictionaryWithExperimentId:self.testID
                                                                       fireDate:self.testFireDate
                                                                    timeOutDate:self.testTimeoutDate];
  NSDictionary* expected = @{@"experimentInstanceId":self.testID,
                             @"notificationFireDate":self.testFireDate,
                             @"notificationTimeoutDate":self.testTimeoutDate};
  XCTAssertEqualObjects(dict, expected, @"should have a valid result");
}

- (void)testCreateUserInfoDictionary1 {
  NSDictionary* dict = [PacoNotificationInfo userInfoDictionaryWithExperimentId:@""
                                                                       fireDate:self.testFireDate
                                                                    timeOutDate:self.testTimeoutDate];
  XCTAssertEqualObjects(dict, nil, @"should have a nil result for an empty experiment id");
}

- (void)testCreateUserInfoDictionary2 {
  NSDictionary* dict = [PacoNotificationInfo userInfoDictionaryWithExperimentId:self.testID
                                                                       fireDate:nil
                                                                    timeOutDate:self.testTimeoutDate];
  XCTAssertEqualObjects(dict, nil, @"should have a nil result for a nil fireDate");
}

- (void)testCreateUserInfoDictionary3 {
  NSDictionary* dict = [PacoNotificationInfo userInfoDictionaryWithExperimentId:self.testID
                                                                       fireDate:self.testFireDate
                                                                    timeOutDate:nil];
  XCTAssertEqualObjects(dict, nil, @"should have a nil result for a nil timeOutDate");
}

- (void)testCreateUserInfoDictionary4 {
  self.testTimeoutDate = [NSDate dateWithTimeInterval:-1 sinceDate:self.testFireDate];
  NSDictionary* dict = [PacoNotificationInfo userInfoDictionaryWithExperimentId:self.testID
                                                                       fireDate:self.testFireDate
                                                                    timeOutDate:self.testTimeoutDate];
  XCTAssertEqualObjects(dict, nil, @"should have a nil result for an invalid timeOutDate");
}

- (void)testStatusNotFired {
  self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:1];//1 seconds after now
  self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:10*60]; //10 minutes after now
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  [info setValue:self.testID forKey:@"experimentId"];
  [info setValue:self.testFireDate forKey:@"fireDate"];
  [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
  XCTAssertEqual([info status], PacoNotificationStatusNotFired, @"should be not fired");
}

- (void)testStatusFiredNotTimeout {
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  [info setValue:self.testID forKey:@"experimentId"];
  [info setValue:[NSDate dateWithTimeIntervalSinceNow:0] forKey:@"fireDate"];
  [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
  XCTAssertEqual([info status], PacoNotificationStatusFiredNotTimeout, @"should be fired but not timeout");
}

- (void)testStatusFiredNotTimeout2 {
  self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:-2*60];//two minutes before now
  self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:1]; //one second after now
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  [info setValue:self.testID forKey:@"experimentId"];
  [info setValue:self.testFireDate forKey:@"fireDate"];
  [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
  XCTAssertEqual([info status], PacoNotificationStatusFiredNotTimeout, @"should be fired but not timeout");
}

- (void)testStatusFiredNotTimeout3 {
  self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:0];//right now
  self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:1]; //one second after now
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  [info setValue:self.testID forKey:@"experimentId"];
  [info setValue:self.testFireDate forKey:@"fireDate"];
  [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
  XCTAssertEqual([info status], PacoNotificationStatusFiredNotTimeout, @"should be fired but not time out");
}


- (void)testStatusTimeout {
  self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:-2*60];//two minutes before now
  self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:0]; //right now
  PacoNotificationInfo* info = [[PacoNotificationInfo alloc] init];
  [info setValue:self.testID forKey:@"experimentId"];
  [info setValue:self.testFireDate forKey:@"fireDate"];
  [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
  XCTAssertEqual([info status], PacoNotificationStatusTimeout, @"should be time out");
}


- (void)testCreateNotification {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                    experimentTitle:self.testTitle
                                                                           fireDate:self.testFireDate
                                                                        timeOutDate:self.testTimeoutDate];
  XCTAssertEqualObjects(noti.timeZone, [NSTimeZone systemTimeZone], @"should be system timezone");
  XCTAssertEqualObjects(noti.fireDate, self.testFireDate, @"firedate should be valid");
  NSString* expectAlertBody = [NSString stringWithFormat:@"[%@]%@",
                               [PacoDateUtility stringForAlertBodyFromDate:self.testFireDate],
                               self.testTitle];
  XCTAssertEqualObjects(noti.alertBody, expectAlertBody, @"alert body should be valid");
  XCTAssertEqualObjects(noti.soundName, @"deepbark_trial.mp3", @"sound name should be valid");
  NSDictionary* userInfo = @{@"experimentInstanceId":self.testID,
                             @"notificationFireDate":self.testFireDate,
                             @"notificationTimeoutDate":self.testTimeoutDate};
  XCTAssertEqualObjects(noti.userInfo, userInfo, @"userInfo should be valid");
}

- (void)testCreateNotification2 {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:@""
                                                                    experimentTitle:self.testTitle
                                                                           fireDate:self.testFireDate
                                                                        timeOutDate:self.testTimeoutDate];
  XCTAssertEqualObjects(noti, nil, @"noti should be invalid with an empty experiment id");
}

- (void)testCreateNotification3 {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                    experimentTitle:@""
                                                                           fireDate:self.testFireDate
                                                                        timeOutDate:self.testTimeoutDate];
  XCTAssertEqualObjects(noti, nil, @"noti should be invalid with an empty alert body");
}

- (void)testCreateNotification4 {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                    experimentTitle:self.testTitle
                                                                           fireDate:nil
                                                                        timeOutDate:self.testTimeoutDate];
  XCTAssertEqualObjects(noti, nil, @"noti should be invalid with a nil fireDate");
}

- (void)testCreateNotification5 {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                    experimentTitle:self.testTitle
                                                                           fireDate:self.testFireDate
                                                                        timeOutDate:nil];
  XCTAssertEqualObjects(noti, nil, @"noti should be invalid with a nil fireDate");
}

- (void)testCreateNotification6 {
  UILocalNotification* noti =
      [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                            experimentTitle:self.testTitle
                                                   fireDate:self.testFireDate
                                                timeOutDate:self.testFireDate];
  XCTAssertEqualObjects(noti, nil, @"noti should be invalid with an invalid timeout date");
}

- (void)testCreateNotification7 {
  self.testTimeoutDate = [NSDate dateWithTimeInterval:-1 sinceDate:self.testFireDate];
  UILocalNotification* noti =
      [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                            experimentTitle:self.testTitle
                                                   fireDate:self.testFireDate
                                                timeOutDate:self.testTimeoutDate];
  XCTAssertEqualObjects(noti, nil, @"noti should be invalid with an invalid timeout date");
}

- (void)testNotificationDescription {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                    experimentTitle:self.testTitle
                                                                           fireDate:self.testFireDate
                                                                        timeOutDate:self.testTimeoutDate];
  NSString* description = [noti pacoDescription];
  NSString* fireDateStr = [NSString stringWithFormat:@"notificationFireDate:%@; ",
                           [PacoDateUtility pacoStringForDate:self.testFireDate]];
  NSString* timeoutDateStr = [NSString stringWithFormat:@"notificationTimeoutDate:%@; }",
                           [PacoDateUtility pacoStringForDate:self.testTimeoutDate]];
  NSString* expect = [NSString stringWithFormat:@"<%@,%p: fireDate=%@, status=(%@), userInfo=%@%@%@>",
                      NSStringFromClass([noti class]),
                      noti,
                      [PacoDateUtility pacoStringForDate:self.testFireDate],
                      @"Fired, Not Timeout",
                      @"{experimentInstanceId:12345; ",
                      fireDateStr,
                      timeoutDateStr
                      ];
  XCTAssertEqualObjects(description, expect, @"description should be correct");
}

- (void)testPacoStatus {
  UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:@""
                                                                    experimentTitle:self.testTitle
                                                                           fireDate:self.testFireDate
                                                                        timeOutDate:self.testTimeoutDate];
  XCTAssertEqualObjects(noti, nil, @"noti should be nil if experiment id is not valid");
  XCTAssertEqual([noti pacoStatus], PacoNotificationStatusUnknown, @"a nil notification should be unknown status");
}


- (void)testPacoNotificationsForExperiment {
  NSArray* notifications = [UILocalNotification pacoNotificationsForExperiment:self.testExperiment
                                                               datesToSchedule:self.testDatesToSchedule];
  XCTAssertEqual(self.testExperiment.schedule.timeout, 479, @"timeout should be 479 minutes");
  NSTimeInterval timeoutInterval = 479 * 60;
  
  NSMutableArray* expect = [NSMutableArray array];
  for (NSDate* date in self.testDatesToSchedule) {
    NSDate* timeOutDate = [date dateByAddingTimeInterval:timeoutInterval];
    NSString* alertBody = [NSString stringWithFormat:@"[%@]%@",
                           [PacoDateUtility stringForAlertBodyFromDate:date],
                           self.testExperiment.definition.title];
    
    NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
    userInfo[kUserInfoKeyExperimentId] = self.testExperiment.instanceId;
    userInfo[kUserInfoKeyNotificationFireDate] = date;
    userInfo[kUserInfoKeyNotificationTimeoutDate] = timeOutDate;

    UILocalNotification *notification = [[UILocalNotification alloc] init];
    notification.timeZone = [NSTimeZone systemTimeZone];
    notification.fireDate = date;
    notification.alertBody = alertBody;
    notification.soundName = kNotificationSoundName;
    notification.userInfo = userInfo;
    [expect addObject:notification];
  }
  XCTAssertEqualObjects(notifications, expect, @"");
}


- (void)testPacoNotificationsForNilExperiment {
  NSArray* notifications = [UILocalNotification pacoNotificationsForExperiment:nil
                                                               datesToSchedule:self.testDatesToSchedule];
  XCTAssertNil(notifications, @"should return nil if experiment is nil");
}

- (void)testPacoNotificationsForEmptyDates {
  NSArray* notifications = [UILocalNotification pacoNotificationsForExperiment:self.testExperiment
                                                               datesToSchedule:@[]];
  XCTAssertNil(notifications, @"should return nil if dates is empty");
}


- (void)testPacoProcessNotificationsWithNilBlock {
  UILocalNotification* testNoti = [[UILocalNotification alloc] init];
  [UILocalNotification pacoProcessNotifications:@[testNoti] withBlock:nil];
}

- (void)testPacoProcessNotificationsWithEmptyNotifications {
  NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                     NSArray* expiredNotifications,
                                     NSArray* notFiredNotifications) {
    XCTAssertNil(activeNotification, @"should be nil");
    XCTAssertNil(expiredNotifications, @"should be nil");
    XCTAssertNil(notFiredNotifications, @"should be nil");
  };
  [UILocalNotification pacoProcessNotifications:nil withBlock:block];
}

- (void)testPacoProcessNotificationsTimeoutAndActive {
  NSString* experimentId = @"1";
  NSString* experimentTitle = @"title";
  NSTimeInterval timeoutInterval = 5;//5 seconds
  
  NSDate* now = [NSDate date];
  NSDate* date1 = [NSDate dateWithTimeInterval:-20 sinceDate:now]; //timeout
  NSDate* date2 = [NSDate dateWithTimeInterval:-3 sinceDate:now]; //active

  UILocalNotification* timeoutNoti =
      [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                            experimentTitle:experimentTitle
                                                   fireDate:date1
                                                timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1]];

  UILocalNotification* activeNoti =
      [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                            experimentTitle:experimentTitle
                                                   fireDate:date2
                                                timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date2]];
  
  NSArray* allNotifications = @[timeoutNoti, activeNoti];
  NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                     NSArray* expiredNotifications,
                                     NSArray* notFiredNotifications) {
    XCTAssertEqualObjects(activeNotification, activeNoti, @"should have an active notification");
    XCTAssertEqualObjects(expiredNotifications, @[timeoutNoti], @"should have one expired notification");
    XCTAssertNil(notFiredNotifications, @"should be nil");
  };
  [UILocalNotification pacoProcessNotifications:allNotifications withBlock:block];
}

- (void)testPacoProcessNotificationsTimeoutObsoleteAndActive {
  NSString* experimentId = @"1";
  NSString* experimentTitle = @"title";
  NSTimeInterval timeoutInterval = 20;//20 seconds
  
  NSDate* now = [NSDate date];
  NSDate* date1 = [NSDate dateWithTimeInterval:-25 sinceDate:now]; //timeout
  NSDate* date2 = [NSDate dateWithTimeInterval:-15 sinceDate:now]; //obsolete
  NSDate* date3 = [NSDate dateWithTimeInterval:-5 sinceDate:now]; //active
  
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

  
  UILocalNotification* activeNoti =
      [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                            experimentTitle:experimentTitle
                                                   fireDate:date3
                                                timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date3]];
  
  NSArray* allNotifications = @[timeoutNoti, obsoleteNoti, activeNoti];
  NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                     NSArray* expiredNotifications,
                                     NSArray* notFiredNotifications) {
    XCTAssertEqualObjects(activeNotification, activeNoti, @"should have one active notification");
    NSArray* expiredNotis = @[timeoutNoti, obsoleteNoti];
    XCTAssertEqualObjects(expiredNotifications, expiredNotis, @"should have two expired notifications");
    XCTAssertNil(notFiredNotifications, @"should be nil");
  };
  [UILocalNotification pacoProcessNotifications:allNotifications withBlock:block];
}

- (void)testPacoProcessNotificationsExpiredActiveAndScheduled {
  NSString* experimentId = @"1";
  NSString* experimentTitle = @"title";
  NSTimeInterval timeoutInterval = 20;//20 seconds
  
  NSDate* now = [NSDate date];
  NSDate* date1 = [NSDate dateWithTimeInterval:-25 sinceDate:now]; //timeout
  NSDate* date2 = [NSDate dateWithTimeInterval:-15 sinceDate:now]; //obsolete
  NSDate* date3 = [NSDate dateWithTimeInterval:-5 sinceDate:now]; //active
  NSDate* date4 = [NSDate dateWithTimeInterval:5 sinceDate:now]; //scheduled 1
  NSDate* date5 = [NSDate dateWithTimeInterval:10 sinceDate:now]; //scheduled 2
  
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
  
  
  UILocalNotification* activeNoti =
      [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                            experimentTitle:experimentTitle
                                                   fireDate:date3
                                                timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date3]];

  UILocalNotification* scheduledNoti1 =
      [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                            experimentTitle:experimentTitle
                                                   fireDate:date4
                                                timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date4]];

  UILocalNotification* scheduledNoti2 =
      [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                            experimentTitle:experimentTitle
                                                   fireDate:date5
                                                timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date5]];

  NSArray* allNotifications = @[timeoutNoti, obsoleteNoti, activeNoti, scheduledNoti1, scheduledNoti2];
  NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                     NSArray* expiredNotifications,
                                     NSArray* notFiredNotifications) {
    XCTAssertEqualObjects(activeNotification, activeNoti, @"should have one active notification");
    NSArray* expiredNotis = @[timeoutNoti, obsoleteNoti];
    XCTAssertEqualObjects(expiredNotifications, expiredNotis, @"should have two expired notifications");
    NSArray* scheduledNotis = @[scheduledNoti1, scheduledNoti2];
    XCTAssertEqualObjects(notFiredNotifications, scheduledNotis, @"should have two scheduled notifications");
  };
  [UILocalNotification pacoProcessNotifications:allNotifications withBlock:block];
}

- (void)testPacoProcessNotificationsTimeoutAndScheduled {
  NSString* experimentId = @"1";
  NSString* experimentTitle = @"title";
  NSTimeInterval timeoutInterval = 20;//20 seconds
  
  NSDate* now = [NSDate date];
  NSDate* date1 = [NSDate dateWithTimeInterval:-25 sinceDate:now]; //timeout
  NSDate* date2 = [NSDate dateWithTimeInterval:-22 sinceDate:now]; //timeout
  NSDate* date3 = [NSDate dateWithTimeInterval:10 sinceDate:now]; //scheduled
  NSDate* date4 = [NSDate dateWithTimeInterval:20 sinceDate:now]; //scheduled
  
  UILocalNotification* timeoutNoti1 =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                        experimentTitle:experimentTitle
                                               fireDate:date1
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1]];
  
  UILocalNotification* timeoutNoti2 =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                        experimentTitle:experimentTitle
                                               fireDate:date2
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date2]];
  
  
  UILocalNotification* scheduledNoti1 =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                        experimentTitle:experimentTitle
                                               fireDate:date3
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date3]];
  
  UILocalNotification* scheduledNoti2 =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                        experimentTitle:experimentTitle
                                               fireDate:date4
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date4]];
  
  
  NSArray* allNotifications = @[timeoutNoti1, timeoutNoti2,scheduledNoti1, scheduledNoti2];
  NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                     NSArray* expiredNotifications,
                                     NSArray* notFiredNotifications) {
    
    XCTAssertNil(activeNotification, @"should be nil");
    NSArray* expiredNotis = @[timeoutNoti1, timeoutNoti2];
    XCTAssertEqualObjects(expiredNotifications, expiredNotis, @"should have two expired notifications");
    NSArray* scheduledNotis = @[scheduledNoti1, scheduledNoti2];
    XCTAssertEqualObjects(notFiredNotifications, scheduledNotis, @"should have two scheduled notifications");
  };
  [UILocalNotification pacoProcessNotifications:allNotifications withBlock:block];

}

- (void)testPacoProcessNotificationsOnlyTimeOut {
  NSString* experimentId = @"1";
  NSString* experimentTitle = @"title";
  NSTimeInterval timeoutInterval = 20;//20 seconds
  
  NSDate* now = [NSDate date];
  NSDate* date1 = [NSDate dateWithTimeInterval:-25 sinceDate:now]; //timeout
  NSDate* date2 = [NSDate dateWithTimeInterval:-22 sinceDate:now]; //timeout
  
  NSString* alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date1], experimentTitle];
  UILocalNotification* timeoutNoti1 =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                              experimentTitle:alertBody
                                               fireDate:date1
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1]];
  
  alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date2], experimentTitle];
  UILocalNotification* timeoutNoti2 =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                              experimentTitle:alertBody
                                               fireDate:date2
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date2]];
  
  NSArray* allNotifications = @[timeoutNoti1, timeoutNoti2];
  NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                     NSArray* expiredNotifications,
                                     NSArray* notFiredNotifications) {
    XCTAssertNil(activeNotification, @"should be nil");
    NSArray* expiredNotis = @[timeoutNoti1, timeoutNoti2];
    XCTAssertEqualObjects(expiredNotifications, expiredNotis, @"should have two expired notifications");
    XCTAssertNil(notFiredNotifications, @"should be nil");
  };
  [UILocalNotification pacoProcessNotifications:allNotifications withBlock:block];

}

- (void)testPacoProcessNotificationActiveAndScheduled {
  NSString* experimentId = @"1";
  NSString* experimentTitle = @"title";
  NSTimeInterval timeoutInterval = 20;//20 seconds
  
  NSDate* now = [NSDate date];
  NSDate* date3 = [NSDate dateWithTimeInterval:-5 sinceDate:now]; //active
  NSDate* date4 = [NSDate dateWithTimeInterval:5 sinceDate:now]; //scheduled 1
  NSDate* date5 = [NSDate dateWithTimeInterval:10 sinceDate:now]; //scheduled 2
  
  NSString* alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date3], experimentTitle];
  UILocalNotification* activeNoti =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                              experimentTitle:alertBody
                                               fireDate:date3
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date3]];
  
  alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date4], experimentTitle];
  UILocalNotification* scheduledNoti1 =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                              experimentTitle:alertBody
                                               fireDate:date4
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date4]];
  
  alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date5], experimentTitle];
  UILocalNotification* scheduledNoti2 =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                              experimentTitle:alertBody
                                               fireDate:date5
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date5]];
  
  NSArray* allNotifications = @[activeNoti, scheduledNoti1, scheduledNoti2];
  NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                     NSArray* expiredNotifications,
                                     NSArray* notFiredNotifications) {
    XCTAssertEqualObjects(activeNotification, activeNoti, @"should have one active notification");
    XCTAssertNil(expiredNotifications, @"should be nil");
    NSArray* scheduledNotis = @[scheduledNoti1, scheduledNoti2];
    XCTAssertEqualObjects(notFiredNotifications, scheduledNotis, @"should have two scheduled notifications");
  };
  [UILocalNotification pacoProcessNotifications:allNotifications withBlock:block];
}

- (void)testPacoProcessNotificationOnlyScheduled {
  NSString* experimentId = @"1";
  NSString* experimentTitle = @"title";
  NSTimeInterval timeoutInterval = 20;//20 seconds
  
  NSDate* now = [NSDate date];
  NSDate* date4 = [NSDate dateWithTimeInterval:5 sinceDate:now]; //scheduled 1
  NSDate* date5 = [NSDate dateWithTimeInterval:10 sinceDate:now]; //scheduled 2
  
  NSString* alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date4], experimentTitle];
  UILocalNotification* scheduledNoti1 =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                              experimentTitle:alertBody
                                               fireDate:date4
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date4]];
  
  alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date5], experimentTitle];
  UILocalNotification* scheduledNoti2 =
  [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                              experimentTitle:alertBody
                                               fireDate:date5
                                            timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date5]];
  
  NSArray* allNotifications = @[scheduledNoti1, scheduledNoti2];
  NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                     NSArray* expiredNotifications,
                                     NSArray* notFiredNotifications) {
    XCTAssertNil(activeNotification, @"should be nil");
    XCTAssertNil(expiredNotifications, @"should be nil");
    NSArray* scheduledNotis = @[scheduledNoti1, scheduledNoti2];
    XCTAssertEqualObjects(notFiredNotifications, scheduledNotis, @"should have two scheduled notifications");
  };
  [UILocalNotification pacoProcessNotifications:allNotifications withBlock:block];
}

- (void)testSortNotificationsPerExperiment {
  NSDate* now = [NSDate date];
  NSDate* date1 = [NSDate dateWithTimeInterval:10 sinceDate:now];
  NSDate* date2 = [NSDate dateWithTimeInterval:20 sinceDate:now];
  NSDate* date3 = [NSDate dateWithTimeInterval:30 sinceDate:now];
  NSDate* date4 = [NSDate dateWithTimeInterval:40 sinceDate:now];
  
  NSTimeInterval timeoutInterval = 479*60;
  NSDate* timeout1 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1];
  NSDate* timeout2 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date2];
  NSDate* timeout3 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date3];
  NSDate* timeout4 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date4];
  
  NSString* experimentId1 = @"1";
  NSString* experimentId2 = @"2";
  NSString* title1 = @"title1";
  NSString* title2 = @"title2";
  
  //id:1, fireDate:date4
  //id:2, fireDate:date3
  //id:1, fireDate:date1
  //id:2, fireDate:date2
  NSMutableArray* allNotifications = [NSMutableArray arrayWithCapacity:4];
  
  //id:1, fireDate:date4
  NSString* alertBody = [NSString stringWithFormat:@"[%@]%@",
                         [PacoDateUtility stringForAlertBodyFromDate:date4],
                         title1];
  UILocalNotification* notification1 =
      [UILocalNotification pacoNotificationWithExperimentId:experimentId1
                                              experimentTitle:alertBody
                                               fireDate:date4
                                            timeOutDate:timeout4];
  [allNotifications addObject:notification1];

  //id:2, fireDate:date3
  alertBody = [NSString stringWithFormat:@"[%@]%@",
                         [PacoDateUtility stringForAlertBodyFromDate:date3],
                         title2];
  UILocalNotification* notification2 =
      [UILocalNotification pacoNotificationWithExperimentId:experimentId2
                                                  experimentTitle:alertBody
                                                   fireDate:date3
                                                timeOutDate:timeout3];
  [allNotifications addObject:notification2];
  
  //id:1, fireDate:date1
  alertBody = [NSString stringWithFormat:@"[%@]%@",
               [PacoDateUtility stringForAlertBodyFromDate:date1],
               title1];
  UILocalNotification* notification3 =
      [UILocalNotification pacoNotificationWithExperimentId:experimentId1
                                                  experimentTitle:alertBody
                                                   fireDate:date1
                                                timeOutDate:timeout1];
  [allNotifications addObject:notification3];

  //id:2, fireDate:date2
  alertBody = [NSString stringWithFormat:@"[%@]%@",
               [PacoDateUtility stringForAlertBodyFromDate:date2],
               title2];
  UILocalNotification* notification4 =
      [UILocalNotification pacoNotificationWithExperimentId:experimentId2
                                                  experimentTitle:alertBody
                                                   fireDate:date2
                                                timeOutDate:timeout2];
  [allNotifications addObject:notification4];

  //allNotifications:
  //id:1, fireDate:date4
  //id:2, fireDate:date3
  //id:1, fireDate:date1
  //id:2, fireDate:date2
  NSDictionary* result = [UILocalNotification sortNotificationsPerExperiment:allNotifications];
  NSMutableDictionary* expect = [NSMutableDictionary dictionaryWithCapacity:2];
  NSArray* notifications1 = @[notification3, notification1];
  NSArray* notifications2 = @[notification4, notification2];
  expect[experimentId1] = notifications1;
  expect[experimentId2] = notifications2;
  
  XCTAssertEqualObjects(result, expect,
                       @"notifications should be put into different buckets according to their "
                       @"experimentId, and each bucket's notifications should be sorted by fire date");
}


@end
