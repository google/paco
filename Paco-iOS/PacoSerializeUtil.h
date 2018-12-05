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


@class PASchedule;
@class PAExperimentDAO;

@interface PacoSerializeUtil : NSObject

@property(strong, readonly) NSArray* classes;


+(PASchedule*) getScheduleAtIndex:(PAExperimentDAO *)  experiment   GroupIndex:(int) groupIndex actionTriggerIndex:(int) actionTriggerIndex  scheduleIndex:(int) scheduleIndex;


+ (NSArray*)getClassNames;

+ (NSString*) jsonFromDefinition:(PAExperimentDAO*) description;
+ (NSString*) jsonFromSchedule:(PASchedule*) shedule;

@end
