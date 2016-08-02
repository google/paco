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

#import "PacoNotificationConstants.h" 

#import <Foundation/Foundation.h>

@interface PacoExtendedNotificationInfo : PacoNotificationInfo



 
@property(nonatomic, strong, readonly)      NSString * groupId;
@property(nonatomic, strong, readonly)      NSString * groupName;
@property(nonatomic, strong, readonly)      NSString * actionTriggerId;
@property(nonatomic, strong, readonly)      NSString * notificationActionId;
@property(nonatomic, strong, readonly)      NSString * actionTriggerSpecId;
 


+ (NSDictionary*)userInfoDictionaryWithExperimentId:(NSString*)experimentId
                                    experimentTitle:(NSString*)experimentTitle
                                           fireDate:(NSDate*)fireDate
                                        timeOutDate:(NSDate*)timeOutDate
                                            groupId:(NSString*) groupId
                                          groupName:(NSString*) groupName
                                    actionTriggerId:(NSString*) actionTriggerId
                               notificationActionId:(NSString*) notificationActionId
                                actionTriggerSpecId:(NSString*) actionTriggerSpecId;




+ (PacoExtendedNotificationInfo*)pacoInfoWithDictionary:(NSDictionary*)infoDict;


- (BOOL)isEqualToNotificationInfo:(PacoExtendedNotificationInfo*)info;


@end
