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
#import "NSMutableArray+Paco.h"
#import "PacoClient.h"

@implementation PacoScheduleGenerator (ESM)

+ (NSArray*)nextDatesForESMExperiment:(PacoExperiment*)experiment
                           numOfDates:(int)numOfDates
                             fromDate:(NSDate*)fromDate {
  NSAssert(numOfDates > 0, @"numOfDates should be valid!");
  NSAssert(([experiment startDate] && [experiment endDate]) ||
           (![experiment startDate] && ![experiment endDate]),
           @"start and end date should be consistent");
  NSAssert(experiment.schedule.scheduleType == kPacoScheduleTypeESM, @"should be an ESM experiment");
  
  NSArray* result = [self datesToScheduleForESMExperiment:experiment
                                              numOfDates:numOfDates
                                                fromDate:fromDate];
  return [result pacoIsNotEmpty] ? result : nil;
}


+ (NSArray*)datesToScheduleForESMExperiment:(PacoExperiment*)experiment
                                 numOfDates:(NSInteger)numOfDates
                                   fromDate:(NSDate*)fromDate {
  NSArray* datesToSchedule = [experiment ESMSchedulesFromDate:fromDate];
  NSInteger extraNumOfDates = numOfDates - [datesToSchedule count];
  if (extraNumOfDates > 0) {
    NSArray* extraDates = [self generateESMDatesForExperiment:experiment
                                            minimumNumOfDates:extraNumOfDates
                                                 lastSchedule:[experiment lastESMScheduleDate]
                                                     fromDate:fromDate];
    if ([extraDates pacoIsNotEmpty]) {
      NSMutableArray* result = [NSMutableArray arrayWithArray:datesToSchedule];
      [result addObjectsFromArray:extraDates];
      datesToSchedule = result;
    }
  }
  
  if (0 < [datesToSchedule count]) {
    experiment.schedule.esmScheduleList = datesToSchedule;
    DDLogInfo(@"New esm schedule list: %@", [datesToSchedule pacoDescriptionForDates]);
  } else {
    //NOTE: don't save empty datesToSchedule, we need to keep the last generated esm schedule in the last cycle,
    //in order to calculate the next esm cycle start correctly. If we save the empty datesToSchedule,
    //next time major task is executed, it will treat it as it's the first time generating esm schedules
    //for this experiment, thus re-generate schedules for the last esm cycle, and this may end up generating
    //extra esm schedules for the last cycle of any fixed-length experiment.
    DDLogInfo(@"No esm schedules, looks like this experiment is finished already.");
  }
  
  if ([datesToSchedule count] <= numOfDates) {
    return datesToSchedule;
  } else {
    return [datesToSchedule subarrayWithRange:NSMakeRange(0, numOfDates)];
  }
}


/*
   Daily:                return current day at midnight
  Weekly:a.ongoing:      return the first day in current calendar week
         b.fixed-length: return the first day in current cycle week determined by experiment start date
 Monthly:a.ongoing:      return the first day in current calendar month
         b.fixed-length: return the first day in current cycle month determined by experiment start date
 **/
+ (NSDate*)currentCycleStartDateForDate:(NSDate*)date
                    experimentStartDate:(NSDate*)experimentStartDate
                           scheduleType:(PacoScheduleRepeatPeriod)repeatPeriod {
  NSDate* result = nil;
  //daily
  if (repeatPeriod == kPacoScheduleRepeatPeriodDay) {
    result = [date pacoCurrentDayAtMidnight];
    return result;
  }
  //weekly
  if (repeatPeriod == kPacoScheduleRepeatPeriodWeek) {
    if (experimentStartDate == nil ) { //ongoing
      result = [date pacoSundayInCurrentWeek];
    } else { //fixed-length
      NSInteger numOfDays = [[NSCalendar pacoGregorianCalendar] pacoDaysFromDate:experimentStartDate
                                                                    toDate:date];
      NSAssert(numOfDays >= 0, @"scheduleDate should be later than experimentStartDate");
      if (numOfDays >= kPacoNumOfDaysInWeek) { //more than a week
        NSInteger weekOffset = numOfDays / kPacoNumOfDaysInWeek;
        result = [experimentStartDate pacoDateByAddingWeekInterval:weekOffset];
      } else {
        result = experimentStartDate;
      }
    }
    return result;
  }
  //monthly
  if (repeatPeriod == kPacoScheduleRepeatPeriodMonth) {
    if (experimentStartDate == nil ) { //ongoing
      result = [date pacoFirstDayInCurrentMonth];
    } else { //fixed-length
      result = [date pacoCycleStartDateOfMonthWithOriginalStartDate:experimentStartDate];
    }
    return result;
  }

  NSAssert(NO, @"should never happen");
  return result;
}


//YMZ:TODO: this method should be refactored using the method of currentCycleStartDateForDate
+ (NSDate*)esmCycleStartDateForSchedule:(PacoExperimentSchedule*)schedule
                    experimentStartDate:(NSDate*)experimentStartDate
                      experimentEndDate:(NSDate*)experimentEndDate
                               fromDate:(NSDate*)fromDate {
  NSDate* realStartDate = [fromDate pacoCurrentDayAtMidnight];
  if (experimentEndDate && [realStartDate pacoNoEarlierThanDate:experimentEndDate]) {
    return nil;
  }
  PacoScheduleRepeatPeriod repeatPeriod = schedule.esmPeriod;
  if (experimentStartDate) { //fixed-length
    //if user joins a fixed-length experiment ealier than the its start date,
    //then we need to adjust the real start date to the experiment start date
    if ([realStartDate pacoNoLaterThanDate:experimentStartDate]) {
      realStartDate = experimentStartDate;
    } else {
      if (repeatPeriod == kPacoScheduleRepeatPeriodWeek ||
          repeatPeriod == kPacoScheduleRepeatPeriodMonth) {
        realStartDate = [self currentCycleStartDateForDate:fromDate
                                       experimentStartDate:experimentStartDate
                                              scheduleType:repeatPeriod];
      }
    }
  } else { //ongoing
    if (repeatPeriod == kPacoScheduleRepeatPeriodWeek) {
      realStartDate = [fromDate pacoSundayInCurrentWeek];
    } else if (repeatPeriod == kPacoScheduleRepeatPeriodMonth) {
      realStartDate = [fromDate pacoFirstDayInCurrentMonth];
    }
  }
  //only adjust the cycle start date for daily esm in terms of including weekends or not
  if (repeatPeriod == kPacoScheduleRepeatPeriodDay) {
    //adjust the startDate if weekend is not included
    if (!schedule.esmWeekends && [realStartDate pacoIsWeekend]) {
      realStartDate = [realStartDate pacoNearestNonWeekendDateAtMidnight];
    }
  }
  //adjust the startDate according to experiment endDate
  if (experimentEndDate && [realStartDate pacoNoEarlierThanDate:experimentEndDate]) {
    return nil;
  } else {
    return realStartDate;
  }
}


+ (NSDate*)nextCycleStartDateForSchedule:(PacoExperimentSchedule*)schedule
                     experimentStartDate:(NSDate*)experimentStartDate
                       experimentEndDate:(NSDate*)experimentEndDate
                          cycleStartDate:(NSDate*)currentStartDate {
  NSDate* nextCycleStartDate = nil;
  if (schedule.esmPeriod == kPacoScheduleRepeatPeriodDay) {
    nextCycleStartDate = [currentStartDate pacoDailyESMNextCycleStartDate:schedule.esmWeekends];
  } else if (schedule.esmPeriod == kPacoScheduleRepeatPeriodWeek) {
    nextCycleStartDate = [currentStartDate pacoWeeklyESMNextCycleStartDate];
  } else if (schedule.esmPeriod == kPacoScheduleRepeatPeriodMonth) {
    nextCycleStartDate = [currentStartDate pacoMonthlyESMNextCycleStartDate];
  }
  NSAssert(nextCycleStartDate, @"should be valid");
  
  if (experimentEndDate && [nextCycleStartDate pacoNoEarlierThanDate:experimentEndDate]) {
    return nil;
  }
  NSAssert(experimentStartDate == nil ||
           (experimentStartDate && [nextCycleStartDate pacoLaterThanDate:experimentStartDate]),
           @"nextCycleStartDate should always be later than experiment start date");
  return nextCycleStartDate;
}

+ (BOOL)isCurrentFromDate:(NSDate*)fromDate
         inLaterCycleThan:(NSDate*)lastScheduleDate
                ofESMType:(PacoScheduleRepeatPeriod)esmType
      experimentStartDate:(NSDate*)experimentStartDate {
  NSDate* lastScheduleCycleStartDate = [self currentCycleStartDateForDate:lastScheduleDate
                                                      experimentStartDate:experimentStartDate
                                                             scheduleType:esmType];
  NSDate* currentCycleStartDate = [self currentCycleStartDateForDate:fromDate
                                                 experimentStartDate:experimentStartDate
                                                        scheduleType:esmType];
  if ([currentCycleStartDate pacoLaterThanDate:lastScheduleCycleStartDate]) {
    return YES;
  } else {
    return NO;
  }
}

+ (NSArray*)generateESMDatesForExperiment:(PacoExperiment*)experiment
                        minimumNumOfDates:(NSUInteger)minimumNumOfDates
                             lastSchedule:(NSDate*)lastSchedule
                                 fromDate:(NSDate*)fromDate {
  if ([experiment endDate] && [fromDate pacoNoEarlierThanDate:[experiment endDate]]) {
    NSAssert(NO, @"should never happen");
  }
  BOOL needsToAdjustCycleStartDate = NO;
  if (lastSchedule && ![self isCurrentFromDate:fromDate
                              inLaterCycleThan:lastSchedule
                                     ofESMType:experiment.schedule.esmPeriod
                           experimentStartDate:[experiment startDate]]) {
    needsToAdjustCycleStartDate = YES;
  }
  
  NSDate* cycleStartDate = nil;
  if (needsToAdjustCycleStartDate) {
      NSDate* lastScheduleCycleStartDate =
          [self currentCycleStartDateForDate:lastSchedule
                         experimentStartDate:[experiment startDate]
                                scheduleType:experiment.schedule.esmPeriod];
      cycleStartDate = [self nextCycleStartDateForSchedule:experiment.schedule
                                       experimentStartDate:[experiment startDate]
                                         experimentEndDate:[experiment endDate]
                                            cycleStartDate:lastScheduleCycleStartDate];
  } else {
    cycleStartDate = [self esmCycleStartDateForSchedule:experiment.schedule
                                    experimentStartDate:[experiment startDate]
                                      experimentEndDate:[experiment endDate]
                                               fromDate:fromDate];
  }
  

  NSMutableArray* result = [NSMutableArray arrayWithCapacity:minimumNumOfDates];
  NSArray* esmDatesInCycle = nil;
  BOOL finished = NO;
  while (!finished) {
    if (cycleStartDate == nil) {
      finished = YES;
    } else {
      esmDatesInCycle = [self createESMScheduleDates:experiment.schedule
                                      cycleStartDate:cycleStartDate
                                            fromDate:fromDate
                                   experimentEndDate:[experiment endDate]];
      [result addObjectsFromArray:esmDatesInCycle];
      if ([result count] >= minimumNumOfDates) {
        finished = YES;
      } else {
        cycleStartDate = [self nextCycleStartDateForSchedule:experiment.schedule
                                         experimentStartDate:[experiment startDate]
                                           experimentEndDate:[experiment endDate]
                                              cycleStartDate:cycleStartDate];
      }
    }
  }
  return result;
}

+ (NSArray *)createESMScheduleDates:(PacoExperimentSchedule*)experimentSchedule
                     cycleStartDate:(NSDate*)cycleStartDate
                           fromDate:(NSDate*)fromDate
                  experimentEndDate:(NSDate*)experimentEndDate {
  //adjust the start date for monthly and weekly esm
  if (cycleStartDate != nil && !experimentSchedule.esmWeekends && [cycleStartDate pacoIsWeekend]) {
    NSAssert(experimentSchedule.esmPeriod != kPacoScheduleRepeatPeriodDay,
             @"cycle start date should have already adjusted for daily esm!");
    cycleStartDate = [cycleStartDate pacoNearestNonWeekendDateAtMidnight];
    
    //adjust the startDate according to experiment endDate
    if (experimentEndDate && [cycleStartDate pacoNoEarlierThanDate:experimentEndDate]) {
      cycleStartDate = nil;
    }
  }
  if (cycleStartDate == nil) {
    return nil;
  }
  
  int numOfExperimentDaysInCycle = 0;
  NSInteger minBuffer = experimentSchedule.minimumBuffer;
  switch (experimentSchedule.esmPeriod) {
    case kPacoScheduleRepeatPeriodDay:
      numOfExperimentDaysInCycle = 1;
      break;
    case kPacoScheduleRepeatPeriodWeek:
      numOfExperimentDaysInCycle = experimentSchedule.esmWeekends ? 7.0 : 5.0;
      minBuffer = 0;
      break;
    case kPacoScheduleRepeatPeriodMonth:
      if (experimentSchedule.esmWeekends) {
        numOfExperimentDaysInCycle = [fromDate pacoNumOfDaysInCurrentMonth];
      } else {
        numOfExperimentDaysInCycle = [fromDate pacoNumOfWeekdaysInCurrentMonth];
      }
      break;
    default:
      NSAssert(NO, @"should never happen");
      return nil;
  }
  NSInteger esmMinutesPerDay = [experimentSchedule minutesPerDayOfESM];
  NSInteger durationMinutes = esmMinutesPerDay * numOfExperimentDaysInCycle;
  NSArray* randomMinutes = [PacoUtility randomIntegersInRange:(unsigned int)durationMinutes
                                                numOfIntegers:(int)experimentSchedule.esmFrequency
                                                    minBuffer:(int)minBuffer];
  
  NSDate* esmStartTime = [experimentSchedule esmStartTimeOnDate:cycleStartDate];
  NSMutableArray* randomDateList = [NSMutableArray arrayWithCapacity:experimentSchedule.esmFrequency];
  for (NSNumber* minutesNumObj in randomMinutes) {
    NSUInteger offsetMinutes = [minutesNumObj unsignedIntegerValue];
    NSInteger dayOffset = 0;
    if (offsetMinutes > esmMinutesPerDay) {
      NSUInteger days = offsetMinutes / esmMinutesPerDay;
      if (offsetMinutes % esmMinutesPerDay != 0) {
        days++;
      }
      dayOffset = days - 1;
    }
    NSAssert(dayOffset >= 0, @"dayOffset should always be larger than or equal to 0!");
    if (dayOffset != 0 && experimentSchedule.esmPeriod == kPacoScheduleRepeatPeriodDay) {
      NSAssert1(NO, @"Daily ESM has a day offset of %ld!", (long)dayOffset);
    }
    
    NSDate* realStartTime = [esmStartTime pacoDateByAddingDayInterval:dayOffset];
    if (!experimentSchedule.esmWeekends && [realStartTime pacoIsWeekend]) {
      realStartTime = [realStartTime pacoNearestNonWeekendDate];
    }
    
    NSUInteger realOffsetMinutes = offsetMinutes - dayOffset * esmMinutesPerDay;
    NSDate* randomDate = [realStartTime pacoDateByAddingMinutesInterval:realOffsetMinutes];
    if (experimentEndDate && [experimentEndDate pacoNoLaterThanDate:randomDate]) {
      break;
    }
    if ([randomDate pacoLaterThanDate:fromDate]) {
      [randomDateList addObject:randomDate];
    }
  }
  return randomDateList;
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
    case kPacoScheduleRepeatPeriodDay: {
      durationMinutes = minutesPerDay;
      startDay = [PacoDateUtility weekdayIndexOfDate:fromThisDate];
    }
      break;
    case kPacoScheduleRepeatPeriodWeek: {
      durationMinutes = minutesPerDay * (experimentSchedule.esmWeekends ? 7.0 : 5.0);
    }
      break;
    case kPacoScheduleRepeatPeriodMonth: {
      //about 21.74 work days per month on average.
      durationMinutes = minutesPerDay * (experimentSchedule.esmWeekends ? 30 : 21.74);
    }
      break;
  }
  
  NSInteger NUM_OF_BUCKETS = experimentSchedule.esmFrequency;
  NSAssert(NUM_OF_BUCKETS >= 1, @"The number of buckets should be larger than or equal to 1");
  double MINUTES_PER_BUCKET = durationMinutes/((double)NUM_OF_BUCKETS);
  
  NSMutableArray *randomDates = [NSMutableArray array];
  NSInteger lowerBound = 0;
  for (int bucketIndex = 1; bucketIndex <= NUM_OF_BUCKETS; ++bucketIndex) {
    int upperBound = MINUTES_PER_BUCKET * bucketIndex;
    int upperBoundByMinBuffer =
    durationMinutes - experimentSchedule.minimumBuffer * (NUM_OF_BUCKETS - bucketIndex);
    if (upperBound > upperBoundByMinBuffer) {
      upperBound = upperBoundByMinBuffer;
      //      NSLog(@"%d: upperBound is adjusted to %d", bucketIndex, upperBound);
    }
    //    NSLog(@"low=%d, upper=%d", lowerBound, upperBound);
    NSUInteger offsetMinutes = [PacoUtility randomUnsignedIntegerBetweenMin:(unsigned int)lowerBound
                                                                     andMax:(unsigned int)upperBound];
    //    NSLog(@"RandomMinutes=%d", offsetMinutes);
    int offsetHours = offsetMinutes / 60.0;
    int offsetDays = offsetHours / hoursPerDay;
    
    if (experimentSchedule.esmPeriod == kPacoScheduleRepeatPeriodDay && offsetDays > 0) {
      double offsetHoursInDouble = offsetMinutes/60.0;
      if (offsetHoursInDouble <= hoursPerDay) {
        offsetDays = 0;
      } else {
        NSAssert(NO, @"offsetDays should always be 0 for kPacoScheduleRepeatPeriodDay");
      }
    }
    
    offsetMinutes -= offsetHours * 60;
    offsetHours -= offsetDays * hoursPerDay;
    
    NSDate *date = [PacoDateUtility dateSameWeekAs:fromThisDate dayIndex:(startDay + offsetDays) hr24:(iStartHour + offsetHours) min:(startMinutes + offsetMinutes)];
    [randomDates addObject:date];
    
    lowerBound = upperBound;
    NSInteger lowestBoundForNextSchedule = offsetMinutes + experimentSchedule.minimumBuffer;
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
