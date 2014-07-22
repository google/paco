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

#import "PacoEvent.h"
#import "PacoDateUtility.h"
#import "PacoClient.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperimentInput.h"
#import <CoreLocation/CoreLocation.h>
#import "NSString+Paco.h"
#import "UIImage+Paco.h"

static NSString* const kPacoEventKeyWho = @"who";
static NSString* const kPacoEventKeyWhen = @"when";
static NSString* const kPacoEventKeyLatitude = @"lat";
static NSString* const kPacoEventKeyLongitude = @"long";
static NSString* const kPacoEventKeyResponseTime = @"responseTime";
static NSString* const kPacoEventKeyAppId = @"appId";
static NSString* const kPacoEventKeyScheduledTime = @"scheduledTime";
static NSString* const kPacoEventKeyPacoVersion = @"pacoVersion";
static NSString* const kPacoEventKeyExperimentId = @"experimentId";
static NSString* const kPacoEventKeyExperimentName = @"experimentName";
static NSString* const kPacoEventKeyExperimentVersion = @"experimentVersion";
static NSString* const kPacoEventKeyResponses = @"responses";

NSString* const kPacoResponseKeyName = @"name";
NSString* const kPacoResponseKeyAnswer = @"answer";
NSString* const kPacoResponseKeyInputId = @"inputId";

NSString* const kPacoResponseJoin = @"joined";

@interface PacoEvent ()
@property (nonatomic, readwrite, copy) NSString *appId;
@property (nonatomic, readwrite, copy) NSString *pacoVersion;
@end

@implementation PacoEvent

- (id)init {
  self = [super init];
  if (self) {
    _appId = @"iOS";
    
    NSString *version = [[NSBundle mainBundle] infoDictionary][(NSString*)kCFBundleVersionKey];
    NSAssert([version length] > 0, @"version number is not valid!");
    _pacoVersion = version;
  }
  return self;
}

+ (id)pacoEventForIOS {
  return [[PacoEvent alloc] init];
}


+ (id)pacoEventFromJSON:(id)jsonObject {
  PacoEvent *event = [[PacoEvent alloc] init];
  NSDictionary *eventMembers = jsonObject;
  event.who = eventMembers[kPacoEventKeyWho];
  event.when = [PacoDateUtility pacoDateForString:eventMembers[kPacoEventKeyWhen]];
  event.latitude = [eventMembers[kPacoEventKeyLatitude] longLongValue];
  event.longitude = [eventMembers[kPacoEventKeyLongitude] longLongValue];
  event.responseTime = [PacoDateUtility pacoDateForString:eventMembers[kPacoEventKeyResponseTime]];
  event.scheduledTime = [PacoDateUtility pacoDateForString:eventMembers[kPacoEventKeyScheduledTime]];
  event.appId = eventMembers[kPacoEventKeyAppId];
  event.pacoVersion = eventMembers[kPacoEventKeyPacoVersion];
  event.experimentId = eventMembers[kPacoEventKeyExperimentId];
  event.experimentName = eventMembers[kPacoEventKeyExperimentName];
  event.experimentVersion = [eventMembers[kPacoEventKeyExperimentVersion] intValue];
  event.responses = eventMembers[kPacoEventKeyResponses];
  return event;
}


- (PacoEventType)type {
  for (NSDictionary *response in self.responses) {
    if ([response[kPacoResponseKeyName] isEqualToString:kPacoResponseJoin]) {
      return [response[kPacoResponseKeyAnswer] boolValue] ? PacoEventTypeJoin : PacoEventTypeStop;
    }
  }
  if (self.scheduledTime && self.responseTime) {
    return PacoEventTypeSurvey;
  } else if (self.scheduledTime && !self.responseTime) {
    return PacoEventTypeMiss;
  } else {
    NSAssert(self.responseTime, @"responseTime should be valid for self report event");
    return PacoEventTypeSelfReport;
  }
}


- (NSString*)description {
  NSString* responseStr = @"[";
  NSUInteger numOfResponse = [self.responses count];
  int index = 0;
  for (NSDictionary* responseDict in self.responses) {
    responseStr = [responseStr stringByAppendingString:@"{"];
    NSAssert([responseDict isKindOfClass:[NSDictionary class]], @"responseDict should be a dictionary!");
    
    NSUInteger numOfKeyValue = [[responseDict allKeys] count];
    int temp = 0;
    for (NSString* key in responseDict) {
      responseStr = [responseStr stringByAppendingString:key];
      responseStr = [responseStr stringByAppendingString:@":"];
      responseStr = [responseStr stringByAppendingString:[responseDict[key] description]];
      temp++;
      if (temp < numOfKeyValue) {
        responseStr = [responseStr stringByAppendingString:@","];
      }
    }
    responseStr = [responseStr stringByAppendingString:@"}"];
    
    index++;
    if (index < numOfResponse) {
      responseStr = [responseStr stringByAppendingString:@", "];
    }
  }
  responseStr = [responseStr stringByAppendingString:@"]"];
  
  NSDateFormatter* dateFormatter = [[NSDateFormatter alloc] init];
  [dateFormatter setDateFormat:@"yyyy/MM/dd HH:mm:ssZ"];
  NSString* formattedTime = [dateFormatter stringFromDate:self.responseTime];
  return [NSString stringWithFormat:@"<%@, %p: id=%@,name=%@,version=%d,responseTime=%@,"
                                      "who=%@,when=%@,response=\r%@>",
                                     NSStringFromClass([self class]),
                                     self,
                                     self.experimentId,
                                     self.experimentName,
                                     self.experimentVersion,
                                     formattedTime,
                                     self.who,
                                     self.when,
                                     responseStr];
}


- (id)generateJsonObject {
  NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
  dictionary[kPacoEventKeyExperimentId] = self.experimentId;
  dictionary[kPacoEventKeyExperimentName] = self.experimentName;
  dictionary[kPacoEventKeyExperimentVersion] = [NSString stringWithFormat:@"%d", self.experimentVersion];
  dictionary[kPacoEventKeyWho] = self.who;
  dictionary[kPacoEventKeyAppId] = self.appId;
  dictionary[kPacoEventKeyPacoVersion] = self.pacoVersion;
  if (self.when) {
    dictionary[kPacoEventKeyWhen] = [PacoDateUtility pacoStringForDate:self.when];
  }
  if (self.latitude) {
    dictionary[kPacoEventKeyLatitude] = [NSString stringWithFormat:@"%lld", self.latitude];
  }
  if (self.longitude) {
    dictionary[kPacoEventKeyLongitude] = [NSString stringWithFormat:@"%lld", self.longitude];
  }
  if (self.responseTime) {
    dictionary[kPacoEventKeyResponseTime] = [PacoDateUtility pacoStringForDate:self.responseTime];
  }
  if (self.scheduledTime) {
    dictionary[kPacoEventKeyScheduledTime] = [PacoDateUtility pacoStringForDate:self.scheduledTime];
  }
  if (self.responses) {
    dictionary[kPacoEventKeyResponses] = self.responses;
  }
  return [NSDictionary dictionaryWithDictionary:dictionary];
}


- (id)payloadJsonWithImageString {
  if (0 == [self.responses count]) {
    return [self generateJsonObject];
  }
  
  NSMutableArray* newReponseList = [NSMutableArray arrayWithArray:self.responses];
  for (int index=0; index<[self.responses count]; index++) {
    id responseDict = (self.responses)[index];
    if (![responseDict isKindOfClass:[NSDictionary class]]) {
      continue;
    }
    id answer = ((NSDictionary*)responseDict)[kPacoResponseKeyAnswer];
    if (![answer isKindOfClass:[NSString class]]) {
      continue;
    }
    NSString* imageName = [UIImage pacoImageNameFromBoxedName:(NSString*)answer];
    if (!imageName) {
      continue;
    }
    NSString* imageString = [UIImage pacoBase64StringWithImageName:imageName];
    if ([imageString length] > 0) {
      NSMutableDictionary* newResponseDict = [NSMutableDictionary dictionaryWithDictionary:responseDict];
      newResponseDict[kPacoResponseKeyAnswer] = imageString;
      newReponseList[index] = newResponseDict;
    }
  }
  NSMutableDictionary* jsonPayload =
      [NSMutableDictionary dictionaryWithDictionary:[self generateJsonObject]];
  jsonPayload[kPacoEventKeyResponses] = newReponseList;
  return jsonPayload;
}



+ (PacoEvent*)joinEventForDefinition:(PacoExperimentDefinition*)definition
                        withSchedule:(PacoExperimentSchedule*)schedule {
  // Setup an event for joining the experiement.
  PacoEvent *event = [PacoEvent pacoEventForIOS];
  event.who = [[PacoClient sharedInstance] userEmail];
  event.experimentId = definition.experimentId;
  event.experimentVersion = definition.experimentVersion;
  event.experimentName = definition.title;
  
  event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];
  
  //Special response values to indicate the user is joining this experiement.
  //For now, we need to indicate inputId=-1 to avoid server exception,
  //in the future, server needs to fix and accept JOIN and STOP events without inputId
  NSDictionary* joinResponse = @{kPacoResponseKeyName:kPacoResponseJoin,
                                 kPacoResponseKeyAnswer:@"true",
                                 kPacoResponseKeyInputId:@"-1"};
  NSMutableArray* responseList = [NSMutableArray arrayWithObject:joinResponse];
  
  // Adding a schedule to the join event.
  if (schedule && [schedule isScheduled]){
    NSDictionary* scheduleResponse = @{kPacoResponseKeyName:@"schedule",
                                       kPacoResponseKeyAnswer:[schedule jsonString],
                                       kPacoResponseKeyInputId:@"-1"};
    [responseList addObject:scheduleResponse];
  }
  event.responses = responseList;
  return event;
}

+ (PacoEvent*)stopEventForExperiment:(PacoExperiment*)experiment 
{
  //create an event for stopping the experiement.
  PacoEvent *event = [PacoEvent pacoEventForIOS];
  event.who = [[PacoClient sharedInstance] userEmail];
  event.experimentId = experiment.definition.experimentId;
  event.experimentName = experiment.definition.title;
  event.experimentVersion = experiment.definition.experimentVersion;
  event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];
  
  //For now, we need to indicate inputId=-1 to avoid server exception,
  //in the future, server needs to fix and accept JOIN and STOP events without inputId
  NSDictionary *responsePair = @{kPacoResponseKeyName:kPacoResponseJoin,
                                 kPacoResponseKeyAnswer:@"false",
                                 kPacoResponseKeyInputId:@"-1"};
  event.responses = @[responsePair];
  
  return event;
}

+ (PacoEvent*)genericEventForDefinition:(PacoExperimentDefinition*)definition
                             withInputs:(NSArray*)inputs {
  PacoEvent *event = [PacoEvent pacoEventForIOS];
  event.who = [[PacoClient sharedInstance] userEmail];
  event.experimentId = definition.experimentId;
  event.experimentName = definition.title;
  event.experimentVersion = definition.experimentVersion;
  
  NSMutableArray *responses = [NSMutableArray array];
  for (PacoExperimentInput *input in inputs) {
    NSMutableDictionary *response = [NSMutableDictionary dictionary];
    id payloadObject = [input payloadObject];
    if (payloadObject == nil) {
      continue;
    }
    NSLog(@"INPUT RESPONSE NAME = %@", input.name);
    response[@"name"] = input.name;
    response[@"inputId"] = input.inputIdentifier;
    
    if (![payloadObject isKindOfClass:[UIImage class]]) {
      response[@"answer"] = payloadObject;
    } else {
      NSString* imageName = [UIImage pacoSaveImageToDocumentDir:payloadObject
                                                  forDefinition:definition.experimentId
                                                        inputId:input.inputIdentifier];
      if ([imageName length] > 0) {
        NSString* fullName = [UIImage pacoBoxedNameFromImageName:imageName];
        response[@"answer"] = fullName;
      } else {
        response[@"answer"] = @"Failed to save image";
      }
    }
    
    [responses addObject:response];
  }
  
  event.responses = responses;
  return event;
}

+ (PacoEvent*)selfReportEventForDefinition:(PacoExperimentDefinition*)definition
                                withInputs:(NSArray*)inputs {
  NSAssert(inputs != nil, @"inputs should not be nil!");
  PacoEvent* event = [PacoEvent genericEventForDefinition:definition withInputs:inputs];
  event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];
  event.scheduledTime = nil;
  return event;
}


+ (PacoEvent*)surveySubmittedEventForDefinition:(PacoExperimentDefinition*)definition
                                     withInputs:(NSArray*)inputs
                               andScheduledTime:(NSDate*)scheduledTime {
  NSAssert(scheduledTime != nil, @"scheduledTime should not be nil!");
  PacoEvent* event = [PacoEvent genericEventForDefinition:definition withInputs:inputs];
  event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];
  event.scheduledTime = scheduledTime;
  return event;  
}


+ (PacoEvent*)surveyMissedEventForDefinition:(PacoExperimentDefinition*)definition
                           withScheduledTime:(NSDate*)scheduledTime {
  NSAssert(scheduledTime != nil, @"scheduledTime should be valid!");
  PacoEvent* event = [self surveyMissedEventForDefinition:definition
                                        withScheduledTime:scheduledTime
                                                userEmail:[[PacoClient sharedInstance] userEmail]];
  return event;
}


+ (PacoEvent*)surveyMissedEventForDefinition:(PacoExperimentDefinition*)definition
                           withScheduledTime:(NSDate*)scheduledTime
                                   userEmail:(NSString*)userEmail{
  NSAssert(definition, @"definition should be valid");
  NSAssert(scheduledTime != nil, @"scheduledTime should be valid!");
  NSAssert([userEmail length] > 0, @"userEmail should be valid!");
  PacoEvent *event = [PacoEvent pacoEventForIOS];
  event.who = userEmail;
  event.experimentId = definition.experimentId;
  event.experimentName = definition.title;
  event.experimentVersion = definition.experimentVersion;
  event.responseTime = nil;
  event.scheduledTime = scheduledTime;
  return event;
}



@end

