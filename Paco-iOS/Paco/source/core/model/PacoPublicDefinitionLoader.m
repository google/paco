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
#import "PacoService.h"
#import "PacoNetwork.h"


NSString * const PublicExperimentsID = @"/experiments?public";
NSString * const MyxperimentsID = @"experiments?mine";

@interface PacoPublicDefinitionLoader ()

@property(nonatomic, strong) PacoEnumerator* enumerator;
@property(nonatomic, strong) NSString* collectionID;

@end


@implementation PacoPublicDefinitionLoader

- (id)initWithLimit:(int)limit Collection:(NSString*) collectonID  {
    
  self = [super init];
  if (self) {
      
      self.collectionID=collectonID;
     _enumerator = [[PacoEnumerator alloc] initWithLimit:limit];
      
  }
  return self;
}

+ (id<PacoEnumerator>)enumeratorWithFetchLimit:(int) limit Collection:(NSString*) collection {
  PacoPublicDefinitionLoader* loader = [[PacoPublicDefinitionLoader alloc] initWithLimit:0 Collection:collection];
  return loader;
}

+ (id<PacoEnumerator>) publicExperimentsEnumerator {
    
    PacoPublicDefinitionLoader* loader =
            [[PacoPublicDefinitionLoader alloc] initWithLimit:0 Collection:PublicExperimentsID];
    return loader;
}

+ (id<PacoEnumerator>) myExperimentsEnumerator {
 
    PacoPublicDefinitionLoader* loader =
    [[PacoPublicDefinitionLoader alloc] initWithLimit:0 Collection:PublicExperimentsID];
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
  
  void(^responseBlock)(NSDictionary*, NSString*, NSError*) = ^(NSDictionary* items, NSString* cursor, NSError* error){
    if (!error) {
        [self.enumerator updateCursor:items[@"cursor"] numOfResults:(int)[items[@"results"] count]];
      block(items[@"results"], nil);
    } else {
      block(nil, error);
    }
  };

    [[PacoNetwork sharedInstance].service loadPublicDefinitionListWithCursorAndEndpoint:self.collectionID cursor:self.enumerator.cursor
                                                                    limit:self.enumerator.limit
                                                                    block:responseBlock];
}

/*
 
 
 
 - (void)loadPublicDefinitionListWithCursorAndEndpoint:(NSString*) endPoint cursor:(NSString*)cursor   limit:(NSUInteger)limit block:(PacoPaginatedResponseBlock)block;
 */


@end
