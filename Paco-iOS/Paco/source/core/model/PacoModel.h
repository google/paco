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

extern NSString* const PacoExperimentDefinitionUpdateNotification;
extern NSString* const PacoExperimentInstancesUpdateNotification;



@class PacoModel;

@interface PacoExperimentFeedback : NSObject
@property (copy) NSString *feedbackId;
@property (copy) NSString *text;
@property (copy) NSString *type;  // currently only 1 type , 'display'
@property (retain) id jsonObject;
+ (id)pacoFeedbackFromJSON:(id)jsonObject;
@end

// ExperimentInput is basically something like a question, or measure of some input like a location or photo.
@interface PacoExperimentInput : NSObject
@property (assign) BOOL conditional;
@property (retain) NSString *conditionalExpression;
@property (copy) NSString *inputIdentifier;
@property (assign) BOOL invisibleInput;
@property (copy) NSString *leftSideLabel;
@property (assign) NSInteger likertSteps; // only for response type 'likert'
@property (retain) NSArray *listChoices; // <NSString>
@property (assign) BOOL mandatory;
@property (copy) NSString *name;
@property (copy) NSString *questionType;  // 'question'/ (text question or sensor input)
@property (copy) NSString *responseType;  // 'likert', 'list', open text, etc.
@property (copy) NSString *rightSideLabel;
@property (copy) NSString *text;
@property (retain) id jsonObject;
@property (retain) id responseObject;  // The user's answer to this question
@property (assign) BOOL isADependencyForOthers;
+ (id)pacoExperimentInputFromJSON:(id)jsonObject;
+ (NSArray *)parseExpression:(NSString *)expr;
@end

typedef enum {
  kPacoScheduleRepeatPeriodDay = 0,
  kPacoScheduleRepeatPeriodWeek = 1,
  kPacoScheduleRepeatPeriodMonth = 2,
} PacoScheduleRepeatPeriod;

typedef enum {
  kPacoScheduleDaySunday = 1,
  kPacoScheduleDayMonday = 1 << 1,
  kPacoScheduleDayTuesday = 1 << 2,
  kPacoScheduleDayWednesday = 1 << 3,
  kPacoScheduleDayThursday = 1 << 4,
  kPacoScheduleDayFriday = 1 << 5,
  kPacoScheduleDaySaturday = 1 << 6,
} PacoScheduleDay;

typedef enum {
  kPacoScheduleTypeDaily = 0,
  kPacoScheduleTypeWeekday = 1,
  kPacoScheduleTypeWeekly = 2,
  kPacoScheduleTypeMonthly = 3,
  kPacoScheduleTypeESM = 4,
  kPacoScheduleTypeSelfReport = 5,
  kPacoScheduleTypeAdvanced = 6,
} PacoScheduleType;

typedef enum {
  kPacoSchedulePeriodDay = 0,
  kPacoSchedulePeriodWeek = 1,
  kPacoSchedulePeriodMonth = 2,
} PacoSchedulePeriod;

@interface PacoExperimentSchedule : NSObject
@property (assign) BOOL byDayOfMonth;
@property (assign) BOOL byDayOfWeek;
@property (assign) NSInteger dayOfMonth;
@property (assign) long long esmEndHour;
@property (assign) NSInteger esmFrequency;
@property (assign) long long esmPeriodInDays;
@property (assign) PacoScheduleRepeatPeriod esmPeriod;
@property (assign) long long esmStartHour;
@property (assign) BOOL esmWeekends;
@property (copy) NSString *scheduleId;
@property (assign) NSInteger nthAMonth;
@property (assign) PacoScheduleRepeatPeriod repeatPeriod;
@property (assign) PacoScheduleType scheduleType;
@property (retain) NSArray *times;  // NSNumber<long >
@property (assign) BOOL userEditable;
@property (assign) NSInteger weekDaysScheduled;  // Binary OR of PacoScheduleDay
@property (retain) id jsonObject;
@property (retain) NSArray *esmSchedule;  // NSArray<NSDate>
+ (id)pacoExperimentScheduleFromJSON:(id)jsonObject;
- (NSString *)jsonString;
@end

@interface PacoExperimentDefinition : NSObject
@property (retain) NSArray *admins;  // <NSString>
@property (copy) NSString *creator;
@property (assign) BOOL deleted;
@property (copy) NSString *experimentDescription;
@property (retain) NSArray *feedback;  // <PacoExperimentFeedback>
@property (assign) BOOL fixedDuration;
@property (copy) NSString *experimentId;
@property (copy) NSString *informedConsentForm;
@property (retain) NSArray *inputs;  // <PacoExperimentInput>
@property (assign) long long modifyDate;
@property (assign) BOOL published;
@property (retain) NSArray *publishedUsers;  // <NSString>
@property (assign) BOOL questionsChange;
@property (retain) PacoExperimentSchedule *schedule;
@property (copy) NSString *title;
@property (assign) BOOL webReccommended;
@property (retain) id jsonObject;
+ (id)pacoExperimentDefinitionFromJSON:(id)jsonObject;
- (void)tagQuestionsForDependencies;
@end

@interface PacoEvent : NSObject
@property (copy) NSString *who;
@property (retain) NSDate *when;
@property (assign) long long latitude;
@property (assign) long long longitude;
@property (retain) NSDate *responseTime;
@property (retain) NSDate *scheduledTime;
@property (readonly, copy) NSString *appId;
@property (readonly, copy) NSString *pacoVersion;
@property (copy) NSString *experimentId;
@property (copy) NSString *experimentName;
@property (retain) NSArray *responses;  // <NSDictionary>
@property (retain) id jsonObject;
+ (id)pacoEventForIOS;
+ (id)pacoEventFromJSON:(id)jsonObject;
- (id)jsonObject;
- (id)generateJsonObject;
@end

@interface PacoExperiment : NSObject
@property (retain) PacoExperimentDefinition *definition;
@property (retain) NSArray *events;
@property (copy) NSString *instanceId;
@property (assign) long long lastEventQueryTime;
@property (retain) PacoExperimentSchedule *schedule;  // Override schedule from definition.
//@property (retain) PacoExperimentSchedule *overrideSchedule;  // Override schedule from definition.
@property (retain) id jsonObject;

- (id)serializeToJSON;
- (void)deserializeFromJSON:(id)json model:(PacoModel *)model;

- (BOOL)haveJoined;
@end

@interface PacoModel : NSObject
@property (retain) NSArray *experimentDefinitions;  // <PacoExperimentDefinition>
@property (retain) NSMutableArray *experimentInstances;  // <PacoExperiment>
@property (retain) id jsonObjectDefinitions;
@property (retain) id jsonObjectInstances;

- (PacoExperimentDefinition *)experimentDefinitionForId:(NSString *)experimentId;
- (PacoExperiment *)experimentForId:(NSString *)instanceId;
- (NSArray *)instancesForExperimentId:(NSString *)experimentId;
- (PacoExperiment *)addExperimentInstance:(PacoExperimentDefinition *)definition
                                 schedule:(PacoExperimentSchedule *)schedule
                                   events:(NSArray *)events;
- (void)addExperimentsWithDefinition:(PacoExperimentDefinition*)definition events:(NSArray*)events;

+ (id)pacoModelFromDefinitionJSON:(id)jsonDefintions
                     instanceJSON:(id)jsonInstances;

- (NSArray *)joinedExperiments;

- (BOOL)saveToFile;
- (BOOL)loadFromFile;
- (BOOL)deleteFile;

+ (PacoModel *)pacoModelFromFile;

@end

