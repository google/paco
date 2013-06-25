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

#import "PacoExperimentSchedule.h"
#import "PacoDate.h"


@implementation PacoExperimentSchedule

@synthesize byDayOfMonth;
@synthesize byDayOfWeek;
@synthesize dayOfMonth;
@synthesize esmEndHour;
@synthesize esmFrequency;
@synthesize esmPeriodInDays;
@synthesize esmPeriod;
@synthesize esmStartHour;
@synthesize esmWeekends;
@synthesize scheduleId;
@synthesize nthAMonth;
@synthesize repeatPeriod;
@synthesize scheduleType;
@synthesize times;  // NSNumber<long long>
@synthesize userEditable;
@synthesize weekDaysScheduled;
@synthesize jsonObject;
@synthesize esmSchedule;

- (id)serializeToJSON
{
  return [NSMutableDictionary dictionaryWithObjectsAndKeys:
          [NSNumber numberWithBool:self.byDayOfMonth], @"byDayOfMonth",
          [NSNumber numberWithBool:self.byDayOfWeek], @"byDayOfWeek",
          [NSNumber numberWithInt:self.dayOfMonth], @"dayOfMonth",
          [NSNumber numberWithLongLong:self.esmEndHour], @"esmEndHour",
          [NSNumber numberWithInt:self.esmFrequency], @"esmFrequency",
          [NSNumber numberWithLongLong:self.esmPeriodInDays], @"esmPeriodInDays",
          [NSNumber numberWithLongLong:self.esmStartHour], @"esmStartHour",
          [NSNumber numberWithBool:self.esmWeekends], @"esmWeekends",
          self.scheduleId, @"id",
          [NSNumber numberWithInt:self.nthAMonth], @"nthAMonth",
          [NSNumber numberWithInt:self.repeatPeriod], @"repeatRate",
          [NSNumber numberWithInt:self.scheduleType], @"scheduleType",
          self.times, @"times",
          [NSNumber numberWithBool:self.userEditable], @"userEditable",
          [NSNumber numberWithInt:self.weekDaysScheduled], @"weekDaysScheduled",
          nil];
}

+ (id)pacoExperimentScheduleFromJSON:(id)jsonObject {
  PacoExperimentSchedule *schedule = [[PacoExperimentSchedule alloc] init];
  NSDictionary *scheduleMembers = jsonObject;
  schedule.byDayOfMonth = [[scheduleMembers objectForKey:@"byDayOfMonth"] boolValue];
  schedule.byDayOfWeek = [[scheduleMembers objectForKey:@"byDayOfWeek"] boolValue];
  schedule.dayOfMonth = [[scheduleMembers objectForKey:@"dayOfMonth"] intValue];
  schedule.esmEndHour = [[scheduleMembers objectForKey:@"esmEndHour"] longLongValue];
  schedule.esmFrequency = [[scheduleMembers objectForKey:@"esmFrequency"] intValue];
  schedule.esmPeriodInDays = [[scheduleMembers objectForKey:@"esmPeriodInDays"] longLongValue];
  if (schedule.esmPeriodInDays == 1) {
    schedule.esmPeriod = kPacoScheduleRepeatPeriodDay;
  } else if (schedule.esmPeriodInDays == 7) {
    schedule.esmPeriod = kPacoScheduleRepeatPeriodWeek;
  } else if (schedule.esmPeriodInDays == 30) {
    schedule.esmPeriod = kPacoScheduleRepeatPeriodMonth;
  }
  schedule.esmStartHour = [[scheduleMembers objectForKey:@"esmStartHour"] longLongValue];
  schedule.esmWeekends = [[scheduleMembers objectForKey:@"esmWeekends" ] boolValue];
  schedule.scheduleId = [NSString stringWithFormat:@"%ld", [[scheduleMembers objectForKey:@"id"] longValue]];
  schedule.nthAMonth = [[scheduleMembers objectForKey:@"nthAMonth"] intValue];
  schedule.repeatPeriod = (PacoScheduleRepeatPeriod)[[scheduleMembers objectForKey:@"repeatRate"] intValue];
  schedule.scheduleType = [[scheduleMembers objectForKey:@"scheduleType"] intValue];
  schedule.times = [scheduleMembers objectForKey:@"times"];
  schedule.times = [schedule.times sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
    NSDate *lhs = obj1;
    NSDate *rhs = obj2;
    return [lhs compare:rhs];
  }];
  schedule.userEditable = [[scheduleMembers objectForKey:@"userEditable"] boolValue];
  schedule.weekDaysScheduled = [[scheduleMembers objectForKey:@"weekDaysScheduled"] intValue];
  schedule.jsonObject = jsonObject;
  return schedule;
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentSchedule:%p - "
          @"byDayOfMonth=%d "
          @"byDayOfWeek=%d "
          @"dayOfMonth=%d "
          @"esmEndHour=%lld "
          @"esmFrequency=%d "
          @"esmPeriodInDays=%lld "
          @"esmPeriod=%d"
          @"esmStartHour=%lld "
          @"esmWeekends=%d "
          @"scheduleId=%@ "
          @"nthAMonth=%d "
          @"repeatPeriod=%d "
          @"scheduleType=%d "
          @"times=%@ "
          @"weekDaysScheduled=%d >",
          self,
          self.byDayOfMonth,
          self.byDayOfWeek,
          self.dayOfMonth,
          self.esmEndHour,
          self.esmFrequency,
          self.esmPeriodInDays,
          self.esmPeriod,
          self.esmStartHour,
          self.esmWeekends,
          self.scheduleId,
          self.nthAMonth,
          self.repeatPeriod,
          self.scheduleType,
          self.times,
          self.weekDaysScheduled,
          nil];
}

+ (NSString *)stringFromType:(PacoScheduleType)type {
  switch(type) {
    case kPacoScheduleTypeDaily:
      return @"Daily";
    case kPacoScheduleTypeWeekday:
      return @"Weekdays";
    case kPacoScheduleTypeWeekly:
      return @"Weekly";
    case kPacoScheduleTypeMonthly:
      return @"Monthly";
    case kPacoScheduleTypeESM:
      return @"Random sampling (ESM)";
    case kPacoScheduleTypeSelfReport:
      return @"Self report only";
    case kPacoScheduleTypeAdvanced:
      return @"Advanced";
  }
  return nil;
}

- (NSString *)typeString {
  return [[self class] stringFromType:self.scheduleType];
}

+ (NSString *)stringFromPeriod:(PacoScheduleRepeatPeriod)period {
  // convert num days into the string for that period enum.
  switch(period) {
    case kPacoScheduleRepeatPeriodDay: return @"Day";
    case kPacoScheduleRepeatPeriodWeek: return @"Week";
    case kPacoScheduleRepeatPeriodMonth: return @"Month";
  }
  return nil;
}

- (NSString *)periodString {
  return [[self class] stringFromPeriod:self.esmPeriod];
}

+ (NSString *)hourAsMillisec:(NSInteger)hourOn24Clock {
  long long millisecondsInHour = 3600000;
  return [NSString stringWithFormat:@"%lld", millisecondsInHour * hourOn24Clock];
}

- (NSString *)weekDaysScheduledString {
  NSString *dayNames[] = { @"Sun", @"Mon", @"Tue", @"Wed", @"Thu", @"Fri", @"Sat" };
  NSMutableString *string = [NSMutableString string];
  for (int i = 0; i < 7; ++i) {
    if (self.weekDaysScheduled & (1 << i)) {
      if ([string length] == 0) {
        [string appendString:dayNames[i]];
      } else {
        [string appendFormat:@", %@", dayNames[i]];
      }
    }
  }
  return string;
}

- (NSString *)jsonString {
  NSMutableString *json = [NSMutableString stringWithString:@"{"];
  
  [json appendFormat:@"type = %@;", [self typeString]];
  
  if (self.scheduleType == kPacoScheduleTypeESM) {
    [json appendFormat:@"frequency = %d", self.esmFrequency];
    [json appendFormat:@"esmPeriod = %@", [self periodString]];
    [json appendFormat:@"startHour = %lld", self.esmStartHour];
    [json appendFormat:@"endHour = %lld", self.esmEndHour];
    [json appendFormat:@"weekends = %d", self.esmWeekends];
  }
  
  [json appendString:@"times = ["];
  for (NSNumber *time in self.times) {
    if ([self.times objectAtIndex:0] == time) {
      [json appendFormat:@"%lld", [time longLongValue]];
    } else {
      [json appendFormat:@", %lld", [time longLongValue]];
    }
  }
  [json appendString:@"];"];
  
  [json appendFormat:@"repeatPeriod = %d;", self.repeatPeriod];
  [json appendFormat:@"daysOfWeek = %@;", [self weekDaysScheduledString]];
  [json appendFormat:@"nthOfMonth = %d;", self.nthAMonth];
  [json appendFormat:@"byDayOfMonth = %d;", self.byDayOfMonth];
  [json appendFormat:@"dayOfMonth = %d", self.dayOfMonth];
  [json appendString:@"}"];
  return json;
  
  return nil;
}

@end
