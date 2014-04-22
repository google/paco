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

#import "PacoExperimentDefinition.h"
#import "PacoExperimentFeedback.h"
#import "PacoExperimentInput.h"
#import "PacoExperimentSchedule.h"
#import "PacoDateUtility.h"
#import "NSDate+Paco.h"
#import "PacoTriggerSignal.h"

static NSString* const DEFINITION_ADMINS = @"admins";
static NSString* const DEFINITION_CREATOR = @"creator";
static NSString* const DEFINITION_DELETED = @"deleted";
static NSString* const DEFINITION_DESCRIPTION = @"description";
static NSString* const DEFINITION_FEEDBACK = @"feedback";
static NSString* const DEFINITION_FIXED_DURATION = @"fixedDuration";
static NSString* const DEFINITION_ID = @"id";
static NSString* const DEFINITION_INFORMED_CONSENTFORM = @"informedConsentForm";
static NSString* const DEFINITION_INPUTS = @"inputs";
static NSString* const DEFINITION_MODIFYDATE = @"modifyDate";
static NSString* const DEFINITION_PUBLISHED = @"published";
static NSString* const DEFINITION_PUBLISHED_USERS = @"publishedUsers";
static NSString* const DEFINITION_STARTDATE = @"startDate";
static NSString* const DEFINITION_ENDDATE = @"endDate";
static NSString* const DEFINITION_QUESTIONS_CHANGE = @"questionsChange";
static NSString* const DEFINITION_SCHEDULE = @"schedule";
static NSString* const DEFINITION_SIGNAL_MECHANISMS = @"signalingMechanisms";
static NSString* const DEFINITION_TITLE = @"title";
static NSString* const DEFINITION_WEBRECOMMENDED = @"webRecommended";
static NSString* const DEFINITION_VERSION = @"version";
static NSString* const DEFINITION_CUSTOM_RENDERING = @"customRendering";

@interface PacoExperimentDefinition ()
@property(nonatomic, strong) NSDate* startDate;
@property(nonatomic, strong) NSDate* endDate;
@property(nonatomic, copy) NSString* inclusiveEndDateString;
@property (nonatomic, strong) NSArray* signalMechanismList;
@end


@implementation PacoExperimentDefinition

+ (id)pacoExperimentDefinitionFromJSON:(id)jsonObject {
  PacoExperimentDefinition *definition = [[PacoExperimentDefinition alloc] init];
  NSDictionary *definitionMembers = jsonObject;
  definition.admins = definitionMembers[DEFINITION_ADMINS];
  definition.creator = definitionMembers[DEFINITION_CREATOR];
  definition.deleted = [definitionMembers[DEFINITION_DELETED] boolValue];
  definition.experimentDescription = definitionMembers[DEFINITION_DESCRIPTION];
  NSArray *jsonFeedbackList = definitionMembers[DEFINITION_FEEDBACK];
  NSMutableArray *feedbackObjects = [NSMutableArray array];
  for (id jsonFeedback in jsonFeedbackList) {
    [feedbackObjects addObject:[PacoExperimentFeedback pacoFeedbackFromJSON:jsonFeedback]];
  }
  definition.feedbackList = feedbackObjects;
  definition.isCustomRendering = [definitionMembers[DEFINITION_CUSTOM_RENDERING] boolValue];
  definition.fixedDuration = [definitionMembers[DEFINITION_FIXED_DURATION] boolValue];
  definition.experimentId = [NSString stringWithFormat:@"%lld", [definitionMembers[DEFINITION_ID] longLongValue]];
  definition.informedConsentForm = definitionMembers[DEFINITION_INFORMED_CONSENTFORM];
  NSArray *jsonInputList = definitionMembers[DEFINITION_INPUTS];
  NSMutableArray *inputObjects = [NSMutableArray array];
  for (id jsonInput in jsonInputList) {
    [inputObjects addObject:[PacoExperimentInput pacoExperimentInputFromJSON:jsonInput]];
  }
  definition.inputs = inputObjects;
  definition.modifyDate = definitionMembers[DEFINITION_MODIFYDATE]; //Format: "2012/01/17"
  definition.published = [definitionMembers[DEFINITION_PUBLISHED] boolValue];
  definition.publishedUsers = definitionMembers[DEFINITION_PUBLISHED_USERS];
  
  //"2013/10/15"
  NSString* startDateStr = definitionMembers[DEFINITION_STARTDATE];
  NSString* endDateStr = definitionMembers[DEFINITION_ENDDATE];
  if (startDateStr && endDateStr) {
    definition.inclusiveEndDateString = endDateStr;
    definition.startDate = [PacoDateUtility dateFromStringWithYearAndDay:startDateStr];
    NSDate* inclusiveEndDate = [PacoDateUtility dateFromStringWithYearAndDay:endDateStr];
    definition.endDate = [inclusiveEndDate pacoNextDayAtMidnight];
    NSAssert(definition.startDate != nil && definition.endDate != nil,
             @"startDate and endDate should be valid!");
    NSAssert([definition.startDate pacoEarlierThanDate:definition.endDate],
             @"startDate must be earlier than endDate");
  }
  
  definition.questionsChange = [definitionMembers[DEFINITION_QUESTIONS_CHANGE] boolValue];
  
  id jsonSchedule = definitionMembers[DEFINITION_SCHEDULE];
  PacoExperimentSchedule *schedule = [PacoExperimentSchedule pacoExperimentScheduleFromJSON:jsonSchedule];
  definition.schedule = schedule;
  
  id jsonSignalMechanismList = definitionMembers[DEFINITION_SIGNAL_MECHANISMS];
  NSAssert([jsonSignalMechanismList isKindOfClass:[NSArray class]], @"signal mechanisms should be an array");
  NSMutableArray* signalingMechanisms = [NSMutableArray arrayWithCapacity:[jsonSignalMechanismList count]];
  for (id signalJson in jsonSignalMechanismList) {
    id signal = nil;
    if ([signalJson[kSignalType] isEqualToString:kTriggerSignal]) { //trigger signal
      signal = [PacoTriggerSignal signalFromJson:signalJson];
    } else { //schedule signal
      signal = [PacoExperimentSchedule pacoExperimentScheduleFromJSON:signalJson];
    }
    NSAssert(signal, @"signal should be valid");
    [signalingMechanisms addObject:signal];
  }
  definition.signalMechanismList = signalingMechanisms;
  
  definition.title = definitionMembers[DEFINITION_TITLE];
  definition.webReccommended = [definitionMembers[DEFINITION_WEBRECOMMENDED] boolValue];
  definition.experimentVersion = [definitionMembers[DEFINITION_VERSION] intValue];
  
  return definition;
}


- (id)copyWithZone:(NSZone*)zone {
  PacoExperimentDefinition* another = [[[self class] allocWithZone:zone] init];
  another.admins = [self.admins copyWithZone:zone];
  another.creator = [self.creator copyWithZone:zone];
  another.deleted = self.deleted;
  another.experimentDescription = [self.experimentDescription copyWithZone:zone];
  another.feedbackList = [self.feedbackList copyWithZone:zone];
  another.isCustomRendering = self.isCustomRendering;
  another.fixedDuration = self.fixedDuration;
  another.experimentId = [self.experimentId copyWithZone:zone];
  another.informedConsentForm = [self.informedConsentForm copyWithZone:zone];
  another.inputs = [self.inputs copyWithZone:zone];
  
  another.modifyDate = [self.modifyDate copyWithZone:zone];
  another.published = self.published;
  another.publishedUsers = [self.publishedUsers copyWithZone:zone];
  
  another.inclusiveEndDateString = [self.inclusiveEndDateString copyWithZone:zone];
  another.startDate = [self.startDate copyWithZone:zone];
  another.endDate = [self.endDate copyWithZone:zone];
  
  another.questionsChange = self.questionsChange;
  another.schedule = [self.schedule copyWithZone:zone];
  
  another.signalMechanismList = [self.signalMechanismList copyWithZone:zone];
  
  another.title = [self.title copyWithZone:zone];
  another.webReccommended = self.webReccommended;
  another.experimentVersion = self.experimentVersion;
  
  return another;
}


- (id)serializeToJSON {
  NSMutableDictionary* json = [NSMutableDictionary dictionary];
  if (self.admins) {
    json[DEFINITION_ADMINS] = self.admins;
  }
  if (self.creator) {
    json[DEFINITION_CREATOR] = self.creator;
  }
  json[DEFINITION_DELETED] = @(self.deleted);
  if (self.experimentDescription) {
    json[DEFINITION_DESCRIPTION] = self.experimentDescription;
  }
  
  NSMutableArray* feedbackJson = [NSMutableArray arrayWithCapacity:[self.feedbackList count]];
  for (PacoExperimentFeedback* feedback in self.feedbackList) {
    [feedbackJson addObject:[feedback serializeToJSON]];
  }
  json[DEFINITION_FEEDBACK] = feedbackJson;
  json[DEFINITION_CUSTOM_RENDERING] = @(self.isCustomRendering);
  if (self.informedConsentForm) {
    json[DEFINITION_INFORMED_CONSENTFORM] = self.informedConsentForm;
  }
  json[DEFINITION_FIXED_DURATION] = @(self.fixedDuration);
  json[DEFINITION_ID] = @([self.experimentId longLongValue]);
  
  NSMutableArray* inputJson = [NSMutableArray arrayWithCapacity:[self.inputs count]];
  for (PacoExperimentInput* input in self.inputs) {
    [inputJson addObject:[input serializeToJSON]];
  }
  json[DEFINITION_INPUTS] = inputJson;
  
  if (self.modifyDate) {
    json[DEFINITION_MODIFYDATE] = self.modifyDate;
  }
  json[DEFINITION_PUBLISHED] = @(self.published);
  if (self.publishedUsers) {
    json[DEFINITION_PUBLISHED_USERS] = self.publishedUsers;
  }
  if (self.startDate && self.endDate) {
    json[DEFINITION_STARTDATE] = [PacoDateUtility stringWithYearAndDayFromDate:self.startDate];
    json[DEFINITION_ENDDATE] = self.inclusiveEndDateString;
  }
  json[DEFINITION_QUESTIONS_CHANGE] = @(self.questionsChange);
  json[DEFINITION_SCHEDULE] = [self.schedule serializeToJSON];
  
  NSMutableArray* signalMechanisms = [NSMutableArray arrayWithCapacity:[self.signalMechanismList count]];
  for (id signal in self.signalMechanismList) {
    NSAssert([signal respondsToSelector:@selector(serializeToJSON)],
             @"PacoExperimentSchedule and PacoTriggerSignal should both implement serializeToJSON");
    id json = [signal performSelector:@selector(serializeToJSON) withObject:nil];
    NSAssert(json, @"json should be valid");
    [signalMechanisms addObject:json];
  }
  json[DEFINITION_SIGNAL_MECHANISMS] = signalMechanisms;

  json[DEFINITION_TITLE] = self.title;
  json[DEFINITION_WEBRECOMMENDED] = @(self.webReccommended);
  json[DEFINITION_VERSION] = @(self.experimentVersion);
  return json;
}

- (BOOL)isTriggerExperiment {
  NSAssert([self.signalMechanismList count] > 0, @"signalMechanismList should have element");
  return [[self.signalMechanismList firstObject] isKindOfClass:[PacoTriggerSignal class]];
}

- (BOOL)hasCustomFeedback {
  return [[self.feedbackList firstObject] isCustomFeedback];
}


- (BOOL)isCompatibleWithIOS {
  return ![self isTriggerExperiment] && ![self hasCustomFeedback] && !self.isCustomRendering;
}

- (BOOL)isFixedLength {
  return self.startDate && self.endDate;
}

- (BOOL)isOngoing {
  return ![self isFixedLength];
}

- (BOOL)hasSameDurationWithDefinition:(PacoExperimentDefinition*)another {
  if ([self isOngoing] && [another isOngoing]) {
    return YES;
  }
  if ([self isFixedLength] &&
      [another isFixedLength] &&
      [self.startDate isEqualToDate:another.startDate] &&
      [self.endDate isEqualToDate:another.endDate]) {
    return YES;
  }
  return NO;
}

- (BOOL)isExperimentValidSinceDate:(NSDate*)fromDate {
  //if experiment is ongoing, it should always be valid
  if ([self isOngoing]) {
    return YES;
  }
  if (fromDate == nil) {
    return NO;
  }
  return [self.endDate pacoLaterThanDate:fromDate];
}


- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentDefinition:%p - "
          @"trigger = %@ "
          @"experimentId=%@ "
          @"title=%@ "
          @"admins=%@ "
          @"creator=%@ "
          @"deleted=%d "
//          @"experimentDescription=%@ "
          @"feedback=%@ "
          @"isCustomRendering=%@ "
          @"hasCustomFeedback=%@ "
          @"fixedDuration=%d "
//          @"informedConsentForm=%@ "
//          @"inputs=%@ "
          @"modifyDate=%@ "
          @"published=%d "
          @"publishedUsers=%@ "
          @"startDate=%@"
          @"endDate=%@"
          @"questionsChange=%d "
          @"signalMechanisms=%@ "
          @"schedule=%@ "
          @"webReccommended=%d "
          @"experimentVersion=%d >",
          self,
          [self isTriggerExperiment] ? @"YES" : @"NO",
          self.experimentId,
          self.title,
          self.admins,
          self.creator,
          self.deleted,
//          self.experimentDescription,
          self.feedbackList,
          self.isCustomRendering ? @"YES" : @"NO",
          [self hasCustomFeedback] ?  @"YES" : @"NO",
          self.fixedDuration,
//          self.informedConsentForm,
//          self.inputs,
          self.modifyDate,
          self.published,
          self.publishedUsers,
          self.startDate ? [PacoDateUtility stringWithYearAndDayFromDate:self.startDate] : @"None",
          self.endDate ? [PacoDateUtility stringWithYearAndDayFromDate:self.endDate] : @"None",
          self.questionsChange,
          self.signalMechanismList,
          self.schedule,
          self.webReccommended,
          self.experimentVersion,
          nil];
}


- (void)clearInputs {
  for (PacoExperimentInput *input in self.inputs) {
    input.responseObject = nil;
  }
}


#pragma mark TEST code
+ (PacoExperimentDefinition*)testDefinitionWithId:(NSString*)definitionId {
  NSString* path = [NSString stringWithFormat:@"Test_%@", definitionId];
  NSDictionary* definitionDict =
      [NSDictionary dictionaryWithContentsOfFile:[[NSBundle mainBundle] pathForResource:path ofType:@"plist"]];
  return [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:definitionDict];
}


+ (PacoExperimentDefinition*)testPacoExperimentDefinition {
  int timeOutMinutes = 2;
  
  int firstTime = (10 /* hour */ * 3600000) + (7 /* minutes */ * 60000);
  int secondTime = (10 /* hour */ * 3600000) + (8 /* minutes */ * 60000);
  
  NSString *testDefinitionJSON = [NSString stringWithFormat:@"{\"title\":\"Test Local: Notification iOS Experiment\",\"description\":\"This experiment is to test the iOS Notification system for Paco.\",\"informedConsentForm\":\"You consent to be used for world domination\",\"creator\":\"tom.pennings@gmail.com\",\"fixedDuration\":false,\"id\":8798005,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":%d,\"id\":1,\"scheduleType\":0,\"esmFrequency\":99,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[%d,%d],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":%d,\"id\":1,\"scheduleType\":0,\"esmFrequency\":99,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[%d,%d],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false},\"questionsChange\":false,\"modifyDate\":\"2013/07/25\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"Do you feel OK today?\",\"mandatory\":true,\"responseType\":\"list\",\"likertSteps\":5,\"name\":\"feel_ok\",\"conditional\":false,\"listChoices\":[\"Yes\",\"No\"],\"multiselect\":true,\"invisibleInput\":false}],\"feedback\":[{\"id\":2,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":1}", timeOutMinutes, firstTime, secondTime, timeOutMinutes, firstTime, secondTime];
  
  NSError* jsonError = nil;
  NSData* jsonData = [testDefinitionJSON dataUsingEncoding:NSUTF8StringEncoding];
  id jsonObj = !jsonData ? nil : [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingAllowFragments error:&jsonError];
  
  return [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:jsonObj];
}


@end
