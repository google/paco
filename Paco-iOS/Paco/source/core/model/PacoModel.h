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

extern NSString* const kPacoNotificationLoadedMyDefinitions;
extern NSString* const kPacoNotificationLoadedRunningExperiments;
extern NSString* const kPacoNotificationAppBecomeActive;

@class PacoModel;
@class PacoExperimentSchedule;
@class PacoExperimentDefinition;
@class PacoExperiment;


@interface PacoModel : NSObject
@property (retain, readonly) NSArray *myDefinitions;  // <PacoExperimentDefinition>
@property (retain, readonly) NSArray *runningExperiments;  // <PacoExperiment>

- (PacoExperimentDefinition *)experimentDefinitionForId:(NSString *)experimentId;
- (PacoExperiment *)experimentForId:(NSString *)instanceId;

- (BOOL)hasLoadedMyDefinitions;
- (BOOL)hasLoadedRunningExperiments;
- (BOOL)hasRunningExperiments;

//NOTE: this method should only be called when PacoModel finishes loading running experiments
- (BOOL)shouldTriggerNotificationSystem;

- (void)fullyUpdateDefinitionList:(NSArray*)definitionList;
- (void)partiallyUpdateDefinitionList:(NSArray*)defintionList;

- (BOOL)saveExperimentDefinitionListJson:(id)definitionsJson;
- (BOOL)saveExperimentDefinitionsToFile;
- (BOOL)saveExperimentInstancesToFile;

- (BOOL)isExperimentJoined:(NSString*)definitionId;

/* Operations on the model */


/* adding/removing Experiment Instances */
- (PacoExperiment*)addExperimentWithDefinition:(PacoExperimentDefinition*)definition
                                      schedule:(PacoExperimentSchedule*)schedule;
- (void)deleteExperimentInstance:(PacoExperiment*)experiment;


- (NSArray*)runningExperimentIdList; //<NSString>

- (BOOL)refreshExperimentsWithDefinitionList:(NSArray*)newDefinitionList;

- (void)configureExperiment:(PacoExperiment*)experiment
               withSchedule:(PacoExperimentSchedule*)newSchedule;

@end

