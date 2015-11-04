//
//  PacoGenerateEventValidator.m
//  Paco
//
//  Created by northropo on 10/28/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoGenerateEventValidator.h"
#import "PacoEventExtended.h" 
#import "PAExperimentDAO+Helper.h"
#import "PacoEventExtended.h"
#import "PacoEventExtended+PacoCoder.h"


@implementation PacoGenerateEventValidator


-(ValidatorExecutionStatus) shouldStart:(PAExperimentDAO*) experiment Specifications:(NSArray*) specifications
{

    ValidatorExecutionStatus retVal = ValidatorExecutionStatusSuccess ;
    
    PacoEventExtended* event = [PacoEventExtended joinEventForActionSpecificatonWithServerExperimentId:experiment serverExperimentId:@"not applicable"];
    [event save];
    
    
    [[NSNotificationCenter defaultCenter] postNotificationName:@"JoinEvent"
                                                        object:nil];
    
    return retVal;
    
}

@end
