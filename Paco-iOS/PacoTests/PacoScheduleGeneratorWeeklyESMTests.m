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
#import "PacoScheduleGenerator+ESM.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"
#import "PacoDateUtility.h"
#import "NSDate+Paco.h"

@interface PacoScheduleGenerator ()
+ (NSArray*)datesToScheduleForESMExperiment:(PacoExperiment*)experiment
                                 numOfDates:(NSInteger)numOfDates
                                   fromDate:(NSDate*)fromDate;
+ (NSDate*)esmCycleStartDateForSchedule:(PacoExperimentSchedule*)schedule
                    experimentStartDate:(NSDate*)experimentStartDate
                      experimentEndDate:(NSDate*)experimentEndDate
                               fromDate:(NSDate*)fromDate;
+ (NSDate*)nextCycleStartDateForSchedule:(PacoExperimentSchedule*)schedule
                     experimentStartDate:(NSDate*)experimentStartDate
                       experimentEndDate:(NSDate*)experimentEndDate
                          cycleStartDate:(NSDate*)currentStartDate;
+ (NSArray*)generateESMDatesForExperiment:(PacoExperiment*)experiment
                        minimumNumOfDates:(NSUInteger)minimumNumOfDates
                                 fromDate:(NSDate*)fromDate;
+ (NSArray *)createESMScheduleDates:(PacoExperimentSchedule*)experimentSchedule
                     cycleStartDate:(NSDate*)cycleStartDate
                           fromDate:(NSDate*)fromDate
                  experimentEndDate:(NSDate*)experimentEndDate;

@end


/*
 ESM: startDate:11/4/13 endDate:12/1/13
 3 times per week, doesn't include weekend, 9:00am - 5:00pm
 timeout: 59 minutes, minimumBuffer: 1440 minutes = 24 hours
 **/
static NSString* testDefinitionJson = @"{\"title\":\"Notification ESM Weekly\",\"description\":\"fdf\",\"informedConsentForm\":\"fda\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":true,\"startDate\":\"2013/11/04\",\"endDate\":\"2013/12/01\",\"id\":12071005,\"questionsChange\":false,\"modifyDate\":\"2013/10/11\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"hello\",\"mandatory\":true,\"responseType\":\"likert\",\"likertSteps\":5,\"leftSideLabel\":\"fda\",\"rightSideLabel\":\"fd\",\"name\":\"Hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":12001,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":4,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"minimumBuffer\":1440,\"id\":1,\"scheduleType\":4,\"esmFrequency\":3,\"esmPeriodInDays\":1,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[36000000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"minimumBuffer\":1440,\"id\":1,\"scheduleType\":4,\"esmFrequency\":3,\"esmPeriodInDays\":1,\"esmStartHour\":32400000,\"esmEndHour\":61200000,\"times\":[36000000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";

@interface PacoScheduleGeneratorWeeklyESMTests : XCTestCase
@property(nonatomic, strong) NSDateComponents* comp;
@property(nonatomic, strong) NSCalendar* calendar;
@property(nonatomic, strong) PacoExperiment* testExperiment;
@end

@implementation PacoScheduleGeneratorWeeklyESMTests


- (void)setUp {
  [super setUp];
  // Put setup code here; it will be run once, before the first test case.
  self.comp = [[NSDateComponents alloc] init];
  NSTimeZone* timeZone = [NSTimeZone timeZoneWithName:@"US/Pacific"];
  XCTAssertNotNil(timeZone, @"timezone should be valid");
  [self.comp setTimeZone:timeZone];
  
  self.calendar = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
  
  NSError* error = nil;
  NSData* data = [testDefinitionJson dataUsingEncoding:NSUTF8StringEncoding];
  id definitionDict = [NSJSONSerialization JSONObjectWithData:data
                                                      options:NSJSONReadingAllowFragments
                                                        error:&error];
  XCTAssertTrue(error == nil && [definitionDict isKindOfClass:[NSDictionary class]],
               @"esmExperimentTemplate should be successfully serialized!");
  PacoExperimentDefinition* definition = [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:definitionDict];
  XCTAssertTrue(definition != nil, @"definition should not be nil!");
  
  PacoExperiment* experimentInstance = [[PacoExperiment alloc] init];
  experimentInstance.schedule = definition.schedule;
  experimentInstance.definition = definition;
  experimentInstance.instanceId = definition.experimentId;
  self.testExperiment = experimentInstance;
}

- (void)tearDown {
  // Put teardown code here; it will be run once, after the last test case.
  self.comp = nil;
  self.calendar = nil;
  self.testExperiment = nil;
  [super tearDown];
}

/*
 ESM: startDate:11/4/13 endDate:12/1/13
 3 times per week, doesn't include weekend, 9:00am - 5:00pm
 timeout: 59 minutes, minimumBuffer: 1440 minutes = 24 hours
 **/
- (void)testWeeklyDatesToScheduleFixedLengthNoWeekends {
  [self.comp setYear:2013];
  [self.comp setMonth:11];
  [self.comp setDay:1];
  [self.comp setHour:9];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //fromDate: 11/1, 9:35:50, 2013, earlier that experiment start date of 11/4/13
  NSDate* fromDate = [self.calendar dateFromComponents:self.comp];
  NSArray* dates = [PacoScheduleGenerator datesToScheduleForESMExperiment:self.testExperiment
                                                               numOfDates:60
                                                                 fromDate:fromDate];
  int numOfWeeks = 4;
  XCTAssertEqual((int)[dates count], 3 * numOfWeeks,
                 @"should generate correct number of dates in total");
  
  NSDate* experimentStartDate = [self.testExperiment startDate];
  NSDate* experimentEndDate = [self.testExperiment endDate];
  
  [self.comp setYear:2013];
  [self.comp setMonth:11];
  [self.comp setDay:4];
  [self.comp setHour:9];
  [self.comp setMinute:0];
  [self.comp setSecond:0];
  //startTime: 11/4, 9am, 2013
  NSDate* cycleStartDate = [self.calendar dateFromComponents:self.comp];
  
  NSDateComponents* compOfFirstBucket =  [[NSDateComponents alloc] init];
  compOfFirstBucket.day = 1;
  compOfFirstBucket.minute = 320;
  NSDateComponents* compOfSecondBucket =  [[NSDateComponents alloc] init];
  compOfSecondBucket.day = 3;
  compOfSecondBucket.minute = 160;
  
  NSDateComponents* compOfStartTime = [[NSDateComponents alloc] init];
  [compOfStartTime setHour:9];
  NSDateComponents* compOfEndTime = [[NSDateComponents alloc] init];
  [compOfEndTime setHour:17];
  
  for (int weekIndex=0; weekIndex<numOfWeeks; weekIndex++) {
    NSDate* first = dates[(weekIndex*3 + 0)];
    NSDate* second = dates[(weekIndex*3 + 1)];
    NSDate* third = dates[(weekIndex*3 + 2)];
    
    XCTAssertTrue([first pacoLaterThanDate:experimentStartDate] &&
                 [first pacoEarlierThanDate:experimentEndDate] &&
                 [second pacoLaterThanDate:experimentStartDate] &&
                 [second pacoEarlierThanDate:experimentEndDate] &&
                 [third pacoLaterThanDate:experimentStartDate] &&
                 [third pacoEarlierThanDate:experimentEndDate], @"should be valid");
    XCTAssertTrue([first pacoEarlierThanDate:second] &&
                 [second pacoEarlierThanDate:third], @"should be sorted");

    XCTAssertTrue(![first pacoIsWeekend] &&
                 ![second pacoIsWeekend] &&
                 ![third pacoIsWeekend], @"shouldn't be weekend");
    
    NSDate* upperBoundOfFirstBucket = [self.calendar dateByAddingComponents:compOfFirstBucket
                                                                     toDate:cycleStartDate
                                                                    options:0];
    NSDate* upperBoundOfSecondBucket = [self.calendar dateByAddingComponents:compOfSecondBucket
                                                                      toDate:cycleStartDate
                                                                     options:0];
    XCTAssertTrue([first pacoLaterThanDate:cycleStartDate] &&
                 [first pacoNoLaterThanDate:upperBoundOfFirstBucket], @"should be in bucket");
    XCTAssertTrue([second pacoLaterThanDate:upperBoundOfFirstBucket] &&
                 [second pacoNoLaterThanDate:upperBoundOfSecondBucket], @"should be in bucket");
    XCTAssertTrue([third pacoLaterThanDate:upperBoundOfSecondBucket], @"should be in bucket");
    
    NSDate* midnight = [first pacoCurrentDayAtMidnight];
    NSDate* startTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfStartTime
                                                                    toDate:midnight
                                                                   options:0];
    NSDate* endTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfEndTime
                                                                  toDate:midnight
                                                                 options:0];
    XCTAssertTrue([first pacoLaterThanDate:startTimeForCurrentDay] &&
                 [first pacoEarlierThanDate:endTimeForCurrentDay], @"should be valid");
    
    midnight = [second pacoCurrentDayAtMidnight];
    startTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfStartTime
                                                            toDate:midnight
                                                           options:0];
    endTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfEndTime
                                                          toDate:midnight
                                                         options:0];
    XCTAssertTrue([second pacoLaterThanDate:startTimeForCurrentDay] &&
                 [second pacoEarlierThanDate:endTimeForCurrentDay], @"should be valid");

    midnight = [third pacoCurrentDayAtMidnight];
    startTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfStartTime
                                                            toDate:midnight
                                                           options:0];
    endTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfEndTime
                                                          toDate:midnight
                                                         options:0];
    XCTAssertTrue([third pacoLaterThanDate:startTimeForCurrentDay] &&
                 [third pacoEarlierThanDate:endTimeForCurrentDay], @"should be valid");

    cycleStartDate = [cycleStartDate pacoDateByAddingDayInterval:7];
  }
}


/*
 ESM: startDate:11/4/13 endDate:12/1/13
 3 times per week, include weekend, 9:00am - 5:00pm
 timeout: 59 minutes, minimumBuffer: 1440 minutes = 24 hours
 **/
- (void)testWeeklyDatesToScheduleFixedLengthWeekends {
  //change schedule to include weekends
  self.testExperiment.schedule.esmWeekends = YES;

  [self.comp setYear:2013];
  [self.comp setMonth:11];
  [self.comp setDay:1];
  [self.comp setHour:9];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //fromDate: 11/1, 9:35:50, 2013, earlier that experiment start date of 11/4/13
  NSDate* fromDate = [self.calendar dateFromComponents:self.comp];
  NSArray* dates = [PacoScheduleGenerator datesToScheduleForESMExperiment:self.testExperiment
                                                               numOfDates:60
                                                                 fromDate:fromDate];
  int numOfWeeks = 4;
  XCTAssertEqual((int)[dates count], 3 * numOfWeeks,
                 @"should generate correct number of dates in total");
  
  NSDate* experimentStartDate = [self.testExperiment startDate];
  NSDate* experimentEndDate = [self.testExperiment endDate];
  
  [self.comp setYear:2013];
  [self.comp setMonth:11];
  [self.comp setDay:4];
  [self.comp setHour:9];
  [self.comp setMinute:0];
  [self.comp setSecond:0];
  //startTime: 11/4, 9am, 2013
  NSDate* cycleStartDate = [self.calendar dateFromComponents:self.comp];
  
  NSDateComponents* compOfFirstBucket =  [[NSDateComponents alloc] init];
  compOfFirstBucket.day = 2;
  compOfFirstBucket.minute = 160;
  NSDateComponents* compOfSecondBucket =  [[NSDateComponents alloc] init];
  compOfSecondBucket.day = 4;
  compOfSecondBucket.minute = 320;
  
  NSDateComponents* compOfStartTime = [[NSDateComponents alloc] init];
  [compOfStartTime setHour:9];
  NSDateComponents* compOfEndTime = [[NSDateComponents alloc] init];
  [compOfEndTime setHour:17];
  
  for (int weekIndex=0; weekIndex<numOfWeeks; weekIndex++) {
    NSDate* first = dates[(weekIndex*3 + 0)];
    NSDate* second = dates[(weekIndex*3 + 1)];
    NSDate* third = dates[(weekIndex*3 + 2)];
    
    XCTAssertTrue([first pacoEarlierThanDate:second] &&
                 [second pacoEarlierThanDate:third], @"should be sorted");

    XCTAssertTrue([first pacoLaterThanDate:experimentStartDate] &&
                 [first pacoEarlierThanDate:experimentEndDate] &&
                 [second pacoLaterThanDate:experimentStartDate] &&
                 [second pacoEarlierThanDate:experimentEndDate] &&
                 [third pacoLaterThanDate:experimentStartDate] &&
                 [third pacoEarlierThanDate:experimentEndDate], @"should be valid");
    
    NSDate* upperBoundOfFirstBucket = [self.calendar dateByAddingComponents:compOfFirstBucket
                                                                     toDate:cycleStartDate
                                                                    options:0];
    NSDate* upperBoundOfSecondBucket = [self.calendar dateByAddingComponents:compOfSecondBucket
                                                                      toDate:cycleStartDate
                                                                     options:0];
    XCTAssertTrue([first pacoLaterThanDate:cycleStartDate] &&
                 [first pacoNoLaterThanDate:upperBoundOfFirstBucket], @"should be in bucket");
    XCTAssertTrue([second pacoLaterThanDate:upperBoundOfFirstBucket] &&
                 [second pacoNoLaterThanDate:upperBoundOfSecondBucket], @"should be in bucket");
    XCTAssertTrue([third pacoLaterThanDate:upperBoundOfSecondBucket], @"should be in bucket");
    
    NSDate* midnight = [first pacoCurrentDayAtMidnight];
    NSDate* startTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfStartTime
                                                                    toDate:midnight
                                                                   options:0];
    NSDate* endTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfEndTime
                                                                  toDate:midnight
                                                                 options:0];
    XCTAssertTrue([first pacoLaterThanDate:startTimeForCurrentDay] &&
                 [first pacoEarlierThanDate:endTimeForCurrentDay], @"should be valid");
    
    midnight = [second pacoCurrentDayAtMidnight];
    startTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfStartTime
                                                            toDate:midnight
                                                           options:0];
    endTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfEndTime
                                                          toDate:midnight
                                                         options:0];
    XCTAssertTrue([second pacoLaterThanDate:startTimeForCurrentDay] &&
                 [second pacoEarlierThanDate:endTimeForCurrentDay], @"should be valid");
    
    midnight = [third pacoCurrentDayAtMidnight];
    startTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfStartTime
                                                            toDate:midnight
                                                           options:0];
    endTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfEndTime
                                                          toDate:midnight
                                                         options:0];
    XCTAssertTrue([third pacoLaterThanDate:startTimeForCurrentDay] &&
                 [third pacoEarlierThanDate:endTimeForCurrentDay], @"should be valid");
    
    cycleStartDate = [cycleStartDate pacoDateByAddingDayInterval:7];
  }
}


/*
 ESM: startDate:11/4/13 endDate:12/1/13
 3 times per week, doesn't include weekend, 9:00am - 5:00pm
 timeout: 59 minutes, minimumBuffer: 1440 minutes = 24 hours
 **/
- (void)testWeeklyDatesToScheduleFixedLengthNoWeekendsStartInMiddle {
  [self.comp setYear:2013];
  [self.comp setMonth:11];
  [self.comp setDay:8];
  [self.comp setHour:17];
  [self.comp setMinute:35];
  [self.comp setSecond:50];
  //fromDate: 11/8, 17:35:50, 2013, later than experiment start date of 11/4/13
  NSDate* fromDate = [self.calendar dateFromComponents:self.comp];
  NSArray* dates = [PacoScheduleGenerator datesToScheduleForESMExperiment:self.testExperiment
                                                               numOfDates:60
                                                                 fromDate:fromDate];
  int numOfWeeks = 3;
  XCTAssertEqual((int)[dates count], 3 * numOfWeeks,
                 @"should generate correct number of dates in total");
  
  NSDate* experimentStartDate = [self.testExperiment startDate];
  NSDate* experimentEndDate = [self.testExperiment endDate];
  
  [self.comp setYear:2013];
  [self.comp setMonth:11];
  [self.comp setDay:4];
  [self.comp setHour:9];
  [self.comp setMinute:0];
  [self.comp setSecond:0];
  //startTime: 11/4, 9am, 2013
  NSDate* cycleStartDate = [self.calendar dateFromComponents:self.comp];
  
  NSDateComponents* compOfFirstBucket =  [[NSDateComponents alloc] init];
  compOfFirstBucket.day = 1;
  compOfFirstBucket.minute = 320;
  NSDateComponents* compOfSecondBucket =  [[NSDateComponents alloc] init];
  compOfSecondBucket.day = 3;
  compOfSecondBucket.minute = 160;
  
  NSDateComponents* compOfStartTime = [[NSDateComponents alloc] init];
  [compOfStartTime setHour:9];
  NSDateComponents* compOfEndTime = [[NSDateComponents alloc] init];
  [compOfEndTime setHour:17];
  
  for (int weekIndex=0; weekIndex<numOfWeeks; weekIndex++) {
    NSDate* first = dates[(weekIndex*3 + 0)];
    NSDate* second = dates[(weekIndex*3 + 1)];
    NSDate* third = dates[(weekIndex*3 + 2)];
    XCTAssertTrue([first pacoEarlierThanDate:second] &&
                 [second pacoEarlierThanDate:third], @"should be sorted");
    
    XCTAssertTrue([first pacoLaterThanDate:experimentStartDate] &&
                 [first pacoEarlierThanDate:experimentEndDate] &&
                 [second pacoLaterThanDate:experimentStartDate] &&
                 [second pacoEarlierThanDate:experimentEndDate] &&
                 [third pacoLaterThanDate:experimentStartDate] &&
                 [third pacoEarlierThanDate:experimentEndDate], @"should be valid");
    XCTAssertTrue(![first pacoIsWeekend] &&
                 ![second pacoIsWeekend] &&
                 ![third pacoIsWeekend], @"shouldn't be weekend");
    
    cycleStartDate = [cycleStartDate pacoDateByAddingDayInterval:7];
    NSDate* upperBoundOfFirstBucket = [self.calendar dateByAddingComponents:compOfFirstBucket
                                                                     toDate:cycleStartDate
                                                                    options:0];
    NSDate* upperBoundOfSecondBucket = [self.calendar dateByAddingComponents:compOfSecondBucket
                                                                      toDate:cycleStartDate
                                                                     options:0];
    XCTAssertTrue([first pacoLaterThanDate:cycleStartDate] &&
                 [first pacoNoLaterThanDate:upperBoundOfFirstBucket], @"should be in bucket");
    XCTAssertTrue([second pacoLaterThanDate:upperBoundOfFirstBucket] &&
                 [second pacoNoLaterThanDate:upperBoundOfSecondBucket], @"should be in bucket");
    XCTAssertTrue([third pacoLaterThanDate:upperBoundOfSecondBucket], @"should be in bucket");
    
    NSDate* midnight = [first pacoCurrentDayAtMidnight];
    NSDate* startTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfStartTime
                                                                    toDate:midnight
                                                                   options:0];
    NSDate* endTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfEndTime
                                                                  toDate:midnight
                                                                 options:0];
    XCTAssertTrue([first pacoLaterThanDate:startTimeForCurrentDay] &&
                 [first pacoEarlierThanDate:endTimeForCurrentDay], @"should be valid");
    
    midnight = [second pacoCurrentDayAtMidnight];
    startTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfStartTime
                                                            toDate:midnight
                                                           options:0];
    endTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfEndTime
                                                          toDate:midnight
                                                         options:0];
    XCTAssertTrue([second pacoLaterThanDate:startTimeForCurrentDay] &&
                 [second pacoEarlierThanDate:endTimeForCurrentDay], @"should be valid");
    
    midnight = [third pacoCurrentDayAtMidnight];
    startTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfStartTime
                                                            toDate:midnight
                                                           options:0];
    endTimeForCurrentDay = [self.calendar dateByAddingComponents:compOfEndTime
                                                          toDate:midnight
                                                         options:0];
    XCTAssertTrue([third pacoLaterThanDate:startTimeForCurrentDay] &&
                 [third pacoEarlierThanDate:endTimeForCurrentDay], @"should be valid");
  }
}



@end
