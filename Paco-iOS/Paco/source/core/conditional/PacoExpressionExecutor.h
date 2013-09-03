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


#import <Foundation/Foundation.h>

typedef void(^PredicateBlock)(NSPredicate* predicate, NSArray* dependencyVariables);

@interface PacoExpressionExecutor : NSObject

/*
 rawExpression : "a contains 1 && b > 7"
 variableDict: {"a":YES, "b":NO}
 YES: variable is a list, NO: variable is not a list
 **/
+ (void)predicateWithRawExpression:(NSString*)rawExpression
            withVariableDictionary:(NSDictionary*)variableDict
                          andBlock:(PredicateBlock)completionBlock;


@end
