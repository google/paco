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
+ (void)processRawExpression:(NSString*)expression
      withVariableDictionary:(NSDictionary*)variableDict
                    andBlock:(void(^)(NSString*, NSArray*))block;
+ (NSArray*)tokenize:(NSString*)input;
@end


@implementation PacoEvaluatorTests

#pragma mark ParseKit tokenizer
- (void)testStringTokenize {
  NSString* exp = @"a-0==2||b_1!=4 && (c0 >=5 || d<=8) &&c contains 1";
  NSArray* tokenList = [PacoExpressionExecutor tokenize:exp];
  NSArray* expectResult =
  @[@"a-0",@"==",@"2",@"||",@"b_1",@"!=",@"4",@"&&",@"(",@"c0",@">=",@"5",@"||",@"d",@"<=",@"8",@")",
    @"&&", @"c", @"contains", @"1"];
  
  STAssertEqualObjects(tokenList,expectResult, @"tokenize isn't correct!");
}




#pragma mark pre-process
- (void)testDollarSign {
  NSString* exp1 = @"a1 == a10";
  NSDictionary* variableDict = @{@"a1" : @NO, @"a10":@NO};
  [PacoExpressionExecutor
      processRawExpression:exp1
      withVariableDictionary:variableDict
      andBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
    STAssertEqualObjects(finalExpression, @"$a1 == $a10", @"failed to apply $ correctly!");
    STAssertTrue([dependencyVariables count] == 2, @"dependency should have two objects!");
    STAssertTrue([dependencyVariables containsObject:@"a1" ], @"dependency should contain a1");
    STAssertTrue([dependencyVariables containsObject:@"a10"], @"dependency should contain a10");
  }];
}

- (void)testUnderscore {
  NSString* exp1 = @"a_1 == a10";
  NSDictionary* variableDict = @{@"a_1" : @NO, @"a10":@NO};
  [PacoExpressionExecutor
   processRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     STAssertEqualObjects(finalExpression, @"$a_1 == $a10", @"failed to parse underscore!");
     STAssertEqualObjects(dependencyVariables, [variableDict allKeys], @"dependency not detected correctly!");
   }];
}

- (void)testHyphen {
  NSString* exp1 = @"a-1 == a10";
  NSDictionary* variableDict = @{@"a-1" : @NO, @"a10":@NO};
  [PacoExpressionExecutor
   processRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     STAssertEqualObjects(finalExpression, @"$a-1 == $a10", @"failed to parse dash!");
     STAssertEqualObjects(dependencyVariables, [variableDict allKeys], @"dependency not detected correctly!");
   }];
}

- (void)testList {
  NSString* exp = @" a >9 ||list  ==  1";
  NSDictionary* variableDict = @{@"a" : @NO, @"list":@YES};
  [PacoExpressionExecutor
   processRawExpression:exp
   withVariableDictionary:variableDict
   andBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     STAssertEqualObjects(finalExpression, @"$a > 9 || $list contains 1", @"failed to process == for list!");
     STAssertTrue([dependencyVariables count] == 2, @"dependency should have two objects!");
     STAssertTrue([dependencyVariables containsObject:@"a" ], @"dependency should contain a");
     STAssertTrue([dependencyVariables containsObject:@"list"], @"dependency should contain list");
   }];
}

- (void)testList2 {
  NSString* exp = @" a >9 &&list  !=b";
  NSDictionary* variableDict = @{@"a" : @NO, @"list":@YES};
  [PacoExpressionExecutor
   processRawExpression:exp
   withVariableDictionary:variableDict
   andBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     STAssertEqualObjects(finalExpression, @"$a > 9 && not $list contains b", @"failed to process != for list!");
     STAssertTrue([dependencyVariables count] == 2, @"dependency should have two objects!");
     STAssertTrue([dependencyVariables containsObject:@"a" ], @"dependency should contain a");
     STAssertTrue([dependencyVariables containsObject:@"list"], @"dependency should contain list");
   }];
  
}



#pragma mark predicate evaluation
- (void)testContains {
  NSString* exp1 = @"a contains 1";
  NSDictionary* variableDict = @{@"a" : @NO, @"b" : @NO};
  [PacoExpressionExecutor
   processRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     STAssertEqualObjects(finalExpression, @"$a contains 1", @"failed to parse contains!");
     STAssertEqualObjects(dependencyVariables, @[@"a"], @"dependency not detected correctly!");
   }];
}

- (void)testListContains {
  NSString* exp1 = @"list contains 1";
  NSDictionary* variableDict = @{@"list" : @YES};
  
  [PacoExpressionExecutor
      predicateWithRawExpression:exp1
      withVariableDictionary:variableDict
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
  NSDictionary* variableDict = @{@"list" : @YES};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
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
  NSDictionary* variableDict = @{@"list" : @YES};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
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


- (void)testListNotContainsCompound {
  NSString* exp1 = @"var > 1 ||not list contains 1";
  NSDictionary* variableDict = @{@"list" : @YES, @"var" : @NO};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @[@2, @3], @"var" : @0};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertTrue(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@1, @3],  @"var" : @0};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertFalse(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@3, @1, @4],  @"var" : @2};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertTrue(satisfied, @"ListNotContains doesn't work");
   }];
}


- (void)testListEquals {
  NSString* exp1 = @"list == 1";
  NSDictionary* variableDict = @{@"list" : @YES};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @[@1, @3]};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertTrue(satisfied, @"Equals shouldn't work");

     dict = @{@"list" : @[@2]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertFalse(satisfied, @"Equals shouldn't work");
   }];
}

- (void)testListNotEquals {
  NSString* exp1 = @"list != 1";
  NSDictionary* variableDict = @{@"list" : @YES};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @[@1, @3]};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertFalse(satisfied, @"Equals shouldn't work");
     
     dict = @{@"list" : @[@2]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     STAssertTrue(satisfied, @"Equals shouldn't work");
   }];
}







@end
