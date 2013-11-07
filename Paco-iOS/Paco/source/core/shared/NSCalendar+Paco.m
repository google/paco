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

#import "NSCalendar+Paco.h"
#import "NSDate+Paco.h"

@implementation NSCalendar (Paco)


+ (NSCalendar*)pacoGregorianCalendar {
  return [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
}

//https://developer.apple.com/library/mac/documentation/Cocoa/Conceptual/DatesAndTimes/Articles/dtCalendricalCalculations.html#//apple_ref/doc/uid/TP40007836-SW8
- (NSInteger)pacoDaysFromDate:(NSDate*)startDate toDate:(NSDate*)endDate {
  startDate = [startDate pacoCurrentDayAtMidnight];
  endDate = [endDate pacoCurrentDayAtMidnight];
  NSInteger startDay = [self ordinalityOfUnit:NSDayCalendarUnit
                                       inUnit:NSEraCalendarUnit
                                      forDate:startDate];
  NSInteger endDay = [self ordinalityOfUnit:NSDayCalendarUnit
                                     inUnit:NSEraCalendarUnit
                                    forDate:endDate];
  NSInteger numOfDaysInBetween = endDay - startDay;
  return numOfDaysInBetween;
}

@end
