//
//  TestUtil.m
//  Paco
//
//  Created by Timo on 10/28/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "TestUtil.h"
#import "PacoSerializer.h" 
#import "ExperimentDAO.h"
#import "java/util/ArrayList.h"



@implementation TestUtil


#pragma mark - helper methods
+(PAExperimentDAO*) buildExperiment:(NSString*) json
{
    
    NSData* data =nil;
    data=  [json dataUsingEncoding:NSUTF8StringEncoding];
    
    PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayOfClasses:nil
                          withNameOfClassAttribute:@"nameOfClass"];
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
    IOSObjectArray * iosArray = [resultArray toArray];
    PAExperimentDAO * dao =  [iosArray objectAtIndex:0];
    return dao;
    
}
@end
