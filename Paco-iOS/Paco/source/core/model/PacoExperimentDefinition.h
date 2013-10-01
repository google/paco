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

@interface PacoExperimentDefinition : NSObject

@property (nonatomic, copy) NSString *experimentId;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *experimentDescription;

@property (nonatomic, copy) NSString *creator;
@property (nonatomic, retain) NSArray *admins;  // <NSString>

@property (nonatomic, assign) BOOL published;
@property (nonatomic, retain) NSArray *publishedUsers;  // <NSString>


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

//clear the old response objects
- (void)clearInputs;

//TEST debug code
+ (PacoExperimentDefinition *)testPacoExperimentDefinition;
+ (PacoExperimentDefinition*)testDefinitionWithId:(NSString*)definitionId;

@end
