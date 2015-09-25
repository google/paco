//
//  PacoExperimentIsNotNil.m
//  Paco
//
//  Created by northropo on 9/24/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoExperimentIsNotNil.h"
#import "ValidatorConsts.h" 
#include "ExperimentDAO.h"


@implementation PacoExperimentIsNotNil



-(ValidatorExecutionStatus) shouldStart:(PAExperimentDAO*) experiment Specifications:(NSArray*) specifications
{
    
    
    ValidatorExecutionStatus retVal = ValidatorExecutionStatusSuccess;
    if(experiment ==nil)
    {
        retVal= ValidatorExecutionStatusFail & ValidatorExecutionStatusExperimentNil;
    }
    
    return retVal;
    
    
    
}
@end
