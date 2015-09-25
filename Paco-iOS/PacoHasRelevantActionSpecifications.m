//
//  PacoHasRelevantActionSpecifications.m
//  Paco
//
//  Created by northropo on 9/24/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoHasRelevantActionSpecifications.h"
#import "ValidatorConsts.h"
#include "ExperimentDAO.h"

@implementation PacoHasRelevantActionSpecifications


-(ValidatorExecutionStatus) shouldStart:(PAExperimentDAO*) experiment Specifications:(NSArray*) specifications
{
    ValidatorExecutionStatus retVal = ValidatorExecutionStatusSuccess ;
    if([specifications count] ==0)
    {
        retVal = ValidatorExecutionStatusFail & ValidatorExecutionStatusNoApplicableSpecifications;
    }
    
    return retVal;
}

@end
