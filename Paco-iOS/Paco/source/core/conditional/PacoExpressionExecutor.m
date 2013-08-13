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


//Inputs: "a > 1 && b == 2 || c == a", [a, b, c]
//Output: "$a > 1 && $b == 2 || $c == $a"
+ (NSString*)applyDollarSignForRawExpression:(NSString*)expression
                         withVariableNameList:(NSArray*)variableNameList {
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
  for (NSString* variableName in variableNameList) {
    if ([tokenList containsObject:variableName]) {
      NSString* replacement = [NSString stringWithFormat:@"$%@", variableName];
      newExpression = [newExpression stringByReplacingOccurrencesOfString:variableName
                                                 withString:replacement];
    }
  }
  return newExpression;
}

+ (NSPredicate*)predicateWithRawExpression:(NSString*)rawExpression
                       withVariableNameList:(NSArray*)variableNameList {
  NSString* expression = [PacoExpressionExecutor applyDollarSignForRawExpression:rawExpression
                                           withVariableNameList:variableNameList];
  NSPredicate* pred = nil;
  @try {
    pred = [NSPredicate predicateWithFormat:expression];
  }
  @catch (NSException *exception) {
    NSLog(@"Failed to create predicate from string: %@", expression);
    return nil;
  }
  return pred;
}


@end
