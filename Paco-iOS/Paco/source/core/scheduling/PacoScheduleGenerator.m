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
#import "PacoScheduleGenerator.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperimentSchedule.h"
#import "PacoDateUtility.h"
#import "NSDate+Paco.h"
#import "NSCalendar+Paco.h"
#import "PacoScheduleGenerator+ESM.h"



@implementation PacoScheduleGenerator

+ (NSDate*)nextScheduledDateFromNow:(PacoExperiment *)experiment {
  return [self nextScheduledDateForExperiment:experiment fromThisDate:[NSDate dateWithTimeIntervalSinceNow:0]];
}


+ (NSArray*)nextDatesForExperiment:(PacoExperiment*)experiment
                        numOfDates:(NSInteger)numOfDates
                          fromDate:(NSDate*)fromDate {
  PacoExperimentSchedule* schedule = experiment.schedule;
  if (schedule.scheduleType == kPacoScheduleTypeDaily) {
    return [self nextDatesForDailyExperiment:experiment
                                  numOfDates:numOfDates
                                    fromDate:fromDate];
  }
  if (schedule.scheduleType == kPacoScheduleTypeESM) {
    return [self nextDatesForESMExperiment:experiment numOfDates:numOfDates fromDate:fromDate];
  }

  //TODO:
}

+ (NSArray*)nextDatesForDailyExperiment:(PacoExperiment*)experiment
                             numOfDates:(NSInteger)numOfDates
                               fromDate:(NSDate*)fromDate {
  NSAssert(numOfDates > 0, @"numOfDates should be valid!");
  
  PacoExperimentSchedule* schedule = experiment.schedule;
  NSAssert(schedule.scheduleType == kPacoScheduleTypeDaily, @"should be a daily experiment");
  
  //experiment already finished
  if (experiment.definition.endDate != nil &&
      [experiment.definition.endDate pacoNoLaterThanDate:fromDate]) {
    return nil;
  }
  
  NSMutableArray* dates = [NSMutableArray arrayWithCapacity:numOfDates];
  NSDate* startDate = [self getRealStartDateWithTimes:schedule.times
                                    originalStartDate:fromDate
                                  experimentStartDate:experiment.definition.startDate
                                           repeatRate:schedule.repeatRate];
  
  int numOfDatesNeeded = numOfDates;
  while (numOfDatesNeeded > 0) {
    NSAssert(startDate != nil, @"startDate should be valid!");
    NSArray* datesOnStartDate = [startDate pacoDatesToScheduleWithTimes:schedule.times
                                                             andEndDate:experiment.definition.endDate];
    int currentNumOfDates = [datesOnStartDate count];
    //if there are no valid dates to schedule, since we reach the experiment's endDate,
    //then we should just stop here.
    if (0 == currentNumOfDates) {
      break;
    }
    
    int numOfDatesToAdd = currentNumOfDates;
    if (currentNumOfDates > numOfDatesNeeded) {
      numOfDatesToAdd = numOfDatesNeeded;
    }
    NSIndexSet* indexSet = [NSIndexSet indexSetWithIndexesInRange:NSMakeRange(0, numOfDatesToAdd)];
    [dates addObjectsFromArray:[datesOnStartDate objectsAtIndexes:indexSet]];
    
    numOfDatesNeeded -= numOfDatesToAdd;
    startDate = [startDate pacoFutureDateAtMidnightWithInterval:schedule.repeatRate];
  }
  return dates;
}

//For daily experiment
+ (NSDate*)getRealStartDateWithTimes:(NSArray*)times
                   originalStartDate:(NSDate*)originalStartDate
                experimentStartDate:(NSDate*)experimentStartDate
                         repeatRate:(NSInteger)repeatRate {
  
  if (experimentStartDate == nil) { //ongoing experiment
    return [self getStartTimeFromDate:originalStartDate withTimes:times];
  } else { //fixe-length experiment
    return [self getStartTimeWithExperimentStartDate:experimentStartDate
                                            fromDate:originalStartDate
                                               times:times
                                          repeatRate:repeatRate];
  }
}


+ (NSDate*)getStartTimeFromDate:(NSDate*)fromDate withTimes:(NSArray*)times {
  //If it's still able to schedule any time from times, then return the current fromDate,
  //otherwise return the next day's midnight as the new fromDate
  if ([fromDate pacoCanScheduleTimes:times]) {
    return fromDate;
  } else {
    return [fromDate pacoNextDayAtMidnight];
  }
}


+ (NSDate*)getStartTimeWithExperimentStartDate:(NSDate*)experimentStartDate
                                      fromDate:(NSDate*)fromDate
                                         times:(NSArray*)times
                                    repeatRate:(NSInteger)repeatRate {
  //if user joins an experiment before experiment's start date,
  //then we should schedule dates from experiment's start date, not fromDate
  if ([fromDate pacoNoLaterThanDate:experimentStartDate]) {
    return experimentStartDate;
  }

  //the user joins an experiment after the experiment starts
  int numOfDaysFromStartDate =
      [[NSCalendar pacoGregorianCalendar] pacoDaysFromDate:experimentStartDate toDate:fromDate];
  
  NSAssert(repeatRate >= 1, @"repeatRate should be valid");
  int repeatTimes = numOfDaysFromStartDate / repeatRate;
  int extraDays = numOfDaysFromStartDate % repeatRate;
  if (extraDays != 0) {
    repeatTimes++;
  }
  
  int daysToNewStartDate = repeatTimes * repeatRate;
  NSAssert(numOfDaysFromStartDate <= daysToNewStartDate,
           @"new start date should either on the same day with fromDate or later than fromDate");
  
  NSDate* newStartDate = nil;
  if (numOfDaysFromStartDate < daysToNewStartDate) { //newStartDate is later than fromDate
    NSAssert(daysToNewStartDate > 0, @"daysToNewStartDate should be larger than 0");
    newStartDate = [experimentStartDate pacoFutureDateAtMidnightWithInterval:daysToNewStartDate];
  } else { //fromDate and newStartDate are on the same day
    if ([fromDate pacoCanScheduleTimes:times]) {
      newStartDate = fromDate;
    } else {
      repeatTimes++;
      daysToNewStartDate = repeatTimes * repeatRate;
      newStartDate = [experimentStartDate pacoFutureDateAtMidnightWithInterval:daysToNewStartDate];
    }
  }
  NSAssert(newStartDate, @"newStartDate should be valid!");
  return newStartDate;
}


+ (NSDate*)nextDateForDailyExperiment:(PacoExperiment*)experiment fromThisDate:(NSDate*)fromThisDate {
  // Today 12:30pm -> Today 1:30pm
  NSDate *repeatTime = [PacoDateUtility nextTimeFromScheduledTimes:experiment.schedule.times
                                                       onDayOfDate:fromThisDate];
  if (repeatTime) {
    // return Today 1:30pm
    return repeatTime;
  } else {
    // Today 12:30pm -> NextDay 9:00am
    NSDate *repeatDay = [PacoDateUtility date:fromThisDate
                             thisManyDaysFrom:experiment.schedule.repeatRate];
    repeatDay = [PacoDateUtility midnightThisDate:repeatDay];
    return [PacoDateUtility nextTimeFromScheduledTimes:experiment.schedule.times onDayOfDate:repeatDay];
  }
}

+ (NSDate*)nextDateForWeeklyExperiment:(PacoExperiment*)experiment fromThisDate:(NSDate*)fromThisDate {
  NSDate *thisWeek = [PacoDateUtility nextScheduledDay:experiment.schedule.weekDaysScheduled fromDate:fromThisDate];
  if (thisWeek) {
    return thisWeek;
  }
  NSDate *repeatWeek = [PacoDateUtility date:fromThisDate thisManyWeeksFrom:experiment.schedule.repeatRate];
  repeatWeek = [PacoDateUtility dateSameWeekAs:repeatWeek dayIndex:0 hr24:0 min:0];
  NSDate *scheduledDay = [PacoDateUtility nextScheduledDay:experiment.schedule.weekDaysScheduled fromDate:repeatWeek];
  assert(scheduledDay);
  NSDate *nextTime = [PacoDateUtility nextTimeFromScheduledTimes:experiment.schedule.times onDayOfDate:scheduledDay];
  return nextTime;
}

+ (NSDate*)nextDateForWeekdayExperiment:(PacoExperiment*)experiment fromThisDate:(NSDate*)fromThisDate {
  unsigned int weekdayFlags = (1<<1) | (1<<2) | (1<<3) | (1<<4) | (1<<5);
  NSDate *thisWeek = [PacoDateUtility nextScheduledDay:weekdayFlags fromDate:fromThisDate];
  if (thisWeek) {
    return thisWeek;
  }
  NSDate *repeatWeek = [PacoDateUtility date:fromThisDate thisManyWeeksFrom:experiment.schedule.repeatRate];
  repeatWeek = [PacoDateUtility dateSameWeekAs:repeatWeek dayIndex:0 hr24:0 min:0];
  NSDate *scheduledDay = [PacoDateUtility nextScheduledDay:weekdayFlags fromDate:repeatWeek];
  assert(scheduledDay);
  NSDate *nextTime = [PacoDateUtility nextTimeFromScheduledTimes:experiment.schedule.times onDayOfDate:scheduledDay];
  return nextTime;
}

+ (NSDate*)nextDateForMonthlyExperiment:(PacoExperiment*)experiment fromThisDate:(NSDate*)fromThisDate {
  NSDate *scheduledMonth = [PacoDateUtility date:fromThisDate thisManyMonthsFrom:experiment.schedule.repeatRate];
  if (experiment.schedule.byDayOfWeek) {
    scheduledMonth = [PacoDateUtility dateOnNthOfMonth:scheduledMonth nth:experiment.schedule.dayOfMonth dayFlags:experiment.schedule.weekDaysScheduled];
  } else {
    scheduledMonth = [PacoDateUtility dateSameMonthAs:scheduledMonth dayIndex:experiment.schedule.dayOfMonth];
  }
  return scheduledMonth;
}

+ (NSDate *)nextScheduledDateForExperiment:(PacoExperiment *)experiment
                              fromThisDate:(NSDate *)fromThisDate {
  switch (experiment.schedule.scheduleType) {
    case kPacoScheduleTypeDaily:
      return [self nextDateForDailyExperiment:experiment fromThisDate:fromThisDate];

    case kPacoScheduleTypeWeekly:
      return [self nextDateForWeeklyExperiment:experiment fromThisDate:fromThisDate];
    
    case kPacoScheduleTypeWeekday:
      return [self nextDateForWeekdayExperiment:experiment fromThisDate:fromThisDate];
    
    case kPacoScheduleTypeMonthly:
      return [self nextDateForMonthlyExperiment:experiment fromThisDate:fromThisDate];
    
    case kPacoScheduleTypeESM: {
      NSDate *scheduled = [self nextESMScheduledDateForExperiment:experiment fromThisDate:fromThisDate];
      NSAssert(scheduled != nil, @"ESM schedule should not be nil!");
      return scheduled;
    }
    case kPacoScheduleTypeSelfReport:
    case kPacoScheduleTypeTesting:
    default:
      return nil;
  }
}




@end
