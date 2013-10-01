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
#import "PacoExperiment.h"
#import "PacoEvent.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperimentDefinition.h"
#import "PacoModel.h"

@interface PacoExperimentSchedule ()
- (id)serializeToJSON;
@end


@implementation PacoExperiment

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperiment:%p - "
          @"definitions=%@ "
          @"events=%@ "
          @"lastEventsQueryTime=%lld>",
          self,
          self.definition,
          self.events,
          self.lastEventQueryTime];
}

- (id)serializeToJSON {
  id  jsonSchedule = [self.schedule serializeToJSON];
  NSMutableArray *pacoEvents = [NSMutableArray array];
  for (PacoEvent *event in self.events) {
    if (!event.jsonObject) {
      event.jsonObject = [event generateJsonObject];
    }
    assert(event.jsonObject);
    [pacoEvents addObject:event.jsonObject];
  }
  return [NSDictionary dictionaryWithObjectsAndKeys:
          self.definition.experimentId, @"experimentId",
          self.instanceId, @"instanceId",
          [NSNumber numberWithLongLong:self.lastEventQueryTime], @"lastEventQueryTime",
          pacoEvents, @"events",
          jsonSchedule, @"schedule",
          nil];
}

- (void)deserializeFromJSON:(id)json model:(PacoModel *)model {
  assert([json isKindOfClass:[NSDictionary class]]);
  NSDictionary *map = json;
  self.instanceId = [map objectForKey:@"instanceId"];
  
  NSNumber *timestamp = [map objectForKey:@"lastEventQueryTime"];
  self.lastEventQueryTime = [timestamp longLongValue];
  
  self.schedule = [PacoExperimentSchedule pacoExperimentScheduleFromJSON:[map objectForKey:@"schedule"]];
  
  NSMutableArray *pacoEvents = [NSMutableArray array];
  NSArray *eventJSONs = [map objectForKey:@"events"];
  for (id eventJSON in eventJSONs) {
    PacoEvent *event = [PacoEvent pacoEventFromJSON:eventJSON];
    assert(event);
    [pacoEvents addObject:event];
  }
  self.events = pacoEvents;
  self.jsonObject = json;
  
  NSString *experimentId = [map objectForKey:@"experimentId"];
  PacoExperimentDefinition *experimentDefinition = [model experimentDefinitionForId:experimentId];
  NSAssert(experimentDefinition != nil, @"definition doesn't exist!");
  self.definition = experimentDefinition;
}

- (BOOL)shouldScheduleNotifications {
  return (self.schedule.scheduleType != kPacoScheduleTypeSelfReport) &&
         (self.schedule.scheduleType != kPacoScheduleTypeAdvanced);
}

- (BOOL)haveJoined {
  // TODO(gregvance): maybe should check for the "joined"="true" in the event
  //     responses, but what about un-joining ?
  return [self.events count];
}

@end
