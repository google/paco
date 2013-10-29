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
#import "PacoScheduleGenerator+ESM.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperimentSchedule.h"
#import "PacoDateUtility.h"
#import "NSDate+Paco.h"
#import "NSCalendar+Paco.h"
#import "PacoUtility.h"


@implementation PacoScheduleGenerator (ESM)

+ (NSArray*)nextDatesForESMExperiment:(PacoExperiment*)experiment
                           numOfDates:(NSInteger)numOfDates
                             fromDate:(NSDate*)fromDate {
  //todo
}


//YMZ:TODO: why 500? when will a nil result be returned?
+ (NSDate *)nextESMScheduledDateForExperiment:(PacoExperiment *)experiment
                                 fromThisDate:(NSDate *)fromThisDate {
  NSDate *scheduled = nil;
  BOOL done = NO;
  NSDate *from = fromThisDate;
  int max = 500;
  while (!done) {
    max -= 1;
    if (max == 0)
      break;
    NSArray *scheduleDates = experiment.schedule.esmScheduleList;
    if (!scheduleDates.count) {
      scheduleDates = [self createESMScheduleDates:experiment.schedule fromThisDate:from];
      experiment.schedule.esmScheduleList = scheduleDates;
      NSLog(@"NEW SCHEDULE: ");
      NSLog(@"(");
      for (NSDate* date in scheduleDates) {
        NSLog(@"%@", [PacoDateUtility pacoStringForDate:date]);
      }
      NSLog(@")");
    }
    scheduled = [PacoDateUtility nextTimeFromScheduledDates:scheduleDates onDayOfDate:fromThisDate];
    if (!scheduled) {
      // need to either schedule entire days here or know whether to use last time or
      // whether to use today+1 for generating the new schedule
      
      
      // Must be for the next day/week/month.
      switch (experiment.schedule.esmPeriod) {
        case kPacoScheduleRepeatPeriodDay:
          from = [PacoDateUtility date:from thisManyDaysFrom:1];
          break;
        case kPacoScheduleRepeatPeriodWeek:
          from = [PacoDateUtility date:from thisManyWeeksFrom:1];
          break;
        case kPacoScheduleRepeatPeriodMonth:
          from = [PacoDateUtility date:from thisManyMonthsFrom:1];
          break;
        default:
          NSAssert(NO, @"Invalid esm period");
      }
      experiment.schedule.esmScheduleList = nil;
    }
    if (scheduled) {
      done = YES;
    }
  }
  return scheduled;
}

//YMZ:TODO: check this algorithm for kPacoSchedulePeriodWeek and kPacoSchedulePeriodMonth
+ (NSArray *)createESMScheduleDates:(PacoExperimentSchedule*)experimentSchedule
                       fromThisDate:(NSDate*)fromThisDate {
  double startSeconds = experimentSchedule.esmStartHour / 1000.0;
  double startMinutes = startSeconds / 60.0;
  double startHour = startMinutes / 60.0;
  int iStartHour = ((int)startHour);
  startMinutes -= (iStartHour * 60);
  double millisecondsPerDay = experimentSchedule.esmEndHour - experimentSchedule.esmStartHour;
  double secondsPerDay = millisecondsPerDay / 1000.0;
  double minutesPerDay = secondsPerDay / 60.0;
  double hoursPerDay = minutesPerDay / 60.0;
  
  int startDay = experimentSchedule.esmWeekends ? 0 : 1;
  
  double durationMinutes = 0;
  switch (experimentSchedule.esmPeriod) {
    case kPacoSchedulePeriodDay: {
      durationMinutes = minutesPerDay;
      startDay = [PacoDateUtility weekdayIndexOfDate:fromThisDate];
    }
      break;
    case kPacoSchedulePeriodWeek: {
      durationMinutes = minutesPerDay * (experimentSchedule.esmWeekends ? 7.0 : 5.0);
    }
      break;
    case kPacoSchedulePeriodMonth: {
      //about 21.74 work days per month on average.
      durationMinutes = minutesPerDay * (experimentSchedule.esmWeekends ? 30 : 21.74);
    }
      break;
  }
  
  int NUM_OF_BUCKETS = experimentSchedule.esmFrequency;
  NSAssert(NUM_OF_BUCKETS >= 1, @"The number of buckets should be larger than or equal to 1");
  double MINUTES_PER_BUCKET = durationMinutes/((double)NUM_OF_BUCKETS);
  
  NSMutableArray *randomDates = [NSMutableArray array];
  int lowerBound = 0;
  for (int bucketIndex = 1; bucketIndex <= NUM_OF_BUCKETS; ++bucketIndex) {
    int upperBound = MINUTES_PER_BUCKET * bucketIndex;
    int upperBoundByMinBuffer =
    durationMinutes - experimentSchedule.minimumBuffer * (NUM_OF_BUCKETS - bucketIndex);
    if (upperBound > upperBoundByMinBuffer) {
      upperBound = upperBoundByMinBuffer;
      //      NSLog(@"%d: upperBound is adjusted to %d", bucketIndex, upperBound);
    }
    //    NSLog(@"low=%d, upper=%d", lowerBound, upperBound);
    int offsetMinutes = [PacoUtility randomUnsignedIntegerBetweenMin:lowerBound andMax:upperBound];
    //    NSLog(@"RandomMinutes=%d", offsetMinutes);
    int offsetHours = offsetMinutes / 60.0;
    int offsetDays = offsetHours / hoursPerDay;
    
    if (experimentSchedule.esmPeriod == kPacoSchedulePeriodDay && offsetDays > 0) {
      double offsetHoursInDouble = offsetMinutes/60.0;
      if (offsetHoursInDouble <= hoursPerDay) {
        offsetDays = 0;
      } else {
        NSAssert(NO, @"offsetDays should always be 0 for kPacoSchedulePeriodDay");
      }
    }
    
    offsetMinutes -= offsetHours * 60;
    offsetHours -= offsetDays * hoursPerDay;
    
    NSDate *date = [PacoDateUtility dateSameWeekAs:fromThisDate dayIndex:(startDay + offsetDays) hr24:(iStartHour + offsetHours) min:(startMinutes + offsetMinutes)];
    [randomDates addObject:date];
    
    lowerBound = upperBound;
    int lowestBoundForNextSchedule = offsetMinutes + experimentSchedule.minimumBuffer;
    if (lowerBound < lowestBoundForNextSchedule) {
      lowerBound = lowestBoundForNextSchedule;
      //      NSLog(@"%d: lowerBound is adjusted to %d", bucketIndex, lowestBoundForNextSchedule);
    }
  }
  
  return [randomDates sortedArrayUsingComparator:^NSComparisonResult(id obj1, id obj2) {
    NSDate *lhs = obj1;
    NSDate *rhs = obj2;
    return [lhs compare:rhs];
  }];
}


@end
