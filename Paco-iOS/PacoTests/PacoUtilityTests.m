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
#import "PacoUtility.h"

@interface PacoUtilityTests : XCTestCase

@end

@implementation PacoUtilityTests

- (void)setUp {
  [super setUp];
  // Put setup code here; it will be run once, before the first test case.
}

- (void)tearDown {
  // Put teardown code here; it will be run once, after the last test case.
  [super tearDown];
}

- (void)testRandomIntegersInRange {
  for (int numOfTests=0; numOfTests < 100; numOfTests++) {
    int hours = 8;
    int totalMinutes = hours * 60;
    int totalNum = 3;
    int minBufferMinutes = 120;
    NSArray* list = [PacoUtility randomIntegersInRange:totalMinutes
                                         numOfIntegers:totalNum
                                             minBuffer:minBufferMinutes];
    int prev = -1;
    XCTAssertEqual((int)[list count], totalNum, @"should generate correct number of integers");
    for (NSNumber* number in list) {
      int num = [number intValue];
      XCTAssertTrue(num >= 0, @"should be larger than 0");
      XCTAssertTrue(num <= totalMinutes, @"should be smaller than or equal to the upperbound");
      XCTAssertTrue(num > prev, @"should be sorted!");
      prev = num;
    }
  }
}

@end
