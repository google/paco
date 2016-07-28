//
//  PacoWillStopExperiment.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/29/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import  "PacoWillStopManageActionSpecifications.h"
#include "ExperimentDAO.h"
#import  "ValidatorConsts.h"
 

@implementation PacoWillStopManageActionSpecifications


 -(ValidatorExecutionStatus) shouldStop:(PAExperimentDAO*) experiment
{
    
    return ValidatorExecutionStatusSuccess;
    
}


@end
