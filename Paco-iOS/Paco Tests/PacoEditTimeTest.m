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




static NSString *dataSource = @"}";



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
                    
                    
                    [NSDate time
               
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
