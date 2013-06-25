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


NSString* const PacoExperimentDefinitionUpdateNotification = @"PacoExperimentDefinitionUpdateNotification";
NSString* const PacoExperimentInstancesUpdateNotification = @"PacoExperimentInstancesUpdateNotification";

@interface PacoExperimentSchedule ()
- (id)serializeToJSON;
@end



@implementation PacoExperimentDefinition

@synthesize admins;  // <NSString>
@synthesize creator;
@synthesize deleted;
@synthesize experimentDescription;
@synthesize feedback;  // <PacoExperimentFeedback>
@synthesize fixedDuration;
@synthesize experimentId;
@synthesize informedConsentForm;
@synthesize inputs;  // <PacoExperimentInput>
@synthesize modifyDate;
@synthesize published;
@synthesize publishedUsers;  // <NSString>
@synthesize questionsChange;
@synthesize schedule;
@synthesize title;
@synthesize webReccommended;
@synthesize jsonObject;

+ (id)pacoExperimentDefinitionFromJSON:(id)jsonObject {
  PacoExperimentDefinition *definition = [[PacoExperimentDefinition alloc] init];
  NSDictionary *definitionMembers = jsonObject;
  definition.admins = [definitionMembers objectForKey:@"admins"];
  definition.creator = [definitionMembers objectForKey:@"creator"];
  definition.deleted = [[definitionMembers objectForKey:@"deleted"] boolValue];
  definition.experimentDescription = [definitionMembers objectForKey:@"description"];
  NSArray *jsonFeedbackList = [definitionMembers objectForKey:@"feedback"];
  NSMutableArray *feedbackObjects = [NSMutableArray array];
  for (id jsonFeedback in jsonFeedbackList) {
    [feedbackObjects addObject:[PacoExperimentFeedback pacoFeedbackFromJSON:jsonFeedback]];
  }
  definition.feedback = feedbackObjects;
  definition.fixedDuration = [[definitionMembers objectForKey:@"fixedDuration"] boolValue];
  definition.experimentId = [NSString stringWithFormat:@"%ld", [[definitionMembers objectForKey:@"id"] longValue]];
  definition.informedConsentForm = [definitionMembers objectForKey:@"informedConsentForm"];
  NSArray *jsonInputList = [definitionMembers objectForKey:@"inputs"];
  NSMutableArray *inputObjects = [NSMutableArray array];
  for (id jsonInput in jsonInputList) {
    [inputObjects addObject:[PacoExperimentInput pacoExperimentInputFromJSON:jsonInput]];
  }
  definition.inputs = inputObjects;
  definition.modifyDate = [[definitionMembers objectForKey:@"modifyDate"] longLongValue];
  definition.published = [[definitionMembers objectForKey:@"published"] boolValue];
  definition.publishedUsers = [definitionMembers objectForKey:@"publishedUsers"];
  definition.questionsChange = [[definitionMembers objectForKey:@"questionsChange"] boolValue];

  id jsonSchedule = [definitionMembers objectForKey:@"schedule"];
  PacoExperimentSchedule *schedule = [PacoExperimentSchedule pacoExperimentScheduleFromJSON:jsonSchedule];
  definition.schedule = schedule;

  definition.title = [definitionMembers objectForKey:@"title"];
  definition.webReccommended = [[definitionMembers objectForKey:@"webRecommended"] boolValue];

  definition.jsonObject = jsonObject;

  return definition;
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentDefinition:%p - "
      @"experimentId=%@ "
      @"title=%@ "
      @"admins=%@ "
      @"creator=%@ "
      @"deleted=%d "
      @"experimentDescription=%@ "
      @"feedback=%@ "
      @"fixedDuration=%d "
      @"informedConsentForm=%@ "
      @"inputs=%@ "
      @"modifyDate=%lld "
      @"published=%d "
      @"publishedUsers=%@ "
      @"questionsChange=%d "
      @"schedule=%@ "
      @"webReccommended=%d >",
      self,
      self.experimentId,
      self.title,
      self.admins,
      self.creator,
      self.deleted,
      self.experimentDescription,
      self.feedback,
      self.fixedDuration,
      self.informedConsentForm,
      self.inputs,
      self.modifyDate,
      self.published,
      self.publishedUsers,
      self.questionsChange,
      self.schedule,
      self.webReccommended,
      nil];
}

- (void)tagQuestionsForDependencies {
  if (![self.title isEqualToString:@"TestExperiment"])
    return;
  for (PacoExperimentInput *input in self.inputs) {
    input.isADependencyForOthers = NO;
  }
  for (PacoExperimentInput *input in self.inputs) {
    if (input.conditional) {
      NSArray *expr = [PacoExperimentInput parseExpression:input.conditionalExpression];
      NSString *dependency = [expr objectAtIndex:0];
      //NSString *op = [expr objectAtIndex:1];
      //NSString *value = [expr objectAtIndex:2];
      for (PacoExperimentInput *input2 in self.inputs) {
        if ([input2.name isEqualToString:dependency]) {
          input2.isADependencyForOthers = YES;
          break;
        }
      }
    }
  }
}

@end

@interface PacoEvent ()
@property (readwrite, copy) NSString *appId;
@property (readwrite, copy) NSString *pacoVersion;
@end

@implementation PacoEvent

@synthesize who;
@synthesize when;
@synthesize latitude;
@synthesize longitude;
@synthesize responseTime;
@synthesize scheduledTime;
@synthesize appId = appId_;
@synthesize pacoVersion = pacoVersion_;
@synthesize experimentId;
@synthesize experimentName;
@synthesize responses;
@synthesize jsonObject;

- (id)init {
  self = [super init];
  if (self) {
    appId_ = @"ios_paco";
    pacoVersion_ = @"1";
  }
  return self;
}

+ (id)pacoEventForIOS {
  return [[PacoEvent alloc] init];
}

+ (NSDate *)pacoDateForString:(NSString *)dateStr {
  // Assuming its a long long for ms since epoch.
  long long timeInterval = [dateStr longLongValue];
  return [NSDate dateWithTimeIntervalSince1970:timeInterval];
}

+ (id)pacoEventFromJSON:(id)jsonObject {
  PacoEvent *event = [[PacoEvent alloc] init];
  NSDictionary *eventMembers = jsonObject;
  event.who = [eventMembers objectForKey:@"who"];
  event.when = [self pacoDateForString:[eventMembers objectForKey:@"when"]];
  event.latitude = [[eventMembers objectForKey:@"lat"] longLongValue];
  event.longitude = [[eventMembers objectForKey:@"long"] longLongValue];
  event.responseTime = [self pacoDateForString:[eventMembers objectForKey:@"responseTime"]];
  event.scheduledTime = [self pacoDateForString:[eventMembers objectForKey:@"scheduledTime"]];
  event.appId = [eventMembers objectForKey:@"appId"];
  event.pacoVersion = [eventMembers objectForKey:@"pacoVersion"];
  event.experimentId = [eventMembers objectForKey:@"experimentId"];
  event.experimentName = [eventMembers objectForKey:@"experimentName"];
  event.responses = [eventMembers objectForKey:@"xxx"];
  return event;
}

- (id)generateJsonObject {
  NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
  [dictionary setValue:self.experimentId forKey:@"experimentId"];
  [dictionary setValue:self.experimentName forKey:@"experimentName"];
  [dictionary setValue:self.who forKey:@"who"];
  [dictionary setValue:self.appId forKey:@"appId"];
  [dictionary setValue:self.pacoVersion forKey:@"pacoVersion"];
  if (self.when) {
    [dictionary setValue:[PacoDate pacoStringForDate:self.when] forKey:@"when"];
  }
  if (self.latitude) {
    [dictionary setValue:[NSString stringWithFormat:@"%lld", self.latitude] forKey:@"lat"];
  }
  if (self.longitude) {
    [dictionary setValue:[NSString stringWithFormat:@"%lld", self.longitude] forKey:@"long"];
  }
  if (self.responseTime) {
    [dictionary setValue:[PacoDate pacoStringForDate:self.responseTime] forKey:@"responseTime"];
  }
  if (self.scheduledTime) {
    [dictionary setValue:[PacoDate pacoStringForDate:self.scheduledTime] forKey:@"scheduledTime"];
  }
  if (self.responses) {
    [dictionary setValue:self.responses forKey:@"responses"];
  }
  return [NSDictionary dictionaryWithDictionary:dictionary];
}

@end

@implementation PacoExperiment
@synthesize definition;
@synthesize events;
@synthesize instanceId;
@synthesize lastEventQueryTime;
@synthesize schedule;
@synthesize jsonObject;
//@synthesize overrideSchedule;

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperiment:%p - "
                                    @"definitions=%@ "
                                    @"events=%@ "
                                    @"lastEventsQueryTime=%lld>",
                                    self,
                                    self.definition,
                                    self.events,
                                    self.lastEventQueryTime];
}

- (id)serializeToJSON {
  id jsonSchedule = self.schedule.jsonObject;
  if (!jsonSchedule) {
    jsonSchedule = [self.schedule serializeToJSON];
  }
  NSMutableArray *pacoEvents = [NSMutableArray array];
  for (PacoEvent *event in self.events) {
    if (!event.jsonObject) {
      event.jsonObject = [event generateJsonObject];
    }
    assert(event.jsonObject);
    [pacoEvents addObject:event.jsonObject];
  }
  return [NSDictionary dictionaryWithObjectsAndKeys:
      self.definition.experimentId, @"experimentId",
      self.instanceId, @"instanceId",
      [NSNumber numberWithLongLong:self.lastEventQueryTime], @"lastEventQueryTime",
      pacoEvents, @"events",
      jsonSchedule, @"schedule",
      nil];
}

- (void)deserializeFromJSON:(id)json model:(PacoModel *)model {
  assert([json isKindOfClass:[NSDictionary class]]);
  NSDictionary *map = json;
  self.instanceId = [map objectForKey:@"instanceId"];
  
  NSNumber *timestamp = [map objectForKey:@"lastEventQueryTime"];
  self.lastEventQueryTime = [timestamp longLongValue];
  
  self.schedule = [PacoExperimentSchedule pacoExperimentScheduleFromJSON:[map objectForKey:@"schedule"]];
  
  NSMutableArray *pacoEvents = [NSMutableArray array];
  NSArray *eventJSONs = [map objectForKey:@"events"];
  for (id eventJSON in eventJSONs) {
    PacoEvent *event = [PacoEvent pacoEventFromJSON:eventJSON];
    assert(event);
    [pacoEvents addObject:event];
  }
  self.events = pacoEvents;
  self.jsonObject = json;
  
  NSString *experimentId = [map objectForKey:@"experimentId"];
  PacoExperimentDefinition *experimentDefinition = [model experimentDefinitionForId:experimentId];
  self.definition = experimentDefinition;
}

- (BOOL)haveJoined {
  // TODO(gregvance): maybe should check for the "joined"="true" in the event
  //     responses, but what about un-joining ?
  return [self.events count];
}

@end

@implementation PacoModel

@synthesize experimentInstances;
@synthesize experimentDefinitions;
@synthesize jsonObjectDefinitions;
@synthesize jsonObjectInstances;

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
  [[NSNotificationCenter defaultCenter] postNotificationName:PacoExperimentDefinitionUpdateNotification object:definitions];
}

- (void)updateExperimentInstances:(NSMutableArray*)experiments
{
  self.experimentInstances = experiments;
  [[NSNotificationCenter defaultCenter] postNotificationName:PacoExperimentInstancesUpdateNotification object:experiments];
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

- (id)initWithDefinitionJSON:(id)jsonDefintions
                instanceJSON:(id)jsonInstances {
  self = [super init];
  if (self) {
    [self applyDefinitionJSON:jsonDefintions];
    [self applyInstanceJSON:jsonInstances];
  }
  return self;
}

+ (id)pacoModelFromDefinitionJSON:(id)jsonDefintions
                     instanceJSON:(id)jsonInstances {
  return [[PacoModel alloc] initWithDefinitionJSON:jsonDefintions
                                      instanceJSON:jsonInstances];
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoModel:%p - experiments=%@>", self, self.experimentInstances];
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
  NSString *nowdateStr = [[[NSDateFormatter alloc] init] stringFromDate:nowdate];
  instance.instanceId = [NSString stringWithFormat:@"%@_%@", definition.title, nowdateStr];
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
    NSString *instanceId = event.experimentName;
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
    
    //YMZ: confusing, why we are using two different instanceId?
    // Use the instance id from the sorted event map.
    experiment.instanceId = instanceId;
  }
  
  [[NSNotificationCenter defaultCenter] postNotificationName:PacoExperimentInstancesUpdateNotification object:nil];
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
  if (!self.jsonObjectInstances) {
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

+ (PacoModel *)pacoModelFromFile {
  PacoModel *model = [[PacoModel alloc] init];
  BOOL loaded = [model loadFromFile];
  if (!loaded) {
    return nil;
  }
  return model;
}

@end
