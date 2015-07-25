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
#import "PacoService.h"
#import "PacoClient.h" 




@interface PacoExperimentTests : XCTestCase

@property (nonatomic, retain, readonly) PacoService *service;

@end

@implementation PacoExperimentTests

- (void)setUp {
  [super setUp];
    
    
    _service=[[PacoService alloc] init];
    
  // Put setup code here; it will be run once, before the first test case.
}

- (void)tearDown {
  // Put teardown code here; it will be run once, after the last test case.
  [super tearDown];
}

-(void) testServer
{
    
    
    
}

@end
