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

@implementation PacoExperimentDefinition

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
  definition.modifyDate = [definitionMembers objectForKey:@"modifyDate"]; //Format: "2012/01/17"
  definition.published = [[definitionMembers objectForKey:@"published"] boolValue];
  definition.publishedUsers = [definitionMembers objectForKey:@"publishedUsers"];
  definition.questionsChange = [[definitionMembers objectForKey:@"questionsChange"] boolValue];
  
  id jsonSchedule = [definitionMembers objectForKey:@"schedule"];
  PacoExperimentSchedule *schedule = [PacoExperimentSchedule pacoExperimentScheduleFromJSON:jsonSchedule];
  definition.schedule = schedule;
  
  definition.title = [definitionMembers objectForKey:@"title"];
  definition.webReccommended = [[definitionMembers objectForKey:@"webRecommended"] boolValue];
  definition.experimentVersion = [[definitionMembers objectForKey:@"version"] intValue];
  
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
          @"modifyDate=%@ "
          @"published=%d "
          @"publishedUsers=%@ "
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
