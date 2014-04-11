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
#import "PacoScheduleGenerator+ESM.h"
#import "PacoScheduleGenerator+Daily.h"
#import "PacoScheduleGenerator+Weekdays.h"
#import "PacoScheduleGenerator+Weekly.h"
#import "PacoScheduleGenerator+MonthlyByDayOfMonth.h"
#import "PacoScheduleGenerator+MonthlyByDayOfWeek.h"
#import "PacoDateUtility.h"
#import "NSDate+Paco.h"
#import "NSCalendar+Paco.h"

@implementation PacoScheduleGenerator


+ (NSArray*)nextDatesForExperiment:(PacoExperiment*)experiment
                        numOfDates:(int)numOfDates
                          fromDate:(NSDate*)fromDate {
  if (numOfDates <= 0 || !fromDate) {
    return nil;
  }
  
  //experiment is a self-report or trigger experiment
  //experiment is fixed-length and already finished
  if (![experiment shouldScheduleNotificationsFromDate:fromDate]) {
    return nil;
  }
  
  PacoExperimentSchedule* schedule = experiment.schedule;
  if (schedule.scheduleType == kPacoScheduleTypeDaily) {
    return [self nextDatesForDailyExperiment:experiment numOfDates:numOfDates fromDate:fromDate];
  }
  if (schedule.scheduleType == kPacoScheduleTypeESM) {
    return [self nextDatesForESMExperiment:experiment numOfDates:numOfDates fromDate:fromDate];
  }
  if (schedule.scheduleType == kPacoScheduleTypeWeekday) {
    return [self nextDatesForWeekdaysExperiment:experiment numOfDates:numOfDates fromDate:fromDate];
  }
  if (schedule.scheduleType == kPacoScheduleTypeWeekly) {
    return [self nextDatesForWeeklyExperiment:experiment numOfDates:numOfDates fromDate:fromDate];
  }
  
  if (schedule.scheduleType == kPacoScheduleTypeMonthly) {
    return [self nextDatesForMonthlyExperiment:experiment numOfDates:numOfDates fromDate:fromDate];
  }
  NSAssert(NO, @"schedule type should be daily, esm, weekday, weekly, or monthly");
  return nil;
}


+ (NSArray*)nextDatesForMonthlyExperiment:(PacoExperiment*)experiment
                               numOfDates:(int)numOfDates
                                 fromDate:(NSDate*)fromDate {
  if (experiment.schedule.byDayOfMonth) {
    return [self nextDatesByDayOfMonthForExperiment:experiment numOfDates:numOfDates fromDate:fromDate];
  }
  if (experiment.schedule.byDayOfWeek) {
    return [self nextDatesByDayOfWeekForExperiment:experiment numOfDates:numOfDates fromDate:fromDate];
  }
  NSAssert(NO, @"monthly schedule should be either by day of month of by day of week");
  return nil;
}


//adjust the generate time if the experiment is fixed-length and the original generate time is
//earlier than the experiment start date
+ (NSDate*)adjustedGenerateTime:(NSDate*)originalGenerateTime forExperiment:(PacoExperiment*)experiment {
    NSAssert([experiment isFixedLength] ||
             ([experiment isOngoing] && [originalGenerateTime pacoNoEarlierThanDate:experiment.joinTime]),
             @"for an ongoing experiment, should always generate schedules after the user joined it");
  
  //fixed-length experiment, and user joined before or when experiment starts
  if ([experiment isFixedLength] && [originalGenerateTime pacoNoLaterThanDate:[experiment startDate]]) {
    return [experiment startDate];
  } else {
    return originalGenerateTime;
  }
}


@end
