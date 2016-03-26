//
//  PacoEditTimeTest.m
//  Paco
//
//  Created by Northrop O'brien on 3/14/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "ExperimentDAO.h" 
#import "TestUtil.h" 
#import "NSObject+J2objcKVO.h"
#include "ScheduleTrigger.h"
#include  "Schedule.h"
#include "SignalTime.h" 
#include "ExperimentGroup.h"
#import "Paco-Swift.h" 
#import "PacoTimeCellModel.h" 
#import "SchedulePrinter.h"
#import "PAExperimentDAO+Helper.h"
#include "java/util/ArrayList.h"
#include "java/util/Iterator.h"
#include "java/lang/Boolean.h"
#include "java/lang/Long.h"
#include "java/lang/Integer.h"
#include "java/lang/Float.h"
#include "java/lang/Double.h"
#include "java/lang/Boolean.h"
#include "java/lang/Short.h"
#include "java/lang/Character.h"
#include "J2ObjC_header.h"


@interface PacoEditTimeTest : XCTestCase

@end

@implementation PacoEditTimeTest



static NSString *dataSource = @" {\r\n  \"title\": \"my new experiment\",\r\n  \"creator\": \"testingpacotoday@gmail.com\",\r\n  \"contactEmail\": \"testingpacotoday@gmail.com\",\r\n  \"id\": 5866608721395712,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2016\/03\/15\",\r\n  \"published\": true,\r\n  \"admins\": [\r\n    \"testingpacotoday@gmail.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 7,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"GroupAd\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1457994166570,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 0,\r\n              \"color\": 0,\r\n              \"dismissible\": true,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1457994166569,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 53233022,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"start layer\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 46800000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"hidden layer\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 50400000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"hidden layer II\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 54000000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Telos\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1457994166571,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    },\r\n    {\r\n      \"name\": \"GroupB\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1458001540814,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 0,\r\n              \"color\": 0,\r\n              \"dismissible\": true,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1458001540813,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 4,\r\n              \"esmFrequency\": 8,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 43200000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": true,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1458001540815,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>\\nNo need to do anything else for now.<br\/><br\/>\\nPaco will send you a notification when it is time to participate.<br\/><br\/>\\nBe sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

-(NSMutableArray*) modifySignalTime:(PAExperimentDAO *) experiment
{
    NSNumber   * numberOfGroups    = [experiment valueForKeyPathEx:@"groups#"];
    int count = [numberOfGroups intValue];
    
    NSMutableArray* groupArray = [[NSMutableArray alloc] init];
    
    for( int i =0;  i < count; i++)
    {
        
        
        
        
        NSString* str = [NSString stringWithFormat: @"groups[%i]",i ];
        PAExperimentGroup*  group  =  [experiment valueForKeyPathEx:str];
        
        
        
        
        
        
        NSNumber*  numberOfActionTriggers =
        [group  valueForKeyEx:@"actionTriggers#"];
        int actionTriggerCount = [numberOfActionTriggers intValue];
        
        for(int ii =0; ii < actionTriggerCount; ii++)
        {
            
            NSString* str = [NSString stringWithFormat: @"actionTriggers[%i]",ii ];
            PAScheduleTrigger  *trigger = [group valueForKeyEx:str];
            
            
            
            NSNumber* numberOfSchedules = [trigger  valueForKeyEx:@"schedules#"];
            int schedulesCount = [numberOfSchedules intValue];
            
            for(int iii=0; iii < schedulesCount; iii++)
            {
                NSString* str = [NSString stringWithFormat: @"schedules[%i]",iii ];
                PASchedule * schedule  = [trigger valueForKeyEx:str];
                
                NSNumber*  numberOfActionTriggers =
                [schedule valueForKeyEx:@"signalTimes#"];
                
                int signalTimesCount = [numberOfActionTriggers intValue];
                
                NSMutableArray * cellModelsForGroup = [[NSMutableArray alloc] init];
                
                
                for( int iiii =0;  iiii< signalTimesCount; iiii++)
                {
                    
                    PacoTimeCellModel * model   = [[PacoTimeCellModel alloc] init];
                    NSString* name = [group valueForKeyEx:@"name"];
                    model.groupName = name;
                    
                    NSString* str = [NSString stringWithFormat: @"signalTimes[%i]",iiii ];
                    PASignalTime * signalTime =  [schedule valueForKeyEx:str];
                    NSString* timeOfDayStr =  [PASchedulePrinter getHourOffsetAsTimeStringWithPASignalTime:signalTime]  ;
                    model.signalTime = signalTime;
                    model.millisecondsSinceMidnight =[[signalTime getFixedTimeMillisFromMidnight] intValue];
                    model.timeLabelStr = timeOfDayStr;
                    [cellModelsForGroup addObject:model];
                    [signalTime setFixedTimeMillisFromMidnightWithJavaLangInteger:[JavaLangInteger valueOfWithInt:-1]];
                    
                }
                
                [groupArray addObject:cellModelsForGroup];
                
                
            }
            
            
            
        }
        
    }
    
    
    return groupArray;
    
}
-(NSMutableArray*) getTableCellModelObjects:(PAExperimentDAO *) experiment
{
    NSNumber   * numberOfGroups    = [experiment valueForKeyPathEx:@"groups#"];
    int count = [numberOfGroups intValue];
    
    NSMutableArray* groupArray = [[NSMutableArray alloc] init];
    
    for( int i =0;  i < count; i++)
    {
        
        
        
        
        NSString* str = [NSString stringWithFormat: @"groups[%i]",i ];
        PAExperimentGroup*  group  =  [experiment valueForKeyPathEx:str];
        
        
        
        
        
        
        NSNumber*  numberOfActionTriggers =
        [group  valueForKeyEx:@"actionTriggers#"];
        int actionTriggerCount = [numberOfActionTriggers intValue];
        
        for(int ii =0; ii < actionTriggerCount; ii++)
        {
            
            NSString* str = [NSString stringWithFormat: @"actionTriggers[%i]",ii ];
            PAScheduleTrigger  *trigger = [group valueForKeyEx:str];
            
            
            
            NSNumber* numberOfSchedules = [trigger  valueForKeyEx:@"schedules#"];
            int schedulesCount = [numberOfSchedules intValue];
            
            for(int iii=0; iii < schedulesCount; iii++)
            {
                NSString* str = [NSString stringWithFormat: @"schedules[%i]",iii ];
                PASchedule * schedule  = [trigger valueForKeyEx:str];
                
                NSNumber*  numberOfActionTriggers =
                [schedule valueForKeyEx:@"signalTimes#"];
                
                int signalTimesCount = [numberOfActionTriggers intValue];
                
                NSMutableArray * cellModelsForGroup = [[NSMutableArray alloc] init];
                
                
                for( int iiii =0;  iiii< signalTimesCount; iiii++)
                {
                    
                    PacoTimeCellModel * model   = [[PacoTimeCellModel alloc] init];
                    NSString* name = [group valueForKeyEx:@"name"];
                    model.groupName = name;
                    
                    NSString* str = [NSString stringWithFormat: @"signalTimes[%i]",iiii ];
                    PASignalTime * signalTime =  [schedule valueForKeyEx:str];
                    NSString* timeOfDayStr =  [PASchedulePrinter getHourOffsetAsTimeStringWithPASignalTime:signalTime]  ;
                    model.signalTime = signalTime;
                    model.millisecondsSinceMidnight =[[signalTime getFixedTimeMillisFromMidnight] intValue];
                    model.timeLabelStr = timeOfDayStr;
                    [cellModelsForGroup addObject:model];
                    
                }
                
                [groupArray addObject:cellModelsForGroup];
                
                
            }
            
            
            
        }
        
    }
    
    
    return groupArray;
    
}

- (void)testFetchSignalTimeModel{
    
    
    PAExperimentDAO * experiment =  [TestUtil buildExperiment:dataSource];
    
 
    
    [self modifySignalTime:experiment];
    NSMutableArray* mutableArray = [self getTableCellModelObjects:experiment];
    
    NSLog(@" mutable array %@", mutableArray);
    
    
 
    
}

-(void) testFetchSignalTimeModelWithCategory
{
     PAExperimentDAO * experiment =  [TestUtil buildExperiment:dataSource];
    NSMutableArray * array = [experiment getTableCellModelObjects];
      NSLog(@" mutable array %@", array);
}
 


- (void)testPerformanceExample {
    // This is an example of a performance test case.
    [self measureBlock:^{
        // Put the code you want to measure the time of here.
    }];
}

@end
