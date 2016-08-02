/* Copyright 2015  Google
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

#import  "NSMutableArray+PacoModel.h"
#import  "ExperimentDAO.h"
#import  "NSObject+J2objcKVO.h"
#import  "PAExperimentDAO+Helper.h"



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



-(void) addUniqueExperiemnt:(PAExperimentDAO*) experiment
{
    
    if([self hasExperiment:[experiment instanceId]])
    {
        [self removeExperiment:[experiment  instanceId]];
  
    }
    
    [self addObject:experiment];
    
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
