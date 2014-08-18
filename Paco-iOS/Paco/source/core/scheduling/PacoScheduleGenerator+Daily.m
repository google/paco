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
#import "PacoExperimentSchedule.h"
#import "NSDate+Paco.h"
#import "NSCalendar+Paco.h"
#import "PacoScheduleGenerator+Daily.h"

@implementation PacoScheduleGenerator (Daily)

+ (NSArray*)nextDatesForDailyExperiment:(PacoExperiment*)experiment
                             numOfDates:(int)numOfDates
                               fromDate:(NSDate*)fromDate {
  //adjust fromDate to experiment start date if fromDate is earlier than experiment start date
  //adjustedGenerateTime is used to calculate the first day that can schedule times
  NSDate* adjustedGenerateTime = [self adjustedGenerateTime:fromDate forExperiment:experiment];
  NSDate* dayToScheduleAtMidnight = [self firstMidnightAbleToSchedule:experiment
                                                         generateTime:adjustedGenerateTime];
  
  NSMutableArray* results = [NSMutableArray arrayWithCapacity:numOfDates];
  PacoExperimentSchedule* schedule = experiment.schedule;
  int numOfDatesNeeded = numOfDates;
  while (numOfDatesNeeded > 0) {
    NSAssert(dayToScheduleAtMidnight != nil, @"dayToScheduleAtMidnight should be valid!");
    NSArray* dates = [dayToScheduleAtMidnight pacoDatesToScheduleWithTimes:schedule.times
                                                              generateTime:fromDate
                                                                andEndDate:[experiment endDate]];
    NSUInteger currentNumOfDates = [dates count];
    //if there are no valid dates to schedule, since we reach the experiment's endDate,
    //then we should just stop here.
    if (0 == currentNumOfDates) {
      break;
    }
    
    NSUInteger numOfDatesToAdd = (currentNumOfDates > numOfDatesNeeded) ? numOfDatesNeeded : currentNumOfDates;
    NSIndexSet* indexSet = [NSIndexSet indexSetWithIndexesInRange:NSMakeRange(0, numOfDatesToAdd)];
    [results addObjectsFromArray:[dates objectsAtIndexes:indexSet]];
    
    numOfDatesNeeded -= numOfDatesToAdd;
    dayToScheduleAtMidnight = [dayToScheduleAtMidnight pacoDateAtMidnightByAddingDayInterval:schedule.repeatRate];
  }
  return [results count] > 0 ? results : nil;
}

//generateTime: either the time that the major task is executed, or the experiment start date
+ (NSDate*)firstMidnightAbleToSchedule:(PacoExperiment*)experiment generateTime:(NSDate*)generateTime {
  NSDate* startDateAtMidnight = [experiment isFixedLength] ? [experiment startDate] : [experiment joinDate];
  NSInteger numOfDaysUntilGenerateTime =
      [[NSCalendar pacoGregorianCalendar] pacoDaysFromDate:startDateAtMidnight toDate:generateTime];
  NSInteger repeatRate = experiment.schedule.repeatRate;
  NSInteger repeatTimes = numOfDaysUntilGenerateTime / repeatRate;
  NSInteger extraDays = numOfDaysUntilGenerateTime % repeatRate;
  BOOL isCurrentDayActive = (0 == extraDays);
  if (isCurrentDayActive &&
      [generateTime pacoCanScheduleTimes:experiment.schedule.times]) {
    return [generateTime pacoCurrentDayAtMidnight];
  }
  
  repeatTimes++;
  NSInteger days = repeatTimes * repeatRate;
  NSDate* firstDayToSchedule = [startDateAtMidnight pacoDateAtMidnightByAddingDayInterval:days];
  return firstDayToSchedule;
}


@end
