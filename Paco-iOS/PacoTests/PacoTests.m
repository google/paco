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

#import "PacoTests.h"

#import "PacoDate.h"

@implementation PacoTests

- (void)setUp
{
    [super setUp];
    
    // Set-up code here.
}

- (void)tearDown
{
    // Tear-down code here.
    
    [super tearDown];
}

- (void)testAddition {
    int valueA = 5;
    int valueB = 10;
    
    STAssertTrue(valueA + valueB == 15, @"The expected value of the addition should be 15");
}

// This method should be running different test suites on the PacoDate object
- (void)testPacoDate {
    // This is the NSDate we'll be working with
    int testDay = 2;
    int testMonth = 7;
    int testYear = 1989;
    int testHour = 20;
    int testMinute = 45;
    NSTimeZone *testTimeZone = [NSTimeZone timeZoneWithName:@"Europe/Brussels"];
    
    // create a NSDate date for use in our tests
    NSDateComponents *comps = [[NSDateComponents alloc] init];
    [comps setDay:testDay];
    [comps setMonth:testMonth];
    [comps setYear:testYear];
    [comps setHour:testHour];
    [comps setMinute:testMinute];
    [comps setTimeZone:testTimeZone];
    
    NSCalendar *gregorian = [[NSCalendar alloc]
                             initWithCalendarIdentifier:NSGregorianCalendar];
    NSDate *testDate = [gregorian dateFromComponents:comps];
 
    // *** TESTS ***
    // PacoDate pacoStringForDate:<#(NSDate *)#>
    // Keep in mind that pacoStringForDate will convert the datetime to the local timezone of the iOS device
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy/MM/dd HH:mm:ssZ"];
    STAssertEqualObjects([PacoDate pacoStringForDate:testDate], [dateFormatter stringFromDate:testDate], @"pacoStringForDate failed");
}

@end
