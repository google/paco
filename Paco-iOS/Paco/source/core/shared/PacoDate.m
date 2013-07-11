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

+ (NSString *)pacoStringForDate:(NSDate *)date {
  NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
  // tpe: temporary disabled changing the timezone to GMT as we're not sure if this is correct behavior
  // [dateFormatter setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
  [dateFormatter setDateFormat:@"yyyy/MM/dd HH:mm:ssZ"];
  return [dateFormatter stringFromDate:date];
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
      NSLog(@"LHS=%@ RHS=%@", dayOfDate, date);
      return date;
    } else {
      NSLog(@"SKIPPING %@ vs. %@", dayOfDate, date);
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
    if (dateScheduled.timeIntervalSince1970 > dayOfDate.timeIntervalSince1970) {
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

+ (NSArray *)createESMScheduleDates:(PacoExperiment *)experiment fromThisDate:(NSDate *)fromThisDate {
  double startSeconds = experiment.schedule.esmStartHour / 1000.0;
  double startMinutes = startSeconds / 60.0;
  double startHour = startMinutes / 60.0;
  int iStartHour = ((int)startHour);
  startMinutes -= (iStartHour * 60);
  double millisecondsPerDay = experiment.schedule.esmEndHour - experiment.schedule.esmStartHour;
  double secondsPerDay = millisecondsPerDay / 1000.0;
  double minutesPerDay = secondsPerDay / 60.0;
  double hoursPerDay = minutesPerDay / 60.0;

  int startDay = experiment.schedule.esmWeekends ? 0 : 1;
  
  double durationMinutes = 0;
  switch (experiment.schedule.esmPeriod) {
    case kPacoSchedulePeriodDay: {
        durationMinutes = minutesPerDay;
        startDay = [PacoDate weekdayIndexOfDate:fromThisDate];
      }
      break;
    case kPacoSchedulePeriodWeek: {
        durationMinutes = minutesPerDay * (experiment.schedule.esmWeekends ? 7.0 : 5.0);
      }
      break;
    case kPacoSchedulePeriodMonth: {
        //about 21.74 work days per month on average.
        durationMinutes = minutesPerDay * (experiment.schedule.esmWeekends ? 30 : 21.74);
      }
      break;
  }

  NSMutableArray *randomDates = [NSMutableArray array];
  for (int i = 0; i < experiment.schedule.esmFrequency; ++i) {
    u_int32_t value = 0;
    // Do half random and half random uniform ?
    if ((i % 2) == 0) {
      value = arc4random_uniform(0xFFFFFFFF);
    } else {
      value = arc4random();
    }

    double max = (double)0xFFFFFFFF;
    double randomValue = (double)value / (double)max;

    int offsetMinutes = (durationMinutes * randomValue);
    int offsetHours = ((durationMinutes * randomValue) / 60.0);
    int offsetDays = ((durationMinutes * randomValue) / 60.0 / hoursPerDay);
    offsetMinutes -= offsetHours * 60;
    offsetHours -= offsetDays * hoursPerDay;

    NSDate *date = [self dateSameWeekAs:fromThisDate dayIndex:(startDay + offsetDays) hr24:(iStartHour + offsetHours) min:(startMinutes + offsetMinutes)];
    [randomDates addObject:date];
  }

  return [randomDates sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
    NSDate *lhs = obj1;
    NSDate *rhs = obj2;
    return [lhs compare:rhs];
  }];
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
    BOOL done = NO;
    NSDate *scheduled = nil;
    NSDate *from = fromThisDate;
    int max = 500;
    while (!done) {
      max -= 1;
      if (max == 0)
        break;
      NSArray *scheduleDates = experiment.schedule.esmSchedule;
      if (!scheduleDates.count) {
        scheduleDates = [self createESMScheduleDates:experiment fromThisDate:from];
        experiment.schedule.esmSchedule = scheduleDates;
        NSLog(@"NEW SCHEDULE \n %@", scheduleDates);
      }
      scheduled = [self nextTimeFromScheduledDates:scheduleDates onDayOfDate:fromThisDate];
      //if (!scheduled) {
      //  NSLog(@"THIS DATE = %@", fromThisDate);
      //  NSLog(@"TO CHOOSE FROM A = %@", scheduleDates);
      //  scheduleDates = [self createESMScheduleDates:experiment fromThisDate:from];
      //  experiment.schedule.esmSchedule = scheduleDates;
      //  NSLog(@"TO CHOOSE FROM B = %@", scheduleDates);
      //  scheduled = [self nextTimeFromScheduledDates:scheduleDates onDayOfDate:from];
     // }
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
            assert(!"Invalid esm period");
        }
        //from = experiment.schedule.esmSchedule.lastObject;
        experiment.schedule.esmSchedule = nil;
      }
      if (scheduled) {
        done = YES;
      }
    }
    assert(scheduled);
    return scheduled;
  }
  break;
  case kPacoScheduleTypeSelfReport:
  case kPacoScheduleTypeAdvanced:
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
