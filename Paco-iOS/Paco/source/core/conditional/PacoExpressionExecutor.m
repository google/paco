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


#import "PacoExpressionExecutor.h"
#import "ParseKit.h"

@implementation PacoExpressionExecutor


+ (PKTokenizer*)tokenizer {
  static PKTokenizer* pacoTokenizer = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    pacoTokenizer = [PKTokenizer tokenizer];
    [pacoTokenizer.symbolState add:@"=="];
    [pacoTokenizer.symbolState add:@"&&"];
    [pacoTokenizer.symbolState add:@"||"];
    [pacoTokenizer.symbolState add:@"!="];
  });
  return pacoTokenizer;
}

+ (NSArray*)tokenize:(NSString*)input {
  PKTokenizer* tokenizer = [PacoExpressionExecutor tokenizer];
  tokenizer.string = input;
  
  NSMutableArray* tokenList = [NSMutableArray array];
  PKToken* token = [tokenizer nextToken];  
  while(token != [PKToken EOFToken]) {
    [tokenList addObject:token.stringValue];
    token = [tokenizer nextToken];
  }
  return tokenList;
}


+ (void)processRawExpression:(NSString*)expression
      withVariableDictionary:(NSDictionary*)variableDict
                    andBlock:(void(^)(NSString*, NSArray*))block {
  NSArray* tokenList = [PacoExpressionExecutor tokenize:expression];
  NSMutableArray* dependencyVariables = [NSMutableArray array];
  
  NSMutableArray* processedTokens = [NSMutableArray arrayWithArray:tokenList];
  for (int index=0; index < [tokenList count]; index++) {
    NSString* token = tokenList[index];
    NSNumber* value = variableDict[token];
    if (value == nil) { //not a variable
      continue;
    }
    
    if (![dependencyVariables containsObject:token]) {
      [dependencyVariables addObject:token];
    }
    NSAssert([value isKindOfClass:[NSNumber class]], @"value should be a number!");
    
    NSString* newToken = [NSString stringWithFormat:@"$%@", token];
    int nextIndex = index + 1;
    if ([value boolValue] &&  nextIndex < [tokenList count]) {//variable is a multi-selected list
      NSString* nextToken = tokenList[nextIndex];
      if ([nextToken isEqualToString:@"=="] || [nextToken isEqualToString:@"!="]) {
        processedTokens[nextIndex] = @"contains";
        if ([nextToken isEqualToString:@"!="]) {
          newToken = [NSString stringWithFormat:@"not %@", newToken];
        }
      }
    }
    processedTokens[index] = newToken;
  }
  NSString* newExpression = [processedTokens componentsJoinedByString:@" "];
  block(newExpression, dependencyVariables);
}


+ (void)predicateWithRawExpression:(NSString*)rawExpression
            withVariableDictionary:(NSDictionary*)variableDict
                          andBlock:(PredicateBlock)completionBlock {
  void(^block)(NSString*, NSArray*) = ^(NSString *newExpression, NSArray *dependencyVariables) {
    NSPredicate* pred = nil;
    @try {
      pred = [NSPredicate predicateWithFormat:newExpression];
    }
    @catch (NSException *exception) {
      NSLog(@"Failed to create predicate from string: %@", newExpression);
      completionBlock(nil, dependencyVariables);
      return;
    }
    completionBlock(pred, dependencyVariables);
  };
  
  [PacoExpressionExecutor processRawExpression:rawExpression
                        withVariableDictionary:variableDict
                                      andBlock:block];
}




@end
