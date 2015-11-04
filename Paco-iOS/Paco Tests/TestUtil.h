//
//  TestUtil.h
//  Paco
//
//  Created by northropo on 10/28/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>


@class PAExperimentDAO;

@interface TestUtil : NSObject

+ (PAExperimentDAO*) buildExperiment:(NSString*) json;


@end
