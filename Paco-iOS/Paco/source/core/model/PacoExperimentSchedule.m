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
   @(self.byDayOfMonth), @"byDayOfMonth",
   @(self.byDayOfWeek), @"byDayOfWeek",
   @(self.dayOfMonth), @"dayOfMonth",
   @(self.esmEndHour), @"esmEndHour",
   @(self.esmFrequency), @"esmFrequency",
   @(self.esmPeriodInDays), @"esmPeriodInDays",
   @(self.esmStartHour), @"esmStartHour",
   @(self.esmWeekends), @"esmWeekends",
   @([self.scheduleId longLongValue]), @"id",
   @(self.nthOfMonth), @"nthOfMonth",
   @(self.repeatRate), @"repeatRate",

   @(self.timeout), @"timeout",
   @(self.minimumBuffer), @"minimumBuffer",
   @(self.scheduleType), @"scheduleType",
   self.times, @"times",
   @(self.userEditable), @"userEditable",
   @(self.weekDaysScheduled), @"weekDaysScheduled",
   nil];

  if ([self.esmScheduleList count] > 0) {
    NSMutableArray* dateStringArr = [NSMutableArray arrayWithCapacity:[self.esmScheduleList count]];
    for (NSDate* date in self.esmScheduleList) {
      [dateStringArr addObject:[PacoDateUtility pacoStringForDate:date]];
    }
    scheduleJson[@"esmScheduleList"] = dateStringArr;
  }
  return scheduleJson;
}

+ (id)pacoExperimentScheduleFromJSON:(id)jsonObject {
  PacoExperimentSchedule *schedule = [[PacoExperimentSchedule alloc] init];
  NSDictionary *scheduleMembers = jsonObject;
  schedule.byDayOfMonth = [scheduleMembers[@"byDayOfMonth"] boolValue];
  schedule.byDayOfWeek = [scheduleMembers[@"byDayOfWeek"] boolValue];
  schedule.dayOfMonth = [scheduleMembers[@"dayOfMonth"] intValue];
  schedule.esmEndHour = [scheduleMembers[@"esmEndHour"] longLongValue];
  schedule.esmFrequency = [scheduleMembers[@"esmFrequency"] intValue];
  schedule.esmPeriodInDays = [scheduleMembers[@"esmPeriodInDays"] longLongValue];
  NSAssert(schedule.esmPeriodInDays == 0 ||
           schedule.esmPeriodInDays == 1 ||
           schedule.esmPeriodInDays == 2 , @"esmPeriodInDays should only be 0, 1 or 2!");
  schedule.esmPeriod = (PacoScheduleRepeatPeriod)schedule.esmPeriodInDays;
  schedule.esmStartHour = [scheduleMembers[@"esmStartHour"] longLongValue];
  schedule.esmWeekends = [scheduleMembers[@"esmWeekends"] boolValue];
  schedule.scheduleId = [NSString stringWithFormat:@"%lld", [scheduleMembers[@"id"] longLongValue]];
  schedule.nthOfMonth = [scheduleMembers[@"nthOfMonth"] intValue];
  schedule.repeatRate = [scheduleMembers[@"repeatRate"] integerValue];
  schedule.scheduleType = [scheduleMembers[@"scheduleType"] intValue];
  schedule.timeout = [scheduleMembers[@"timeout"] intValue];

  NSNumber* minimumBufferNum = scheduleMembers[@"minimumBuffer"];
  if (minimumBufferNum != nil) {
    schedule.minimumBuffer = [minimumBufferNum intValue];
  } else {
    schedule.minimumBuffer = 59; //default
  }

  schedule.times = scheduleMembers[@"times"];
  schedule.times = [schedule.times sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
    NSDate *lhs = obj1;
    NSDate *rhs = obj2;
    return [lhs compare:rhs];
  }];

  NSNumber* userEditableObject = scheduleMembers[@"userEditable"];
  if (userEditableObject) {
    schedule.userEditable = [userEditableObject boolValue];
  } else {
    schedule.userEditable = YES; //userEditable is YES by default
  }
  schedule.weekDaysScheduled = [scheduleMembers[@"weekDaysScheduled"] intValue];

  // !!! TPE temporary timeout fix for issue #9
  if (schedule.timeout == 0) {
    if (schedule.scheduleType == kPacoScheduleTypeESM) {
      schedule.timeout = 59;
    } else {
      schedule.timeout = 479;
    }
  }
  NSArray* esmScheduleStringArr = scheduleMembers[@"esmScheduleList"];
  if ([esmScheduleStringArr count] > 0) {
    NSMutableArray* dateArr = [NSMutableArray arrayWithCapacity:[esmScheduleStringArr count]];
    for (NSString* dateStr in esmScheduleStringArr) {
      [dateArr addObject:[PacoDateUtility pacoDateForString:dateStr]];
    }
    schedule.esmScheduleList = dateArr;
  }

  return schedule;
}


- (id)copyWithZone:(NSZone *)zone {
  PacoExperimentSchedule* copy = [[[self class] allocWithZone:zone] init];
  copy.byDayOfMonth = self.byDayOfMonth;
  copy.byDayOfWeek = self.byDayOfWeek;
  copy.dayOfMonth = self.dayOfMonth;
  copy.esmEndHour = self.esmEndHour;
  copy.esmFrequency = self.esmFrequency;
  copy.esmPeriodInDays = self.esmPeriodInDays;
  copy.esmPeriod = self.esmPeriod;
  copy.esmStartHour = self.esmStartHour;
  copy.esmWeekends = self.esmWeekends;
  copy.scheduleId = [self.scheduleId copyWithZone:zone];
  copy.nthOfMonth = self.nthOfMonth;
  copy.repeatRate = self.repeatRate;
  copy.scheduleType = self.scheduleType;
  copy.times = [self.times copyWithZone:zone];
  copy.userEditable = self.userEditable;
  copy.weekDaysScheduled = self.weekDaysScheduled;
  copy.timeout = self.timeout;
  copy.minimumBuffer = self.minimumBuffer;
  copy.esmScheduleList = [self.esmScheduleList copyWithZone:zone];
  return copy;
}


- (NSString *)description {
  return [NSString stringWithFormat:@"<PacoExperimentSchedule:%p - "
          @"byDayOfMonth=%d "
          @"byDayOfWeek=%d "
          @"dayOfMonth=%ld "
          @"esmStartHour=%@ "
          @"esmEndHour=%@ "
          @"esmMinBuffer=%ld "
          @"esmFrequency=%ld "
          @"esmPeriodInDays=%lld "
          @"esmPeriod=%@ "
          @"esmWeekends=%d "
          @"scheduleId=%@ "
          @"nthOfMonth=%ld "
          @"repeatRate=%ld "
          @"scheduleType=%ld "
          @"times=%@ "
          @"timeout=%ld "
          @"minimumBuffer=%ld "
          @"weekDaysScheduled=%@ >",
          self,
          self.byDayOfMonth,
          self.byDayOfWeek,
          (long)self.dayOfMonth,
          [PacoDateUtility timeStringFromMilliseconds:self.esmStartHour],
          [PacoDateUtility timeStringFromMilliseconds:self.esmEndHour],
          (long)self.minimumBuffer,
          (long)self.esmFrequency,
          self.esmPeriodInDays,
          [self periodString],
          self.esmWeekends,
          self.scheduleId,
          (long)self.nthOfMonth,
          (long)self.repeatRate,
          (long)self.scheduleType,
          [self.times pacoDescriptionForTimeNumbers],
          (long)self.timeout,
          (long)self.minimumBuffer,
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


- (NSArray*)weeklyConfigureTable {
  if (self.scheduleType != kPacoScheduleTypeWeekly) {
    return nil;
  }
  if (0 == self.weekDaysScheduled) { //none of any day is selected, should be validated by server
    return nil;
  }

  NSMutableArray* table = [NSMutableArray arrayWithCapacity:kPacoNumOfDaysInWeek];
  for (int digit = 0; digit < kPacoNumOfDaysInWeek; digit++) {
    BOOL daySelected = (self.weekDaysScheduled & (1 << digit));
    [table addObject:@(daySelected)];
  }
  return table;
}


- (int)dayIndexByDayOfWeek {
  if (self.scheduleType != kPacoScheduleTypeMonthly || !self.byDayOfWeek) {
    return 0;
  }
  for (int digit = 0; digit < kPacoNumOfDaysInWeek; digit++) {
    BOOL daySelected = (self.weekDaysScheduled & (1 << digit));
    if (daySelected) {
      return digit + 1;
    }
  }
  return 0;
}


- (NSString *)jsonString {
  NSMutableString *json = [NSMutableString stringWithString:@"{"];

  [json appendFormat:@"type = %@,", [self typeString]];

  if (self.scheduleType == kPacoScheduleTypeESM) {
    [json appendFormat:@"frequency = %ld,", (long)self.esmFrequency];
    [json appendFormat:@"esmPeriod = %@,", [self periodString]];
    [json appendFormat:@"startHour = %@,", [PacoDateUtility timeStringFromMilliseconds:self.esmStartHour]];
    [json appendFormat:@"endHour = %@,", [PacoDateUtility timeStringFromMilliseconds:self.esmEndHour]];
    [json appendFormat:@"weekends = %@,", self.esmWeekends ? @"true" : @"false"];
  }
  [json appendFormat:@"times = %@,", [self.times pacoDescriptionForTimeNumbers]];
  [json appendFormat:@"repeatRate = %ld,", (long)self.repeatRate];
  [json appendFormat:@"daysOfWeek = %@,", [self weekDaysScheduledString]];
  [json appendFormat:@"nthOfMonth = %ld,", (long)self.nthOfMonth];
  [json appendFormat:@"byDayOfMonth = %@,", self.byDayOfMonth ? @"true" : @"false"];
  [json appendFormat:@"dayOfMonth = %ld", (long)self.dayOfMonth];
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

- (NSString*)validate {
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


//Note: userEditable is ignored, since it won't influence notification system
- (BOOL)compareWithSchedule:(PacoExperimentSchedule*)another includeConfigure:(BOOL)includeConfigure {
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

  BOOL hasSameTimes = includeConfigure ? [self.times isEqualToArray:another.times] :
                                         [self.times count] == [another.times count];
  switch (self.scheduleType) {
    case kPacoScheduleTypeDaily: {
      return (self.repeatRate == another.repeatRate && hasSameTimes);
    }

    case kPacoScheduleTypeWeekday: {
      return hasSameTimes;
    }

    case kPacoScheduleTypeWeekly: {
      return (self.repeatRate == another.repeatRate &&
              self.weekDaysScheduled == another.weekDaysScheduled &&
              hasSameTimes);
    }

    case kPacoScheduleTypeMonthly: {
      if (self.repeatRate != another.repeatRate) {
        return NO;
      }
      if (!hasSameTimes) {
        return NO;
      }
      if (self.byDayOfMonth) {
        return self.dayOfMonth == another.dayOfMonth;
      } else { //by day of week
        return (self.weekDaysScheduled == another.weekDaysScheduled &&
                self.nthOfMonth == another.nthOfMonth);
      }
    }

    case kPacoScheduleTypeESM: {
      BOOL isEqual = (self.esmFrequency == another.esmFrequency &&
                      self.esmPeriod == another.esmPeriod &&
                      ((self.esmWeekends && another.esmWeekends) || (!self.esmWeekends && !another.esmWeekends)) &&
                      self.minimumBuffer == another.minimumBuffer);
      if (includeConfigure) {
        BOOL hasSameStartEndTime = (self.esmStartHour == another.esmStartHour &&
                                    self.esmEndHour == another.esmEndHour);
        return isEqual && hasSameStartEndTime;
      } else {
        return isEqual;
      }
    }

    default:
      NSAssert(NO, @"should be a valid schedule type");
      return NO;
  }
}


//ESM startHour and endHour are ignored here
//times are considered the same if the number of times are the same
- (BOOL)isEqualToSchedule:(PacoExperimentSchedule*)another {
  return [self compareWithSchedule:another includeConfigure:NO];
}

- (BOOL)isExactlyEqualToSchedule:(PacoExperimentSchedule*)another {
  return [self compareWithSchedule:another includeConfigure:YES];
}


@end
