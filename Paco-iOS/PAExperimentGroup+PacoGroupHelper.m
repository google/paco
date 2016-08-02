/* Copyright 2015  Google
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
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
