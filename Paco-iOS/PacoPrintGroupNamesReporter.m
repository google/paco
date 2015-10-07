//
//  PacoPrintReporter.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/25/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoPrintGroupNamesReporter.h"
#import "PacoExerimentDidStartVerificationProtocol.h" 
#include "ExperimentDAO.h"
#import "NSObject+J2objcKVO.h"

@implementation PacoPrintGroupNameReporter

-(void)  notifyDidStart:(PAExperimentDAO*) experiment
{
    
   
                        
    if([experiment valueForKeyEx:@"groups"] && [[experiment valueForKeyPathEx:@"groups#"] intValue] >0)
    {
        NSNumber *numberOfGroups = [experiment valueForKeyPathEx:@"groups#"];
        int numGroups = [numberOfGroups intValue];
        
        
        for(int i=0; i < numGroups; i++)
        {
            NSString* titleStr = [NSString stringWithFormat:@"groups[%i].name",i];
            
            NSLog(@"Group Title:%@", [experiment valueForKeyPathEx:titleStr] );
            
        }
    }
    
    
}


@end
