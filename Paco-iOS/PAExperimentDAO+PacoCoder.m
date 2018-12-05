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

#import "PAExperimentDAO+PacoCoder.h"

#import  "PacoSerializer.h"
#import  "java/util/ArrayList.h"
#import  "PacoSerializeUtil.h"
#import  "ExperimentDAO.h"

#define JsonKey @"kjsonPrsistanceKey"

@implementation PAExperimentDAO (PacoCoder)

- (id)initWithCoder:(NSCoder *)decoder
{
    /* super does not support  initWithCoder so we don't try to invoke it */
    
        NSData* data = [decoder decodeObjectForKey:JsonKey];
        PacoSerializer* serializer =
        [[PacoSerializer alloc] initWithArrayOfClasses:nil
                              withNameOfClassAttribute:@"nameOfClass"];
        JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
        IOSObjectArray * iosArray = [resultArray toArray];
        PAExperimentDAO * experiment  =  [iosArray objectAtIndex:0];
        self =experiment;
    
    return self;
 
    
    
}


- (void) encodeWithCoder:(NSCoder *)encoder
{
    
    NSArray* array = [PacoSerializeUtil getClassNames];
    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:array withNameOfClassAttribute:@"nameOfClass"];
    NSData* json = [serializer toJSONobject:self];
    [encoder encodeObject:json  forKey:JsonKey];
}



- (id)copyWithZone:(NSZone *)zone {
    
    NSArray* array = [PacoSerializeUtil getClassNames];
    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:array withNameOfClassAttribute:@"nameOfClass"];
    NSData* json = [serializer toJSONobject:self];
    
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:json];
    IOSObjectArray * iosArray = [resultArray toArray];
    PAExperimentDAO  * experimentCopy =  [iosArray objectAtIndex:0];
    return experimentCopy;
    
}
@end
