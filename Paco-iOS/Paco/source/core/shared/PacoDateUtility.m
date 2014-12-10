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

#import "PacoDateUtility.h"

#import "PacoModel.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperiment.h"

@implementation PacoDateUtility

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
  return [[PacoDateUtility dateFormatter] stringFromDate:date];
}

+ (NSDate *)pacoDateForString:(NSString *)dateStr {
  return [[PacoDateUtility dateFormatter] dateFromString:dateStr];
}


/*
 * "2013/10/15"
**/
+ (NSDateFormatter*)dateFormatterWithYearAndDay {
  static NSDateFormatter* dateFormatter = nil;
  
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy/MM/dd"];
  });
  
  return dateFormatter;
}

+ (NSDate *)dateFromStringWithYearAndDay:(NSString *)dateStr {
  return [[PacoDateUtility dateFormatterWithYearAndDay] dateFromString:dateStr];
}

+ (NSString*)stringWithYearAndDayFromDate:(NSDate*)date {
  return [[PacoDateUtility dateFormatterWithYearAndDay] stringFromDate:date];
}


/*
 * 12:33:22-0700, Sep 12, 2013 
 */
+ (NSDateFormatter*)dateFormatterForAlertBody {
  static NSDateFormatter* dateFormatter = nil;
  
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"HH:mm:ssZZZ, MMM dd, YYYY"];
    [dateFormatter setTimeZone:[NSTimeZone systemTimeZone]];
  });
  
  return dateFormatter;
}

+ (NSString*)stringForAlertBodyFromDate:(NSDate*)date {
  return [[PacoDateUtility dateFormatterForAlertBody] stringFromDate:date];
}

+ (NSString*)timeStringFromMilliseconds:(long long)milliSeconds {
  long seconds = milliSeconds / 1000;
  long minutes = seconds / 60;
  long hours = minutes / 60;
  long minutesLeft = minutes - hours * 60;
  NSString* minutesString = [NSString stringWithFormat:@"%ld", minutesLeft];
  if (minutesLeft < 10) {
    minutesString = [NSString stringWithFormat:@"0%ld", minutesLeft];
  }
  int noon = 12;
  NSString* suffix = hours < noon ?  @"am" : @"pm";
  if (hours > noon) {
    hours -= noon;
  }
  NSString* timeString = [NSString stringWithFormat:@"%ld:%@%@", hours, minutesString, suffix];
  return timeString;
}

+ (NSString*)timeString24hrFromMilliseconds:(long long)milliSeconds {
  double hourInMS = 1000 * 60 * 60;
  double minInMS = 1000 * 60;
  double hours = milliSeconds / hourInMS;
  int hrs = floorf(hours);
  double leftover = milliSeconds - (hrs * hourInMS);
  double minutes = leftover / minInMS;
  int mins = floorf(minutes);
  return [NSString stringWithFormat:@"%02d:%02d", hrs, mins];
}

+ (NSString*)timeStringAMPMFromMilliseconds:(long long)milliSeconds {
  double hourInMS = 1000 * 60 * 60;
  double minInMS = 1000 * 60;
  double hours = milliSeconds / hourInMS;
  int hrs = floorf(hours);
  double leftover = milliSeconds - (hrs * hourInMS);
  double minutes = leftover / minInMS;
  int mins = floorf(minutes);
  BOOL isAM = hrs < 12;
  hrs %= 12;
  if (hrs == 0)
    hrs = 12;
  return [NSString stringWithFormat:@"%2d:%02d %@", hrs, mins, isAM ? @"AM" : @"PM"];
}

+ (NSUInteger)dayIndexOfDate:(NSDate *)date {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSUInteger day = [calendar ordinalityOfUnit:NSDayCalendarUnit inUnit:NSYearCalendarUnit forDate:date];
  assert(day > 0);
  return day - 1;
}

+ (int)weekdayIndexOfDate:(NSDate *)date {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *components = [calendar components:NSWeekdayCalendarUnit fromDate:date];
  int weekday = (int)[components weekday];
  return weekday - 1;
}

+ (NSInteger)weekOfYearIndexOfDate:(NSDate *)date {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *components = [calendar components:NSWeekOfYearCalendarUnit fromDate:date];
  NSInteger week = [components week];
  return week - 1;
}

+ (NSInteger)monthOfYearIndexOfDate:(NSDate *)date {
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
                        hrs24:(long)hrs24
                      minutes:(long)minutes {
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
            [PacoDateUtility pacoStringForDate:dayOfDate], [PacoDateUtility pacoStringForDate:date]);
      return date;
    } else {
      NSLog(@"SKIPPING %@ vs. %@",
            [PacoDateUtility pacoStringForDate:dayOfDate], [PacoDateUtility pacoStringForDate:date]);
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
  
  int weekdayIndex = (int)[self weekdayIndexOfDate:sameWeekAs];
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
  int weekday = (int)[self weekdayIndexOfDate:date];
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

+ (NSString*)escapedNameForTimeZone:(NSTimeZone *)timeZone {
    CFStringRef escaped = CFURLCreateStringByAddingPercentEscapes(NULL,
                                                                  (CFStringRef)[timeZone name], NULL, CFSTR("!*'();:@&=+$,/?%#[]"), kCFStringEncodingUTF8);
    return CFBridgingRelease(escaped);
}

+ (NSString*)escapedNameForSystemTimeZone {
    NSTimeZone *timeZone = [NSTimeZone systemTimeZone];
    return [PacoDateUtility escapedNameForTimeZone:timeZone];
}




@end
