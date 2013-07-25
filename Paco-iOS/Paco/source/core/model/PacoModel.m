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
#import "PacoDate.h"
#import "PacoExperimentFeedback.h"
#import "PacoExperimentInput.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperimentDefinition.h"
#import "PacoEvent.h"
#import "PacoExperiment.h"


NSString* const PacoFinishLoadingDefinitionNotification = @"PacoFinishLoadingDefinitionNotification";
NSString* const PacoFinishLoadingExperimentNotification = @"PacoFinishLoadingExperimentNotification";

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
  NSLog(@"MODEL DEFINITION JSON = \n%@", jsonObject);
  NSArray *jsonExperiments = jsonObject;
  self.jsonObjectDefinitions = jsonObject;
  //NSMutableArray *experiments = [NSMutableArray array];
  NSMutableArray *definitions = [NSMutableArray array];

  for (id jsonExperiment in jsonExperiments) {
    PacoExperimentDefinition *experimentDefinition =
        [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:jsonExperiment];
    [experimentDefinition tagQuestionsForDependencies];
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

- (void)updateExperimentDefinitions:(NSArray*)definitions
{
  self.experimentDefinitions = definitions;
}

- (void)updateExperimentInstances:(NSMutableArray*)experiments
{
  self.experimentInstances = experiments;
}


- (void)applyInstanceJSON:(id)jsonObject {
  NSMutableArray *instances = [NSMutableArray array];
  if (jsonObject == nil) {
    self.experimentInstances = instances;
    return;
  }
  
  NSLog(@"MODEL INSTANCE JSON = \n%@", jsonObject);
  NSArray *jsonExperiments = jsonObject;
  self.jsonObjectInstances = jsonObject;
  //NSMutableArray *experiments = [NSMutableArray array];

  for (id jsonExperiment in jsonExperiments) {
    PacoExperiment *experiment = [[PacoExperiment alloc] init];
    [experiment deserializeFromJSON:jsonExperiment model:self];
    assert(experiment);
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


- (NSArray *)joinedExperiments {
  NSMutableArray *array = [NSMutableArray array];
  for (PacoExperiment *experiment in self.experimentInstances) {
    if ([experiment haveJoined]) {
      [array addObject:experiment];
    }
  }
  return array;
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

- (PacoExperiment *)addExperimentInstance:(PacoExperimentDefinition *)definition
                                 schedule:(PacoExperimentSchedule *)schedule
                                   events:(NSArray *)events {
  PacoExperiment *instance = [[PacoExperiment alloc] init];
  instance.schedule = schedule;
  instance.definition = definition;
  instance.events = events;
  NSDate *nowdate = [NSDate dateWithTimeIntervalSinceNow:0];
  instance.instanceId = definition.experimentId;
  instance.lastEventQueryTime = nowdate;
  [self.experimentInstances addObject:instance];
  return instance;
}

//YMZ: this piece of code needs to be refactored to be more efficient!
- (void)addExperimentsWithDefinition:(PacoExperimentDefinition*)definition events:(NSArray*)events
{
  NSAssert(definition != nil && [events count] > 0, @"definition should NOT be nil, or events should have more than one element!");
  
  NSArray *instances = [self instancesForExperimentId:definition.experimentId];
  // Expecting no existing instances in model
  assert([instances count] == 0);
  //need to split events out into each instance via the experiment name == experiment instance id
  
  NSMutableDictionary *map = [NSMutableDictionary dictionary];
  for (PacoEvent *event in events) {
    NSString *instanceId = event.experimentId;
    NSMutableArray *instanceEvents = [map objectForKey:instanceId];
    if (!instanceEvents) {
      instanceEvents = [NSMutableArray array];
      [map setObject:instanceEvents forKey:instanceId];
    }
    [instanceEvents addObject:event];
  }
  
  // Make experiment instances for each instance id.
  NSArray *instanceIds = [map allKeys];
  NSLog(@"FOUND %d INSTANCES OF EXPERIMENT %@ %@", instanceIds.count, definition.experimentId, definition.title);
  
  
  for (NSString *instanceId in instanceIds) {
    NSArray *instanceEvents = [map objectForKey:instanceId];
    NSLog(@"\tFOUND %d EVENTS FOR INSTANCE %@", instanceEvents.count, instanceId);
    PacoExperiment *experiment = [self addExperimentInstance:definition
                                                    schedule:definition.schedule
                                                      events:instanceEvents];
    NSAssert([experiment.instanceId isEqualToString:definition.experimentId] &&
             [instanceId isEqualToString:definition.experimentId],
             @"instanceId should be equal to experimentId!");    
  }
}


- (void)makeJSONObjectFromExperiments {
  NSMutableArray *experiments = [[NSMutableArray alloc] init];
  for (PacoExperimentDefinition *definition in self.experimentDefinitions) {
  
    // remove this after debbugging
    if (![definition.title isEqualToString:@"TestExperiment"]) {
      continue;
    }

    assert(definition.jsonObject);
    [experiments addObject:definition.jsonObject];
  }
  self.jsonObjectDefinitions = experiments;
}

- (void)makeJSONObjectFromInstances {
  NSMutableArray *experiments = [[NSMutableArray alloc] init];
  for (PacoExperiment *experiment in self.experimentInstances) {
    if (!experiment.jsonObject) {
      id json = [experiment serializeToJSON];
      experiment.jsonObject = json;
    }
    assert(experiment.jsonObject);
    [experiments addObject:experiment.jsonObject];
  }
  self.jsonObjectInstances = experiments;
}

#pragma mark file writing operations
- (BOOL)saveExperimentDefinitionsToFile {
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *documentsDirectory = [paths objectAtIndex:0];
  NSString *fileName = [NSString stringWithFormat:@"%@/definitions.plist", documentsDirectory];
  NSLog(@"Saving to %@", fileName);
  if (!self.jsonObjectDefinitions) {
    [self makeJSONObjectFromExperiments];
  }
  NSDictionary *json = self.jsonObjectDefinitions;

  NSError *jsonError = nil;
  NSData *jsonData =
      [NSJSONSerialization dataWithJSONObject:json
                                      options:NSJSONWritingPrettyPrinted
                                        error:&jsonError];
  if (jsonError) {
    NSLog(@"ERROR serializing to JSON %@", jsonError);
  }


  NSLog(@"WRItiNG DEFINITION JSON to FILE \n%@", json);
//  return [json writeToFile:fileName atomically:NO];
  return [[NSFileManager defaultManager] createFileAtPath:fileName contents:jsonData attributes:nil];
}

- (BOOL)saveExperimentInstancesToFile {
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *documentsDirectory = [paths objectAtIndex:0];
  NSString *fileName = [NSString stringWithFormat:@"%@/instances.plist", documentsDirectory];
  NSLog(@"Saving to %@", fileName);
  if ([self.jsonObjectInstances count] == 0) {
    [self makeJSONObjectFromInstances];
  }
  NSAssert([self.jsonObjectInstances isKindOfClass:[NSArray class]], @"jsonObjectInstances should be an array!");

  NSError *jsonError = nil;
  NSData *jsonData =
      [NSJSONSerialization dataWithJSONObject:self.jsonObjectInstances
                                      options:NSJSONWritingPrettyPrinted
                                        error:&jsonError];
  if (jsonError) {
    NSLog(@"ERROR serializing to JSON %@", jsonError);
  }

  NSLog(@"WRItiNG INSTANCE JSON to FILE \n%@", self.jsonObjectInstances);
  //return [json writeToFile:fileName atomically:NO];
  return [[NSFileManager defaultManager] createFileAtPath:fileName contents:jsonData attributes:nil];
}

- (BOOL)saveToFile {
  BOOL success = YES;
  BOOL check1 = [self saveExperimentDefinitionsToFile];
  success = success && check1;
  BOOL check2 = [self saveExperimentInstancesToFile];
  success = success && check2;
  return success;
}

- (BOOL)deleteFile {
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *documentsDirectory = [paths objectAtIndex:0];
  NSString *fileName = [NSString stringWithFormat:@"%@/definitions.plist", documentsDirectory];
  NSError *error = nil;
  if ([[NSFileManager defaultManager] removeItemAtPath:fileName error:&error] != YES) {
    return NO;
  }
  return YES;
}


#pragma mark file reading operations
- (BOOL)loadExperimentDefinitionsFromFile {
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *documentsDirectory = [paths objectAtIndex:0];
  NSString *fileName = [NSString stringWithFormat:@"%@/definitions.plist", documentsDirectory];
  NSLog(@"Loading from %@", fileName);
  NSError *error = nil;
  NSDictionary *attribs = [[NSFileManager defaultManager] attributesOfItemAtPath:fileName error:&error];
  if (error) {
    NSLog(@"File error %@", error);
  } else {
    NSLog(@"File attribs = %@", attribs);
  }

  NSData *jsonData = [[NSFileManager defaultManager] contentsAtPath:fileName];
  if (!jsonData) {
    NSLog(@"Failed to load data for file %@", fileName);
  }
  NSError *jsonError = nil;
  id jsonObj = !jsonData ? nil : [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingAllowFragments error:&jsonError];
  if (!jsonObj) {
    return NO;
  }
  NSLog(@"class = %@", NSStringFromClass([jsonObj class]));
  assert([jsonObj isKindOfClass:[NSArray class]]);

  //NSDictionary *json = jsonObj;//[NSDictionary dictionaryWithContentsOfFile:fileName];
  //if (!json) {
  //  NSLog(@"Failed to load from %@", fileName);
 // }
  NSArray *experiments = jsonObj;//[json objectForKey:@"definitions"];
  if (!experiments) {
    NSLog(@"Failed to load from %@", fileName);
  }
  [self applyDefinitionJSON:experiments];
  NSLog(@"LOADED DEFINTION JSON FROM FILE \n%@", self.jsonObjectDefinitions);
  return experiments.count > 0;
}

- (BOOL)loadExperimentInstancesFromFile {
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *documentsDirectory = [paths objectAtIndex:0];
  NSString *fileName = [NSString stringWithFormat:@"%@/instances.plist", documentsDirectory];
  NSLog(@"Loading from %@", fileName);
  //NSDictionary *json = [NSDictionary dictionaryWithContentsOfFile:fileName];
  //if (!json) {
  //  NSLog(@"Failed to load from %@", fileName);
 // }
  NSData *jsonData = [[NSFileManager defaultManager] contentsAtPath:fileName];
  if (!jsonData) {
    NSLog(@"Failed to load data for file %@", fileName);
  }
  NSError *jsonError = nil;
  id jsonObj = !jsonData ? nil : [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingAllowFragments error:&jsonError];
  if (!jsonObj) {
    return NO;
  }
  assert([jsonObj isKindOfClass:[NSArray class]]);

  NSArray *experiments = jsonObj;
  [self applyInstanceJSON:experiments];
  NSLog(@"LOADED INSTANCE JSON FROM FILE \n%@", self.jsonObjectInstances);
  return experiments.count > 0;
}

- (BOOL)loadFromFile {
  BOOL success = YES;
  BOOL check1 = [self loadExperimentDefinitionsFromFile];
  success = success && check1;
  BOOL check2 = [self loadExperimentInstancesFromFile];
  success = success && check2;
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


#pragma mark delete an experiment
- (void)deleteExperiment:(PacoExperiment*)experiment
{
  NSUInteger index = [self.experimentInstances indexOfObject:experiment];
  NSAssert(index != NSNotFound, @"An experiment must be in model to be deleted!");
  [self.experimentInstances removeObject:experiment];
}


@end
