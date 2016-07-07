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


typedef NS_ENUM(NSInteger, PacoFeedbackType) {
    PacoFeedbackTypeStaticMessage,
    PacoFeedbackTypeRetrospective,
    PacoFeedbackTypeResponsive,
    PacoFeedbackTypeCustomCode,
    PacoFeedbackTypeDisableMessage
};



-(int) feedbackType
{
    
  
    NSNumber* feedbackType   = [self valueForKeyEx:@"feedbackType"];
    int type = [feedbackType intValue];

    return type;
    
}

-(NSString*) jsonStringForJavascript
{

    NSString* feedback   = [self valueForKeyPathEx:@"feedback.text"];
    return feedback;
    
}



- (BOOL)isGroupIsTriggered{
    
    BOOL isGroupTriggered = NO;
    
    NSNumber*  numberOfActionTriggers =  [self   valueForKeyEx:@"actionTriggers#"];
    int actionTriggerCount = [numberOfActionTriggers intValue];
    
    
    for(int ii =0; ii < actionTriggerCount; ii++)
    {
        NSString* str = [NSString stringWithFormat: @"actionTriggers[%i]",ii ];
        PAScheduleTrigger  *trigger = [self  valueForKeyEx:str];
        
        
        
        
        
        
    }
    
    
    
     NSString* feedback   = [self valueForKeyPathEx:@"cues"];
 
   // return [[self.signalMechanismList firstObject] isKindOfClass:[PacoTriggerSignal class]];
}


- (BOOL)isFeedbackCompatibleWithIOS {
    
    return self.feedbackType == PacoFeedbackTypeStaticMessage
    || self.feedbackType == PacoFeedbackTypeRetrospective
    || self.feedbackType == PacoFeedbackTypeCustomCode;
}






@end
