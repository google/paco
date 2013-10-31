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

#import "NSMutableArray+Paco.h"
#import "UILocalNotification+Paco.h"
#import "PacoDateUtility.h"

@implementation NSMutableArray (Paco)

- (void)pacoSortDatesToSchedule {
  [self sortUsingComparator:^NSComparisonResult(id obj1, id obj2) {
    NSDate* firstDate = (NSDate*)obj1;
    NSDate* secondDate = (NSDate*)obj2;
    NSAssert([firstDate isKindOfClass:[NSDate class]], @"firstDate should be NSDate");
    NSAssert([secondDate isKindOfClass:[NSDate class]], @"secondDate should be NSDate");
    return [firstDate compare:secondDate];
  }];
}

- (void)pacoSortLocalNotificationsByFireDate {
  [self sortUsingComparator:^NSComparisonResult(id obj1, id obj2) {
    UILocalNotification* first = (UILocalNotification*)obj1;
    UILocalNotification* second = (UILocalNotification*)obj2;
    NSAssert([first isKindOfClass:[UILocalNotification class]] &&
             [second isKindOfClass:[UILocalNotification class]],
             @"obj1 and obj2 should be UILocalNotification");
    
    NSDate* firstFireDate = [first pacoFireDate];
    NSDate* secondFireDate = [second pacoFireDate];
    NSAssert(firstFireDate && secondFireDate, @"fire date should be valid");
    return [firstFireDate compare:secondFireDate];
  }];
}
@end



@implementation NSArray (Paco)

- (BOOL)pacoIsNotEmpty {
  return [self count] > 0;
}

- (NSString*)pacoDescriptionForDates {
  NSString* descript = @"(\n";
  for (id object in self) {
    NSDate* date = (NSDate*)object;
    NSAssert([date isKindOfClass:[NSDate class]], @"every element should be NSDate");
    descript = [descript stringByAppendingString:[PacoDateUtility pacoStringForDate:date]];
    descript = [descript stringByAppendingString:@"\n"];
  }
  descript = [descript stringByAppendingString:@")"];
  return descript;
}

@end
