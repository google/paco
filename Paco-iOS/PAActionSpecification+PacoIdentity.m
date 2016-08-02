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
#import "PAActionSpecification+PacoIdentity.h"
#import "NSObject+J2objcKVO.h"
#import "ActionSpecification.h" 



@implementation PAActionSpecification (PacoIdentity)


/*
 return a unique id for an object
 */
+(NSString*) uniqueId
{
    
    /* get experiment id */
    
    
    NSString * actionId =  [[self       valueForKeyEx:@"experiment_.id"] stringValue];
    NSString * groupId =  [[self        valueForKeyEx:@"experimentGroup_.name"] stringValue];
    NSString * triggerId =  [[self      valueForKeyEx:@"actionTrigger_.id"] stringValue];
    NSString * triggerSpecId =  [[self  valueForKeyEx:@"actionTriggerSpecId_"] stringValue];
    NSDate   * time  = [self  valueForKeyEx:@"time_"];
    NSString* idStr =  [NSString stringWithFormat:@"%@-%@-%@-%@-%@",actionId,groupId,triggerId,triggerSpecId , time ];
    return idStr;
}

@end
