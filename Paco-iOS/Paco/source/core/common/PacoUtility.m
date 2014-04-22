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

+ (unsigned int)randomUnsignedIntegerBetweenMin:(unsigned int)min andMax:(unsigned int)max {
  NSAssert(max >= min, @"max should be larger than or equal to min!");
  unsigned int temp = arc4random_uniform(max - min + 1);  //[0, max-min]
  return temp + min; //[min, max]
}

+ (NSArray*)randomIntegersInRange:(unsigned int)rangeNumber
                    numOfIntegers:(int)numOfIntegers
                        minBuffer:(int)minBuffer {
  if (0 == numOfIntegers || 0 == rangeNumber) {
    return nil;
  }
  
  NSMutableArray* randomNumberList = [NSMutableArray array];
  unsigned int duration = rangeNumber;
  
  if (minBuffer > 0 && duration >= minBuffer) {
    int maxNumOfIntegers = duration/minBuffer + 1;
    if (maxNumOfIntegers <= numOfIntegers) {
      for (int index=0; index<maxNumOfIntegers; index++) {
        unsigned int num = index * minBuffer;
        [randomNumberList addObject:@(num)];
      }
      return randomNumberList;
    }
  }
  
  int NUM_OF_BUCKETS = numOfIntegers;
  unsigned int MIN_BUFFER = minBuffer;
  if (MIN_BUFFER > 0 && duration < MIN_BUFFER) {
    NUM_OF_BUCKETS = 1;
    MIN_BUFFER = 0;
  }
  NSAssert(NUM_OF_BUCKETS >= 1, @"The number of buckets should be larger than or equal to 1");
  unsigned int DURATION_PER_BUCKET = duration/NUM_OF_BUCKETS;
  
  unsigned int lowerBound = 0;
  for (int bucketIndex = 1; bucketIndex <= NUM_OF_BUCKETS; ++bucketIndex) {
    unsigned int upperBound = DURATION_PER_BUCKET * bucketIndex;
    //adjust upperBound according to MIN_BUFFER
    unsigned int upperBoundByMinBuffer = duration - MIN_BUFFER * (NUM_OF_BUCKETS - bucketIndex);
    if (upperBound > upperBoundByMinBuffer) {
      upperBound = upperBoundByMinBuffer;
    }
    NSAssert(lowerBound <= upperBound, @"lowerBound and upperBound should be valid");
    int randomNum = [PacoUtility randomUnsignedIntegerBetweenMin:lowerBound andMax:upperBound];
    [randomNumberList addObject:@(randomNum)];
    
    //prepare lowerBound and upperBound for generating next random number
    lowerBound = upperBound;
    int lowestBoundForNextRandomNumber = randomNum + MIN_BUFFER;
    if (lowerBound < lowestBoundForNextRandomNumber) {
      lowerBound = lowestBoundForNextRandomNumber;
    }
  }
  NSAssert(NUM_OF_BUCKETS == [randomNumberList count], @"should generate NUM_OF_BUCKETS");
  return randomNumberList;
}

@end
