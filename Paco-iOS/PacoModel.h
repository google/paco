 

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

/* delete this */

- (NSArray* )loadExperimentDefinitionsFromFileWithJson;

@end

