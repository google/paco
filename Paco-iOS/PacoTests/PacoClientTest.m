//
//  PacoClientTest.m
//  Paco
//
//  Created by northropo on 8/12/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

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




@interface PacoClientTest : XCTestCase
@property(strong, nonatomic) NSArray* classes;
@end



@implementation PacoClientTest


static NSString *def0 =
@"{\r\n  \"title\": \"Have You Filled out All Paperwork\",\r\n  \"description\": \"Check if I am up to paperwork no matter what the kind, personal, work, bills, timecards.....\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"My World\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5739004087500800,\r\n  \"recordPhoneDetails\": true,\r\n  \"extraDataCollectionDeclarations\": [\r\n    1,\r\n    2,\r\n    3\r\n  ],\r\n  \"deleted\": false,\r\n  \"published\": true,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 6,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/8\/7\",\r\n      \"endDate\": \"2014\/8\/14\",\r\n      \"logActions\": true,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1438996742500,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1438996742499,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 4,\r\n              \"esmFrequency\": 11,\r\n              \"esmPeriodInDays\": 1,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": true,\r\n              \"minimumBuffer\": 77,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1438996742501,\r\n              \"onlyEditableOnJoin\": true,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            },\r\n            {\r\n              \"scheduleType\": 4,\r\n              \"esmFrequency\": 8,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": true,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1439490359621,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [\r\n        {\r\n          \"name\": \"myVariable\",\r\n          \"required\": true,\r\n          \"conditional\": true,\r\n          \"conditionExpression\": \"are you finisidhed\",\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"How are we doing?\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        }\r\n      ],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";




static NSString* newDefinition =
@"[{\r\n  \"title\" : \"My Title\",\r\n  \"description\" : \"this is muy "
@"description\",\r\n  \"creator\" : \"tim.n.obrien@yahoo.com\",\r\n  "
@"\"organization\" : null,\r\n  \"contactEmail\" : null,\r\n  "
@"\"contactPhone\" : null,\r\n  \"joinDate\" : \"12\/14\/2014\",\r\n  "
@"\"id\" : 12345,\r\n  \"informedConsentForm\" : \"informed "
@"consent\",\r\n  \"recordPhoneDetails\" : false,\r\n  "
@"\"extraDataCollectionDeclarations\" : [ ],\r\n  \"deleted\" : "
@"false,\r\n  \"earliestStartDate\" : null,\r\n  \"latestEndDate\" : "
@"null,\r\n  \"modifyDate\" : null,\r\n  \"published\" : true,\r\n  "
@"\"admins\" : [ \"tim\", \"jack\", \"john\", \"mike\" ],\r\n  "
@"\"publishedUsers\" : [ \"tim\", \"jack\", \"john\", \"mike\" ],\r\n  "
@"\"version\" : 1,\r\n  \"groups\" : [ {\r\n    \"name\" : \"test "
@"experiment groups\",\r\n    \"customRendering\" : false,\r\n    "
@"\"customRenderingCode\" : null,\r\n    \"fixedDuration\" : false,\r\n   "
@" \"startDate\" : null,\r\n    \"endDate\" : null,\r\n    \"logActions\" "
@": false,\r\n    \"backgroundListen\" : false,\r\n    "
@"\"backgroundListenSourceIdentifier\" : null,\r\n    \"actionTriggers\" "
@": [ {\r\n      \"type\" : \"scheduleTrigger\",\r\n      \"actions\" : [ "
@"],\r\n      \"id\" : null,\r\n      \"schedules\" : [ {\r\n        "
@"\"scheduleType\" : 0,\r\n        \"esmFrequency\" : 3,\r\n        "
@"\"esmPeriodInDays\" : 0,\r\n        \"esmStartHour\" : 32400000,\r\n    "
@"    \"esmEndHour\" : 61200000,\r\n        \"signalTimes\" : [ ],\r\n    "
@"    \"repeatRate\" : 1,\r\n        \"weekDaysScheduled\" : 0,\r\n       "
@" \"nthOfMonth\" : 1,\r\n        \"byDayOfMonth\" : true,\r\n        "
@"\"dayOfMonth\" : 1,\r\n        \"esmWeekends\" : false,\r\n        "
@"\"minimumBuffer\" : 59,\r\n        \"joinDateMillis\" : 0,\r\n        "
@"\"beginDate\" : null,\r\n        \"id\" : null,\r\n        "
@"\"onlyEditableOnJoin\" : false,\r\n        \"userEditable\" : true,\r\n "
@"\"defaultMinimumBuffer\" : "
@"59,\r\n        \"nameOfClass\" : "
@"\"com.pacoapp.paco.shared.model2.Schedule\"\r\n      }, {\r\n        "
@"\"scheduleType\" : 0,\r\n        \"esmFrequency\" : 3,\r\n        "
@"\"esmPeriodInDays\" : 0,\r\n        \"esmStartHour\" : 32400000,\r\n    "
@"    \"esmEndHour\" : 61200000,\r\n        \"signalTimes\" : [ ],\r\n    "
@"    \"repeatRate\" : 1,\r\n        \"weekDaysScheduled\" : 0,\r\n       "
@" \"nthOfMonth\" : 1,\r\n        \"byDayOfMonth\" : true,\r\n        "
@"\"dayOfMonth\" : 1,\r\n        \"esmWeekends\" : false,\r\n        "
@"\"minimumBuffer\" : 59,\r\n        \"joinDateMillis\" : 0,\r\n        "
@"\"beginDate\" : null,\r\n        \"id\" : null,\r\n        "
@"\"onlyEditableOnJoin\" : false,\r\n        \"userEditable\" : true,\r\n "
@"       \"byDayOfWeek\" : false,\r\n        \"defaultMinimumBuffer\" : "
@"59,\r\n        \"nameOfClass\" : "
@"\"com.pacoapp.paco.shared.model2.Schedule\"\r\n      }, {\r\n        "
@"\"scheduleType\" : 0,\r\n        \"esmFrequency\" : 3,\r\n        "
@"\"esmPeriodInDays\" : 0,\r\n        \"esmStartHour\" : 32400000,\r\n    "
@"    \"esmEndHour\" : 61200000,\r\n        \"signalTimes\" : [ ],\r\n    "
@"    \"repeatRate\" : 1,\r\n        \"weekDaysScheduled\" : 0,\r\n       "
@" \"nthOfMonth\" : 1,\r\n        \"byDayOfMonth\" : true,\r\n        "
@"\"dayOfMonth\" : 1,\r\n        \"esmWeekends\" : false,\r\n        "
@"\"minimumBuffer\" : 59,\r\n        \"joinDateMillis\" : 0,\r\n        "
@"\"beginDate\" : null,\r\n        \"id\" : null,\r\n        "
@"\"onlyEditableOnJoin\" : false,\r\n        \"userEditable\" : true,\r\n "
@"       \"byDayOfWeek\" : false,\r\n        \"defaultMinimumBuffer\" : "
@"59,\r\n        \"nameOfClass\" : "
@"\"com.pacoapp.paco.shared.model2.Schedule\"\r\n      } ],\r\n      "
@"\"nameOfClass\" : "
@"\"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n    } ],\r\n    "
@"\"inputs\" : [ ],\r\n    \"endOfDayGroup\" : false,\r\n    "
@"\"endOfDayReferredGroupName\" : null,\r\n    \"feedback\" : null,\r\n   "
@" \"feedbackType\" : 0,\r\n    \"nameOfClass\" : "
@"\"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n  } ],\r\n  "
@"\"nameOfClass\" : "
@"\"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}]";


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

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}


- (void)testJoinExperiment{
    
    NSData* data = [def0 dataUsingEncoding:NSUTF8StringEncoding];
    PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayOfClasses:_classes
                          withNameOfClassAttribute:@"nameOfClass"];
    
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
    IOSObjectArray * iosArray = [resultArray toArray];
    
   PAExperimentDAO * dao =  [iosArray objectAtIndex:0];
 
    
    PacoExtendedClient* client= [PacoExtendedClient sharedInstance];
   // PAActionScheduleGenerator * asg = [[PAActionScheduleGenerator alloc] initWithPAExperimentDAO:dao];
    
    PASchedule  * oo = [dao  valueForKeyPathEx:@"groups[0].actionTriggers[0].schedules[0]"];
    
    [client joinExperimentWithDefinition:dao schedule:oo completionBlock:^{
        
    }];
    
    
}


/*
 
 - (instancetype)initWithJavaLangInteger:(JavaLangInteger *)scheduleType
 withJavaLangBoolean:(JavaLangBoolean *)byDayOfMonth
 withJavaLangInteger:(JavaLangInteger *)dayOfMonth
 withJavaLangLong:(JavaLangLong *)esmEndHour
 withJavaLangInteger:(JavaLangInteger *)esmFrequency
 withJavaLangInteger:(JavaLangInteger *)esmPeriodInDays
 withJavaLangLong:(JavaLangLong *)esmStartHour
 withJavaLangInteger:(JavaLangInteger *)nthOfMonth
 withJavaLangInteger:(JavaLangInteger *)repeatRate
 withJavaUtilList:(id<JavaUtilList>)times
 withJavaLangInteger:(JavaLangInteger *)weekDaysScheduled
 withJavaLangBoolean:(JavaLangBoolean *)esmWeekends
 withJavaLangInteger:(JavaLangInteger *)timeout
 withJavaLangInteger:(JavaLangInteger *)minimumBuffer
 withJavaLangInteger:(JavaLangInteger *)snoozeCount
 withJavaLangInteger:(JavaLangInteger *)snoozeTime;
 
 
 
 */
/*

-(PASchedule* ) setupSchedul:(OrgJodaTimeDateTime*) startDate
                EndHoursMill:(long)endHoursMill
               StartHourMill:(long) startHourMill
               ESMFrequencey:(int) esmFrequency
                   EMSPeriod:(int) esmPeriod
                 ESMWeekends:(bool) esmWeekends
{
    
    PASchedule* schedule  = [[PASchedule alloc] initWithJavaLangInteger:[JavaLangInteger valueOfWithInt:0]
                                                    withJavaLangBoolean: [JavaLangBoolean valueOfWithBoolean:NO]
                                                    withJavaLangInteger:nil
                                                       withJavaLangLong:[JavaLangLong valueOfWithLong:endHoursMill]
                                                    withJavaLangInteger:[JavaLangInteger valueOfWithInt:esmFrequency]
                                                    withJavaLangInteger:[JavaLangInteger valueOfWithInt:esmPeriod]
                                                       withJavaLangLong:[JavaLangLong  valueOfWithLong:startHourMill]
                                                    withJavaLangInteger:nil
                                                    withJavaLangInteger:nil
                                                       withJavaUtilList:nil
                                                    withJavaLangInteger:[JavaLangInteger     valueOfWithInt:2 ]
                                                    withJavaLangBoolean:[JavaLangBoolean valueOfWithBoolean:esmWeekends]
                                                    withJavaLangInteger:nil
                                                    withJavaLangInteger:[JavaLangInteger valueOfWithInt:59]
                                                    withJavaLangInteger:nil
                                                    withJavaLangInteger:nil] ;
    
    return schedule;
    
}


-(PASchedule*)  createSchedule
{
    OrgJodaTimeDateTime *startDate = [[OrgJodaTimeDateTime alloc] initWithInt:2010 withInt:12 withInt:20 withInt:0 withInt:0 withInt:0 withInt:0];
    
    
    
    
    OrgJodaTimeHours * hours        = [OrgJodaTimeHours hoursWithInt:17];
    OrgJodaTimeDuration * duration  = [hours toStandardDuration];
    long endHourMIlls               = [duration  getStandardSeconds]*1000;
    
    
    OrgJodaTimeHours *      startHours = [OrgJodaTimeHours hoursWithInt:9];
    OrgJodaTimeDuration *   startDuration = [startHours toStandardDuration];
    long                    startHourMIlls = [startDuration  getStandardSeconds]*1000;
    
    
    int     esmFrequency = 1;
    int     esmPeriod = 0;
    BOOL    esmWeekends = false;
    
    
    
   PASchedule* schedule  = [self setupSchedul:startDate EndHoursMill:endHourMIlls StartHourMill:startHourMIlls ESMFrequencey:esmFrequency EMSPeriod:esmPeriod ESMWeekends:esmWeekends];
   
    
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
 */



@end
