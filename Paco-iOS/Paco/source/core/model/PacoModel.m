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

@implementation PacoExperimentFeedback

@synthesize feedbackId;
@synthesize text;
@synthesize type;
@synthesize jsonObject;

+ (id)pacoFeedbackFromJSON:(id)jsonObject {
  NSDictionary *feedbackMembers = jsonObject;
  PacoExperimentFeedback *feedback = [[PacoExperimentFeedback alloc] init];
  feedback.feedbackId = [NSString stringWithFormat:@"%ld", [[feedbackMembers objectForKey:@"id"] longValue]];
  feedback.type = [feedbackMembers objectForKey:@"feedbackType"];
  feedback.text = [feedbackMembers objectForKey:@"text"];
  feedback.jsonObject = jsonObject;
  return feedback;
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentFeedback:%p - "
      @"feedbackId=%@ "
      @"type=%@ "
      @"text=%@ >",
      self, self.feedbackId, self.type, self.text, nil];
}

@end

@implementation PacoExperimentInput

@synthesize conditional;
@synthesize conditionalExpression;
@synthesize inputIdentifier;
@synthesize invisibleInput;
@synthesize leftSideLabel;
@synthesize likertSteps;
@synthesize listChoices; // <NSString>
@synthesize mandatory;
@synthesize name;
@synthesize questionType;  // 'question'/
@synthesize responseType;  // 'likert', 'list'
@synthesize rightSideLabel;
@synthesize text;
@synthesize jsonObject;
@synthesize responseObject;

+ (id)pacoExperimentInputFromJSON:(id)jsonObject {
  PacoExperimentInput *input = [[PacoExperimentInput alloc] init];
  NSDictionary *inputMembers = jsonObject;
  input.conditional = [[inputMembers objectForKey:@"conditional"] boolValue];
  input.conditionalExpression = [inputMembers objectForKey:@"conditionExpression"];
  input.inputIdentifier = [NSString stringWithFormat:@"%ld", [[inputMembers objectForKey:@"id"] longValue]];
  input.invisibleInput = [[inputMembers objectForKey:@"invisibleInput"] boolValue];
  input.leftSideLabel = [inputMembers objectForKey:@"leftSideLabel"];
  input.likertSteps = [[inputMembers objectForKey:@"likertSteps"] intValue];
  input.listChoices = [inputMembers objectForKey:@"listChoices"];
  input.mandatory = [[inputMembers objectForKey:@"mandatory"] boolValue];
  input.name = [inputMembers objectForKey:@"name"];
  input.questionType = [inputMembers objectForKey:@"questionType"];
  input.responseType = [inputMembers objectForKey:@"responseType"];
  input.rightSideLabel = [inputMembers objectForKey:@"rightSideLabel"];
  input.text = [inputMembers objectForKey:@"text"];
  input.jsonObject = jsonObject;
  return input;
}

+ (NSArray *)parseExpression:(NSString *)expr {
  NSArray *ops = [NSArray arrayWithObjects:
                      @">=",
                      @"<=",
                      @"==",
                      @"!=",
                      @">",
                      @"<",
                      @"=",
                      nil];
  for (NSString *op in ops) {
    NSArray *exprArray = [expr componentsSeparatedByString:op];
    if (exprArray.count == 2) {
      NSString *dep = [exprArray objectAtIndex:0];
      dep = [dep stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
      NSString *value = [exprArray objectAtIndex:1];
      value = [value stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
      return [NSArray arrayWithObjects:dep, op, value, nil];
    }
  }
  return nil;
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentInput:%p - "
      @"conditional=%d "
      @"conditionalExpression=%@"
      @"inputIdentifier=%@ "
      @"invisibleInput=%d "
      @"leftSideLabel=%@ "
      @"likertSteps=%d "
      @"listChoices=%@ "
      @"mandatory=%d "
      @"name=%@ "
      @"questionType=%@ "
      @"responseType=%@ "
      @"rightSideLabel=%@ "
      @"text=%@ >",
      self,
      self.conditional,
      self.conditionalExpression,
      self.inputIdentifier,
      self.invisibleInput,
      self.leftSideLabel,
      self.likertSteps,
      self.listChoices,
      self.mandatory,
      self.name,
      self.questionType,
      self.responseType,
      self.rightSideLabel,
      self.text, nil];
}

@end

@implementation PacoExperimentSchedule

@synthesize byDayOfMonth;
@synthesize byDayOfWeek;
@synthesize dayOfMonth;
@synthesize esmEndHour;
@synthesize esmFrequency;
@synthesize esmPeriodInDays;
@synthesize esmPeriod;
@synthesize esmStartHour;
@synthesize esmWeekends;
@synthesize scheduleId;
@synthesize nthAMonth;
@synthesize repeatPeriod;
@synthesize scheduleType;
@synthesize times;  // NSNumber<long long>
@synthesize userEditable;
@synthesize weekDaysScheduled;
@synthesize jsonObject;
@synthesize esmSchedule;

- (id)serializeToJSON {
  return [NSMutableDictionary dictionaryWithObjectsAndKeys:
      [NSNumber numberWithBool:self.byDayOfMonth], @"byDayOfMonth",
      [NSNumber numberWithBool:self.byDayOfWeek], @"byDayOfWeek",
      [NSNumber numberWithInt:self.dayOfMonth], @"dayOfMonth",
      [NSNumber numberWithLongLong:self.esmEndHour], @"esmEndHour",
      [NSNumber numberWithInt:self.esmFrequency], @"esmFrequency",
      [NSNumber numberWithLongLong:self.esmPeriodInDays], @"esmPeriodInDays",
      [NSNumber numberWithLongLong:self.esmStartHour], @"esmStartHour",
      [NSNumber numberWithBool:self.esmWeekends], @"esmWeekends",
      self.scheduleId, @"id",
      [NSNumber numberWithInt:self.nthAMonth], @"nthAMonth",
      [NSNumber numberWithInt:self.repeatPeriod], @"repeatRate",
      [NSNumber numberWithInt:self.scheduleType], @"scheduleType",
      self.times, @"times",
      [NSNumber numberWithBool:self.userEditable], @"userEditable",
      [NSNumber numberWithInt:self.weekDaysScheduled], @"weekDaysScheduled",
      nil];
}

+ (id)pacoExperimentScheduleFromJSON:(id)jsonObject {
  PacoExperimentSchedule *schedule = [[PacoExperimentSchedule alloc] init];
  NSDictionary *scheduleMembers = jsonObject;
  schedule.byDayOfMonth = [[scheduleMembers objectForKey:@"byDayOfMonth"] boolValue];
  schedule.byDayOfWeek = [[scheduleMembers objectForKey:@"byDayOfWeek"] boolValue];
  schedule.dayOfMonth = [[scheduleMembers objectForKey:@"dayOfMonth"] intValue];
  schedule.esmEndHour = [[scheduleMembers objectForKey:@"esmEndHour"] longLongValue];
  schedule.esmFrequency = [[scheduleMembers objectForKey:@"esmFrequency"] intValue];
  schedule.esmPeriodInDays = [[scheduleMembers objectForKey:@"esmPeriodInDays"] longLongValue];
  if (schedule.esmPeriodInDays == 1) {
    schedule.esmPeriod = kPacoScheduleRepeatPeriodDay;
  } else if (schedule.esmPeriodInDays == 7) {
    schedule.esmPeriod = kPacoScheduleRepeatPeriodWeek;
  } else if (schedule.esmPeriodInDays == 30) {
    schedule.esmPeriod = kPacoScheduleRepeatPeriodMonth;
  }
  schedule.esmStartHour = [[scheduleMembers objectForKey:@"esmStartHour"] longLongValue];
  schedule.esmWeekends = [[scheduleMembers objectForKey:@"esmWeekends" ] boolValue];
  schedule.scheduleId = [NSString stringWithFormat:@"%ld", [[scheduleMembers objectForKey:@"id"] longValue]];
  schedule.nthAMonth = [[scheduleMembers objectForKey:@"nthAMonth"] intValue];
  schedule.repeatPeriod = (PacoScheduleRepeatPeriod)[[scheduleMembers objectForKey:@"repeatRate"] intValue];
  schedule.scheduleType = [[scheduleMembers objectForKey:@"scheduleType"] intValue];
  schedule.times = [scheduleMembers objectForKey:@"times"];
  schedule.times = [schedule.times sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
      NSDate *lhs = obj1;
      NSDate *rhs = obj2;
      return [lhs compare:rhs];
  }];
  schedule.userEditable = [[scheduleMembers objectForKey:@"userEditable"] boolValue];
  schedule.weekDaysScheduled = [[scheduleMembers objectForKey:@"weekDaysScheduled"] intValue];
  schedule.jsonObject = jsonObject;
  return schedule;
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentSchedule:%p - "
      @"byDayOfMonth=%d " 
      @"byDayOfWeek=%d "
      @"dayOfMonth=%d "
      @"esmEndHour=%lld "
      @"esmFrequency=%d "
      @"esmPeriodInDays=%lld "
      @"esmPeriod=%d"
      @"esmStartHour=%lld "
      @"esmWeekends=%d "
      @"scheduleId=%@ "
      @"nthAMonth=%d " 
      @"repeatPeriod=%d " 
      @"scheduleType=%d " 
      @"times=%@ "
      @"weekDaysScheduled=%d >",
      self,
      self.byDayOfMonth,
      self.byDayOfWeek,
      self.dayOfMonth,
      self.esmEndHour,
      self.esmFrequency,
      self.esmPeriodInDays,
      self.esmPeriod,
      self.esmStartHour,
      self.esmWeekends,
      self.scheduleId,
      self.nthAMonth,
      self.repeatPeriod,
      self.scheduleType,
      self.times,
      self.weekDaysScheduled,
      nil];
}

+ (NSString *)stringFromType:(PacoScheduleType)type {
  switch(type) {
  case kPacoScheduleTypeDaily:
    return @"Daily";
  case kPacoScheduleTypeWeekday:
    return @"Weekdays";
  case kPacoScheduleTypeWeekly:
    return @"Weekly";
  case kPacoScheduleTypeMonthly:
    return @"Monthly";
  case kPacoScheduleTypeESM:
    return @"Random sampling (ESM)";
  case kPacoScheduleTypeSelfReport:
    return @"Self report only";
  case kPacoScheduleTypeAdvanced:
    return @"Advanced";
  }
  return nil;
}

- (NSString *)typeString {
  return [[self class] stringFromType:self.scheduleType];
}

+ (NSString *)stringFromPeriod:(PacoScheduleRepeatPeriod)period {
  // convert num days into the string for that period enum.
  switch(period) {
  case kPacoScheduleRepeatPeriodDay: return @"Day";
  case kPacoScheduleRepeatPeriodWeek: return @"Week";
  case kPacoScheduleRepeatPeriodMonth: return @"Month";
  }
  return nil;
}

- (NSString *)periodString {
 return [[self class] stringFromPeriod:self.esmPeriod];
}

+ (NSString *)hourAsMillisec:(NSInteger)hourOn24Clock {
  long long millisecondsInHour = 3600000;
  return [NSString stringWithFormat:@"%lld", millisecondsInHour * hourOn24Clock];
}

- (NSString *)weekDaysScheduledString {
  NSString *dayNames[] = { @"Sun", @"Mon", @"Tue", @"Wed", @"Thu", @"Fri", @"Sat" };
  NSMutableString *string = [NSMutableString string];
  for (int i = 0; i < 7; ++i) {
    if (self.weekDaysScheduled & (1 << i)) {
      if ([string length] == 0) {
        [string appendString:dayNames[i]];
      } else {
        [string appendFormat:@", %@", dayNames[i]];
      }
    }
  }
  return string;
}

- (NSString *)jsonString {
  NSMutableString *json = [NSMutableString stringWithString:@"{"];
  
    [json appendFormat:@"type = %@;", [self typeString]];
    
    if (self.scheduleType == kPacoScheduleTypeESM) {
      [json appendFormat:@"frequency = %d", self.esmFrequency];
      [json appendFormat:@"esmPeriod = %@", [self periodString]];
      [json appendFormat:@"startHour = %lld", self.esmStartHour];
      [json appendFormat:@"endHour = %lld", self.esmEndHour];
      [json appendFormat:@"weekends = %d", self.esmWeekends];
    }

    [json appendString:@"times = ["];
      for (NSNumber *time in self.times) {
        if ([self.times objectAtIndex:0] == time) {
          [json appendFormat:@"%lld", [time longLongValue]];
        } else {
          [json appendFormat:@", %lld", [time longLongValue]];
        }
      }
    [json appendString:@"];"];
  
    [json appendFormat:@"repeatPeriod = %d;", self.repeatPeriod];
    [json appendFormat:@"daysOfWeek = %@;", [self weekDaysScheduledString]];
    [json appendFormat:@"nthOfMonth = %d;", self.nthAMonth];
    [json appendFormat:@"byDayOfMonth = %d;", self.byDayOfMonth];
    [json appendFormat:@"dayOfMonth = %d", self.dayOfMonth];
  [json appendString:@"}"];
  return json;

  return nil;
}

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
  self.experimentDefinitions = definitions;
}

- (void)applyInstanceJSON:(id)jsonObject {
  NSLog(@"MODEL INSTANCE JSON = \n%@", jsonObject);
  NSArray *jsonExperiments = jsonObject;
  self.jsonObjectInstances = jsonObject;
  //NSMutableArray *experiments = [NSMutableArray array];
  NSMutableArray *instances = [NSMutableArray array];

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
  self.experimentInstances = instances;
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
