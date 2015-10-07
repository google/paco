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

@interface PacoEventPersistenceHelper : NSObject


-(void) fetchEvent:(JavaLangLong  *) experimentId
withOrgJodaTimeDateTime:(OrgJodaTimeDateTime *)scheduledTime
withNSString:(NSString *)groupName
withJavaLangLong:(JavaLangLong *)actionTriggerId
withJavaLangLong:(JavaLangLong *)scheduleId;


-(void) updateEventRecord:(JavaLangLong  *) experimentId
withOrgJodaTimeDateTime:(OrgJodaTimeDateTime *)scheduledTime
      withNSString:(NSString *)groupName
  withJavaLangLong:(JavaLangLong *)actionTriggerId
  withJavaLangLong:(JavaLangLong *)scheduleId;




@end
