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
#import "PacoDateUtility.h"

NSString* const kNotificationSoundName = @"deepbark_trial.mp3";

NSString* const kUserInfoKeyExperimentId = @"id";
NSString* const kUserInfoKeyExperimentTitle = @"title";
NSString* const kUserInfoKeyNotificationFireDate = @"fireDate";
NSString* const kUserInfoKeyNotificationTimeoutDate = @"timeoutDate";
NSString* const kNotificationGroupId                 = @"groupId";
NSString* const kNotificationGroupName               =@"groupName";
NSString* const kUserInfoKeyActionTriggerId          = @"actionTriggerId";
NSString* const kUserInfoKeyNotificationActionId     = @"notificationActionId";
NSString* const kUserInfoKeyActionTriggerSpecId      =@"actionTriggerSpecId";



@interface PacoNotificationInfo ()
@property(nonatomic, copy) NSString* experimentId;
@property(nonatomic, copy) NSString* experimentTitle;
@property(nonatomic, strong) NSDate* fireDate;
@property(nonatomic, strong) NSDate* timeOutDate;
@end



@implementation PacoNotificationConstants

@end

@implementation PacoNotificationInfo

 


/*
NSString* experimentId = infoDict[kUserInfoKeyExperimentId];
NSString* experimentTitle = infoDict[kUserInfoKeyExperimentTitle];
NSDate* fireDate = infoDict[kUserInfoKeyNotificationFireDate];
NSDate* timeOutDate = infoDict[kUserInfoKeyNotificationTimeoutDate];
NSString* groupId = infoDict[kNotificationGroupId];
NSString* groupName = infoDict[kNotificationGroupName];
NSString* triggerId = infoDict[kUserInfoKeyActionTriggerId];
NSString* actionTriggerId = infoDict[kUserInfoKeyActionTriggerId];
NSString* notificationActionId =infoDict[kUserInfoKeyNotificationActionId];
 */







+ (NSDictionary*) userInfoDictionaryWithExperimentId:(NSString*)experimentId
                                    experimentTitle:(NSString*)experimentTitle
                                           fireDate:(NSDate*)fireDate
                                        timeOutDate:(NSDate*)timeOutDate {
    if (0 == [experimentId length] || 0 == [experimentTitle length] ||
        fireDate == nil || timeOutDate == nil || [timeOutDate timeIntervalSinceDate:fireDate] <= 0) {
        return nil;
    }
    NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
    userInfo[kUserInfoKeyExperimentId] = experimentId;
    userInfo[kUserInfoKeyExperimentTitle] = experimentTitle;
    userInfo[kUserInfoKeyNotificationFireDate] = fireDate;
    userInfo[kUserInfoKeyNotificationTimeoutDate] = timeOutDate;
    return userInfo;
}

- (PacoNotificationStatus)status {
    if ([self.fireDate timeIntervalSinceNow] > 0) {
        NSAssert([self.timeOutDate timeIntervalSinceDate:self.fireDate] > 0,
                 @"timeout data should always be later than fire date");
        return PacoNotificationStatusNotFired;
    } else {
        if ([self.timeOutDate timeIntervalSinceNow] > 0) {
            return PacoNotificationStatusFiredNotTimeout;
        } else {
            return PacoNotificationStatusTimeout;
        }
    }
}

- (long)timeoutMinutes {
    
    return [self.timeOutDate timeIntervalSinceDate:self.fireDate] / 60;
}

- (NSString*)description {
    NSString* description = @"{";
    description = [description stringByAppendingString:[NSString stringWithFormat:@"%@:%@; ",
                                                        kUserInfoKeyExperimentId, self.experimentId]];
    description = [description stringByAppendingString:[NSString stringWithFormat:@"%@:%@; ",
                                                        kUserInfoKeyExperimentTitle, self.experimentTitle]];
    description = [description stringByAppendingString:[NSString stringWithFormat:@"%@:%@; ",
                                                        kUserInfoKeyNotificationFireDate,
                                                        [PacoDateUtility pacoStringForDate:self.fireDate]]];
    description = [description stringByAppendingString:[NSString stringWithFormat:@"%@:%@; ",
                                                        kUserInfoKeyNotificationTimeoutDate,
                                                        [PacoDateUtility pacoStringForDate:self.timeOutDate]]];
    description = [description stringByAppendingString:@"}"];
    return description;
}



- (BOOL)isEqualToNotificationInfo:(PacoNotificationInfo*)info {
    if ([self.experimentId isEqualToString:info.experimentId] &&
        [self.experimentTitle isEqualToString:info.experimentTitle] &&
        [self.fireDate isEqualToDate:info.fireDate] &&
        [self.timeOutDate isEqualToDate:info.timeOutDate]) {
        return YES;
    } else {
        return NO;
    }
}

@end
