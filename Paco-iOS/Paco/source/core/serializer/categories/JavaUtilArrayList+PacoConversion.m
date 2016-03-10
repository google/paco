//
//  JavaUtilArrayList+PacoConversion.m
//  Paco
//
//  Created by Timo on 10/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "JavaUtilArrayList+PacoConversion.h"

@implementation JavaUtilArrayList (PacoConversion)



-(NSArray*) toNSArray
{
    
    NSMutableArray* mutableArray = [[NSMutableArray alloc] init];
    
    for(NSObject* o in self)
    {
        [mutableArray addObject:[o copy]];
    }
    return mutableArray;
    
}
@end
