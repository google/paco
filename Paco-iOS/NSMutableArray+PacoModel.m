//
//  NSArray+PacoModel.m
//  Paco
//
//  Created by northropo on 9/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "NSMutableArray+PacoModel.h"
#import "ExperimentDAO.h"
#import "NSObject+J2objcKVO.h"




@implementation NSMutableArray (PacoModel)


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

-(PAExperimentDAO*) removeExperiment:(NSString*) experimentId
{
 
     PAExperimentDAO* experiment = [self findExperiment:experimentId];
     [self  removeObject:experiment];
     return experiment;
    
}

-(BOOL) hasExperiment:(NSString*) experimentId;
{
    
   id found  =  [self findExperiment:experimentId];
    
    return ( found !=nil );
    
    
}

@end
