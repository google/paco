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
#import "ExperimentDAO.h"
#import "PacoExpressionExecutor.h"
#import "Input2.h"
#include "java/lang/Boolean.h"

#import "java/util/List.h"
#import "ExperimentGroup.h"

@interface PacoInputEvaluator ()

@property(nonatomic, strong) PAExperimentDAO* experiment;
@property(nonatomic, strong) PAExperimentGroup* group;

@property(nonatomic, strong) NSArray* visibleInputs;
// key: "inputName", value: inputValue
@property(nonatomic, strong) NSMutableDictionary* inputValueDict;
// key: "inputName", value: NSPredicate object
@property(nonatomic, strong) NSDictionary* expressionDict;
// key: "inputName", value: PacoExperimentInput object
@property(nonatomic, strong) NSDictionary* indexDict;

@end

@implementation PacoInputEvaluator

- (id)initWithExperimentGroup:(PAExperimentGroup*) group {
  self = [super init];
  if (self) {
      
    _group  = group;

    _inputValueDict =
        [NSMutableDictionary dictionaryWithCapacity:20];
   
    
    [self buildIndex];
  }
  return self;
}

+ (PacoInputEvaluator*)evaluatorWithExperimentGroup:(PAExperimentGroup*)group {
    
  return [[PacoInputEvaluator alloc] initWithExperimentGroup:group];
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
      [NSMutableDictionary dictionaryWithCapacity:20];
  for (PAInput2 * input in self.group.getInputs) {
    NSAssert([[input getName] length] > 0, @"input name should not be empty!");
    dict[[input getName]] = input;
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
    
    
  id<JavaUtilList> inputs  = [self.group getInputs];
  for (PAInput2 * input in  inputs ) {
    NSAssert([[input getName ] length] > 0, @"input name should non empty!");
    BOOL isMultiSelectedList = ([[input getResponseType] isEqualToString:@"list"]
                                && [input getMultiselect].booleanValue == true
                                );
      
    variableDict[[input getName] ] = @(isMultiSelectedList);
  }
  
  //run time: N
  NSMutableDictionary* dict = [NSMutableDictionary dictionary];
  for (PAInput2 * input in [[self.group getInputs] toArray]) {
    if (![input getConditional].booleanValue) {
      continue;
    }
      
      NSString*  rawExpression =  [input getConditionExpression];
      
    //we should be able to handle bad data on server safely
    if (0 == [rawExpression length]) { 
           continue;
    }
    
    void(^completionBlock)(NSPredicate*, NSArray*) =
        ^(NSPredicate* predicate, NSArray* dependencyVariables){
          if (predicate == nil) {
              
              // log error
              
                     }else {
                         
                         
                         
            dict[[input getName]] = predicate;
          }
          [self tagInputsAsDependency:dependencyVariables];
        };
    [PacoExpressionExecutor predicateWithRawExpression:rawExpression
                                withVariableDictionary:variableDict
                                              andBlock:completionBlock];
      
  }
  self.expressionDict = dict;
  
}

//run time: 2 * N
- (NSArray*)evaluateAllInputs {
  [self buildExpressionDictionaryIfNecessary];
  
  //run time: N
  for( PAInput2 *question in [self.group getInputs] )
  {
      NSString* conditionalExpression = [question getConditionExpression] ;
     if( [conditionalExpression length] != 0)
     {
          (self.inputValueDict)[[question getName]] =  [question getConditionExpression];
     }
      else
      {
          (self.inputValueDict)[[question getName]] =  [NSNull null];
          
      }
  }


  //run time: N
  NSMutableArray *questions = [NSMutableArray array];
  for (PAInput2   *question in [self.group getInputs] ) {
      
    BOOL visible =  [self evaluateSingleInput:question];
    if (visible) {
      [questions addObject:question];
    } else {
      //for the invisible inputs, their values are not valid to use for evaluating anymore, even if
      //their responseObject is not nil, so we should mark their values to be null 
      (self.inputValueDict)[[question getName]] = [NSNull null];
    }
  }
  self.visibleInputs = questions;
  return self.visibleInputs;
}

//In case of any possible error, we return YES so that those inputs can at least show up
- (BOOL)evaluateSingleInput:(PAInput2*)input{
  if (![input getConditional].booleanValue) {
    return YES;
  }
  NSPredicate* predicate = (self.expressionDict)[[input getName]];
  if (predicate == nil) {
  
    return YES;
  }
  
  BOOL satisfied = NO;
  @try {
    satisfied = [predicate evaluateWithObject:nil substitutionVariables:self.inputValueDict];    
  }
  @catch (NSException *exception) {
      
      
      
    satisfied = YES;
   
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
