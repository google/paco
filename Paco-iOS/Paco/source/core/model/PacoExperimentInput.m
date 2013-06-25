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

#import "PacoExperimentInput.h"

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
