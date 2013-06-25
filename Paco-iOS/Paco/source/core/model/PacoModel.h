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



@interface PacoExperimentDefinition : NSObject
@property (retain) NSArray *admins;  // <NSString>
@property (copy) NSString *creator;
@property (assign) BOOL deleted;
@property (copy) NSString *experimentDescription;
@property (retain) NSArray *feedback;  // <PacoExperimentFeedback>
@property (assign) BOOL fixedDuration;
@property (copy) NSString *experimentId;
@property (copy) NSString *informedConsentForm;
@property (retain) NSArray *inputs;  // <PacoExperimentInput>
@property (assign) long long modifyDate;
@property (assign) BOOL published;
@property (retain) NSArray *publishedUsers;  // <NSString>
@property (assign) BOOL questionsChange;
@property (retain) PacoExperimentSchedule *schedule;
@property (copy) NSString *title;
@property (assign) BOOL webReccommended;
@property (retain) id jsonObject;
+ (id)pacoExperimentDefinitionFromJSON:(id)jsonObject;
- (void)tagQuestionsForDependencies;
@end

@interface PacoEvent : NSObject
@property (copy) NSString *who;
@property (retain) NSDate *when;
@property (assign) long long latitude;
@property (assign) long long longitude;
@property (retain) NSDate *responseTime;
@property (retain) NSDate *scheduledTime;
@property (readonly, copy) NSString *appId;
@property (readonly, copy) NSString *pacoVersion;
@property (copy) NSString *experimentId;
@property (copy) NSString *experimentName;
@property (retain) NSArray *responses;  // <NSDictionary>
@property (retain) id jsonObject;
+ (id)pacoEventForIOS;
+ (id)pacoEventFromJSON:(id)jsonObject;
- (id)jsonObject;
- (id)generateJsonObject;
@end

@interface PacoExperiment : NSObject
@property (retain) PacoExperimentDefinition *definition;
@property (retain) NSArray *events;
@property (copy) NSString *instanceId;
@property (assign) long long lastEventQueryTime;
@property (retain) PacoExperimentSchedule *schedule;  // Override schedule from definition.
//@property (retain) PacoExperimentSchedule *overrideSchedule;  // Override schedule from definition.
@property (retain) id jsonObject;

- (id)serializeToJSON;
- (void)deserializeFromJSON:(id)json model:(PacoModel *)model;

- (BOOL)haveJoined;
@end

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

