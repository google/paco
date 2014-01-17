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
  input.conditional = [[inputMembers objectForKey:INPUT_CONDITIONAL] boolValue];
  input.conditionalExpression = [inputMembers objectForKey:INPUT_CONDITION_EXPRESSION];
  input.inputIdentifier = [NSString stringWithFormat:@"%ld", [[inputMembers objectForKey:INPUT_ID] longValue]];
  input.invisibleInput = [[inputMembers objectForKey:INPUT_INVISIBLE_INPUT] boolValue];
  input.leftSideLabel = [inputMembers objectForKey:INPUT_LEFT_SIDE_LABEL];
  input.likertSteps = [[inputMembers objectForKey:INPUT_LIKERT_STEPS] intValue];
  input.listChoices = [inputMembers objectForKey:INPUT_LIST_CHOICES];
  input.mandatory = [[inputMembers objectForKey:INPUT_MANDATORY] boolValue];
  input.multiSelect = [[inputMembers objectForKey:INPUT_MULTI_SELECT] boolValue];
  input.name = [inputMembers objectForKey:INPUT_NAME];
  input.questionType = [inputMembers objectForKey:INPUT_QUESTION_TYPE];
  input.responseType = [inputMembers objectForKey:INPUT_RESPONSE_TYPE];
  input.responseEnumType = [PacoExperimentInput responseEnumTypeFromString:input.responseType];
  input.rightSideLabel = [inputMembers objectForKey:INPUT_RIGHT_SIDE_LABEL];
  input.text = [inputMembers objectForKey:INPUT_TEXT];
  input.jsonObject = jsonObject;
  return input;
}


- (id)serializeToJSON {
  NSMutableDictionary* json = [NSMutableDictionary dictionary];
  [json setObject:[NSNumber numberWithBool:self.conditional] forKey:INPUT_CONDITIONAL];
  if (self.conditionalExpression) {
    [json setObject:self.conditionalExpression forKey:INPUT_CONDITION_EXPRESSION];
  }
  [json setObject:[NSNumber numberWithLongLong:[self.inputIdentifier longLongValue]] forKey:INPUT_ID];
  [json setObject:[NSNumber numberWithBool:self.invisibleInput] forKey:INPUT_INVISIBLE_INPUT];
  if (self.leftSideLabel) {
    [json setObject:self.leftSideLabel forKey:INPUT_LEFT_SIDE_LABEL];
  }
  if (self.likertSteps) {
    [json setObject:[NSNumber numberWithInteger:self.likertSteps] forKey:INPUT_LIKERT_STEPS];
  }
  if (self.listChoices) {
    [json setObject:self.listChoices forKey:INPUT_LIST_CHOICES];
  }
  [json setObject:[NSNumber numberWithBool:self.mandatory] forKey:INPUT_MANDATORY];
  [json setObject:[NSNumber numberWithBool:self.multiSelect] forKey:INPUT_MULTI_SELECT];
  [json setObject:self.name forKey:INPUT_NAME];
  if (self.questionType) {
    [json setObject:self.questionType forKey:INPUT_QUESTION_TYPE];
  }
  if (self.responseType) {
    [json setObject:self.responseType forKey:INPUT_RESPONSE_TYPE];
  }
  if (self.rightSideLabel) {
    [json setObject:self.rightSideLabel forKey:INPUT_RIGHT_SIDE_LABEL];
  }
  if (self.text) {
    [json setObject:self.text forKey:INPUT_TEXT];
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
      [choices addObject:[NSNumber numberWithInt:(itemIndex + 1)]]; //start from 1, not 0
    }
  }
  return choices;
}

- (NSArray*)choicesForList {
  if (ResponseEnumTypeList != self.responseEnumType) {
    return nil;
  }
  
  int sizeOfList = [self.listChoices count];
  NSArray* choices = [PacoExperimentInput choicesFromBitFlags:(NSNumber*)self.responseObject
                                                   sizeOfList:sizeOfList];
  return choices;
}

- (NSString*)stringForListChoices {
  if (![self.responseType isEqualToString:@"list"]) {
    return nil;
  }
  NSArray* choices = [self choicesForList];
  NSAssert(!(!self.multiSelect && [choices count] > 1), @"Radio list should not have more than one selection!");
  if (0 == [choices count]) {
    return @"";
  }else{
    return [choices componentsJoinedByString:@","];
  }
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentInput:%p - "
          @"conditional=%d "
          @"conditionalExpression=%@"
          @"inputIdentifier=%@ "
          @"invisibleInput=%d "
          @"leftSideLabel=%@ "
          @"likertSteps=%d "
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
          self.likertSteps,
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
  if (self.responseObject == nil) {
    return value;
  }
    
  ResponseEnumType answerType = self.responseEnumType;
  id answerObj = self.responseObject;
  switch (answerType) {
    case ResponseEnumTypeLikertSmileys:
      NSAssert([answerObj isKindOfClass:[NSNumber class]],
               @"The answer to likert_smileys should be a number!");
      value = [NSNumber numberWithInt:([answerObj intValue] + 1)];
      break;
      
    case ResponseEnumTypeLikert:
      NSAssert([answerObj isKindOfClass:[NSNumber class]],
               @"The answer to likert should be a number!");
      value = [NSNumber numberWithInt:([answerObj intValue] + 1)];
      break;
      
    case ResponseEnumTypeOpenText:  //YMZ:TODO: need to confirm this
      NSAssert([answerObj isKindOfClass:[NSString class]],
               @"The answer to open text should be a string!");
      value = answerObj;
      break;
      
    case ResponseEnumTypeList: 
      NSAssert([answerObj isKindOfClass:[NSNumber class]], @"The answer to list should be a number!");
      value = [self choicesForList];
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
  if (value == nil) {
    value = [NSNull null];
  }
  return value;
}

+ (NSString*)locationInfoFromResponse:(id)responseObject {
  CLLocation *location = responseObject;
  NSAssert([location isKindOfClass:[CLLocation class]],
           @"responseObject should be class of CLLocation!");
  NSString *locationString = [NSString stringWithFormat:@"(%f,%f)",
                                  location.coordinate.latitude, location.coordinate.longitude];
  return locationString;
}

- (id)payloadObject {
  if (self.responseObject == nil) {
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
      payload = [NSNumber numberWithInt:([self.responseObject intValue] + 1)];
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
