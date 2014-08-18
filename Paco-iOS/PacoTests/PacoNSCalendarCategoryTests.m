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
#import "NSCalendar+Paco.h"
#import "PacoDateUtility.h"

@interface PacoNSCalendarCategoryTests : XCTestCase
@property(nonatomic, strong) NSCalendar* calendar;
@end

@implementation PacoNSCalendarCategoryTests

- (void)setUp {
  [super setUp];
  // Put setup code here; it will be run once, before the first test case.
  self.calendar = [NSCalendar pacoGregorianCalendar];
  XCTAssertNotNil(self.calendar, @"calendar should be valid!");
  XCTAssertEqualObjects(self.calendar.calendarIdentifier, NSGregorianCalendar, @"should be gregorian");
}

- (void)tearDown {
  // Put teardown code here; it will be run once, after the last test case.
  [super tearDown];
}

- (void)testPacoDaysForSameDay {
  NSDate* firstTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 00:00:00-0700"];
  XCTAssertNotNil(firstTestDate, @"firstTestDate should be valid");
  NSDate* secondTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 23:59:59-0700"];
  XCTAssertNotNil(secondTestDate, @"secondTestDate should be valid");
  XCTAssertEqual([self.calendar pacoDaysFromDate:firstTestDate toDate:secondTestDate], 0, @"should be 0 day in between");
}

- (void)testPacoNegativeDays {
  NSDate* firstTestDate = [PacoDateUtility pacoDateForString:@"2013/10/03 00:00:00-0700"];
  XCTAssertNotNil(firstTestDate, @"firstTestDate should be valid");
  NSDate* secondTestDate = [PacoDateUtility pacoDateForString:@"2013/09/29 23:59:59-0700"];
  XCTAssertNotNil(secondTestDate, @"secondTestDate should be valid");
  XCTAssertEqual([self.calendar pacoDaysFromDate:firstTestDate toDate:secondTestDate], -4, @"should be -4 day in between");
}

- (void)testPacoDaysForNeighboringDay {
  NSDate* firstTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 00:00:00-0700"];
  XCTAssertNotNil(firstTestDate, @"firstTestDate should be valid");
  NSDate* secondTestDate = [PacoDateUtility pacoDateForString:@"2013/10/02 23:59:59-0700"];
  XCTAssertNotNil(secondTestDate, @"secondTestDate should be valid");
  XCTAssertEqual([self.calendar pacoDaysFromDate:firstTestDate toDate:secondTestDate], 1, @"should be 1 day in between");
}

- (void)testPacoDaysFromDate1 {
  NSDate* firstTestDate = [PacoDateUtility pacoDateForString:@"2013/09/25 13:03:59-0700"];
  XCTAssertNotNil(firstTestDate, @"firstTestDate should be valid");
  NSDate* secondTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 08:03:59-0700"];
  XCTAssertNotNil(secondTestDate, @"secondTestDate should be valid");
  XCTAssertEqual([self.calendar pacoDaysFromDate:firstTestDate toDate:secondTestDate], 6, @"should be 6 days in between");
}

- (void)testPacoDaysFromDate2 {
  NSDate* firstTestDate = [PacoDateUtility pacoDateForString:@"2013/09/25 13:03:59-0700"];
  XCTAssertNotNil(firstTestDate, @"firstTestDate should be valid");
  NSDate* secondTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 15:03:59-0700"];
  XCTAssertNotNil(secondTestDate, @"secondTestDate should be valid");
  XCTAssertEqual([self.calendar pacoDaysFromDate:firstTestDate toDate:secondTestDate], 6, @"should be 6 days in between");
}

- (void)testPacoDaysFromDate3 {
  NSDate* secondTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 15:03:59-0700"];
  XCTAssertNotNil(secondTestDate, @"secondTestDate should be valid");
  NSDate* thirdTestDate = [PacoDateUtility pacoDateForString:@"2013/11/02 08:03:59-0700"];
  XCTAssertNotNil(thirdTestDate, @"thirdTestDate should be valid");
  XCTAssertEqual([self.calendar pacoDaysFromDate:secondTestDate toDate:thirdTestDate], 32, @"should be 32 days in between");
}


#pragma mark number of weeks between two dates
- (void)testPacoWeeksAcrossTwoYearsInSameWeek {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:12];
  [comp setDay:30];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Mon, 12/30, 2013, 10:00:00
  NSDate* fromDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");

  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:1];
  [comp setDay:4];
  [comp setHour:18];
  [comp setMinute:0];
  [comp setSecond:0];
  //Sat, 1/4, 2014, 18:00:00
  NSDate* endDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(endDate, @"endDate should be valid");
  XCTAssertEqual([self.calendar pacoWeeksFromDate:fromDate toDate:endDate], 0, @"should be in the same week");
}

- (void)testPacoWeeksAcrossTwoYearsNotInSameWeek {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:12];
  [comp setDay:30];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Mon, 12/30, 2013, 10:00:00
  NSDate* fromDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:1];
  [comp setDay:5];
  [comp setHour:18];
  [comp setMinute:0];
  [comp setSecond:0];
  //Sat, 1/5, 2014, 18:00:00
  NSDate* endDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(endDate, @"endDate should be valid");
  XCTAssertEqual([self.calendar pacoWeeksFromDate:fromDate toDate:endDate], 1, @"should not be in the same week");
}


- (void)testPacoWeeksAcrossTwoYearsThreeWeeksApart {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2013];
  [comp setMonth:12];
  [comp setDay:31];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Mon, 12/31, 2013, 10:00:00
  NSDate* fromDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:1];
  [comp setDay:20];
  [comp setHour:18];
  [comp setMinute:0];
  [comp setSecond:0];
  //Wed, 1/20, 2014, 18:00:00
  NSDate* endDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(endDate, @"endDate should be valid");
  XCTAssertEqual([self.calendar pacoWeeksFromDate:fromDate toDate:endDate], 3, @"should not be in the same week");
}

- (void)testPacoWeeksAcrossTwoMonthsInSameWeek {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:2];
  [comp setDay:26];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Sun, 2/26, 2014, 10:00:00
  NSDate* fromDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:3];
  [comp setDay:1];
  [comp setHour:18];
  [comp setMinute:0];
  [comp setSecond:0];
  //Sat, 3/1, 2014, 18:00:00
  NSDate* endDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(endDate, @"endDate should be valid");
  XCTAssertEqual([self.calendar pacoWeeksFromDate:fromDate toDate:endDate], 0, @"should be in the same week");
}


- (void)testPacoWeeksAcrossTwoMonthsNotInSameWeek {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:2];
  [comp setDay:23];
  [comp setHour:0];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //Sun, 2/23, 2014, 00:00:00
  NSDate* fromDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:3];
  [comp setDay:2];
  [comp setHour:18];
  [comp setMinute:0];
  [comp setSecond:0];
  //Sun, 3/2, 2014, 18:00:00
  NSDate* endDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(endDate, @"endDate should be valid");
  XCTAssertEqual([self.calendar pacoWeeksFromDate:fromDate toDate:endDate], 1, @"should not be in the same week");
}


- (void)testPacoMonthsSameMonth {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:2];
  [comp setDay:1];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //2/1, 2014, 10:00:00
  NSDate* fromDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:2];
  [comp setDay:28];
  [comp setHour:18];
  [comp setMinute:0];
  [comp setSecond:0];
  //2/28, 2014, 18:00:00
  NSDate* endDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(endDate, @"endDate should be valid");
  XCTAssertEqual([self.calendar pacoMonthsFromDate:fromDate toDate:endDate], 0, @"should be in the same month");
}


- (void)testPacoMonthsThreeMonthsApart {
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:2];
  [comp setDay:3];
  [comp setHour:10];
  [comp setMinute:0];
  [comp setSecond:0];
  NSCalendar* gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  //2/3, 2014, 10:00:00
  NSDate* fromDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:5];
  [comp setDay:31];
  [comp setHour:18];
  [comp setMinute:0];
  [comp setSecond:0];
  //Wed, 5/31, 2014, 18:00:00
  NSDate* endDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(endDate, @"endDate should be valid");
  XCTAssertEqual([self.calendar pacoMonthsFromDate:fromDate toDate:endDate], 3, @"should not be 3 months apart");
}

- (void)testPacoMonthsOneYearApart {
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
  NSDate* fromDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  
  [comp setTimeZone:timeZone];
  [comp setYear:2014];
  [comp setMonth:4];
  [comp setDay:1];
  [comp setHour:18];
  [comp setMinute:0];
  [comp setSecond:0];
  //4/01, 2014, 18:00:00
  NSDate* endDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(endDate, @"endDate should be valid");
  XCTAssertEqual([self.calendar pacoMonthsFromDate:fromDate toDate:endDate], 5, @"should 5 month apart");
}


- (void)testPacoMonthsTwoYearsApart {
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
  NSDate* fromDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  
  
  [comp setTimeZone:timeZone];
  [comp setYear:2015];
  [comp setMonth:2];
  [comp setDay:27];
  [comp setHour:18];
  [comp setMinute:0];
  [comp setSecond:0];
  //2/27, 2015, 18:00:00
  NSDate* endDate = [gregorian dateFromComponents:comp];
  XCTAssertNotNil(endDate, @"endDate should be valid");
  XCTAssertEqual([self.calendar pacoMonthsFromDate:fromDate toDate:endDate], 15, @"should 15 months apart");
}






@end
