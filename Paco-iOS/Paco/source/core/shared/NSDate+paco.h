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
- (NSDate*)pacoFutureDateAtMidnightWithInterval:(NSInteger)intervalDays;

//The array of times should be already sorted!
- (NSArray*)pacoDatesToScheduleWithTimes:(NSArray*)times andEndDate:(NSDate*)endDate;

- (BOOL)pacoCanScheduleTimes:(NSArray*)times;

- (BOOL)pacoOnSameDayWithDate:(NSDate*)anotherDate;

- (BOOL)pacoIsWeekend;

//return a midnight date
- (NSDate*)pacoFirstFutureNonWeekendDate;

- (NSDate*)pacoDateByAddingMinutesInterval:(NSUInteger)offsetMinutes;

- (NSDate*)pacoNextCycleStartDateForESMType:(PacoScheduleRepeatPeriod)esmType
                            includeWeekends:(BOOL)includeWeekends;

@end
