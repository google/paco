//
//  PacoHasRelevantActionSpecifications.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/24/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoHasRelevantActionSpecifications.h"
#import "ValidatorConsts.h"
#include "ExperimentDAO.h"
#include "PacoMediator.h"



@implementation PacoHasRelevantActionSpecifications


-(ValidatorExecutionStatus) shouldStart:(PAExperimentDAO*) experiment Specifications:(NSArray*) specifications
{
    ValidatorExecutionStatus retVal = ValidatorExecutionStatusSuccess ;
    BOOL isLive =[[PacoMediator sharedInstance] isExperimentLive:experiment];
    
    if([specifications count] ==0 &&  !isLive)
    {
        retVal = ValidatorExecutionStatusFail & ValidatorExecutionStatusNoApplicableSpecifications;
    }
    
    return retVal;
}

@end
