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
static NSString* const kAlertBodyDefault = @"Default Alert Body Default";
static NSString* const kSoundNameDefault = @"deepbark_trial_default.mp3";

static NSString* const kAlertBodyFirst = @"Default Alert Body First";
static NSString* const kAlertBodySecond = @"Default Alert Body Second";
static NSString* const kAlertBodyThird = @"Default Alert Body Third";

static NSString* const kSoundNameFirst = @"deepbark_trial_first.mp3";
static NSString* const kSoundNameSecond = @"deepbark_trial_second.mp3";
static NSString* const kSoundNameThird = @"deepbark_trial_third.mp3";

@interface PacoIOSLocalNotificationTests : XCTestCase
@end

@implementation PacoIOSLocalNotificationTests

- (void)setUp {
  [super setUp];
  // Put setup code here; it will be run once, before the first test case.
  [[UIApplication sharedApplication] cancelAllLocalNotifications];
}

- (void)tearDown {
  // Put teardown code here; it will be run once, after the last test case.
  [super tearDown];
  [[UIApplication sharedApplication] cancelAllLocalNotifications];
}

- (void)testScheduleNilNotifications {
  UILocalNotification* notification = [[UILocalNotification alloc] init];
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.alertBody = kAlertBodyDefault;
  notification.soundName = kSoundNameDefault;
  notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:10];
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 1, @"there should be one notification");
  
  [UIApplication sharedApplication].scheduledLocalNotifications = nil;
  
  scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 0, @"there should be 0 notification");
}

- (void)testScheduleEmptyNotifications {
  UILocalNotification* notification = [[UILocalNotification alloc] init];
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.alertBody = kAlertBodyDefault;
  notification.soundName = kSoundNameDefault;
  notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:10];
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 1, @"there should be one notification");
  
  [UIApplication sharedApplication].scheduledLocalNotifications = @[];
  
  scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 0, @"there should be 0 notification");
}

- (void)testScheduleInvalidNotificationBeforeNow {
  UILocalNotification* notification = [[UILocalNotification alloc] init];
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.alertBody = kAlertBodyDefault;
  notification.soundName = kSoundNameDefault;
  notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:-1];
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 0,
                 @"it should be invalid to schedule a notification one second before now");
}

- (void)testScheduleInvalidNotificationForNow {
  UILocalNotification* notification = [[UILocalNotification alloc] init];
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.alertBody = kAlertBodyDefault;
  notification.soundName = kSoundNameDefault;
  notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:0];
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 0,
                 @"it should be invalid to schedule a notification for now");
}


- (void)testCancelNilNotification {
  XCTAssertThrows([[UIApplication sharedApplication] cancelLocalNotification:nil],
                 @"Cancle a nil notification will throw an exception!");
}


- (void)testCancelAnInvalidNotification {
  UILocalNotification* notification = [[UILocalNotification alloc] init];
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.alertBody = kAlertBodyDefault;
  notification.soundName = kSoundNameDefault;
  notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:0];
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 0,
                 @"it should be invalid to schedule a notification for now");
  
  [[UIApplication sharedApplication] cancelLocalNotification:notification];
  scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 0,
                 @"it should be fine to cancel an invalid notification from UIApplication");
}


- (void)testCancelTimeoutNotification {
  UILocalNotification* notification = [[UILocalNotification alloc] init];
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.alertBody = kAlertBodyDefault;
  notification.soundName = kSoundNameDefault;
  notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:1];
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 1,
                 @"it should be invalid to schedule a notification for now");
  
  sleep(1);
  
  scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 0,
                 @"the notification should fire already");

  [[UIApplication sharedApplication] cancelLocalNotification:notification];
  
  scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 0,
                 @"it should be fine to cancel an invalid notification from UIApplication");
}

- (void)testScheduleValidNotificationAfterNow {
  UILocalNotification* notification = [[UILocalNotification alloc] init];
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.alertBody = kAlertBodyDefault;
  notification.soundName = kSoundNameDefault;
  notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:1];
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 1,
                 @"it should be valid to schedule a notification one second after now");
}

- (void)testCancelScheduledNotification {
  UILocalNotification* notification = [[UILocalNotification alloc] init];
  notification.timeZone = [NSTimeZone systemTimeZone];
  notification.alertBody = kAlertBodyDefault;
  notification.soundName = kSoundNameDefault;
  notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:10];
  
  [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 1,
                 @"it should be valid to schedule a notification one second after now");
  
  UILocalNotification* notificationInIOS = [scheduled firstObject];
  XCTAssertTrue(notification != notificationInIOS,
               @"notification is copied into iOS SDK, instead of retained as a pointer");
  XCTAssertTrue([notification isEqual:notificationInIOS],
               @"iOS SDK implemented an isEqual method for UILocalNotification");
  XCTAssertEqualObjects(notification, notificationInIOS,
                       @"different objects of UILocalNotification are considered the same"
                       @"as long as they have exactly the same information");
  
  [[UIApplication sharedApplication] cancelLocalNotification:notification];
  scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 0,
                 @"cancelling the original notification object is able to cancel the one"
                 @"held in iOS SDK");
}


- (void)testScheduleMaximumNotifcations {
  NSDate* date = [NSDate dateWithTimeIntervalSinceNow:10];
  for (int count = 0; count < 64; count++) {
    UILocalNotification *notification = [[UILocalNotification alloc] init];
    notification.timeZone = [NSTimeZone systemTimeZone];
    notification.alertBody = kAlertBodyDefault;
    notification.soundName = kSoundNameDefault;
    notification.fireDate = [date dateByAddingTimeInterval:count];
    [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  }
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 64,
                 @"we should be able to schedule as many as 64 notifcations");
}

- (void)testScheduleMoreThanMaximumNotifcations {
  NSDate* date = [NSDate dateWithTimeIntervalSinceNow:10];
  for (int count = 0; count < 70; count++) {
    UILocalNotification* notification = [[UILocalNotification alloc] init];
    notification.timeZone = [NSTimeZone systemTimeZone];
    notification.alertBody = kAlertBodyDefault;
    notification.soundName = kSoundNameDefault;
    notification.fireDate = [date dateByAddingTimeInterval:count];
    [[UIApplication sharedApplication] scheduleLocalNotification:notification];
  }
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 64,
                 @"notifications exceeding 64 will be dropped!");
  UILocalNotification* first = [scheduled firstObject];
  XCTAssertEqualObjects(first.fireDate, [date dateByAddingTimeInterval:0],
                       @"notifications fired later will be dropped!");
  UILocalNotification* last = [scheduled lastObject];
  XCTAssertEqualObjects(last.fireDate,[date dateByAddingTimeInterval:63],
                       @"notifications fired later will be dropped!");
}

- (void)testScheduleDifferentNotifcationsWithSameFireDate {
  NSDate* date = [NSDate dateWithTimeIntervalSinceNow:10];

  UILocalNotification* notificationFirst = [[UILocalNotification alloc] init];
  notificationFirst.timeZone = [NSTimeZone systemTimeZone];
  notificationFirst.alertBody = kAlertBodyFirst;
  notificationFirst.soundName = kSoundNameFirst;
  notificationFirst.fireDate = date;
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationFirst];
  
  UILocalNotification* notificationSecond = [[UILocalNotification alloc] init];
  notificationSecond.timeZone = [NSTimeZone systemTimeZone];
  notificationSecond.alertBody = kAlertBodySecond;
  notificationSecond.soundName = kSoundNameSecond;
  notificationSecond.fireDate = date;
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationSecond];
  
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 2,
                 @"notifications with same fire date but different other information will be"
                 @"treated as two different notifications");
}

- (void)testScheduleTwoNotifcationsWithDifferentFireDates {
  NSDate* dateFirst = [NSDate dateWithTimeIntervalSinceNow:10];
  NSDate* dateSecond = [dateFirst dateByAddingTimeInterval:1];
  
  UILocalNotification* notificationFirst = [[UILocalNotification alloc] init];
  notificationFirst.timeZone = [NSTimeZone systemTimeZone];
  notificationFirst.alertBody = kAlertBodyDefault;
  notificationFirst.soundName = kSoundNameDefault;
  notificationFirst.fireDate = dateFirst;
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationFirst];
  
  UILocalNotification* notificationSecond = [[UILocalNotification alloc] init];
  notificationSecond.timeZone = [NSTimeZone systemTimeZone];
  notificationSecond.alertBody = kAlertBodyDefault;
  notificationSecond.soundName = kSoundNameDefault;
  notificationSecond.fireDate = dateSecond;
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationSecond];
  
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 2,
                 @"notifications with different fire dates will be treated as two different notifications");
}


- (void)testScheduleTwoIdenticalNotifcationsWithoutUserInfo {
  NSDate* date = [NSDate dateWithTimeIntervalSinceNow:10];
  
  UILocalNotification* notificationFirst = [[UILocalNotification alloc] init];
  notificationFirst.timeZone = [NSTimeZone systemTimeZone];
  notificationFirst.alertBody = kAlertBodyDefault;
  notificationFirst.soundName = kSoundNameDefault;
  notificationFirst.fireDate = date;
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationFirst];
  
  UILocalNotification* notificationSecond = [[UILocalNotification alloc] init];
  notificationSecond.timeZone = [NSTimeZone systemTimeZone];
  notificationSecond.alertBody = kAlertBodyDefault;
  notificationSecond.soundName = kSoundNameDefault;
  notificationSecond.fireDate = date;
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationSecond];
  
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 1,
                 @"notifications with same information will be treated as one notification");
}

- (void)testScheduleTwoIdenticalNotifcationsWithSameUserInfo {
  NSDate* date = [NSDate dateWithTimeIntervalSinceNow:10];
  
  UILocalNotification* notificationFirst = [[UILocalNotification alloc] init];
  notificationFirst.timeZone = [NSTimeZone systemTimeZone];
  notificationFirst.alertBody = kAlertBodyDefault;
  notificationFirst.soundName = kSoundNameDefault;
  notificationFirst.fireDate = date;
  notificationFirst.userInfo = @{@"fireDate":date};
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationFirst];
  
  UILocalNotification* notificationSecond = [[UILocalNotification alloc] init];
  notificationSecond.timeZone = [NSTimeZone systemTimeZone];
  notificationSecond.alertBody = kAlertBodyDefault;
  notificationSecond.soundName = kSoundNameDefault;
  notificationSecond.fireDate = date;
  notificationSecond.userInfo = @{@"fireDate":date};
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationSecond];
  
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  NSLog(@"[%d]%@",[scheduled count], scheduled);
  XCTAssertEqual((int)[scheduled count], 1,
                 @"notifications with same information should be treated as one notification");
}


- (void)testScheduleTwoNotifcationsWithUserInfoOrderedDifferently {
  NSDate* date = [NSDate dateWithTimeIntervalSinceNow:10];
  NSDate* timeOutDate = [date dateByAddingTimeInterval:20];
  
  UILocalNotification* notificationFirst = [[UILocalNotification alloc] init];
  notificationFirst.timeZone = [NSTimeZone systemTimeZone];
  notificationFirst.alertBody = kAlertBodyDefault;
  notificationFirst.soundName = kSoundNameDefault;
  notificationFirst.fireDate = date;
  notificationFirst.userInfo = @{@"id":@"1", @"fireDate":date, @"timeOutDate":timeOutDate};
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationFirst];
  
  UILocalNotification* notificationSecond = [[UILocalNotification alloc] init];
  notificationSecond.timeZone = [NSTimeZone systemTimeZone];
  notificationSecond.alertBody = kAlertBodyDefault;
  notificationSecond.soundName = kSoundNameDefault;
  notificationSecond.fireDate = date;
  notificationSecond.userInfo =  @{@"timeOutDate":timeOutDate, @"fireDate":date, @"id":@"1"};;
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationSecond];
  
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 1,
                 @"notifications with same information should be treated as one notification");
}

- (void)testScheduleTwoNotifcationsWithDifferentUserInfo {
  NSDate* date = [NSDate dateWithTimeIntervalSinceNow:10];
  NSDate* timeOutDateFirst = [date dateByAddingTimeInterval:5];
  NSDate* timeOutDateSecond = [date dateByAddingTimeInterval:6];
  
  UILocalNotification* notificationFirst = [[UILocalNotification alloc] init];
  notificationFirst.timeZone = [NSTimeZone systemTimeZone];
  notificationFirst.alertBody = kAlertBodyDefault;
  notificationFirst.soundName = kSoundNameDefault;
  notificationFirst.fireDate = date;
  notificationFirst.userInfo = @{@"id":@"1", @"timeoutDate":timeOutDateFirst};
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationFirst];
  
  UILocalNotification* notificationSecond = [[UILocalNotification alloc] init];
  notificationSecond.timeZone = [NSTimeZone systemTimeZone];
  notificationSecond.alertBody = kAlertBodyDefault;
  notificationSecond.soundName = kSoundNameDefault;
  notificationSecond.fireDate = date;
  notificationSecond.userInfo = @{@"id":@"1", @"timeoutDate":timeOutDateSecond};
  [[UIApplication sharedApplication] scheduleLocalNotification:notificationSecond];
  
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  NSLog(@"[%d]%@",[scheduled count], scheduled);
  XCTAssertEqual((int)[scheduled count], 2,
                 @"notifications with different userInfo should be treated as two notifications");
}


- (void)testScheduleMultipleIdenticalNotifcations {
  NSDate* date = [NSDate dateWithTimeIntervalSinceNow:10];
  NSDate* timeOutDate = [date dateByAddingTimeInterval:20];
  for (int count = 0; count < 60; count++) {
    UILocalNotification* noti = [[UILocalNotification alloc] init];
    noti.timeZone = [NSTimeZone systemTimeZone];
    noti.alertBody = kAlertBodyDefault;
    noti.soundName = kSoundNameDefault;
    noti.fireDate = date;
    noti.userInfo = @{@"id":@"1", @"fireDate":date, @"timeOutDate":timeOutDate};
    [[UIApplication sharedApplication] scheduleLocalNotification:noti];
  }
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 1,
                 @"notifications with same information should be treated as one notification");
}


- (void)testCancelNotificationWithoutUserInfo {
  NSDate* date = [NSDate dateWithTimeIntervalSinceNow:10];
  UILocalNotification* noti = [[UILocalNotification alloc] init];
  noti.timeZone = [NSTimeZone systemTimeZone];
  noti.alertBody = kAlertBodyDefault;
  noti.soundName = kSoundNameDefault;
  noti.fireDate = date;
  [[UIApplication sharedApplication] scheduleLocalNotification:noti];
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 1,
                 @"there should be one notification scheduled!");
  
  
  UILocalNotification* toBeCancelled = [[UILocalNotification alloc] init];
  toBeCancelled.timeZone = [NSTimeZone systemTimeZone];
  toBeCancelled.alertBody = kAlertBodyDefault;
  toBeCancelled.soundName = kSoundNameDefault;
  toBeCancelled.fireDate = date;
  [[UIApplication sharedApplication] cancelLocalNotification:toBeCancelled];
  scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 0,
                 @"the scheduled notification should be cancelled");
}

- (void)testCancelNotificationWithUserInfo {
  NSDate* date = [NSDate dateWithTimeIntervalSinceNow:10];
  NSDate* timeOutDate = [date dateByAddingTimeInterval:20];
  UILocalNotification* noti = [[UILocalNotification alloc] init];
  noti.timeZone = [NSTimeZone systemTimeZone];
  noti.alertBody = kAlertBodyDefault;
  noti.soundName = kSoundNameDefault;
  noti.fireDate = date;
  noti.userInfo = @{@"id":@"1", @"fireDate":date, @"timeOutDate":timeOutDate};
  [[UIApplication sharedApplication] scheduleLocalNotification:noti];
  NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 1,
                 @"there should be one notification scheduled!");
  
  
  UILocalNotification* toBeCancelled = [[UILocalNotification alloc] init];
  toBeCancelled.timeZone = [NSTimeZone systemTimeZone];
  toBeCancelled.alertBody = kAlertBodyDefault;
  toBeCancelled.soundName = kSoundNameDefault;
  toBeCancelled.fireDate = date;
  toBeCancelled.userInfo = @{@"id":@"1", @"timeOutDate":timeOutDate, @"fireDate":date};
  [[UIApplication sharedApplication] cancelLocalNotification:toBeCancelled];
  scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
  XCTAssertEqual((int)[scheduled count], 0,
                 @"the scheduled notification should be cancelled");
}

@end
