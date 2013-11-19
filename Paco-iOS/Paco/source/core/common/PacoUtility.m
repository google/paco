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
#import "PacoUtility.h"

@implementation PacoUtility

+ (NSUInteger)randomUnsignedIntegerBetweenMin:(NSUInteger)min andMax:(NSUInteger)max {
  NSAssert(max >= min, @"max should be larger than or equal to min!");
  int temp = arc4random_uniform(max - min + 1);  //[0, max-min]
  return temp + min; //[min, max]
}

+ (NSArray*)randomIntegersInRange:(NSUInteger)rangeNumber
                    numOfIntegers:(NSUInteger)numOfIntegers
                        minBuffer:(NSUInteger)minBuffer {
  if (0 == numOfIntegers || 0 == rangeNumber) {
    return nil;
  }
  int duration = rangeNumber;
  int NUM_OF_BUCKETS = numOfIntegers;
  NSAssert(NUM_OF_BUCKETS >= 1, @"The number of buckets should be larger than or equal to 1");
  int DURATION_PER_BUCKET = duration/NUM_OF_BUCKETS;
  
  NSMutableArray* randomNumberList = [NSMutableArray array];
  int lowerBound = 0;
  for (int bucketIndex = 1; bucketIndex <= NUM_OF_BUCKETS; ++bucketIndex) {
    int upperBound = DURATION_PER_BUCKET * bucketIndex;
    //adjust upperBound according to minBuffer
    int upperBoundByMinBuffer = duration - minBuffer * (NUM_OF_BUCKETS - bucketIndex);
    if (upperBound > upperBoundByMinBuffer) {
      upperBound = upperBoundByMinBuffer;
    }
    NSAssert(lowerBound <= upperBound, @"lowerBound and upperBound should be valid");
    int randomNum = [PacoUtility randomUnsignedIntegerBetweenMin:lowerBound andMax:upperBound];
    [randomNumberList addObject:[NSNumber numberWithInt:randomNum]];
    
    //prepare lowerBound and upperBound for generating next random number
    lowerBound = upperBound;
    int lowestBoundForNextRandomNumber = randomNum + minBuffer;
    if (lowerBound < lowestBoundForNextRandomNumber) {
      lowerBound = lowestBoundForNextRandomNumber;
    }
  }
  NSAssert(numOfIntegers == [randomNumberList count], @"should generate numOfIntegers");
  return randomNumberList;
}

@end
