//
//  PAActionSpecification+PacoCoder.m
//  Paco
//
//  Created by northropo on 9/30/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PAActionSpecification+PacoCoder.h"
#import "PacoSerializer.h"
#import "java/util/ArrayList.h"
#import  "PacoSerializeUtil.h"



#define JsonKey @"kjsonPrsistanceKey"

@implementation PAActionSpecification (PacoCoder)

- (id)initWithCoder:(NSCoder *)decoder
{
    
    NSData* data = [decoder decodeObjectForKey:JsonKey];
    PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayOfClasses:nil
                          withNameOfClassAttribute:@"nameOfClass"];
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
    IOSObjectArray * iosArray = [resultArray toArray];
    PAActionSpecification * actonSpecification =  [iosArray objectAtIndex:0];
    self =actonSpecification;
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
