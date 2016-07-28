//
//  PacoIsSelfReport.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/24/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoIsSelfReport.h"
#import "ValidatorConsts.h"
#include "ExperimentDAO.h"
#import "NSObject+J2objcKVO.h"
#import "ExperimentGroup.h" 



@implementation PacoIsSelfReport


-(ValidatorExecutionStatus) shouldStart:(PAExperimentDAO*) experiment Specifications:(NSArray*) specifications
{
    
    ValidatorExecutionStatus retVal = ValidatorExecutionStatusSuccess;
    if([self isSelfReport:experiment])
    {
        retVal= ValidatorExecutionStatusFail & ValidatorExecutionStatusIsSelfReport;
    }
    
    return retVal;

}


-(BOOL) isSelfReport:(PAExperimentDAO*) experiment
{
    BOOL retVal = YES;
    
 
    
    if([experiment valueForKeyEx:@"groups"] && [[experiment valueForKeyEx:@"groups#"] intValue] >0)
    {
        NSNumber *numberOfGroups = [experiment valueForKeyEx:@"groups#"];
        int numGroups = [numberOfGroups intValue];
        
        
        
            for(int i=0; i < numGroups; i++)
            {
                NSString* groupStr = [NSString stringWithFormat:@"groups[%i]",i];
                PAExperimentGroup * group = [experiment valueForKeyEx:groupStr];
                if([group valueForKeyEx:@"actionTriggers"]  && [[group valueForKeyEx:@"actionTriggers#"] intValue] > 0)
                {
                    retVal = NO;
                    break;
                }
            }
    }
    return retVal;
    
}

@end
