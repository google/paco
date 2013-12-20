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

typedef enum {
  kPacoScheduleRepeatPeriodDay = 0,
  kPacoScheduleRepeatPeriodWeek = 1,
  kPacoScheduleRepeatPeriodMonth = 2,
} PacoScheduleRepeatPeriod;

typedef enum {
  kPacoScheduleDaySunday = 1,
  kPacoScheduleDayMonday = 1 << 1,
  kPacoScheduleDayTuesday = 1 << 2,
  kPacoScheduleDayWednesday = 1 << 3,
  kPacoScheduleDayThursday = 1 << 4,
  kPacoScheduleDayFriday = 1 << 5,
  kPacoScheduleDaySaturday = 1 << 6,
} PacoScheduleDay;

typedef enum {
  kPacoScheduleTypeDaily = 0,
  kPacoScheduleTypeWeekday = 1,
  kPacoScheduleTypeWeekly = 2,
  kPacoScheduleTypeMonthly = 3,
  kPacoScheduleTypeESM = 4,
  kPacoScheduleTypeSelfReport = 5,
  kPacoScheduleTypeTesting = 999, // TPE a scheduleType introducted for testing Notifications
} PacoScheduleType;


@interface PacoExperimentSchedule : NSObject

@property (nonatomic, assign) BOOL byDayOfMonth;
@property (nonatomic, assign) BOOL byDayOfWeek;
@property (nonatomic, assign) NSInteger dayOfMonth;
@property (nonatomic, assign) long long esmEndHour;
@property (nonatomic, assign) NSInteger esmFrequency;
@property (nonatomic, assign) long long esmPeriodInDays;
@property (nonatomic, assign) PacoScheduleRepeatPeriod esmPeriod;
@property (nonatomic, assign) long long esmStartHour;
@property (nonatomic, assign) BOOL esmWeekends;
@property (nonatomic, copy) NSString *scheduleId;
@property (nonatomic, assign) NSInteger nthOfMonth;

/*
 repeatRate is only valid for three types of experiment: daily, weekly and monthly
 daily:   repeat every x days
 weekly:  repeat every x weeks
 monthly: repeat every x months
 **/
@property (nonatomic, assign) NSInteger repeatRate;
@property (nonatomic, assign) PacoScheduleType scheduleType;
@property (nonatomic, retain) NSArray *times;  // NSNumber<long >
@property (nonatomic, assign) BOOL userEditable;
@property (nonatomic, assign) NSInteger weekDaysScheduled;  // Binary OR of PacoScheduleDay
@property (nonatomic, assign) NSInteger timeout;
@property (nonatomic, assign) NSInteger minimumBuffer;
@property (nonatomic, retain) id jsonObject;
@property (nonatomic, retain) NSArray *esmScheduleList;  // NSArray<NSDate>
+ (id)pacoExperimentScheduleFromJSON:(id)jsonObject;
- (NSString *)jsonString;
- (NSString*)esmStartTimeString;
- (NSString*)esmEndTimeString;
- (BOOL)isESMSchedule;
- (NSInteger)minutesPerDayOfESM;
- (NSDate*)esmStartTimeOnDate:(NSDate*)date;

- (NSString*)evaluateSchedule;

@end
