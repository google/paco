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

#import "PacoExperimentFeedback.h"


@implementation PacoExperimentFeedback


+ (id)pacoFeedbackFromJSON:(id)jsonObject {
  NSDictionary *feedbackMembers = jsonObject;
  PacoExperimentFeedback *feedback = [[PacoExperimentFeedback alloc] init];
  feedback.feedbackId = [NSString stringWithFormat:@"%lld", [feedbackMembers[@"id"] longLongValue]];
  feedback.type = feedbackMembers[@"feedbackType"];
  feedback.text = feedbackMembers[@"text"];
  return feedback;
}

- (id)copyWithZone:(NSZone *)zone {
  PacoExperimentFeedback* copy = [[[self class] allocWithZone:zone] init];
  copy.feedbackId = [self.feedbackId copyWithZone:zone];
  copy.text = [self.text copyWithZone:zone];
  copy.type = [self.type copyWithZone:zone];
  return copy;
}

- (id)serializeToJSON {
  NSMutableDictionary* json = [NSMutableDictionary dictionary];
  json[@"id"] = @([self.feedbackId longLongValue]);
  json[@"feedbackType"] = self.type;
  json[@"text"] = self.text;
  return json;
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentFeedback:%p - "
          @"feedbackId=%@ "
          @"type=%@ "
          @"text=%@ >",
          self, self.feedbackId, self.type, self.text, nil];
}

- (BOOL)isCustomFeedback {
  return ![self.text isEqualToString:@"Thanks for Participating!"];
}

@end
