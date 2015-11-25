//
//  PacoInputTest.m
//  Paco
//
//  Created by northropo on 11/24/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "ExperimentDAO.h"
#import "ExperimentGroup.h"
#import "PacoSerializer.h" 
#include "java/util/ArrayList.h"
#import "PacoInputEvaluator.h" 
#import "java/util/List.h"
#import "NSObject+J2objcKVO.h"

@interface PacoInputTest : XCTestCase

@property (nonatomic,strong) PAExperimentDAO* experiment;
@property (nonatomic,strong) PAExperimentGroup * group;

@end

@implementation PacoInputTest




static NSString * JSON_DATA = @"{\r\n  \"title\": \"Drink Water Required\",\r\n  \"description\": \"tim obrien\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"Self\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5660531612450816,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/11\/24\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 17,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"startDate\": \"2015\/8\/29\",\r\n      \"endDate\": \"2015\/9\/30\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1440120356423,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"color\": 0,\r\n              \"dismissible\": true,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1440120356422,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 32400000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Nine AM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 36000000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Three PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 57600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"4 PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1440120356424,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": false,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [\r\n        {\r\n          \"name\": \"first question\",\r\n          \"required\": true,\r\n          \"conditional\": false,\r\n          \"conditionExpression\": \"this>=$that\",\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"what is your favorite drink\",\r\n          \"likertSteps\": 5,\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"last drink\",\r\n          \"required\": true,\r\n          \"conditional\": false,\r\n          \"responseType\": \"list\",\r\n          \"text\": \"Select Last Drink\",\r\n          \"likertSteps\": 5,\r\n          \"listChoices\": [\r\n            \"Soa\",\r\n            \"Coffee\",\r\n            \"Water\",\r\n            \"Tea\",\r\n            \"Others\"\r\n          ],\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\",\r\n          \"conditionExpression\": \"first question = \\\"yes\\\"\"\r\n        },\r\n        {\r\n          \"name\": \"myinputname\",\r\n          \"required\": false,\r\n          \"conditional\": true,\r\n          \"conditionExpression\": \"Last Drink == 1\",\r\n          \"responseType\": \"likert\",\r\n          \"text\": \"how do you like this\",\r\n          \"likertSteps\": 5,\r\n          \"leftSideLabel\": \"low\",\r\n          \"rightSideLabel\": \"high\",\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        }\r\n      ],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";







-( PAExperimentDAO*) buildExpeiment:(NSString*) json
{
    
    NSData* data =nil;
    data=  [json dataUsingEncoding:NSUTF8StringEncoding];
    
    PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayOfClasses:nil
                          withNameOfClassAttribute:@"nameOfClass"];
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
    IOSObjectArray * iosArray = [resultArray toArray];
    PAExperimentDAO * dao =  [iosArray objectAtIndex:0];
    return dao;
    
}

- (void)setUp {
    [super setUp];
    
    
     self.experiment = [self buildExpeiment:JSON_DATA];
     self.group =  [self.experiment valueForKeyPathEx:@"groups[0]"];

}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testInput {
    
    PacoInputEvaluator * evaluator = [PacoInputEvaluator evaluatorWithExperimentGroup:self.group];
    NSArray * array =  [evaluator evaluateAllInputs];
 
    
 
    XCTAssert(YES, @"Pass");
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
