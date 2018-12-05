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
#import "java/lang/Long.h"
#include "DateTime.h"
#include "EventInterface.h" 

@interface PacoEventPersistenceHelper : NSObject

- (void) deleteAllEvents;
- (void)insertEventWithPAEventInterface:(id<PAEventInterface>)event;

- (id<PAEventInterface>)getEventWithJavaLangLong:(JavaLangLong *)experimentId
                         withOrgJodaTimeDateTime:(OrgJodaTimeDateTime *)scheduledTime
                                    withNSString:(NSString *)groupName
                                withJavaLangLong:(JavaLangLong *)actionTriggerId
                                withJavaLangLong:(JavaLangLong *)scheduleId;


- (void)updateEventWithPAEventInterface:(id<PAEventInterface>)correspondingEvent;
-(NSArray*) eventsForUpload;
-(void) markUploaded:(NSDictionary* )correspondingEvent;
-(NSArray*) allEvents;
-(NSArray*) eventsForUploadNative;
-(NSArray*) eventsForExperimentId:(long) experimentId;


@end
