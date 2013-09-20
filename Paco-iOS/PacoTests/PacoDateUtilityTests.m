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


#import <SenTestingKit/SenTestingKit.h>
#import "PacoDate.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"

@interface PacoDate ()
+ (int)dayIndexOfDate:(NSDate *)date;
+ (int)weekdayIndexOfDate:(NSDate *)date;
+ (int)weekOfYearIndexOfDate:(NSDate *)date;
+ (int)monthOfYearIndexOfDate:(NSDate *)date;
+ (NSDate *)midnightThisDate:(NSDate *)date;
+ (NSDate *)firstDayOfMonth:(NSDate *)date;
+ (NSDate *)timeOfDayThisDate:(NSDate *)date
                        hrs24:(int)hrs24
                      minutes:(int)minutes;
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
+ (NSDate *)nextScheduledDateForExperiment:(PacoExperiment *)experiment
                              fromThisDate:(NSDate *)fromThisDate;

@end


@interface PacoDateUtilityTests : SenTestCase
@property(nonatomic, retain) PacoExperiment* experiment;

@end


//Ongoing, Fixed interval, Daily, Repeat every 1 day, timeout 479 minutes
//9:30 am, 12:50 pm, 6:11 pm
static NSString* experimentTemplate = @"{\"title\":\"NotificationTest-FixInterval-2\",\"description\":\"test\",\"informedConsentForm\":\"test\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":false,\"id\":10451001,\"questionsChange\":false,\"modifyDate\":\"2013/08/23\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"hello\",\"mandatory\":false,\"responseType\":\"likert\",\"likertSteps\":5,\"leftSideLabel\":\"left\",\"rightSideLabel\":\"right\",\"name\":\"hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":20001,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":22,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[34200000,46200000,65460000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[34200000,46200000,65460000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";

@implementation PacoDateUtilityTests

- (void)setUp
{
  [super setUp];
  // Put setup code here; it will be run once, before the first test case.
  NSError* error = nil;
  NSData* data = [experimentTemplate dataUsingEncoding:NSUTF8StringEncoding];
  id definitionDict = [NSJSONSerialization JSONObjectWithData:data
                                                      options:NSJSONReadingAllowFragments
                                                        error:&error];
  STAssertTrue(error == nil && [definitionDict isKindOfClass:[NSDictionary class]],
               @"experimentTemplate should be successfully serialized!");
  PacoExperimentDefinition* definition = [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:definitionDict];
  STAssertTrue(definition != nil, @"definition should not be nil!");
  
  PacoExperiment* experimentInstance = [[PacoExperiment alloc] init];
  experimentInstance.schedule = definition.schedule;
  experimentInstance.definition = definition;
  experimentInstance.instanceId = definition.experimentId;
  self.experiment = experimentInstance;

}

- (void)tearDown
{
  // Put teardown code here; it will be run once, after the last test case.
  [super tearDown];
  self.experiment = nil;
}


- (void)testNextScheduledDateForExperiment {
  
}


@end
