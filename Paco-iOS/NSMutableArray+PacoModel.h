//
//  NSArray+PacoModel.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
@class PAExperimentDAO;


@interface NSMutableArray (PacoModel)

-(PAExperimentDAO*) findExperiment:(NSString*) experimentId;
-(BOOL) hasExperiment:(NSString*) experimentId;
-(PAExperimentDAO*) removeExperiment:(NSString*) experimentId;
 


@end
