//
//  PacoEventExtended+PacoCoder.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 10/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoEventExtended+PacoCoder.h"
#import  "PacoSerializer.h"
#import  "java/util/ArrayList.h"
#import  "PacoSerializeUtil.h"
#import  "PacoEventExtended.h"
#import  "PacoEventPersistenceHelper.h"



#define JsonKey @"kjsonPrsistanceKey/ForPacoEvent"

@implementation PacoEventExtended (PacoCoder)

- (id)initWithCoder:(NSCoder *)decoder
{
    
    /* super does not support  initWithCoder so we don't try to invoke it */
    
    NSData* data = [decoder decodeObjectForKey:JsonKey];
    PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayOfClasses:nil
                          withNameOfClassAttribute:@"nameOfClass"];
    
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
    
    IOSObjectArray * iosArray = [resultArray toArray];
    PacoEventExtended * event  =  [iosArray objectAtIndex:0];
    self =event;
    
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
    
    NSString* str =  [[NSString alloc] initWithData:json encoding:NSUTF8StringEncoding];
    
    NSLog(@"%@",str );
    
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:json];
    IOSObjectArray * iosArray = [resultArray toArray];
    PacoEventExtended  * event =  [iosArray objectAtIndex:0];
    return event;
    
}


-(void) save
{
    
    PacoEventPersistenceHelper * persistanceHelper = [PacoEventPersistenceHelper new];
    [persistanceHelper insertEventWithPAEventInterface:self];
    
}

@end
