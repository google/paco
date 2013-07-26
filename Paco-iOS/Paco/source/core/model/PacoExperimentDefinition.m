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
  definition.modifyDate = [[definitionMembers objectForKey:@"modifyDate"] longLongValue];
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
          @"modifyDate=%lld "
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
