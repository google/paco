//
//  PacoNotificationCategoryExtendedTests.m
//  Paco
//
//  Created by northropo on 9/15/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//


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





@interface PacoNotificationCategoryExtendedTests : XCTestCase

@property(nonatomic, strong) NSString* testID;
@property(nonatomic, strong) NSString* testTitle;
@property(nonatomic, strong) NSDate* testFireDate;
@property(nonatomic, strong) NSDate* testTimeoutDate;
@property(nonatomic, strong) PAExperimentDAO* experiment;
@property(nonatomic, strong) NSArray* testDatesToSchedule;
@property(nonatomic, strong) NSArray* fireTimes;


@end

static NSString *def2 =
@" {\r\n  \"title\": \"How Many Conversations\",\r\n  \"description\": \"How many conversations are going on around you\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"Google\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5717865130885120,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/09\/02\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 10,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/9\/1\",\r\n      \"endDate\": \"2015\/9\/10\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1441060623472,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1441060623471,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 4,\r\n              \"esmFrequency\": 15,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 60,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1441060623473,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";


static NSString *def1 =
@"{\r\n  \"title\": \"Drink Water\",\r\n  \"description\": \"tim obrien\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"Self\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5755617021001728,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/09\/03\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 28,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"startDate\": \"2015\/8\/29\",\r\n      \"endDate\": \"2015\/9\/20\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1440120356423,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1440120356422,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 32400000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Nine AM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 36000000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Three PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 57600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"4 PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1440120356424,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": false,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";


static NSString *def0 =
@"{\r\n  \"title\": \"Is Odds Changed\",\r\n  \"description\": \"Find check if odds have been recalculated\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"Pennies and Dimes\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5739463179239424,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/09\/02\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 15,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/8\/31\",\r\n      \"endDate\": \"2015\/9\/10\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1441060481422,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1441060481421,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 28800000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Eight AM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 39600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Eleven AM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 56700000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Three Forty Five PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 75600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Nine PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 82800000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"eleven pm\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1441060481423,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";




@implementation PacoNotificationCategoryExtendedTests

- (void)setUp {
    [super setUp];
    
    
    PacoSignalStore * signalStore          =  [[PacoSignalStore alloc] init];
    PacoEventStore  * eventStore           =  [[PacoEventStore  alloc] init];
    PAExperimentDAO*  experiment  = [self experimentDAO:1];
    
    
    
     NSMutableDictionary * results  = [[NSMutableDictionary alloc] init];
    [results setObject:[NSMutableArray new] forKey:[self uniqueId:experiment]];
    [self getFireTimes:experiment  results:results SignalStore:signalStore EventStore:eventStore];
    NSArray* definitions  = [results objectForKey:[self uniqueId:experiment]];
    
    

    self.testID = @"12345";
    self.testTitle = @"Paco Notification One";
    self.testFireDate = [NSDate date];
    self.testTimeoutDate = [NSDate dateWithTimeInterval:10 sinceDate:self.testFireDate];
    
    
   
}



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


/*
 
 NSString* const kUserInfoKeyNotificationTimeoutDate = @"timeoutDate";
 NSString* const kNotificationGroupId                 = @"groupId";
 NSString* const kNotificationGroupName               =@"groupName";
 NSString* const kUserInfoKeyActionTriggerId          = @"actionTriggerId";
 NSString* const kUserInfoKeyNotificationActionId     = @"notificationActionId";
 NSString* const kUserInfoKeyActionTriggerSpecId      =@"actionTriggerSpecId";
 

 
 
*/

- (void)testCreatePacoInfo {
    
    
   /*
    PAExperimentDAO * dao = [self experimentDAO:0];
    
    NSDictionary* dict = @{kUserInfoKeyExperimentId:self.testID,
                           kUserInfoKeyExperimentTitle:self.testTitle,
                           kUserInfoKeyNotificationFireDate:self.testFireDate,
                           kUserInfoKeyNotificationTimeoutDate:self.testTimeoutDate,
                           kNotificationGroupId:
                           kNotificationGroupName:
                           kUserInfoKeyNotificationActionId
                           };
    
    PacoNotificationInfo* info = [PacoNotificationInfo pacoInfoWithDictionary:dict];
    
    XCTAssertTrue(info != nil, @"should have a valid info");
    XCTAssertEqualObjects(info.experimentId, @"12345", @"should have a valid experiment id");
    XCTAssertEqualObjects(info.fireDate, self.testFireDate, @"should have a valid fire date");
    XCTAssertEqualObjects(info.timeOutDate, self.testTimeoutDate, @"should have a valid timeout date");
    */
   
    
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testExample {
    // This is an example of a functional test case.
    XCTAssert(YES, @"Pass");
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
