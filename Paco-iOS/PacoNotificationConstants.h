//
//  PacoNotificationConstants.h
//  Paco
//
//  Created by northropo on 9/11/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

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


 