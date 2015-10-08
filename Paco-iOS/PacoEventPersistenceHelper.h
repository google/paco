//
//  PacoEventPersistenceHelper.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 10/5/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

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
-(void) markUploaded:(id<PAEventInterface>)correspondingEvent;
-(NSArray*) allEvents;


@end
