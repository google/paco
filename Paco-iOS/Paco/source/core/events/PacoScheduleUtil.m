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

#import "PacoScheduleUtil.h"
#import "ExperimentDAO.h" 
#import "NSObject+J2objcKVO.h"
#import "ExperimentGroup.h"
#import "ActionTrigger.h"
#import "SchedulePrinter.h"

@implementation PacoScheduleUtil


+(NSString*) buildScheduleString:(PAExperimentDAO*) experiment
{
    NSMutableString* mutableString  = [ NSMutableString new];
    
   
    if([experiment valueForKeyEx:@"groups"] && [[experiment valueForKeyEx:@"groups#"] intValue] >0)
    {
        int groupsCount  = [[experiment valueForKeyEx:@"groups#"] intValue];
        for(int i=0; i < groupsCount; i++)
        {
            
            NSString* groupStr = [NSString stringWithFormat:@"groups[%i]",i];
            PAExperimentGroup * group = [experiment valueForKeyEx:groupStr];
            
            if([group valueForKeyEx:@"actionTriggers"]  && [[group valueForKeyEx:@"actionTriggers#"] intValue] > 0)
            {
                int numberOfActionTriggers = [[group valueForKeyEx:@"actionTriggers#"] intValue];
                
                for(int i=0; i < numberOfActionTriggers; i++)
                {
                    NSString* actionTriggerString = [NSString stringWithFormat:@"actionTriggers[%i]",i];
                    PAActionTrigger * actionTrigger  = [group valueForKeyEx:actionTriggerString];
                    
                    if([actionTrigger valueForKeyEx:@"schedules"] && [[actionTrigger valueForKeyEx:@"schedules#"] intValue] > 0)
                    {
                        int numberOfSchedules = [[actionTrigger valueForKeyEx:@"schedules#"] intValue];
                        for(int i =0; i < numberOfSchedules; i ++)
                        {
                            
                            NSString* actionString = [NSString stringWithFormat:@"schedules[%i]",i];
                            PASchedule  * schedule    = [actionTrigger valueForKeyEx:actionString];
                            [mutableString appendString:[PASchedulePrinter toStringWithPASchedule:schedule]];
                        }
  
                    }
                }
                
                
            }
            
        }
    }
    
    
    return mutableString;
    
    
    
}

@end
