//
//  NSArray+PacoModel.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/23/15.
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
        {
        
            retVal = experiment;
            break;
        }
      
        
    }
    
    return retVal;
    
}

/*
 
  optimize if time available.  Else probably ok because the number or experiemts removed will be the running
  experiments which will be 'small"
 
 */


-(void) removeExperiments:(NSArray*) experiments
{
    
    for(  PAExperimentDAO*  experiment in experiments)
    {
        NSString* experimentStringId  =    [[experiment  valueForKeyEx:@"id"] stringValue];
        [self removeExperiment:experimentStringId];
    }
    
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
