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
#import "NSDate+Paco.h"
#import "PacoDateUtility.h"
#import <Foundation/Foundation.h>


@interface NSDate ()
- (NSDate*)pacoTimeWithIntervalOfHoursIn24:(NSInteger)hoursIn24
                                   minutes:(NSInteger)minutes
                                   seconds:(NSInteger)seconds;
- (NSDate*)pacoTimeFromMidnightWithMilliSeconds:(NSNumber*)milliSecondsNumber;
- (NSDate*)pacoFirstAvailableTimeWithTimes:(NSArray*)times;

- (NSDate*)pacoDailyESMNextCycleStartDate:(BOOL)includeWeekends;
- (NSDate*)pacoWeeklyESMNextCycleStartDate:(BOOL)includeWeekends;
- (NSDate*)pacoMonthlyESMNextCycleStartDate:(BOOL)includeWeekends;



@end



@interface PacoNSDateCategoryTests : XCTestCase

@property(nonatomic, strong) NSDateComponents* comp;
@property(nonatomic, strong) NSCalendar* calendar;

@property(nonatomic, strong) NSDate* dateEarlier;
@property(nonatomic, strong) NSDate* dateLater;

@end

@implementation PacoNSDateCategoryTests

- (void)setUp {
  [super setUp];
  // Put setup code here; it will be run once, before the first test case.
  self.comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [self.comp setTimeZone:timeZone];
  self.calendar = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  
  NSString* testStrEarlier = @"2013/07/25 12:33:22-0700";
  NSString* testStrLater = @"2013/07/25 12:33:23-0700";
  self.dateEarlier = [PacoDateUtility pacoDateForString:testStrEarlier];
  self.dateLater = [PacoDateUtility pacoDateForString:testStrLater];
  XCTAssertNotNil(self.dateEarlier, @"dateEarlier should be valid");
  XCTAssertNotNil(self.dateLater, @"dateLater should be valid");
}

- (void)tearDown {
  // Put teardown code here; it will be run once, after the last test case.
  self.comp = nil;
  self.calendar = nil;
  
  self.dateEarlier = nil;
  self.dateLater = nil;
  [super tearDown];
}

- (void)testEarlierThan {
  XCTAssertTrue([self.dateEarlier pacoEarlierThanDate:self.dateLater],  @"should be earlier");
  XCTAssertFalse([self.dateLater pacoEarlierThanDate:self.dateEarlier],  @"should be later");
  XCTAssertFalse([self.dateEarlier pacoEarlierThanDate:self.dateEarlier],  @"should be equal");
}

- (void)testLaterThan {
  XCTAssertFalse([self.dateEarlier pacoLaterThanDate:self.dateLater],  @"should be earlier");
  XCTAssertTrue([self.dateLater pacoLaterThanDate:self.dateEarlier],  @"should be later");
  XCTAssertFalse([self.dateEarlier pacoLaterThanDate:self.dateEarlier],  @"should be equal");
}

- (void)testEqualTo {
  XCTAssertFalse([self.dateEarlier pacoEqualToDate:self.dateLater],  @"should be earlier");
  XCTAssertFalse([self.dateLater pacoEqualToDate:self.dateEarlier],  @"should be later");
  XCTAssertTrue([self.dateEarlier pacoEqualToDate:self.dateEarlier],  @"should be equal");
}

- (void)testNoEarlierThan {
  XCTAssertFalse([self.dateEarlier pacoNoEarlierThanDate:self.dateLater],  @"should be earlier");
  XCTAssertTrue([self.dateLater pacoNoEarlierThanDate:self.dateEarlier],  @"should be later");
  XCTAssertTrue([self.dateEarlier pacoNoEarlierThanDate:self.dateEarlier],  @"should be equal");
}

- (void)testNoLaterThan {
  XCTAssertTrue([self.dateEarlier pacoNoLaterThanDate:self.dateLater],  @"should be earlier");
  XCTAssertFalse([self.dateLater pacoNoLaterThanDate:self.dateEarlier],  @"should be later");
  XCTAssertTrue([self.dateEarlier pacoNoLaterThanDate:self.dateEarlier],  @"should be equal");
}

- (void)testCurrentDayAtMidnight {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/07/25 12:33:22-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  NSDate* midnightDate = [testDate pacoCurrentDayAtMidnight];
  
  NSString* testStrMidnight = @"2013/07/25 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:testStrMidnight];
  XCTAssertNotNil(expect, @"expect should be valid");

  XCTAssertEqualObjects(midnightDate, expect, @"should be midnight");
}

- (void)testCurrentDayAtMidnight2 {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/07/25 00:00:00-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  NSDate* midnightDate = [testDate pacoCurrentDayAtMidnight];
  
  NSString* testStrMidnight = @"2013/07/25 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:testStrMidnight];
  XCTAssertNotNil(expect, @"expect should be valid");
  
  XCTAssertEqualObjects(midnightDate, expect, @"should be midnight");
}

//YMZ:TODO: the following test will fail, need to figure it out
//Feb 2, 2013 seems not being represented correctly by iOS
//- (void)testCurrentDayAtMidnightSpecialDay {
//  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/02/28 13:59:59-0700"];
//  STAssertNotNil(testDate, @"testDate should be valid");
//  STAssertEqualObjects([testDate description], @"2013-02-28 20:59:59 +0000", @"should be valid");
//
//  NSDate* midnightDate = [testDate pacoCurrentDayAtMidnight];
//  
//  NSString* testStrMidnight = @"2013/02/28 00:00:00-0700";
//  NSDate* expect = [PacoDateUtility pacoDateForString:testStrMidnight];
//  STAssertNotNil(expect, @"expect should be valid");
//  
//  STAssertEqualObjects(midnightDate, expect, @"should be midnight");
//}

- (void)testFutureDateAtMidnight {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/07/25 13:03:59-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  NSDate* futureDate = [testDate pacoDateAtMidnightByAddingDayInterval:3];

  NSString* futureStr = @"2013/07/28 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  XCTAssertNotNil(expect, @"expect should be valid");
  
  XCTAssertEqualObjects(futureDate, expect, @"should be 3 days later at midnight");
}

- (void)testFutureDateAtMidnight2 {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/07/25 13:03:59-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  NSDate* futureDate = [testDate pacoDateAtMidnightByAddingDayInterval:8];
  
  NSString* futureStr = @"2013/08/02 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  XCTAssertNotNil(expect, @"expect should be valid");
  
  XCTAssertEqualObjects(futureDate, expect, @"8 days later at midnight should be August 2");
}

- (void)testNextDayAtMidnight {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/07/25 13:03:59-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  NSDate* futureDate = [testDate pacoNextDayAtMidnight];
  
  NSString* futureStr = @"2013/07/26 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  XCTAssertNotNil(expect, @"expect should be valid");
  
  XCTAssertEqualObjects(futureDate, expect, @"should be one day later at midnight");
}

- (void)testNextDayAtMidnight2 {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/07/31 23:59:59-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  NSDate* futureDate = [testDate pacoNextDayAtMidnight];
  
  NSString* futureStr = @"2013/08/01 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  XCTAssertNotNil(expect, @"expect should be valid");
  
  XCTAssertEqualObjects(futureDate, expect, @"should be one day later at midnight");
}

//YMZ:TODO: the following test will fail
//- (void)testNextDayAtMidnightOnASpecialDay {
//  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/02/28 13:59:59-0700"];
//  STAssertNotNil(testDate, @"testDate should be valid");
//  STAssertEqualObjects([testDate description], @"2013-02-28 20:59:59 +0000", @"should be valid");
//  NSDate* futureDate = [testDate pacoNextDayAtMidnight];
//
//  NSString* futureStr = @"2013/03/01 00:00:00-0700";
//  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
//  STAssertNotNil(expect, @"expect should be valid");
//  
//  STAssertEqualObjects(futureDate, expect, @"should be one day later at midnight");
//}


- (void)testTimeWithIntervalOfHours {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 00:00:00-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  NSDate* futureDate = [testDate pacoTimeWithIntervalOfHoursIn24:3 minutes:10 seconds:56];
  
  NSString* futureStr = @"2013/10/12 03:10:56-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  XCTAssertNotNil(expect, @"expect should be valid");
  
  XCTAssertEqualObjects(futureDate, expect, @"should be 3 hours and 10 minutes after midnight");
}

- (void)testTimeFromMidnightWithMilliseconds {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 21:34:15-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  
  int hours = 13;
  int minutes = 35;
  int seconds = 56;
  long milliseconds = ((hours*60+minutes)*60+seconds)*1000;
  NSDate* futureDate =
      [testDate pacoTimeFromMidnightWithMilliSeconds:@(milliseconds)];
  
  NSString* futureStr = @"2013/10/12 13:35:56-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  XCTAssertNotNil(expect, @"expect should be valid");
  XCTAssertEqualObjects(futureDate, expect, @"should be valid");
}

- (void)testTimeFromMidnightWithMilliseconds2 {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 21:34:15-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  
  int hours = 0;
  int minutes = 0;
  int seconds = 0;
  long milliseconds = ((hours*60+minutes)*60+seconds)*1000;
  NSDate* futureDate =
      [testDate pacoTimeFromMidnightWithMilliSeconds:@(milliseconds)];
  
  NSString* futureStr = @"2013/10/12 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  XCTAssertNotNil(expect, @"expect should be valid");
  XCTAssertEqualObjects(futureDate, expect, @"should be midnight");
}

- (void)testFirstAvailableTimeWithTimes {
  int hours = 9;
  int minutes = 35;
  int seconds = 50;
  long firstMilliseconds = ((hours*60+minutes)*60+seconds)*1000;
  hours = 17;
  minutes = 23;
  seconds = 44;
  long secondMilliseconds = ((hours*60+minutes)*60+seconds)*1000;
  
  //09:35:50, 17:23:44
  NSArray* times = @[@(firstMilliseconds),
                     @(secondMilliseconds)];
  NSDate* firstTime = [PacoDateUtility pacoDateForString:@"2013/10/12 09:35:50-0700"];
  NSDate* secondTime = [PacoDateUtility pacoDateForString:@"2013/10/12 17:23:44-0700"];

  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 08:34:15-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  XCTAssertEqualObjects([testDate pacoFirstAvailableTimeWithTimes:times], firstTime,
                       @"should be the first time");
  XCTAssertTrue([testDate pacoCanScheduleTimes:times], @"should be able to schedule times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 17:23:00-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  XCTAssertEqualObjects([testDate pacoFirstAvailableTimeWithTimes:times], secondTime,
                       @"should be the second time");
  XCTAssertTrue([testDate pacoCanScheduleTimes:times], @"should be able to schedule times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 17:23:44-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  XCTAssertEqualObjects([testDate pacoFirstAvailableTimeWithTimes:times], secondTime,
                       @"should be the second time");
  XCTAssertTrue([testDate pacoCanScheduleTimes:times], @"should be able to schedule times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 17:23:45-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  XCTAssertNil([testDate pacoFirstAvailableTimeWithTimes:times], @"should be nil");
  XCTAssertFalse([testDate pacoCanScheduleTimes:times], @"should not be able to schedule times");
}

- (void)testDatesToScheduleWithTimes {
  int hours = 9;
  int minutes = 35;
  int seconds = 50;
  long firstMilliseconds = ((hours*60+minutes)*60+seconds)*1000;
  hours = 12;
  minutes = 13;
  seconds = 22;
  long secondMilliseconds = ((hours*60+minutes)*60+seconds)*1000;
  hours = 17;
  minutes = 23;
  seconds = 44;
  long thirdMilliseconds = ((hours*60+minutes)*60+seconds)*1000;
  
  //09:35:50, 12:13:22, 17:23:44
  NSArray* times = @[@(firstMilliseconds),
                     @(secondMilliseconds),
                     @(thirdMilliseconds)];
  NSDate* firstTime = [PacoDateUtility pacoDateForString:@"2013/10/12 09:35:50-0700"];
  NSDate* secondTime = [PacoDateUtility pacoDateForString:@"2013/10/12 12:13:22-0700"];
  NSDate* thirdTime = [PacoDateUtility pacoDateForString:@"2013/10/12 17:23:44-0700"];
  
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 08:34:15-0700"];
  NSDate* endDate = [PacoDateUtility pacoDateForString:@"2013/10/13 00:00:00-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  XCTAssertNotNil(endDate, @"endDate should be valid");
  NSArray* expect = @[firstTime, secondTime, thirdTime];
  XCTAssertEqualObjects([testDate pacoDatesToScheduleWithTimes:times andEndDate:endDate], expect,
                       @"should be able to schedule three times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 10:34:15-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  expect = @[secondTime, thirdTime];
  XCTAssertEqualObjects([testDate pacoDatesToScheduleWithTimes:times andEndDate:endDate], expect,
                       @"should be able to schedule two times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 13:34:15-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  expect = @[thirdTime];
  XCTAssertEqualObjects([testDate pacoDatesToScheduleWithTimes:times andEndDate:endDate], expect,
                       @"should be able to schedule one times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 17:34:15-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  expect = @[];
  XCTAssertEqualObjects([testDate pacoDatesToScheduleWithTimes:times andEndDate:endDate], expect,
                       @"should not be able to schedule any time");
  
  
  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 08:34:15-0700"];
  endDate = [PacoDateUtility pacoDateForString:@"2013/10/12 00:00:00-0700"];
  XCTAssertNotNil(testDate, @"testDate should be valid");
  XCTAssertNotNil(endDate, @"endDate should be valid");
  expect = @[];
  XCTAssertEqualObjects([testDate pacoDatesToScheduleWithTimes:times andEndDate:endDate], expect,
                       @"should not be able to schedule any time since it passed the endDate");
}


- (void)testPacoIsWeekend {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:10];
  [comp setDay:16];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Wed, Oct 16, 2013, 00:00:00
  NSDate* date = [gregorian dateFromComponents:comp];
  BOOL isWeekend = [date pacoIsWeekend];
  XCTAssertFalse(isWeekend, @"should not be weekend");
  
  //Sat, Nov 2, 2013, 12:00:00
  [comp setMonth:11];
  [comp setDay:2];
  date = [gregorian dateFromComponents:comp];
  isWeekend = [date pacoIsWeekend];
  XCTAssertTrue(isWeekend, @"should be weekend");

  //Sun, Nov 10, 2013, 23:59:59
  [comp setMonth:11];
  [comp setDay:10];
  date = [gregorian dateFromComponents:comp];
  isWeekend = [date pacoIsWeekend];
  XCTAssertTrue(isWeekend, @"should be weekend");
}

- (void)testPacoDateByAddingMonthIntervalSameYear {
  [self.comp setYear:2014];
  [self.comp setMonth:3];
  [self.comp setDay:6];
  [self.comp setHour:9];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //testDate: 3/6, 9:35:50, 2014
  NSDate* testDate = [self.calendar dateFromComponents:self.comp];
  
  NSDate* threeMonthsLater = [testDate pacoDateByAddingMonthInterval:3];
  [self.comp setYear:2014];
  [self.comp setMonth:6];
  [self.comp setDay:6];
  [self.comp setHour:9];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //threeMonthsLater: 6/6, 9:35:50, 2014
  NSDate* expect = [self.calendar dateFromComponents:self.comp];
  XCTAssertEqualObjects(threeMonthsLater, expect, @"should be three months later");
}


- (void)testPacoDateByAddingMonthIntervalAcrossYears {
  [self.comp setYear:2013];
  [self.comp setMonth:11];
  [self.comp setDay:6];
  [self.comp setHour:9];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //testDate: 11/6, 9:35:50, 2013
  NSDate* testDate = [self.calendar dateFromComponents:self.comp];
  
  NSDate* twoMonthsLater = [testDate pacoDateByAddingMonthInterval:2];
  [self.comp setYear:2014];
  [self.comp setMonth:1];
  [self.comp setDay:6];
  [self.comp setHour:9];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //twoMonthsLater: 1/6, 9:35:50, 2014
  NSDate* expect = [self.calendar dateFromComponents:self.comp];
  XCTAssertEqualObjects(twoMonthsLater, expect, @"should be two months later");
  
}


- (void)testPacoDateByAddingWeekInterval {
  [self.comp setYear:2013];
  [self.comp setMonth:11];
  [self.comp setDay:6];
  [self.comp setHour:9];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //testDate: 11/6, 9:35:50, 2013
  NSDate* testDate = [self.calendar dateFromComponents:self.comp];
  
  NSDate* oneWeekLater = [testDate pacoDateByAddingWeekInterval:1];
  [self.comp setYear:2013];
  [self.comp setMonth:11];
  [self.comp setDay:13];
  [self.comp setHour:9];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //oneWeekLater: 11/13, 9:35:50, 2013
  NSDate* expect = [self.calendar dateFromComponents:self.comp];
  XCTAssertEqualObjects(oneWeekLater, expect, @"should be one week later");
  
  NSDate* twoWeeksLater = [testDate pacoDateByAddingWeekInterval:2];
  [self.comp setYear:2013];
  [self.comp setMonth:11];
  [self.comp setDay:20];
  [self.comp setHour:9];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //twoWeeksLater: 11/20, 9:35:50, 2013
  expect = [self.calendar dateFromComponents:self.comp];
  XCTAssertEqualObjects(twoWeeksLater, expect, @"should be two weeks later");

  NSDate* fourWeeksLater = [testDate pacoDateByAddingWeekInterval:4];
  [self.comp setYear:2013];
  [self.comp setMonth:12];
  [self.comp setDay:4];
  [self.comp setHour:9];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //fourWeeksLater: 11/20, 9:35:50, 2013
  expect = [self.calendar dateFromComponents:self.comp];
  XCTAssertEqualObjects(fourWeeksLater, expect, @"should be four weeks later");

  NSDate* eightWeeksLater = [testDate pacoDateByAddingWeekInterval:8];
  [self.comp setYear:2014];
  [self.comp setMonth:1];
  [self.comp setDay:1];
  [self.comp setHour:9];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //eightWeeksLater: 1/1, 9:35:50, 2014
  expect = [self.calendar dateFromComponents:self.comp];
  XCTAssertEqualObjects(eightWeeksLater, expect, @"should be eight weeks later");
}


- (void)testNumOfDaysInJan2013 {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:1];
  [comp setDay:16];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Wed, Jan 16, 2013, 00:00:00
  NSDate* date = [gregorian dateFromComponents:comp];
  int numOfDays = [date pacoNumOfDaysInCurrentMonth];
  int numOfWeekDays = [date pacoNumOfWeekdaysInCurrentMonth];
  XCTAssertEqual(numOfDays, 31, @"should have 31 days in Jan 2013");
  XCTAssertEqual(numOfWeekDays, 23, @"should have 28 week days in Jan 2013");
  
  [comp setDay:31];
  [comp setHour:10];
  [comp setMinute:23];
  [comp setSecond:54];
  //Thurs, Jan 31, 2013, 10:23:54
  date = [gregorian dateFromComponents:comp];
  numOfDays = [date pacoNumOfDaysInCurrentMonth];
  numOfWeekDays = [date pacoNumOfWeekdaysInCurrentMonth];
  XCTAssertEqual(numOfDays, 28, @"should have 28 days in Feb 2013");
  XCTAssertEqual(numOfWeekDays, 20, @"should have 20 week days in Feb 2013");
}

- (void)testNumOfDaysInFeb2013 {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:2];
  [comp setDay:28];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Thurs, Feb 28, 2013, 00:00:00
  NSDate* date = [gregorian dateFromComponents:comp];
  int numOfDays = [date pacoNumOfDaysInCurrentMonth];
  int numOfWeekDays = [date pacoNumOfWeekdaysInCurrentMonth];
  XCTAssertEqual(numOfDays, 28, @"should have 28 days in Feb 2013");
  XCTAssertEqual(numOfWeekDays, 20, @"should have 20 week days in Feb 2013");
}


- (void)testPacoFirstDayInCurrentMonth {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:11];
  [comp setDay:30];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //11/30, 2013, 10:00:00
  NSDate* date = [gregorian dateFromComponents:comp];
  NSDate* firstDayInMonth = [date pacoFirstDayInCurrentMonth];
  [comp setMonth:11];
  [comp setDay:1];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  //11/30, 2013, 10:00:00
  NSDate* expect = [gregorian dateFromComponents:comp];
  XCTAssertEqualObjects(firstDayInMonth, expect, @"should be first day in month");
}


- (void)testPacoDayInCurrentMonth {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:3];
  [comp setDay:11];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Mar 11, 2014, 10:00:00
  NSDate* date = [gregorian dateFromComponents:comp];
  
  [comp setMonth:3];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  
  for (int dayIndex=1; dayIndex<=31; dayIndex++) {
    NSDate* dayInMonth = [date pacoDayInCurrentMonth:dayIndex];
    XCTAssertNotNil(dayInMonth, @"should be valid date");
    [comp setDay:dayIndex];
    NSDate* expect = [gregorian dateFromComponents:comp];
    XCTAssertNotNil(expect, @"should be valid date");
    XCTAssertEqualObjects(dayInMonth, expect, @"should be a day in month");
  }
}


- (void)testPacoDayInCurrentMonthExceedLimit {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:2];
  [comp setDay:11];
  [comp setHour:10];
  [comp setMinute:28];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Feb 11, 2014, 10:28:00
  NSDate* date = [gregorian dateFromComponents:comp];

  NSDate* dayInMonth = [date pacoDayInCurrentMonth:28];
  XCTAssertNotNil(dayInMonth, @"Feb 28 is a valid date");
  //Feb 28, 2014, 00:00:00
  [comp setMonth:2];
  [comp setDay:28];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  NSDate* expect = [gregorian dateFromComponents:comp];
  XCTAssertEqualObjects(dayInMonth, expect, @"should be the last day in February");

  dayInMonth = [date pacoDayInCurrentMonth:30];
  XCTAssertNil(dayInMonth, @"Feb 30 is not a valid date");
}



- (void)testPacoSundayInCurrentWeekFromWeekday {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:11];
  [comp setDay:4];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Mon, 11/4, 2013, 10:00:00
  NSDate* date = [gregorian dateFromComponents:comp];
  NSDate* firstDayInWeek = [date pacoSundayInCurrentWeek];
  [comp setMonth:11];
  [comp setDay:3];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  //Sun, 11/3, 2013, 10:00:00
  NSDate* expect = [gregorian dateFromComponents:comp];
  XCTAssertEqualObjects(firstDayInWeek, expect, @"should be first day in week");
}

- (void)testPacoSundayInCurrentWeekFromSunday {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:11];
  [comp setDay:3];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Sun, 11/3, 2013, 10:00:00
  NSDate* date = [gregorian dateFromComponents:comp];
  NSDate* firstDayInWeek = [date pacoSundayInCurrentWeek];
  [comp setMonth:11];
  [comp setDay:3];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  //Sun, 11/3, 2013, 10:00:00
  NSDate* expect = [gregorian dateFromComponents:comp];
  XCTAssertEqualObjects(firstDayInWeek, expect, @"should be first day in week");
}

- (void)testPacoSundayInCurrentWeekFromSaturday {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:11];
  [comp setDay:9];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Sat, 11/9, 2013, 10:00:00
  NSDate* date = [gregorian dateFromComponents:comp];
  NSDate* firstDayInWeek = [date pacoSundayInCurrentWeek];
  [comp setMonth:11];
  [comp setDay:3];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  //Sun, 11/3, 2013, 10:00:00
  NSDate* expect = [gregorian dateFromComponents:comp];
  XCTAssertEqualObjects(firstDayInWeek, expect, @"should be first day in week");
}


- (void)testPacoSundayInCurrentWeekOnYearLastDay {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:12];
  [comp setDay:31];
  [comp setHour:18];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Tues, 12/31, 2013, 18:00:00
  NSDate* date = [gregorian dateFromComponents:comp];
  NSDate* firstDayInWeek = [date pacoSundayInCurrentWeek];
  
  [comp setYear:2013];
  [comp setMonth:12];
  [comp setDay:29];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  //Sun, 12/29, 2013, 00:00:00
  NSDate* expect = [gregorian dateFromComponents:comp];
  
  XCTAssertEqualObjects(firstDayInWeek, expect, @"should be first day in week");
}


- (void)testPacoSundayInCurrentWeekOnYearFirstDay {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:1];
  [comp setDay:1];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Sun, 1/1, 2014, 10:00:00
  NSDate* date = [gregorian dateFromComponents:comp];
  NSDate* firstDayInWeek = [date pacoSundayInCurrentWeek];
  
  [comp setYear:2013];
  [comp setMonth:12];
  [comp setDay:29];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  //Sun, 12/29, 2013, 00:00:00
  NSDate* expect = [gregorian dateFromComponents:comp];
  XCTAssertEqualObjects(firstDayInWeek, expect, @"should be first day in week");
}



- (void)testPacoCycleStartDateOfMonthWithOriginalStartDate {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:11];
  [comp setDay:4];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Mon, 11/4, 2013, 0:00:00
  NSDate* startDate = [gregorian dateFromComponents:comp];
  
  [comp setMonth:11];
  [comp setDay:5];
  [comp setHour:20];
  [comp setMinute:10];
  [comp setSecond:45];
  //11/5, 2013, 20:10:45
  NSDate* date = [gregorian dateFromComponents:comp];
  NSDate* cycleStartDate = [date pacoCycleStartDateOfMonthWithOriginalStartDate:startDate];
  NSDate* expect = startDate;
  XCTAssertEqualObjects(cycleStartDate, expect, @"should be original start date");

  [comp setYear:2014];
  [comp setMonth:1];
  [comp setDay:5];
  [comp setHour:20];
  [comp setMinute:10];
  [comp setSecond:45];
  //1/5, 2014, 20:10:45
  date = [gregorian dateFromComponents:comp];
  cycleStartDate = [date pacoCycleStartDateOfMonthWithOriginalStartDate:startDate];
  [comp setYear:2014];
  [comp setMonth:1];
  [comp setDay:4];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  //1/4, 2014, 00:00:00
  expect = [gregorian dateFromComponents:comp];
  XCTAssertEqualObjects(cycleStartDate, expect, @"should be original start date");
}



@end
