//
//  PacoEventTests.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/14/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import  <XCTest/XCTest.h>
#import "PacoEventExtended.h" 
#import   "PacoSerializer.h"
#include  "java/lang/IllegalStateException.h"
#include "java/lang/Integer.h"
#include  "java/lang/Long.h"
#include  "java/util/ArrayList.h"
#include  "java/util/Collections.h"
#include  "java/util/List.h"
#include  "org/joda/time/Hours.h"
#include  "org/joda/time/Duration.h"
#include  "EsmGenerator2.h"
#include  "NSObject+J2objcKVO.h"
#include  "PacoSerializer.h"
#include  "PacoSerializeUtil.h"
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
#include "java/util/List.h"
#include "org/joda/time/Hours.h"
#include "org/joda/time/Duration.h"
#include "EsmGenerator2.h"
#include "NSDate+PacoTimeZoneHelper.h"
#include "PacoEventPersistenceHelper.h"
#include "NSNumber+PacoJ2OBCType.h" 
#include "DateTime.h"
#include "NSDate+PacoTimeZoneHelper.h"






@interface PacoEventTests : XCTestCase
@property(strong, nonatomic) NSArray* classes;
@end

@implementation PacoEventTests

static NSString *dataSource =
@"{\r\n  \"title\": \"Have You Filled out All Paperwork\",\r\n  \"description\": \"Check if I am up to paperwork no matter what the kind, personal, work, bills, timecards.....\",\r\n  \"creator\": \"Timo@google.com\",\r\n  \"organization\": \"My World\",\r\n  \"contactEmail\": \"Timo@google.com\",\r\n  \"id\": 5739004087500800,\r\n  \"recordPhoneDetails\": true,\r\n  \"extraDataCollectionDeclarations\": [\r\n    1,\r\n    2,\r\n    3\r\n  ],\r\n  \"deleted\": false,\r\n  \"published\": true,\r\n  \"admins\": [\r\n    \"Timo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 6,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/8\/7\",\r\n      \"endDate\": \"2014\/8\/14\",\r\n      \"logActions\": true,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1438996742500,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1438996742499,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 4,\r\n              \"esmFrequency\": 11,\r\n              \"esmPeriodInDays\": 1,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": true,\r\n              \"minimumBuffer\": 77,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1438996742501,\r\n              \"onlyEditableOnJoin\": true,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            },\r\n            {\r\n              \"scheduleType\": 4,\r\n              \"esmFrequency\": 8,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": true,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1439490359621,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [\r\n        {\r\n          \"name\": \"myVariable\",\r\n          \"required\": true,\r\n          \"conditional\": true,\r\n          \"conditionExpression\": \"are you finisidhed\",\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"How are we doing?\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        }\r\n      ],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";




- (void)setUp {
    [super setUp];
    
    _classes = [PacoSerializeUtil    getClassNames];
    
    
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}



- (NSArray*)getClassNames {
    NSMutableArray* mutableArray = [NSMutableArray new];
    NSString* path = @"/Users/Timo/Project/paco/Paco-iOS/DerivedData/Paco/"
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


/*
- (void)testSheduleToJsonObject
{
 
    NSArray * classNames = [self getClassNames];
    NSData* data = [dataSource dataUsingEncoding:NSUTF8StringEncoding];
    PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayOfClasses:classNames
                          withNameOfClassAttribute:@"nameOfClass"];
    
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
    IOSObjectArray * iosArray = [resultArray toArray];
    
    PAExperimentDAO * dao =  [iosArray objectAtIndex:0];

    PASchedule * schedule =  [PacoSerializeUtil getScheduleAtIndex:dao  GroupIndex:0 actionTriggerIndex:0 scheduleIndex:0];
    
   // PacoEventExtended * event = [PacoEventExtended joinEventForDefinition:dao withSchedule:schedule];
    
    
    NSString * iddd   = [dao valueForKeyPathEx:@"id"];
    JavaLangInteger  * version  = [dao valueForKeyPathEx:@"version"];
    NSString * name   = [dao valueForKeyPathEx:@"title"];
    XCTAssert( ([event.experimentId longLongValue] ==   [iddd longLongValue]) , @"Pass");
    XCTAssert( event.experimentVersion ==  [[dao valueForKeyPathEx:@"version"] intValue]  , @"Pass");
    XCTAssert([event.experimentName isEqualToString:[dao valueForKeyPathEx:@"title"]], @"Pass");
 
               
}*/




/*
 
 @property (nonatomic, copy) NSString *who;
 @property (nonatomic, retain) NSDate *when;
 @property (nonatomic, assign) long long latitude;
 @property (nonatomic, assign) long long longitude;
 @property (nonatomic, retain) NSDate *responseTime;
 @property (nonatomic, retain) NSDate *scheduledTime;
 @property (nonatomic, readonly, copy) NSString *appId;
 @property (nonatomic, readonly, copy) NSString *pacoVersion;
 @property (nonatomic, copy) NSString *experimentId;
 @property (nonatomic, copy) NSString *experimentName;
 @property (nonatomic, assign) int experimentVersion;
 @property (nonatomic, retain) NSArray *responses;  // <NSDictionary>
 
 */


-(void) testEventToJsonString
{

     NSArray * classNames = [self getClassNames];
     PacoSerializer* serializer = [[PacoSerializer alloc] initWithArrayOfClasses:classNames
                          withNameOfClassAttribute:@"nameOfClass"];
    
    
    PacoEventExtended * event = [[PacoEventExtended alloc] init];
    
    event.who =@"me";
    event.when = [NSDate date];
   // event.latitude = 12345;
   // event.longitude = 54321;
   // event.responseTime= [NSDate date];
    event.experimentId = @"experimentID";
    event.experimentName = @"experimentName";
    
    
    NSData*  data =  [serializer toJSONobject:event];
    NSString *string = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    
    NSLog(@" this is the end %@", string);
    
}


/*
@property (nonatomic, assign) int experimentVersion;
@property (nonatomic, retain) NSArray *responses;  // <NSDictionary>
@property (nonatomic,strong)  NSString* scheduleId;
@property (nonatomic,strong)  NSString* actionTriggerId;
@property (nonatomic,strong)  NSString* groupName;

*/




-(void) testEncoder
{
    
        PacoEventExtended * event = [[PacoEventExtended alloc] init];
        event.who =@"me";
        event.when =  [[NSDate date] dateToStringLocalTimezone] ;
        event.latitude = [NSNumber numberWithInt:12345];
        event.longitude = [NSNumber numberWithInt:765432];
        event.responseTime= [[NSDate date] dateToStringLocalTimezone];
        event.experimentId =  [NSNumber numberWithInt:1234];
        event.experimentName = @"experimentName";
        event.experimentVersion = [NSNumber numberWithInt:5];
    
        JavaUtilArrayList* arrayList = [[JavaUtilArrayList alloc] init];
        [arrayList addWithId:@"One"];
        [arrayList addWithId:@"Two"];
        [arrayList addWithId:@"Three"];
    
        event.responses =arrayList;
        event.actionTriggerId =[NSNumber numberWithInt:12347];[NSNumber numberWithInt:1234];
        event.scheduleId =[NSNumber numberWithInt:12345];
    
    
       /* this is the one */
      PacoEventExtended * eventII = [event copy];
    
      XCTAssertTrue(   [event.who isEqualToString:eventII.who] );
      XCTAssertTrue(   [event.experimentId isEqual:eventII.experimentId]);
      XCTAssertTrue(   [event.experimentName isEqualToString:eventII.experimentName]);
      XCTAssertTrue(   [event.experimentVersion isEqual:eventII.experimentVersion]);
      XCTAssertTrue(   [event.actionTriggerId isEqual: eventII.actionTriggerId]);
      XCTAssertTrue(   [event.scheduleId isEqual:eventII.scheduleId]);
      XCTAssertTrue(   [event.when isEqualToString:eventII.when]);
      XCTAssertTrue(   [event.longitude isEqual:eventII.longitude]);
      XCTAssertTrue(   [event.latitude isEqual:eventII.latitude]);
      XCTAssertTrue(   [event.responses isEqual:eventII.responses]);
}

/*
 
 - (id<PAEventInterface>)getEventWithJavaLangLong:(JavaLangLong *)experimentId
 withOrgJodaTimeDateTime:(OrgJodaTimeDateTime *)scheduledTime
 withNSString:(NSString *)groupName
 withJavaLangLong:(JavaLangLong *)actionTriggerId
 withJavaLangLong:(JavaLangLong *)scheduleId;
 
 
 */
-(void) testPacoEventPersistenceHelperStoreAndFetch
{
    
    PacoEventPersistenceHelper * helper  = [PacoEventPersistenceHelper new];
    [helper deleteAllEvents];
    
    NSString* schedledTime = [[NSDate date] dateToStringLocalTimezone]  ;
    PacoEventExtended * event = [[PacoEventExtended alloc] init];
    event.who =@"me";
    event.when = schedledTime; ;
    event.latitude = [NSNumber numberWithInt:12345];
    event.longitude = [NSNumber numberWithInt:765432];
    event.responseTime= [[NSDate date] dateToStringLocalTimezone];
    event.experimentId =  [NSNumber numberWithInt:1234];
    event.experimentName = @"experimentName";
    event.experimentVersion = [NSNumber numberWithInt:5];
    event.groupName=@"GroupAAA";
    event.scheduleId =[NSNumber numberWithInt:3];
    event.actionTriggerId =[NSNumber numberWithInt:3];
    event.scheduledTime =schedledTime;
    JavaUtilArrayList* arrayList = [[JavaUtilArrayList alloc] init];
    [arrayList addWithId:@"One"];
    [arrayList addWithId:@"Two"];
    [arrayList addWithId:@"Three"];
    event.responses = arrayList;
    [helper insertEventWithPAEventInterface:event];
    
    PacoEventExtended * eventII = (id<PAEventInterface> ) [helper getEventWithJavaLangLong:[event.experimentId toJLL]
                                                                   withOrgJodaTimeDateTime:[NSDate jodaFromString:event.scheduledTime]
                                                                              withNSString:event.groupName withJavaLangLong:[event.actionTriggerId toJLL]
                                                                              withJavaLangLong:[event.scheduleId  toJLL]];
    
    XCTAssertTrue(   [event.who isEqualToString:eventII.who] );
    XCTAssertTrue(   [event.experimentId isEqual:eventII.experimentId]);
    XCTAssertTrue(   [event.experimentName isEqualToString:eventII.experimentName]);
    XCTAssertTrue(   [event.experimentVersion isEqual:eventII.experimentVersion]);
    XCTAssertTrue(   [event.actionTriggerId isEqual: eventII.actionTriggerId]);
    XCTAssertTrue(   [event.scheduleId isEqual:eventII.scheduleId]);
    XCTAssertTrue(   [event.when isEqual:eventII.when]);
    XCTAssertTrue(   [event.longitude isEqual:eventII.longitude]);
    XCTAssertTrue(   [event.latitude isEqual:eventII.latitude]);
    XCTAssertTrue(   [event.responses isEqual:eventII.responses]);
 
    
    
    
    
    
}


-(void) testPacoEventPersistenceHelperUpdate
{
    
    PacoEventPersistenceHelper * helper  = [PacoEventPersistenceHelper new];
    [helper deleteAllEvents];
    
    NSString* schedledTime = [[NSDate date] dateToStringLocalTimezone]  ;
    PacoEventExtended * event = [[PacoEventExtended alloc] init];
    event.who =@"me";
    event.when = schedledTime; ;
    event.latitude = [NSNumber numberWithInt:12345];
    event.longitude = [NSNumber numberWithInt:765432];
    event.responseTime= [[NSDate date] dateToStringLocalTimezone];
    event.experimentId =  [NSNumber numberWithInt:1234];
    event.experimentName = @"experimentName";
    event.experimentVersion = [NSNumber numberWithInt:5];
    event.groupName=@"GroupAAA";
    event.scheduleId =[NSNumber numberWithInt:3];
    event.actionTriggerId =[NSNumber numberWithInt:3];
    event.scheduledTime =schedledTime;
    JavaUtilArrayList* arrayList = [[JavaUtilArrayList alloc] init];
    [arrayList addWithId:@"One"];
    [arrayList addWithId:@"Two"];
    [arrayList addWithId:@"Three"];
    event.responses = arrayList;
    [helper insertEventWithPAEventInterface:event];
    
    PacoEventExtended * modifiedEvent = [event clone];
    
    
    modifiedEvent.who = @"not me";
    
    
    [helper updateEventWithPAEventInterface:modifiedEvent];
    
    
    PacoEventExtended * eventII = (id<PAEventInterface> ) [helper getEventWithJavaLangLong:[event.experimentId toJLL]
                                                                   withOrgJodaTimeDateTime:[NSDate jodaFromString:event.scheduledTime]
                                                                              withNSString:event.groupName withJavaLangLong:[event.actionTriggerId toJLL]
                                                                          withJavaLangLong:[event.scheduleId  toJLL]];
    
    
    XCTAssertFalse(  [event.who isEqualToString:eventII.who] );
    XCTAssertTrue(   [event.experimentId isEqual:eventII.experimentId]);
    XCTAssertTrue(   [event.experimentName isEqualToString:eventII.experimentName]);
    XCTAssertTrue(   [event.experimentVersion isEqual:eventII.experimentVersion]);
    XCTAssertTrue(   [event.actionTriggerId isEqual: eventII.actionTriggerId]);
    XCTAssertTrue(   [event.scheduleId isEqual:eventII.scheduleId]);
    XCTAssertTrue(   [event.when isEqual:eventII.when]);
    XCTAssertTrue(   [event.longitude isEqual:eventII.longitude]);
    XCTAssertTrue(   [event.latitude isEqual:eventII.latitude]);
    XCTAssertTrue(   [event.responses isEqual:eventII.responses]);
    
    
    
    
    
}


-(void) testPacoEventPersistenceHelperAllEvents
{
    
    PacoEventPersistenceHelper * helper  = [PacoEventPersistenceHelper new];
    [helper deleteAllEvents];
    
    NSString* schedledTime = [[NSDate date] dateToStringLocalTimezone]  ;
    PacoEventExtended * event = [[PacoEventExtended alloc] init];
    event.who =@"me";
    event.when = schedledTime; ;
    event.latitude = [NSNumber numberWithInt:12345];
    event.longitude = [NSNumber numberWithInt:765432];
    event.responseTime= [[NSDate date] dateToStringLocalTimezone];
    event.experimentId =  [NSNumber numberWithInt:1234];
    event.experimentName = @"experimentName";
    event.experimentVersion = [NSNumber numberWithInt:5];
    event.groupName=@"GroupAAA";
    event.scheduleId =[NSNumber numberWithInt:3];
    event.actionTriggerId =[NSNumber numberWithInt:3];
    event.scheduledTime =schedledTime;
    JavaUtilArrayList* arrayList = [[JavaUtilArrayList alloc] init];
    [arrayList addWithId:@"One"];
    [arrayList addWithId:@"Two"];
    [arrayList addWithId:@"Three"];
    event.responses = arrayList;
    [helper insertEventWithPAEventInterface:event];
    
    NSArray * eventsArray  = [helper  eventsForUpload];
    XCTAssertTrue([eventsArray count]==1,@"exactly one event for upload");

    PacoEventExtended * singelEvent =  [eventsArray firstObject];
    [helper markUploaded:singelEvent];
    
    eventsArray  = [helper  eventsForUpload];
    XCTAssertTrue([eventsArray count]==0,@"exactly zero event for upload");
    
    
    
    
    
    
}
// updateEventWithPAEventInterface



@end
