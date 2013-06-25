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

// ExperimentInput is basically something like a question, or measure of some input like a location or photo.
@interface PacoExperimentInput : NSObject

@property (assign) BOOL conditional;
@property (retain) NSString *conditionalExpression;
@property (copy) NSString *inputIdentifier;
@property (assign) BOOL invisibleInput;
@property (copy) NSString *leftSideLabel;
@property (assign) NSInteger likertSteps; // only for response type 'likert'
@property (retain) NSArray *listChoices; // <NSString>
@property (assign) BOOL mandatory;
@property (copy) NSString *name;
@property (copy) NSString *questionType;  // 'question'/ (text question or sensor input)
@property (copy) NSString *responseType;  // 'likert', 'list', open text, etc.
@property (copy) NSString *rightSideLabel;
@property (copy) NSString *text;
@property (retain) id jsonObject;
@property (retain) id responseObject;  // The user's answer to this question
@property (assign) BOOL isADependencyForOthers;

+ (id)pacoExperimentInputFromJSON:(id)jsonObject;
+ (NSArray *)parseExpression:(NSString *)expr;

@end
