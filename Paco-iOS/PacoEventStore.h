//
//  PacoEventStore.h
//  Paco
//
//  Created by northropo on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//




#include "EventStore.h"
#import <Foundation/Foundation.h>

@interface PacoEventStore : NSObject<PAEventStore>

 
- (id<PAEventInterface>) getEventWithJavaLangLong:(JavaLangLong  *)experimentId
                         withOrgJodaTimeDateTime:(OrgJodaTimeDateTime *)scheduledTime
                                    withNSString:(NSString *)groupName
                                withJavaLangLong:(JavaLangLong *)actionTriggerId
                                withJavaLangLong:(JavaLangLong *)scheduleId;

- (void)updateEventWithPAEventInterface:(id<PAEventInterface>)correspondingEvent;


- (void)insertEventWithPAEventInterface:(id<PAEventInterface>)event;


@end
