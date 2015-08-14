//
//  PacoModelExtended.m
//  Paco
//
//  Created by northropo on 8/12/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoModelExtended.h"
#import "PacoExtendedClient.h"
#import "PacoScheduler.h"
#import "Schedule.h"
#import "PacoExperimentExtended.h"

static NSString* const kPacoKeyHasRunningExperimentsExtended = @"has_running_experiments";

NSString* const kPacoNotificationLoadedMyDefinitionsExtended = @"kPacoNotificationLoadedMyDefinitions";
NSString* const kPacoNotificationLoadedRunningExperimentsExtended = @"kPacoNotificationLoadedRunningExperiments";
NSString* const kPacoNotificationRefreshedMyDefinitionsExtended = @"kPacoNotificationRefreshedMyDefinitions";
NSString* const kPacoNotificationAppBecomeActiveExtended = @"kPacoNotificationAppBecomeActive";

static NSString* kPacoDefinitionPlistName = @"definitions.plist";
static NSString* kPacoExperimentPlistName = @"instances.plist";



@interface PacoModelExtended ()
@property (retain) NSArray *myDefinitions;  // <PacoExperimentDefinition>
@property (retain) NSArray *runningExperiments;  // <PacoExperiment>
@end

@implementation PacoModelExtended


#pragma mark Experiment Instance operations
- (PacoExperimentExtended*)addExperimentWithDefinition:(PAExperimentDAO *)definition
                                      schedule:(PASchedule *)schedule {
    @synchronized(self) {
        
        //create an experiment instance
        NSDate* nowdate = [NSDate dateWithTimeIntervalSinceNow:0];
        PacoExperimentExtended * experimentInstance = [PacoExperimentExtended experimentWithDefinition:definition
                                                                             schedule:schedule
                                                                             joinTime:nowdate];
        //add it to instances array and save the instance file
        NSMutableArray* newInstances = [NSMutableArray arrayWithArray:self.runningExperiments];
        [newInstances addObject:experimentInstance];
        self.runningExperiments = [NSArray arrayWithArray:newInstances];
        
        [[NSUserDefaults standardUserDefaults] setBool:YES forKey:kPacoKeyHasRunningExperimentsExtended];
        [[NSUserDefaults standardUserDefaults] synchronize];
        
        [self saveExperimentInstancesToFile];
        return experimentInstance;
    }
   
}


- (BOOL)saveExperimentDefinitionListJson:(id)definitionsJson
{
    return TRUE;
    
}
- (BOOL)saveExperimentDefinitionsToFile
{
    return TRUE;
    
}
- (BOOL)saveExperimentInstancesToFile
{
    return TRUE;
    
}

@end
