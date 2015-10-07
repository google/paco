//
//  PacoScheduleGeneratorj2ObjC.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/19/15.
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


@interface PacoScheduleGeneratorj2ObjC : XCTestCase
@property(strong, nonatomic) NSArray* classes;
@end

@implementation PacoScheduleGeneratorj2ObjC


static NSString *def1 =
@"{\r\n  \"title\": \"Drink Coke\",\r\n  \"description\": \"tim drinking value\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"Purdue\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5755617021001729,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/08\/22\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 10,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/8\/28\",\r\n      \"endDate\": \"2015\/9\/15\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1440120356411,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1440120356411,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 32400000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Nine AM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 54000000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Three PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 57600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"4 PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1440120356424,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": false,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";


static NSString *def0 =
@"{\r\n  \"title\": \"Drink Water\",\r\n  \"description\": \"tim obrien\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"Self\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5755617021001728,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/08\/22\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 10,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/8\/23\",\r\n      \"endDate\": \"2015\/8\/28\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1440120356423,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1440120356422,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 32400000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Nine AM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 54000000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Three PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 57600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"4 PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1440120356424,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": false,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";

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

-(PAExperimentDAO*) experimentDAO2
{
    
    NSData* data = [def1 dataUsingEncoding:NSUTF8StringEncoding];
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
    
       // OrgJodaTimeDateTime  * time = [OrgJodaTimeDateTime now];
      //  PacoSignalStore * signalStore = [[PacoSignalStore alloc] init];
        PacoEventStore * eventStore =  [[PacoEventStore alloc] init];
    
     PASchedule * firstSchedules= [dao valueForKeyPathEx:@"groups[0].actionTriggers[0].schedules[0]"];
     NSNumber* actionTriggerId = [dao valueForKeyPathEx:@"groups[0].actionTriggers[0].id"];
     NSString* groupName        =  [dao valueForKeyPathEx:@"groups[0].name"];
     long long triggerId = [actionTriggerId longLongValue];
     long long experimentId = [[dao valueForKeyEx:@"id"] longLongValue];
    
    OrgJodaTimeDateTime* time = [[OrgJodaTimeDateTime alloc] initWithInt:2015 withInt:8 withInt:24 withInt:13 withInt:0];
     OrgJodaTimeDateTime * ogTime =  [scheduleGenerator getNextAlarmTimeWithOrgJodaTimeDateTime:time  withJavaLangLong:[JavaLangLong valueOfWithLong:experimentId] withPASchedule:firstSchedules  withPAEventStore:eventStore withNSString:groupName  withJavaLangLong: [JavaLangLong valueOfWithLong:triggerId]];
    
    for(int i =0; i < 24; i++)
    {
      OrgJodaTimeDateTime* newTime= [[OrgJodaTimeDateTime alloc] initWithInt:2015 withInt:8 withInt:24 withInt:i withInt:0];
       OrgJodaTimeDateTime * ogTimeII =  [scheduleGenerator getNextAlarmTimeWithOrgJodaTimeDateTime:newTime   withJavaLangLong:[JavaLangLong valueOfWithLong:experimentId] withPASchedule:firstSchedules  withPAEventStore:eventStore withNSString:groupName  withJavaLangLong: [JavaLangLong valueOfWithLong:triggerId]];
    
         NSLog(@"check ogTime %@  --- for hour %@",   ogTimeII, newTime);
    }
    
    NSLog(@"Done" );
    
    
    
}


-(void) testGetNextFromNow
{
    PAExperimentDAO      * dao  = [self experimentDAO];
    PAActionScheduleGenerator* scheduleGenerator = [[PAActionScheduleGenerator alloc] initWithPAExperimentDAO:dao];
    PacoEventStore * eventStore =  [[PacoEventStore alloc] init];
    
    PASchedule * firstSchedules= [dao valueForKeyPathEx:@"groups[0].actionTriggers[0].schedules[0]"];
    NSNumber* actionTriggerId = [dao valueForKeyPathEx:@"groups[0].actionTriggers[0].id"];
    NSString* groupName        =  [dao valueForKeyPathEx:@"groups[0].name"];
    NSNumber* numberOfGroups        =  [dao valueForKeyPathEx:@"groups#"];
    long long triggerId = [actionTriggerId longLongValue];
    long long experimentId = [[dao valueForKeyEx:@"id"] longLongValue];
    
    OrgJodaTimeDateTime* time =  [OrgJodaTimeDateTime  now];
    OrgJodaTimeDateTime * ogTime =  [scheduleGenerator getNextAlarmTimeWithOrgJodaTimeDateTime:time  withJavaLangLong:[JavaLangLong valueOfWithLong:experimentId] withPASchedule:firstSchedules  withPAEventStore:eventStore withNSString:groupName  withJavaLangLong: [JavaLangLong valueOfWithLong:triggerId]];
    
  
     XCTAssertTrue(groupName !=nil);
     XCTAssertTrue(actionTriggerId !=nil);
     XCTAssertTrue(numberOfGroups !=nil);
     XCTAssertTrue(firstSchedules !=nil);
     XCTAssertTrue(firstSchedules !=nil);
     XCTAssertTrue(ogTime !=nil);
     XCTAssertTrue(time !=nil);
    
}


-(void) testOne
{
    PAExperimentDAO      * dao  = [self experimentDAO];
    PAExperimentGroup* group  = [dao valueForKeyPathEx:@"groups[0]"];
    
    
    BOOL b = [PAActionScheduleGenerator  areAllGroupsFixedDurationWithPAExperimentDAO:dao];
    XCTAssert(b, @"Pass");
    
}

-(void) testIsOVer
{
   // OrgJodaTimeDateTime  * time = [OrgJodaTimeDateTime now];
    PAExperimentDAO      * dao  = [self experimentDAO];
    
     OrgJodaTimeDateTime      * time = [[OrgJodaTimeDateTime alloc] initWithInt:2015 withInt:8 withInt:23 withInt:18 withInt:30];
    
    BOOL b = [PAActionScheduleGenerator isOverWithOrgJodaTimeDateTime:time withPAExperimentDAO:dao];
    XCTAssert(!b, @"Pass");
    
}


-(void) testTwo
{
    PAExperimentDAO      * dao          = [self experimentDAO];
    PAExperimentDAO      * dao2         = [self experimentDAO2];
    JavaUtilArrayList* list             = [[JavaUtilArrayList  alloc]    init];
    PacoSignalStore * signalStore       = [[PacoSignalStore alloc] init];
    PacoEventStore * eventStore        = [[PacoEventStore  alloc] init];
    
    
    [list addWithId:dao];
    [list addWithId:dao2];
    
    
      OrgJodaTimeDateTime      * time = [[OrgJodaTimeDateTime alloc] initWithInt:2015 withInt:8 withInt:30 withInt:18 withInt:30];
    
  id<JavaUtilList>   returnList   =   [PAActionScheduleGenerator arrangeExperimentsByNextTimeFromWithJavaUtilList:list withOrgJodaTimeDateTime:time withPAEsmSignalStore:signalStore withPAEventStore:eventStore];
    
    NSLog(@"return value");
    
}




@end
