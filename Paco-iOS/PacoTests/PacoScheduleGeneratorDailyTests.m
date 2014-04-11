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
#import "NSCalendar+Paco.h"

//Ongoing, Daily, Repeat every 1 day, timeout 479 minutes
//9:30 am, 12:50 pm, 6:11 pm
static NSString* experimentTemplate = @"{\"title\":\"NotificationTest-FixInterval-2\",\"description\":\"test\",\"informedConsentForm\":\"test\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":false,\"id\":10451001,\"questionsChange\":false,\"modifyDate\":\"2013/08/23\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"hello\",\"mandatory\":false,\"responseType\":\"likert\",\"likertSteps\":5,\"leftSideLabel\":\"left\",\"rightSideLabel\":\"right\",\"name\":\"hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":20001,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":22,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[34200000,46200000,65460000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[34200000,46200000,65460000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";

//Ongoing, Fixed interval, Daily, Repeat every 1 day, timeout 479 minutes
//12am
static NSString* experimentPingAtNoon = @"{\"title\":\"First\",\"description\":\"This is a first experiment\",\"informedConsentForm\":\"Won't be shared.\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":false,\"id\":11059019,\"questionsChange\":false,\"modifyDate\":\"2011/05/31\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"Is this here?\",\"mandatory\":false,\"responseType\":\"open text\",\"name\":\"var1\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":2,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":1,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[0],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[0],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";

//Ongoing, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
static NSString* experimentOngoingEveryTwoDays = @"{\"title\":\"NotificationTest-FixInterval: Daily\",\"description\":\"fix interval\",\"informedConsentForm\":\"test\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":false,\"id\":10446002,\"questionsChange\":false,\"modifyDate\":\"2013/08/23\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"test\",\"mandatory\":true,\"responseType\":\"likert\",\"likertSteps\":5,\"leftSideLabel\":\"left\",\"rightSideLabel\":\"right\",\"name\":\"Hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":9001,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":10,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[32400000,61200000],\"repeatRate\":2,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[32400000,61200000],\"repeatRate\":2,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";

//10/16-10/16, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
static NSString* experimentFixedEveryTwoDays = @"{\"title\":\"NotificationTest-FixInterval: Daily\",\"description\":\"fix interval\",\"informedConsentForm\":\"test\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":true,\"startDate\":\"2013/10/16\",\"endDate\":\"2013/10/16\",\"id\":10446002,\"questionsChange\":false,\"modifyDate\":\"2013/08/23\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"test\",\"mandatory\":true,\"responseType\":\"likert\",\"likertSteps\":5,\"leftSideLabel\":\"left\",\"rightSideLabel\":\"right\",\"name\":\"Hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":9002,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":11,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[32400000,61200000],\"repeatRate\":2,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":479,\"id\":1,\"scheduleType\":0,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[32400000,61200000],\"repeatRate\":2,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";


@interface PacoScheduleGeneratorDailyTests : XCTestCase
@property(nonatomic, strong) PacoExperiment* experiment;
@end

@implementation PacoScheduleGeneratorDailyTests

- (void)setUp {
  [super setUp];
}

- (void)tearDown {
  self.experiment = nil;
  [super tearDown];
}

- (void)setupExperimentWithJsonName:(NSString*)jsonName
                           joinTime:(NSDate*)joinTime {
  NSError* error = nil;
  NSData* data = [jsonName dataUsingEncoding:NSUTF8StringEncoding];
  id definitionDict = [NSJSONSerialization JSONObjectWithData:data
                                                      options:NSJSONReadingAllowFragments
                                                        error:&error];
  XCTAssertTrue(error == nil && [definitionDict isKindOfClass:[NSDictionary class]],
                @"experimentTemplate should be successfully serialized!");
  PacoExperimentDefinition* definition = [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:definitionDict];
  XCTAssertTrue(definition != nil, @"definition should not be nil!");
  PacoExperiment* experimentInstance = [PacoExperiment experimentWithDefinition:definition
                                                                       schedule:definition.schedule
                                                                       joinTime:joinTime];
  self.experiment = experimentInstance;
}

- (void)setupTimes:(NSArray*)times {
  if (times) {
    self.experiment.schedule.times = times;
  }
}

- (void)setupEndDate:(NSString*)endDateStr {
  if (endDateStr) {
    NSDate* inclusiveEndDate = [PacoDateUtility dateFromStringWithYearAndDay:endDateStr];
    NSDate* realEndDate = [inclusiveEndDate pacoNextDayAtMidnight];
    XCTAssertNotNil(realEndDate, @"realEndDate should be valid");
    [self.experiment.definition setValue:realEndDate forKey:@"endDate"];
  }
}

- (void)setupRepeatRate:(int)repeatRate {
  if (repeatRate > 0) {
    self.experiment.schedule.repeatRate = repeatRate;
  }
}

//Ongoing, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
- (void)testNextDatesForDailyExperimentRepeatEveryTwoDays {
  NSDate* joinTime = [PacoDateUtility pacoDateForString:@"2013/10/14 10:22:30-0700"];
  [self setupExperimentWithJsonName:experimentOngoingEveryTwoDays
                           joinTime:joinTime];
  
  NSDate* fromDate = [joinTime dateByAddingTimeInterval:5];
  NSArray* result = [PacoScheduleGenerator nextDatesForExperiment:self.experiment
                                                       numOfDates:4
                                                         fromDate:fromDate];
  
  NSDate* date1 = [PacoDateUtility pacoDateForString:@"2013/10/14 17:00:00-0700"];
  NSDate* date2 = [PacoDateUtility pacoDateForString:@"2013/10/16 09:00:00-0700"];
  NSDate* date3 = [PacoDateUtility pacoDateForString:@"2013/10/16 17:00:00-0700"];
  NSDate* date4 = [PacoDateUtility pacoDateForString:@"2013/10/18 09:00:00-0700"];
  NSArray* expect = @[date1, date2, date3, date4];
  XCTAssertEqualObjects(result, expect, @"result should be valid");
}


//Ongoing, Daily, Repeat every 1 day, timeout 479 minutes
//9am, 5pm
- (void)testNextDatesForDailyExperimentRepeatEveryOneDay {
  NSDate* joinTime = [PacoDateUtility pacoDateForString:@"2013/10/14 10:22:30-0700"];
  [self setupExperimentWithJsonName:experimentOngoingEveryTwoDays
                           joinTime:joinTime];
  [self setupRepeatRate:1];
 
  
  NSDate* fromDate = [joinTime dateByAddingTimeInterval:5];
  NSArray* result = [PacoScheduleGenerator nextDatesForExperiment:self.experiment
                                                       numOfDates:4
                                                         fromDate:fromDate];
  
  NSDate* date1 = [PacoDateUtility pacoDateForString:@"2013/10/14 17:00:00-0700"];
  NSDate* date2 = [PacoDateUtility pacoDateForString:@"2013/10/15 09:00:00-0700"];
  NSDate* date3 = [PacoDateUtility pacoDateForString:@"2013/10/15 17:00:00-0700"];
  NSDate* date4 = [PacoDateUtility pacoDateForString:@"2013/10/16 09:00:00-0700"];
  NSArray* expect = @[date1, date2, date3, date4];
  XCTAssertEqualObjects(result, expect, @"result should be valid");
}


//10/16-10/16, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
//join at 2013/10/14 10:22:34-0700
- (void)testNextDatesForFixedDailyExperimentRepeatEveryTwoDays {
  NSDate* joinTime = [PacoDateUtility pacoDateForString:@"2013/10/14 10:22:30-0700"];
  [self setupExperimentWithJsonName:experimentFixedEveryTwoDays
                           joinTime:joinTime];
  
  NSDate* fromDate = [joinTime dateByAddingTimeInterval:5];
  NSArray* result = [PacoScheduleGenerator nextDatesForExperiment:self.experiment
                                                       numOfDates:4
                                                         fromDate:fromDate];
  NSDate* date1 = [PacoDateUtility pacoDateForString:@"2013/10/16 09:00:00-0700"];
  NSDate* date2 = [PacoDateUtility pacoDateForString:@"2013/10/16 17:00:00-0700"];
  NSArray* expect = @[date1, date2];
  XCTAssertEqualObjects(result, expect, @"result should be valid");
}


//10/16-10/16, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
//join at 2013/10/16 10:22:34-0700
- (void)testNextDatesForFixedDailyExperimentRepeatEveryOneDay {
  NSDate* joinTime = [PacoDateUtility pacoDateForString:@"2013/10/16 10:22:34-0700"];
  [self setupExperimentWithJsonName:experimentFixedEveryTwoDays
                           joinTime:joinTime];
  [self setupRepeatRate:1];
  
  NSDate* fromDate = [joinTime dateByAddingTimeInterval:5];
  NSArray* result = [PacoScheduleGenerator nextDatesForExperiment:self.experiment
                                                       numOfDates:4
                                                         fromDate:fromDate];
  NSDate* date1 = [PacoDateUtility pacoDateForString:@"2013/10/16 17:00:00-0700"];
  NSArray* expect = @[date1];
  XCTAssertEqualObjects(result, expect, @"result should be valid");
}


//10/16-10/16, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
//join at 2013/10/16 17:22:34-0700
- (void)testNextDatesWhenJoiningLate {
  NSDate* joinTime = [PacoDateUtility pacoDateForString:@"2013/10/16 17:22:34-0700"];
  [self setupExperimentWithJsonName:experimentFixedEveryTwoDays
                           joinTime:joinTime];
  [self setupRepeatRate:1];
  
  NSDate* fromDate = [joinTime dateByAddingTimeInterval:5];
  NSArray* result = [PacoScheduleGenerator nextDatesForExperiment:self.experiment
                                                       numOfDates:4
                                                         fromDate:fromDate];
  NSArray* expect = @[];
  XCTAssertEqualObjects(result, expect, @"result should be valid");
}


//10/16-10/21, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
//join at 2013/10/14 10:22:34-0700
- (void)testNextDatesWhenJoinedBeforeExperimentStarts {
  NSDate* joinTime = [PacoDateUtility pacoDateForString:@"2013/10/14 10:22:34-0700"];
  [self setupExperimentWithJsonName:experimentFixedEveryTwoDays
                           joinTime:joinTime];
  [self setupEndDate:@"2013/10/21"];

  NSDate* fromDate = [joinTime dateByAddingTimeInterval:5];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSArray* result = [PacoScheduleGenerator nextDatesForExperiment:self.experiment
                                                       numOfDates:60
                                                         fromDate:fromDate];
  
  NSDate* date1 = [PacoDateUtility pacoDateForString:@"2013/10/16 09:00:00-0700"];
  NSDate* date2 = [PacoDateUtility pacoDateForString:@"2013/10/16 17:00:00-0700"];
  NSDate* date3 = [PacoDateUtility pacoDateForString:@"2013/10/18 09:00:00-0700"];
  NSDate* date4 = [PacoDateUtility pacoDateForString:@"2013/10/18 17:00:00-0700"];
  NSDate* date5 = [PacoDateUtility pacoDateForString:@"2013/10/20 09:00:00-0700"];
  NSDate* date6 = [PacoDateUtility pacoDateForString:@"2013/10/20 17:00:00-0700"];
  NSArray* expect = @[date1, date2, date3, date4, date5, date6];
  XCTAssertEqualObjects(result, expect, @"result should be valid");
}

//10/16-10/21, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
//join at 2013/10/16 18:00:00-0700
- (void)testNextDatesWhenJoinedLateTheExperimentStartDay {
  NSDate* joinTime = [PacoDateUtility pacoDateForString:@"2013/10/16 18:00:00-0700"];
  [self setupExperimentWithJsonName:experimentFixedEveryTwoDays
                           joinTime:joinTime];
  [self setupEndDate:@"2013/10/21"];
  
  NSDate* fromDate = [joinTime dateByAddingTimeInterval:5];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSArray* result = [PacoScheduleGenerator nextDatesForExperiment:self.experiment
                                                       numOfDates:60
                                                         fromDate:fromDate];
  
  NSDate* date3 = [PacoDateUtility pacoDateForString:@"2013/10/18 09:00:00-0700"];
  NSDate* date4 = [PacoDateUtility pacoDateForString:@"2013/10/18 17:00:00-0700"];
  NSDate* date5 = [PacoDateUtility pacoDateForString:@"2013/10/20 09:00:00-0700"];
  NSDate* date6 = [PacoDateUtility pacoDateForString:@"2013/10/20 17:00:00-0700"];
  NSArray* expect = @[date3, date4, date5, date6];
  XCTAssertEqualObjects(result, expect, @"result should be valid");
}


//10/16-10/21, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
//join at 2013/10/17 08:00:00-0700
- (void)testNextDatesWhenJoinedNextMorningOfTheExperimentStartDay {
  NSDate* joinTime = [PacoDateUtility pacoDateForString:@"2013/10/17 08:00:00-0700"];
  [self setupExperimentWithJsonName:experimentFixedEveryTwoDays
                           joinTime:joinTime];
  [self setupEndDate:@"2013/10/21"];
  
  NSDate* fromDate = [joinTime dateByAddingTimeInterval:5];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSArray* result = [PacoScheduleGenerator nextDatesForExperiment:self.experiment
                                                       numOfDates:60
                                                         fromDate:fromDate];
  NSDate* date3 = [PacoDateUtility pacoDateForString:@"2013/10/18 09:00:00-0700"];
  NSDate* date4 = [PacoDateUtility pacoDateForString:@"2013/10/18 17:00:00-0700"];
  NSDate* date5 = [PacoDateUtility pacoDateForString:@"2013/10/20 09:00:00-0700"];
  NSDate* date6 = [PacoDateUtility pacoDateForString:@"2013/10/20 17:00:00-0700"];
  NSArray* expect = @[date3, date4, date5, date6];
  XCTAssertEqualObjects(result, expect, @"result should be valid");
}


//10/16-10/21, Daily, Repeat every 2 day, timeout 479 minutes
//9am, 5pm
//join at 2013/10/21 08:00:00-0700
- (void)testNextDatesWhenJoinedEarlyTheLastDay {
  NSDate* joinTime = [PacoDateUtility pacoDateForString:@"2013/10/21 08:00:00-0700"];
  [self setupExperimentWithJsonName:experimentFixedEveryTwoDays
                           joinTime:joinTime];
  [self setupEndDate:@"2013/10/21"];
  
  NSDate* fromDate = [joinTime dateByAddingTimeInterval:5];
  XCTAssertNotNil(fromDate, @"fromDate should be valid");
  NSArray* result = [PacoScheduleGenerator nextDatesForExperiment:self.experiment
                                                       numOfDates:60
                                                         fromDate:fromDate];
  XCTAssertEqualObjects(result, @[], @"result should be valid");
}



@end
