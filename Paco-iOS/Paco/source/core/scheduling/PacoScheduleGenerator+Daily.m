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

#import "PacoScheduleGenerator+Daily.h"

@implementation PacoScheduleGenerator (Daily)



+ (NSArray*)nextDatesForDailyExperiment:(PacoExperiment*)experiment
                             numOfDates:(int)numOfDates
                               fromDate:(NSDate*)fromDate {
  NSAssert(numOfDates > 0, @"numOfDates should be valid!");
  
  PacoExperimentSchedule* schedule = experiment.schedule;
  NSAssert(schedule.scheduleType == kPacoScheduleTypeDaily, @"should be a daily experiment");
  
  NSMutableArray* dates = [NSMutableArray arrayWithCapacity:numOfDates];
  NSDate* startDate = [self getRealStartDateWithTimes:schedule.times
                                    originalStartDate:fromDate
                                  experimentStartDate:experiment.definition.startDate
                                   experimentJoinDate:[experiment joinDate]
                                           repeatRate:schedule.repeatRate];
  
  int numOfDatesNeeded = numOfDates;
  while (numOfDatesNeeded > 0) {
    NSAssert(startDate != nil, @"startDate should be valid!");
    NSArray* datesOnStartDate = [startDate pacoDatesToScheduleWithTimes:schedule.times
                                                             andEndDate:experiment.definition.endDate];
    NSUInteger currentNumOfDates = [datesOnStartDate count];
    //if there are no valid dates to schedule, since we reach the experiment's endDate,
    //then we should just stop here.
    if (0 == currentNumOfDates) {
      break;
    }
    
    NSUInteger numOfDatesToAdd = currentNumOfDates;
    if (currentNumOfDates > numOfDatesNeeded) {
      numOfDatesToAdd = numOfDatesNeeded;
    }
    NSIndexSet* indexSet = [NSIndexSet indexSetWithIndexesInRange:NSMakeRange(0, numOfDatesToAdd)];
    [dates addObjectsFromArray:[datesOnStartDate objectsAtIndexes:indexSet]];
    
    numOfDatesNeeded -= numOfDatesToAdd;
    startDate = [startDate pacoDateAtMidnightByAddingDayInterval:schedule.repeatRate];
  }
  return dates;
}

//For daily experiment
+ (NSDate*)getRealStartDateWithTimes:(NSArray*)times
                   originalStartDate:(NSDate*)originalStartDate
                 experimentStartDate:(NSDate*)experimentStartDate
                  experimentJoinDate:(NSDate*)experimentJoinDate
                          repeatRate:(NSInteger)repeatRate {
  NSDate* startDateForAllSchedules = experimentStartDate;
  if (!startDateForAllSchedules) { //ongoing experiment
    startDateForAllSchedules = experimentJoinDate;
  }
  return [self getStartTimeWithStartDate:startDateForAllSchedules
                                fromDate:originalStartDate
                                   times:times
                              repeatRate:repeatRate];
}


+ (NSDate*)getStartTimeWithStartDate:(NSDate*)startDateForAllSchedules
                            fromDate:(NSDate*)fromDate
                               times:(NSArray*)times
                          repeatRate:(NSInteger)repeatRate {
  //if user joins an experiment before experiment's start date,
  //then we should schedule dates from experiment's start date, not fromDate
  if ([fromDate pacoNoLaterThanDate:startDateForAllSchedules]) {
    return startDateForAllSchedules;
  }
  
  //the user joins an experiment after the experiment starts
  NSInteger numOfDaysFromStartDate =
  [[NSCalendar pacoGregorianCalendar] pacoDaysFromDate:startDateForAllSchedules toDate:fromDate];
  
  NSAssert(repeatRate >= 1, @"repeatRate should be valid");
  NSInteger repeatTimes = numOfDaysFromStartDate / repeatRate;
  NSInteger extraDays = numOfDaysFromStartDate % repeatRate;
  if (extraDays != 0) {
    repeatTimes++;
  }
  
  NSInteger daysToNewStartDate = repeatTimes * repeatRate;
  NSAssert(numOfDaysFromStartDate <= daysToNewStartDate,
           @"new start date should either on the same day with fromDate or later than fromDate");
  
  NSDate* newStartDate = nil;
  if (numOfDaysFromStartDate < daysToNewStartDate) { //newStartDate is later than fromDate
    NSAssert(daysToNewStartDate > 0, @"daysToNewStartDate should be larger than 0");
    newStartDate = [startDateForAllSchedules pacoDateAtMidnightByAddingDayInterval:daysToNewStartDate];
  } else { //fromDate and newStartDate are on the same day
    if ([fromDate pacoCanScheduleTimes:times]) {
      newStartDate = fromDate;
    } else {
      repeatTimes++;
      daysToNewStartDate = repeatTimes * repeatRate;
      newStartDate = [startDateForAllSchedules pacoDateAtMidnightByAddingDayInterval:daysToNewStartDate];
    }
  }
  NSAssert(newStartDate, @"newStartDate should be valid!");
  return newStartDate;
}


@end
