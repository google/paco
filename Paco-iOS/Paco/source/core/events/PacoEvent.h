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
#import <CoreData/CoreData.h>

@class PacoExperiment;
@class PacoExperimentDefinition;
@class PacoExperimentSchedule;


extern NSString* const kPacoResponseKeyName;
extern NSString* const kPacoResponseKeyAnswer;
extern NSString* const kPacoResponseKeyInputId;


typedef NS_ENUM(NSInteger, PacoEventType) {
    PacoEventTypeJoin,
    PacoEventTypeStop,
    PacoEventTypeSurvey,
    PacoEventTypeMiss,
    PacoEventTypeSelfReport
};

static NSString* const PACO_EVENT_ENTITY_NAME = @"PacoEvent";

@interface PacoEvent : NSManagedObject

@property (nonatomic, retain) NSString * who;
@property (nonatomic, retain) NSDate * when;
@property (nonatomic, retain) NSNumber * latitude;
@property (nonatomic, retain) NSNumber * longitude;
@property (nonatomic, retain) NSDate * responseTime;
@property (nonatomic, retain) NSDate * scheduledTime;
@property (nonatomic, readonly, copy) NSString * appId;
@property (nonatomic, readonly, copy) NSString * pacoVersion;
@property (nonatomic, retain) NSString * experimentId;
@property (nonatomic, retain) NSString * experimentName;
@property (nonatomic, retain) NSNumber * experimentVersion;
@property (nonatomic, retain) id responses;
@property (nonatomic, retain) NSNumber * isPending;

+ (id)pacoEventForIOS;
+ (id)pacoEventFromJSON:(id)jsonObject;
- (id)generateJsonObject;
- (id)payloadJsonWithImageString;

- (PacoEventType)type;

+ (PacoEvent*)stopEventForExperiment:(PacoExperiment*)experiment;
+ (PacoEvent*)joinEventForDefinition:(PacoExperimentDefinition*)definition
                        withSchedule:(PacoExperimentSchedule*)schedule;
+ (PacoEvent*)selfReportEventForDefinition:(PacoExperimentDefinition*)definition
                                withInputs:(NSArray*)inputs;
+ (PacoEvent*)surveySubmittedEventForDefinition:(PacoExperimentDefinition*)definition
                                     withInputs:(NSArray*)inputs
                               andScheduledTime:(NSDate*)scheduledTime;
+ (PacoEvent*)surveyMissedEventForDefinition:(PacoExperimentDefinition*)definition
                           withScheduledTime:(NSDate*)scheduledTime;
+ (PacoEvent*)surveyMissedEventForDefinition:(PacoExperimentDefinition*)definition
                           withScheduledTime:(NSDate*)scheduledTime
                                   userEmail:(NSString*)userEmail;

@end
