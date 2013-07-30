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
  input.multiSelect = [[inputMembers objectForKey:@"multiselect"] boolValue];
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


+ (NSArray*)choicesFromBitFlags:(NSNumber*)bitFlags sizeOfList:(int)sizeOfList {
  NSAssert(sizeOfList > 0, @"sizeOfList should be larger than 0!");
  
  NSMutableArray* choices = [NSMutableArray arrayWithCapacity:sizeOfList];
  unsigned int result = [bitFlags unsignedIntValue];
  if (0 == result) {
    return choices;
  }
  
  for (int itemIndex = 0; itemIndex < sizeOfList; itemIndex++) {
    unsigned int flag = (1 << itemIndex);
    unsigned int value = (result & flag);
    unsigned int bitValue = value >> itemIndex;
    NSAssert(bitValue == 1 || bitValue == 0, @"bitValue is not correct!");
    if (bitValue == 1) {
      [choices addObject:[NSNumber numberWithInt:(itemIndex + 1)]]; //start from 1, not 0
    }
  }
  return choices;
}

- (NSString*)stringForListChoices {
  if (![self.responseType isEqualToString:@"list"]) {
    return nil;
  }
  int sizeOfList = [self.listChoices count];
  NSArray* choices = [PacoExperimentInput choicesFromBitFlags:(NSNumber*)self.responseObject
                                                   sizeOfList:sizeOfList];
  if (0 == [choices count]) {
    return @"";
  }else{
    return [choices componentsJoinedByString:@","];
  }
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
          @"multiSelect=%@ "
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
          self.multiSelect ? @"YES" : @"NO",
          self.name,
          self.questionType,
          self.responseType,
          self.rightSideLabel,
          self.text, nil];
}

@end
