//
//  PAExperimentGroup+PacoGroupHelper.m
//  Paco
//
//  Created by Northrop O'brien on 7/6/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "PAExperimentGroup+PacoGroupHelper.h"
#import "NSObject+J2objcKVO.h"
#import "ScheduleTrigger.h"
#import "PAActionTrigger+PacoHelper.h" 
#import "JavaUtilArrayList+PacoConversion.h"
#import "Input2.h" 
#include "java/util/ArrayList.h"
#include "java/util/List.h"


@implementation PAExperimentGroup (PacoGroupHelper)


-(PacoFeedbackType) feedbackType
{
    NSNumber* feedbackType   = [self valueForKeyEx:@"feedbackType"];
    PacoFeedbackType type = [feedbackType intValue];

    return type;
    
}



-(PAInput2*) inputWithId:(NSString*) inputID
{
    PAInput2* retValue;
 
    
    JavaUtilArrayList*  list =    (JavaUtilArrayList*)   [self getInputs] ;
    IOSObjectArray * inputArray =  [list toArray];
    
        for(PAInput2 * input in inputArray)
        {
            // should be by name not by id"
            NSString* theId = [input  valueForKeyEx:@"name"];
            if([inputID isEqualToString:theId])
            {
                
                retValue = input;
                break;
            }
            
        }
        
  
    return retValue;
    
}



-(NSString*) jsonStringForJavascript
{
    NSString* feedback   = [self valueForKeyPathEx:@"feedback.text"];
    return feedback;
}



-(BOOL) isContainsAllOthers
{
    BOOL isContainsAllOthers = NO;
    NSNumber*  numberOfActionTriggers =  [self   valueForKeyEx:@"actionTriggers#"];
    int actionTriggerCount = [numberOfActionTriggers intValue];
    
    for(int ii =0; ii < actionTriggerCount; ii++)
    {
        NSString* str = [NSString stringWithFormat: @"actionTriggers[%i]",ii ];
        PAActionTrigger  *actionTrigger = [self  valueForKeyEx:str];
        
        if( [actionTrigger containsAllOthers])
        {
            isContainsAllOthers = YES;
            break;
            
        }
        
        
    }
    return isContainsAllOthers;
    
}


-(NSArray* ) allInputs
{
    
    JavaUtilArrayList*  list =    (JavaUtilArrayList*)   [self  getInputs] ;
    NSArray * inputArray =  [list toNSArray];
    return inputArray;
    
}



- (BOOL)isGroupTriggered
{
    
    BOOL isGroupTriggered = NO;
    NSNumber*  numberOfActionTriggers =  [self   valueForKeyEx:@"actionTriggers#"];
    int actionTriggerCount = [numberOfActionTriggers intValue];
    PAActionTrigger   *trigger ;
    for(int ii =0; ii < actionTriggerCount; ii++)
    {
        NSString* str = [NSString stringWithFormat: @"actionTriggers[%i]",ii ];
        trigger = [self  valueForKeyEx:str];
        if( ![trigger isKindOfClass:[PAScheduleTrigger class]])
        {
            
           
            
            isGroupTriggered =  YES;
            break;
        }
        
    }
    
    if(!isGroupTriggered)
    {
        NSObject * cues    = [trigger  valueForKeyPathEx:@"cues"];
        
        if(cues)
        {
            isGroupTriggered = YES;
            
        }
    }
    
    return isGroupTriggered;
    
}


- (BOOL)isCompatibleWithIOS {
    
    return ![self isGroupTriggered] && [self isFeedbackCompatibleWithIOS];
}




- (BOOL)isFeedbackCompatibleWithIOS {
    
    return self.feedbackType == PacoFeedbackTypeStaticMessage
    || self.feedbackType == PacoFeedbackTypeRetrospective
    || self.feedbackType == PacoFeedbackTypeCustomCode;
}






@end
