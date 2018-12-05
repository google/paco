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

@interface PacoDateUtility : NSObject
//"yyyy/MM/dd HH:mm:ssZ" : "2013/07/25 12:33:22-0700"
+ (NSDate *)pacoDateForString:(NSString *)dateStr;
+ (NSString *)pacoStringForDate:(NSDate *)date;

//"yyyy/MM/dd": "2013/10/15"
+ (NSDate*)dateFromStringWithYearAndDay:(NSString*)dateStr;
+ (NSString*)stringWithYearAndDayFromDate:(NSDate*)date;

+ (NSString*)stringForAlertBodyFromDate:(NSDate*)date;

//10:00am, 5:00pm
+ (NSString*)timeStringFromMilliseconds:(long long)milliSeconds;
+ (NSString*)timeString24hrFromMilliseconds:(long long)milliSeconds;
+ (NSString*)timeStringAMPMFromMilliseconds:(long long)milliSeconds;

+ (NSUInteger)dayIndexOfDate:(NSDate *)date;
+ (int)weekdayIndexOfDate:(NSDate *)date;
+ (NSInteger)weekOfYearIndexOfDate:(NSDate *)date;
+ (NSInteger)monthOfYearIndexOfDate:(NSDate *)date;
+ (NSDate *)midnightThisDate:(NSDate *)date;
+ (NSDate *)firstDayOfMonth:(NSDate *)date;
+ (NSDate *)timeOfDayThisDate:(NSDate *)date
                        hrs24:(long)hrs24
                      minutes:(long)minutes;
+ (NSDate *)nextTimeFromScheduledDates:(NSArray *)scheduledDates
                           onDayOfDate:(NSDate *)dayOfDate;
+ (NSDate *)nextTimeFromScheduledTimes:(NSArray *)scheduledTimes
                           onDayOfDate:(NSDate *)dayOfDate;
+ (NSDate *)date:(NSDate *)date thisManyDaysFrom:(int)daysFrom;
+ (NSDate *)date:(NSDate *)date thisManyWeeksFrom:(int)weeksFrom;
+ (NSDate *)date:(NSDate *)date thisManyMonthsFrom:(int)monthsFrom;
+ (NSDate *)dateSameWeekAs:(NSDate *)sameWeekAs
                  dayIndex:(int)dayIndex
                      hr24:(int)hr24
                       min:(int)min;
+ (NSDate *)dateSameMonthAs:(NSDate *)sameMonthAs
                   dayIndex:(int)dayIndex;
+ (NSDate *)dateOnNthOfMonth:(NSDate *)sameMonthAs
                         nth:(int)nth
                    dayFlags:(unsigned int)dayFlags;
+ (NSDate *)nextScheduledDay:(NSUInteger)dayFlags fromDate:(NSDate *)date;
+ (NSString *)escapedNameForTimeZone:(NSTimeZone *)timeZone;
+ (NSString *)escapedNameForSystemTimeZone;



@end
