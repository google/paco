//
//  PAActionTrigger+PacoHelper.m
//  Paco
//
//  Created by Northrop O'brien on 7/7/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "PAActionTrigger+PacoHelper.h"
#import "NSObject+J2objcKVO.h" 
#include "java/util/ArrayList.h"
#include "java/util/Iterator.h"

@implementation PAActionTrigger (PacoHelper)


-(NSArray*) actionStringTypes
{
     NSMutableArray *  returnTypes = [NSMutableArray new];
     JavaUtilArrayList*   actions =   [self valueForKeyEx:@"actions"];
     id<JavaUtilIterator> iter = actions.iterator;
    
     while([iter hasNext])
     {
        NSObject*  obj =  [iter next];
        NSString* type = [obj valueForKeyEx:@"type"];
        [returnTypes addObject:type];
        
    }
    
    return returnTypes;
   
}


-(BOOL) containsAllOthers
{
    NSArray * types = [self actionStringTypes];
    BOOL containsType= NO;
    
    if([types containsObject:@"pacoActionAllOthers"])
    {
        containsType = YES;
    }
    return containsType;
    
    
}


@end
