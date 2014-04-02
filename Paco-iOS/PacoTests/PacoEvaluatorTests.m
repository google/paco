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
  
  XCTAssertEqualObjects(tokenList,expectResult, @"tokenize isn't correct!");
}




#pragma mark pre-process
- (void)testDollarSign {
  NSString* exp1 = @"a1 == a10";
  NSDictionary* variableDict = @{@"a1" : @NO, @"a10":@NO};
  [PacoExpressionExecutor
      processRawExpression:exp1
      withVariableDictionary:variableDict
      andBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
    XCTAssertEqualObjects(finalExpression, @"$a1 == $a10", @"failed to apply $ correctly!");
    XCTAssertTrue([dependencyVariables count] == 2, @"dependency should have two objects!");
    XCTAssertTrue([dependencyVariables containsObject:@"a1" ], @"dependency should contain a1");
    XCTAssertTrue([dependencyVariables containsObject:@"a10"], @"dependency should contain a10");
  }];
}

- (void)testUnderscore {
  NSString* exp1 = @"a_1 == a10";
  NSDictionary* variableDict = @{@"a_1" : @NO, @"a10":@NO};
  [PacoExpressionExecutor
   processRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     XCTAssertEqualObjects(finalExpression, @"$a_1 == $a10", @"failed to parse underscore!");
     XCTAssertEqualObjects(dependencyVariables, [variableDict allKeys], @"dependency not detected correctly!");
   }];
}

- (void)testHyphen {
  NSString* exp1 = @"a-1 == a10";
  NSDictionary* variableDict = @{@"a-1" : @NO, @"a10":@NO};
  [PacoExpressionExecutor
   processRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     XCTAssertEqualObjects(finalExpression, @"$a-1 == $a10", @"failed to parse dash!");
     XCTAssertEqualObjects(dependencyVariables, [variableDict allKeys], @"dependency not detected correctly!");
   }];
}

- (void)testMultiSelectedList {
  NSString* exp = @" a >9 ||list  ==  1";
  NSDictionary* variableDict = @{@"a" : @NO, @"list":@YES};
  [PacoExpressionExecutor
   processRawExpression:exp
   withVariableDictionary:variableDict
   andBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     XCTAssertEqualObjects(finalExpression, @"$a > 9 || $list contains 1", @"failed to process == for list!");
     XCTAssertTrue([dependencyVariables count] == 2, @"dependency should have two objects!");
     XCTAssertTrue([dependencyVariables containsObject:@"a" ], @"dependency should contain a");
     XCTAssertTrue([dependencyVariables containsObject:@"list"], @"dependency should contain list");
   }];
}

- (void)testMultiSelectedList2 {
  NSString* exp = @" a >9 &&list  !=b";
  NSDictionary* variableDict = @{@"a" : @NO, @"list":@YES};
  [PacoExpressionExecutor
   processRawExpression:exp
   withVariableDictionary:variableDict
   andBlock:^(NSString* finalExpression, NSArray* dependencyVariables) {
     XCTAssertEqualObjects(finalExpression, @"$a > 9 && not $list contains b", @"failed to process != for list!");
     XCTAssertTrue([dependencyVariables count] == 2, @"dependency should have two objects!");
     XCTAssertTrue([dependencyVariables containsObject:@"a" ], @"dependency should contain a");
     XCTAssertTrue([dependencyVariables containsObject:@"list"], @"dependency should contain list");
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
     XCTAssertEqualObjects(finalExpression, @"$a contains 1", @"failed to parse contains!");
     XCTAssertEqualObjects(dependencyVariables, @[@"a"], @"dependency not detected correctly!");
   }];
}

#pragma mark predicate evaluation for multi-selected list
- (void)testMultiSelectedListContains {
  NSString* exp1 = @"list contains 1";
  NSDictionary* variableDict = @{@"list" : @YES};
  
  [PacoExpressionExecutor
      predicateWithRawExpression:exp1
      withVariableDictionary:variableDict
      andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
        NSDictionary* dict = @{@"list" : @[@2, @3]};
        BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
        XCTAssertFalse(satisfied, @"contains doesn't work");
        dict = @{@"list" : @[@1, @3]};
        satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
        XCTAssertTrue(satisfied, @"contains doesn't work");
  }];
}

- (void)testMultiSelectedListNotContains {
  NSString* exp1 = @"!(list contains 1)";
  NSDictionary* variableDict = @{@"list" : @YES};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @[@2, @3]};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@1, @3]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@3, @1, @4]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"ListNotContains doesn't work");
 }];
}

- (void)testMultiSelectedListNotContains2 {
  NSString* exp1 = @"not list contains 1";
  NSDictionary* variableDict = @{@"list" : @YES};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @[@2, @3]};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@1, @3]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@3, @1, @4]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"ListNotContains doesn't work");
   }];
}


- (void)testMultiSelectedListNotContainsCompound {
  NSString* exp1 = @"var > 1 ||not list contains 1";
  NSDictionary* variableDict = @{@"list" : @YES, @"var" : @NO};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @[@2, @3], @"var" : @0};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@1, @3],  @"var" : @0};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"ListNotContains doesn't work");
     dict = @{@"list" : @[@3, @1, @4],  @"var" : @2};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"ListNotContains doesn't work");
   }];
}


- (void)testMultiSelectedListEquals {
  NSString* exp1 = @"list == 1";
  NSDictionary* variableDict = @{@"list" : @YES};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @[@1, @3]};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"Equals shouldn't work");

     dict = @{@"list" : @[@2]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"Equals shouldn't work");
   }];
}

- (void)testMultiSelectedListNotEquals {
  NSString* exp1 = @"list != 1";
  NSDictionary* variableDict = @{@"list" : @YES};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @[@1, @3]};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"Equals shouldn't work");
     
     dict = @{@"list" : @[@2]};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"Equals shouldn't work");
   }];
}


#pragma mark predicate evaluation for single-selected list
- (void)testSingleSelectedListContains {
  NSString* exp1 = @"list contains 1";
  NSDictionary* variableDict = @{@"list" : @NO};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @1};
     XCTAssertThrows([predicate evaluateWithObject:nil substitutionVariables:dict],
                    @"Can't user contains for NSNumber, since it's not a collection");
   }];
}


- (void)testSingleSelectedListEquals {
  NSString* exp1 = @"list == 1";
  NSDictionary* variableDict = @{@"list" : @NO};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @1};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"list should be equal to 1");
     
     dict = @{@"list" : @2};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"list should not be equal to 1");
   }];
}

- (void)testSingleSelectedListNotEquals {
  NSString* exp1 = @"list != 1";
  NSDictionary* variableDict = @{@"list" : @NO};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @1};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"list should not be equal to 1");
     
     dict = @{@"list" : @2};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"list should not be equal to 1");
   }];
}

- (void)testSingleSelectedListLessThan {
  NSString* exp1 = @"list < 4";
  NSDictionary* variableDict = @{@"list" : @NO};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @1};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"list should be less than 4");
     
     dict = @{@"list" : @4};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"list should not be less than 4");

     dict = @{@"list" : @5};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"list should not be less than 4");
  }];
}

- (void)testSingleSelectedListLessThanOrEqualTo {
  NSString* exp1 = @"list <= 4";
  NSDictionary* variableDict = @{@"list" : @NO};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @1};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"list should be less than or equal to 4");
     
     dict = @{@"list" : @4};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"list should be less than or equal to 4");
     
     dict = @{@"list" : @5};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"list should not be less than or equal to 4");
   }];
}


- (void)testSingleSelectedListLargerThan {
  NSString* exp1 = @"list > 4";
  NSDictionary* variableDict = @{@"list" : @NO};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @1};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"list should be less than 4");
     
     dict = @{@"list" : @4};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"list should be equal to 4");
     
     dict = @{@"list" : @5};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"list should be larger than 4");
   }];
}

- (void)testSingleSelectedListLargerThanOrEqualTo {
  NSString* exp1 = @"list >= 4";
  NSDictionary* variableDict = @{@"list" : @NO};
  
  [PacoExpressionExecutor
   predicateWithRawExpression:exp1
   withVariableDictionary:variableDict
   andBlock:^(NSPredicate *predicate, NSArray *dependencyVariables) {
     NSDictionary* dict = @{@"list" : @1};
     BOOL satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertFalse(satisfied, @"list should less than 4");
     
     dict = @{@"list" : @4};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"list should be equal to 4");
     
     dict = @{@"list" : @5};
     satisfied = [predicate evaluateWithObject:nil substitutionVariables:dict];
     XCTAssertTrue(satisfied, @"list should larger than 4");
   }];
}






@end
