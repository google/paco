//
//  PacoData.h
//  Paco
//
//  Created by northropo on 9/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>


@class PacoSignalStore;
@class PacoEventStore;


@interface PacoData : NSObject

@property (strong,nonatomic) NSMutableArray* allExperiments;
@property (strong,nonatomic) NSMutableArray* runningExperiments;
@property (strong,nonatomic) NSMutableArray* actionSpecifications;
@property (strong,nonatomic) NSMutableArray* oldActionSpecifications;

@property (strong,nonatomic) PacoSignalStore * signalStore;
@property (strong,nonatomic) PacoEventStore * eventStore;

+ (PacoData*)sharedInstance;


@end
