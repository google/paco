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

#import "PacoTriggerSignal.h"

NSString* const kID = @"id";
NSString* const kSignalType= @"type";
NSString* const kEventCode = @"eventCode";
NSString* const kDelay = @"delay";

NSString* const kTriggerSignal = @"trigger";

@interface PacoTriggerSignal ()

@property (nonatomic, copy) NSString* identifier;
@property (nonatomic, copy) NSString* signalType;
@property (nonatomic, assign) int eventCode;
@property (nonatomic, assign) long long delay;

@end

@implementation PacoTriggerSignal

//designated initializer
- (id)initWithIdentifier:(NSString*)identifier
              signalType:(NSString*)signalType
               eventCode:(int)eventCode
                   delay:(long long)delay {
  self = [super init];
  if (self) {
    _identifier = identifier;
    _signalType = signalType;
    _eventCode = eventCode;
    _delay = delay;
  }
  return self;
}

+ (id)signalFromJson:(id)jsonObject {
  NSAssert([jsonObject isKindOfClass:[NSDictionary class]],
           @"it has to be a dictionary for trigger signal");
  
  NSString* identifier = [NSString stringWithFormat:@"%lld", [jsonObject[kID] longLongValue]];
  return [[PacoTriggerSignal alloc] initWithIdentifier:identifier
                                            signalType:jsonObject[kSignalType]
                                             eventCode:[jsonObject[kEventCode] intValue]
                                                 delay:[jsonObject[kDelay] longLongValue]];
}


- (id)copyWithZone:(NSZone *)zone {
  PacoTriggerSignal* copy = [[[self class] allocWithZone:zone] initWithIdentifier:self.identifier
                                                                       signalType:self.signalType
                                                                        eventCode:self.eventCode
                                                                            delay:self.delay];
  return copy;
}


- (id)serializeToJSON {
  NSMutableDictionary* dict = [NSMutableDictionary dictionary];
  dict[kID] = @([self.identifier longLongValue]);
  dict[kSignalType] = self.signalType;
  dict[kEventCode] = @(self.eventCode);
  dict[kDelay] = @(self.delay);
  return dict;
}

- (NSString*)description {
  return [NSString stringWithFormat:@"<%@, %p: id=%@, type=%@, eventCode=%d, delay=%lld>",
          NSStringFromClass([self class]),
          self,
          self.identifier,
          self.signalType,
          self.eventCode,
          self.delay];
}

@end
