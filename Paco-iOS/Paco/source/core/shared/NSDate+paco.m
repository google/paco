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


#import "NSDate+Paco.h"

@implementation NSDate (Paco)

- (BOOL)pacoEarlierThanDate:(NSDate*)another {
  return ([self timeIntervalSinceDate:another] < 0);
}

- (BOOL)pacoLaterThanDate:(NSDate*)another {
  return ([self timeIntervalSinceDate:another] > 0);
}

- (BOOL)pacoEqualToDate:(NSDate*)another {
  return (0 == [self timeIntervalSinceDate:another]);
}

- (BOOL)pacoNoEarlierThanDate:(NSDate*)another {
  return ![self pacoEarlierThanDate:another];
}

- (BOOL)pacoNoLaterThanDate:(NSDate*)another {
  return ![self pacoLaterThanDate:another];
}

- (NSDate*)pacoCurrentDayAtMidnight {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSCalendarUnit units = NSYearCalendarUnit |
                         NSMonthCalendarUnit |
                         NSDayCalendarUnit |
                         NSHourCalendarUnit |
                         NSMinuteCalendarUnit |
                         NSSecondCalendarUnit |
                         NSWeekdayCalendarUnit |
                         NSWeekOfYearCalendarUnit;
  NSDateComponents *components = [calendar components:units fromDate:self];
  components.hour = 0;
  components.minute = 0;
  components.second = 0;
  return [calendar dateFromComponents:components];
}

//intervalDays should be larger than or equal to 0
- (NSDate*)pacoFutureDateAtMidnightWithInterval:(NSInteger)intervalDays {
  NSAssert(intervalDays >= 0, @"intervalDays should be larger than or equal to 0");
  
  NSDate* midnightDate = [self pacoCurrentDayAtMidnight];
  NSCalendar* calendar = [NSCalendar currentCalendar];
  NSDateComponents* dayComponents = [[NSDateComponents alloc] init];
  dayComponents.day = intervalDays;
  return [calendar dateByAddingComponents:dayComponents toDate:midnightDate options:0];
}

- (NSDate*)pacoNextDayAtMidnight {
  return [self pacoFutureDateAtMidnightWithInterval:1];
}

//The receiver must be a midnight date!
- (NSDate*)pacoTimeWithIntervalOfHoursIn24:(NSInteger)hoursIn24
                                   minutes:(NSInteger)minutes
                                   seconds:(NSInteger)seconds {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSCalendarUnit units = NSYearCalendarUnit |
                         NSMonthCalendarUnit |
                         NSDayCalendarUnit |
                         NSHourCalendarUnit |
                         NSMinuteCalendarUnit |
                         NSSecondCalendarUnit |
                         NSWeekdayCalendarUnit |
                         NSWeekOfYearCalendarUnit;
  NSDateComponents *components = [calendar components:units fromDate:self];
  components.hour = hoursIn24;
  components.minute = minutes;
  components.second = seconds;
  return [calendar dateFromComponents:components];
}

- (NSDate*)pacoTimeFromMidnightWithMilliSeconds:(NSNumber*)milliSecondsNumber {
  long TOTAL_MILLISECONDS_IN_A_DAY = 24 * 60 * 60 * 1000;
  long milliseconds = [milliSecondsNumber longValue];
  NSAssert(milliseconds >= 0 && milliseconds < TOTAL_MILLISECONDS_IN_A_DAY,
           @"milliseconds should be a valid number!");
  
  NSDate* midnight = [self pacoCurrentDayAtMidnight];
  if (0 == milliseconds) {
    return midnight;
  }
  long seconds = milliseconds / 1000;
  long minutes = seconds / 60;
  long hrs = minutes / 60;
  minutes -= hrs * 60;
  seconds -= ((hrs * 60 + minutes) * 60);
  NSDate* time = [midnight pacoTimeWithIntervalOfHoursIn24:hrs minutes:minutes seconds:seconds];
  return time;
}

//Assume the array of times is already sorted!
- (NSDate*)pacoFirstAvailableTimeWithTimes:(NSArray*)times {
  NSAssert([times count] > 0, @"times should be valid!");
  
  NSDate* firstAvailableDate = nil;
  for (NSNumber* millisecondsNumber in times) {
    NSAssert([millisecondsNumber isKindOfClass:[NSNumber class]], @"time should be NSNumber!");
    firstAvailableDate = [self pacoTimeFromMidnightWithMilliSeconds:millisecondsNumber];
    if ([firstAvailableDate pacoNoEarlierThanDate:self]) {
      return firstAvailableDate;
    }
  }
  return nil;
}


//The array of times should be already sorted!
- (NSArray*)pacoDatesToScheduleWithTimes:(NSArray*)times andEndDate:(NSDate*)endDate{
  NSAssert([times count] > 0, @"times should be valid!");
  
  NSDate* dateToSchedule = nil;
  NSMutableArray* dates = [NSMutableArray arrayWithCapacity:[times count]];
  for (NSNumber* millisecondsNumber in times) {
    NSAssert([millisecondsNumber isKindOfClass:[NSNumber class]], @"time should be NSNumber!");
    
    dateToSchedule = [self pacoTimeFromMidnightWithMilliSeconds:millisecondsNumber];
    NSAssert(dateToSchedule, @"dateToSchedule should be valid");
    
    //if the dateToSchedule is later than or equal to endDate,
    //we should stop adding dates.
    if (endDate != nil && [dateToSchedule pacoNoEarlierThanDate:endDate]) {
      break;
    }
    
    if ([dateToSchedule pacoNoEarlierThanDate:self]) {
      [dates addObject:dateToSchedule];
    }
  }
  return dates;
}


- (BOOL)pacoCanScheduleTimes:(NSArray*)times {
  NSAssert([times count] > 0, @"times should be valid!");
  return [self pacoFirstAvailableTimeWithTimes:times] != nil;
}


- (BOOL)pacoOnSameDayWithDate:(NSDate*)anotherDate {
  NSDate* midnight = [self pacoCurrentDayAtMidnight];
  NSDate* anotherMidnight = [anotherDate pacoCurrentDayAtMidnight];
  return [midnight isEqualToDate:anotherMidnight];
}

static NSUInteger kSundayIndex = 1;
static NSUInteger kFridayIndex = 6;
static NSUInteger kSaturdayIndex = 7;
- (BOOL)pacoIsWeekend {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *components = [calendar components:NSWeekdayCalendarUnit fromDate:self];
  NSUInteger weekdayIndex = [components weekday];
  NSAssert(weekdayIndex >= kSundayIndex && weekdayIndex <= kSaturdayIndex,
           @"weekday index should be between 1 and 7");
  return weekdayIndex == kSundayIndex || weekdayIndex == kSaturdayIndex;
}

- (NSDate*)pacoFirstFutureNonWeekendDate {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *components = [calendar components:NSWeekdayCalendarUnit fromDate:self];
  NSUInteger weekdayIndex = [components weekday];
  NSUInteger intervalForFutureDay = 1; //next day
  if (weekdayIndex == kFridayIndex) { //next monday
    intervalForFutureDay = 3;
  } else if (weekdayIndex == kSaturdayIndex){ //next monday
    intervalForFutureDay = 2;
  }
  return [self pacoFutureDateAtMidnightWithInterval:intervalForFutureDay];
}

- (NSDate*)pacoDateByAddingMinutesInterval:(NSUInteger)offsetMinutes {
  NSTimeInterval interval = offsetMinutes * 60;
  return [self dateByAddingTimeInterval:interval];
}

@end
