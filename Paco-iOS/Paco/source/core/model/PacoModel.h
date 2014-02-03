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

extern NSString* const PacoFinishLoadingDefinitionNotification;
extern NSString* const PacoFinishLoadingExperimentNotification;
extern NSString* const PacoFinishRefreshing;


@class PacoModel;
@class PacoExperimentSchedule;
@class PacoExperimentDefinition;
@class PacoExperiment;


@interface PacoModel : NSObject
//YMZ:TODO: need to think about if it's necessary to use atomic
//for the following properties
@property (retain, readonly) NSArray *experimentDefinitions;  // <PacoExperimentDefinition>
@property (retain, readonly) NSMutableArray *experimentInstances;  // <PacoExperiment>
@property (retain) id jsonObjectDefinitions;
@property (retain) id jsonObjectInstances;

- (PacoExperimentDefinition *)experimentDefinitionForId:(NSString *)experimentId;
- (PacoExperiment *)experimentForId:(NSString *)instanceId;
- (NSArray *)instancesForExperimentId:(NSString *)experimentId;

- (BOOL)shouldTriggerNotificationSystem;

- (BOOL)saveExperimentDefinitionsToFile;
- (BOOL)saveExperimentInstancesToFile;

- (BOOL)loadFromFile;
- (BOOL)deleteFile;

- (void)cleanAllExperiments;


+ (PacoModel *)pacoModelFromFile;
- (BOOL)isExperimentJoined:(NSString*)definitionId;

/* Operations on the model */

/* adding/removing Experiment Definitions */
- (void)addExperimentDefinition:(PacoExperimentDefinition*)experimentDefinition;
- (void)deleteExperimentDefinition:(PacoExperimentDefinition*)experimentDefinition;

/* adding/removing Experiment Instances */
- (PacoExperiment*)addExperimentWithDefinition:(PacoExperimentDefinition*)definition
                                      schedule:(PacoExperimentSchedule*)schedule;
- (void)deleteExperimentInstance:(PacoExperiment*)experiment;


- (BOOL)hasRunningExperiments;

- (BOOL)refreshExperiments;

@end

