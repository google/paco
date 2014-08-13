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

#import <Foundation/Foundation.h>

@class PacoAuthenticator;
@class PacoEvent;
@class PacoExperimentDefinition;
@class PacoExperimentSchedule;


typedef void(^PacoPaginatedResponseBlock)(NSArray* items, NSString* cursor, NSError* error);


@interface PacoService : NSObject

@property (nonatomic, retain) PacoAuthenticator *authenticator;


- (void)loadPublicDefinitionListWithCursor:(NSString*)cursor limit:(NSUInteger)limit block:(PacoPaginatedResponseBlock)block;

- (void)loadFullDefinitionWithID:(NSString*)definitionID andBlock:(void (^)(PacoExperimentDefinition*, NSError*))completionBlock;

//completionBlock takes an array of NSString
- (void)loadFullDefinitionListWithIDs:(NSArray*)idList andBlock:(void (^)(NSArray*, NSError*))completionBlock;

//completionBlock takes an array of PacoExperimentDefinition
- (void)loadMyFullDefinitionListWithBlock:(void (^)(NSArray*, NSError*))completionBlock;

// Batch submit a list of events
- (void)submitEventList:(NSArray*)eventList withCompletionBlock:(void (^)(NSArray*, NSError*))completionBlock;

@end
