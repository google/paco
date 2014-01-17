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
#import "PacoDateUtility.h"
#import "NSDate+Paco.h"
#import "NSMutableArray+Paco.h"

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
          [NSNumber numberWithInteger:self.repeatRate], @"repeatRate",
          
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
      [dateStringArr addObject:[PacoDateUtility pacoStringForDate:date]];
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
  NSAssert(schedule.esmPeriodInDays == 0 ||
           schedule.esmPeriodInDays == 1 ||
           schedule.esmPeriodInDays == 2 , @"esmPeriodInDays should only be 0, 1 or 2!");
  schedule.esmPeriod = (PacoScheduleRepeatPeriod)schedule.esmPeriodInDays;
  schedule.esmStartHour = [[scheduleMembers objectForKey:@"esmStartHour"] longLongValue];
  schedule.esmWeekends = [[scheduleMembers objectForKey:@"esmWeekends" ] boolValue];
  schedule.scheduleId = [NSString stringWithFormat:@"%lld", [[scheduleMembers objectForKey:@"id"] longLongValue]];
  schedule.nthOfMonth = [[scheduleMembers objectForKey:@"nthOfMonth"] intValue];
  schedule.repeatRate = [[scheduleMembers objectForKey:@"repeatRate"] integerValue];
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
      [dateArr addObject:[PacoDateUtility pacoDateForString:dateStr]];
    }
    schedule.esmScheduleList = dateArr;
  }
  
  schedule.jsonObject = jsonObject;
  return schedule;
}


- (id)copyWithZone:(NSZone *)zone {
  PacoExperimentSchedule* another = [[self class] pacoExperimentScheduleFromJSON:self.jsonObject];
  return another;
}


- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentSchedule:%p - "
          @"byDayOfMonth=%d "
          @"byDayOfWeek=%d "
          @"dayOfMonth=%d "
          @"esmStartHour=%@ "
          @"esmEndHour=%@ "
          @"esmFrequency=%d "
          @"esmPeriodInDays=%lld "
          @"esmPeriod=%@ "
          @"esmWeekends=%d "
          @"scheduleId=%@ "
          @"nthOfMonth=%d "
          @"repeatRate=%d "
          @"scheduleType=%d "
          @"times=%@ "
          @"timeout=%d "
          @"minimumBuffer=%d "
          @"weekDaysScheduled=%@ >",
          self,
          self.byDayOfMonth,
          self.byDayOfWeek,
          self.dayOfMonth,
          [PacoDateUtility timeStringFromMilliseconds:self.esmStartHour],
          [PacoDateUtility timeStringFromMilliseconds:self.esmEndHour],
          self.esmFrequency,
          self.esmPeriodInDays,
          [self periodString],
          self.esmWeekends,
          self.scheduleId,
          self.nthOfMonth,
          self.repeatRate,
          self.scheduleType,
          [self.times pacoDescriptionForTimeNumbers],
          self.timeout,
          self.minimumBuffer,
          [self weekDaysScheduledString],
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
  if (0 == self.weekDaysScheduled) {
    return @"None";
  }
  NSString *dayNames[] = { @"Sun", @"Mon", @"Tue", @"Wed", @"Thu", @"Fri", @"Sat" };
  NSMutableString *string = [NSMutableString string];
  for (int i = 0; i < 7; ++i) {
    if (self.weekDaysScheduled & (1 << i)) {
      if ([string length] == 0) {
        [string appendFormat:@"(%@", dayNames[i]];
      } else {
        [string appendFormat:@", %@", dayNames[i]];
      }
    }
  }
  [string appendString:@")"];
  return string;
}

- (NSString *)jsonString {
  NSMutableString *json = [NSMutableString stringWithString:@"{"];
  
  [json appendFormat:@"type = %@,", [self typeString]];
  
  if (self.scheduleType == kPacoScheduleTypeESM) {
    [json appendFormat:@"frequency = %d,", self.esmFrequency];
    [json appendFormat:@"esmPeriod = %@,", [self periodString]];
    [json appendFormat:@"startHour = %@,", [PacoDateUtility timeStringFromMilliseconds:self.esmStartHour]];
    [json appendFormat:@"endHour = %@,", [PacoDateUtility timeStringFromMilliseconds:self.esmEndHour]];
    [json appendFormat:@"weekends = %@,", self.esmWeekends ? @"true" : @"false"];
  }
  [json appendFormat:@"times = %@,", [self.times pacoDescriptionForTimeNumbers]];
  [json appendFormat:@"repeatRate = %d,", self.repeatRate];
  [json appendFormat:@"daysOfWeek = %@,", [self weekDaysScheduledString]];
  [json appendFormat:@"nthOfMonth = %d,", self.nthOfMonth];
  [json appendFormat:@"byDayOfMonth = %@,", self.byDayOfMonth ? @"true" : @"false"];
  [json appendFormat:@"dayOfMonth = %d", self.dayOfMonth];
  [json appendString:@"}"];
  return json;
}

- (BOOL)isESMSchedule {
  return self.scheduleType == kPacoScheduleTypeESM;
}

- (BOOL)isSelfReport {
  return kPacoScheduleTypeSelfReport == self.scheduleType;
}

- (BOOL)isScheduled {
  return ![self isSelfReport];
}

- (NSInteger)minutesPerDayOfESM {
  if (![self isESMSchedule]) {
    return 0;
  }
  long long millisecondsPerDay = self.esmEndHour - self.esmStartHour;
  long secondsPerDay = millisecondsPerDay / 1000.0;
  NSInteger minutesPerDay = secondsPerDay / 60.0;
  return minutesPerDay;
}

- (NSString*)esmStartTimeString {
  if (![self isESMSchedule]) {
    return nil;
  }
  return [NSString stringWithFormat:@"Start Time: %@", [PacoDateUtility timeStringFromMilliseconds:self.esmStartHour]];
}

- (NSString*)esmEndTimeString {
  if (![self isESMSchedule]) {
    return nil;
  }
  return [NSString stringWithFormat:@"End  Time: %@", [PacoDateUtility timeStringFromMilliseconds:self.esmEndHour]];
}

- (NSDate*)esmStartTimeOnDate:(NSDate*)date {
  if (![self isESMSchedule] || date == nil) {
    return nil;
  }
  NSDate* midnight = [date pacoCurrentDayAtMidnight];
  double intervalFromMidnight = self.esmStartHour / 1000.0;
  NSDate* startTime = [midnight dateByAddingTimeInterval:intervalFromMidnight];
  NSAssert(startTime, @"startTime should be valid!");
  return startTime;
}

- (NSString*)evaluateSchedule {
  if (self.scheduleType == kPacoScheduleTypeDaily) {
    self.times = [self.times pacoSortedNumbers];
    if (![self.times pacoIsNonDuplicate]) {
      return @"There shouldn't be duplicate signal times!";
    }
  } else if (self.scheduleType == kPacoScheduleTypeESM) {
    if ([self minutesPerDayOfESM] <= 0) {
      return @"Start Time must be earlier than End Time!";
    }
  }
  return nil;
}


//Note: userEditable is ignored here
//ESM startHour and endHour are ignored here
//times are ignored if the number of times are the same
- (BOOL)isEqualToSchedule:(PacoExperimentSchedule*)another {
  if (!another) {
    return NO;
  }
  if (self.scheduleType != another.scheduleType) {
    return NO;
  }
  if (self.scheduleType == kPacoScheduleTypeSelfReport) {
    return YES;
  }
  //common field owned by all schedule types
  if (self.timeout != another.timeout) {
    return NO;
  }
  
  switch (self.scheduleType) {
    case kPacoScheduleTypeDaily:
      return (self.repeatRate == another.repeatRate && [self.times count] == [another.times count]);

    case kPacoScheduleTypeWeekday:
      return ([self.times count] == [another.times count]);
      
    case kPacoScheduleTypeWeekly:
      return (self.repeatRate == another.repeatRate &&
              self.weekDaysScheduled == another.weekDaysScheduled &&
              [self.times count] == [another.times count]);
      
    case kPacoScheduleTypeMonthly:
    {
      if (self.repeatRate != another.repeatRate) {
        return NO;
      }
      if ([self.times count] != [another.times count]) {
        return NO;
      }
      if (self.byDayOfMonth) {
        return self.dayOfMonth == another.dayOfMonth;
      } else { //by day of week
        return self.weekDaysScheduled == another.weekDaysScheduled;
      }
    }

    case kPacoScheduleTypeESM:
      return (self.esmFrequency == another.esmFrequency &&
              self.esmPeriod == another.esmPeriod &&
              ((self.esmWeekends && another.esmWeekends) || (!self.esmWeekends && !another.esmWeekends)) &&
              self.minimumBuffer == another.minimumBuffer);
      
    default:
      NSAssert(NO, @"should be a valid schedule type");
      return NO;
  }
}



@end
