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
#import "PacoExperimentSchedule.h"
#import "PacoExperimentDefinition.h"
#import "PacoModel.h"
#import "NSDate+Paco.h"


@implementation PacoExperiment

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperiment:%p - "
          @"schedule=%@\n"
          @"definition=%@>",
          self,
          self.schedule,
          self.definition];
}

- (id)serializeToJSON {
  id  jsonSchedule = [self.schedule serializeToJSON];
  id jsonDefinition = [self.definition serializeToJSON];
  return [NSDictionary dictionaryWithObjectsAndKeys:
          self.definition.experimentId, @"experimentId",
          self.instanceId, @"instanceId",
          jsonSchedule, @"schedule",
          jsonDefinition, @"definition",
          nil];
}

- (void)deserializeFromJSON:(id)json {
  NSAssert([json isKindOfClass:[NSDictionary class]], @"json should be a dictionary");
  self.jsonObject = json;

  self.instanceId = [self.jsonObject objectForKey:@"instanceId"];
  
  NSDictionary* jsonSchedule = [self.jsonObject objectForKey:@"schedule"];
  self.schedule = [PacoExperimentSchedule pacoExperimentScheduleFromJSON:jsonSchedule];
  NSAssert(self.schedule, @"schedule doesn't exist!");
  
  NSDictionary* jsonDefinition = [self.jsonObject objectForKey:@"definition"];
  self.definition = [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:jsonDefinition];
  NSAssert(self.definition, @"definition doesn't exist!");
}

- (BOOL)shouldScheduleNotifications {
  if ([self isSelfReportExperiment]) {
    return NO;
  }
  return [self.definition isExperimentValid];
}

- (BOOL)isSelfReportExperiment {
  return [self.schedule isSelfReport];
}

- (BOOL)isScheduledExperiment {
  return [self.schedule isScheduled];
}

- (BOOL)isExperimentValidSinceDate:(NSDate*)fromDate {
  return [self.definition isExperimentValidSinceDate:fromDate];
}

- (BOOL)isFixedLength {
  return [self.definition isFixedLength];
}

- (BOOL)isOngoing {
  return [self.definition isOngoing];
}

- (BOOL)hasESMScheduleList {
  return [self.schedule.esmScheduleList count] > 0;
}

static int INVALID_INDEX = -1;
- (NSArray*)ESMSchedulesFromDate:(NSDate*)fromDate {
  if (![self.schedule isESMSchedule] || fromDate == nil) {
    return nil;
  }
  int index = INVALID_INDEX;
  NSArray* dates = self.schedule.esmScheduleList;
  for (NSUInteger currentIndex = 0; currentIndex < [dates count]; currentIndex++) {
    NSDate* date = [dates objectAtIndex:currentIndex];
    if ([date pacoLaterThanDate:fromDate]) {
      index = currentIndex;
      break;
    }
  }
  NSArray* result = nil;
  if (index != INVALID_INDEX) {
    //since esmScheduleList is sorted already, just return the sub-array
    int count = [dates count] - index;
    result = [dates subarrayWithRange:NSMakeRange(index, count)];
  }
  return result;
}

- (NSUInteger)numOfESMSchedulesFromDate:(NSDate*)fromDate {
  NSArray* esmSchedules = [self ESMSchedulesFromDate:fromDate];
  return [esmSchedules count];
}

- (BOOL)hasESMSchedulesWithMinimumCount:(NSUInteger)numOfESMSchedules fromDate:(NSDate*)fromDate {
  return [self numOfESMSchedulesFromDate:fromDate] >= numOfESMSchedules;
}

- (BOOL)hasESMSchedulesWithMaximumCount:(NSUInteger)numOfESMSchedules fromDate:(NSDate*)fromDate {
  return [self numOfESMSchedulesFromDate:fromDate] <= numOfESMSchedules;
}

- (NSDate*)startDate {
  return self.definition.startDate;
}

- (NSDate*)endDate {
  return self.definition.endDate;
}


- (BOOL)refreshWithSchedule:(PacoExperimentSchedule*)newSchedule {
  NSAssert(newSchedule, @"newSchedule should be valid");
  
  self.schedule.userEditable = newSchedule.userEditable;
  if ([self.schedule isEqualToSchedule:newSchedule]) {
    return NO;
  }
  long long startHourConfigured = self.schedule.esmStartHour;
  long long endHourConfigured = self.schedule.esmEndHour;
  NSArray* timesConfigured = self.schedule.times;
  PacoExperimentSchedule* oldSchedule = self.schedule;
  self.schedule = newSchedule;
  
  //esm
  if ([oldSchedule isESMSchedule] && [newSchedule isESMSchedule]) {
    self.schedule.esmStartHour = startHourConfigured;
    self.schedule.esmEndHour = endHourConfigured;
  }
   //daily, weekdays, weekly, monthly
  if (![oldSchedule isESMSchedule] && ![newSchedule isESMSchedule] &&
      [timesConfigured count] == [newSchedule.times count]) {
    self.schedule.times = timesConfigured;
  }
  return YES;
}

@end
