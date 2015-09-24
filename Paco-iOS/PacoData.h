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
@class PAExperimentDAO;



@interface PacoData : NSObject



@property (strong,nonatomic) PacoSignalStore * signalStore;
@property (strong,nonatomic) PacoEventStore * eventStore;


+ (PacoData*) sharedInstance;

-(NSArray*) runningExperiments;
-(void) clearRunningExperiments;
-(void) updateActionSpecifications:(NSArray*) newActionSpecifications;
-(BOOL) addExperimentToAvailableStore:(PAExperimentDAO*) experiment;



/* join & unjoin */
-(void) stopRiunningExperiment:(NSString*) experimentI;
-(void) startRunningExperiment:(NSString*) experimentIdId;

@end
