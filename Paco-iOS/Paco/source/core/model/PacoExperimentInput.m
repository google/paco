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

#import "PacoExperimentInput.h"
#import <CoreLocation/CoreLocation.h>

static NSString* const INPUT_CONDITIONAL = @"conditional";
static NSString* const INPUT_CONDITION_EXPRESSION = @"conditionExpression";
static NSString* const INPUT_ID = @"id";
static NSString* const INPUT_INVISIBLE_INPUT = @"invisibleInput";
static NSString* const INPUT_LEFT_SIDE_LABEL = @"leftSideLabel";
static NSString* const INPUT_RIGHT_SIDE_LABEL = @"rightSideLabel";
static NSString* const INPUT_LIKERT_STEPS = @"likertSteps";
static NSString* const INPUT_LIST_CHOICES = @"listChoices";
static NSString* const INPUT_MANDATORY = @"mandatory";
static NSString* const INPUT_MULTI_SELECT = @"multiselect";
static NSString* const INPUT_NAME = @"name";
static NSString* const INPUT_QUESTION_TYPE = @"questionType";
static NSString* const INPUT_RESPONSE_TYPE = @"responseType";
static NSString* const INPUT_TEXT = @"text";

@implementation PacoExperimentInput

+ (id)pacoExperimentInputFromJSON:(id)jsonObject {
  PacoExperimentInput *input = [[PacoExperimentInput alloc] init];
  NSDictionary *inputMembers = jsonObject;
  input.conditional = [inputMembers[INPUT_CONDITIONAL] boolValue];
  input.conditionalExpression = inputMembers[INPUT_CONDITION_EXPRESSION];
  input.inputIdentifier = [NSString stringWithFormat:@"%lld", [inputMembers[INPUT_ID] longLongValue]];
  input.invisibleInput = [inputMembers[INPUT_INVISIBLE_INPUT] boolValue];
  input.leftSideLabel = inputMembers[INPUT_LEFT_SIDE_LABEL];
  input.likertSteps = [inputMembers[INPUT_LIKERT_STEPS] intValue];
  input.listChoices = inputMembers[INPUT_LIST_CHOICES];
  input.mandatory = [inputMembers[INPUT_MANDATORY] boolValue];
  input.multiSelect = [inputMembers[INPUT_MULTI_SELECT] boolValue];
  input.name = inputMembers[INPUT_NAME];
  input.questionType = inputMembers[INPUT_QUESTION_TYPE];
  input.responseType = inputMembers[INPUT_RESPONSE_TYPE];
  input.responseEnumType = [PacoExperimentInput responseEnumTypeFromString:input.responseType];
  input.rightSideLabel = inputMembers[INPUT_RIGHT_SIDE_LABEL];
  input.text = inputMembers[INPUT_TEXT];
  return input;
}

//NOTE: responseObject is not copied
- (id)copyWithZone:(NSZone *)zone {
  PacoExperimentInput* copy = [[[self class] allocWithZone:zone] init];
  copy.conditional = self.conditional;
  copy.conditionalExpression = [self.conditionalExpression copyWithZone:zone];
  copy.inputIdentifier = [self.inputIdentifier copyWithZone:zone];
  copy.invisibleInput = self.invisibleInput;
  copy.leftSideLabel = [self.leftSideLabel copyWithZone:zone];
  copy.likertSteps = self.likertSteps;
  copy.listChoices = [self.listChoices copyWithZone:zone];
  copy.mandatory = self.mandatory;
  copy.multiSelect = self.multiSelect;
  copy.name = [self.name copyWithZone:zone];
  copy.questionType = [self.questionType copyWithZone:zone];
  copy.responseType = [self.responseType copyWithZone:zone];
  copy.responseEnumType = self.responseEnumType;
  copy.rightSideLabel = [self.rightSideLabel copyWithZone:zone];
  copy.text = [self.text copyWithZone:zone];
  copy.isADependencyForOthers = self.isADependencyForOthers;
  return copy;
}


- (id)serializeToJSON {
  NSMutableDictionary* json = [NSMutableDictionary dictionary];
  json[INPUT_CONDITIONAL] = @(self.conditional);
  if (self.conditionalExpression) {
    json[INPUT_CONDITION_EXPRESSION] = self.conditionalExpression;
  }
  json[INPUT_ID] = @([self.inputIdentifier longLongValue]);
  json[INPUT_INVISIBLE_INPUT] = @(self.invisibleInput);
  if (self.leftSideLabel) {
    json[INPUT_LEFT_SIDE_LABEL] = self.leftSideLabel;
  }
  if (self.likertSteps) {
    json[INPUT_LIKERT_STEPS] = @(self.likertSteps);
  }
  if (self.listChoices) {
    json[INPUT_LIST_CHOICES] = self.listChoices;
  }
  json[INPUT_MANDATORY] = @(self.mandatory);
  json[INPUT_MULTI_SELECT] = @(self.multiSelect);
  
  if (self.name && self.name.length > 0) {
    json[INPUT_NAME] = self.name;
  } else {
    json[INPUT_NAME] = @"";
  }
  
  if (self.questionType) {
    json[INPUT_QUESTION_TYPE] = self.questionType;
  }
  if (self.responseType) {
    json[INPUT_RESPONSE_TYPE] = self.responseType;
  }
  if (self.rightSideLabel) {
    json[INPUT_RIGHT_SIDE_LABEL] = self.rightSideLabel;
  }
  if (self.text) {
    json[INPUT_TEXT] = self.text;
  }
  return json;
}


+ (ResponseEnumType)responseEnumTypeFromString:(NSString*)responseTypeString {
  ResponseEnumType enumType = ResponseEnumTypeInvalid;
  
  if ([responseTypeString isEqualToString:@"likert_smileys"]) {
    enumType = ResponseEnumTypeLikertSmileys;
    
  } else if ([responseTypeString isEqualToString:@"likert"]) {
    enumType = ResponseEnumTypeLikert;
    
  } else if ([responseTypeString isEqualToString:@"open text"]) { 
    enumType = ResponseEnumTypeOpenText;
    
  } else if ([responseTypeString isEqualToString:@"list"]) {
    enumType = ResponseEnumTypeList;
    
  } else if ([responseTypeString isEqualToString:@"number"]) {
    enumType = ResponseEnumTypeNumber;
    
  } else if ([responseTypeString isEqualToString:@"location"]) {
    enumType = ResponseEnumTypeLocation;
    
  } else if ([responseTypeString isEqualToString:@"photo"]) {
    enumType = ResponseEnumTypePhoto;
    
  } else {
    NSAssert1(NO, @"[ERROR]responseType %@ is not implemented!", responseTypeString);
  }
  return enumType;
}


+ (NSArray*)choicesFromBitFlags:(NSNumber*)bitFlags sizeOfList:(int)sizeOfList {
  NSAssert(sizeOfList > 0, @"sizeOfList should be larger than 0!");
  
  NSMutableArray* choices = [NSMutableArray arrayWithCapacity:sizeOfList];
  unsigned int result = [bitFlags unsignedIntValue];
  if (0 == result) {
    return choices;
  }
  
  for (int itemIndex = 0; itemIndex < sizeOfList; itemIndex++) {
    unsigned int flag = (1 << itemIndex);
    unsigned int value = (result & flag);
    unsigned int bitValue = value >> itemIndex;
    NSAssert(bitValue == 1 || bitValue == 0, @"bitValue is not correct!");
    if (bitValue == 1) {
      [choices addObject:@(itemIndex + 1)]; //start from 1, not 0
    }
  }
  return choices;
}

//return a NSNumber object or nil for single-selected list
//return a NSArray object or nil for multi-selected list
- (id)answerForList {
  if (ResponseEnumTypeList != self.responseEnumType) {
    return nil;
  }
  
  int sizeOfList = (int)[self.listChoices count];
  NSArray* choices = [PacoExperimentInput choicesFromBitFlags:(NSNumber*)self.responseObject
                                                   sizeOfList:sizeOfList];
  if (self.multiSelect) {
    return choices;
  } else {
    NSAssert([choices count] < 2, @"Radio list should not have more than one selection!");
    return [choices firstObject];
  }
}

- (NSString*)stringForListChoices {
  if (![self.responseType isEqualToString:@"list"]) {
    return nil;
  }
  id answer = [self answerForList];
  if (!answer) {
    return @"";
  }
  
  if (self.multiSelect) {
    NSAssert([answer isKindOfClass:[NSArray class]], @"answer should be an array for multi-select list");
    if (0 == [answer count]) {
      return @"";
    }else{
      return [answer componentsJoinedByString:@","];
    }
  } else {
    NSAssert([answer isKindOfClass:[NSNumber class]], @"answer should be a number for single-select list");
    return [NSString stringWithFormat:@"%d", [answer intValue]];
  }
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentInput:%p - "
          @"conditional=%d "
          @"conditionalExpression=%@"
          @"inputIdentifier=%@ "
          @"invisibleInput=%d "
          @"leftSideLabel=%@ "
          @"likertSteps=%ld "
          @"listChoices=%@ "
          @"mandatory=%d "
          @"multiSelect=%@ "
          @"name=%@ "
          @"questionType=%@ "
          @"responseType=%@ "
          @"rightSideLabel=%@ "
          @"text=%@ >",
          self,
          self.conditional,
          self.conditionalExpression,
          self.inputIdentifier,
          self.invisibleInput,
          self.leftSideLabel,
          (long)self.likertSteps,
          self.listChoices,
          self.mandatory,
          self.multiSelect ? @"YES" : @"NO",
          self.name,
          self.questionType,
          self.responseType,
          self.rightSideLabel,
          self.text, nil];
}


- (id)valueForValidation {
  //default value is null
  id value = [NSNull null];
  if (![self.questionType isEqualToString:@"question"]) {
    NSLog(@"[ERROR]input's questionType is not equal to question!");
    return value;
  }
  if (!self.responseObject) {
    return value;
  }
    
  ResponseEnumType answerType = self.responseEnumType;
  id answerObj = self.responseObject;
  switch (answerType) {
    case ResponseEnumTypeLikertSmileys:
      NSAssert([answerObj isKindOfClass:[NSNumber class]],
               @"The answer to likert_smileys should be a number!");
      value = @([answerObj intValue] + 1);
      break;
      
    case ResponseEnumTypeLikert:
      NSAssert([answerObj isKindOfClass:[NSNumber class]],
               @"The answer to likert should be a number!");
      value = @([answerObj intValue] + 1);
      break;
      
    case ResponseEnumTypeOpenText:  //YMZ:TODO: need to confirm this
      NSAssert([answerObj isKindOfClass:[NSString class]],
               @"The answer to open text should be a string!");
      value = answerObj;
      break;
      
    case ResponseEnumTypeList: 
      NSAssert([answerObj isKindOfClass:[NSNumber class]], @"The answer to list should be a number!");
      value = [self answerForList];
      break;
      
    case ResponseEnumTypeNumber:
      NSAssert([answerObj isKindOfClass:[NSNumber class]], @"The answer to number should be a number!");
      NSAssert([answerObj intValue] >= 0, @"The answer to number should be larger than or equal to 0!");
      value = answerObj;
      break;
      
    case ResponseEnumTypeLocation: //YMZ:TODO: need to confirm this
      break;
      
    case ResponseEnumTypePhoto: //YMZ:TODO: need to confirm this
      break;
      
    default:
      NSAssert(NO, @"Invalid response enum type!");
      break;
  }
  if (!value) {
    value = [NSNull null];
  }
  return value;
}

+ (NSString*)locationInfoFromResponse:(id)responseObject {
  CLLocation *location = responseObject;
  NSAssert([location isKindOfClass:[CLLocation class]],
           @"responseObject should be class of CLLocation!");
  NSString *locationString = [NSString stringWithFormat:@"%f,%f",
                                  location.coordinate.latitude, location.coordinate.longitude];
  return locationString;
}

- (id)payloadObject {
  if (!self.responseObject) {
    return nil;
  }
  if (![self.questionType isEqualToString:@"question"]) {
    NSAssert1(NO, @"questionType %@ is NOT implemented!", self.questionType);
    return nil;
  }
  
  id payload = nil;
  switch (self.responseEnumType) {
    case ResponseEnumTypeLikertSmileys:
    case ResponseEnumTypeLikert://result starts from 1, not 0
      payload = @([self.responseObject intValue] + 1);
      break;

    case ResponseEnumTypeOpenText:
      NSAssert([self.responseObject isKindOfClass:[NSString class]],
               @"responseObject should be a string!");
      payload = self.responseObject;
      break;
      
    case ResponseEnumTypeList:
      payload = [self stringForListChoices];
      break;
      
    case ResponseEnumTypeNumber:
      payload = self.responseObject;
      break;
      
    case ResponseEnumTypePhoto:
      payload = self.responseObject;//UIImage object
      break;

    case ResponseEnumTypeLocation:
      payload = [PacoExperimentInput locationInfoFromResponse:self.responseObject];
      break;
      
    default:
      NSAssert(NO, @"Invalid response type!");
      break;
  }
  NSAssert(payload!=nil, @"payload should not be nil!");
  return payload;
}

@end
