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

#import "PacoEvaluatorTests.h"
#import "PacoExpressionExecutor.h"

@interface PacoExpressionExecutor ()
+ (void)applyDollarSignForRawExpression:(NSString*)expression
                   withVariableNameList:(NSArray*)variableNameList
                              withBlock:(void(^)(NSString*, NSArray*))block;
@end


@implementation PacoEvaluatorTests

- (void)testDollarSign {
  NSString* exp1 = @"a1 == a10";
  NSArray* variableNames = @[@"a1", @"a10"];
  [PacoExpressionExecutor
      applyDollarSignForRawExpression:exp1
                 withVariableNameList:variableNames
                            withBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
    STAssertEqualObjects(finalExpression, @"$a1 == $a10", @"failed to apply $ correctly!");
    STAssertEqualObjects(dependencyVariables, variableNames, @"dependency not detected correctly!");
  }];
}

- (void)testUnderscore {
  NSString* exp1 = @"a_1 == a10";
  NSArray* variableNames = @[@"a_1", @"a10"];
  [PacoExpressionExecutor
   applyDollarSignForRawExpression:exp1
   withVariableNameList:variableNames
   withBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     STAssertEqualObjects(finalExpression, @"$a_1 == $a10", @"failed to parse underscore!");
     STAssertEqualObjects(dependencyVariables, variableNames, @"dependency not detected correctly!");
   }];
}

- (void)testHyphen {
  NSString* exp1 = @"a-1 == a10";
  NSArray* variableNames = @[@"a-1", @"a10"];
  [PacoExpressionExecutor
   applyDollarSignForRawExpression:exp1
   withVariableNameList:variableNames
   withBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     STAssertEqualObjects(finalExpression, @"$a-1 == $a10", @"failed to parse dash!");
     STAssertEqualObjects(dependencyVariables, variableNames, @"dependency not detected correctly!");
   }];
}

- (void)testContains {
  NSString* exp1 = @"a contains 1";
  NSArray* variableNames = @[@"a"];
  [PacoExpressionExecutor
   applyDollarSignForRawExpression:exp1
   withVariableNameList:variableNames
   withBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     STAssertEqualObjects(finalExpression, @"$a contains 1", @"failed to parse contains!");
     STAssertEqualObjects(dependencyVariables, variableNames, @"dependency not detected correctly!");
   }];
}

- (void)testListContains {
  NSString* exp1 = @"list contains 1";
  NSArray* variableNames = @[@"list"];
  
  [PacoExpressionExecutor
      predicateWithRawExpression:exp1
      withVariableNameList:variableNames
      andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
      NSDictionary* dict = @{@"list" : @[@2, @3]};
      BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
      STAssertFalse(satisfied, @"contains doesn't work");
      dict = @{@"list" : @[@1, @3]};
      satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
      STAssertTrue(satisfied, @"contains doesn't work");
  }];  
}

- (void)testListNotContains {
  NSString* exp1 = @"!(list contains 1)";
  NSArray* variableNames = @[@"list"];
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableNameList:variableNames
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @[@2, @3]};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertTrue(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@1, @3]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertFalse(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@3, @1, @4]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertFalse(satisfied, @"ListNotContains doesn't work");
 }];
}

- (void)testListNotContains2 {
  NSString* exp1 = @"not list contains 1";
  NSArray* variableNames = @[@"list"];
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableNameList:variableNames
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @[@2, @3]};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertTrue(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@1, @3]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertFalse(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@3, @1, @4]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertFalse(satisfied, @"ListNotContains doesn't work");
   }];
}

- (void)testListEquals {
  NSString* exp1 = @"list == 1";
  NSArray* variableNames = @[@"list"];
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableNameList:variableNames
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @[@1, @3]};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertFalse(satisfied, @"Equals shouldn't work");

     dict = @{@"list" : @[@1]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertFalse(satisfied, @"Equals shouldn't work");
   }];
}






@end
