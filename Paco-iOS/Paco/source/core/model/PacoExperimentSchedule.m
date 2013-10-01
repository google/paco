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


- (id)serializeToJSON
{
  NSMutableDictionary* scheduleJson =
      [NSMutableDictionary dictionaryWithObjectsAndKeys:
          [NSNumber numberWithBool:self.byDayOfMonth], @"byDayOfMonth",
          [NSNumber numberWithBool:self.byDayOfWeek], @"byDayOfWeek",
          [NSNumber numberWithInt:self.dayOfMonth], @"dayOfMonth",
          [NSNumber numberWithLongLong:self.esmEndHour], @"esmEndHour",
          [NSNumber numberWithInt:self.esmFrequency], @"esmFrequency",
          [NSNumber numberWithLongLong:self.esmPeriodInDays], @"esmPeriodInDays",
          [NSNumber numberWithLongLong:self.esmStartHour], @"esmStartHour",
          [NSNumber numberWithBool:self.esmWeekends], @"esmWeekends",
          [NSNumber numberWithLongLong:[self.scheduleId longLongValue]], @"id",
          [NSNumber numberWithInt:self.nthOfMonth], @"nthOfMonth",
          [NSNumber numberWithInt:self.repeatPeriod], @"repeatRate",
          
          [NSNumber numberWithInt:self.timeout], @"timeout",
          [NSNumber numberWithInt:self.minimumBuffer], @"minimumBuffer",
          [NSNumber numberWithInt:self.scheduleType], @"scheduleType",
          self.times, @"times",
          [NSNumber numberWithBool:self.userEditable], @"userEditable",
          [NSNumber numberWithInt:self.weekDaysScheduled], @"weekDaysScheduled",
          nil];
  
  if ([self.esmScheduleList count] > 0) {
    NSMutableArray* dateStringArr = [NSMutableArray arrayWithCapacity:[self.esmScheduleList count]];
    for (NSDate* date in self.esmScheduleList) {
      [dateStringArr addObject:[PacoDate pacoStringForDate:date]];
    }
    [scheduleJson setObject:dateStringArr forKey:@"esmScheduleList"];
  }
  return scheduleJson;
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
  schedule.scheduleId = [NSString stringWithFormat:@"%lld", [[scheduleMembers objectForKey:@"id"] longLongValue]];
  schedule.nthOfMonth = [[scheduleMembers objectForKey:@"nthOfMonth"] intValue];
  schedule.repeatPeriod = (PacoScheduleRepeatPeriod)[[scheduleMembers objectForKey:@"repeatRate"] intValue];
  schedule.scheduleType = [[scheduleMembers objectForKey:@"scheduleType"] intValue];
  schedule.timeout = [[scheduleMembers objectForKey:@"timeout"] intValue];
  
  NSNumber* minimumBufferNum = [scheduleMembers objectForKey:@"minimumBuffer"];
  if (minimumBufferNum != nil) {
    schedule.minimumBuffer = [minimumBufferNum intValue];
  } else {
    schedule.minimumBuffer = 59; //default
  }
  
  schedule.times = [scheduleMembers objectForKey:@"times"];
  schedule.times = [schedule.times sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
    NSDate *lhs = obj1;
    NSDate *rhs = obj2;
    return [lhs compare:rhs];
  }];
  
  NSNumber* userEditableObject = [scheduleMembers objectForKey:@"userEditable"];
  if (userEditableObject) {
    schedule.userEditable = [userEditableObject boolValue];
  } else {
    schedule.userEditable = YES; //userEditable is YES by default
  }
  schedule.weekDaysScheduled = [[scheduleMembers objectForKey:@"weekDaysScheduled"] intValue];
  
  // !!! TPE temporary timeout fix for issue #9
  if (schedule.timeout == 0) {
    if (schedule.scheduleType == kPacoScheduleTypeESM) {
      schedule.timeout = 59;
    } else {
      schedule.timeout = 479;
    }
  }
  NSArray* esmScheduleStringArr = [scheduleMembers objectForKey:@"esmScheduleList"];
  if ([esmScheduleStringArr count] > 0) {
    NSMutableArray* dateArr = [NSMutableArray arrayWithCapacity:[esmScheduleStringArr count]];
    for (NSString* dateStr in esmScheduleStringArr) {
      [dateArr addObject:[PacoDate pacoDateForString:dateStr]];
    }
    schedule.esmScheduleList = dateArr;
  }
  
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
          @"nthOfMonth=%d "
          @"repeatPeriod=%d "
          @"scheduleType=%d "
          @"times=%@ "
          @"timeout=%d "
          @"minimumBuffer=%d "
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
          self.nthOfMonth,
          self.repeatPeriod,
          self.scheduleType,
          self.times,
          self.timeout,
          self.minimumBuffer,
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
    case kPacoScheduleTypeTesting:
      return @"iOS Testing";
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

//YMZ:TODO: examine if we need to add timeout and minimumBuffer
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
  [json appendFormat:@"nthOfMonth = %d;", self.nthOfMonth];
  [json appendFormat:@"byDayOfMonth = %d;", self.byDayOfMonth];
  [json appendFormat:@"dayOfMonth = %d", self.dayOfMonth];
  [json appendString:@"}"];
  return json;
  
  return nil;
}

@end
