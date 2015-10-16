//
//  PAActionSpecification+PacoIdentity.m
//  Paco
//
//  Created by northropo on 10/15/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PAActionSpecification+PacoIdentity.h"
#import "NSObject+J2objcKVO.h"



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
