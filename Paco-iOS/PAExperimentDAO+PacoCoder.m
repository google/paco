//
//  PAExperimentDAO+PacoCoder.m
//  Paco
//
//  Created by northropo on 9/30/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PAExperimentDAO+PacoCoder.h"

#import "PacoSerializer.h"
#import "java/util/ArrayList.h"
#import  "PacoSerializeUtil.h"
#import "ExperimentDAO.h"

#define JsonKey @"kjsonPrsistanceKey"

@implementation PAExperimentDAO (PacoCoder)

- (id)initWithCoder:(NSCoder *)decoder
{
    
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
@end
