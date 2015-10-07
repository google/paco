//
//  PacoExtendedNotificationInfo.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/11/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoExtendedNotificationInfo.h"
#import "ModelBase+PacoAssociatedId.m"
#import "PacoNotificationConstants.h" 

#define kNumOfKeysInUserInfoExtended kNumOfKeysInUserInfo+4

@interface PacoExtendedNotificationInfo ()

@property(nonatomic, strong )      NSString * groupId;
@property(nonatomic, strong )      NSString * groupName;
@property(nonatomic, strong )      NSString * actionTriggerId;
@property(nonatomic, strong )      NSString * notificationActionId;
@property(nonatomic, strong )      NSString * actionTriggerSpecId;
@end

@interface PacoNotificationInfo ()
@property(nonatomic, copy) NSString* experimentId;
@property(nonatomic, copy) NSString* experimentTitle;
@property(nonatomic, strong) NSDate* fireDate;
@property(nonatomic, strong) NSDate* timeOutDate;
@end

@implementation PacoExtendedNotificationInfo




+ (PacoExtendedNotificationInfo*)pacoInfoWithDictionary:(NSDictionary*)infoDict {
    if ([[infoDict allKeys] count] != kNumOfKeysInUserInfoExtended) {
        return nil;
    }
    
    NSString* experimentId = infoDict[kUserInfoKeyExperimentId];
    NSString* experimentTitle = infoDict[kUserInfoKeyExperimentTitle];
    NSDate* fireDate = infoDict[kUserInfoKeyNotificationFireDate];
    NSDate* timeOutDate = infoDict[kUserInfoKeyNotificationTimeoutDate];
    NSString* groupId = infoDict[kNotificationGroupId];
    NSString* groupName = infoDict[kNotificationGroupName];
    NSString* triggerId = infoDict[kUserInfoKeyActionTriggerId];
    NSString* actionTriggerId = infoDict[kUserInfoKeyActionTriggerId];
    NSString* notificationActionId =infoDict[kUserInfoKeyNotificationActionId];
   /* NSString* specId  = infoDict[kUserInfoKeyActionTriggerSpecId]; */ 
 
    if ( 0== [groupId length] || [groupName length] ==0 || [triggerId length] ==0 || [actionTriggerId length] ==0 || /*[specId length] ==0  ||*/   0 == [experimentId length] || 0 == [experimentTitle length] ||
        fireDate == nil || timeOutDate == nil || [notificationActionId length] ==0/*|| [timeOutDate timeIntervalSinceDate:fireDate] <= 0*/) {
        return nil;
    }
    
    PacoExtendedNotificationInfo* info = [[PacoExtendedNotificationInfo alloc] init];
    info.experimentId = experimentId;
    info.experimentTitle = experimentTitle;
    info.fireDate = fireDate;
    info.timeOutDate = timeOutDate;
    info.groupId = groupId;
    info.groupName = groupName;
    info.actionTriggerId = actionTriggerId;
    info.notificationActionId=notificationActionId;
   /* info.actionTriggerSpecId = specId*/
    
 
    
    return info;
}









+ (NSDictionary*)userInfoDictionaryWithExperimentId:(NSString*)experimentId
                                    experimentTitle:(NSString*)experimentTitle
                                           fireDate:(NSDate*)fireDate
                                        timeOutDate:(NSDate*)timeOutDate
                                            groupId:(NSString*) groupId
                                          groupName:(NSString*) groupName
                                    actionTriggerId:(NSString*) actionTriggerId
                               notificationActionId:(NSString*) notificationActionId
                                actionTriggerSpecId:(NSString*) actionTriggerSpecId
{
    
    
    
    
      NSDictionary* superInfo =  [PacoNotificationInfo userInfoDictionaryWithExperimentId:experimentId experimentTitle:experimentTitle  fireDate:fireDate timeOutDate:timeOutDate];
    
    if (0 == [groupId length] || 0 == [groupName length] ||
        [actionTriggerId length] == 0 || [notificationActionId length] == 0 || [actionTriggerSpecId  length] == 0|| superInfo ==nil) {
        return nil;
    }
    
    NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
    userInfo[kUserInfoKeyExperimentId] = experimentId;
    userInfo[kUserInfoKeyExperimentTitle] = experimentTitle;
    userInfo[kUserInfoKeyNotificationFireDate] = fireDate;
     userInfo[kUserInfoKeyNotificationTimeoutDate] = timeOutDate;
    userInfo[kNotificationGroupId] = groupId;
    userInfo[kNotificationGroupName] = groupName;
    userInfo[kUserInfoKeyActionTriggerId] = actionTriggerId;
    userInfo[kUserInfoKeyNotificationActionId] = notificationActionId;
    /*userInfo[kUserInfoKeyActionTriggerSpecId] = actionTriggerSpecId; */
    [userInfo  addEntriesFromDictionary:superInfo];
    return userInfo;

}
    

- (BOOL)isEqualToNotificationInfo:(PacoExtendedNotificationInfo*)info {
    if ([self.experimentId isEqualToString:info.experimentId] &&
        [self.experimentTitle isEqualToString:info.experimentTitle] &&
        [self.fireDate isEqualToDate:info.fireDate] &&
        [self.timeOutDate isEqualToDate:info.timeOutDate] &&
        [self.groupId isEqualToString:info.groupId]  &&
        [self.groupName isEqualToString:info.groupName] &&
        [self.actionTriggerId isEqualToString:info.actionTriggerId]  

        )
    {
        return YES;
    } else {
        return NO;
    }
}
 

@end
