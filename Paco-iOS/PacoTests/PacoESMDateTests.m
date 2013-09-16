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
#import "PacoDate.h"

//ESM per day, doesn't include weekends
//minimumBuffer: 5 min
//timeout: 50 min
//esmFrequency: 10
//esmStartHour: 4pm, 57600000
//esmEndHour: 5pm, 61200000
static NSString* esmScheduleTemplate = @"{\"type\":\"signalSchedule\",\"timeout\":50,\"minimumBuffer\":5,\"id\":1,\"scheduleType\":4,\"esmFrequency\":10,\"esmPeriodInDays\":0,\"esmStartHour\":57600000,\"esmEndHour\":61200000,\"times\":[46800000],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":false,\"byDayOfWeek\":false}";

@interface PacoDate ()
+ (NSUInteger)randomUnsignedIntegerBetweenMin:(NSUInteger)min andMax:(NSUInteger)max;
@end


@interface PacoESMDateTests ()
@property(nonatomic, retain) PacoExperimentSchedule* esmSchedule;
@end


@implementation PacoESMDateTests
- (void)setUp {
  NSError* error = nil;
  NSData* data = [esmScheduleTemplate dataUsingEncoding:NSUTF8StringEncoding];
  id scheduleDict = [NSJSONSerialization JSONObjectWithData:data
                                                    options:NSJSONReadingAllowFragments
                                                      error:&error];
  STAssertTrue(error == nil && [scheduleDict isKindOfClass:[NSDictionary class]],
               @"esmScheduleTemplate should be successfully serialized!");
  PacoExperimentSchedule* schedule = [PacoExperimentSchedule pacoExperimentScheduleFromJSON:scheduleDict];
  STAssertTrue(schedule != nil, @"schedule should not be nil!");
  self.esmSchedule = schedule;
}

- (void)tearDown {
  self.esmSchedule = nil;
}

- (void)testRandomNumberGenerator {
  for (int numOfTests = 0; numOfTests < 50; numOfTests++) {
    int rand = [PacoDate randomUnsignedIntegerBetweenMin:0 andMax:0];
    STAssertEquals(rand, 0, @"rand should be equal to 0");
  }
  
  for (int numOfTests = 0; numOfTests < 50; numOfTests++) {
    int rand = [PacoDate randomUnsignedIntegerBetweenMin:50 andMax:1000];
    STAssertTrue(rand >= 50 && rand <= 1000, @"rand should be between 50 and 1000");
  }
}

- (void)testCreateESMDates {
  NSDate* fromDate = [PacoDate pacoDateForString:@"2013/09/10 15:33:22-0700"]; //Tues
  NSDate* esmStartDate = [PacoDate pacoDateForString:@"2013/09/10 16:00:00-0700"];

  for (int numOfTests=0; numOfTests<100; numOfTests++) {
    NSArray* dates = [PacoDate createESMScheduleDates:self.esmSchedule fromThisDate:fromDate];
    STAssertTrue([dates count] == self.esmSchedule.esmFrequency, @"dates's count should be equal to esmFrequency");
    
    int totalMinutes = (self.esmSchedule.esmEndHour - self.esmSchedule.esmStartHour)/1000.0/60.0;
    double minutesPerBucket = totalMinutes/(double)self.esmSchedule.esmFrequency;
    
    NSDate* previous = [dates objectAtIndex:0];
    NSTimeInterval intervalFromStart = [previous timeIntervalSinceDate:esmStartDate];
    double bucketLowerBound = 0;
    double bucketUpperBound = minutesPerBucket * 60;
    STAssertTrue(intervalFromStart >=bucketLowerBound && intervalFromStart <= bucketUpperBound,
                 @"schedule should be in its bucket!");
    
    NSDate* current = nil;
    //NSLog(@"****************************************");
    //NSLog(@"%@", [PacoDate pacoStringForDate:previous]);
    //esm schedules should be sorted and not duplicate
    for (int index=1; index < [dates count]; index++) {
      current = [dates objectAtIndex:index];
      //NSLog(@"%@", [PacoDate pacoStringForDate:current]);
      NSTimeInterval interval = [current timeIntervalSinceDate:previous];
      STAssertTrue(interval > 0,
                   @"%@ should be later than %@", [PacoDate pacoStringForDate:current], [PacoDate pacoStringForDate:previous]);
      STAssertTrue(interval >= self.esmSchedule.minimumBuffer*60,
                   @"schedule interval %d should have %d seconds buffer at least.", interval, self.esmSchedule.minimumBuffer*60);
      
      intervalFromStart = [current timeIntervalSinceDate:esmStartDate];
      bucketLowerBound = index * minutesPerBucket * 60;
      bucketUpperBound = (index + 1) * minutesPerBucket * 60;
      STAssertTrue(intervalFromStart >= bucketLowerBound,
                   @"schedule should be larger than or equal to bucket lower bound!");
      STAssertTrue(intervalFromStart <= bucketUpperBound,
                   @"schedule intervalFromStart %d should be smaller than or equal to bucket upper bound %d!", intervalFromStart, bucketUpperBound);
      previous = current;
    }
  }
}


- (void)testCreateESMDates1 {
  int MILLI_SECONDS_PER_HOUR = 1 * 60 * 60 * 1000;
  self.esmSchedule.esmStartHour = 15 * MILLI_SECONDS_PER_HOUR;  //3pm
  self.esmSchedule.esmEndHour = (15 + 30.0/60.0) * MILLI_SECONDS_PER_HOUR; //3:30pm
  self.esmSchedule.esmFrequency = 3;
  self.esmSchedule.minimumBuffer = 11;
  
  int totalMinutes = (self.esmSchedule.esmEndHour - self.esmSchedule.esmStartHour)/1000.0/60.0;
  double minutesPerBucket = totalMinutes/(double)self.esmSchedule.esmFrequency;
  
  NSDate* fromDate = [PacoDate pacoDateForString:@"2013/09/10 14:20:22-0700"]; //Tues
  NSDate* esmStartDate = [PacoDate pacoDateForString:@"2013/09/10 15:00:00-0700"];

  for (int numOfTests=0; numOfTests<100; numOfTests++) {
    NSArray* dates = [PacoDate createESMScheduleDates:self.esmSchedule fromThisDate:fromDate];
    STAssertTrue([dates count] == self.esmSchedule.esmFrequency, @"dates's count should be equal to esmFrequency");
    NSDate* previous = [dates objectAtIndex:0];
    
    NSTimeInterval intervalFromStart = [previous timeIntervalSinceDate:esmStartDate];
    double bucketLowerBound = 0;
    double bucketUpperBound = minutesPerBucket * 60;
    STAssertTrue(intervalFromStart >=bucketLowerBound && intervalFromStart <= bucketUpperBound,
                 @"schedule should be in its bucket!");
    
    NSDate* current = nil;
//    NSLog(@"****************************************");
//    NSLog(@"%@", [PacoDate pacoStringForDate:previous]);
    //esm schedules should be sorted and not duplicate    
    for (int index=1; index < [dates count]; index++) {
      current = [dates objectAtIndex:index];
//      NSLog(@"%@", [PacoDate pacoStringForDate:current]);
      NSTimeInterval interval = [current timeIntervalSinceDate:previous];
      STAssertTrue(interval > 0,
                   @"%@ should be later than %@", [PacoDate pacoStringForDate:current], [PacoDate pacoStringForDate:previous]);
      STAssertTrue(interval >= self.esmSchedule.minimumBuffer*60,
                   @"schedule interval %d should have %d seconds buffer at least.", interval, self.esmSchedule.minimumBuffer*60);
      
      intervalFromStart = [current timeIntervalSinceDate:esmStartDate];
      bucketLowerBound = index * minutesPerBucket * 60;
      bucketUpperBound = (index + 1) * minutesPerBucket * 60;
      STAssertTrue(intervalFromStart >= bucketLowerBound && intervalFromStart <= bucketUpperBound,
                   @"schedule should be in its bucket!");
      
      previous = current;
    }
  }
}

@end
