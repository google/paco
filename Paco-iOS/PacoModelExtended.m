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
#import "NSString+Paco.h"
#import "NSError+Paco.h"
#import "PacoSerializer.h" 
#import "PacoSerializer.h" 
#import "ExperimentDAO.h" 
#import "NSObject+J2objcKVO.h"
#import "PacoSerializeUtil.h"
#include "java/lang/Long.h"


static NSString* const kPacoKeyHasRunningExperimentsExtended = @"has_running_experiments";

NSString* const kPacoNotificationLoadedMyDefinitionsExtended = @"kPacoNotificationLoadedMyDefinitions";
NSString* const kPacoNotificationLoadedRunningExperimentsExtended = @"kPacoNotificationLoadedRunningExperiments";
NSString* const kPacoNotificationRefreshedMyDefinitionsExtended = @"kPacoNotificationRefreshedMyDefinitions";
NSString* const kPacoNotificationAppBecomeActiveExtended = @"kPacoNotificationAppBecomeActive";

static NSString* kPacoDefinitionPlistNameExtended = @"definitions.plist";
static NSString* kPacoExperimentPlistNameExtended  = @"instances.plist";

#define kPacoDefinitionPlistName @"Experiments" 



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


#pragma mark Experiment Instance operations
- (void)addExperimentWithActionDefinition:(PAActionSpecification *) actonSpecification
{
    @synchronized(self) {
        
        //create an experiment instance
        NSDate* nowdate = [NSDate dateWithTimeIntervalSinceNow:0];
        
        //add it to instances array and save the instance file
        NSMutableArray* newInstances = [NSMutableArray arrayWithArray:self.runningExperiments];
        [newInstances addObject:actonSpecification];
        self.runningExperiments = [NSArray arrayWithArray:newInstances];
        
       // [[NSUserDefaults standardUserDefaults] setBool:YES forKey:kPacoKeyHasRunningExperiments];
        [[NSUserDefaults standardUserDefaults] synchronize];
        
        [self saveExperimentInstancesToFile];
        
        
    }
}


- (void)fullyUpdateDefinitionList:(NSArray*)definitionList {
    @synchronized(self) {
        [self saveNewDefinitionList:definitionList];
    }
}


- (PAExperimentDAO *) experimentDefinitionForIdOBject:(JavaLangLong*) experimentId {
    for (PAExperimentDAO *definition in self.myDefinitions) {
        if ( [definition->id__ longValue]== [experimentId longValue]) {
            return definition;
        }
    }
    return nil;
}


- (void)partiallyUpdateDefinitionList:(NSArray*)defintionList {
    @synchronized(self) {
        NSMutableArray* newDefinitionList =
        [NSMutableArray arrayWithCapacity:[self.myDefinitions count]];
        for (PAExperimentDAO* oldDefinition in self.myDefinitions) {
            PAExperimentDAO* definitionToBeAdded = oldDefinition;
            for (PAExperimentDAO* newDefinition in defintionList) {
                if ( [newDefinition->id__ longValue]  == [oldDefinition->id__ longValue] ) {
                    definitionToBeAdded = newDefinition;
                    break;
                }
            }
            [newDefinitionList addObject:definitionToBeAdded];
        }
        [self saveNewDefinitionList:newDefinitionList];
    }
}


- (void)saveNewDefinitionList:(NSArray*)newDefinitions {
    @synchronized(self) {
        
        self.myDefinitions = newDefinitions;
        
       PacoSerializer*  serializer =
        [[PacoSerializer alloc] initWithArrayOfClasses:nil
                              withNameOfClassAttribute:@"nameOfClass"];
        
        [self saveExperimentDefinitionsToFile:serializer];
    }
}




- (PAExperimentDAO *)experimentDefinitionForId:(long) instanceId {
    for (PAExperimentDAO *dao  in self.myDefinitions )
    {
        if ( instanceId ==  [[dao  valueForKey:@"id"] longValue] ) {
            return dao;
        }
    }
    return nil;  
}

- (BOOL)saveExperimentDefinitionsToFile:(PacoSerializer*) serializer
{
    BOOL methodSuccess;
    NSArray * serializedDefinitions = [serializer toJSonStringFromNSArrayOfDefinitionObjects:_myDefinitions];
    NSLog(@"the json %@", serializedDefinitions);
    
    NSString *fileName = [NSString pacoDocumentDirectoryFilePathWithName:kPacoDefinitionPlistName];
    BOOL success =  [serializedDefinitions writeToFile:fileName atomically:YES];
    if (success) {
        
        methodSuccess = YES;
    }
    else
    {
        methodSuccess = NO;
       
    }
    return methodSuccess;
    }

- (BOOL)loadExperimentDefinitionsFromFile {
    
    NSString *fileName = [NSString pacoDocumentDirectoryFilePathWithName:kPacoDefinitionPlistName];
    NSArray* array = [PacoSerializeUtil getClassNames];
    NSArray* definitions = [NSArray arrayWithContentsOfFile:fileName];
    
    NSMutableArray * serializedDefinitions  = [[NSMutableArray alloc] init];

    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:array withNameOfClassAttribute:@"nameOfClass"];
    
    for(NSString* jsonString in serializedDefinitions)
    {
        PAExperimentDAO* dao = ( PAExperimentDAO*) [serializer buildSingleObjectHierarchyFromJSONString:jsonString];
        [serializedDefinitions addObject:dao];
    }
    
    
    self.myDefinitions = serializedDefinitions;
 
  
    
    
    return [definitions count] > 0;
}




@end
