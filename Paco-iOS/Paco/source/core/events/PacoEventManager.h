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

@class PacoEvent;
@class PacoEventUploader;
@class PacoExperimentDefinition;
@class PacoExperimentSchedule;
@class PacoExperiment;

//YMZ:TODO: fully testing
//YMZ:TODO: thread safe
//YMZ:TODO: use async design
//YMZ:TODO: use core data
//YMZ:TODO: error handling of file operation
@interface PacoEventManager : NSObject

+ (PacoEventManager*)defaultManager;

- (void)saveEvent:(PacoEvent*)event;

- (void)saveDataToFile;

- (void)startUploadingEvents;
- (void)stopUploadingEvents;


- (void)saveJoinEventWithDefinition:(PacoExperimentDefinition*)definition
                       withSchedule:(PacoExperimentSchedule*)schedule;
- (void)saveStopEventWithExperiment:(PacoExperiment*)experiment;
- (void)saveSelfReportEventWithDefinition:(PacoExperimentDefinition*)definition
                                andInputs:(NSArray*)visibleInputs;
- (void)saveSurveySubmittedEventForDefinition:(PacoExperimentDefinition*)definition
                                   withInputs:(NSArray*)inputs
                             andScheduledTime:(NSDate*)scheduledTime;
- (void)saveSurveyMissedEventForDefinition:(PacoExperimentDefinition*)definition
                         withScheduledTime:(NSDate*)scheduledTime;

@end
