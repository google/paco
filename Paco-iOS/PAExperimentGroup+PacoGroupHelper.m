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


@implementation PAExperimentGroup (PacoGroupHelper)






-(PacoFeedbackType) feedbackType
{
    NSNumber* feedbackType   = [self valueForKeyEx:@"feedbackType"];
    PacoFeedbackType type = [feedbackType intValue];

    return type;
    
}

-(NSString*) jsonStringForJavascript
{
    NSString* feedback   = [self valueForKeyPathEx:@"feedback.text"];
    return feedback;
}



- (BOOL)isGroupTriggered
{
    
    BOOL isGroupTriggered = NO;
    NSNumber*  numberOfActionTriggers =  [self   valueForKeyEx:@"actionTriggers#"];
    int actionTriggerCount = [numberOfActionTriggers intValue];

    for(int ii =0; ii < actionTriggerCount; ii++)
    {
        NSString* str = [NSString stringWithFormat: @"actionTriggers[%i]",ii ];
        NSObject   *trigger = [self  valueForKeyEx:str];
        if( ![trigger isKindOfClass:[PAScheduleTrigger class]])
        {
            isGroupTriggered =  YES;
            break;
        }
        
    }
    
    if(!isGroupTriggered)
    {
        NSObject * cues    = [self valueForKeyPathEx:@"cues"];
        
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
