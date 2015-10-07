//
//  PacoNotificationCategoryExtendedTests.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/15/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//



/*
 
 
  IMPORTANT: To run this test make sure you have an experiment with startTime 
  and endTime equal to today and one event firing in the future.
 
 
 
 
 */


#import <XCTest/XCTest.h>
#import "ExperimentDAO.h" 
#import "PacoNotificationConstants.h"
#import "PacoSerializer.h" 
#import "java/util/ArrayList.h" 
#import "ExperimentDAO.h" 
#import "DateTime.h"
#include "EsmSignalStore.h"
#include "EventStore.h" 
#import "ActionSpecification.h"
#import "ActionScheduleGenerator.h"
#import "PacoEventStore.h"
#import "PacoSignalStore.h" 
#import "java/lang/Long.h"
#import "NSObject+J2objcKVO.h"
#import "NSDate+Paco.h" 
#import "OrgJodaTimeDateTime+PacoDateHelper.h"
#import "PacoNotificationConstants.h"
#import "PacoExtendedNotificationInfo.h"
#import "UILocalNotification+PacoExteded.h"
#import "PacoMediator.h"
#import "PacoScheduler.h"
#import "ActionSpecification.h"
#import "PacoDateUtility.h"
#import "PacoMediator.h" 
#import "PAExperimentDAO+Helper.h"
#import "PacoSchedulingUtil.h"




@interface PacoNotificationCategoryExtendedTests : XCTestCase

@property(nonatomic, strong) NSString* testID;
@property(nonatomic, strong) NSString* testTitle;
@property(nonatomic, strong) NSDate* testFireDate;
@property(nonatomic, strong) NSDate* testTimeoutDate;
@property(nonatomic, strong) PAExperimentDAO* experiment;
@property(nonatomic, strong) NSArray* testDatesToSchedule;
@property(nonatomic, strong) NSArray* fireTimes;
@property(nonatomic, strong) NSString* groupId;
@property(nonatomic, strong) NSString* groupName;
@property(nonatomic, strong) NSString* actionTriggerId;
@property(nonatomic, strong) NSString* notificationActionId;
@property(nonatomic, strong) NSString* triggerSpecId;
@property (nonatomic, retain) PacoScheduler *scheduler;
@property (nonatomic, retain) PacoSchedulingUtil *schedulerDelegate;
@property (nonatomic, retain) NSArray *specifications;
@property (nonatomic, retain) PAExperimentDAO *dao;
@property long timeoutVal;
@end


static NSString* def3 = @"{\r\n  \"title\": \"Only One Event Today (rolling)\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5682121540632576,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/09\/24\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 21,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/9\/24\",\r\n      \"endDate\": \"2015\/9\/24\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1442592233059,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1442592233058,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 46800000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1442592233060,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";




@interface PacoMediator ()

@property (strong,nonatomic ) NSMutableArray* allExperiments;
@property (strong,nonatomic)   NSMutableArray* runningExperiments;
@property (strong,nonatomic)  NSMutableArray* actionSpecifications;
@property (strong,nonatomic ) NSMutableArray* oldActionSpecifications;
/* verifitcation protocols */
@property (strong,nonatomic ) NSMutableArray* willStartVerifiers;
@property (strong,nonatomic ) NSMutableArray*  didStartNotifiers;
@property (strong,nonatomic ) NSMutableArray*  willStopVerifiers;
@property (strong,nonatomic ) NSMutableArray*  didStopNotifiers;

@end

@implementation PacoNotificationCategoryExtendedTests
 


- (void)setUp {
    [super setUp];
    
    
    
    static dispatch_once_t once;
  
    //dispatch_once(&once, ^{
        [self setup];
   // });
        
    
    
  
 
}



-(void) setup
{
    
    
    
    PacoSignalStore * signalStore          =  [[PacoSignalStore alloc] init];
    PacoEventStore  * eventStore           =  [[PacoEventStore  alloc] init];
    PAExperimentDAO*  experiment  = [self experimentDAO:3];
    
    
    
    [[PacoMediator sharedInstance] clearRunningExperiments];
    
    
    [[PacoMediator sharedInstance] addExperimentToAvailableStore:experiment];
    
    [[PacoMediator sharedInstance] startRunningExperiment:[experiment instanceId]];
    
    
    NSMutableDictionary * results  = [[NSMutableDictionary alloc] init];
    [results setObject:[NSMutableArray new] forKey:[self uniqueId:experiment]];
    [self getFireTimes:experiment  results:results SignalStore:signalStore EventStore:eventStore];
    NSArray* definitions  = [results objectForKey:[self uniqueId:experiment]];
    PAActionSpecification*  spec = [definitions firstObject];
    
    _fireTimes = definitions;
    
    self.testID =              [[experiment valueForKeyEx:@"id"] stringValue];
    self.testTitle =           [experiment valueForKeyEx:@"title"]  ;
    self.testFireDate =        [[spec valueForKey:@"time_"]  nsDateValue];
    long  timeoutVal =         [ [experiment valueForKeyPathEx:@"groups[0].actionTriggers[0].actions[0].timeout"] longValue];
    self.testTimeoutDate =      [NSDate dateWithTimeInterval:timeoutVal  sinceDate:self.testFireDate];
    self.groupId =               [experiment valueForKeyPathEx:@"groups[0].name"] ;
    self.groupName =             [experiment valueForKeyPathEx:@"groups[0].name"]  ;
    self.actionTriggerId=        [[experiment valueForKeyPathEx:@"groups[0].actionTriggers[0].id"] stringValue];
    
    
    self.notificationActionId  =   self.notificationActionId  = [[experiment valueForKeyPathEx:@"groups[0].actionTriggers[0].actions[0].id"] stringValue];
    self.triggerSpecId = @"trigger spec id";
    
    
    
    
    _schedulerDelegate = [[PacoSchedulingUtil alloc] init];
    
    
    
    
    
    
   // buildActionSpecifications:(NSArray*) experiments IsDryRun:(BOOL) isTryRun;
    

    _dao = experiment;
    
    
    
    /* do this just to remove notfications */
    [UIApplication sharedApplication].scheduledLocalNotifications = [NSArray new];
    
    
}








/* test creation of PacoInfo */
- (void)testCreatePacoInfo {
    
    
    
 
    
    NSDictionary* dict = @{kUserInfoKeyExperimentId:self.testID,
                           kUserInfoKeyExperimentTitle:self.testTitle,
                           kUserInfoKeyNotificationFireDate:self.testFireDate,
                           kUserInfoKeyNotificationTimeoutDate:self.testTimeoutDate,
                           kNotificationGroupId:self.groupId,
                           kNotificationGroupName:self.groupName,
                           kUserInfoKeyActionTriggerId:self.actionTriggerId,
                           kUserInfoKeyNotificationActionId:self.notificationActionId,
                           };
    
    
    PacoExtendedNotificationInfo* info = [PacoExtendedNotificationInfo pacoInfoWithDictionary:dict];
    
    
    XCTAssertTrue(info != nil, @"should have a valid info");
    XCTAssertEqualObjects(info.experimentId, self.testID, @"should have a valid experiment id");
    XCTAssertEqualObjects(info.fireDate, self.testFireDate, @"should have a valid fire date");
    XCTAssertEqualObjects(info.timeOutDate, self.testTimeoutDate, @"should have a valid timeout date");
    XCTAssertEqualObjects(info.groupId, self.groupId, @"should have a valid timeout date");
    XCTAssertEqualObjects(info.groupName, self.groupName, @"should have a valid timeout date");
    XCTAssertEqualObjects(info.actionTriggerId, self.actionTriggerId, @"should have a valid timeout date");
    XCTAssertEqualObjects(info.notificationActionId, self.notificationActionId, @"should have a valid timeout date");
    
}






- (void)testCreatePacoInfo1 {
    NSDictionary* dict = @{@"experimentInstanceId":self.testID};
    XCTAssertEqualObjects([PacoExtendedNotificationInfo pacoInfoWithDictionary:dict], nil,
                          @"missing information will get a nil result");
}



- (void)testCreatePacoInfo2 {
    NSDictionary* dict = @{@"notificationFireDate":self.testFireDate};
    XCTAssertEqualObjects([PacoExtendedNotificationInfo pacoInfoWithDictionary:dict], nil,
                          @"missing information will get a nil result");
}

- (void)testCreatePacoInfo3 {
    NSDictionary* dict = @{@"id":self.testID,
                           @"notificationFireDate":self.testFireDate,
                           @"notificationTimeoutDate":self.testTimeoutDate};
    XCTAssertEqualObjects([PacoExtendedNotificationInfo pacoInfoWithDictionary:dict], nil,
                          @"wrong dict key will get a nil result");
}


- (void)testStatusNotFired {
    self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:1];//1 seconds after now
    self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:10*60]; //10 minutes after now
    PacoExtendedNotificationInfo* info = [[PacoExtendedNotificationInfo alloc] init];
    [info setValue:self.testID forKey:@"experimentId"];
    [info setValue:self.testFireDate forKey:@"fireDate"];
    [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
    XCTAssertEqual([info status], PacoNotificationStatusNotFired, @"should be not fired");
}




- (void)testStatusFiredNotTimeout {
    PacoExtendedNotificationInfo* info = [[PacoExtendedNotificationInfo alloc] init];
    [info setValue:self.testID forKey:@"experimentId"];
    [info setValue:[NSDate dateWithTimeIntervalSinceNow:0] forKey:@"fireDate"];
    [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
    XCTAssertEqual([info status], PacoNotificationStatusFiredNotTimeout, @"should be fired but not timeout");
}

- (void)testStatusFiredNotTimeout2 {
    self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:-2*60];//two minutes before now
    self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:1]; //one second after now
    PacoExtendedNotificationInfo* info = [[PacoExtendedNotificationInfo alloc] init];
    [info setValue:self.testID forKey:@"experimentId"];
    [info setValue:self.testFireDate forKey:@"fireDate"];
    [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
    XCTAssertEqual([info status], PacoNotificationStatusFiredNotTimeout, @"should be fired but not timeout");
}

- (void)testStatusFiredNotTimeout3 {
    self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:0];//right now
    self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:1]; //one second after now
    PacoExtendedNotificationInfo* info = [[PacoExtendedNotificationInfo alloc] init];
    [info setValue:self.testID forKey:@"experimentId"];
    [info setValue:self.testFireDate forKey:@"fireDate"];
    [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
    XCTAssertEqual([info status], PacoNotificationStatusFiredNotTimeout, @"should be fired but not time out");
}


- (void)testStatusTimeout {
    self.testFireDate = [NSDate dateWithTimeIntervalSinceNow:-2*60];//two minutes before now
    self.testTimeoutDate = [NSDate dateWithTimeIntervalSinceNow:0]; //right now
    PacoExtendedNotificationInfo* info = [[PacoExtendedNotificationInfo alloc] init];
    [info setValue:self.testID forKey:@"experimentId"];
    [info setValue:self.testFireDate forKey:@"fireDate"];
    [info setValue:self.testTimeoutDate forKey:@"timeOutDate"];
    XCTAssertEqual([info status], PacoNotificationStatusTimeout, @"should be time out");
}



- (void)testCreateNotification {
    
    self.testFireDate = [NSDate date];
    
    UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:self.testID  experimentTitle:self.testTitle  fireDate:self.testFireDate timeOutDate:self.testTimeoutDate    groupId:self.groupId groupName:self.groupName triggerId:self.actionTriggerId notificationActionId:self.notificationActionId actionTriggerSpecId:self.triggerSpecId];
    
    
    XCTAssertEqualObjects(noti.timeZone, [NSTimeZone systemTimeZone], @"should be system timezone");
    XCTAssertEqualObjects(noti.fireDate, self.testFireDate, @"firedate should be valid");
    /* NSString* expectAlertBody = [NSString stringWithFormat:@"[%@]%@",
     [PacoDateUtility stringForAlertBodyFromDate:self.testFireDate],
     self.testTitle];
     */
    
    
    /* the title should be templated  in a common file */
    
    NSString* exptected = [NSString stringWithFormat:@"%@\nTime to participate!",self.testTitle];
    XCTAssertEqualObjects(noti.alertBody, exptected, @"alert body should be valid");
    XCTAssertEqualObjects(noti.soundName, @"deepbark_trial.mp3", @"sound name should be valid");
    
    
    NSDictionary* userInfo = @{@"id":self.testID,
                               @"fireDate":self.testFireDate,
                               @"timeoutDate":self.testTimeoutDate,
                               @"title":self.testTitle,
                               @"actionTriggerId":self.actionTriggerId,
                               @"groupId":self.groupId,
                               @"groupName":self.groupName,
                               @"notificationActionId":self.notificationActionId
                               
                            };
    XCTAssertEqualObjects(noti.userInfo, userInfo, @"userInfo should be valid");
    
}


- (void)testCreateNotification2 {
    
   UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:self.testID  experimentTitle:@""  fireDate:self.testFireDate timeOutDate:self.testTimeoutDate    groupId:self.groupId groupName:self.groupName triggerId:self.actionTriggerId notificationActionId:self.notificationActionId actionTriggerSpecId:self.triggerSpecId];
    
    XCTAssertEqualObjects(noti, nil, @"noti should be invalid with an empty experiment id");
}


- (void)testPacoStatus {
    
    
    UILocalNotification* noti = [UILocalNotification pacoNotificationWithExperimentId:@""  experimentTitle:self.testTitle  fireDate:self.testFireDate timeOutDate:self.testTimeoutDate    groupId:self.groupId groupName:self.groupName triggerId:self.actionTriggerId notificationActionId:self.notificationActionId actionTriggerSpecId:self.triggerSpecId];
    
    
    XCTAssertEqualObjects(noti, nil, @"noti should be nil if experiment id is not valid");
    XCTAssertEqual([noti pacoStatusExt], PacoNotificationStatusUnknown, @"a nil notification should be unknown status");
}



- (void)testPacoNotificationsForNilExperiment {
    NSArray* notifications = [UILocalNotification pacoNotificationsForExperimentSpecifications:@[]];
                              
    XCTAssertNil(notifications, @"should return nil if experiment is nil");
}

/*
- (void)testPacoNotificationsForExperiment
{
 
    
    NSArray* notifications = [UILocalNotification pacoNotificationsForExperiment:self.dao  Delegate:_schedulerDelegate];
  
    
    NSMutableArray* expect = [NSMutableArray array];
    

    NSString* alertBody = [NSString stringWithFormat:@"%@\n%@",
                               self.testTitle,@"Time to participate!"];
  
        NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
        userInfo[kUserInfoKeyExperimentId] = _testID;
        userInfo[kUserInfoKeyNotificationFireDate] = self.testFireDate;
        userInfo[kNotificationGroupId] = self.groupId;
        userInfo[kNotificationGroupName] = self.groupName;
        userInfo[kUserInfoKeyExperimentTitle] = self.testTitle;
        userInfo[kUserInfoKeyNotificationTimeoutDate] = self.testTimeoutDate;
        userInfo[kUserInfoKeyActionTriggerId] = self.actionTriggerId;
        userInfo[kUserInfoKeyNotificationActionId] = self.notificationActionId;
    
        UILocalNotification *notification = [[UILocalNotification alloc] init];
        notification.timeZone = [NSTimeZone systemTimeZone];
        notification.fireDate = self.testFireDate;
        notification.alertBody = alertBody;
        notification.soundName = kNotificationSoundName;
        notification.userInfo = userInfo;
        [expect addObject:notification];
        BOOL isSame =   [[notifications firstObject] pacoIsSame:[expect firstObject]];
   
        XCTAssertTrue(isSame,"notification objects should be the same");
    
   
}
*/


- (void)testPacoNotificationsForEmptyDates {
 
    
      NSArray* notifications = [UILocalNotification pacoNotificationsForExperiment:self.dao  Delegate:nil];
    XCTAssertNil(notifications, @"should return nil if dates is empty");
}


- (void)testPacoProcessNotificationsWithNilBlock {
    UILocalNotification* testNoti = [[UILocalNotification alloc] init];
    [UILocalNotification pacoProcessNotificationsExt:@[testNoti] withBlock:nil];
}

- (void)testPacoProcessNotificationsWithEmptyNotifications {
    NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                       NSArray* expiredNotifications,
                                       NSArray* notFiredNotifications) {
        XCTAssertNil(activeNotification, @"should be nil");
        XCTAssertNil(expiredNotifications, @"should be nil");
        XCTAssertNil(notFiredNotifications, @"should be nil");
    };
    [UILocalNotification pacoProcessNotificationsExt:nil withBlock:block];
}



- (void)testPacoProcessNotificationsTimeoutAndActive {
    NSString* experimentId = @"1";
    NSString* experimentTitle = @"title";
    NSTimeInterval timeoutInterval = 5;//5 seconds
    
    NSDate* now = [NSDate date];
    NSDate* date1 = [NSDate dateWithTimeInterval:-20 sinceDate:now]; //timeout
    NSDate* date2 = [NSDate dateWithTimeInterval:-3 sinceDate:now]; //active
    
  
  /* [UILocalNotification pacoNotificationWithExperimentId:experimentId
                                          experimentTitle:experimentTitle
                                                 fireDate:date1
                                              timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1]];*/
    
    
    
    
    
    
      UILocalNotification* timeoutNotification =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                           experimentTitle:self.testTitle
                                                  fireDate:date2
                                               timeOutDate:[NSDate
                                                dateWithTimeInterval:timeoutInterval
                                                            sinceDate:date1]
                                                   groupId:self.groupId
                                                 groupName:self.groupName
                                                 triggerId:self.actionTriggerId
                                      notificationActionId:self.notificationActionId
                                       actionTriggerSpecId:@"23456"];

    
  
    
    
    UILocalNotification* activeNotification =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                              experimentTitle:self.testTitle
                                                                                     fireDate:date2
                                                                                  timeOutDate:[NSDate
                                                                                               dateWithTimeInterval:timeoutInterval
                                                                                               sinceDate:date2]
                                                                                      groupId:self.groupId
                                                                                    groupName:self.groupName
                                                                                    triggerId:self.actionTriggerId
                                                                         notificationActionId:self.notificationActionId
                                                                          actionTriggerSpecId:@"23456"];
    
    
    
    
    NSArray* allNotifications = @[activeNotification];
    NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                       NSArray* expiredNotifications,
                                       NSArray* notFiredNotifications) {
        XCTAssertEqualObjects(activeNotification, activeNotification, @"should have an active notification");
         //XCTAssertEqualObjects(expiredNotifications, @[timeoutNotification], @"should have one expired notification");
         XCTAssertNil(notFiredNotifications, @"should be nil");
        
        
    };
                                                      
                                                      
    [UILocalNotification pacoProcessNotificationsExt:allNotifications withBlock:block];
}



- (void)testPacoProcessNotificationsTimeoutObsoleteAndActive {
    NSString* experimentId = @"1";
    NSString* experimentTitle = @"title";
    NSTimeInterval timeoutInterval = 20;//20 seconds
    
    NSDate* now = [NSDate date];
    NSDate* date1 = [NSDate dateWithTimeInterval:-25 sinceDate:now]; //timeout
    NSDate* date2 = [NSDate dateWithTimeInterval:-15 sinceDate:now]; //obsolete
    NSDate* date3 = [NSDate dateWithTimeInterval:-5 sinceDate:now]; //active
    
    
    
    
    
    UILocalNotification* timeoutNotification =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                                      experimentTitle:self.testTitle
                                                                                             fireDate:date1
                                                                                          timeOutDate:[NSDate
                                                                                                       dateWithTimeInterval:timeoutInterval
                                                                                                       sinceDate:date1]
                                                                                              groupId:self.groupId
                                                                                            groupName:self.groupName
                                                                                            triggerId:self.actionTriggerId
                                                                                 notificationActionId:self.notificationActionId
                                                                                  actionTriggerSpecId:@"23456"];

    
    
    UILocalNotification* timeoutNoti =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                                      experimentTitle:self.testTitle
                                                                                             fireDate:date1
                                                                                          timeOutDate:[NSDate
                                                                                                       dateWithTimeInterval:timeoutInterval
                                                                                                       sinceDate:date1]
                                                                                              groupId:self.groupId
                                                                                            groupName:self.groupName
                                                                                            triggerId:self.actionTriggerId
                                                                                 notificationActionId:self.notificationActionId
                                                                                  actionTriggerSpecId:@"23456"];
    

    
    
    UILocalNotification* obsoleteNoti =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                                      experimentTitle:self.testTitle
                                                                                             fireDate:date2
                                                                                          timeOutDate:[NSDate
                                                                                                       dateWithTimeInterval:timeoutInterval
                                                                                                       sinceDate:date2]
                                                                                              groupId:self.groupId
                                                                                            groupName:self.groupName
                                                                                            triggerId:self.actionTriggerId
                                                                                 notificationActionId:self.notificationActionId
                                                                                  actionTriggerSpecId:@"23456"];
    

    
    
    UILocalNotification* activeNoti =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                                      experimentTitle:self.testTitle
                                                                                             fireDate:date3
                                                                                          timeOutDate:[NSDate
                                                                                                       dateWithTimeInterval:timeoutInterval
                                                                                                       sinceDate:date3]
                                                                                              groupId:self.groupId
                                                                                            groupName:self.groupName
                                                                                            triggerId:self.actionTriggerId
                                                                                 notificationActionId:self.notificationActionId
                                                                                  actionTriggerSpecId:@"23456"];
    
    
    
    
    NSArray* allNotifications = @[timeoutNoti, obsoleteNoti, activeNoti];
    NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                       NSArray* expiredNotifications,
                                       NSArray* notFiredNotifications) {
        XCTAssertEqualObjects(activeNotification, activeNoti, @"should have one active notification");
        NSArray* expiredNotis = @[timeoutNoti, obsoleteNoti];
        XCTAssertEqualObjects(expiredNotifications, expiredNotis, @"should have two expired notifications");
        XCTAssertNil(notFiredNotifications, @"should be nil");
    };
    
    
    
    [UILocalNotification pacoProcessNotificationsExt:allNotifications withBlock:block];
}



- (void)testPacoProcessNotificationsExpiredActiveAndScheduled {
    NSString* experimentId = @"1";
    NSString* experimentTitle = @"title";
    NSTimeInterval timeoutInterval = 20;//20 seconds
    
    NSDate* now = [NSDate date];
    NSDate* date1 = [NSDate dateWithTimeInterval:-25 sinceDate:now]; //timeout
    NSDate* date2 = [NSDate dateWithTimeInterval:-15 sinceDate:now]; //obsolete
    NSDate* date3 = [NSDate dateWithTimeInterval:-5 sinceDate:now]; //active
    NSDate* date4 = [NSDate dateWithTimeInterval:5 sinceDate:now]; //scheduled 1
    NSDate* date5 = [NSDate dateWithTimeInterval:10 sinceDate:now]; //scheduled 2
    
    

    
    
    
    UILocalNotification* timeoutNoti =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                             experimentTitle:self.testTitle
                                                                                    fireDate:date1
                                                                                 timeOutDate:[NSDate
                                                                                              dateWithTimeInterval:timeoutInterval
                                                                                              sinceDate:date1]
                                                                                     groupId:self.groupId
                                                                                   groupName:self.groupName
                                                                                   triggerId:self.actionTriggerId
                                                                        notificationActionId:self.notificationActionId
                                                                     actionTriggerSpecId:@"23456"];
    

                                        
                                        
    UILocalNotification* obsoleteNoti =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                                         experimentTitle:self.testTitle
                                                                                            fireDate:date2
                                                                                         timeOutDate:[NSDate
                                                                                                      dateWithTimeInterval:timeoutInterval
                                                                                                      sinceDate:date2]
                                                                                             groupId:self.groupId
                                                                                           groupName:self.groupName
                                                                                           triggerId:self.actionTriggerId
                                                                                notificationActionId:self.notificationActionId
                                                                                 actionTriggerSpecId:@"23456"];
                                        
                                        
                                        

                                        
                                        
UILocalNotification* activeNoti =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                       experimentTitle:self.testTitle
                                                                              fireDate:date3
                                                                           timeOutDate:[NSDate
                                                                                        dateWithTimeInterval:timeoutInterval
                                                                                        sinceDate:date3]
                                                                               groupId:self.groupId
                                                                             groupName:self.groupName
                                                                             triggerId:self.actionTriggerId
                                                                  notificationActionId:self.notificationActionId
                                                                   actionTriggerSpecId:@"23456"];
                                        

                                        
                                        
 
UILocalNotification* scheduledNoti1 =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                         experimentTitle:self.testTitle
                                                                                fireDate:date4
                                                                             timeOutDate:[NSDate
                                                                                          dateWithTimeInterval:timeoutInterval
                                                                                          sinceDate:date4]
                                                                                 groupId:self.groupId
                                                                               groupName:self.groupName
                                                                               triggerId:self.actionTriggerId
                                                                    notificationActionId:self.notificationActionId
                                                                     actionTriggerSpecId:@"23456"];
                                        
                                        
                                        

                                         
                                         
                                         
 UILocalNotification* scheduledNoti2 =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                          experimentTitle:self.testTitle
                                                                                 fireDate:date4
                                                                              timeOutDate:[NSDate
                                                                                           dateWithTimeInterval:timeoutInterval
                                                                                           sinceDate:date4]
                                                                                  groupId:self.groupId
                                                                                groupName:self.groupName
                                                                                triggerId:self.actionTriggerId
                                                                     notificationActionId:self.notificationActionId
                                                                      actionTriggerSpecId:@"23456"];
                                         
    
    NSArray* allNotifications = @[timeoutNoti, obsoleteNoti, activeNoti, scheduledNoti1, scheduledNoti2];
    
    
    
    NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                       NSArray* expiredNotifications,
                                       NSArray* notFiredNotifications) {
        
        XCTAssertTrue( activeNotification !=nil , @"should have one active notifications");
        XCTAssertTrue([expiredNotifications count]==2, @"should have two expired notifications");
        XCTAssertTrue([expiredNotifications count]==2, @"should have two scheduled notifications");
        
    };
    
    [UILocalNotification pacoProcessNotificationsExt:allNotifications withBlock:block];
}

- (void)testPacoProcessNotificationsTimeoutAndScheduled {
    NSString* experimentId = @"1";
    NSString* experimentTitle = @"title";
    NSTimeInterval timeoutInterval = 20;//20 seconds
    
    NSDate* now = [NSDate date];
    NSDate* date1 = [NSDate dateWithTimeInterval:-25 sinceDate:now]; //timeout
    NSDate* date2 = [NSDate dateWithTimeInterval:-22 sinceDate:now]; //timeout
    NSDate* date3 = [NSDate dateWithTimeInterval:10 sinceDate:now]; //scheduled
    NSDate* date4 = [NSDate dateWithTimeInterval:20 sinceDate:now]; //scheduled
    
    
    
    
    
    
    UILocalNotification* timeoutNoti1 =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                              experimentTitle:self.testTitle
                                                                                     fireDate:date1
                                                                                  timeOutDate:[NSDate
                                                                                               dateWithTimeInterval:timeoutInterval
                                                                                               sinceDate:date1]
                                                                                      groupId:self.groupId
                                                                                    groupName:self.groupName
                                                                                    triggerId:self.actionTriggerId
                                                                         notificationActionId:self.notificationActionId
                                                                          actionTriggerSpecId:@"23456"];
   
    
    UILocalNotification* timeoutNoti2 =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                               experimentTitle:self.testTitle
                                                                                      fireDate:date2
                                                                                   timeOutDate:[NSDate
                                                                                                dateWithTimeInterval:timeoutInterval
                                                                                                sinceDate:date2]
                                                                                       groupId:self.groupId
                                                                                     groupName:self.groupName
                                                                                     triggerId:self.actionTriggerId
                                                                          notificationActionId:self.notificationActionId
                                                                           actionTriggerSpecId:@"23456"];
    
    
    
    UILocalNotification* scheduledNoti1 =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                             experimentTitle:self.testTitle
                                                                                    fireDate:date3
                                                                                 timeOutDate:[NSDate
                                                                                              dateWithTimeInterval:timeoutInterval
                                                                                              sinceDate:date3]
                                                                                     groupId:self.groupId
                                                                                   groupName:self.groupName
                                                                                   triggerId:self.actionTriggerId
                                                                        notificationActionId:self.notificationActionId
                                                                         actionTriggerSpecId:@"23456"];
    

    
    UILocalNotification* scheduledNoti2 =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                                 experimentTitle:self.testTitle
                                                                                        fireDate:date4
                                                                                     timeOutDate:[NSDate
                                                                                                  dateWithTimeInterval:timeoutInterval
                                                                                                  sinceDate:date4]
                                                                                         groupId:self.groupId
                                                                                       groupName:self.groupName
                                                                                       triggerId:self.actionTriggerId
                                                                            notificationActionId:self.notificationActionId
                                                                             actionTriggerSpecId:@"23456"];
    

    NSArray* allNotifications = @[timeoutNoti1, timeoutNoti2,scheduledNoti1, scheduledNoti2];
    NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                       NSArray* expiredNotifications,
                                       NSArray* notFiredNotifications) {
        
        XCTAssertNil(activeNotification, @"should be nil");
        XCTAssertTrue([expiredNotifications count] ==2, @"should have two expired notifications ");
        XCTAssertTrue([notFiredNotifications count] == 2, @"should have two not fired notifications");
     
    };
    
    [UILocalNotification pacoProcessNotificationsExt:allNotifications withBlock:block];
}


- (void)testPacoProcessNotificationsOnlyTimeOut {

    NSString* experimentTitle = @"title";
    NSTimeInterval timeoutInterval = 20;//20 seconds
    
    NSDate* now = [NSDate date];
    NSDate* date1 = [NSDate dateWithTimeInterval:-25 sinceDate:now]; //timeout
    NSDate* date2 = [NSDate dateWithTimeInterval:-22 sinceDate:now]; //timeout
    
    NSString* alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date1], experimentTitle];
    
    
    
    
    UILocalNotification* timeoutNoti1 =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                                 experimentTitle:alertBody
                                                                                        fireDate:date1
                                                                                     timeOutDate:[NSDate
                                                                                                  dateWithTimeInterval:timeoutInterval
                                                                                                  sinceDate:date1]
                                                                                         groupId:self.groupId
                                                                                       groupName:self.groupName
                                                                                       triggerId:self.actionTriggerId
                                                                            notificationActionId:self.notificationActionId
                                                                              actionTriggerSpecId:@"23456"];
    
    
    alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date2], experimentTitle];

    UILocalNotification* timeoutNoti2 =  [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                                                 experimentTitle:alertBody
                                                                                        fireDate:date1
                                                                                     timeOutDate:[NSDate
                                                                                                  dateWithTimeInterval:timeoutInterval
                                                                                                  sinceDate:date1]
                                                                                         groupId:self.groupId
                                                                                       groupName:self.groupName
                                                                                       triggerId:self.actionTriggerId
                                                                            notificationActionId:self.notificationActionId
                                                                             actionTriggerSpecId:@"23456"];
    

    NSArray* allNotifications = @[timeoutNoti1, timeoutNoti2];
    NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                       NSArray* expiredNotifications,
                                       NSArray* notFiredNotifications) {
        XCTAssertNil(activeNotification, @"should be nil");
        XCTAssertTrue([expiredNotifications count] ==2, @"should have two expired notifications ");
        XCTAssertNil(notFiredNotifications, @"should be nil");
        
        
    };
    
    [UILocalNotification pacoProcessNotificationsExt:allNotifications withBlock:block];
    
}





- (void)testPacoProcessNotificationActiveAndScheduled {
        NSString* experimentTitle = @"title";
    NSTimeInterval timeoutInterval = 20;//20 seconds
    
    NSDate* now = [NSDate date];
    NSDate* date3 = [NSDate dateWithTimeInterval:-5 sinceDate:now]; //active
    NSDate* date4 = [NSDate dateWithTimeInterval:5 sinceDate:now]; //scheduled 1
    NSDate* date5 = [NSDate dateWithTimeInterval:10 sinceDate:now]; //scheduled 2
    
    NSString* alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date3], experimentTitle];
    UILocalNotification* activeNoti =

    
   [ UILocalNotification pacoNotificationWithExperimentId:self.testID
                                                            experimentTitle:alertBody
                                                            fireDate:date3
                                                            timeOutDate:[NSDate
                                                                                            dateWithTimeInterval:timeoutInterval
                                                                                            sinceDate:date3]
                                                            groupId:self.groupId
                                                            groupName:self.groupName
                                                            triggerId:self.actionTriggerId
                                                            notificationActionId:self.notificationActionId
                                                            actionTriggerSpecId:@"23456"];
    
    

    
    alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date4], experimentTitle];
    
    
    
    UILocalNotification* scheduledNoti1 =
    
    [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                          experimentTitle:alertBody
                                                 fireDate:date4
                                              timeOutDate:[NSDate
                                                           dateWithTimeInterval:timeoutInterval
                                                           sinceDate:date4]
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    
    
    
    

    
    
    
    alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date5], experimentTitle];
    UILocalNotification* scheduledNoti2 =
        [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                          experimentTitle:alertBody
                                                 fireDate:date5
                                              timeOutDate:[NSDate
                                                           dateWithTimeInterval:timeoutInterval
                                                           sinceDate:date5]
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    
    
    
    
 
    
    NSArray* allNotifications = @[activeNoti, scheduledNoti1, scheduledNoti2];
    NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                       NSArray* expiredNotifications,
                                       NSArray* notFiredNotifications) {
        XCTAssertEqualObjects(activeNotification, activeNoti, @"should have one active notification");
        XCTAssertNil(expiredNotifications, @"should be nil");
        NSArray* scheduledNotis = @[scheduledNoti1, scheduledNoti2];
        XCTAssertEqualObjects(notFiredNotifications, scheduledNotis, @"should have two scheduled notifications");
    };
    [UILocalNotification pacoProcessNotificationsExt:allNotifications withBlock:block];
}



- (void)testPacoProcessNotificationOnlyScheduled {
 
    NSString* experimentTitle = @"title";
    NSTimeInterval timeoutInterval = 20;//20 seconds
    
    NSDate* now = [NSDate date];
    NSDate* date4 = [NSDate dateWithTimeInterval:5 sinceDate:now]; //scheduled 1
    NSDate* date5 = [NSDate dateWithTimeInterval:10 sinceDate:now]; //scheduled 2
    
    NSString* alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date4], experimentTitle];
     UILocalNotification* scheduledNoti1 =
    [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                          experimentTitle:alertBody
                                                 fireDate:date4
                                              timeOutDate:[NSDate
                                                           dateWithTimeInterval:timeoutInterval
                                                           sinceDate:date4]
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    
    
    
    
    
    alertBody = [NSString stringWithFormat:@"[%@]%@", [PacoDateUtility stringForAlertBodyFromDate:date5], experimentTitle];
  
    
    UILocalNotification* scheduledNoti2 =
    [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                          experimentTitle:alertBody
                                                 fireDate:date5
                                              timeOutDate:[NSDate
                                                           dateWithTimeInterval:timeoutInterval
                                                           sinceDate:date5]
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    
    
    
    
   NSArray* allNotifications = @[scheduledNoti1, scheduledNoti2];
    NotificationProcessBlock block = ^(UILocalNotification* activeNotification,
                                       NSArray* expiredNotifications,
                                       NSArray* notFiredNotifications) {
        XCTAssertNil(activeNotification, @"should be nil");
        XCTAssertNil(expiredNotifications, @"should be nil");
        XCTAssertTrue([notFiredNotifications count]==2, @" should havel two scheduled notifications");
       
     
    };
    
    [UILocalNotification pacoProcessNotificationsExt:allNotifications withBlock:block];
}



- (void)testSortNotificationsPerExperiment {
    NSDate* now = [NSDate date];
    NSDate* date1 = [NSDate dateWithTimeInterval:10 sinceDate:now];
    NSDate* date2 = [NSDate dateWithTimeInterval:20 sinceDate:now];
    NSDate* date3 = [NSDate dateWithTimeInterval:30 sinceDate:now];
    NSDate* date4 = [NSDate dateWithTimeInterval:40 sinceDate:now];
    
    NSTimeInterval timeoutInterval = 479*60;
    NSDate* timeout1 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date1];
    NSDate* timeout2 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date2];
    NSDate* timeout3 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date3];
    NSDate* timeout4 = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:date4];
    
    NSString* experimentId1 = @"1";
    NSString* experimentId2 = @"2";
    NSString* title1 = @"title1";
    NSString* title2 = @"title2";
    
    //id:1, fireDate:date4
    //id:2, fireDate:date3
    //id:1, fireDate:date1
    //id:2, fireDate:date2
    NSMutableArray* allNotifications = [NSMutableArray arrayWithCapacity:4];
    
    //id:1, fireDate:date4
    NSString* alertBody = [NSString stringWithFormat:@"[%@]%@",
                           [PacoDateUtility stringForAlertBodyFromDate:date4],
                           title1];
    UILocalNotification* notification1 =
    [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                          experimentTitle:alertBody
                                                 fireDate:date4
                                              timeOutDate:timeout4
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    
    
    [allNotifications addObject:notification1];
    
    //id:2, fireDate:date3
    alertBody = [NSString stringWithFormat:@"[%@]%@",
                 [PacoDateUtility stringForAlertBodyFromDate:date3],
                 title2];
    UILocalNotification* notification2 =
    [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                          experimentTitle:alertBody
                                                 fireDate:date3
                                              timeOutDate:timeout3
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    
    
    
    
    [allNotifications addObject:notification2];
    
    //id:1, fireDate:date1
    alertBody = [NSString stringWithFormat:@"[%@]%@",
                 [PacoDateUtility stringForAlertBodyFromDate:date1],
                 title1];
    UILocalNotification* notification3 =
    [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                          experimentTitle:alertBody
                                                 fireDate:date1
                                              timeOutDate:timeout1
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    
    
    
    
    [allNotifications addObject:notification3];
    
    //id:2, fireDate:date2
    alertBody = [NSString stringWithFormat:@"[%@]%@",
                 [PacoDateUtility stringForAlertBodyFromDate:date2],
                 title2];
    UILocalNotification* notification4 =
    [UILocalNotification pacoNotificationWithExperimentId:self.testID
                                          experimentTitle:alertBody
                                                 fireDate:date2
                                              timeOutDate:timeout2
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    [allNotifications addObject:notification4];
    
    
    
    
    
    //allNotifications:
    //id:1, fireDate:date4
    //id:2, fireDate:date3
    //id:1, fireDate:date1
    //id:2, fireDate:date2
    NSDictionary* result = [UILocalNotification pacoSortedDictionaryFromNotificationsExt:allNotifications];
    NSMutableDictionary* expect = [NSMutableDictionary dictionaryWithCapacity:2];
    NSArray* notifications1 = @[notification3, notification1];
    NSArray* notifications2 = @[notification4, notification2];
    expect[experimentId1] = notifications1;
    expect[experimentId2] = notifications2;
    
    
    XCTAssertTrue([[result allKeys] count] ==1);
    XCTAssertTrue( [[[result allValues] firstObject] count] ==4);
    
    NSDate* olderDate =   [NSDate dateWithTimeInterval:-100000000 sinceDate:[NSDate date]];
    
    
    NSArray * array =  [[result allValues] firstObject];
    
    for(UILocalNotification* notification in array)
    {
         NSDate* newDate =  [notification pacoFireDateExt];
         XCTAssertTrue(newDate.timeIntervalSince1970 >=  olderDate.timeIntervalSince1970);
        
         olderDate = newDate;
        
    }
    
}


- (void)testCompareAndCancel {
    NSTimeInterval tenMinutes = 60 * 10;
    NSTimeInterval twentyMinutes = 60 * 20;
    NSTimeInterval thirtyMinutes = 60 * 30;
    NSTimeInterval timeoutOne = 20;
    NSTimeInterval timeoutTwo = 40;
    
    NSMutableArray *notifications = [NSMutableArray array];
    
    NSString* testIdOne = @"12345";
    NSString* testTitleOne = @"Experiment One";
    NSDate* dateOne = [NSDate dateWithTimeIntervalSinceNow:tenMinutes];
    NSDate* timeOutDateOne = [dateOne dateByAddingTimeInterval:timeoutOne];
    UILocalNotification* noti11 =
         [UILocalNotification pacoNotificationWithExperimentId:testIdOne
                                          experimentTitle:testTitleOne
                                                 fireDate:dateOne
                                              timeOutDate:timeOutDateOne
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    
    
    [notifications addObject:noti11];
    
    
    
    dateOne = [NSDate dateWithTimeIntervalSinceNow:twentyMinutes];
    timeOutDateOne = [dateOne dateByAddingTimeInterval:timeoutOne];
    
    
    
    UILocalNotification* noti12 =
    
    
    [UILocalNotification pacoNotificationWithExperimentId:testIdOne
                                          experimentTitle:testTitleOne
                                                 fireDate:dateOne
                                              timeOutDate:timeOutDateOne
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    [notifications addObject:noti12];
    
    
    NSString* testIdTwo = @"34567";
    NSString* testTitleTwo = @"Experiment Two";
    NSDate* dateTwo = [NSDate dateWithTimeIntervalSinceNow:twentyMinutes];
    NSDate* timeOutDateTwo = [dateTwo dateByAddingTimeInterval:timeoutTwo];
    
    
    UILocalNotification* noti21 =
    
    
    [UILocalNotification pacoNotificationWithExperimentId:testIdTwo
                                          experimentTitle:testTitleTwo
                                                 fireDate:dateTwo
                                              timeOutDate:timeOutDateTwo
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    
    
    [notifications addObject:noti21];
    
    
    
    for (UILocalNotification *noti in notifications) {
        [[UIApplication sharedApplication] scheduleLocalNotification:noti];
    }
    
    
    
    NSArray* scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
    
     NSArray *expect = @[noti11, noti12, noti21];
    XCTAssertTrue([scheduled count] == [expect count] );
    
   
 
    
    
    UILocalNotification *new1 = [noti11 copy];
    UILocalNotification *new2 = [noti21 copy];
    
    NSString *newId = @"891011";
    NSString *anewTitle = @"Experiment New";
    NSDate *newFireDate = [NSDate dateWithTimeIntervalSinceNow:thirtyMinutes];
    NSDate *newTimeOutDate = [newFireDate dateByAddingTimeInterval:timeoutTwo];
    
    
    
    UILocalNotification *new3 =
         [UILocalNotification pacoNotificationWithExperimentId:newId
                                          experimentTitle:anewTitle
                                                 fireDate:newFireDate
                                              timeOutDate:newTimeOutDate
                                                  groupId:self.groupId
                                                groupName:self.groupName
                                                triggerId:self.actionTriggerId
                                     notificationActionId:self.notificationActionId
                                      actionTriggerSpecId:@"23456"];
    
    
    
 
    
    NSArray* newNotifications = @[new1, new2, new3];
    
    NSMutableArray *notificationsToCancel = [NSMutableArray array];
    NSMutableArray *notificationsToSchedule = [NSMutableArray array];
    for (UILocalNotification *newNoti in newNotifications) {
        UILocalNotification *notiScheduled = nil;
        for (UILocalNotification *oldNoti in notifications) {
            if ([newNoti isEqual:oldNoti]) {
                notiScheduled = oldNoti;
                break;
            }
        }
        if (!notiScheduled) {
            [notificationsToSchedule addObject:newNoti];
        }
    }
    for (UILocalNotification *oldNoti in notifications) {
        UILocalNotification *notiToSchedule = nil;
        for (UILocalNotification *newNoti in newNotifications) {
            if ([newNoti isEqual:oldNoti]) {
                notiToSchedule = newNoti;
                break;
            }
        }
        if (!notiToSchedule) {
            [notificationsToCancel addObject:oldNoti];
        }
    }
    
    NSArray *expectToCancel = @[noti12];
    NSArray *expectToSchedule = @[new3];
    XCTAssertEqualObjects(notificationsToCancel, expectToCancel, @"should only cancel noti12");
    XCTAssertEqualObjects(notificationsToSchedule, expectToSchedule,
                          @"should have one new notification to schedule");
    
    for (UILocalNotification *noti in notificationsToCancel) {
        [[UIApplication sharedApplication] cancelLocalNotification:noti];
    }
    scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
    XCTAssertEqual((int)[scheduled count], 2, @"there should be 2 notification scheduled!");
    for (UILocalNotification *noti in notificationsToSchedule) {
        [[UIApplication sharedApplication] scheduleLocalNotification:noti];
    }
    

    scheduled = [[UIApplication sharedApplication] scheduledLocalNotifications];
    expect = @[noti11, noti21, new3];
    
    
    for(int i =0; i < [expect count]; i++)
    {
        
        bool b =   [expect[i] isEqual:scheduled[i] ];
        XCTAssertTrue(b);
    }

}





#pragma mark - helper methods



-(PAExperimentDAO*) experimentDAO:(int) index
{
    
    NSData* data =nil;
 
        
        data=  [def3 dataUsingEncoding:NSUTF8StringEncoding];
        
   
    
    PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayOfClasses:nil
                          withNameOfClassAttribute:@"nameOfClass"];
    
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
    IOSObjectArray * iosArray = [resultArray toArray];
    
    PAExperimentDAO * dao =  [iosArray objectAtIndex:0];
    return dao;
    
}





/*
 
 return a unique id for an object
 */
-(NSValue*) uniqueId:(NSObject*) actionSpecification
{
    return [NSValue valueWithPointer:(__bridge const void *)(actionSpecification)];
}



/*
 
 Fetch action specification from definition using the j2boc scheduler. Resutls are sored in 'results' dictionary.
 
 */
-(void ) getFireTimes:(PAExperimentDAO*)  definition  results:(NSMutableDictionary*) results  SignalStore:( id<PAEsmSignalStore>)signalStore EventStore:( id<PAEventStore>)eventStore
{
    
    OrgJodaTimeDateTime *  nextTime =  [OrgJodaTimeDateTime  now];
    PAActionSpecification *actionSpecification ;
    int count  =0;
    do {
        
        PAActionScheduleGenerator *actionScheduleGenerator = [[PAActionScheduleGenerator alloc] initWithPAExperimentDAO:definition];
        PAActionSpecification*   actionSpecification   = [actionScheduleGenerator getNextTimeFromNowWithOrgJodaTimeDateTime:nextTime withPAEsmSignalStore:signalStore withPAEventStore:eventStore];
        
        if( actionSpecification )
        {
            nextTime = [actionSpecification->time_ plusMinutesWithInt:1];
            NSMutableArray* mArray =[results objectForKey:[self uniqueId:definition]];
            [mArray  addObject:actionSpecification];
            NSLog(@" added  %@", nextTime);
        }
        
    } while (actionSpecification !=nil &&  count++ <= 60 );
    
    
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    
    
    PacoSignalStore * signalStore          =  [[PacoSignalStore alloc] init];
    PacoEventStore  * eventStore           =  [[PacoEventStore  alloc] init];
    NSMutableDictionary * results  = [[NSMutableDictionary alloc] init];
    [results setObject:[NSMutableArray new] forKey:[self uniqueId:_dao]];
    
    [self measureBlock:^{
        for(int i =0; i < 1000; i++)
        {
           [self getFireTimes:_dao  results:results SignalStore:signalStore EventStore:eventStore];
        }
    }];
}

@end
