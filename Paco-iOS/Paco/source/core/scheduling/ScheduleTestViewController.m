//
//  ScheduleTestViewController.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "ScheduleTestViewController.h"
#import "PacoNotificationManager.h"
#import "UILocalNotification+Paco.h"
#import "PAActionSpecification+PacoActionSpecification.h"




//
//  PacoScheduleGeneratorj2ObjC.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.

#import <UIKit/UIKit.h>
#import "PacoSerializer.h"
#import "PacoExtendedClient.h"
#import "ActionScheduleGenerator.h"
#include "ExperimentDAO.h"
#include "ExperimentDAOCore.h"
#include "ExperimentGroup.h"
#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "ListMaker.h"
#include "Validator.h"
#include "java/lang/Boolean.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/ArrayList.h"
#include "java/util/Arrays.h"
#include "java/util/List.h"
#import "ExperimentDAO.h"
#import <objc/runtime.h>
#include "ActionScheduleGenerator.h"
#include "ActionSpecification.h"
#include "ActionTrigger.h"
#include "DateMidnight.h"
#include "DateTime.h"
#include "EsmGenerator2.h"
#include "EsmSignalStore.h"
#include "EventStore.h"
#include "ExperimentDAO.h"
#include "ExperimentGroup.h"
#include "Interval.h"
#include "J2ObjC_source.h"
#include "NonESMSignalGenerator.h"
#include "PacoAction.h"
#include "PacoNotificationAction.h"
#include "Schedule.h"
#include "ScheduleTrigger.h"
#include "SignalTime.h"
#include "TimeUtil.h"
#include "java/lang/Boolean.h"
#include "java/lang/IllegalStateException.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/Collections.h"
#include "java/util/List.h"
#include "org/joda/time/Hours.h"
#include "org/joda/time/Duration.h"
#include "EsmGenerator2.h"
#include "PacoSerializeUtil.h"
#import  "PacoSignalStore.h"
#import   "PacoEventStore.h"
#import   "DateTime.h"
#import  "NSObject+J2objcKVO.h"
#import  "OrgJodaTimeDateTime+PacoDateHelper.h"
#import  "PacoScheduler.h" 
#import "PacoExtendedClient.h"
#import "UILocalNotification+Paco.h"
#import "PacoModelExtended.h" 
#import  "PacoSchedulingUtil.h"
#import "PacoMediator.h"

  


@interface ScheduleTestViewController ()

@property (nonatomic,strong)   NSMutableDictionary* processing;
@property (nonatomic,strong)   PacoModelExtended * model;


@end

@implementation ScheduleTestViewController
{
    PacoNotificationManager*  notificationManager;
    
}

static NSString *def2 =
@" {\r\n  \"title\": \"How Many Conversations\",\r\n  \"description\": \"How many conversations are going on around you\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"Google\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5717865130885120,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/09\/02\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 10,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/9\/1\",\r\n      \"endDate\": \"2015\/9\/10\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1441060623472,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1441060623471,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 4,\r\n              \"esmFrequency\": 15,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 60,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1441060623473,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";


static NSString *def1 =
@"{\r\n  \"title\": \"Drink Water\",\r\n  \"description\": \"tim obrien\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"Self\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5755617021001728,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/09\/03\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 28,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"startDate\": \"2015\/8\/29\",\r\n      \"endDate\": \"2015\/9\/10\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1440120356423,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1440120356422,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 32400000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Nine AM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 36000000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Three PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 57600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"4 PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1440120356424,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": false,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";


static NSString *def0 =
@"{\r\n  \"title\": \"Is Odds Changed\",\r\n  \"description\": \"Find check if odds have been recalculated\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"Pennies and Dimes\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5739463179239424,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/09\/02\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 15,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/8\/31\",\r\n      \"endDate\": \"2015\/9\/10\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1441060481422,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1441060481421,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 28800000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Eight AM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 39600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Eleven AM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 56700000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Three Forty Five PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 75600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Nine PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 82800000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"eleven pm\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1441060481423,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";




- (void)viewDidLoad {
    [super viewDidLoad];
    
      _processing  = [[NSMutableDictionary  alloc] init];
      _schedulerDelegate = [[PacoSchedulingUtil alloc] init];
      self.scheduler = [PacoScheduler schedulerWithDelegate:_schedulerDelegate  firstLaunchFlag:YES];
      self.client   = [[PacoExtendedClient alloc] init];
      self.model = [[PacoModelExtended alloc] init];
}




- (IBAction)firePointFive:(id)sender
{
   
    notificationManager =[PacoNotificationManager managerWithDelegate:self firstLaunchFlag:NO];
    [_firstTime.text intValue];
    NSDate* firstFireDate = [NSDate dateWithTimeIntervalSinceNow:[_firstTime.text intValue]]; //active
    
    /* schedule nofitification */
    NSTimeInterval timeoutInterval = 479*60;
    NSDate* secondFireDate = [NSDate dateWithTimeIntervalSinceNow:[_secondTime.text intValue]]; //active
    NSDate* secondTimeout = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:firstFireDate];
    NSString* experimentId3 = @"3";
    NSString* title3 = @"title3";
    UILocalNotification* secondNoti = [UILocalNotification pacoNotificationWithExperimentId:experimentId3
                                                                            experimentTitle:title3
                                                                                   fireDate:secondFireDate
                                                                                timeOutDate:secondTimeout];

    /* end this */
    
    
    [notificationManager scheduleNotifications:@[secondNoti]];
    
    
    
    
    /*
    UILocalNotification *localNotif = [[UILocalNotification alloc] init];
    if (localNotif == nil) return;
    NSDate *fireTime = [[NSDate date] addTimeInterval:.5]; // adds 10 secs
    localNotif.fireDate = fireTime;
    localNotif.alertBody = @"Alert!";
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotif];
     */
    
}




- (IBAction)readAndWrite:(id)sender
{
    
    /* initialize sample experiments */
    PAExperimentDAO      * dao            =  [self experimentDAO:0];
    PAExperimentDAO      * dao1           =  [self experimentDAO:1];
    PAExperimentDAO      * dao2           =  [self experimentDAO:2];
    
    [self.model fullyUpdateDefinitionList:@[dao,dao1,dao2]];
    [self.model loadExperimentDefinitionsFromFile];
    PacoMediator * data = [PacoMediator sharedInstance];
    
       [self.model experimentForId:dao1->id__];
    
    NSLog(@"done");
    
}




- (IBAction)loadDefinitions:(id)sender
{
    
    /* initialize sample experiments */
    PAExperimentDAO      * dao            =  [self experimentDAO:0];
    PAExperimentDAO      * dao1           =  [self experimentDAO:1];
    PAExperimentDAO      * dao2           =  [self experimentDAO:2];
    [[PacoMediator sharedInstance] addExperimentToAvailableStore:dao];
    [[PacoMediator sharedInstance] addExperimentToAvailableStore:dao1];
    [[PacoMediator sharedInstance] addExperimentToAvailableStore:dao2];
    
}


/* we will use the first experiment in the all experiments array  just for this test */




- (void)executeMajorTask:(BOOL)experimentModelChanged {
    @synchronized(self) {
        if (![self.schedulerDelegate isDoneInitializationForMajorTask]) {
           
            return;
        }
        
      // DDLogInfo(@"Executing Major Task...");
        
        
        BOOL needToScheduleNewNotifications = YES;
        NSArray* notificationsToSchedule = nil;
        
        
        
//        if (!experimentModelChanged && [self.notificationManager hasMaximumScheduledNotifications]) {
//            needToScheduleNewNotifications = NO;
//           // DDLogInfo(@"No need to schedule new notifications, there are 60 notifications already.");
//        }
       // if (needToScheduleNewNotifications) {
        
       // NSArray * runningExperiments =  [[PacoMediator sharedInstance] runningExperiments];
       // notificationsToSchedule  = [PacoSchedulingUtil buildActionSpecifications:runningExperiments  IsDryRun:NO];
      
             
        //}
//        if (!experimentModelChanged &&
//            needToScheduleNewNotifications &&
//            [self.scheduler.notificationManager numOfScheduledNotifications] == [notificationsToSchedule count]) {
//           // DDLogInfo(@"There are already %lu notifications scheduled, skip scheduling new notifications.", (unsigned long)[notificationsToSchedule count]);
//            needToScheduleNewNotifications = NO;
//        }
      //  if (needToScheduleNewNotifications) {
            
           // DDLogInfo(@"Schedule %lu new notifications ...",(unsigned long)[notificationsToSchedule count]);
           // [self.scheduler.notificationManager scheduleNotifications:notificationsToSchedule];
       // } else {
            
           // [self.scheduler.notificationManager cleanExpiredNotifications];
       // }
      //  [self.delegate updateNotificationSystem];
       // DDLogInfo(@"Finished major task.");
    }
    
}


/*
    main test methods.
 
 */

- (IBAction)Test:(id)sender
{
    
    /* initialize sample experiments */
    PAExperimentDAO      * dao            =  [self experimentDAO:0];
    PAExperimentDAO      * dao1           =  [self experimentDAO:1];
    PAExperimentDAO      * dao2           =  [self experimentDAO:2];
    
    
    [self.model fullyUpdateDefinitionList:@[dao,dao1,dao2]];
    
    
    
    
    JavaUtilArrayList* list               =  [[JavaUtilArrayList  alloc]    init];
    PacoSignalStore * signalStore         =  [[PacoSignalStore alloc] init];
    PacoEventStore  * eventStore           =  [[PacoEventStore  alloc] init];
    
    
    [list addWithId:dao];
    [list addWithId:dao1];
    [list addWithId:dao2];
    
 
    
     NSMutableDictionary * results = [[NSMutableDictionary alloc] init];
     [results setObject:[NSMutableArray new] forKey:[self uniqueId:dao]];
     [results setObject:[NSMutableArray new] forKey:[self uniqueId:dao1]];
     [results setObject:[NSMutableArray new] forKey:[self uniqueId:dao2]];
 

    
    [self getFireTimes:dao results:results SignalStore:signalStore EventStore: eventStore];
    [self getFireTimes:dao1 results:results SignalStore:signalStore EventStore: eventStore];
    [self getFireTimes:dao2 results:results SignalStore:signalStore EventStore: eventStore];
    
    
 /*
    NSArray* processedTimes = [self sortAlarmTimes:results];
    NSArray* alarms = [PacoSchedulingUtil makeAlarms:processedTimes];
    
    for (UILocalNotification *noti in alarms) {
        [[UIApplication sharedApplication] scheduleLocalNotification:noti];
    }
 

  
      NSLog(@" processed results %@",
                      [processedTimes subarrayWithRange:NSMakeRange(0, MIN(60, processedTimes.count))]) ;
 */ 
}

 
 




/*
 
    merge action specification for each active experiment. take top 60. 
   Note that the actions specification for each active experiment must be sorted
   before sending a message to this method.
 
 */
-(NSArray*) sortAlarmTimes:(NSDictionary*) fireTimes
{
 
    NSMutableArray * unionOfAllTimes = [[NSMutableArray alloc] init];
    NSArray* allValues = [fireTimes allValues];
 
    for(NSMutableArray * definitions in allValues)
    {
        [unionOfAllTimes  addObjectsFromArray:definitions];
    }
    
    
    NSArray *sortedArray;
    sortedArray = [unionOfAllTimes sortedArrayUsingComparator:^NSComparisonResult(id a, id b) {
    
          PAActionSpecification *actionDefinitionA =(PAActionSpecification*) a;
          PAActionSpecification *actionDefinitionB =(PAActionSpecification*) b;
           if( [actionDefinitionA->time_ isGreaterThan:actionDefinitionB->time_] )
           {
               return  NSOrderedDescending;
           }
        else
        {
            return  NSOrderedAscending;
        }
    }];
    
    return sortedArray;
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
             
             
          actionSpecification   = [actionScheduleGenerator getNextTimeFromNowWithOrgJodaTimeDateTime:nextTime withPAEsmSignalStore:signalStore withPAEventStore:eventStore];
             
            if( actionSpecification )
            {
                
                nextTime = [actionSpecification->time_ plusMinutesWithInt:1];
                NSMutableArray* mArray =[results objectForKey:[self uniqueId:definition]];
                [mArray  addObject:actionSpecification];
                NSLog(@" added  %@", nextTime);
            }
          
            
        } while (actionSpecification !=nil &&  count++ <= 60 );
  
    
}



/*
 
  spare method. 
 
 */
-(OrgJodaTimeDateTime*) getNextTime:(id<JavaUtilList>)  list
{
     PAActionSpecification* specification;
     OrgJodaTimeDateTime* iterTimeTime;
     OrgJodaTimeDateTime* previousEarliestTime;
    
    NSLog(@" next times %@ \n\n", list);
    
    for(specification in list)
    {
        if(  previousEarliestTime ==nil ||  ([previousEarliestTime  isGreaterThan:specification->time_] ))
        {
            previousEarliestTime = specification->time_;
        }
 
    }
    
    NSLog(@" next time %@", previousEarliestTime);
    return [previousEarliestTime plusMinutesWithInt:1];
    
}



-(void) handleOngoing :(NSDictionary*) actionSpecifications Definitions:(id<JavaUtilList>) definitions
{
    
    NSArray * keys = [actionSpecifications allKeys];
    NSValue* value =nil;
    
    for(value in keys)
    {
        PAExperimentDAO * dao = (PAExperimentDAO*)   [value pointerValue];
        JavaUtilArrayList* groups =  [dao valueForKeyEx:@"groups"];
        BOOL isOngoing = NO;
        
        for(int i=0; i < [groups size]; i++)
        {
            PAExperimentGroup * group = [groups getWithInt:i];
            bool b  = [group getFixedDuration].booleanValue;
            isOngoing = b;
        }
        
        if(/*isOngoing &&*/ [[actionSpecifications objectForKey:value] count] >= 60)
        {
            [definitions removeWithId:dao];
        }
    }
}





#pragma mark - utility methods

/* 
 
 return a unique id for an object
 */
-(NSValue*) uniqueId:(NSObject*) actionSpecification
{
    return [NSValue valueWithPointer:(__bridge const void *)(actionSpecification)];
}

/*
 
  fetch a test experiment definition. used for testing
 
 */
-(PAExperimentDAO*) experimentDAO:(int) index
{
    
    NSData* data =nil;
    if(index ==0 )
    {
        data =  [def0 dataUsingEncoding:NSUTF8StringEncoding];
    }
    else if (index ==1)
    {
        data=  [def1 dataUsingEncoding:NSUTF8StringEncoding];
    }
    else if (index ==2)
    {
        data=  [def2 dataUsingEncoding:NSUTF8StringEncoding];
    }
    
    
    PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayOfClasses:nil
                          withNameOfClassAttribute:@"nameOfClass"];
    
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
    IOSObjectArray * iosArray = [resultArray toArray];
    
    PAExperimentDAO * dao =  [iosArray objectAtIndex:0];
    return dao;
    
}




- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
