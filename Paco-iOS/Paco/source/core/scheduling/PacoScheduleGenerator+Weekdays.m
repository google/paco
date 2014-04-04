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
#import "PacoDateUtility.h"
#import "NSDate+Paco.h"
#import "PacoScheduleGenerator+Weekdays.h"

@implementation PacoScheduleGenerator (Weekdays)


+ (NSArray*)nextDatesForWeekdaysExperiment:(PacoExperiment*)experiment
                                numOfDates:(int)numOfDates
                                  fromDate:(NSDate*)fromDate {
  NSAssert(numOfDates > 0, @"numOfDates should be valid!");
  
  PacoExperimentSchedule* schedule = experiment.schedule;
  NSAssert(schedule.scheduleType == kPacoScheduleTypeWeekday, @"should be a weekday experiment");
  
  
  NSMutableArray* results = [NSMutableArray arrayWithCapacity:numOfDates];
  NSDate* startTime = [self getStartTimeToScheduleTimes:schedule.times
                                           generateTime:fromDate
                                    experimentStartDate:[experiment startDate]
                                     experimentJoinDate:[experiment joinDate]];
  
  int numOfDatesNeeded = numOfDates;
  while (numOfDatesNeeded > 0) {
    NSAssert(startTime != nil, @"startDate should be valid!");
    NSArray* datesOnStartDate = [startTime pacoDatesToScheduleWithTimes:schedule.times
                                                             andEndDate:[experiment endDate]];
    int currentNumOfDates = (int)[datesOnStartDate count];
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
    [results addObjectsFromArray:[datesOnStartDate objectsAtIndexes:indexSet]];
    
    numOfDatesNeeded -= numOfDatesToAdd;
    startTime = [startTime pacoNearestNonWeekendDateAtMidnight];
  }
  return results;
}


/*
 generateTime: the time that Paco needs to schedule more notifications for this experiment
 This method returns a start time that all schedules will be generated after
 **/
+ (NSDate*)getStartTimeToScheduleTimes:(NSArray*)times
                          generateTime:(NSDate*)generateTime
                   experimentStartDate:(NSDate*)experimentStartDate
                    experimentJoinDate:(NSDate*)experimentJoinDate {
  //startDateForAllSchedules: the midnight time of experiment's start date(fixed-length) or join date(on-going)
  NSDate* startDateForAllSchedules = experimentStartDate;
  if (!startDateForAllSchedules) { //ongoing experiment
    startDateForAllSchedules = experimentJoinDate;
  }
  /*
   if generateTime is earlier than or equal to the startDateForAllSchedules - the midnight of experiment's start
   date(fixed-length) or join date(on-going), we should schedule dates from startDateForAllSchedules;
   
   if generateTime is on or after the experiment start day(fixed-length) or join day(on-going),
   we should schedule dates from generateTime
   **/
  NSDate* newStartTime = nil;
  if ([generateTime pacoNoLaterThanDate:startDateForAllSchedules]) {
    newStartTime = startDateForAllSchedules;
  } else {
    newStartTime = generateTime;
  }
  
  if ([newStartTime pacoCanScheduleTimes:times] && ![newStartTime pacoIsWeekend]) {
    return newStartTime;
  } else {
    return [newStartTime pacoNearestNonWeekendDateAtMidnight];
  }
}



@end
