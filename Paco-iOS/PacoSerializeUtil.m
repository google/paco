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

#import "PacoSerializeUtil.h"
#import <Foundation/Foundation.h>
#import <objc/runtime.h>
#include "ExperimentDAO.h"
#include "PacoSerializer.h"
#include "Schedule.h"
#include "java/lang/Boolean.h"
#include "java/lang/IllegalStateException.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/Collections.h"
#include "java/util/List.h"
#include "java/util/List.h"
#include "org/joda/time/Hours.h"
#include "org/joda/time/Duration.h"
#import "NSObject+J2objcKVO.h"





#define SEARCHED_CLASS_PREFIX @"PA"




@interface PacoSerializeUtil ()
@property(strong, nonatomic) NSArray* classes;
@end

@implementation PacoSerializeUtil


- (instancetype)init
{
    self = [super init];
    if (self) {
        
        _classes = [PacoSerializeUtil getClassNames];
        
    }
    return self;
}

/*
   // list all classes that have SEARCHED_CLASS_PREFIX prefix. 

*/
+ (NSArray*)getClassNames
{
    
     NSMutableArray* mutableArray = [NSMutableArray new];
    int count = objc_getClassList(NULL, 0);
    Class buffer[count];
    objc_getClassList(buffer, count);
  
    for(int i = 0; i < count; i++)
    {
        Class c = buffer[i];
        NSString* className =  NSStringFromClass(c);
        if(  [className hasPrefix:@"PA"] )
        {
            [mutableArray addObject:className];
            
        }
   
    }
    return mutableArray;
    
}


+(PASchedule*) getScheduleAtIndex:(PAExperimentDAO *)  experiment   GroupIndex:(int) groupIndex actionTriggerIndex:(int) actionTriggerIndex  scheduleIndex:(int) scheduleIndex
{

      NSString* path = [NSString stringWithFormat:@"groups[%i].actionTriggers[%i].schedules[%i]",groupIndex, actionTriggerIndex, scheduleIndex];
      PASchedule  * oo = [experiment  valueForKeyPathEx:path];
      return oo;
}

+ (NSString*) jsonFromDefinition:(PAExperimentDAO*) description
{
    
    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayWithClassAttributeName:@"nameOfClass"];
    NSData * data = [serializer toJSONobject:description];
    NSString* string =
    [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    
    return string;
    
    
}



+ (NSString*) jsonFromSchedule:(PASchedule*) schedule
{
    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayWithClassAttributeName:@"nameOfClass"];
    NSData * data = [serializer toJSONobject:schedule];
    NSString* string =
    [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    
    return string;
    
}



@end
