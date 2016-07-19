//
//  JavaUtilArrayList+PacoConversion.m
//  Paco
//
//  Created by Timo on 10/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "JavaUtilArrayList+PacoConversion.h"

@implementation JavaUtilArrayList (PacoConversion)



+ (instancetype) arrayListWithValues:(NSArray*) arrayOfValues
{
    
    JavaUtilArrayList * arrayList = [[JavaUtilArrayList alloc] init];
    [arrayList addArrayElements:arrayOfValues];
    return arrayList;

}

-(NSArray*) toNSArrayCopy
{
    
    NSMutableArray* mutableArray = [[NSMutableArray alloc] init];
    
    for(NSObject* o in self)
    {
        [mutableArray addObject:[o copy]];
    }
    return mutableArray;
    
}


-(NSArray*) toNSArray
{
    
    NSMutableArray* mutableArray = [[NSMutableArray alloc] init];
    
    for(NSObject* o in self)
    {
        [mutableArray addObject:o];
    }
    return mutableArray;
    
}



-(void) addArrayElements:(NSArray*) array
{
   
    for(id ob in array)
    {
         [self addWithId:ob];
    }
   
}



@end
