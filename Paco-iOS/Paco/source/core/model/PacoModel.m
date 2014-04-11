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

#import "PacoModel.h"

#import "PacoClient.h"
#import "PacoDateUtility.h"
#import "PacoExperimentFeedback.h"
#import "PacoExperimentInput.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperimentDefinition.h"
#import "PacoEvent.h"
#import "PacoExperiment.h"
#import "NSString+Paco.h"
#import "NSError+Paco.h"

NSString* const PacoFinishLoadingDefinitionNotification = @"PacoFinishLoadingDefinitionNotification";
NSString* const PacoFinishLoadingExperimentNotification = @"PacoFinishLoadingExperimentNotification";
NSString* const PacoFinishRefreshing = @"PacoFinishRefreshing";
NSString* const PacoAppBecomeActive = @"PacoAppBecomeActive";

static NSString* kPacoDefinitionPlistName = @"definitions.plist";
static NSString* kPacoExperimentPlistName = @"instances.plist";



@interface PacoModel ()
@property (retain) NSArray *experimentDefinitions;  // <PacoExperimentDefinition>
@property (retain) NSMutableArray *experimentInstances;  // <PacoExperiment>
@end


@implementation PacoModel


#pragma mark Object Lifecycle
//designated initializer
- (id)init {
  self = [super init];
  if (self) {
    _experimentDefinitions = @[];
    _experimentInstances = [NSMutableArray array];
  }
  return self;
}


- (NSString*)description {
  return [NSString stringWithFormat:@"<PacoModel:%p - experiments=%@>", self, self.experimentInstances];
}


- (void)applyDefinitionJSON:(id)jsonObject {
  //NSLog(@"MODEL DEFINITION JSON = \n%@", jsonObject);
  NSArray *jsonExperiments = jsonObject;
  NSMutableArray *definitions = [NSMutableArray array];

  for (id jsonExperiment in jsonExperiments) {
    PacoExperimentDefinition *experimentDefinition =
        [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:jsonExperiment];
    NSAssert(experimentDefinition, @"definition should be created successfully");
    [definitions addObject:experimentDefinition];
  }
  self.experimentDefinitions = definitions;
}

- (void)applyInstanceJSON:(id)jsonObject {
  NSMutableArray *instances = [NSMutableArray array];
  if (jsonObject == nil) {
    self.experimentInstances = instances;
    return;
  }
  
//  NSLog(@"MODEL INSTANCE JSON = \n%@", jsonObject);
  NSArray *jsonExperiments = jsonObject;

  for (id jsonExperiment in jsonExperiments) {
    PacoExperiment *experiment = [[PacoExperiment alloc] init];
    [experiment deserializeFromJSON:jsonExperiment];
    NSAssert(experiment, @"experiment should be valid");
    [instances addObject:experiment];
  }
  self.experimentInstances = instances;
}

- (BOOL)areRunningExperimentsLoaded {
  return self.experimentInstances != nil;
}

- (BOOL)shouldTriggerNotificationSystem {
  if (![self areRunningExperimentsLoaded]) {
    DDLogError(@"Running experiments are not loaded yet!");
  }
  
  if (![self hasRunningExperiments]) {
    DDLogInfo(@"No running experiments.");
    return NO;
  }
  for (PacoExperiment* experiment in self.experimentInstances) {
    if ([experiment shouldScheduleNotificationsFromNow]) {
      return YES;
    }
  }
  DDLogInfo(@"There are %lu running experiments, none of them should schedule notifications.",
            (unsigned long)[self.experimentInstances count]);
  return NO;
}


- (PacoExperimentDefinition *)experimentDefinitionForId:(NSString *)experimentId {
  for (PacoExperimentDefinition *definition in self.experimentDefinitions) {
    if ([definition.experimentId isEqualToString:experimentId]) {
      return definition;
    }
  }
  return nil;
}

- (PacoExperiment *)experimentForId:(NSString *)instanceId {
  for (PacoExperiment *instance in self.experimentInstances) {
    if ([instance.instanceId isEqualToString:instanceId]) {
      return instance;
    }
  }
  return nil;  
}


- (id)makeJSONObjectFromInstances {
  NSMutableArray *experiments = [[NSMutableArray alloc] init];
  for (PacoExperiment *experiment in self.experimentInstances) {
    id json = [experiment serializeToJSON];
    NSAssert(json, @"experiment json should not be nil");
    [experiments addObject:json];
  }
  return experiments;
}


- (BOOL)refreshExperiments {
  @synchronized(self) {
    if (![self hasRunningExperiments]) {
      return NO;
    }

    BOOL schedulesChanged = NO;
    for (PacoExperiment* experiment in self.experimentInstances) {
      NSString* definitionId = experiment.definition.experimentId;
      NSAssert(definitionId, @"definitionId should be valid");
      
      PacoExperimentDefinition* newDefinition = [self experimentDefinitionForId:definitionId];
      PacoExperimentDefinition* oldDefinition = experiment.definition;
      NSAssert(oldDefinition, @"oldDefinition should be valid");

      /*
       In the following 3 cases, definition won't be returned from server:
       a. definition expires 
       b. definition was purged by administrator
       c. definition is no longer published to this user
       **/
      if (newDefinition) {
        //replace with the new definition, if newDefinition is nil, then keep the old definition
        experiment.definition = newDefinition;
      }
      
      //if it's a self report schedule, no need to modify the notification system
      if ([oldDefinition.schedule isSelfReport] &&
          (!newDefinition || (newDefinition && [newDefinition.schedule isSelfReport]))) {
        continue;
      }
      //if newDefinition is nil, then we will still keep the old definition and old schedule
      //nothing needs to be changed for this experiment
      if (!newDefinition) {
        continue;
      }
      
      BOOL refreshed = [experiment refreshWithSchedule:newDefinition.schedule];
      if (refreshed) {
        schedulesChanged = YES;
      }
      if (![newDefinition hasSameDurationWithDefinition:oldDefinition]) {
        schedulesChanged = YES;
      }
    }
    
    [self saveExperimentInstancesToFile];
    return schedulesChanged;
  }
}


#pragma mark file writing operations
- (BOOL)saveExperimentDefinitionListJson:(id)definitionsJson {
  NSError *jsonError = nil;
  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:definitionsJson
                                                     options:NSJSONWritingPrettyPrinted
                                                       error:&jsonError];
  if (jsonError) {
    DDLogError(@"ERROR serializing to JSON %@", jsonError);
  }
  NSString *fileName = [NSString pacoDocumentDirectoryFilePathWithName:kPacoDefinitionPlistName];
  BOOL success =  [[NSFileManager defaultManager] createFileAtPath:fileName contents:jsonData attributes:nil];
  if (success) {
    DDLogInfo(@"Succeeded to save %@", fileName);
  } else {
    DDLogError(@"Failed to save %@", fileName);
  }
  return success;
}


- (BOOL)saveExperimentInstancesToFile {
  id instanceListJson = [self makeJSONObjectFromInstances];
  NSAssert([instanceListJson isKindOfClass:[NSArray class]],
           @"instanceListJson should be an array!");

  NSError *jsonError = nil;
  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:instanceListJson
                                                     options:NSJSONWritingPrettyPrinted
                                                       error:&jsonError];
  if (jsonError) {
    DDLogError(@"ERROR serializing to JSON %@", jsonError);
  }
  NSString *fileName = [NSString pacoDocumentDirectoryFilePathWithName:kPacoExperimentPlistName];
  BOOL success = [[NSFileManager defaultManager] createFileAtPath:fileName contents:jsonData attributes:nil];
  if (success) {
    DDLogInfo(@"Succeeded to save %@", fileName);
  } else {
    DDLogError(@"Failed to save %@", fileName);
  }
  return success;
}


#pragma mark file reading operations
- (BOOL)loadExperimentDefinitionsFromFile {
  NSString *fileName = [NSString pacoDocumentDirectoryFilePathWithName:kPacoDefinitionPlistName];
  DDLogInfo(@"Loading from %@", fileName);
  
  NSError* error = nil;
  NSData* fileData = [NSData dataWithContentsOfFile:fileName options:NSDataReadingMappedIfSafe error:&error];
  if (error && [error pacoIsFileNotExistError]) {
    DDLogWarn(@"Definition plist doesn't exist.");
    return NO;
  }
  if (error) {
    DDLogError(@"Failed to load data for file %@", fileName);
  }
  NSError *jsonError = nil;
  id jsonObj = !fileData ? nil : [NSJSONSerialization JSONObjectWithData:fileData
                                                                 options:NSJSONReadingAllowFragments
                                                                   error:&jsonError];
  if (!jsonObj || jsonError) {
    DDLogError(@"Failed to parse definition json");
    return NO;
  }
  NSAssert([jsonObj isKindOfClass:[NSArray class]], @"should be an array");
  NSArray* definitions = jsonObj;
  [self applyDefinitionJSON:definitions];
  return [definitions count] > 0;
}

- (NSError*)loadExperimentInstancesFromFile {
  NSString *fileName = [NSString pacoDocumentDirectoryFilePathWithName:kPacoExperimentPlistName];
  DDLogInfo(@"Loading from %@", fileName);
  
  NSError* error = nil;
  NSData* jsonData = [NSData dataWithContentsOfFile:fileName options:NSDataReadingMappedIfSafe error:&error];
  if (error != nil) {
    //We should ignore error of "No such file or directory"
    if ([error pacoIsFileNotExistError]) {
      DDLogWarn(@"Instances plist doesn't exist.");
      [self applyInstanceJSON:nil];
      return nil;
    }
    
    DDLogError(@"[Error]Failed to load instances: %@",
          error.description ? error.description : @"unknown error");
    return error;
  }
  
  if (jsonData == nil) {
    DDLogInfo(@"Loaded 0 instances from file \n");
    [self applyInstanceJSON:nil];
    return nil;
  }
  
  NSError *jsonError = nil;
  id jsonObj = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingAllowFragments error:&jsonError];
  if (jsonError) {
    DDLogError(@"[Error]Failed to parse instances json data: %@",
          error.description ? error.description : @"unknown error");
    return jsonError;
  }
  
  NSAssert([jsonObj isKindOfClass:[NSArray class]], @"jsonObj should be an array!");

  [self applyInstanceJSON:jsonObj];
  DDLogInfo(@"Loaded %lu instances from file \n", (unsigned long)[jsonObj count]);
  return nil;
}


//YMZ: TODO: this method may need to be re-designed to be more efficient
- (BOOL)isExperimentJoined:(NSString*)definitionId {
  if (0 == [definitionId length]) {
    return NO;
  }
  
  for (PacoExperiment* experiment in self.experimentInstances) {
    NSAssert([experiment.definition.experimentId length] > 0, @"experimentId is invalid!");
    if ([experiment.definition.experimentId isEqualToString:definitionId]) {
      return YES;
    }
  }
  return NO;
}

#pragma mark Experiment Definition operations
- (void)addExperimentDefinition:(PacoExperimentDefinition*)experimentDefinition {
  NSMutableArray* definitions = [self.experimentDefinitions mutableCopy];
  [definitions insertObject:experimentDefinition atIndex:0];
  
  self.experimentDefinitions = [NSArray arrayWithArray:definitions];
}


#pragma mark Experiment Instance operations
- (PacoExperiment*)addExperimentWithDefinition:(PacoExperimentDefinition *)definition
                                      schedule:(PacoExperimentSchedule *)schedule {
  //create an experiment instance
  NSDate* nowdate = [NSDate dateWithTimeIntervalSinceNow:0];
  PacoExperiment* experimentInstance = [PacoExperiment experimentWithDefinition:definition
                                                                       schedule:schedule
                                                                       joinTime:nowdate];
  //add it to instances array and save the instance file
  [self.experimentInstances addObject:experimentInstance];
  [self saveExperimentInstancesToFile];
  return experimentInstance;
}


- (void)deleteExperimentInstance:(PacoExperiment*)experiment {
  NSUInteger index = [self.experimentInstances indexOfObject:experiment];
  NSAssert(index != NSNotFound, @"An experiment must be in model to be deleted!");
  [self.experimentInstances removeObject:experiment];
  [self saveExperimentInstancesToFile];
}


- (BOOL)hasRunningExperiments {
  return [self.experimentInstances count] > 0;
}

@end
