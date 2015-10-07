//
//  PacoWasAbleToSaveState.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/24/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoWasAbleToSaveState.h"
#import "ValidatorConsts.h"
#include "ExperimentDAO.h"


@implementation PacoWasAbleToSaveState




-(ValidatorExecutionStatus) shouldStart:(PAExperimentDAO*) experiment Specifications:(NSArray*) specifications
{
    
   
         ValidatorExecutionStatus retVal = ValidatorExecutionStatusSuccess ;
        
        
        // save experiemtn here
        
 
    
        return retVal;
   
 
    
}

@end
