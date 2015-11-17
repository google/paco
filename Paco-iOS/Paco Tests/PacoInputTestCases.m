//
//  PacoInputTestCases.m
//  Paco
//
//  Created by northropo on 11/9/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "TestUtil.h" 
#import "ExperimentHelper.h" 
#import "java/util/ArrayList.h"
#import "java/util/List.h"
#import "Input2.h"




@interface PacoInputTestCases : XCTestCase

@end

@implementation PacoInputTestCases

static NSString *dataSource = @"{\r\n  \"title\": \"For Inputing\",\r\n  \"description\": \"to view inputting controlls\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"self\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5726461709254656,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/11\/06\",\r\n  \"published\": true,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [\r\n    \"elasticsearch64@gmail.com\"\r\n  ],\r\n  \"version\": 22,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1446848310613,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"color\": 0,\r\n              \"dismissible\": true,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1446848310612,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 53700000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"only time\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1446848310614,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [\r\n        {\r\n          \"name\": \"apple\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"best fruit\",\r\n          \"likertSteps\": 5,\r\n          \"listChoices\": [\r\n            \"most intersting\",\r\n            \"pair\",\r\n            \"orange\",\r\n            \"plum\"\r\n          ],\r\n          \"multiselect\": true,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"uu\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"list\",\r\n          \"text\": \"how are you\",\r\n          \"likertSteps\": 5,\r\n          \"listChoices\": [\r\n            \"234\",\r\n            \"2346\",\r\n            \"765\"\r\n          ],\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        }\r\n      ],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>\\nNo need to do anything else for now.<br\/><br\/>\\nPaco will send you a notification when it is time to participate.<br\/><br\/>\\nBe sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";





- (void)testInputs {
    
     PAExperimentDAO * experiment =  [TestUtil buildExperiment:dataSource];
     id<JavaUtilList>  inputs =  [PAExperimentHelper getInputsWithPAExperimentDAO:experiment];
    
    PAInput2* input;
    
    for(input in inputs)
    {
        NSLog(@" name : %@", [input getName]);
        NSLog(@" name : %@", [input getResponseType]);
        
    }
    
    
    
    XCTAssert(YES, @"Pass");
}

- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
