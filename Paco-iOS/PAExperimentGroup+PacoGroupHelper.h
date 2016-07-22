//
//  PAExperimentGroup+PacoGroupHelper.h
//  Paco
//
//  Created by Northrop O'brien on 7/6/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "ExperimentGroup.h"

@class PAInput2;


typedef NS_ENUM(NSInteger, PacoFeedbackType) {
    PacoFeedbackTypeStaticMessage,
    PacoFeedbackTypeRetrospective,
    PacoFeedbackTypeResponsive,
    PacoFeedbackTypeCustomCode,
    PacoFeedbackTypeDisableMessage
};


@interface PAExperimentGroup (PacoGroupHelper)
/* get the feedback type */ 
-(PacoFeedbackType) feedbackType;
-(NSString*) jsonStringForJavascript;
-(NSArray* ) allInputs;
-(PAInput2*) inputWithId:(NSString*) inputID;


@end
