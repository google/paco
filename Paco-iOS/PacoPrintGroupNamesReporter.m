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
