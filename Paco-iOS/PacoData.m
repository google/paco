//
//  PacoData.m
//  Paco
//
//  Created by northropo on 9/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoData.h"
#import "PacoSignalStore.h"
#import "PacoEventStore.h"


@implementation PacoData


- (instancetype)init
{
    self = [super init];
    
    if (self) {
        
        self.allExperiments         = [[NSMutableArray alloc] init];
        self.runningExperiments     = [[NSMutableArray alloc] init];
        self.actionSpecifications   = [[NSMutableArray alloc] init];
        self.signalStore            = [[PacoSignalStore alloc] init];
        self.eventStore             = [[PacoEventStore alloc] init];
    }
    return self;
}


+ (PacoData*)sharedInstance
{
    static dispatch_once_t once;
    static PacoData *sharedInstance;
    dispatch_once(&once, ^ { sharedInstance = [[self alloc] init]; });
    return sharedInstance;
}

@end
