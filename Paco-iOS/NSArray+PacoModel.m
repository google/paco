//
//  NSArray+PacoModel.m
//  Paco
//
//  Created by northropo on 9/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "NSArray+PacoModel.h"
#import "ExperimentDAO.h"
#import "NSObject+J2objcKVO.h"




@implementation NSArray (PacoModel)


-(PAExperimentDAO*) findExperiment:(NSString*) experimentId
{
    PAExperimentDAO* retVal = nil;
    
    for(PAExperimentDAO* experiment  in self)
    {
        NSString* experimentStringId  =    [[experiment valueForKeyEx:@"id"] stringValue];
        
        if([experimentStringId isEqualToString:experimentId])
        
            retVal = experiment;
        break;
        
    }
    
    return retVal;
    
}

@end
