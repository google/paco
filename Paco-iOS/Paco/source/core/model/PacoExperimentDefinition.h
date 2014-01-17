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
@class PacoExperimentSchedule;

@interface PacoExperimentDefinition : NSObject <NSCopying>

@property (nonatomic, copy) NSString *experimentId;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *experimentDescription;

@property (nonatomic, copy) NSString *creator;
@property (nonatomic, retain) NSArray *admins;  // <NSString>

@property (nonatomic, assign) BOOL published;
@property (nonatomic, retain) NSArray *publishedUsers;  // <NSString>

@property(nonatomic, strong, readonly) NSDate* startDate;
//exclusive, used for computing schedules
@property(nonatomic, strong, readonly) NSDate* endDate;
//inclusive, used only for UI display
@property(nonatomic, strong, readonly) NSString* inclusiveEndDateString;


@property (nonatomic, assign) BOOL deleted;
@property (nonatomic, retain) NSArray *feedback;  // <PacoExperimentFeedback>
@property (nonatomic, assign) BOOL fixedDuration;
@property (nonatomic, copy) NSString *informedConsentForm;
@property (nonatomic, retain) NSArray *inputs;  // <PacoExperimentInput>
@property (nonatomic, copy) NSString* modifyDate;
@property (nonatomic, assign) BOOL questionsChange;
@property (nonatomic, retain) PacoExperimentSchedule *schedule;
@property (nonatomic, assign) BOOL webReccommended;
@property (nonatomic, retain) id jsonObject;
@property (nonatomic, assign) int experimentVersion;

+ (id)pacoExperimentDefinitionFromJSON:(id)jsonObject;
- (id)serializeToJSON;

//An experiment can be either on-going or fixed-length with valid start date and end date
- (BOOL)isFixedLength;
- (BOOL)isOngoing;

- (BOOL)hasSameDurationWithDefinition:(PacoExperimentDefinition*)another;

//An on-going experiment is always valid
//For a fixed-length experiment, if it doesn't finish yet, then it's valid; otherwise it's invalid
- (BOOL)isExperimentValid;

- (BOOL)isExperimentValidSinceDate:(NSDate*)fromDate;

//clear the old response objects
- (void)clearInputs;

- (void)clearEsmScheduleList;

//TEST debug code
+ (PacoExperimentDefinition *)testPacoExperimentDefinition;
+ (PacoExperimentDefinition*)testDefinitionWithId:(NSString*)definitionId;

@end
