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



static int const kNumOfKeysInUserInfo = 4;

@interface PacoNotificationConstants : NSObject



typedef NS_ENUM(NSInteger, PacoNotificationStatus) {
    PacoNotificationStatusUnknown = 0,      //unknown
    PacoNotificationStatusNotFired,         //not fired yet
    PacoNotificationStatusFiredNotTimeout,  //fired, but not timed out
    PacoNotificationStatusTimeout,          //fired, and timed out
};

extern NSString* const kNotificationSoundName;
extern NSString* const kUserInfoKeyExperimentId;
extern NSString* const kUserInfoKeyExperimentTitle;
extern NSString* const kUserInfoKeyNotificationFireDate;
extern NSString* const kUserInfoKeyNotificationTimeoutDate;
extern NSString* const kNotificationGroupId;
extern NSString* const kNotificationGroupName;
extern NSString* const kUserInfoKeyActionTriggerId;
extern NSString* const kUserInfoKeyNotificationActionId;
extern NSString* const kUserInfoKeyActionTriggerSpecId;


@end


@interface PacoNotificationInfo : NSObject
@property(nonatomic, copy, readonly) NSString* experimentId;
@property(nonatomic, copy, readonly) NSString* experimentTitle;
@property(nonatomic, strong, readonly) NSDate* fireDate;
@property(nonatomic, strong, readonly) NSDate* timeOutDate;


+ (NSDictionary*)userInfoDictionaryWithExperimentId:(NSString*)experimentId
                                          experimentTitle:(NSString*)experimentTitle
                                          fireDate:(NSDate*)fireDate
                                          timeOutDate:(NSDate*)timeOutDate;

+ (PacoNotificationInfo*)pacoInfoWithDictionary:(NSDictionary*)infoDict;
- (PacoNotificationStatus)status;
- (long)timeoutMinutes;
- (BOOL)isEqualToNotificationInfo:(PacoNotificationInfo*)info;

@end


 