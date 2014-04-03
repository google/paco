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

#import "PacoPublicDefinitionLoader.h"
#import "PacoEnumerator.h"
#import "PacoClient.h"
#import "PacoService.h"



@interface PacoPublicDefinitionLoader ()

@property(nonatomic, strong) PacoEnumerator* enumerator;

@end


@implementation PacoPublicDefinitionLoader

- (id)initWithLimit:(int)limit {
  self = [super init];
  if (self) {
    _enumerator = [[PacoEnumerator alloc] initWithLimit:limit];
  }
  return self;
}

+ (id<PacoEnumerator>)enumerator {
  PacoPublicDefinitionLoader* loader = [[PacoPublicDefinitionLoader alloc] initWithLimit:0];
  return loader;
}


#pragma mark PacoEnumerator protocol
- (BOOL)hasMoreItems {
  return [self.enumerator hasMoreItems];
}

- (void)loadNextPage:(void(^)(NSArray*, NSError*))block {
  if (![self.enumerator hasMoreItems]) {
    block(nil, nil);
    return;
  }
  
  void(^responseBlock)(NSArray*, NSString*, NSError*) = ^(NSArray* items, NSString* cursor, NSError* error){
    if (!error) {
      [self.enumerator updateCursor:cursor numOfResults:(int)[items count]];
      block(items, nil);
    } else {
      block(nil, error);
    }
  };

  [[PacoClient sharedInstance].service loadPublicDefinitionListWithCursor:self.enumerator.cursor
                                                                    limit:self.enumerator.limit
                                                                    block:responseBlock];
}


@end
