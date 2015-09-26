//
//  NSArray+PacoModel.h
//  Paco
//
//  Created by northropo on 9/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
@class PAExperimentDAO;


@interface NSArray (PacoModel)

-(PAExperimentDAO*) findExperiment:(NSString*) experimentId;
-(BOOL) hasExperiment:(NSString*) experimentId;


@end
