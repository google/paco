//
//  TempStorage.m
//  Paco
//
//  Created by Tim O'Brien on 10/8/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "TempStorage.h"
#import "PacoMediator.h"


@implementation TempStorage



- (instancetype)init
{
    self = [super init];
    if (self) {
        
        _listOfAvailableExperiments= [NSMutableArray new];
        
    }
    return self;
}

+ (TempStorage*)sharedInstance
{
    static dispatch_once_t once;
    static TempStorage *sharedInstance;
    dispatch_once(&once, ^ {
        
               sharedInstance = [[self alloc] init];
        
    });
    
    return sharedInstance;
}

@end
