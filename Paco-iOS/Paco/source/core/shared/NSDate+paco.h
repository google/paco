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


#import <Foundation/Foundation.h>
#import "PacoExperimentSchedule.h"



@interface NSDate (Paco)

- (BOOL)pacoEarlierThanDate:(NSDate*)another;
- (BOOL)pacoLaterThanDate:(NSDate*)another;
- (BOOL)pacoEqualToDate:(NSDate*)another;

- (BOOL)pacoNoEarlierThanDate:(NSDate*)another;
- (BOOL)pacoNoLaterThanDate:(NSDate*)another;

- (NSDate*)pacoCurrentDayAtMidnight;
- (NSDate*)pacoNextDayAtMidnight;

//intervalDays should be larger than or equal to 0
- (NSDate*)pacoDateAtMidnightByAddingDayInterval:(NSInteger)intervalDays;

//The array of times should be already sorted!
- (NSArray*)pacoDatesToScheduleWithTimes:(NSArray*)times andEndDate:(NSDate*)endDate;

- (NSArray*)pacoDatesToScheduleWithTimes:(NSArray*)times
                            generateTime:(NSDate*)generateTime
                              andEndDate:(NSDate*)endDate;

- (BOOL)pacoCanScheduleTimes:(NSArray*)times;

- (BOOL)pacoOnSameDayWithDate:(NSDate*)anotherDate;

- (NSDate*)pacoFirstDayInCurrentMonth;

//dayIndex starts from 1, and ends up to 31
//if dayIndex exceeds the max days in the current month, a nil object will be returned
- (NSDate*)pacoDayInCurrentMonth:(NSUInteger)dayIndex;

- (NSDate*)pacoSundayInCurrentWeek;

- (BOOL)pacoInSameMonthWith:(NSDate*)another;

- (NSDate*)pacoCycleStartDateOfMonthWithOriginalStartDate:(NSDate*)originalStartDate;

- (BOOL)pacoIsWeekend;
- (int)pacoIndexInWeek;

- (NSDate*)pacoNearestNonWeekendDateAtMidnight;

- (NSDate*)pacoNearestNonWeekendDate;

- (NSDate*)pacoDateByAddingMinutesInterval:(NSUInteger)offsetMinutes;

- (NSDate*)pacoDateByAddingDayInterval:(NSInteger)intervalDays;
- (NSDate*)pacoDateByAddingWeekInterval:(NSUInteger)weekInterval;
- (NSDate*)pacoDateByAddingMonthInterval:(NSUInteger)monthInterval;

- (int)pacoNumOfDaysInCurrentMonth;
- (int)pacoNumOfWeekdaysInCurrentMonth;

- (NSDate*)pacoDailyESMNextCycleStartDate:(BOOL)includeWeekends;
- (NSDate*)pacoWeeklyESMNextCycleStartDate;
- (NSDate*)pacoMonthlyESMNextCycleStartDate;

@end
