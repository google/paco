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

#import <XCTest/XCTest.h>
#import "PacoScheduleGenerator.h"
#import "PacoDateUtility.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"
#import "NSDate+Paco.h"
#import "PacoScheduleGenerator+Daily.h"

//Ongoing, Fixed interval, Daily, Repeat every 1 day, timeout 479 minutes
//9:30 am, 12:50 pm, 6:11 pm
static NSString* experimentTemplate = @"{\"title\":\"NotificationTest-FixInterval-2\",\"description\":\"test\",\"informedConsentForm\":\"test\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":false,\"id\":10451001,\"questionsChange\":false,\"modifyDate\":\"2013/08/23\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"hello\",\"mandatory\":false,\"responseType\":\"likert\",\"likertSteps\":5,\"leftSideLabel\":\"left\",\"rightSideLabel\":\"right\",\"name\":\"hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":20001,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":22,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[34200000,46200000,65460000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[34200000,46200000,65460000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";

//Ongoing, Fixed interval, Daily, Repeat every 1 day, timeout 479 minutes
//12am
static NSString* experimentFirst = @"{\"title\":\"First\",\"description\":\"This is a first experiment\",\"informedConsentForm\":\"Won't be shared.\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":false,\"id\":11059019,\"questionsChange\":false,\"modifyDate\":\"2011/05/31\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"Is this here?\",\"mandatory\":false,\"responseType\":\"open text\",\"name\":\"var1\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":2,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":1,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[0],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[0],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";

//Ongoing, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
static NSString* experimentOngoingDaily = @"{\"title\":\"NotificationTest-FixInterval: Daily\",\"description\":\"fix interval\",\"informedConsentForm\":\"test\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":false,\"id\":10446002,\"questionsChange\":false,\"modifyDate\":\"2013/08/23\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"test\",\"mandatory\":true,\"responseType\":\"likert\",\"likertSteps\":5,\"leftSideLabel\":\"left\",\"rightSideLabel\":\"right\",\"name\":\"Hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":9001,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":10,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[32400000,61200000],\"repeatRate\":2,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[32400000,61200000],\"repeatRate\":2,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";

//10/16-10/16, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
static NSString* experimentFixedLengthDaily = @"{\"title\":\"NotificationTest-FixInterval: Daily\",\"description\":\"fix interval\",\"informedConsentForm\":\"test\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":true,\"startDate\":\"2013/10/16\",\"endDate\":\"2013/10/16\",\"id\":10446002,\"questionsChange\":false,\"modifyDate\":\"2013/08/23\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"test\",\"mandatory\":true,\"responseType\":\"likert\",\"likertSteps\":5,\"leftSideLabel\":\"left\",\"rightSideLabel\":\"right\",\"name\":\"Hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":9002,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":11,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[32400000,61200000],\"repeatRate\":2,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[32400000,61200000],\"repeatRate\":2,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";

@interface PacoScheduleGenerator ()
+ (NSDate*)getStartTimeWithExperimentStartDate:(NSDate*)experimentStartDate
                                      fromDate:(NSDate*)fromDate
                                         times:(NSArray*)times
                                    repeatRate:(NSInteger)repeatRate;
+ (NSDate*)getStartTimeFromDate:(NSDate*)fromDate withTimes:(NSArray*)times;
+ (NSDate*)getRealStartDateWithTimes:(NSArray*)times
                   originalStartDate:(NSDate*)originalStartDate
                 experimentStartDate:(NSDate*)experimentStartDate
                          repeatRate:(NSInteger)repeatRate;

@end

@interface PacoScheduleGeneratorTests : XCTestCase

@property(nonatomic, strong) PacoExperiment* experiment;
@property(nonatomic, strong) PacoExperiment* experimentFirst;
@property(nonatomic, strong) PacoExperiment* dailyExperiment;
@property(nonatomic, strong) PacoExperiment* fixedLengthDailyExperiment;
@property(nonatomic, strong) NSArray* times;

@end

@implementation PacoScheduleGeneratorTests

- (void)setUp
{
  [super setUp];
  // Put setup code here; it will be run once, before the first test case.
  NSError* error = nil;
  NSData* data = [experimentTemplate dataUsingEncoding:NSUTF8StringEncoding];
  id definitionDict = [NSJSONSerialization JSONObjectWithData:data
                                                      options:NSJSONReadingAllowFragments
                                                        error:&error];
  XCTAssertTrue(error == nil && [definitionDict isKindOfClass:[NSDictionary class]],
               @"experimentTemplate should be successfully serialized!");
  PacoExperimentDefinition* definition = [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:definitionDict];
  XCTAssertTrue(definition != nil, @"definition should not be nil!");
  
  PacoExperiment* experimentInstance = [[PacoExperiment alloc] init];
  experimentInstance.schedule = definition.schedule;
  experimentInstance.definition = definition;
  experimentInstance.instanceId = definition.experimentId;
  self.experiment = experimentInstance;
  
  
  error = nil;
  data = [experimentFirst dataUsingEncoding:NSUTF8StringEncoding];
  definitionDict = [NSJSONSerialization JSONObjectWithData:data
                                                   options:NSJSONReadingAllowFragments
                                                     error:&error];
  XCTAssertTrue(error == nil && [definitionDict isKindOfClass:[NSDictionary class]],
               @"experimentFirst should be successfully serialized!");
  definition = [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:definitionDict];
  XCTAssertTrue(definition != nil, @"definition should not be nil!");
  
  experimentInstance = [[PacoExperiment alloc] init];
  experimentInstance.schedule = definition.schedule;
  experimentInstance.definition = definition;
  experimentInstance.instanceId = definition.experimentId;
  self.experimentFirst = experimentInstance;
  
  
  error = nil;
  data = [experimentOngoingDaily dataUsingEncoding:NSUTF8StringEncoding];
  definitionDict = [NSJSONSerialization JSONObjectWithData:data
                                                   options:NSJSONReadingAllowFragments
                                                     error:&error];
  XCTAssertTrue(error == nil && [definitionDict isKindOfClass:[NSDictionary class]],
               @"dailyExperiment should be successfully serialized!");
  definition = [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:definitionDict];
  XCTAssertTrue(definition != nil, @"definition should not be nil!");
  experimentInstance = [[PacoExperiment alloc] init];
  experimentInstance.schedule = definition.schedule;
  experimentInstance.definition = definition;
  experimentInstance.instanceId = definition.experimentId;
  self.dailyExperiment = experimentInstance;
  
  error = nil;
  data = [experimentFixedLengthDaily dataUsingEncoding:NSUTF8StringEncoding];
  definitionDict = [NSJSONSerialization JSONObjectWithData:data
                                                   options:NSJSONReadingAllowFragments
                                                     error:&error];
  XCTAssertTrue(error == nil && [definitionDict isKindOfClass:[NSDictionary class]],
               @"fixedLengthDailyExperiment should be successfully serialized!");
  definition = [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:definitionDict];
  XCTAssertTrue(definition != nil, @"definition should not be nil!");
  experimentInstance = [[PacoExperiment alloc] init];
  experimentInstance.schedule = definition.schedule;
  experimentInstance.definition = definition;
  experimentInstance.instanceId = definition.experimentId;
  self.fixedLengthDailyExperiment = experimentInstance;

  int hours = 9;
  int minutes = 35;
  int seconds = 50;
  long firstMilliseconds = ((hours*60+minutes)*60+seconds)*1000;
  hours = 17;
  minutes = 23;
  seconds = 44;
  long secondMilliseconds = ((hours*60+minutes)*60+seconds)*1000;
  //09:35:50, 17:23:44
  self.times = @[@(firstMilliseconds),
                 @(secondMilliseconds)];
}

- (void)tearDown
{
  // Put teardown code here; it will be run once, after the last test case.
  self.experiment = nil;
  self.times = nil;
  [super tearDown];
}


- (void)testGetStartTimeWithExperiment {
  NSInteger repeatRate = 2;
  NSDate* startDate = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/21"];
  XCTAssertNotNil(startDate, @"startDate should be valid");
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/18 14:00:00-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeWithExperimentStartDate:startDate
                                                                     fromDate:fromDate
                                                                        times:self.times
                                                                   repeatRate:repeatRate];
  XCTAssertEqualObjects(result, startDate,
                       @"should be experiment's start date if user joins before experiment starts");
}

//09:35:50, 17:23:44
- (void)testGetStartTimeWithFromDateOnSameDayAsExperimentStart {
  NSInteger repeatRate = 2;
  NSDate* startDate = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/21"];
  XCTAssertNotNil(startDate, @"startDate should be valid");
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/21 14:00:00-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeWithExperimentStartDate:startDate
                                                                     fromDate:fromDate
                                                                        times:self.times
                                                                   repeatRate:repeatRate];
  XCTAssertEqualObjects(result, fromDate,
                       @"should be fromDate if user joins on experiment startDate and is still"
                       @"able to schedule times");
}

//09:35:50, 17:23:44
- (void)testFromDateOnSameDayAsExperimentStartRepeatRateOne {
  NSInteger repeatRate = 1;
  NSDate* startDate = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/21"];
  XCTAssertNotNil(startDate, @"startDate should be valid");
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/21 18:00:00-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeWithExperimentStartDate:startDate
                                                                     fromDate:fromDate
                                                                        times:self.times
                                                                   repeatRate:repeatRate];
  NSDate* expect = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/22"];
  XCTAssertEqualObjects(result, expect,
                       @"should be next day");
}

//09:35:50, 17:23:44
- (void)testGetStartTimeWithFromDateOnNextDayOfExperimentStart {
  NSInteger repeatRate = 2;
  NSDate* startDate = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/21"];
  XCTAssertNotNil(startDate, @"startDate should be valid");
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/22 14:00:00-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeWithExperimentStartDate:startDate
                                                                     fromDate:fromDate
                                                                        times:self.times
                                                                   repeatRate:repeatRate];
  NSDate* expect = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/23"];
  XCTAssertEqualObjects(result, expect,
                       @"should be next next day");
}


- (void)testGetStartTimeWithExperiment2 {
  NSInteger repeatRate = 2;
  NSDate* startDate = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/18"];
  XCTAssertNotNil(startDate, @"startDate should be valid");
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/21 14:00:00-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeWithExperimentStartDate:startDate
                                                                     fromDate:fromDate
                                                                        times:self.times
                                                                   repeatRate:repeatRate];
  NSDate* expect = [PacoDateUtility pacoDateForString:@"2013/10/22 00:00:00-0700"];
  XCTAssertEqualObjects(result, expect,
                       @"should be 10/22 since user joins the experiment after it starts, and"
                       @"the repeatRate is 2 days");
}

- (void)testGetStartTimeWithExperiment3 {
  NSInteger repeatRate = 2;
  NSDate* startDate = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/18"];
  XCTAssertNotNil(startDate, @"startDate should be valid");
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/20 14:00:00-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeWithExperimentStartDate:startDate
                                                                     fromDate:fromDate
                                                                        times:self.times
                                                                   repeatRate:repeatRate];
  XCTAssertEqualObjects(result, fromDate,
                       @"should be fromDate since it's still able to schedule for 17:23:44");
}

- (void)testGetStartTimeWithExperiment4 {
  NSInteger repeatRate = 2;
  NSDate* startDate = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/18"];
  XCTAssertNotNil(startDate, @"startDate should be valid");
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/18 17:23:45-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeWithExperimentStartDate:startDate
                                                                     fromDate:fromDate
                                                                        times:self.times
                                                                   repeatRate:repeatRate];
  NSDate* expect = [PacoDateUtility pacoDateForString:@"2013/10/20 00:00:00-0700"];
  XCTAssertEqualObjects(result, expect,
                       @"should be 2 days later since it's not able to schedule for 17:23:44");
}

- (void)testGetStartTimeWithExperiment5 {
  NSInteger repeatRate = 2;
  NSDate* startDate = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/18"];
  XCTAssertNotNil(startDate, @"startDate should be valid");
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/19 01:23:45-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeWithExperimentStartDate:startDate
                                                                     fromDate:fromDate
                                                                        times:self.times
                                                                   repeatRate:repeatRate];
  NSDate* expect = [PacoDateUtility pacoDateForString:@"2013/10/20 00:00:00-0700"];
  XCTAssertEqualObjects(result, expect,
                       @"should be next day");
}


- (void)testGetStartTimeWithExperimentRepeatRateOne {
  NSInteger repeatRate = 1;
  NSDate* startDate = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/18"];
  XCTAssertNotNil(startDate, @"startDate should be valid");
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/18 17:23:45-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeWithExperimentStartDate:startDate
                                                                     fromDate:fromDate
                                                                        times:self.times
                                                                   repeatRate:repeatRate];
  NSDate* expect = [PacoDateUtility pacoDateForString:@"2013/10/19 00:00:00-0700"];
  XCTAssertEqualObjects(result, expect,
                       @"should be the next day since it's not able to schedule for 17:23:44");
}

- (void)testGetStartTimeWithExperimentRepeatRateOne2 {
  NSInteger repeatRate = 1;
  NSDate* startDate = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/31"];
  XCTAssertNotNil(startDate, @"startDate should be valid");
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/31 17:23:45-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeWithExperimentStartDate:startDate
                                                                     fromDate:fromDate
                                                                        times:self.times
                                                                   repeatRate:repeatRate];
  NSDate* expect = [PacoDateUtility pacoDateForString:@"2013/11/01 00:00:00-0700"];
  XCTAssertEqualObjects(result, expect,
                       @"should be the next day since it's not able to schedule for 17:23:44");
}

- (void)testGetStartTimeWithExperimentRepeatRateOne3 {
  NSInteger repeatRate = 1;
  NSDate* startDate = [PacoDateUtility dateFromStringWithYearAndDay:@"2013/10/31"];
  XCTAssertNotNil(startDate, @"startDate should be valid");
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/31 10:23:45-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeWithExperimentStartDate:startDate
                                                                     fromDate:fromDate
                                                                        times:self.times
                                                                   repeatRate:repeatRate];
  XCTAssertEqualObjects(result, fromDate,
                       @"should be the fromDate since it's still able to schedule for 17:23:44");
}

- (void)testGetStartTimeFromDate {
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/31 10:23:45-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeFromDate:fromDate withTimes:self.times];
  XCTAssertEqualObjects(result, fromDate, @"should be fromDate since it's still able to schedule");
}

- (void)testGetStartTimeFromDate2 {
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/31 08:23:45-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeFromDate:fromDate withTimes:self.times];
  XCTAssertEqualObjects(result, fromDate, @"should be fromDate since it's still able to schedule");
}

- (void)testGetStartTimeFromDate3 {
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/31 17:23:45-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getStartTimeFromDate:fromDate withTimes:self.times];
  XCTAssertNotNil(result, @"result should be valid");
  NSDate* expect = [PacoDateUtility pacoDateForString:@"2013/11/01 00:00:00-0700"];
  XCTAssertNotNil(expect, @"expect should be valid");
  XCTAssertEqualObjects(result, expect, @"should be next day");
}

- (void)testRealStartDate {
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/31 17:23:45-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getRealStartDateWithTimes:self.times
                                                  originalStartDate:fromDate
                                                experimentStartDate:nil
                                                         repeatRate:2];
  NSDate* expect = [PacoDateUtility pacoDateForString:@"2013/11/01 00:00:00-0700"];
  XCTAssertEqualObjects(result, expect,
                       @"should be next day since it's not able to schedule");
}

- (void)testRealStartDate2 {
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/31 17:23:00-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getRealStartDateWithTimes:self.times
                                                  originalStartDate:fromDate
                                                experimentStartDate:nil
                                                         repeatRate:2];
  XCTAssertEqualObjects(result, fromDate,
                       @"should be fromDate since it's still able to schedule for today");
}

- (void)testRealStartDate3 {
  NSDate* experimentStartDate = [PacoDateUtility pacoDateForString:@"2013/10/29 00:00:00-0700"];
  XCTAssertNotNil(experimentStartDate, @"experimentStartDate should be valid");

  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/31 17:23:00-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getRealStartDateWithTimes:self.times
                                                  originalStartDate:fromDate
                                                experimentStartDate:experimentStartDate
                                                         repeatRate:2];
  XCTAssertEqualObjects(result, fromDate,
                       @"should be fromDate since it's still able to schedule for today");
}

- (void)testRealStartDate4 {
  NSDate* experimentStartDate = [PacoDateUtility pacoDateForString:@"2013/10/29 00:00:00-0700"];
  XCTAssertNotNil(experimentStartDate, @"experimentStartDate should be valid");
  
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/10/31 17:30:00-0700"];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSDate* result = [PacoScheduleGenerator getRealStartDateWithTimes:self.times
                                                  originalStartDate:fromDate
                                                experimentStartDate:experimentStartDate
                                                         repeatRate:2];
  NSDate* expect = [PacoDateUtility pacoDateForString:@"2013/11/02 00:00:00-0700"];
  XCTAssertNotNil(expect, @"expect should be valid");
 
  XCTAssertEqualObjects(result, expect,
                       @"should be 11/02 since it's not able to schedule for today and should"
                       @"be 2 days later");
}









@end
