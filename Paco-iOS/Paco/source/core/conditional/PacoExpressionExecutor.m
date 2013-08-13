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

@implementation PacoExpressionExecutor

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


/*     "a > 1 && b == 2 || c == a", [a, b, c]
 *  ==>"$a > 1 && $b == 2 || $c == $a"
 **/
+ (void)applyDollarSignForRawExpression:(NSString*)expression
               withVariableNameList:(NSArray*)variableNameList
                          withBlock:(void(^)(NSString*, NSArray*))block {
  CFStringRef expressionRef = (__bridge CFStringRef)expression;
  CFLocaleRef locale = CFLocaleCopyCurrent();
  CFStringTokenizerRef tokenizer =
      CFStringTokenizerCreate(kCFAllocatorDefault,
                              expressionRef,
                              CFRangeMake(0, CFStringGetLength(expressionRef)),
                              kCFStringTokenizerUnitWord,
                              locale);
  
  NSMutableArray* tokenList = [NSMutableArray array];
  CFStringTokenizerTokenType tokenType = CFStringTokenizerAdvanceToNextToken(tokenizer);
  while(kCFStringTokenizerTokenNone != tokenType) {
    CFRange tokenRange = CFStringTokenizerGetCurrentTokenRange(tokenizer);
    CFStringRef tokenValue = CFStringCreateWithSubstring(kCFAllocatorDefault,
                                                         expressionRef,
                                                         tokenRange);
    
    NSString* tokenStr = (__bridge NSString*)tokenValue;
    [tokenList addObject:tokenStr];
    CFRelease(tokenValue);
    
    tokenType = CFStringTokenizerAdvanceToNextToken(tokenizer);
  }
  CFRelease(tokenizer);
  CFRelease(locale);
  
  NSString* newExpression = [expression copy];
  NSMutableArray* dependencyVariables = [NSMutableArray array];
  for (NSString* variableName in variableNameList) {
    if ([tokenList containsObject:variableName]) {
      [dependencyVariables addObject:variableName];

      NSString* replacement = [NSString stringWithFormat:@"$%@", variableName];
      newExpression = [newExpression stringByReplacingOccurrencesOfString:variableName
                                                               withString:replacement];
    }
  }
  block(newExpression, dependencyVariables);
}

+ (void)predicateWithRawExpression:(NSString*)rawExpression
              withVariableNameList:(NSArray*)variableNameList
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
  
  [PacoExpressionExecutor applyDollarSignForRawExpression:rawExpression
                                     withVariableNameList:variableNameList
                                                withBlock:block];
}



@end
