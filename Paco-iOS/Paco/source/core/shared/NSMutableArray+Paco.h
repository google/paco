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

#import <Foundation/Foundation.h>

@interface NSMutableArray (Paco)

//The receiver must contain NSDate objects
- (void)pacoSortDatesToSchedule;

//The receiver must contain UILocalNotification objects composed by UILocalNotification+Paco
- (void)pacoSortLocalNotificationsByFireDate;

//The receiver must contain UILocalNotification objects composed by UILocalNotification+Paco
- (NSMutableArray*)pacoSortLocalNotificationsAndRemoveDuplicates;

@end


@interface NSArray (Paco)

- (BOOL)pacoIsNotEmpty;

//The receiver must contain only NSDate objects
- (NSString*)pacoDescriptionForDates;

//The receiver must contain only NSNumber object with long long value
- (NSString*)pacoDescriptionForTimeNumbers;

//The receiver must contain only UILocalNotification objects
- (NSString*)pacoDescriptionForNotifications;

//The receiver must contain only NSNumber objects
- (NSArray*)pacoSortedNumbers;

//The receiver must contain only NSNumber objects
- (BOOL)pacoIsNonDuplicate;

@end



@interface NSDictionary (Paco)

- (NSString*)pacoDescriptionForNotificationDict;

@end