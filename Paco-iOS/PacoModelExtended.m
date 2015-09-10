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
 

- (void)fullyUpdateDefinitionList:(NSArray*)definitionList {
    @synchronized(self) {
        [self saveNewDefinitionList:definitionList];
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
