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


#import "PacoInputEvaluator.h"
#import "PacoExperimentInput.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperiment.h"
#import "PacoExpressionExecutor.h"

@interface PacoInputEvaluator ()

@property(nonatomic, strong) PacoExperiment* experiment;
@property(nonatomic, strong) NSArray* visibleInputs;

@end

@implementation PacoInputEvaluator

- (id)initWithExperiment:(PacoExperiment*)experiment {
  self = [super init];
  if (self) {
    _experiment = experiment;
    NSAssert(_experiment.definition != nil, @"definition should not be nil!");
    [self tagQuestionsForDependencies];
  }
  return self;
}

+ (PacoInputEvaluator*)evaluatorWithExperiment:(PacoExperiment*)experiment {
  return [[PacoInputEvaluator alloc] initWithExperiment:experiment];
}

- (void)tagQuestionsForDependencies {
  for (PacoExperimentInput *input in self.experiment.definition.inputs) {
    input.isADependencyForOthers = NO;
  }
  for (PacoExperimentInput *input in self.experiment.definition.inputs) {
    if (input.conditional) {
      NSArray *expr = [PacoExpressionExecutor parseExpression:input.conditionalExpression];
      NSString *dependency = [expr objectAtIndex:0];
      for (PacoExperimentInput *input2 in self.experiment.definition.inputs) {
        if ([input2.name isEqualToString:dependency]) {
          input2.isADependencyForOthers = YES;
          break;
        }
      }
    }
  }
}

//validate all the inputs until we find the first invalid input
- (NSError*)validateVisibleInputs {
  NSError* error = nil;
  for (PacoExperimentInput* input in self.visibleInputs) {
    if (input.mandatory && input.responseObject == nil) {
      error = [NSError errorWithDomain:@"com.paco.userinput"
                                  code:-1
                              userInfo:@{NSLocalizedDescriptionKey : input.text}];
      break;
    }
  }
  return error;
}

- (NSArray*)evaluateAllInputs {
  NSMutableArray *questions = [NSMutableArray array];
  for (PacoExperimentInput *question in self.experiment.definition.inputs) {
    if (!question.conditional) {
      [questions addObject:question];
    } else {
      BOOL conditionsSatified = [self checkConditions:question];
      if (conditionsSatified) {
        [questions addObject:question];
      }
    }
  }
  
  self.visibleInputs = questions;
  return self.visibleInputs;
}


- (PacoExperimentInput *)questionByName:(NSString *)name {
  for (PacoExperimentInput *question in self.experiment.definition.inputs) {
    if ([question.name isEqualToString:name]) {
      return question;
    }
  }
  return nil;
}


- (BOOL)checkConditions:(PacoExperimentInput *)question {
  if (!question.conditional) {
    return YES;
  }
  
  if ([question.conditionalExpression length] == 0) {
    return NO;
  }
  NSArray *expr = [PacoExpressionExecutor parseExpression:question.conditionalExpression];
  NSString *questionName = [expr objectAtIndex:0];
  questionName = [questionName stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
  NSString *op = [expr objectAtIndex:1];
  op = [op stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
  NSString *value = [expr objectAtIndex:2];
  value = [value stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
  
  PacoExperimentInput *dependantQuestion = [self questionByName:questionName];
  
  // Apparently we can't find the parent question, so no use for this one.
  if (dependantQuestion == nil) {
    return NO;
  }
  
  // If the parent isn't answered yet, then hide this question.
  if (dependantQuestion.responseObject == nil) {
    return NO;
  }
  
  // If the dependent question is conditional, make sure it passes it's conditions
  // before proceeding to check the current ones.
  BOOL parentConditionalsPass = [self checkConditions:dependantQuestion];
  if (!parentConditionalsPass) {
    return NO;
  }
  
  // Prepare the value for the left hand side of the expression.
  int iValueLHS = [dependantQuestion intValueOfAnswer];
  
  // Prepare the value for the right hand side of the expression.
  int iValueRHS = [value intValue];
  
  // Evaluate the expression.
  BOOL satisfiesCondition = NO;
  if ([op isEqualToString:@">="]) {
    satisfiesCondition = iValueLHS >= iValueRHS;
  } else if ([op isEqualToString:@"<="]) {
    satisfiesCondition = iValueLHS <= iValueRHS;
  } else if ([op isEqualToString:@"=="]) {
    satisfiesCondition = iValueLHS == iValueRHS;
  } else if ([op isEqualToString:@"!="]) {
    satisfiesCondition = iValueLHS != iValueRHS;
  } else if ([op isEqualToString:@">"]) {
    satisfiesCondition = iValueLHS > iValueRHS;
  } else if ([op isEqualToString:@"<"]) {
    satisfiesCondition = iValueLHS < iValueRHS;
  } else if ([op isEqualToString:@"="]) {
    satisfiesCondition = iValueLHS == iValueRHS;
  } else {
    NSLog(@"Invalid operation [%@]", op);
  }
  return satisfiesCondition;
}




@end
