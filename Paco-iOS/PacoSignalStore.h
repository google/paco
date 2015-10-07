//
//  PacoSignalStore.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EsmSignalStore.h"


@interface PacoSignalStore : NSObject<PAEsmSignalStore>

- (void)storeSignalWithJavaLangLong:(JavaLangLong *)date
                   withJavaLangLong:(JavaLangLong *)experimentId
                   withJavaLangLong:(JavaLangLong *)alarmTime
                       withNSString:(NSString *)groupName
                   withJavaLangLong:(JavaLangLong *)actionTriggerId
                   withJavaLangLong:(JavaLangLong *)scheduleId;

- (id<JavaUtilList>)getSignalsWithJavaLangLong:(JavaLangLong *)experimentId
                              withJavaLangLong:(JavaLangLong *)periodStart
                                  withNSString:(NSString *)groupName
                              withJavaLangLong:(JavaLangLong *)actionTriggerId
                              withJavaLangLong:(JavaLangLong *)scheduleId;

- (void)deleteAll;
- (void)deleteAllSignalsForSurveyWithJavaLangLong:(JavaLangLong *)experimentId;
- (void)deleteSignalsForPeriodWithJavaLangLong:(JavaLangLong *)experimentId
                              withJavaLangLong:(JavaLangLong *)periodStart
                                  withNSString:(NSString *)groupName
                              withJavaLangLong:(JavaLangLong *)actionTriggerId
                              withJavaLangLong:(JavaLangLong *)scheduleId;

-(NSArray*)  matchRecords:(JavaLangLong *)date
         withJavaLangLong:(JavaLangLong *)experimentId
         withJavaLangLong:(JavaLangLong *)alarmTime
             withNSString:(NSString *)groupName
         withJavaLangLong:(JavaLangLong *)actionTriggerId
         withJavaLangLong:(JavaLangLong *)scheduleId;

@end
