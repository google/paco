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
  feedback.feedbackId = [NSString stringWithFormat:@"%ld", [[feedbackMembers objectForKey:@"id"] longValue]];
  feedback.type = [feedbackMembers objectForKey:@"feedbackType"];
  feedback.text = [feedbackMembers objectForKey:@"text"];
  feedback.jsonObject = jsonObject;
  return feedback;
}

- (id)serializeToJSON {
  NSMutableDictionary* json = [NSMutableDictionary dictionary];
  [json setObject:[NSNumber numberWithLongLong:[self.feedbackId longLongValue]] forKey:@"id"];
  [json setObject:self.type forKey:@"feedbackType"];
  [json setObject:self.text forKey:@"text"];
  return json;
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentFeedback:%p - "
          @"feedbackId=%@ "
          @"type=%@ "
          @"text=%@ >",
          self, self.feedbackId, self.type, self.text, nil];
}

@end
