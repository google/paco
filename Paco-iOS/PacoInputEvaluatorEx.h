//
//  PacoInputEvaluatorEx.h
//  Paco
//
//  Created by Northrop O'brien on 4/15/16.
//  Copyright © 2016 Paco. All rights reserved.
//




#import <Foundation/Foundation.h>
@class PacoExperiment;
@class PacoExperimentInput;

/*
 NOTE: The evaluation of conditions assumes:
 - The conditianl relationship is linear, the dependency input should always be in the position
 before other questions it decides
 **/

@interface PacoInputEvaluatorEx : NSObject
@property(nonatomic, strong, readonly) PacoExperiment* experiment;
@property(nonatomic, strong, readonly) NSArray* visibleInputs;

+ (PacoInputEvaluatorEx*)evaluatorWithExperiment:(PacoExperiment*)experiment;
- (NSError*)validateVisibleInputs;
- (NSArray*)evaluateAllInputs;


@end
