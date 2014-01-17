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

typedef enum {
  ResponseEnumTypeLikertSmileys,
  ResponseEnumTypeLikert,
  ResponseEnumTypeOpenText,
  ResponseEnumTypeList,
  ResponseEnumTypeNumber,
  ResponseEnumTypeLocation,
  ResponseEnumTypePhoto,
  ResponseEnumTypeInvalid
} ResponseEnumType;

// ExperimentInput is basically something like a question, or measure of some input like a location or photo.
@interface PacoExperimentInput : NSObject

@property (nonatomic, assign) BOOL conditional;
@property (nonatomic, retain) NSString *conditionalExpression;
@property (nonatomic, copy) NSString *inputIdentifier;
@property (nonatomic, assign) BOOL invisibleInput;
@property (nonatomic, copy) NSString *leftSideLabel;
@property (nonatomic, assign) NSInteger likertSteps; // only for response type 'likert'
@property (nonatomic, retain) NSArray *listChoices; // <NSString>
@property (nonatomic, assign) BOOL mandatory;
@property (nonatomic, assign) BOOL multiSelect;
@property (nonatomic, copy) NSString *name;
@property (nonatomic, copy) NSString *questionType;  // 'question'/ (text question or sensor input)
@property (nonatomic, copy) NSString *responseType;  // 'likert', 'list', open text, etc.
@property (nonatomic, assign) ResponseEnumType responseEnumType;  
@property (nonatomic, copy) NSString *rightSideLabel;
@property (nonatomic, copy) NSString *text;
@property (nonatomic, retain) id jsonObject;
@property (nonatomic, retain) id responseObject;  // The user's answer to this question
@property (nonatomic, assign) BOOL isADependencyForOthers;

+ (id)pacoExperimentInputFromJSON:(id)jsonObject;
- (id)serializeToJSON;

- (NSString*)stringForListChoices;
- (id)valueForValidation;
- (id)payloadObject;

@end
