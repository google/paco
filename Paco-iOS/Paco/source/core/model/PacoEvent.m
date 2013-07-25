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
#import "PacoDate.h"
#import "PacoClient.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperimentSchedule.h"

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
static NSString* const kPacoEventKeyResponses = @"responses";

static NSString* const kPacoResponseKeyName = @"name";
static NSString* const kPacoResponseKeyAnswer = @"answer";
static NSString* const kPacoResponseKeyInputId = @"inputId";

@interface PacoEvent ()
@property (nonatomic, readwrite, copy) NSString *appId;
@property (nonatomic, readwrite, copy) NSString *pacoVersion;
@end

@implementation PacoEvent

- (id)init {
  self = [super init];
  if (self) {
    _appId = @"ios_paco";
    _pacoVersion = @"1";
  }
  return self;
}

+ (id)pacoEventForIOS {
  return [[PacoEvent alloc] init];
}


+ (id)pacoEventFromJSON:(id)jsonObject {
  PacoEvent *event = [[PacoEvent alloc] init];
  NSDictionary *eventMembers = jsonObject;
  event.who = [eventMembers objectForKey:kPacoEventKeyWho];
  event.when = [PacoDate pacoDateForString:[eventMembers objectForKey:kPacoEventKeyWhen]];
  event.latitude = [[eventMembers objectForKey:kPacoEventKeyLatitude] longLongValue];
  event.longitude = [[eventMembers objectForKey:kPacoEventKeyLongitude] longLongValue];
  event.responseTime = [PacoDate pacoDateForString:[eventMembers objectForKey:kPacoEventKeyResponseTime]];
  event.scheduledTime = [PacoDate pacoDateForString:[eventMembers objectForKey:kPacoEventKeyScheduledTime]];
  event.appId = [eventMembers objectForKey:kPacoEventKeyAppId];
  event.pacoVersion = [eventMembers objectForKey:kPacoEventKeyPacoVersion];
  event.experimentId = [eventMembers objectForKey:kPacoEventKeyExperimentId];
  event.experimentName = [eventMembers objectForKey:kPacoEventKeyExperimentName];
  event.responses = [eventMembers objectForKey:kPacoEventKeyResponses];
  return event;
}

- (id)generateJsonObject {
  NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
  [dictionary setValue:self.experimentId forKey:kPacoEventKeyExperimentId];
  [dictionary setValue:self.experimentName forKey:kPacoEventKeyExperimentName];
  [dictionary setValue:self.who forKey:kPacoEventKeyWho];
  [dictionary setValue:self.appId forKey:kPacoEventKeyAppId];
  [dictionary setValue:self.pacoVersion forKey:kPacoEventKeyPacoVersion];
  if (self.when) {
    [dictionary setValue:[PacoDate pacoStringForDate:self.when] forKey:kPacoEventKeyWhen];
  }
  if (self.latitude) {
    [dictionary setValue:[NSString stringWithFormat:@"%lld", self.latitude] forKey:kPacoEventKeyLatitude];
  }
  if (self.longitude) {
    [dictionary setValue:[NSString stringWithFormat:@"%lld", self.longitude] forKey:kPacoEventKeyLongitude];
  }
  if (self.responseTime) {
    [dictionary setValue:[PacoDate pacoStringForDate:self.responseTime] forKey:kPacoEventKeyResponseTime];
  }
  if (self.scheduledTime) {
    [dictionary setValue:[PacoDate pacoStringForDate:self.scheduledTime] forKey:kPacoEventKeyScheduledTime];
  }
  if (self.responses) {
    [dictionary setValue:self.responses forKey:kPacoEventKeyResponses];
  }
  return [NSDictionary dictionaryWithDictionary:dictionary];
}

+ (PacoEvent*)joinEventForDefinition:(PacoExperimentDefinition*)definition
                        withSchedule:(PacoExperimentSchedule*)schedule {
  // Setup an event for joining the experiement.
  PacoEvent *event = [PacoEvent pacoEventForIOS];
  event.who = [PacoClient sharedInstance].userEmail;
  event.experimentId = definition.experimentId;
  
  event.experimentName = definition.title;
  
  event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];
  NSMutableDictionary *response = [NSMutableDictionary dictionary];
  NSArray *responses = [NSArray arrayWithObject:response];
  
  // Special response values to indicate the user is joining this experiement.
  [response setObject:@"joined" forKey:kPacoResponseKeyName];
  [response setObject:@"true" forKey:kPacoResponseKeyAnswer];
  
  // Adding a schedule to the join event.  The join event is the only way to
  // edit a schedule.
  if (schedule &&
      definition.schedule.scheduleType != kPacoScheduleTypeSelfReport &&
      definition.schedule.scheduleType != kPacoScheduleTypeAdvanced) {
    [response setObject:@"schedule" forKey:kPacoResponseKeyName];
    [response setObject:[schedule jsonString] forKey:kPacoResponseKeyAnswer];
  }
  
  //For now, we need to indicate inputId=-1 to avoid server exception,
  //in the future, server needs to fix and accept JOIN and STOP events without inputId
  [response setObject:@"-1" forKey:kPacoResponseKeyInputId];
  
  event.responses = responses;
  return event;
}

+ (PacoEvent*)stopEventForExperiment:(PacoExperiment*)experiment 
{
  //create an event for stopping the experiement.
  PacoEvent *event = [PacoEvent pacoEventForIOS];
  event.who = [PacoClient sharedInstance].userEmail;
  event.experimentId = experiment.definition.experimentId;
  event.experimentName = experiment.definition.title;
  event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];
  
  //For now, we need to indicate inputId=-1 to avoid server exception,
  //in the future, server needs to fix and accept JOIN and STOP events without inputId
  NSDictionary *responsePair = @{kPacoResponseKeyName:@"joined",
                                 kPacoResponseKeyAnswer:@"false",
                                 kPacoResponseKeyInputId:@"-1"};
  event.responses = @[responsePair];
  
  return event;
}

@end

