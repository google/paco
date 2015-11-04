//
//  PacoEventTests.m
//  Paco
//
//  Created by northropo on 10/28/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "TestUtil.h"
#import "ExperimentDAO.h" 
#import "PacoEventExtended.h"
#import "NSObject+J2objcKVO.h"
#import "ExperimentGroup.h" 
#import "ActionTrigger.h" 
#import "SchedulePrinter.h"
#import "PAExperimentDAO+Helper.h"
#import "PacoEventExtended.h"
#import "PacoEventExtended+PacoCoder.h"

@interface PacoEventUtilTests : XCTestCase

@end

@implementation PacoEventUtilTests : XCTestCase


static NSString *dataSource = @"{\r\n  \"title\": \"Large Experiment\",\r\n  \"description\": \"experiment to test handling large multiple elements\",\r\n  \"creator\": \"northropo@google.com\",\r\n  \"organization\": \"big organization\",\r\n  \"contactEmail\": \"northropo@google.com\",\r\n  \"id\": 5687040687472640,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/10\/28\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 6,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1446067923940,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"color\": 0,\r\n              \"dismissible\": true,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1446067923939,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 46800000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"label\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 61200000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"label 2\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1446067923941,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    },\r\n    {\r\n      \"name\": \"new group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1446067923943,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"color\": 0,\r\n              \"dismissible\": true,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1446067923942,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 32400000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 39600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 43200000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 54000000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1446067923944,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>\\nNo need to do anything else for now.<br\/><br\/>\\nPaco will send you a notification when it is time to participate.<br\/><br\/>\\nBe sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";



- (void)setUp {
    [super setUp];
    }

- (void)tearDown {
    
    [super tearDown];
}

- (void)testExample {
  
    XCTAssert(YES, @"Pass");
}

/*
public String createSchedulesString() {
    StringBuffer buf = new StringBuffer();
    List<ExperimentGroup> groups = experiment.getExperimentDAO().getGroups();
    boolean firstItem = true;
    for (ExperimentGroup experimentGroup : groups) {
        List<ActionTrigger> actionTriggers = experimentGroup.getActionTriggers();
        for (ActionTrigger actionTrigger : actionTriggers) {
            if (actionTrigger instanceof ScheduleTrigger) {
                List<Schedule> schedules = ((ScheduleTrigger)actionTrigger).getSchedules();
                
                for (Schedule schedule : schedules) {
                    if (firstItem) {
                        firstItem = false;
                    } else {
                        buf.append("; ");
                    }
                    buf.append(SchedulePrinter.toString(schedule));
                }
            }
        }
    }
    return buf.toString();
}
 
 
 
 
 -(BOOL) isSelfReport:(PAExperimentDAO*) experiment
 {
 BOOL retVal = YES;
 
 
 
 if([experiment valueForKeyEx:@"groups"] && [[experiment valueForKeyEx:@"groups#"] intValue] >0)
 {
 NSNumber *numberOfGroups = [experiment valueForKeyEx:@"groups#"];
 int numGroups = [numberOfGroups intValue];
 
 
 
 for(int i=0; i < numGroups; i++)
 {
 NSString* groupStr = [NSString stringWithFormat:@"groups[%i]",i];
 PAExperimentGroup * group = [experiment valueForKeyEx:groupStr];
 if([group valueForKeyEx:@"actionTriggers"]  && [[group valueForKeyEx:@"actionTriggers#"] intValue] > 0)
 {
 retVal = NO;
 break;
 }
 }
 }
 return retVal;
 
 }
 
 
 
 */





-(void) testCreateScheduleString
{
    
     PAExperimentDAO * experiment =  [TestUtil buildExperiment:dataSource];
     NSString * scheduleString  = [experiment scheduleString];
     NSLog(@" schedule string %@ ->", scheduleString);
    
   
}


-(void) testGenerate
{
    PAExperimentDAO * experiment =  [TestUtil buildExperiment:dataSource];
    PacoEventExtended* event = [PacoEventExtended joinEventForActionSpecificatonWithServerExperimentId:experiment serverExperimentId:@"not applicable"];
     XCTAssert(TRUE ,@" No Excpetions thrown or Sigs");
    
}


@end
