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

extern NSString* const PacoExperimentDefinitionUpdateNotification;
extern NSString* const PacoExperimentInstancesUpdateNotification;


@class PacoModel;
@class PacoExperimentSchedule;
@class PacoExperimentDefinition;
@class PacoExperiment;


@interface PacoModel : NSObject
@property (retain) NSArray *experimentDefinitions;  // <PacoExperimentDefinition>
@property (retain) NSMutableArray *experimentInstances;  // <PacoExperiment>
@property (retain) id jsonObjectDefinitions;
@property (retain) id jsonObjectInstances;

- (PacoExperimentDefinition *)experimentDefinitionForId:(NSString *)experimentId;
- (PacoExperiment *)experimentForId:(NSString *)instanceId;
- (NSArray *)instancesForExperimentId:(NSString *)experimentId;
- (PacoExperiment *)addExperimentInstance:(PacoExperimentDefinition *)definition
                                 schedule:(PacoExperimentSchedule *)schedule
                                   events:(NSArray *)events;
- (void)addExperimentsWithDefinition:(PacoExperimentDefinition*)definition events:(NSArray*)events;

+ (id)pacoModelFromDefinitionJSON:(id)jsonDefintions
                     instanceJSON:(id)jsonInstances;

- (NSArray *)joinedExperiments;

- (BOOL)saveToFile;
- (BOOL)loadFromFile;
- (BOOL)deleteFile;

+ (PacoModel *)pacoModelFromFile;

@end

