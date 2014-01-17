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

@class PacoExperimentDefinition;
@class PacoExperimentSchedule;
@class PacoModel;

@interface PacoExperiment : NSObject

@property (nonatomic, copy) PacoExperimentDefinition *definition;
@property (nonatomic, copy) NSString *instanceId;
@property (nonatomic, assign) long long lastEventQueryTime;
@property (nonatomic, retain) PacoExperimentSchedule *schedule;  // Override schedule from definition.
//@property (retain) PacoExperimentSchedule *overrideSchedule;  // Override schedule from definition.
@property (nonatomic, retain) id jsonObject;

- (id)serializeToJSON;
- (void)deserializeFromJSON:(id)json;

- (BOOL)shouldScheduleNotifications;

- (BOOL)isSelfReportExperiment;
- (BOOL)isScheduledExperiment;

- (BOOL)isExperimentValidSinceDate:(NSDate*)fromDate;

- (BOOL)isFixedLength;
- (BOOL)isOngoing;

- (NSArray*)ESMSchedulesFromDate:(NSDate*)fromDate;

- (NSDate*)startDate;
- (NSDate*)endDate;

//when definition is refreshed, refresh experiment's schedule
//but keep the esmStartHour, esmEndHour, or times configured specifically by user
//return YES if the schedule is changed, NO if the schedule doesn't need to be updated
- (BOOL)refreshWithSchedule:(PacoExperimentSchedule*)newSchedule;

@end

