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
#import "NSString+Paco.h"
#import "NSError+Paco.h"

@interface PacoNSErrorCategoryTests : XCTestCase

@end

@implementation PacoNSErrorCategoryTests

- (void)setUp {
  [super setUp];
  // Put setup code here; it will be run once, before the first test case.
}

- (void)tearDown {
  // Put teardown code here; it will be run once, after the last test case.
  [super tearDown];
}

- (void)testNilError {
  NSError* error = nil;
  BOOL isFileNotExist = [error pacoIsFileNotExistError];
  XCTAssertFalse(isFileNotExist, @"nil error should not be file not exist error");
}

- (void)testFileNotExist {
  NSString* filePath = [NSString pacoDocumentDirectoryFilePathWithName:@"unit_test.plist"];
  NSError* error = nil;
  NSData* data = [NSData dataWithContentsOfFile:filePath
                                        options:NSDataReadingMappedIfSafe
                                          error:&error];
  XCTAssertNil(data, @"should be nil");
  XCTAssertTrue([error pacoIsFileNotExistError], @"should be file not exist error");
}

@end
