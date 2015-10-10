//
//  TempStorage.h
//  Paco
//
//  Created by northropo on 10/8/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TempStorage : NSObject
@property (strong,nonatomic) NSMutableArray* listOfAvailableExperiments;

+ (TempStorage*)sharedInstance;
@end
