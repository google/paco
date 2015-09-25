//
//  PacoMediator.h
//  Paco
//
//  Created by northropo on 9/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ValidatorConsts.h"

@class PacoSignalStore;
@class PacoEventStore;
@class PAExperimentDAO;



@interface PacoMediator : NSObject



@property (strong,nonatomic) PacoSignalStore * signalStore;
@property (strong,nonatomic) PacoEventStore * eventStore;


+ (PacoMediator*) sharedInstance;

-(NSArray*) runningExperiments;
-(void) clearRunningExperiments;
-(void) updateActionSpecifications:(NSArray*) newActionSpecifications;
-(BOOL) addExperimentToAvailableStore:(PAExperimentDAO*) experiment;


/* join & unjoin */
-(ValidatorExecutionStatus) stopRiunningExperiment:(NSString*) experimentI;
-(ValidatorExecutionStatus) startRunningExperiment:(NSString*) experimentIdId;

@end
