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

@interface PacoClientTests : SenTestCase

@end

@implementation PacoClientTests

- (void)setUp
{
    [super setUp];
    // Put setup code here; it will be run once, before the first test case.
}

- (void)tearDown
{
    // Put teardown code here; it will be run once, after the last test case.
    [super tearDown];
}

- (void)testOSVersion
{
  NSString* version = @"7.0.0";
  float floatVersion = [version floatValue];
  STAssertEquals(floatVersion, (float)7.0, @"version number should be correct.");
  STAssertTrue(floatVersion >= 7.0, @"version number should be correct.");

  version = @"7.0.1";
  floatVersion = [version floatValue];
  STAssertEquals(floatVersion, (float)7.0, @"version number should be correct.");

  version = @"7.2.1";
  floatVersion = [version floatValue];
  STAssertEquals(floatVersion, (float)7.2, @"version number should be correct.");
  STAssertTrue(floatVersion >= 7.0, @"version number should be correct.");

  version = @"7.2.0";
  floatVersion = [version floatValue];
  STAssertEquals(floatVersion, (float)7.2, @"version number should be correct.");
  
  version = @"6.2.3";
  floatVersion = [version floatValue];
  STAssertEquals(floatVersion, (float)6.2, @"version number should be correct.");
  STAssertFalse(floatVersion >= 7.0, @"version number should be correct.");
  
  version = @"6.0.0";
  floatVersion = [version floatValue];
  STAssertEquals(floatVersion, (float)6.0, @"version number should be correct.");
  STAssertFalse(floatVersion >= 7.0, @"version number should be correct.");
  
  version = @"6.3.3.6";
  floatVersion = [version floatValue];
  STAssertEquals(floatVersion, (float)6.3, @"version number should be correct.");
}

@end
