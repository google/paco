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
static NSString* const DEFINITION_TITLE = @"title";
static NSString* const DEFINITION_WEBRECOMMENDED = @"webRecommended";
static NSString* const DEFINITION_VERSION = @"version";


@interface PacoExperimentDefinition ()
@property(nonatomic, strong) NSDate* startDate;
@property(nonatomic, strong) NSDate* endDate;
@property(nonatomic, strong) NSString* inclusiveEndDateString;
@end


@implementation PacoExperimentDefinition

+ (id)pacoExperimentDefinitionFromJSON:(id)jsonObject {
  PacoExperimentDefinition *definition = [[PacoExperimentDefinition alloc] init];
  NSDictionary *definitionMembers = jsonObject;
  definition.admins = [definitionMembers objectForKey:DEFINITION_ADMINS];
  definition.creator = [definitionMembers objectForKey:DEFINITION_CREATOR];
  definition.deleted = [[definitionMembers objectForKey:DEFINITION_DELETED] boolValue];
  definition.experimentDescription = [definitionMembers objectForKey:DEFINITION_DESCRIPTION];
  NSArray *jsonFeedbackList = [definitionMembers objectForKey:DEFINITION_FEEDBACK];
  NSMutableArray *feedbackObjects = [NSMutableArray array];
  for (id jsonFeedback in jsonFeedbackList) {
    [feedbackObjects addObject:[PacoExperimentFeedback pacoFeedbackFromJSON:jsonFeedback]];
  }
  definition.feedback = feedbackObjects;
  definition.fixedDuration = [[definitionMembers objectForKey:DEFINITION_FIXED_DURATION] boolValue];
  definition.experimentId = [NSString stringWithFormat:@"%ld", [[definitionMembers objectForKey:DEFINITION_ID] longValue]];
  definition.informedConsentForm = [definitionMembers objectForKey:DEFINITION_INFORMED_CONSENTFORM];
  NSArray *jsonInputList = [definitionMembers objectForKey:DEFINITION_INPUTS];
  NSMutableArray *inputObjects = [NSMutableArray array];
  for (id jsonInput in jsonInputList) {
    [inputObjects addObject:[PacoExperimentInput pacoExperimentInputFromJSON:jsonInput]];
  }
  definition.inputs = inputObjects;
  definition.modifyDate = [definitionMembers objectForKey:DEFINITION_MODIFYDATE]; //Format: "2012/01/17"
  definition.published = [[definitionMembers objectForKey:DEFINITION_PUBLISHED] boolValue];
  definition.publishedUsers = [definitionMembers objectForKey:DEFINITION_PUBLISHED_USERS];
  
  //"2013/10/15"
  NSString* startDateStr = [definitionMembers objectForKey:DEFINITION_STARTDATE];
  NSString* endDateStr = [definitionMembers objectForKey:DEFINITION_ENDDATE];
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
  
  definition.questionsChange = [[definitionMembers objectForKey:DEFINITION_QUESTIONS_CHANGE] boolValue];
  
  id jsonSchedule = [definitionMembers objectForKey:DEFINITION_SCHEDULE];
  PacoExperimentSchedule *schedule = [PacoExperimentSchedule pacoExperimentScheduleFromJSON:jsonSchedule];
  definition.schedule = schedule;
  
  definition.title = [definitionMembers objectForKey:DEFINITION_TITLE];
  definition.webReccommended = [[definitionMembers objectForKey:DEFINITION_WEBRECOMMENDED] boolValue];
  definition.experimentVersion = [[definitionMembers objectForKey:DEFINITION_VERSION] intValue];
  
  definition.jsonObject = jsonObject;
  
  return definition;
}

- (id)copyWithZone:(NSZone*)zone {
  PacoExperimentDefinition* another = [[self class] pacoExperimentDefinitionFromJSON:self.jsonObject];
  return another;
}


- (id)serializeToJSON {
  NSMutableDictionary* json = [NSMutableDictionary dictionary];
  if (self.admins) {
    [json setObject:self.admins forKey:DEFINITION_ADMINS];
  }
  if (self.creator) {
    [json setObject:self.creator forKey:DEFINITION_CREATOR];
  }
  [json setObject:[NSNumber numberWithBool:self.deleted] forKey:DEFINITION_DELETED];
  if (self.experimentDescription) {
    [json setObject:self.experimentDescription forKey:DEFINITION_DESCRIPTION];
  }
  
  NSMutableArray* feedbackJson = [NSMutableArray arrayWithCapacity:[self.feedback count]];
  for (PacoExperimentFeedback* feedback in self.feedback) {
    [feedbackJson addObject:[feedback serializeToJSON]];
  }
  [json setObject:feedbackJson forKey:DEFINITION_FEEDBACK];

  if (self.informedConsentForm) {
    [json setObject:self.informedConsentForm forKey:DEFINITION_INFORMED_CONSENTFORM];
  }
  [json setObject:[NSNumber numberWithBool:self.fixedDuration] forKey:DEFINITION_FIXED_DURATION];
  [json setObject:[NSNumber numberWithLongLong:[self.experimentId longLongValue]] forKey:DEFINITION_ID];
  
  NSMutableArray* inputJson = [NSMutableArray arrayWithCapacity:[self.inputs count]];
  for (PacoExperimentInput* input in self.inputs) {
    [inputJson addObject:[input serializeToJSON]];
  }
  [json setObject:inputJson forKey:DEFINITION_INPUTS];
  
  if (self.modifyDate) {
    [json setObject:self.modifyDate forKey:DEFINITION_MODIFYDATE];
  }
  [json setObject:[NSNumber numberWithBool:self.published] forKey:DEFINITION_PUBLISHED];
  if (self.publishedUsers) {
    [json setObject:self.publishedUsers forKey:DEFINITION_PUBLISHED_USERS];
  }
  if (self.startDate && self.endDate) {
    [json setObject:[PacoDateUtility stringWithYearAndDayFromDate:self.startDate] forKey:DEFINITION_STARTDATE];
    [json setObject:self.inclusiveEndDateString forKey:DEFINITION_ENDDATE];
  }
  [json setObject:[NSNumber numberWithBool:self.questionsChange] forKey:DEFINITION_QUESTIONS_CHANGE];
  [json setObject:[self.schedule serializeToJSON] forKey:DEFINITION_SCHEDULE];
  [json setObject:self.title forKey:DEFINITION_TITLE];
  [json setObject:[NSNumber numberWithBool:self.webReccommended] forKey:DEFINITION_WEBRECOMMENDED];
  [json setObject:[NSNumber numberWithInt:self.experimentVersion] forKey:DEFINITION_VERSION];
  return json;
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

- (BOOL)isExperimentValid {
  return [self isExperimentValidSinceDate:[NSDate date]];
}

- (BOOL)isExperimentValidSinceDate:(NSDate*)fromDate {
  if (fromDate == nil) {
    return NO;
  }
  if ([self isOngoing]) {
    return YES;
  }
  return [self.endDate pacoLaterThanDate:fromDate];
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
          @"modifyDate=%@ "
          @"published=%d "
          @"publishedUsers=%@ "
          @"startDate=%@"
          @"endDate=%@"
          @"questionsChange=%d "
          @"schedule=%@ "
          @"webReccommended=%d "
          @"experimentVersion=%d >",
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
          self.startDate ? [PacoDateUtility stringWithYearAndDayFromDate:self.startDate] : @"None",
          self.endDate ? [PacoDateUtility stringWithYearAndDayFromDate:self.endDate] : @"None",
          self.questionsChange,
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

- (void)clearEsmScheduleList {
  self.schedule.esmScheduleList = nil;
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
