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

static NSString* kPacoDefinitionPlistName = @"definitions.plist";
static NSString* kPacoExperimentPlistName = @"instances.plist";

@interface PacoExperimentSchedule ()
- (id)serializeToJSON;
@end


@interface PacoModel ()
@property (retain, readwrite) NSArray *experimentDefinitions;  // <PacoExperimentDefinition>
@property (retain, readwrite) NSMutableArray *experimentInstances;  // <PacoExperiment>
@end


@implementation PacoModel


#pragma mark Object Lifecycle
//designated initializer
- (id)init
{
  self = [super init];
  if (self) {
    _experimentDefinitions = [NSArray array];
    _experimentInstances = [NSMutableArray array];
  }
  return self;
}

+ (PacoModel *)pacoModelFromFile {
  PacoModel *model = [[PacoModel alloc] init];
  BOOL loaded = [model loadFromFile];
  if (!loaded) {
    return nil;
  }
  return model;
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoModel:%p - experiments=%@>", self, self.experimentInstances];
}


- (void)applyDefinitionJSON:(id)jsonObject {
  // TODO TPE: temporary disabled this comment since it's quite verbose
  // NSLog(@"MODEL DEFINITION JSON = \n%@", jsonObject);
  NSArray *jsonExperiments = jsonObject;
  self.jsonObjectDefinitions = jsonObject;
  //NSMutableArray *experiments = [NSMutableArray array];
  NSMutableArray *definitions = [NSMutableArray array];

  for (id jsonExperiment in jsonExperiments) {
    PacoExperimentDefinition *experimentDefinition =
        [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:jsonExperiment];
    assert(experimentDefinition);
    [definitions addObject:experimentDefinition];
    //PacoExperiment *experiment = [[PacoExperiment alloc] init];
    //experiment.definition = experimentDefinition;
    //experiment.events = nil;
    //[experiment.definition tagQuestionsForDependencies];
    //[experiments addObject:experiment];
  }
  //self.experimentInstances = experiments;
  [self updateExperimentDefinitions:definitions];
}

- (void)applyInstanceJSON:(id)jsonObject {
  NSMutableArray *instances = [NSMutableArray array];
  if (jsonObject == nil) {
    self.experimentInstances = instances;
    return;
  }
  
//  NSLog(@"MODEL INSTANCE JSON = \n%@", jsonObject);
  NSArray *jsonExperiments = jsonObject;
  self.jsonObjectInstances = jsonObject;
  //NSMutableArray *experiments = [NSMutableArray array];

  for (id jsonExperiment in jsonExperiments) {
    PacoExperiment *experiment = [[PacoExperiment alloc] init];
    [experiment deserializeFromJSON:jsonExperiment];
    NSAssert(experiment, @"experiment should be valid");
    [instances addObject:experiment];
    //PacoExperiment *experiment = [[PacoExperiment alloc] init];
    //experiment.definition = experimentDefinition;
    //experiment.events = nil;
    //[experiment.definition tagQuestionsForDependencies];
    //[experiments addObject:experiment];
  }
  //self.experimentInstances = experiments;
  [self updateExperimentInstances:instances];
}

- (BOOL)shouldTriggerNotificationSystem {
  if (0 == self.experimentInstances.count) {
    return NO;
  }
  for (PacoExperiment* experiment in self.experimentInstances) {
    if ([experiment shouldScheduleNotifications]) {
      return YES;
    }
  }
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

- (NSArray *)instancesForExperimentId:(NSString *)experimentId {
  NSMutableArray *array = [NSMutableArray array];
  for (PacoExperiment *instance in self.experimentInstances) {
    if ([instance.definition.experimentId isEqualToString:experimentId]) {
      [array addObject:instance];
    }
  }
  return array;
}

- (void)makeJSONObjectFromExperiments {
  NSMutableArray *experiments = [[NSMutableArray alloc] init];
  for (PacoExperimentDefinition *definition in self.experimentDefinitions) {
    assert(definition.jsonObject);
    [experiments addObject:definition.jsonObject];
  }
  self.jsonObjectDefinitions = experiments;
}

- (void)makeJSONObjectFromInstances {
  NSMutableArray *experiments = [[NSMutableArray alloc] init];
  for (PacoExperiment *experiment in self.experimentInstances) {
    id json = [experiment serializeToJSON];
    experiment.jsonObject = json;
    NSAssert(experiment.jsonObject, @"experiment json should not be nil");
    [experiments addObject:experiment.jsonObject];
  }
  self.jsonObjectInstances = experiments;
}


- (void)cleanAllExperiments {
  self.experimentInstances = [NSMutableArray array];
  self.jsonObjectInstances = nil;
  [self saveExperimentInstancesToFile];
}


- (BOOL)refreshExperiments {
  @synchronized(self) {
    if (0 == [self.experimentInstances count]) {
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
- (BOOL)saveExperimentDefinitionsToFile {
  if (!self.jsonObjectDefinitions) {
    [self makeJSONObjectFromExperiments];
  }
  NSError *jsonError = nil;
  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:self.jsonObjectDefinitions
                                                     options:NSJSONWritingPrettyPrinted
                                                       error:&jsonError];
  if (jsonError) {
    NSLog(@"ERROR serializing to JSON %@", jsonError);
  }
  NSString *fileName = [NSString pacoDocumentDirectoryFilePathWithName:kPacoDefinitionPlistName];
  BOOL success =  [[NSFileManager defaultManager] createFileAtPath:fileName contents:jsonData attributes:nil];
  if (success) {
    NSLog(@"Succeeded to save %@", fileName);
  } else {
    NSLog(@"Failed to save %@", fileName);
  }
  return success;
}

- (BOOL)saveExperimentInstancesToFile {
  [self makeJSONObjectFromInstances];
  NSAssert([self.jsonObjectInstances isKindOfClass:[NSArray class]],
           @"jsonObjectInstances should be an array!");

  NSError *jsonError = nil;
  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:self.jsonObjectInstances
                                                     options:NSJSONWritingPrettyPrinted
                                                       error:&jsonError];
  if (jsonError) {
    NSLog(@"ERROR serializing to JSON %@", jsonError);
  }
  NSString *fileName = [NSString pacoDocumentDirectoryFilePathWithName:kPacoExperimentPlistName];
  BOOL success = [[NSFileManager defaultManager] createFileAtPath:fileName contents:jsonData attributes:nil];
  if (success) {
    NSLog(@"Succeeded to save %@", fileName);
  } else {
    NSLog(@"Failed to save %@", fileName);
  }
  return success;
}


- (BOOL)deleteFile {
  NSString *fileName = [NSString pacoDocumentDirectoryFilePathWithName:kPacoDefinitionPlistName];
  NSError *error = nil;
  if ([[NSFileManager defaultManager] removeItemAtPath:fileName error:&error] != YES) {
    return NO;
  }
  return YES;
}


#pragma mark file reading operations
- (BOOL)loadExperimentDefinitionsFromFile {
  NSString *fileName = [NSString pacoDocumentDirectoryFilePathWithName:kPacoDefinitionPlistName];
  NSLog(@"Loading from %@", fileName);
  
  NSError* error = nil;
  NSData* fileData = [NSData dataWithContentsOfFile:fileName options:NSDataReadingMappedIfSafe error:&error];
  if (error && [error pacoIsFileNotExistError]) {
    NSLog(@"Definition plist doesn't exist.");
    return NO;
  }
  if (error) {
    NSLog(@"Failed to load data for file %@", fileName);
  }
  NSError *jsonError = nil;
  id jsonObj = !fileData ? nil : [NSJSONSerialization JSONObjectWithData:fileData
                                                                 options:NSJSONReadingAllowFragments
                                                                   error:&jsonError];
  if (!jsonObj || jsonError) {
    NSLog(@"Failed to parse definition json");
    return NO;
  }
  // NSLog(@"LOADED DEFINITION JSON FROM FILE \n%@", self.jsonObjectDefinitions);
  NSAssert([jsonObj isKindOfClass:[NSArray class]], @"should be an array");
  NSArray* definitions = jsonObj;
  [self applyDefinitionJSON:definitions];
  return [definitions count] > 0;
}

- (NSError*)loadExperimentInstancesFromFile {
  NSString *fileName = [NSString pacoDocumentDirectoryFilePathWithName:kPacoExperimentPlistName];
  NSLog(@"Loading from %@", fileName);
  
  NSError* error = nil;
  NSData* jsonData = [NSData dataWithContentsOfFile:fileName options:NSDataReadingMappedIfSafe error:&error];
  if (error != nil) {
    //We should ignore error of "No such file or directory"
    if ([error pacoIsFileNotExistError]) {
      NSLog(@"Instances plist doesn't exist.");
      [self applyInstanceJSON:nil];
      return nil;
    }
    
    NSLog(@"[Error]Failed to load instances: %@",
          error.description ? error.description : @"unknown error");
    return error;
  }
  
  if (jsonData == nil) {
    NSLog(@"Loaded 0 instances from file \n");
    [self applyInstanceJSON:nil];
    return nil;
  }
  
  NSError *jsonError = nil;
  id jsonObj = [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingAllowFragments error:&jsonError];
  if (jsonError) {
    NSLog(@"[Error]Failed to parse instances json data: %@",
          error.description ? error.description : @"unknown error");
    return jsonError;
  }
  
  NSAssert([jsonObj isKindOfClass:[NSArray class]], @"jsonObj should be an array!");

  [self applyInstanceJSON:jsonObj];
  NSAssert(self.jsonObjectInstances != nil, @"jsonObjectInstances shouldn't be nil!");
  NSLog(@"Loaded %d instances from file \n", [self.jsonObjectInstances count]);
  return nil;
}

- (BOOL)loadFromFile {
  BOOL success = YES;
  BOOL check1 = [self loadExperimentDefinitionsFromFile];
  success = success && check1;
  NSError* error = [self loadExperimentInstancesFromFile];
  success = success && (error == nil);
  return success;
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

- (void)deleteExperimentDefinition:(PacoExperimentDefinition*)experimentDefinition {
  NSUInteger index = [self.experimentDefinitions indexOfObject:experimentDefinition];
  NSAssert(index != NSNotFound, @"An experiment definition must be in model to be deleted!");
  
  NSMutableArray* definitions = [self.experimentDefinitions mutableCopy];
  [definitions removeObject:experimentDefinition];
  
  self.experimentDefinitions = [NSArray arrayWithArray:definitions];
}

- (void)updateExperimentDefinitions:(NSArray*)definitions {
  self.experimentDefinitions = definitions;
}

#pragma mark Experiment Instance operations
- (PacoExperiment*)addExperimentWithDefinition:(PacoExperimentDefinition *)definition
                                      schedule:(PacoExperimentSchedule *)schedule {
  //create an experiment instance
  PacoExperiment* experimentInstance = [[PacoExperiment alloc] init];
  experimentInstance.schedule = schedule;
  experimentInstance.definition = definition;
  NSDate* nowdate = [NSDate dateWithTimeIntervalSinceNow:0];
  experimentInstance.instanceId = definition.experimentId;
  experimentInstance.lastEventQueryTime = nowdate;
  
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

- (void)updateExperimentInstances:(NSMutableArray*)experiments {
  self.experimentInstances = experiments;
}

- (BOOL)hasRunningExperiments {
  return [self.experimentInstances count] > 0;
}

@end
