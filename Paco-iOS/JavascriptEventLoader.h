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
@class PAExperimentDAO;
@class PAExperimentGroup;


@interface JavascriptEventLoader : NSObject

@property(nonatomic, strong, readonly) PAExperimentDAO* experiment;
@property(nonatomic, strong, readonly) PAExperimentGroup* group;

+ (instancetype)loaderForExperiment:(PAExperimentDAO*)experiment group:(PAExperimentGroup*) group;

+ (NSString*)convertEventsToJsonString:(NSArray*)events experiment:(PAExperimentDAO*)experiment group:(PAExperimentGroup*) group;

- (NSString*)getAllEvents;

- (NSString*)loadAllEvents;

- (NSString*)jsonStringForLastEvent;

@end
