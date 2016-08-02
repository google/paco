/* Copyright 2015  Google
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@class PAExperimentDAO;
@class PASchedule;
@class PacoEventExtended;
@class PacoExperimentExtended;


@interface PacoParticipateStatusExtended : NSObject

@property(nonatomic, readonly) NSUInteger numberOfNotifications;
@property(nonatomic, readonly) NSUInteger numberOfParticipations;
@property(nonatomic, readonly) NSUInteger numberOfSelfReports;
@property(nonatomic, readonly) float percentageOfParticipation; //0.867
@property(nonatomic, copy, readonly) NSString *percentageText; //87%

@end


@class PAActionSpecification;



@interface PacoEventManagerExtended : NSObject

+ (PacoEventManagerExtended*) defaultManager;

- (void)saveEvent:(PacoEventExtended*)event;
- (void)saveEvents:(NSArray*)events;
- (void)startUploadingEvents;

//When background fetch API triggers or location significantly changes, call this method
//to upload events in a limited time frame, we are allowed to finish our tasks in 30 seconds.
- (void)startUploadingEventsInBackgroundWithBlock:(void(^)(UIBackgroundFetchResult))completionBlock;

- (void)stopUploadingEvents;
- (void)markEventsComplete:(NSArray*)events;



- (void)saveStopEventWithExperiment:(PacoExperimentExtended*)experiment;


- (void)saveSelfReportEventWithDefinition:(PAExperimentDAO*)definition
                                andInputs:(NSArray*)visibleInputs;
- (void)saveSurveySubmittedEventForDefinition:(PAExperimentDAO*)definition
                                   withInputs:(NSArray*)inputs
                             andScheduledTime:(NSDate*)scheduledTime
                                    groupName:(NSString*) groupName
                              actionTriggerId:(NSString*) actionTriggerId
                                     actionId:(NSString*) actionId
                          actionTriggerSpecId:(NSString*) actionTriggerSpecId
                                    userEmail:(NSString*)userEmail;



-(NSArray*) allEventsForExperiment:(NSNumber*) experimentId;


- (PacoParticipateStatusExtended*)statsForExperiment:(NSString*)experimentId;
- (void)saveJoinEventWithActionSpecification:(PAActionSpecification*) actionSpecification;

@end
