//
//  PacoScheduleGeneratorj2ObjC.m
//  Paco
//
//  Created by northropo on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.



#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "PacoSerializer.h"
#import "PacoExtendedClient.h"
#import "ActionScheduleGenerator.h"
#import "NSObject+J2objcKVO.h"


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


@interface PacoScheduleGeneratorj2ObjC : XCTestCase
@property(strong, nonatomic) NSArray* classes;
@end

@implementation PacoScheduleGeneratorj2ObjC


static NSString *def1 =
@"{\r\n  \"title\": \"Have You Filled out All Paperwork\",\r\n  \"description\": \"Check if I am up to paperwork no matter what the kind, personal, work, bills, timecards.....\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"My World\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5739004087500800,\r\n  \"recordPhoneDetails\": true,\r\n  \"extraDataCollectionDeclarations\": [\r\n    1,\r\n    2,\r\n    3\r\n  ],\r\n  \"deleted\": false,\r\n  \"published\": true,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 6,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/8\/7\",\r\n      \"endDate\": \"2014\/8\/14\",\r\n      \"logActions\": true,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n    xx      \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1438996742500,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1438996742499,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 4,\r\n              \"esmFrequency\": 11,\r\n              \"esmPeriodInDays\": 1,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": true,\r\n              \"minimumBuffer\": 77,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1438996742501,\r\n              \"onlyEditableOnJoin\": true,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            },\r\n            {\r\n              \"scheduleType\": 4,\r\n              \"esmFrequency\": 8,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": true,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1439490359621,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [\r\n        {\r\n          \"name\": \"myVariable\",\r\n          \"required\": true,\r\n          \"conditional\": true,\r\n          \"conditionExpression\": \"are you finisidhed\",\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"How are we doing?\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        }\r\n      ],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";


static NSString *def0 =
@"{\r\n  \"title\": \"Drink Water\",\r\n  \"description\": \"tim obrien\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"Self\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5755617021001728,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/08\/21\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 8,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/8\/19\",\r\n      \"endDate\": \"2015\/8\/28\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1440120356423,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1440120356422,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 32400000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 54000000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 57600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1440120356424,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            },\r\n            {\r\n              \"scheduleType\": 4,\r\n              \"esmFrequency\": 8,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": true,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1440120356425,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";

- (void)setUp {
    [super setUp];
    
    
     _classes = [self getClassNames];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}


- (NSArray*)getClassNames {
    NSMutableArray* mutableArray = [NSMutableArray new];
    NSString* path = @"/Users/northropo/Project/paco/Paco-iOS/DerivedData/Paco/"
    @"Build/Intermediates/Paco.build/Debug-iphonesimulator/"
    @"Paco.build/DerivedSources";
    NSArray* dirs =
    [[NSFileManager defaultManager] contentsOfDirectoryAtPath:path error:Nil];
    NSArray* headers =
    [dirs filteredArrayUsingPredicate:
     [NSPredicate predicateWithFormat:@"self ENDSWITH '.h'"]];
    for (NSString* fileName in headers) {
        NSString* trimmedString = [fileName substringToIndex:[fileName length] - 2];
        [mutableArray addObject:trimmedString];
    }
    return mutableArray;
}

- (void)testAreAllGroupsFixedDurationWithPAExperimentDAO {
    
    
    
    NSData* data = [def0 dataUsingEncoding:NSUTF8StringEncoding];
    PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayOfClasses:_classes
                          withNameOfClassAttribute:@"nameOfClass"];
    
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
    IOSObjectArray * iosArray = [resultArray toArray];
    
    PAExperimentDAO * dao =  [iosArray objectAtIndex:0];
    
    
    PacoSignalStore * signalStore = [[PacoSignalStore alloc] init];
    
    [signalStore storeSignalWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    [signalStore storeSignalWithJavaLangLong:[JavaLangLong valueOfWithLong:1234567]  withJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    JavaUtilArrayList * list =  (JavaUtilArrayList*)  [signalStore getSignalsWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    
    
    
    PAActionScheduleGenerator * scheduleGenerator = [[PAActionScheduleGenerator alloc] initWithPAExperimentDAO:dao];
    
   
 
    BOOL areAllGroupsFixedDuration = [PAActionScheduleGenerator  areAllGroupsFixedDurationWithPAExperimentDAO:dao];
    
    
    XCTAssert(!areAllGroupsFixedDuration, @"Pass");
}


-(void) testGetEarlienstStartDatey
{
      PAExperimentDAO * dao  = [self experimentDAO];
    
     [PAActionScheduleGenerator     getEarliestStartDateWithPAExperimentDAO:dao];
}


-(void) testGetEndDateTime
{
    PAExperimentDAO * dao  = [self experimentDAO];
   OrgJodaTimeDateTime * joda =  [PAActionScheduleGenerator     getEndDateTimeWithPAExperimentDAO:dao];
    
    NSLog(@" description %@", joda.description);
}




-(PAExperimentDAO*) experimentDAO
{
    
    NSData* data = [def0 dataUsingEncoding:NSUTF8StringEncoding];
    PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayOfClasses:_classes
                          withNameOfClassAttribute:@"nameOfClass"];
    
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
    IOSObjectArray * iosArray = [resultArray toArray];
    
    PAExperimentDAO * dao =  [iosArray objectAtIndex:0];
    return dao;
    
}

/*
 
 - (OrgJodaTimeDateTime *)getNextAlarmTimeWithOrgJodaTimeDateTime:(OrgJodaTimeDateTime *)dateTime
 withJavaLangLong:(JavaLangLong *)experimentServerId
 withPASchedule:(PASchedule *)schedule
 withPAEventStore:(id<PAEventStore>)eventStore
 withNSString:(NSString *)groupName
 withJavaLangLong:(JavaLangLong *)actionTriggerId;
 
 
 */
-(void) testGetNextTime
{
        PAExperimentDAO      * dao  = [self experimentDAO];
        PAActionScheduleGenerator* scheduleGenerator = [[PAActionScheduleGenerator alloc] initWithPAExperimentDAO:dao];
    
        OrgJodaTimeDateTime  * time = [OrgJodaTimeDateTime now];
      //  PacoSignalStore * signalStore = [[PacoSignalStore alloc] init];
        PacoEventStore * eventStore =  [[PacoEventStore alloc] init];
    
     PASchedule * firstSchedules= [dao valueForKeyPathEx:@"groups[0].actionTriggers[0].schedules[0]"];
     NSNumber* actionTriggerId = [dao valueForKeyPathEx:@"groups[0].actionTriggers[0].id"];
     NSString* groupName        =  [dao valueForKeyPathEx:@"groups[0].name"];
     long long triggerId = [actionTriggerId longLongValue];
     long long experimentId = [[dao valueForKeyEx:@"id"] longLongValue];
    
    
   OrgJodaTimeDateTime * ogTime =  [scheduleGenerator getNextAlarmTimeWithOrgJodaTimeDateTime:time  withJavaLangLong:[JavaLangLong valueOfWithLong:experimentId] withPASchedule:firstSchedules  withPAEventStore:eventStore withNSString:groupName  withJavaLangLong: [JavaLangLong valueOfWithLong:triggerId]];
    
    for(int i =0; i < 24; i++)
    {
      OrgJodaTimeDateTime*   newTime  = [[OrgJodaTimeDateTime alloc] initWithInt:2015 withInt:8 withInt:22 withInt:i withInt:0];
       OrgJodaTimeDateTime * ogTimeII =  [scheduleGenerator getNextAlarmTimeWithOrgJodaTimeDateTime:newTime   withJavaLangLong:[JavaLangLong valueOfWithLong:experimentId] withPASchedule:firstSchedules  withPAEventStore:eventStore withNSString:groupName  withJavaLangLong: [JavaLangLong valueOfWithLong:triggerId]];
    
         NSLog(@"check ogTime %@  for hour %i",   ogTimeII, i);
    }
    
    NSLog(@"Done" );
    
    
    
}

-(void) testIsOVer
{
   // OrgJodaTimeDateTime  * time = [OrgJodaTimeDateTime now];
    PAExperimentDAO      * dao  = [self experimentDAO];
    
     OrgJodaTimeDateTime      * time = [[OrgJodaTimeDateTime alloc] initWithInt:2017 withInt:8 withInt:19 withInt:18 withInt:30];
    
    BOOL b = [PAActionScheduleGenerator isOverWithOrgJodaTimeDateTime:time withPAExperimentDAO:dao];
    
    XCTAssert(!b, @"Pass");
    
}
- (void)testExample {
    
    
    
    PAExperimentDAO * dao  = [self experimentDAO];
    
    PacoSignalStore * signalStore = [[PacoSignalStore alloc] init];
    
    [signalStore storeSignalWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    [signalStore storeSignalWithJavaLangLong:[JavaLangLong valueOfWithLong:1234567]  withJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    JavaUtilArrayList * list =  (JavaUtilArrayList*)  [signalStore getSignalsWithJavaLangLong:[JavaLangLong valueOfWithLong:123]  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withNSString:@"hello"  withJavaLangLong:[JavaLangLong valueOfWithLong:123] withJavaLangLong:[JavaLangLong valueOfWithLong:123]];
    
    
    PAActionScheduleGenerator * scheduleGenerator = [[PAActionScheduleGenerator alloc] initWithPAExperimentDAO:dao];
    
    /*
    + (id<JavaUtilList>)arrangeExperimentsByNextTimeWithJavaUtilList:(id<JavaUtilList>)experiments
withPAEsmSignalStore:(id<PAEsmSignalStore>)alarmStore
withPAEventStore:(id<PAEventStore>)eventStore;
     */
    
    JavaUtilArrayList * list2   =  [[JavaUtilArrayList alloc] initWithInt:1];
    [list addWithId:dao];
    /*
    - (instancetype)initWithInt:(jint)year
withInt:(jint)monthOfYear
withInt:(jint)dayOfMonth
withInt:(jint)hourOfDay
withInt:(jint)minuteOfHour;
     */
    
    
    OrgJodaTimeDateTime      * time = [[OrgJodaTimeDateTime alloc] initWithInt:2015 withInt:8 withInt:19 withInt:18 withInt:30];
    
    
   // [PAActionScheduleGenerator arrangeExperimentsByNextTimeFromWithJavaUtilList:list  withOrgJodaTimeDateTime:time withPAEsmSignalStore:signalStore withPAEventStore:(id<PAEventStore>)
     
     
   // XCTAssert(TRUE, @"Pass");
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
