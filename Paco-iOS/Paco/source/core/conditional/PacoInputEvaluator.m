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
#import "PacoClient.h"

@interface PacoInputEvaluator ()

@property(nonatomic, strong) PacoExperiment* experiment;
@property(nonatomic, strong) NSArray* visibleInputs;
// key: "inputName", value: inputValue
@property(nonatomic, strong) NSMutableDictionary* inputValueDict;
// key: "inputName", value: NSPredicate object
@property(nonatomic, strong) NSDictionary* expressionDict;
// key: "inputName", value: PacoExperimentInput object
@property(nonatomic, strong) NSDictionary* indexDict;

@end

@implementation PacoInputEvaluator

- (id)initWithExperiment:(PacoExperiment*)experiment {
  self = [super init];
  if (self) {
    _experiment = experiment;
    _inputValueDict =
        [NSMutableDictionary dictionaryWithCapacity:[_experiment.definition.inputs count]];
    NSAssert(_experiment.definition != nil, @"definition should not be nil!");
    
    [self buildIndex];
  }
  return self;
}

+ (PacoInputEvaluator*)evaluatorWithExperiment:(PacoExperiment*)experiment {
  return [[PacoInputEvaluator alloc] initWithExperiment:experiment];
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

//run time: N
- (void)buildIndex {
  NSMutableDictionary* dict =
      [NSMutableDictionary dictionaryWithCapacity:[self.experiment.definition.inputs count]];
  for (PacoExperimentInput* input in self.experiment.definition.inputs) {
    NSAssert([input.name length] > 0, @"input name should not be empty!");
    dict[input.name] = input;
  }
  self.indexDict = dict;
}

- (void)tagInputsAsDependency:(NSArray*)inputNameList {
  for (NSString* name in inputNameList) {
    PacoExperimentInput* input = (self.indexDict)[name];
    NSAssert(input != nil, @"input should not be nil!");
    input.isADependencyForOthers = YES;
  }
}

//run time: 2 * N
- (void)buildExpressionDictionaryIfNecessary {
  //build expression dictionary lazily
  if (self.expressionDict != nil) {
    return;
  }
  
  //run time: N
  NSMutableDictionary* variableDict = [NSMutableDictionary dictionary];
  for (PacoExperimentInput* input in self.experiment.definition.inputs) {
    NSAssert([input.name length] > 0, @"input name should non empty!");
    BOOL isMultiSelectedList = (input.responseEnumType == ResponseEnumTypeList && input.multiSelect);
    variableDict[input.name] = @(isMultiSelectedList);
  }
  
  //run time: N
  NSMutableDictionary* dict = [NSMutableDictionary dictionary];
  for (PacoExperimentInput* input in self.experiment.definition.inputs) {
    if (!input.conditional) {
      continue;
    }
    NSString* rawExpression = input.conditionalExpression;
    //we should be able to handle bad data on server safely
    if (0 == [rawExpression length]) { 
      DDLogError(@"Error: expression should not be empty!");
      continue;
    }
    
    void(^completionBlock)(NSPredicate*, NSArray*) =
        ^(NSPredicate* predicate, NSArray* dependencyVariables){
          if (predicate == nil) {
            DDLogError(@"[ERROR]failed to create a predicate for inputName: %@, expression: %@",
                  input.name, rawExpression);
          }else {
            dict[input.name] = predicate;
          }
          [self tagInputsAsDependency:dependencyVariables];
        };
    [PacoExpressionExecutor predicateWithRawExpression:rawExpression
                                withVariableDictionary:variableDict
                                              andBlock:completionBlock];
  }
  
  self.expressionDict = dict;
  DDLogInfo(@"Finished building expression dict: \n %@", [self.expressionDict description]);
}

//run time: 2 * N
- (NSArray*)evaluateAllInputs {
  [self buildExpressionDictionaryIfNecessary];
  
  //run time: N
  for (PacoExperimentInput *question in self.experiment.definition.inputs) {
    (self.inputValueDict)[question.name] = [question valueForValidation];
  }

  //run time: N
  NSMutableArray *questions = [NSMutableArray array];
  for (PacoExperimentInput *question in self.experiment.definition.inputs) {
    BOOL visible =  [self evaluateSingleInput:question];
    if (visible) {
      [questions addObject:question];
    } else {
      //for the invisible inputs, their values are not valid to use for evaluating anymore, even if
      //their responseObject is not nil, so we should mark their values to be null 
      (self.inputValueDict)[question.name] = [NSNull null];
    }
  }
  self.visibleInputs = questions;
  return self.visibleInputs;
}

//In case of any possible error, we return YES so that those inputs can at least show up
- (BOOL)evaluateSingleInput:(PacoExperimentInput*)input{
  if (!input.conditional) {
    return YES;
  }
  NSPredicate* predicate = (self.expressionDict)[input.name];
  if (predicate == nil) {
    DDLogError(@"[ERROR]No predicate to evaluate inputName: %@", input.name);
    return YES;
  }
  
  BOOL satisfied = NO;
  @try {
    satisfied = [predicate evaluateWithObject:nil substitutionVariables:self.inputValueDict];    
  }
  @catch (NSException *exception) {
    satisfied = YES;
    DDLogError(@"[ERROR]Exception to evaluate single input: %@", [exception description]);
  }
  @finally {
    if (satisfied) {
      //NSLog(@"[Satisfied]InputName:%@, Expression:%@", input.name, input.conditionalExpression);
    }else {
      //NSLog(@"[NOT Satisfied]InputName:%@, Expression:%@", input.name, input.conditionalExpression);
    }
    return satisfied;
  }
}



@end
