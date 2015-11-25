//
//  PAExperimentDAO+Helper.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/11/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PacoNotificationConstants.h"
#import "PacoNotificationAction.h"
#import  "ActionSpecification.h"




@class PacoScheduleDelegate;
@class PacoExperiment;
@class PAExperimentDAO;




@interface NotificationData

@property (strong, nonatomic) NSDate*  fireDate;
@property (strong,nonatomic) PAActionSpecification* actionSpec;
@property NSTimeInterval timeoutInterval;

@end




typedef void(^NotificationProcessBlock)(UILocalNotification* activeNotification,
                                        NSArray* expiredNotifications,
                                        NSArray* notFiredNotifications);

    
 

typedef void(^NotificationReplaceBlock)(UILocalNotification* active,
                                        NSArray* expired,
                                        NSArray* toBeCanceled,
                                        NSArray* toBeScheduled);

typedef void(^FetchExpiredBlock)(NSArray* expiredNotifications, NSArray* nonExpiredNotifications);

@interface UILocalNotification (PacoExtended)

- (PacoNotificationStatus)pacoStatusExt;
- (NSString*)pacoStatusDescriptionExt;
- (NSString*)pacoDescriptionExt;


+ (NSArray*)pacoNotificationsForExperimentSpecifications:(NSArray*) specifications;

+ (UILocalNotification*) pacoNotificationWithExperimentId:(NSString*)experimentId
                                          experimentTitle:(NSString*)experimentTitle
                                                 fireDate:(NSDate*)fireDate
                                              timeOutDate:(NSDate*)timeOutDate
                                                  groupId:(NSString*) groupId
                                                groupName:(NSString*) groupName
                                                triggerId:(NSString*) triggerId
                                     notificationActionId:(NSString*) notificationActionId
                                      actionTriggerSpecId:(NSString*) actionTriggerSpecId;


+ (NSArray*) pacoNotificationsForExperiment:(PAExperimentDAO * ) experiment  Delegate:(PacoScheduleDelegate*) schedulerDelegate;



//datesToSchedule Must be sorted already


- (NSString*)pacoExperimentIdExt;
- (NSString*)pacoExperimentTitleExt;
- (NSDate*)pacoFireDateExt;
- (NSDate*)pacoTimeoutDateExt;
- (long)pacoTimeoutMinutesExt;

+ (NSArray*)scheduledLocalNotificationsForExperimentExt:(NSString*)experimentInstanceId;
+ (BOOL)hasLocalNotificationScheduledForExperimentExt:(NSString*)experimentInstanceId;
+ (void)cancelScheduledNotificationsForExperimentExt:(NSString*)experimentInstanceId;

+ (void)pacoCancelLocalNotificationExt:(UILocalNotification*)notification;
+ (void)pacoCancelNotificationsExt:(NSArray*)notifications;
+ (void)pacoScheduleNotificationsExt:(NSArray*)notifications;

//notifications MUST be sorted already
+ (void)pacoProcessNotificationsExt:(NSArray*)notifications withBlock:(NotificationProcessBlock)block;
+ (void)pacoFetchExpiredNotificationsFromExt:(NSArray*)notifications withBlock:(FetchExpiredBlock)block;

//{ NSString : NSMutableArray }
+ (NSDictionary*)pacoSortedDictionaryFromNotificationsExt:(NSArray*)notifications;

- (BOOL)pacoIsSame:(UILocalNotification*)notification;

+ (void)pacoReplaceCurrentNotificationsExt:(NSArray*)currentNotifications
                   withNewNotifications:(NSArray*)newNotifications
                               andBlock:(NotificationReplaceBlock)block;

@end
