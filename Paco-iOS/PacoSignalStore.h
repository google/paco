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
