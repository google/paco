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
#import "NSDate+Paco.h"
#import "PacoDateUtility.h"
#import <Foundation/Foundation.h>


@interface NSDate ()
- (NSDate*)pacoTimeWithIntervalOfHoursIn24:(NSInteger)hoursIn24
                                   minutes:(NSInteger)minutes
                                   seconds:(NSInteger)seconds;
- (NSDate*)pacoTimeFromMidnightWithMilliSeconds:(NSNumber*)milliSecondsNumber;
- (NSDate*)pacoFirstAvailableTimeWithTimes:(NSArray*)times;
@end



@interface PacoNSDateCategoryTests : SenTestCase

@property(nonatomic, strong) NSDate* dateEarlier;
@property(nonatomic, strong) NSDate* dateLater;

@end

@implementation PacoNSDateCategoryTests

- (void)setUp {
  [super setUp];
  // Put setup code here; it will be run once, before the first test case.
  NSString* testStrEarlier = @"2013/07/25 12:33:22-0700";
  NSString* testStrLater = @"2013/07/25 12:33:23-0700";
  self.dateEarlier = [PacoDateUtility pacoDateForString:testStrEarlier];
  self.dateLater = [PacoDateUtility pacoDateForString:testStrLater];
  STAssertNotNil(self.dateEarlier, @"dateEarlier should be valid");
  STAssertNotNil(self.dateLater, @"dateLater should be valid");
}

- (void)tearDown {
  // Put teardown code here; it will be run once, after the last test case.
  self.dateEarlier = nil;
  self.dateLater = nil;
  [super tearDown];
}

- (void)testEarlierThan {
  STAssertTrue([self.dateEarlier pacoEarlierThanDate:self.dateLater],  @"should be earlier");
  STAssertFalse([self.dateLater pacoEarlierThanDate:self.dateEarlier],  @"should be later");
  STAssertFalse([self.dateEarlier pacoEarlierThanDate:self.dateEarlier],  @"should be equal");
}

- (void)testLaterThan {
  STAssertFalse([self.dateEarlier pacoLaterThanDate:self.dateLater],  @"should be earlier");
  STAssertTrue([self.dateLater pacoLaterThanDate:self.dateEarlier],  @"should be later");
  STAssertFalse([self.dateEarlier pacoLaterThanDate:self.dateEarlier],  @"should be equal");
}

- (void)testEqualTo {
  STAssertFalse([self.dateEarlier pacoEqualToDate:self.dateLater],  @"should be earlier");
  STAssertFalse([self.dateLater pacoEqualToDate:self.dateEarlier],  @"should be later");
  STAssertTrue([self.dateEarlier pacoEqualToDate:self.dateEarlier],  @"should be equal");
}

- (void)testNoEarlierThan {
  STAssertFalse([self.dateEarlier pacoNoEarlierThanDate:self.dateLater],  @"should be earlier");
  STAssertTrue([self.dateLater pacoNoEarlierThanDate:self.dateEarlier],  @"should be later");
  STAssertTrue([self.dateEarlier pacoNoEarlierThanDate:self.dateEarlier],  @"should be equal");
}

- (void)testNoLaterThan {
  STAssertTrue([self.dateEarlier pacoNoLaterThanDate:self.dateLater],  @"should be earlier");
  STAssertFalse([self.dateLater pacoNoLaterThanDate:self.dateEarlier],  @"should be later");
  STAssertTrue([self.dateEarlier pacoNoLaterThanDate:self.dateEarlier],  @"should be equal");
}

- (void)testCurrentDayAtMidnight {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/07/25 12:33:22-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  NSDate* midnightDate = [testDate pacoCurrentDayAtMidnight];
  
  NSString* testStrMidnight = @"2013/07/25 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:testStrMidnight];
  STAssertNotNil(expect, @"expect should be valid");

  STAssertEqualObjects(midnightDate, expect, @"should be midnight");
}

- (void)testCurrentDayAtMidnight2 {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/07/25 00:00:00-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  NSDate* midnightDate = [testDate pacoCurrentDayAtMidnight];
  
  NSString* testStrMidnight = @"2013/07/25 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:testStrMidnight];
  STAssertNotNil(expect, @"expect should be valid");
  
  STAssertEqualObjects(midnightDate, expect, @"should be midnight");
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
  STAssertNotNil(testDate, @"testDate should be valid");
  NSDate* futureDate = [testDate pacoFutureDateAtMidnightWithInterval:3];

  NSString* futureStr = @"2013/07/28 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  STAssertNotNil(expect, @"expect should be valid");
  
  STAssertEqualObjects(futureDate, expect, @"should be 3 days later at midnight");
}

- (void)testFutureDateAtMidnight2 {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/07/25 13:03:59-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  NSDate* futureDate = [testDate pacoFutureDateAtMidnightWithInterval:8];
  
  NSString* futureStr = @"2013/08/02 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  STAssertNotNil(expect, @"expect should be valid");
  
  STAssertEqualObjects(futureDate, expect, @"8 days later at midnight should be August 2");
}

- (void)testNextDayAtMidnight {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/07/25 13:03:59-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  NSDate* futureDate = [testDate pacoNextDayAtMidnight];
  
  NSString* futureStr = @"2013/07/26 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  STAssertNotNil(expect, @"expect should be valid");
  
  STAssertEqualObjects(futureDate, expect, @"should be one day later at midnight");
}

- (void)testNextDayAtMidnight2 {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/07/31 23:59:59-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  NSDate* futureDate = [testDate pacoNextDayAtMidnight];
  
  NSString* futureStr = @"2013/08/01 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  STAssertNotNil(expect, @"expect should be valid");
  
  STAssertEqualObjects(futureDate, expect, @"should be one day later at midnight");
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
  STAssertNotNil(testDate, @"testDate should be valid");
  NSDate* futureDate = [testDate pacoTimeWithIntervalOfHoursIn24:3 minutes:10 seconds:56];
  
  NSString* futureStr = @"2013/10/12 03:10:56-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  STAssertNotNil(expect, @"expect should be valid");
  
  STAssertEqualObjects(futureDate, expect, @"should be 3 hours and 10 minutes after midnight");
}

- (void)testTimeFromMidnightWithMilliseconds {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 21:34:15-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  
  int hours = 13;
  int minutes = 35;
  int seconds = 56;
  long milliseconds = ((hours*60+minutes)*60+seconds)*1000;
  NSDate* futureDate =
      [testDate pacoTimeFromMidnightWithMilliSeconds:[NSNumber numberWithLong:milliseconds]];
  
  NSString* futureStr = @"2013/10/12 13:35:56-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  STAssertNotNil(expect, @"expect should be valid");
  STAssertEqualObjects(futureDate, expect, @"should be valid");
}

- (void)testTimeFromMidnightWithMilliseconds2 {
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 21:34:15-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  
  int hours = 0;
  int minutes = 0;
  int seconds = 0;
  long milliseconds = ((hours*60+minutes)*60+seconds)*1000;
  NSDate* futureDate =
      [testDate pacoTimeFromMidnightWithMilliSeconds:[NSNumber numberWithLong:milliseconds]];
  
  NSString* futureStr = @"2013/10/12 00:00:00-0700";
  NSDate* expect = [PacoDateUtility pacoDateForString:futureStr];
  STAssertNotNil(expect, @"expect should be valid");
  STAssertEqualObjects(futureDate, expect, @"should be midnight");
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
  NSArray* times = @[[NSNumber numberWithLong:firstMilliseconds],
                     [NSNumber numberWithLong:secondMilliseconds]];
  NSDate* firstTime = [PacoDateUtility pacoDateForString:@"2013/10/12 09:35:50-0700"];
  NSDate* secondTime = [PacoDateUtility pacoDateForString:@"2013/10/12 17:23:44-0700"];

  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 08:34:15-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  STAssertEqualObjects([testDate pacoFirstAvailableTimeWithTimes:times], firstTime,
                       @"should be the first time");
  STAssertTrue([testDate pacoCanScheduleTimes:times], @"should be able to schedule times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 17:23:00-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  STAssertEqualObjects([testDate pacoFirstAvailableTimeWithTimes:times], secondTime,
                       @"should be the second time");
  STAssertTrue([testDate pacoCanScheduleTimes:times], @"should be able to schedule times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 17:23:44-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  STAssertEqualObjects([testDate pacoFirstAvailableTimeWithTimes:times], secondTime,
                       @"should be the second time");
  STAssertTrue([testDate pacoCanScheduleTimes:times], @"should be able to schedule times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 17:23:45-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  STAssertNil([testDate pacoFirstAvailableTimeWithTimes:times], @"should be nil");
  STAssertFalse([testDate pacoCanScheduleTimes:times], @"should not be able to schedule times");
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
  NSArray* times = @[[NSNumber numberWithLong:firstMilliseconds],
                     [NSNumber numberWithLong:secondMilliseconds],
                     [NSNumber numberWithLong:thirdMilliseconds]];
  NSDate* firstTime = [PacoDateUtility pacoDateForString:@"2013/10/12 09:35:50-0700"];
  NSDate* secondTime = [PacoDateUtility pacoDateForString:@"2013/10/12 12:13:22-0700"];
  NSDate* thirdTime = [PacoDateUtility pacoDateForString:@"2013/10/12 17:23:44-0700"];
  
  NSDate* testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 08:34:15-0700"];
  NSDate* endDate = [PacoDateUtility pacoDateForString:@"2013/10/13 00:00:00-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  STAssertNotNil(endDate, @"endDate should be valid");
  NSArray* expect = @[firstTime, secondTime, thirdTime];
  STAssertEqualObjects([testDate pacoDatesToScheduleWithTimes:times andEndDate:endDate], expect,
                       @"should be able to schedule three times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 10:34:15-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  expect = @[secondTime, thirdTime];
  STAssertEqualObjects([testDate pacoDatesToScheduleWithTimes:times andEndDate:endDate], expect,
                       @"should be able to schedule two times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 13:34:15-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  expect = @[thirdTime];
  STAssertEqualObjects([testDate pacoDatesToScheduleWithTimes:times andEndDate:endDate], expect,
                       @"should be able to schedule one times");

  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 17:34:15-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  expect = @[];
  STAssertEqualObjects([testDate pacoDatesToScheduleWithTimes:times andEndDate:endDate], expect,
                       @"should not be able to schedule any time");
  
  
  testDate = [PacoDateUtility pacoDateForString:@"2013/10/12 08:34:15-0700"];
  endDate = [PacoDateUtility pacoDateForString:@"2013/10/12 00:00:00-0700"];
  STAssertNotNil(testDate, @"testDate should be valid");
  STAssertNotNil(endDate, @"endDate should be valid");
  expect = @[];
  STAssertEqualObjects([testDate pacoDatesToScheduleWithTimes:times andEndDate:endDate], expect,
                       @"should not be able to schedule any time since it passed the endDate");
}

- (void)testPacoOnSameDayWithDate {
  STFail(@"No implementation for \"%s\"", __PRETTY_FUNCTION__);
}

- (void)testPacoIsWeekend {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  STAssertNotNil(timeZone, @"timezone should be valid");
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
  STAssertFalse(isWeekend, @"should not be weekend");
  
  //Sat, Nov 2, 2013, 12:00:00
  [comp setMonth:11];
  [comp setDay:2];
  date = [gregorian dateFromComponents:comp];
  isWeekend = [date pacoIsWeekend];
  STAssertTrue(isWeekend, @"should be weekend");

  //Sun, Nov 10, 2013, 23:59:59
  [comp setMonth:11];
  [comp setDay:10];
  date = [gregorian dateFromComponents:comp];
  isWeekend = [date pacoIsWeekend];
  STAssertTrue(isWeekend, @"should be weekend");
}

- (void)testPacoFirstFutureNonWeekendDate {
  STFail(@"No implementation for \"%s\"", __PRETTY_FUNCTION__);
}


- (void)testPacoDateByAddingMinutesInterval {
  STFail(@"No implementation for \"%s\"", __PRETTY_FUNCTION__);
}


@end
