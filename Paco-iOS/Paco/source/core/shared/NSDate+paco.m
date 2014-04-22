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
#import "NSCalendar+Paco.h"

static NSUInteger kSundayIndex = 1;
static NSUInteger kFridayIndex = 6;
static NSUInteger kSaturdayIndex = 7;


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


- (NSDate*)pacoNextDayAtMidnight {
  return [self pacoDateAtMidnightByAddingDayInterval:1];
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
  return [self pacoDatesToScheduleWithTimes:times generateTime:self andEndDate:endDate];
}


- (NSArray*)pacoDatesToScheduleWithTimes:(NSArray*)times
                            generateTime:(NSDate*)generateTime
                              andEndDate:(NSDate*)endDate {
  NSAssert([times count] > 0, @"times should be valid!");
  
  NSDate* dateToSchedule = nil;
  NSMutableArray* dates = [NSMutableArray arrayWithCapacity:[times count]];
  for (NSNumber* millisecondsNumber in times) {
    NSAssert([millisecondsNumber isKindOfClass:[NSNumber class]], @"time should be NSNumber!");
    
    dateToSchedule = [self pacoTimeFromMidnightWithMilliSeconds:millisecondsNumber];
    NSAssert(dateToSchedule, @"dateToSchedule should be valid");
    
    //if the dateToSchedule is later than or equal to endDate,
    //we should stop adding dates.
    if (endDate && [dateToSchedule pacoNoEarlierThanDate:endDate]) {
      break;
    }
    
    if ([dateToSchedule pacoLaterThanDate:generateTime]) {
      [dates addObject:dateToSchedule];
    }
  }
  return [dates count] > 0 ? dates : nil;
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

- (NSDate*)pacoFirstDayInCurrentMonth {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSCalendarUnit units =  NSYearCalendarUnit |
                          NSMonthCalendarUnit |
                          NSDayCalendarUnit |
                          NSHourCalendarUnit |
                          NSMinuteCalendarUnit |
                          NSSecondCalendarUnit |
                          NSWeekdayCalendarUnit |
                          NSWeekOfYearCalendarUnit;
  NSDateComponents *components = [calendar components:units fromDate:self];
  components.day = 1;
  components.hour = 0;
  components.minute = 0;
  components.second = 0;
  return [calendar dateFromComponents:components];
}


- (NSDate*)pacoDayInCurrentMonth:(NSUInteger)dayIndex {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSCalendarUnit units =  NSYearCalendarUnit |
                          NSMonthCalendarUnit |
                          NSDayCalendarUnit |
                          NSHourCalendarUnit |
                          NSMinuteCalendarUnit |
                          NSSecondCalendarUnit |
                          NSWeekdayCalendarUnit |
                          NSWeekOfYearCalendarUnit;
  NSDateComponents *components = [calendar components:units fromDate:self];
  components.day = dayIndex;
  components.hour = 0;
  components.minute = 0;
  components.second = 0;
  NSDate* result = [calendar dateFromComponents:components];
  
  NSDateComponents* compResult = [calendar components:units fromDate:result];
  NSDateComponents* compCurrentDate = [calendar components:units fromDate:self];
  return compResult.month == compCurrentDate.month ? result : nil;
}

- (NSDate*)pacoSundayInCurrentWeek {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *components = [calendar components:NSWeekdayCalendarUnit fromDate:self];
  NSInteger weekdayIndex = [components weekday];
  NSInteger dayOffsetToSunday = weekdayIndex - kSundayIndex;
  NSDate* currentMidnight = [self pacoCurrentDayAtMidnight];
  return [currentMidnight pacoDateByAddingDayInterval:-dayOffsetToSunday];
}

- (NSDate*)pacoCycleStartDateOfMonthWithOriginalStartDate:(NSDate*)originalStartDate {
  NSAssert([originalStartDate pacoNoLaterThanDate:self], @"should be no later than self");
  BOOL finished = NO;
  NSDate* startDateInCurrentMonth = originalStartDate;
  NSDate* startDateInNextMonth = [startDateInCurrentMonth pacoDateByAddingMonthInterval:1];
  while (!finished) {
    if ([startDateInNextMonth pacoLaterThanDate:self]) {
      finished = YES;
    } else {
      startDateInCurrentMonth = startDateInNextMonth;
      startDateInNextMonth = [startDateInNextMonth pacoDateByAddingMonthInterval:1];
    }
  }
  return startDateInCurrentMonth;
}


- (BOOL)pacoInSameMonthWith:(NSDate*)another {
  return [[self pacoFirstDayInCurrentMonth] isEqualToDate:[another pacoFirstDayInCurrentMonth]];
}


- (BOOL)pacoIsWeekend {
  NSUInteger weekdayIndex = [self pacoIndexInWeek];
  return weekdayIndex == kSundayIndex || weekdayIndex == kSaturdayIndex;
}

- (int)pacoIndexInWeek {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *components = [calendar components:NSWeekdayCalendarUnit fromDate:self];
  int weekdayIndex = (int)[components weekday];
  NSAssert(weekdayIndex >= kSundayIndex && weekdayIndex <= kSaturdayIndex,
           @"weekday index should be between 1 and 7");
  return weekdayIndex;
}

- (NSDate*)pacoNearestNonWeekendDateAtMidnight {
  return [[self pacoNearestNonWeekendDate] pacoCurrentDayAtMidnight];
}

- (NSDate*)pacoNearestNonWeekendDate {
  NSCalendar *calendar = [NSCalendar currentCalendar];
  NSDateComponents *components = [calendar components:NSWeekdayCalendarUnit fromDate:self];
  NSUInteger weekdayIndex = [components weekday];
  NSUInteger intervalForFutureDay = 1; //next day
  if (weekdayIndex == kFridayIndex) { //next monday
    intervalForFutureDay = 3;
  } else if (weekdayIndex == kSaturdayIndex){ //next monday
    intervalForFutureDay = 2;
  }
  return [self pacoDateByAddingDayInterval:intervalForFutureDay];
}

//intervalDays should be larger than or equal to 0
- (NSDate*)pacoDateAtMidnightByAddingDayInterval:(NSInteger)intervalDays {
  NSAssert(intervalDays >= 0, @"intervalDays should be larger than or equal to 0");
  NSDate* futureDay = [self pacoDateByAddingDayInterval:intervalDays];
  return [futureDay pacoCurrentDayAtMidnight];
}

- (NSDate*)pacoDateByAddingDayInterval:(NSInteger)intervalDays {
  if (intervalDays == 0) {
    return self;
  }
  
  NSCalendar* calendar = [NSCalendar currentCalendar];
  NSDateComponents* dayComponents = [[NSDateComponents alloc] init];
  dayComponents.day = intervalDays;
  return [calendar dateByAddingComponents:dayComponents toDate:self options:0];
}

- (NSDate*)pacoDateByAddingWeekInterval:(NSUInteger)weekInterval {
  if (weekInterval == 0) {
    return self;
  }
  NSCalendar* calendar = [NSCalendar currentCalendar];
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  comp.week = weekInterval;
  return [calendar dateByAddingComponents:comp toDate:self options:0];
}

- (NSDate*)pacoDateByAddingMonthInterval:(NSUInteger)monthInterval {
  NSCalendar* calendar = [NSCalendar currentCalendar];
  NSDateComponents* comp = [[NSDateComponents alloc] init];
  comp.month = monthInterval;
  return [calendar dateByAddingComponents:comp toDate:self options:0];
}

- (NSDate*)pacoDateByAddingMinutesInterval:(NSUInteger)offsetMinutes {
  NSTimeInterval interval = offsetMinutes * 60;
  return [self dateByAddingTimeInterval:interval];
}


- (NSDate*)pacoDailyESMNextCycleStartDate:(BOOL)includeWeekends {
  NSDate* nextCycleStartDate = [self pacoNextDayAtMidnight];
  if (!includeWeekends && [nextCycleStartDate pacoIsWeekend]) {
    nextCycleStartDate = [self pacoNearestNonWeekendDateAtMidnight];
  }
  return nextCycleStartDate;
}

- (NSDate*)pacoWeeklyESMNextCycleStartDate {
  NSDate* sameDayNextWeek = [self pacoDateAtMidnightByAddingDayInterval:7];
  return sameDayNextWeek;
}

- (NSDate*)pacoMonthlyESMNextCycleStartDate {
  NSDate* sameDayNextMonth = [self pacoDateByAddingMonthInterval:1];
  sameDayNextMonth = [sameDayNextMonth pacoCurrentDayAtMidnight];
  return sameDayNextMonth;
}

- (int)pacoNumOfDaysInCurrentMonth {
  NSDate* sameDayNextMonth = [self pacoDateByAddingMonthInterval:1];
  int numOfDays = (int)[[NSCalendar pacoGregorianCalendar] pacoDaysFromDate:self toDate:sameDayNextMonth];
  NSAssert(numOfDays >= 28 && numOfDays <= 31, @"numOfDays should be valid");
  return numOfDays;
}

- (int)pacoNumOfWeekdaysInCurrentMonth {
  NSDate* startDate = [self pacoCurrentDayAtMidnight];
  NSDate* sameDayNextMonth = [startDate pacoDateByAddingMonthInterval:1];
  int numOfDays = (int)[[NSCalendar pacoGregorianCalendar] pacoDaysFromDate:startDate
                                                                toDate:sameDayNextMonth];
  NSAssert(numOfDays >= 28 && numOfDays <= 31, @"numOfDays should be valid");
  NSDate* date = nil;
  int count = 0;
  for(int dayIndex = 0; dayIndex < numOfDays; dayIndex++) {
    date = [startDate pacoDateByAddingDayInterval:dayIndex];
    if (![date pacoIsWeekend]) {
      count++;
    }
  }
  return count;
}


@end
