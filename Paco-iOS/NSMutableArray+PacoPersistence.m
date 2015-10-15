//
//  NSArray+PacoPersistence.m
//  Paco
//
//  Created by northropo on 10/14/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "NSMutableArray+PacoPersistence.h"
#import "ArrayStore.h" 



@implementation NSMutableArray (PacoPersistence)

-(void) store:(NSString*) key
{
    @synchronized(self)
    {
    
       ArrayStore  * store = [[ArrayStore alloc] init];
       [store updateOrInsert:key Array:self];
    }
    
}


-(void) refreshFromStore:(NSString*) key
{
    
   @synchronized(self)
    {
            [self removeAllObjects];
            ArrayStore  * store = [[ArrayStore alloc] init];
            NSArray * array = [store fetchArray:key];
            for(NSObject* o in array)
            {
                
                
                [self addObject:o];
            }
    
    }
}

@end
