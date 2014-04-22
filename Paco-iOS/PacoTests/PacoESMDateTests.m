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

#import "PacoESMDateTests.h"
#import "PacoExperimentSchedule.h"
#import "PacoDateUtility.h"
#import "PacoExperiment.h"
#import "PacoExperimentDefinition.h"
#import "PacoScheduleGenerator.h"
#import "PacoUtility.h"

//ESM per day, doesn't include weekends
//minimumBuffer: 5 min
//timeout: 50 min
//esmFrequency: 10
//esmStartHour: 4pm, 57600000
//esmEndHour: 5pm, 61200000
static NSString* esmScheduleTemplate = @"{\"type\":\"signalSchedule\",\"timeout\":50,\"minimumBuffer\":5,\"id\":1,\"scheduleType\":4,\"esmFrequency\":10,\"esmPeriodInDays\":0,\"esmStartHour\":57600000,\"esmEndHour\":61200000,\"times\":[46800000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}";


//ESM per day, doesn't include weekends
//minimumBuffer: 5 min
//timeout: 50 min
//esmFrequency: 10
//esmStartHour: 4pm, 57600000
//esmEndHour: 5pm, 61200000
static NSString* esmExperimentTemplate = @"{\"title\":\"Notification - ESM Test\",\"description\":\"te\",\"informedConsentForm\":\"test\",\"creator\":\"ymggtest@gmail.com\",\"fixedDuration\":false,\"id\":10948007,\"questionsChange\":false,\"modifyDate\":\"2013/09/05\",\"inputs\":[{\"id\":3,\"questionType\":\"question\",\"text\":\"hello\",\"mandatory\":false,\"responseType\":\"likert\",\"likertSteps\":5,\"leftSideLabel\":\"q\",\"rightSideLabel\":\"f\",\"name\":\"hello\",\"conditional\":false,\"listChoices\":[],\"invisibleInput\":false}],\"feedback\":[{\"id\":9001,\"feedbackType\":\"display\",\"text\":\"Thanks for Participating!\"}],\"published\":false,\"deleted\":false,\"webRecommended\":false,\"version\":10,\"signalingMechanisms\":[{\"type\":\"signalSchedule\",\"timeout\":50,\"minimumBuffer\":5,\"id\":1,\"scheduleType\":4,\"esmFrequency\":10,\"esmPeriodInDays\":0,\"esmStartHour\":57600000,\"esmEndHour\":61200000,\"times\":[46800000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}],\"schedule\":{\"type\":\"signalSchedule\",\"timeout\":50,\"minimumBuffer\":5,\"id\":1,\"scheduleType\":4,\"esmFrequency\":10,\"esmPeriodInDays\":0,\"esmStartHour\":57600000,\"esmEndHour\":61200000,\"times\":[46800000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}}";

@interface PacoScheduleGenerator ()
+ (NSArray *)createESMScheduleDates:(PacoExperimentSchedule*)experimentSchedule
                       fromThisDate:(NSDate*)fromThisDate;
+ (NSDate *)nextScheduledDateForExperiment:(PacoExperiment *)experiment
                              fromThisDate:(NSDate *)fromThisDate;
@end


@interface PacoESMDateTests ()
@property(nonatomic, retain) PacoExperimentSchedule* esmSchedule;
@property(nonatomic, retain) PacoExperiment* esmExperiment;
@property(nonatomic, retain) NSDate* fromDate;
@property(nonatomic, retain) NSDate* esmStartDate;
@end


@implementation PacoESMDateTests
- (void)setUp {
  [super setUp];
  
  NSError* error = nil;
  NSData* data = [esmScheduleTemplate dataUsingEncoding:NSUTF8StringEncoding];
  id scheduleDict = [NSJSONSerialization JSONObjectWithData:data
                                                    options:NSJSONReadingAllowFragments
                                                      error:&error];
  XCTAssertTrue(error == nil && [scheduleDict isKindOfClass:[NSDictionary class]],
               @"esmScheduleTemplate should be successfully serialized!");
  PacoExperimentSchedule* schedule = [PacoExperimentSchedule pacoExperimentScheduleFromJSON:scheduleDict];
  XCTAssertTrue(schedule != nil, @"schedule should not be nil!");
  self.esmSchedule = schedule;
  
  
  data = [esmExperimentTemplate dataUsingEncoding:NSUTF8StringEncoding];
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
  self.esmExperiment = experimentInstance;
}

- (void)tearDown {
  self.esmSchedule = nil;
  self.esmExperiment = nil;
  self.fromDate = nil;
  self.esmStartDate = nil;
  
  [super tearDown];
}

//generic test
- (void)genericTestESMDates:(NSArray*)dates
               esmFrequency:(int)esmFrequency
              minimumBuffer:(int)minimumBuffer
           minutesPerBucket:(double)minutesPerBucket
                   fromDate:(NSDate*)fromDate
               esmStartDate:(NSDate*)esmStartDate
             shouldPrintLog:(BOOL)shouldPrintLog{
  XCTAssertTrue([dates count] == esmFrequency, @"dates's count should be equal to esmFrequency");
  
  NSDate* previous = dates[0];
  
  NSTimeInterval intervalFromStart = [previous timeIntervalSinceDate:esmStartDate];
  double bucketLowerBound = 0;
  double bucketUpperBound = minutesPerBucket * 60;
  XCTAssertTrue(intervalFromStart >=bucketLowerBound && intervalFromStart <= bucketUpperBound,
               @"schedule should be in its bucket!");
  
  NSDate* current = nil;
  if (shouldPrintLog) {
    NSLog(@"****************************************");
    NSLog(@"%@", [PacoDateUtility pacoStringForDate:previous]);    
  }
  //esm schedules should be sorted and not duplicate
  for (int index=1; index < [dates count]; index++) {
    current = dates[index];
    if (shouldPrintLog) {
      NSLog(@"%@", [PacoDateUtility pacoStringForDate:current]);
    }
    NSTimeInterval interval = [current timeIntervalSinceDate:previous];
    XCTAssertTrue(interval > 0,
                 @"%@ should be later than %@", [PacoDateUtility pacoStringForDate:current], [PacoDateUtility pacoStringForDate:previous]);
    XCTAssertTrue(interval >= minimumBuffer*60,
                 @"schedule interval %d should have %d seconds buffer at least.", interval, minimumBuffer*60);
    
    intervalFromStart = [current timeIntervalSinceDate:esmStartDate];
    bucketLowerBound = index * minutesPerBucket * 60;
    bucketUpperBound = (index + 1) * minutesPerBucket * 60;
    XCTAssertTrue(intervalFromStart >= bucketLowerBound && intervalFromStart <= bucketUpperBound,
                 @"schedule should be in its bucket!");
    
    previous = current;
  }
}

//generic test
- (void)genericTestESMCreat {
  int totalMinutes = (self.esmSchedule.esmEndHour - self.esmSchedule.esmStartHour)/1000.0/60.0;
  double minutesPerBucket = totalMinutes/(double)self.esmSchedule.esmFrequency;
  
  for (int numOfTests=0; numOfTests<100; numOfTests++) {
    NSArray* dates = [PacoScheduleGenerator createESMScheduleDates:self.esmSchedule fromThisDate:self.fromDate];
    
    [self genericTestESMDates:dates
                 esmFrequency:self.esmSchedule.esmFrequency
                minimumBuffer:self.esmSchedule.minimumBuffer
             minutesPerBucket:minutesPerBucket
                     fromDate:self.fromDate
                 esmStartDate:self.esmStartDate
               shouldPrintLog:NO];
  }
}

- (void)testRandomNumberGenerator {
  for (int numOfTests = 0; numOfTests < 50; numOfTests++) {
    int rand = [PacoUtility randomUnsignedIntegerBetweenMin:0 andMax:0];
    XCTAssertEqual(rand, 0, @"rand should be equal to 0");
  }
  
  for (int numOfTests = 0; numOfTests < 50; numOfTests++) {
    int rand = [PacoUtility randomUnsignedIntegerBetweenMin:50 andMax:1000];
    XCTAssertTrue(rand >= 50 && rand <= 1000, @"rand should be between 50 and 1000");
  }
}

- (void)testCreateESMDates {
  self.fromDate = [PacoDateUtility pacoDateForString:@"2013/09/10 15:33:22-0700"]; //Tues
  self.esmStartDate = [PacoDateUtility pacoDateForString:@"2013/09/10 16:00:00-0700"];
  [self genericTestESMCreat];
}


- (void)testCreateESMDates1 {
  int MILLI_SECONDS_PER_HOUR = 1 * 60 * 60 * 1000;
  self.esmSchedule.esmStartHour = 15 * MILLI_SECONDS_PER_HOUR;  //3pm
  self.esmSchedule.esmEndHour = (15 + 30.0/60.0) * MILLI_SECONDS_PER_HOUR; //3:30pm
  self.esmSchedule.esmFrequency = 3;
  self.esmSchedule.minimumBuffer = 11;
  
  self.fromDate = [PacoDateUtility pacoDateForString:@"2013/09/10 14:20:22-0700"]; //Tues
  self.esmStartDate = [PacoDateUtility pacoDateForString:@"2013/09/10 15:00:00-0700"];
  [self genericTestESMCreat];
}


- (void)testNextScheduledDateForNextDay {
  XCTAssertTrue(self.esmExperiment.schedule.esmScheduleList == nil, @"esmScheduleList should be nil!");
  
  NSDate* fromDate = [PacoDateUtility pacoDateForString:@"2013/09/10 17:33:22-0700"]; //Tues
  NSDate* esmStartDate = [PacoDateUtility pacoDateForString:@"2013/09/11 16:00:00-0700"];

  NSDate* nextScheduleDate = [PacoScheduleGenerator nextScheduledDateForExperiment:self.esmExperiment fromThisDate:fromDate];
  XCTAssertTrue(nextScheduleDate != nil, @"%@ should not be nil!", [PacoDateUtility pacoStringForDate:nextScheduleDate]);
  
  XCTAssertTrue([self.esmExperiment.schedule.esmScheduleList count] == self.esmExperiment.schedule.esmFrequency, @"esmScheduleList should be generated successfully!");

  PacoExperimentSchedule* schedule = self.esmExperiment.schedule;
  int totalMinutes = (schedule.esmEndHour - schedule.esmStartHour)/1000.0/60.0;
  double minutesPerBucket = totalMinutes/(double)schedule.esmFrequency;
  NSArray* dates = schedule.esmScheduleList;
  [self genericTestESMDates:dates
               esmFrequency:schedule.esmFrequency
              minimumBuffer:schedule.minimumBuffer
           minutesPerBucket:minutesPerBucket
                   fromDate:fromDate
               esmStartDate:esmStartDate
             shouldPrintLog:NO];
}

@end
