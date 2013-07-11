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
    // create a NSDate date for use in our tests
    NSDateComponents *comps = [[NSDateComponents alloc] init];
    [comps setDay:2];
    [comps setMonth:7];
    [comps setYear:1989];
    
    NSCalendar *gregorian = [[NSCalendar alloc]
                             initWithCalendarIdentifier:NSGregorianCalendar];
    NSDate *testDate = [gregorian dateFromComponents:comps];

    // this is what the Paco string should look like
    NSString *testDateString = @"1989/07/02 07:00:00+0000";
    
    // *** TESTS ***
    // PacoDate pacoStringForDate:<#(NSDate *)#>
    STAssertEqualObjects([PacoDate pacoStringForDate:testDate], testDateString, @"pacoStringForDate failed");
}

@end
