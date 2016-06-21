//
//  NSArray+PacoEvent.m
//  Paco
//
//  Created by Northrop O'brien on 6/21/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "NSMutableArray+PacoEvent.h"

@implementation NSMutableArray (PacoEvent)


-(void) removeEvent:(NSDictionary*) event
{
    NSDictionary* removeDict;
    for(NSDictionary* dict in self )
    {
        if([dict[@"_guid"]isEqualToString:event[@"_guid"]])
        {
            removeDict = dict;
            break;
            
        }
    }
    
    if(removeDict != nil)
    {
     
        [self  removeObject:removeDict];
    }

    
    
}

@end
