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

#import "PacoScheduleGenerator+Weekly.h"
#import "PacoScheduleGenerator.h"
#import "PacoExperiment.h"
#import "PacoExperimentSchedule.h"
#import "PacoDateUtility.h"
#import "NSDate+Paco.h"
#import "NSCalendar+Paco.h"

@implementation PacoScheduleGenerator (Weekly)

/*
 generateTime: the time that Paco needs to schedule more notifications for this experiment
 **/
+ (NSArray*)nextDatesForWeeklyExperiment:(PacoExperiment*)experiment
                              numOfDates:(int)numOfDates
                                fromDate:(NSDate*)fromDate {
  //handle the corner case that user didn't select any day
  if (![experiment.schedule weeklyConfigureTable]) {
    return nil;
  }
  
  NSDate* adjustedGenerateTime = [self adjustedGenerateTime:fromDate forExperiment:experiment];
  
  //find the first sunday that can schedule notifications in that week
  //according to repeate rate and other constraints configured in schedule
  NSDate* sundayMidnight = [self sundayMidnightToScheduleForExperiment:experiment
                                                          generateTime:adjustedGenerateTime];
  
  int numOfDatesNeeded = numOfDates;
  NSMutableArray* results = [NSMutableArray arrayWithCapacity:numOfDates];
  while (numOfDatesNeeded > 0) {
    NSArray* dates = [self datesFromSunday:sundayMidnight
                             forExperiment:experiment
                              generateTime:adjustedGenerateTime];
    int numOfDates = (int)[dates count];
    if (0 == numOfDates) {
      break;
    }
    int numOfDatesToAdd = (numOfDates > numOfDatesNeeded) ? numOfDatesNeeded : numOfDates;
    NSIndexSet* indexSet = [NSIndexSet indexSetWithIndexesInRange:NSMakeRange(0, numOfDatesToAdd)];
    [results addObjectsFromArray:[dates objectsAtIndexes:indexSet]];
    
    numOfDatesNeeded -= numOfDatesToAdd;
    sundayMidnight = [sundayMidnight pacoDateByAddingWeekInterval:experiment.schedule.repeatRate];
  }
  return ([results count] > 0) ? results : nil;
}




//return the first sunday midnight that is able to schedule notifications
+ (NSDate*)sundayMidnightToScheduleForExperiment:(PacoExperiment*)experiment
                                    generateTime:(NSDate*)generateTime {
  NSDate* exeprimentStartDateMidnight = [experiment isFixedLength] ? [experiment startDate] : [experiment joinDate];
  NSInteger numOfWeeksUntilGenerateTime = [[NSCalendar pacoGregorianCalendar] pacoWeeksFromDate:exeprimentStartDateMidnight
                                                                                  toDate:generateTime];
  NSInteger repeatRate = experiment.schedule.repeatRate;
  NSInteger repeatTimes = numOfWeeksUntilGenerateTime / repeatRate;
  NSInteger extraWeeks = numOfWeeksUntilGenerateTime % repeatRate;
  
  BOOL isCurrentWeekActive = (0 == extraWeeks);
  if (isCurrentWeekActive &&
      [self isExperiment:experiment ableToGenerateSince:generateTime]) {
    return [generateTime pacoSundayInCurrentWeek];
  }
  
  repeatTimes++;
  NSInteger weeks = repeatTimes * repeatRate;
  NSDate* sunday = [[exeprimentStartDateMidnight pacoSundayInCurrentWeek] pacoDateByAddingWeekInterval:weeks];
  if ([experiment isFixedLength] && [sunday pacoNoEarlierThanDate:[experiment endDate]]) {
    sunday = nil;
  }
  return sunday;
}


+ (BOOL)isExperiment:(PacoExperiment*)experiment ableToGenerateSince:(NSDate*)generateTime {
  NSDate* sundayInCurrentWeek = [generateTime pacoSundayInCurrentWeek];
  NSArray* dates = [self datesFromSunday:sundayInCurrentWeek
                          forExperiment:experiment
                           generateTime:generateTime];
  return ([dates count] > 0);
}

+ (NSArray*)datesFromSunday:(NSDate*)sundayMidnight
              forExperiment:(PacoExperiment*)experiment
               generateTime:(NSDate*)generateTime {
  if (!sundayMidnight) {
    return nil;
  }
  
  NSArray* weeklyConfigureTable = [experiment.schedule weeklyConfigureTable];
  NSMutableArray* results = [NSMutableArray arrayWithCapacity:7 * [experiment.schedule.times count]];
  for (int dayIndex = 0; dayIndex < kPacoNumOfDaysInWeek; dayIndex++) {
    BOOL daySelected = [weeklyConfigureTable[dayIndex] boolValue];
    if (daySelected) {
      NSDate* midnight = [sundayMidnight pacoDateByAddingDayInterval:dayIndex];
      NSArray* dates = [midnight pacoDatesToScheduleWithTimes:experiment.schedule.times
                                                 generateTime:generateTime
                                                   andEndDate:[experiment endDate]];
      [results addObjectsFromArray:dates];
    }
  }
  return [results count] > 0 ? results : nil;
}


@end
