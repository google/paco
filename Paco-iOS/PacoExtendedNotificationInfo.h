//
//  PacoExtendedNotificationInfo.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/11/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//
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
