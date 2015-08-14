//
//  PacoModelExtended.h
//  Paco
//
//  Created by northropo on 8/12/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PacoExperimentExtended.h" 



@class PAExperimentDAO;
@class PASchedule;


@interface PacoModelExtended : NSObject


@property (retain, readonly) NSArray *myDefinitions;  // <PacoExperimentDefinition>
@property (retain, readonly) NSArray *runningExperiments;  // <PacoExperiment>

- (PAExperimentDAO *)experimentDefinitionForId:(NSString *)experimentId;
- (PacoExperimentExtended *)experimentForId:(NSString *)instanceId;

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
- (PacoExperimentExtended*) addExperimentWithDefinition:(PAExperimentDAO*)definition
                                      schedule:(PASchedule*)schedule;
- (void)deleteExperimentInstance:(PacoExperimentExtended*)experiment;


- (NSArray*)runningExperimentIdList; //<NSString>

- (BOOL)refreshExperimentsWithDefinitionList:(NSArray*)newDefinitionList;

- (void)configureExperiment:(PacoExperimentExtended*)experiment
               withSchedule:(PASchedule*)newSchedule;

/* delete this */

- (NSArray* )loadExperimentDefinitionsFromFileWithJson;


@end
