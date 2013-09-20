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

#import "PacoDate.h"

#import "PacoModel.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperiment.h"

@implementation PacoDate

/*
 * 2013/07/25 12:33:22-0700
 */
+ (NSDateFormatter*)dateFormatter {
  static NSDateFormatter* dateFormatter = nil;
  
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy/MM/dd HH:mm:ssZ"];
  });
  
  return dateFormatter;
}

+ (NSString *)pacoStringForDate:(NSDate *)date {
  return [[PacoDate dateFormatter] stringFromDate:date];
}

+ (NSDate *)pacoDateForString:(NSString *)dateStr {
  return [[PacoDate dateFormatter] dateFromString:dateStr];
}


/*
 * 12:33:22-0700, Sep 12, 2013 
 */
+ (NSDateFormatter*)debugDateFormatter {
  static NSDateFormatter* debugDateFormatter = nil;
  
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    debugDateFormatter = [[NSDateFormatter alloc] init];
    [debugDateFormatter setDateFormat:@"HH:mm:ssZZZ, MMM dd, YYYY"];
    [debugDateFormatter setTimeZone:[NSTimeZone systemTimeZone]];
  });
  
  return debugDateFormatter;
}

+ (NSString*)debugStringForDate:(NSDate*)date {
  return [[PacoDate debugDateFormatter] stringFromDate:date];
}


+ (int)dayIndexOfDate:(NSDate *)date {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSUInteger day = [calendar ordinalityOfUnit:NSDayCalendarUnit inUnit:NSYearCalendarUnit forDate:date];
  assert(day > 0);
  return day - 1;
}

+ (int)weekdayIndexOfDate:(NSDate *)date {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *components = [calendar components:NSWeekdayCalendarUnit fromDate:date];
  NSInteger weekday = [components weekday];
  return weekday - 1;
}

+ (int)weekOfYearIndexOfDate:(NSDate *)date {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *components = [calendar components:NSWeekOfYearCalendarUnit fromDate:date];
  NSInteger week = [components week];
  return week - 1;
}

+ (int)monthOfYearIndexOfDate:(NSDate *)date {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *components = [calendar components:NSMonthCalendarUnit fromDate:date];
  NSInteger month = [components month];
  return month - 1;
}

+ (NSDate *)midnightThisDate:(NSDate *)date {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSCalendarUnit units = NSYearCalendarUnit |
                         NSMonthCalendarUnit |
                         NSDayCalendarUnit |
                         NSHourCalendarUnit |
                         NSMinuteCalendarUnit |
                         NSSecondCalendarUnit |
                         NSWeekdayCalendarUnit |
                         NSWeekOfYearCalendarUnit;
  NSDateComponents *components = [calendar components:units fromDate:date];
  components.hour = 0;
  components.minute = 0;
  components.second = 0;
  return [calendar dateFromComponents:components];
}

+ (NSDate *)firstDayOfMonth:(NSDate *)date {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSCalendarUnit units = NSYearCalendarUnit |
                         NSMonthCalendarUnit |
                         NSDayCalendarUnit |
                         NSHourCalendarUnit |
                         NSMinuteCalendarUnit |
                         NSSecondCalendarUnit |
                         NSWeekdayCalendarUnit |
                         NSWeekOfYearCalendarUnit;
  NSDateComponents *components = [calendar components:units fromDate:date];
  components.day = 1;
  components.hour = 0;
  components.minute = 0;
  components.second = 0;
  return [calendar dateFromComponents:components];
}

+ (NSDate *)timeOfDayThisDate:(NSDate *)date
                        hrs24:(int)hrs24
                      minutes:(int)minutes {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSCalendarUnit units = NSYearCalendarUnit |
                         NSMonthCalendarUnit |
                         NSDayCalendarUnit |
                         NSHourCalendarUnit |
                         NSMinuteCalendarUnit |
                         NSSecondCalendarUnit |
                         NSWeekdayCalendarUnit |
                         NSWeekOfYearCalendarUnit;
  NSDateComponents *components = [calendar components:units fromDate:date];
  components.hour = hrs24;
  components.minute = minutes;
  components.second = 0;
  return [calendar dateFromComponents:components];
}

+ (NSDate *)nextTimeFromScheduledDates:(NSArray *)scheduledDates
                           onDayOfDate:(NSDate *)dayOfDate {
  //NSDate *now = [self midnightThisDate:dayOfDate];
  for (NSDate *date in scheduledDates) {
    if ([dayOfDate compare:date] == NSOrderedAscending) {
      NSLog(@"LHS=%@ RHS=%@",
            [PacoDate pacoStringForDate:dayOfDate], [PacoDate pacoStringForDate:date]);
      return date;
    } else {
      NSLog(@"SKIPPING %@ vs. %@",
            [PacoDate pacoStringForDate:dayOfDate], [PacoDate pacoStringForDate:date]);
    }
  }
  // Time for a new list of scheduled dates.
  return nil;
}

+ (NSDate *)nextTimeFromScheduledTimes:(NSArray *)scheduledTimes
                           onDayOfDate:(NSDate *)dayOfDate {
  NSDate *now = [self midnightThisDate:dayOfDate];
  for (NSNumber *longSeconds in scheduledTimes) {
    long milliseconds = [longSeconds longValue];
    long seconds = milliseconds / 1000;
    long minutes = seconds / 60;
    long hrs = minutes / 60;
    hrs %= 24;
    minutes %= 60;
    NSDate *dateScheduled = [self timeOfDayThisDate:now hrs24:hrs minutes:minutes];
    if (dateScheduled.timeIntervalSince1970 >= dayOfDate.timeIntervalSince1970) {
      return dateScheduled;
    }
  }
  // Must be the next day.
  return nil;
}

+ (NSDate *)date:(NSDate *)date thisManyDaysFrom:(int)daysFrom {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *oneDay = [[NSDateComponents alloc] init];
  oneDay.day = daysFrom;
  return [calendar dateByAddingComponents:oneDay toDate:date options:0];
}

+ (NSDate *)date:(NSDate *)date thisManyWeeksFrom:(int)weeksFrom {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *weeks = [[NSDateComponents alloc] init];
  weeks.day = weeksFrom * 7;
  return [calendar dateByAddingComponents:weeks toDate:date options:0];
}

+ (NSDate *)date:(NSDate *)date thisManyMonthsFrom:(int)monthsFrom {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *months = [[NSDateComponents alloc] init];
  months.month = monthsFrom;
  return [calendar dateByAddingComponents:months toDate:date options:0];
}

+ (NSDate *)dateSameWeekAs:(NSDate *)sameWeekAs
                  dayIndex:(int)dayIndex
                      hr24:(int)hr24
                       min:(int)min {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  
  int weekdayIndex = [self weekdayIndexOfDate:sameWeekAs];
  int diff = dayIndex - weekdayIndex;
  NSDate *day = [self date:sameWeekAs thisManyDaysFrom:diff];
  day = [self midnightThisDate:day];
  NSDateComponents *time = [[NSDateComponents alloc] init];
  time.hour = hr24;
  time.minute = min;
  return [calendar dateByAddingComponents:time toDate:day options:0];
}

+ (NSDate *)dateSameMonthAs:(NSDate *)sameMonthAs
                   dayIndex:(int)dayIndex {
  NSDate *monthStart = [self firstDayOfMonth:sameMonthAs];
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *day = [[NSDateComponents alloc] init];
  day.day = dayIndex;
  return [calendar dateByAddingComponents:day toDate:monthStart options:0];
}

+ (NSDate *)dateOnNthOfMonth:(NSDate *)sameMonthAs
                         nth:(int)nth
                    dayFlags:(unsigned int)dayFlags {
  NSDate *startMonth = [self dateSameMonthAs:sameMonthAs dayIndex:0];
  NSDateComponents *day = [[NSDateComponents alloc] init];
  day.weekdayOrdinal = nth;
  day.weekday = 0;
  for (int i = 0; i < 7; ++i) {
    if (dayFlags & (1 << i)) {
      day.weekday = i;
      break;
    }
  }
  NSCalendar *calendar = [NSCalendar currentCalendar];
  return [calendar dateByAddingComponents:day toDate:startMonth options:0];

}

+ (NSDate *)nextScheduledDay:(NSUInteger)dayFlags fromDate:(NSDate *)date {
  int weekday = [self weekdayIndexOfDate:date];
  int startIndex = ((weekday + 1) % 7);
  int count = 1;
  for (int i = startIndex; i < 7; ++i) {
    NSUInteger flag = 1 << i;
    if (dayFlags & flag) {
      return [self date:date thisManyDaysFrom:count];
    }
    count++;
  }
  return nil;
}


+ (NSUInteger)randomUnsignedIntegerBetweenMin:(NSUInteger)min andMax:(NSUInteger)max {
  NSAssert(max >= min, @"max should be larger than or equal to min!");
  int temp = arc4random_uniform(max - min + 1);  //[0, max-min]
  return temp + min; //[min, max]
}


//YMZ:TODO: check this algorithm for kPacoSchedulePeriodWeek and kPacoSchedulePeriodMonth
+ (NSArray *)createESMScheduleDates:(PacoExperimentSchedule*)experimentSchedule
                       fromThisDate:(NSDate*)fromThisDate {
  double startSeconds = experimentSchedule.esmStartHour / 1000.0;
  double startMinutes = startSeconds / 60.0;
  double startHour = startMinutes / 60.0;
  int iStartHour = ((int)startHour);
  startMinutes -= (iStartHour * 60);
  double millisecondsPerDay = experimentSchedule.esmEndHour - experimentSchedule.esmStartHour;
  double secondsPerDay = millisecondsPerDay / 1000.0;
  double minutesPerDay = secondsPerDay / 60.0;
  double hoursPerDay = minutesPerDay / 60.0;

  int startDay = experimentSchedule.esmWeekends ? 0 : 1;
  
  double durationMinutes = 0;
  switch (experimentSchedule.esmPeriod) {
    case kPacoSchedulePeriodDay: {
        durationMinutes = minutesPerDay;
        startDay = [PacoDate weekdayIndexOfDate:fromThisDate];
      }
      break;
    case kPacoSchedulePeriodWeek: {
        durationMinutes = minutesPerDay * (experimentSchedule.esmWeekends ? 7.0 : 5.0);
      }
      break;
    case kPacoSchedulePeriodMonth: {
        //about 21.74 work days per month on average.
        durationMinutes = minutesPerDay * (experimentSchedule.esmWeekends ? 30 : 21.74);
      }
      break;
  }
  
  int NUM_OF_BUCKETS = experimentSchedule.esmFrequency;
  NSAssert(NUM_OF_BUCKETS >= 1, @"The number of buckets should be larger than or equal to 1");
  double MINUTES_PER_BUCKET = durationMinutes/((double)NUM_OF_BUCKETS);
  
  NSMutableArray *randomDates = [NSMutableArray array];
  int lowerBound = 0;
  for (int bucketIndex = 1; bucketIndex <= NUM_OF_BUCKETS; ++bucketIndex) {
    int upperBound = MINUTES_PER_BUCKET * bucketIndex;
    int upperBoundByMinBuffer =
        durationMinutes - experimentSchedule.minimumBuffer * (NUM_OF_BUCKETS - bucketIndex);
    if (upperBound > upperBoundByMinBuffer) {
      upperBound = upperBoundByMinBuffer;
//      NSLog(@"%d: upperBound is adjusted to %d", bucketIndex, upperBound);
    }
//    NSLog(@"low=%d, upper=%d", lowerBound, upperBound);
    int offsetMinutes = [self randomUnsignedIntegerBetweenMin:lowerBound andMax:upperBound];
//    NSLog(@"RandomMinutes=%d", offsetMinutes);
    int offsetHours = offsetMinutes / 60.0;
    int offsetDays = offsetHours / hoursPerDay;
    
    if (experimentSchedule.esmPeriod == kPacoSchedulePeriodDay && offsetDays > 0) {
      double offsetHoursInDouble = offsetMinutes/60.0;
      if (offsetHoursInDouble <= hoursPerDay) {
        offsetDays = 0;
      } else {
        NSAssert(NO, @"offsetDays should always be 0 for kPacoSchedulePeriodDay");
      }
    }
    
    offsetMinutes -= offsetHours * 60;
    offsetHours -= offsetDays * hoursPerDay;

    NSDate *date = [self dateSameWeekAs:fromThisDate dayIndex:(startDay + offsetDays) hr24:(iStartHour + offsetHours) min:(startMinutes + offsetMinutes)];
    [randomDates addObject:date];
    
    lowerBound = upperBound;
    int lowestBoundForNextSchedule = offsetMinutes + experimentSchedule.minimumBuffer;
    if (lowerBound < lowestBoundForNextSchedule) {
      lowerBound = lowestBoundForNextSchedule;
//      NSLog(@"%d: lowerBound is adjusted to %d", bucketIndex, lowestBoundForNextSchedule);
    }
  }

  return [randomDates sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
    NSDate *lhs = obj1;
    NSDate *rhs = obj2;
    return [lhs compare:rhs];
  }];
}

//YMZ:TODO: why 500? when will a nil result be returned?
+ (NSDate *)nextESMScheduledDateForExperiment:(PacoExperiment *)experiment
                                 fromThisDate:(NSDate *)fromThisDate {
  NSDate *scheduled = nil;
  BOOL done = NO;
  NSDate *from = fromThisDate;
  int max = 500;
  while (!done) {
    max -= 1;
    if (max == 0)
      break;
    NSArray *scheduleDates = experiment.schedule.esmScheduleList;
    if (!scheduleDates.count) {
      scheduleDates = [self createESMScheduleDates:experiment.schedule fromThisDate:from];
      experiment.schedule.esmScheduleList = scheduleDates;
      NSLog(@"NEW SCHEDULE: ");
      NSLog(@"(");
      for (NSDate* date in scheduleDates) {
        NSLog(@"%@", [PacoDate pacoStringForDate:date]);
      }
      NSLog(@")");
    }
    scheduled = [self nextTimeFromScheduledDates:scheduleDates onDayOfDate:fromThisDate];
    if (!scheduled) {
      // need to either schedule entire days here or know whether to use last time or
      // whether to use today+1 for generating the new schedule
      

      // Must be for the next day/week/month.
      switch (experiment.schedule.esmPeriod) {
        case kPacoScheduleRepeatPeriodDay:
          from = [PacoDate date:from thisManyDaysFrom:1];
          break;
        case kPacoScheduleRepeatPeriodWeek:
          from = [PacoDate date:from thisManyWeeksFrom:1];
          break;
        case kPacoScheduleRepeatPeriodMonth:
          from = [PacoDate date:from thisManyMonthsFrom:1];
          break;
        default:
          NSAssert(NO, @"Invalid esm period");
      }
      experiment.schedule.esmScheduleList = nil;
    }
    if (scheduled) {
      done = YES;
    }
  }
  return scheduled;
}

+ (NSDate*)nextScheduledDateFromNow:(PacoExperiment *)experiment {
  return [self nextScheduledDateForExperiment:experiment fromThisDate:[NSDate dateWithTimeIntervalSinceNow:0]];
}


+ (NSDate *)nextScheduledDateForExperiment:(PacoExperiment *)experiment
                              fromThisDate:(NSDate *)fromThisDate {
  switch (experiment.schedule.scheduleType) {
  case kPacoScheduleTypeDaily: {
      // Today 12:30pm -> Today 1:30pm
      NSDate *repeatTime = [self nextTimeFromScheduledTimes:experiment.schedule.times
                                                onDayOfDate:fromThisDate];
      if (repeatTime) {
        // return Today 1:30pm
        return repeatTime;
      } else {
        // Today 12:30pm -> NextDay 9:00am
        NSDate *repeatDay = [self date:fromThisDate
                                thisManyDaysFrom:experiment.schedule.repeatPeriod];
        repeatDay = [self midnightThisDate:repeatDay];
        return [self nextTimeFromScheduledTimes:experiment.schedule.times onDayOfDate:repeatDay];
      }
    }
  case kPacoScheduleTypeWeekly: {
      NSDate *thisWeek = [self nextScheduledDay:experiment.schedule.weekDaysScheduled fromDate:fromThisDate];
      if (thisWeek) {
        return thisWeek;
      }
      NSDate *repeatWeek = [self date:fromThisDate thisManyWeeksFrom:experiment.schedule.repeatPeriod];
      repeatWeek = [self dateSameWeekAs:repeatWeek dayIndex:0 hr24:0 min:0];
      NSDate *scheduledDay = [self nextScheduledDay:experiment.schedule.weekDaysScheduled fromDate:repeatWeek];
      assert(scheduledDay);
      NSDate *nextTime = [self nextTimeFromScheduledTimes:experiment.schedule.times onDayOfDate:scheduledDay];
      return nextTime;
    }
    break;
  case kPacoScheduleTypeWeekday: {
      unsigned int weekdayFlags = (1<<1) | (1<<2) | (1<<3) | (1<<4) | (1<<5);
      NSDate *thisWeek = [self nextScheduledDay:weekdayFlags fromDate:fromThisDate];
      if (thisWeek) {
        return thisWeek;
      }
      NSDate *repeatWeek = [self date:fromThisDate thisManyWeeksFrom:experiment.schedule.repeatPeriod];
      repeatWeek = [self dateSameWeekAs:repeatWeek dayIndex:0 hr24:0 min:0];
      NSDate *scheduledDay = [self nextScheduledDay:weekdayFlags fromDate:repeatWeek];
      assert(scheduledDay);
      NSDate *nextTime = [self nextTimeFromScheduledTimes:experiment.schedule.times onDayOfDate:scheduledDay];
      return nextTime;
    }
    break;
  case kPacoScheduleTypeMonthly: {
      NSDate *scheduledMonth = [self date:fromThisDate thisManyMonthsFrom:experiment.schedule.repeatPeriod];
      if (experiment.schedule.byDayOfWeek) {
        scheduledMonth = [self dateOnNthOfMonth:scheduledMonth nth:experiment.schedule.dayOfMonth dayFlags:experiment.schedule.weekDaysScheduled];
      } else {
        scheduledMonth = [self dateSameMonthAs:scheduledMonth dayIndex:experiment.schedule.dayOfMonth];
      }
      return scheduledMonth;
    }
    break;
  case kPacoScheduleTypeESM: {
    NSDate *scheduled = [self nextESMScheduledDateForExperiment:experiment fromThisDate:fromThisDate];
    NSAssert(scheduled != nil, @"ESM schedule should not be nil!");
    return scheduled;
  }
  break;
  case kPacoScheduleTypeSelfReport:
  case kPacoScheduleTypeAdvanced:
  case kPacoScheduleTypeTesting:    
    break;
  }
  return nil;
}

+ (NSString *)timestampFromDate:(NSDate *)date {
  NSTimeInterval value = [date timeIntervalSince1970];
  return [NSString stringWithFormat:@"%f", value];
}

+ (NSDate *)dateFromTimestamp:(NSString *)string {
  NSTimeInterval value = [string doubleValue];
  return [NSDate dateWithTimeIntervalSince1970:value];
}

@end
