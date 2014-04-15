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
#import <XCTest/XCTest.h>



/*
 ESM: startDate:11/5/13 endDate:11/12/13
 3 times per day, doesn't include weekend, 9:30am - 5:30pm
 timeout: 479 minutes, minimumBuffer: 120 minutes
 **/
static NSString* testDefinitionJson = @"{\"title\":\"Notification - ESM Daily\",\"description\":\"te\",\"informedConsentForm\":\"This is beer rating data for your personal use.\\n\\nIf you want to share results and see others' ratings email me, bobevans999@gmail.com, and I will share the results of those who want to share with others who express interest.\\n\\nIf you want to share results and see others' ratings email me, bobevans999@gmail.com, and I will share the results of those who want to share with others who express interest.\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":true,\"startDate\":\"2013/11/05\",\"endDate\":\"2013/11/12\",\"id\":10948007,\"questionsChange\":false,\"modifyDate\":\"2013/09/05\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"hello\",\"mandatory\":false,\"responseType\":\"likert\",\"likertSteps\":9,\"leftSideLabel\":\"q\",\"rightSideLabel\":\"f\",\"name\":\"hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":28001,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":29,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":479,\"minimumBuffer\":120,\"id\":1,\"scheduleType\":4,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":34200000,\"esmEndHour\":63000000,\"times\":[46800000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":479,\"minimumBuffer\":120,\"id\":1,\"scheduleType\":4,\"esmFrequency\":3,\"esmPeriodInDays\":0,\"esmStartHour\":34200000,\"esmEndHour\":63000000,\"times\":[46800000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";


@interface PacoESMDailyGeneratorWithCache : XCTestCase
@property(nonatomic, strong) NSDateComponents* comp;
@property(nonatomic, strong) NSCalendar* calendar;
@property(nonatomic, strong) PacoExperiment* testExperiment;
@end

@implementation PacoESMDailyGeneratorWithCache

- (void)setUp {
  [super setUp];
  
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
  
  self.testExperiment = [PacoExperiment experimentWithDefinition:definition
                                                        schedule:definition.schedule
                                                        joinTime:nil];
}

- (void)tearDown {
  self.comp = nil;
  self.calendar = nil;
  self.testExperiment = nil;
  [super tearDown];
}

- (NSDate*)dateFromComponentsWithYear:(NSInteger)year
                                month:(NSInteger)month
                                  day:(NSInteger)day
                                 hour:(NSInteger)hour
                               minute:(NSInteger)minute
                               second:(NSInteger)second {
  [self.comp setYear:year];
  [self.comp setMonth:month];
  [self.comp setDay:day];
  [self.comp setHour:hour];
  [self.comp setMinute:minute];
  [self.comp setSecond:second];
  return [self.calendar dateFromComponents:self.comp];
}


#pragma mark Fixed-length
/*
 ESM: 8 days - startDate:11/5/13 endDate:11/12/13
 3 times per day, doesn't include weekend, 9:30am - 5:30pm
 timeout: 479 minutes, minimumBuffer: 120 minutes
 **/
- (void)testJoinBeforeExperimentStartsWithoutWeekends {
  //joinTime: 2013 11/01 09:35:50
  NSDate* joinTime = [self dateFromComponentsWithYear:2013
                                                month:11
                                                  day:1
                                                 hour:9
                                               minute:35
                                               second:50];
  [self.testExperiment setValue:joinTime forKey:@"joinTime"];
  
  NSDate* fromDate = [joinTime dateByAddingTimeInterval:4];
  NSArray* initialDates = [PacoScheduleGenerator nextDatesForExperiment:self.testExperiment
                                                             numOfDates:60
                                                               fromDate:fromDate];
  XCTAssertEqual([initialDates count], (8 - 2) * 3, @"should generate 18 dates for 8 days without weekends");
  XCTAssertEqualObjects([self.testExperiment.schedule esmScheduleList], initialDates, @"esm dates should be stored in the schedule");
  
  //new fromDate: 2013 11/02 09:35:50
  fromDate = [self dateFromComponentsWithYear:2013
                                        month:11
                                          day:2
                                         hour:9
                                       minute:35
                                       second:50];
  
  NSArray* newDates = [PacoScheduleGenerator nextDatesForExperiment:self.testExperiment
                                                         numOfDates:60
                                                           fromDate:fromDate];
  XCTAssertEqualObjects(newDates, initialDates, @"should return the dates stored inside esmScheduleList");
  XCTAssertEqualObjects([self.testExperiment.schedule esmScheduleList], newDates, @"esmScheduleList should stay the same");
  
  
  //new fromDate: 2013 11/06 08:35:50
  fromDate = [self dateFromComponentsWithYear:2013
                                        month:11
                                          day:6
                                         hour:8
                                       minute:35
                                       second:50];
  
  NSArray* datesOnDayTwo = [PacoScheduleGenerator nextDatesForExperiment:self.testExperiment
                                                              numOfDates:60
                                                                fromDate:fromDate];
  XCTAssertEqual([datesOnDayTwo count], (8 - 2) * 3 - 3, @"should return 15 dates");
  NSIndexSet* set = [NSIndexSet indexSetWithIndexesInRange:NSMakeRange(3, (8 - 2) * 3 - 3)];
  XCTAssertEqualObjects([newDates objectsAtIndexes:set], datesOnDayTwo, @"datesOnDayTwo should be a sub-array of previously generated dates");
  XCTAssertEqualObjects([self.testExperiment.schedule esmScheduleList], datesOnDayTwo, @"esmScheduleList should be updated");
  
  
  //new fromDate: 2013 11/12 16:00:00
  fromDate = [self dateFromComponentsWithYear:2013
                                        month:11
                                          day:12
                                         hour:16
                                       minute:0
                                       second:0];
  
  NSArray* datesOnLastDayAtFourPM = [PacoScheduleGenerator nextDatesForExperiment:self.testExperiment
                                                                       numOfDates:60
                                                                         fromDate:fromDate];
  for (NSDate* date in datesOnLastDayAtFourPM) {
    XCTAssertTrue([datesOnDayTwo containsObject:date], @"every date should be from the previously generated esm list");
  }
  XCTAssertTrue([self.testExperiment.schedule.esmScheduleList count] > 0, @"the esmScheduleList should at leat keep the last generated date");
  
  
  //new fromDate: 2013 11/12 16:30:00
  fromDate = [self dateFromComponentsWithYear:2013
                                        month:11
                                          day:12
                                         hour:16
                                       minute:30
                                       second:0];
  
  NSArray* datesOnLastDayAtFourThirtyPM = [PacoScheduleGenerator nextDatesForExperiment:self.testExperiment
                                                                             numOfDates:60
                                                                               fromDate:fromDate];
  for (NSDate* date in datesOnLastDayAtFourThirtyPM) {
    XCTAssertTrue([initialDates containsObject:date], @"every date should be from the originally generated esm list");
  }
  XCTAssertTrue([self.testExperiment.schedule.esmScheduleList count] > 0, @"the esmScheduleList should at leat keep the last generated date");
  
  
  //new fromDate: 2013 11/12 17:00:00
  fromDate = [self dateFromComponentsWithYear:2013
                                        month:11
                                          day:12
                                         hour:17
                                       minute:0
                                       second:0];
  
  NSArray* datesOnLastDayAtFivePM = [PacoScheduleGenerator nextDatesForExperiment:self.testExperiment
                                                                       numOfDates:60
                                                                         fromDate:fromDate];
  for (NSDate* date in datesOnLastDayAtFivePM) {
    XCTAssertTrue([initialDates containsObject:date], @"every date should be from the originally generated esm list");
  }
  XCTAssertTrue([self.testExperiment.schedule.esmScheduleList count] > 0, @"the esmScheduleList should at leat keep the last generated date");
  
  
  //new fromDate: 2013 11/12 17:35:00
  fromDate = [self dateFromComponentsWithYear:2013
                                        month:11
                                          day:12
                                         hour:17
                                       minute:35
                                       second:0];
  
  NSArray* datesOnLastDayAfterEndTime = [PacoScheduleGenerator nextDatesForExperiment:self.testExperiment
                                                                           numOfDates:60
                                                                             fromDate:fromDate];
  XCTAssertEqual([datesOnLastDayAfterEndTime count], 0, @"should return 0 dates since esm end time is 5:30pm");
  XCTAssertEqual([self.testExperiment.schedule.esmScheduleList count], 1, @"the esmScheduleList should keep the last generated date");
}

@end
