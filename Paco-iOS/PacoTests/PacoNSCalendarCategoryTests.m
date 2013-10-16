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
#import "NSCalendar+Paco.h"
#import "PacoDateUtility.h"

@interface PacoNSCalendarCategoryTests : SenTestCase
@property(nonatomic, strong) NSCalendar* calendar;
@end

@implementation PacoNSCalendarCategoryTests

- (void)setUp {
  [super setUp];
  // Put setup code here; it will be run once, before the first test case.
  self.calendar = [NSCalendar pacoGregorianCalendar];
  STAssertNotNil(self.calendar, @"calendar should be valid!");
  STAssertEqualObjects(self.calendar.calendarIdentifier, NSGregorianCalendar, @"should be gregorian");
}

- (void)tearDown {
  // Put teardown code here; it will be run once, after the last test case.
  [super tearDown];
}

- (void)testPacoDaysForSameDay {
  NSDate* firstTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 00:00:00-0700"];
  STAssertNotNil(firstTestDate, @"firstTestDate should be valid");
  NSDate* secondTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 23:59:59-0700"];
  STAssertNotNil(secondTestDate, @"secondTestDate should be valid");
  STAssertEquals([self.calendar pacoDaysFromDate:firstTestDate toDate:secondTestDate], 0, @"should be 0 day in between");
}

- (void)testPacoDaysForNeighboringDay {
  NSDate* firstTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 00:00:00-0700"];
  STAssertNotNil(firstTestDate, @"firstTestDate should be valid");
  NSDate* secondTestDate = [PacoDateUtility pacoDateForString:@"2013/10/02 23:59:59-0700"];
  STAssertNotNil(secondTestDate, @"secondTestDate should be valid");
  STAssertEquals([self.calendar pacoDaysFromDate:firstTestDate toDate:secondTestDate], 1, @"should be 1 day in between");
}

- (void)testPacoDaysFromDate1 {
  NSDate* firstTestDate = [PacoDateUtility pacoDateForString:@"2013/09/25 13:03:59-0700"];
  STAssertNotNil(firstTestDate, @"firstTestDate should be valid");
  NSDate* secondTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 08:03:59-0700"];
  STAssertNotNil(secondTestDate, @"secondTestDate should be valid");
  STAssertEquals([self.calendar pacoDaysFromDate:firstTestDate toDate:secondTestDate], 6, @"should be 6 days in between");
}

- (void)testPacoDaysFromDate2 {
  NSDate* firstTestDate = [PacoDateUtility pacoDateForString:@"2013/09/25 13:03:59-0700"];
  STAssertNotNil(firstTestDate, @"firstTestDate should be valid");
  NSDate* secondTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 15:03:59-0700"];
  STAssertNotNil(secondTestDate, @"secondTestDate should be valid");
  STAssertEquals([self.calendar pacoDaysFromDate:firstTestDate toDate:secondTestDate], 6, @"should be 6 days in between");
}

- (void)testPacoDaysFromDate3 {
  NSDate* secondTestDate = [PacoDateUtility pacoDateForString:@"2013/10/01 15:03:59-0700"];
  STAssertNotNil(secondTestDate, @"secondTestDate should be valid");
  NSDate* thirdTestDate = [PacoDateUtility pacoDateForString:@"2013/11/02 08:03:59-0700"];
  STAssertNotNil(thirdTestDate, @"thirdTestDate should be valid");
  STAssertEquals([self.calendar pacoDaysFromDate:secondTestDate toDate:thirdTestDate], 32, @"should be 32 days in between");
}



@end
