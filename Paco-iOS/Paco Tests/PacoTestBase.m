//
//  PacoTestBase.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/25/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//


#import <XCTest/XCTest.h>
#import <XCTest/XCTest.h>
#import "ExperimentDAO.h"
#import "PacoNotificationConstants.h"
#import "PacoSerializer.h"
#import "java/util/ArrayList.h"
#import "ExperimentDAO.h"
#import "DateTime.h"
#include "EsmSignalStore.h"
#include "EventStore.h"
#import "ActionSpecification.h"
#import "ActionScheduleGenerator.h"
#import "PacoEventStore.h"
#import "PacoSignalStore.h"
#import "java/lang/Long.h"
#import "NSObject+J2objcKVO.h"
#import "NSDate+Paco.h"
#import "OrgJodaTimeDateTime+PacoDateHelper.h"
#import "PacoNotificationConstants.h"
#import "PacoExtendedNotificationInfo.h"
#import "UILocalNotification+PacoExteded.h"
#import "PacoMediator.h"
#import "PacoScheduler.h"
#import "ActionSpecification.h"
#import "PacoDateUtility.h"
#import "PacoMediator.h"
#import "PAExperimentDAO+Helper.h"
#import "PacoSchedulingUtil.h"

@interface PacoTestBase  : XCTestCase
-(PAExperimentDAO*) buildExpeiment:(NSString*) json;
@end

@implementation PacoTestBase




-(PAExperimentDAO*) buildExpeiment:(NSString*) json
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





/*
 
 return a unique id for an object
 */
-(NSValue*) uniqueId:(NSObject*) actionSpecification
{
    return [NSValue valueWithPointer:(__bridge const void *)(actionSpecification)];
}


@end
