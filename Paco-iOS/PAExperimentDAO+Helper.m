//
//  PAExperimentDAO+Helper.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/11/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PAExperimentDAO+Helper.h"
#import "java/lang/Long.h"
#import "ActionScheduleGenerator.h" 

#import "OrgJodaTimeDateTime+PacoDateHelper.h"
#import "NSDate+PacoTimeZoneHelper.h"
#import "OrgJodaTimeDateMidnight+PacoDateHelper.h"
#import "PacoScheduleUtil.h"
//
#import "NSObject+J2objcKVO.h"
#include "ScheduleTrigger.h"
#include  "Schedule.h"
#include "SignalTime.h"
#include "ExperimentGroup.h"
#import "Paco-Swift.h"
#import "PacoTimeCellModel.h"
#import "SchedulePrinter.h"
#import "PAExperimentDAO+Helper.h"
#import "NSObject+J2objcKVO.h"
#import "PacoExperimentSchedule.h" 


@implementation PAExperimentDAO (Helper)

-(NSString*) instanceId
{
    NSString* retValue = nil;
    
    if(self->id__ !=nil)
    {
      retValue = [self->id__ stringValue];
    }
    
    return  retValue;
    
}


-(NSString*) scheduleString
{

    NSString * schedulingString =  [PacoScheduleUtil buildScheduleString:self];
    return schedulingString;
    
}

-(NSString*) lastEndDate
{
    
    OrgJodaTimeDateTime * joda =  [PAActionScheduleGenerator getLastEndTimeWithPAExperimentDAO:self];
    NSDate* date = [joda nsDateValue];
    NSString* dateString = [date dateToStringLocalTimezone];
    return dateString;
    
}


-(NSString*) earliestStartDate
{
    
    OrgJodaTimeDateMidnight * joda =  [PAActionScheduleGenerator getEarliestStartDateWithPAExperimentDAO:self];
    NSDate* date = [joda nsDateValue];
    NSString* dateString = [date dateToStringLocalTimezone];
    return dateString;
    
}


- (BOOL)isCompatibleWithIOS
{
    BOOL retVal = YES;
    
    NSNumber   * numberOfGroups    = [self  valueForKeyPathEx:@"groups#"];
    int count = [numberOfGroups intValue];
    
    for( int i =0;  i < count; i++)
    {
        NSString* str = [NSString stringWithFormat: @"groups[%i]",i ];
        PAExperimentGroup*  group  =  [self  valueForKeyPathEx:str];
        
        if(![group isCompatibleWithIOS])
        {
            
            retVal = NO;
            break;
            
        }
    }
    
    
    
    return retVal;
    
    
}

-(PAInput2*) inputWithId:(NSString*) inputID
{
    PAInput2* retValue;
     NSNumber   * numberOfGroups    = [self  valueForKeyPathEx:@"groups#"];
     int count = [numberOfGroups intValue];
    
     for( int i =0;  i < count; i++)
     {
          NSString* str = [NSString stringWithFormat: @"groups[%i]",i ];
          PAExperimentGroup*  group  =  [self  valueForKeyPathEx:str];
          JavaUtilArrayList*  list =    (JavaUtilArrayList*)   [group getInputs] ;
          IOSObjectArray * inputArray =  [list toArray];
         
         for(PAInput2 * input in inputArray)
         {
             // should be by name not by id"
             NSString* theId = [input  valueForKeyEx:@"id"];
             if([inputID isEqualToString:theId])
             {
                 
                 retValue = input;
                 break;
             }
             
         }
 
     }
         return retValue;
  
}





-(NSDictionary* ) inputs
{
    
    NSMutableDictionary* dictionaryOfInputArray  = [NSMutableDictionary new];
    NSNumber   * numberOfGroups    = [self  valueForKeyPathEx:@"groups#"];
    int count = [numberOfGroups intValue];
    
    for( int i =0;  i < count; i++)
    {
        NSString* str = [NSString stringWithFormat: @"groups[%i]",i ];
        PAExperimentGroup*  group  =  [self  valueForKeyPathEx:str];
        NSString * groupName = [group  valueForKeyPathEx:@"name"];
        JavaUtilArrayList*  list =    (JavaUtilArrayList*)   [group getInputs] ;
        IOSObjectArray * inputArray =  [list toArray];
        [dictionaryOfInputArray setObject:inputArray forKey:groupName];
        
    
    }

    return dictionaryOfInputArray;
    
}



-(NSString*) description
{
    
    NSMutableString* mutableString = [NSMutableString new];
    
    
     [mutableString appendString:@"Experiment Name: "];
     [mutableString appendString:[self  valueForKeyPathEx:@"title"]];
     [mutableString appendString:@"\n"];
    
    
    NSNumber   * numberOfGroups    = [self  valueForKeyPathEx:@"groups#"];
    int count = [numberOfGroups intValue];
    
    NSMutableArray* groupArray = [[NSMutableArray alloc] init];
    
    for( int i =0;  i < count; i++)
    {
        
        
        
        
        NSString* str = [NSString stringWithFormat: @"groups[%i]",i ];
        PAExperimentGroup*  group  =  [self  valueForKeyPathEx:str];

        NSString * groupName = [group  valueForKeyPathEx:@"name"];
      
        [mutableString appendString:@" Group Name: "];
        [mutableString appendString:groupName];
        [mutableString appendString:@"\n"];

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
                
                
                if([[schedule getScheduleType] intValue] == 4)
                {
                    
    
                    NSString*  startTime = [PASchedulePrinter getHourOffsetAsTimeStringWithJavaLangLong:[schedule getEsmStartHour]];
                    
                    [mutableString appendString:@"  Start Time: "];
                    [mutableString appendString:startTime];
                    [mutableString appendString:@"\n"];
     
                    NSString*  endTime = [PASchedulePrinter getHourOffsetAsTimeStringWithJavaLangLong:[schedule getEsmEndHour]];
                    [mutableString appendString:@"  End Time: "];
                    [mutableString appendString:endTime];
                    [mutableString appendString:@"\n"];
                    
                    
                    
              
                    
                }
                else if([[schedule getScheduleType] intValue] ==0)
                {
                    NSNumber*  numberOfActionTriggers =
                    [schedule valueForKeyEx:@"signalTimes#"];
                    
                    int signalTimesCount = [numberOfActionTriggers intValue];
                    
                    
                    
                    
                    
                    for( int iiii =0;  iiii< signalTimesCount; iiii++)
                    {
                        
                        
                        
                        DatePickerCell*  datePickerCell =  [[DatePickerCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:nil];
                        
                        
                  
                        //[mutableString appendString:@"  Signal Time" ];
                        
                        datePickerCell.groupName = [group valueForKeyEx:@"name"];
                        NSString* str = [NSString stringWithFormat: @"signalTimes[%i]",iiii ];
                        PASignalTime * signalTime =  [schedule valueForKeyEx:str];
                        NSString* timeOfDayStr =  [PASchedulePrinter getHourOffsetAsTimeStringWithPASignalTime:signalTime]  ;
                        NSString* label= [signalTime getLabel];
                        
                        
                        
                        [mutableString appendString:[NSString stringWithFormat:@"  %@ :",label]];
                        [mutableString appendString:timeOfDayStr];
                        [mutableString appendString:@"\n"];
                        
                        
              
                        
                       
                        
                    }
                }
                
                
               
                
                
            }
            
            
            
        }
        
    }
    
    
    return mutableString;
    
}



-(NSDictionary*) fetchExperimentGroupDictionary
{
    
    NSMutableDictionary * dict = [NSMutableDictionary new];
    NSArray * groups = [self fetchAllExperimentGroups];
    
    for(PAExperimentGroup*  group in groups)
    {
        NSString* name = [group valueForKeyEx:@"name"];
        dict[name] = group;
    }
    
    return dict;
}




-(PAExperimentGroup*) groupWithName:(NSString*) groupName
{
    PAExperimentGroup* foundGroup;
    NSNumber   * numberOfGroups    = [self  valueForKeyPathEx:@"groups#"];
    int count = [numberOfGroups intValue];
    for( int i =0;  i < count; i++)
    {
        NSString* str = [NSString stringWithFormat: @"groups[%i]",i ];
        PAExperimentGroup*  group  =  [self  valueForKeyPathEx:str];
        
        NSString* currName = [group  valueForKey:@"name"];
        
        if([groupName isEqualToString:currName])
        {
            
            foundGroup = group;
        }
    }
    return foundGroup;
    
   
   
}



-(PAExperimentGroup*) soloGroup
{
    NSNumber   * numberOfGroups    = [self  valueForKeyPathEx:@"groups#"];
    int numGroups = [numberOfGroups intValue];
    assert(numGroups ==1);
    
    PAExperimentGroup*  group;
    group  =  [self  valueForKeyPathEx:@"groups[0]"];
    return group;
  
}

-(int) numberOfGroups
{
    
    NSNumber   * numberOfGroups    = [self  valueForKeyPathEx:@"groups#"];
    return [numberOfGroups intValue];
    
    
}


-(NSArray*) fetchAllExperimentGroups
{
    NSMutableArray* allGroups   = [NSMutableArray new];
    NSNumber   * numberOfGroups    = [self  valueForKeyPathEx:@"groups#"];
    int count = [numberOfGroups intValue];
    for( int i =0;  i < count; i++)
    {
        NSString* str = [NSString stringWithFormat: @"groups[%i]",i ];
        PAExperimentGroup*  group  =  [self  valueForKeyPathEx:str];
        [allGroups  addObject:group];
        
    }
    
    return allGroups;
    
}



/* 
 
  consider refactorying this method. the experiment should not need to know how its schedule is rendered. 
 
 */

-(NSArray*) getTableCellModelObjects
{
    
    
    
    NSNumber   * numberOfGroups    = [self  valueForKeyPathEx:@"groups#"];
    int count = [numberOfGroups intValue];
    
    NSMutableArray* groupArray = [[NSMutableArray alloc] init];
    
    for( int i =0;  i < count; i++)
    {
        
        
        
        
        NSString* str = [NSString stringWithFormat: @"groups[%i]",i ];
        PAExperimentGroup*  group  =  [self  valueForKeyPathEx:str];
        
        
        
        
        
        
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
                
                NSMutableArray * cellModelsForGroup = [[NSMutableArray alloc] init];
                
                if([[schedule getScheduleType] intValue] == kPacoScheduleTypeESM )
                {
                         DatePickerCell*  startTimeCell =  [[DatePickerCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:nil];
                    
                    startTimeCell.groupName = [group valueForKeyEx:@"name"];
                    startTimeCell.timeLabelStr = @"Start";
                    
                    
                    DatePickerCell*  endTimeCell =  [[DatePickerCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:nil];
                    
                    endTimeCell.timeLabelStr = @"End";
                    endTimeCell.groupName =[group valueForKeyEx:@"name"];
                    
                    
                    NSString*  startTime = [PASchedulePrinter getHourOffsetAsTimeStringWithJavaLangLong:[schedule getEsmStartHour]];
                    
                    startTimeCell.timeOfDayString = startTime;
                    
                    NSString*  endTime = [PASchedulePrinter getHourOffsetAsTimeStringWithJavaLangLong:[schedule getEsmEndHour]];
                    
                    
                    endTimeCell.timeOfDayString= endTime; 
                    
                    
                    
                    [endTimeCell setup];
                    [startTimeCell setup];
                    [cellModelsForGroup addObject:startTimeCell];
                    [cellModelsForGroup addObject:endTimeCell];
                    
                }
                else if( [[schedule getScheduleType] intValue] ==kPacoScheduleTypeDaily
                        || [[schedule getScheduleType] intValue] ==kPacoScheduleTypeWeekday
                        || [[schedule getScheduleType] intValue] ==kPacoScheduleTypeWeekly
                        || [[schedule getScheduleType] intValue] == kPacoScheduleTypeMonthly 
                        
                        
                      /* need to add more types here */ )
                {
                        NSNumber*  numberOfActionTriggers =
                        [schedule valueForKeyEx:@"signalTimes#"];
                        
                        int signalTimesCount = [numberOfActionTriggers intValue];
                        
                    
                        
                        
                        
                        for( int iiii =0;  iiii< signalTimesCount; iiii++)
                        {
                            
                            
                            
                           DatePickerCell*  datePickerCell =  [[DatePickerCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:nil];
                            
                    
                            datePickerCell.groupName = [group valueForKeyEx:@"name"];
                            NSString* str = [NSString stringWithFormat: @"signalTimes[%i]",iiii ];
                            PASignalTime * signalTime =  [schedule valueForKeyEx:str];
                            NSString* timeOfDayStr =  [PASchedulePrinter getHourOffsetAsTimeStringWithPASignalTime:signalTime]  ;
                            NSString* label= [signalTime getLabel];
                            datePickerCell.signalTime = signalTime;
                            datePickerCell.timeLabelStr = label;
                            datePickerCell.timeOfDayString = timeOfDayStr;
                            
                        
                            
                             NSLog(@" ----type %i", [[schedule getScheduleType] intValue] );
                             datePickerCell.theType = @"first";
                            
                            
                            
                            [datePickerCell setup];
                            [cellModelsForGroup addObject:datePickerCell];
                            
                        }
                }

                
                [groupArray addObject:cellModelsForGroup];
                
                
            }

        }
        
    }

    return groupArray;
    
}


/*
   to do, should return true if the experiment contains no scheduling information.
 
 */
-(BOOL) isSelfReport
{
    // ToDo
    return NO;
    
}

@end
