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
#import "PacoEnumerator.h"


static const int kPacoDefaultEnumeratorLimit = 20;

@interface PacoEnumerator ()

@property(nonatomic, assign) NSUInteger limit;
@property(nonatomic, copy) NSString* cursor;

@end

@implementation PacoEnumerator

- (id)initWithLimit:(NSUInteger)limit {
  self = [super init];
  if (self) {
    _limit = (limit > 0 ? limit : kPacoDefaultEnumeratorLimit);
  }
  return self;
}

- (void)updateCursor:(NSString*)cursor numOfResults:(int)numOfResults {
  if (!cursor || numOfResults < self.limit || 0 == numOfResults) { //last page
    self.cursor = (NSString*)[NSNull null];
  } else {
    NSAssert([cursor length] > 0, @"cursor should be valid");
    self.cursor = cursor;
  }
}


- (BOOL)hasMoreItems {
  return ![self.cursor isEqual:[NSNull null]];
}



@end
