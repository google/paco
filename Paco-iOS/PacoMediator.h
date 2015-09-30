//
//  PacoMediator.h
//  Paco
//
//  Created by northropo on 9/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ValidatorConsts.h"
#import "PacoExerimentDidStartVerificationProtocol.h"
#import "PacoExerimentWillStartVerificationProtocol.h"
#import "PacoExperimentDidStopVerificatonProtocol.h"
#import "PacoExperimentWillStopVerificatonProtocol.h"
#import "PacoNotificationManager.h"


@class PacoSignalStore;


@class PacoEventStore;
@class PAExperimentDAO;



@interface PacoMediator : NSObject<PacoNotificationManagerDelegate>



@property (strong,nonatomic) PacoSignalStore * signalStore;
@property (strong,nonatomic) PacoEventStore * eventStore;


+ (PacoMediator*) sharedInstance;


-(void) clearRunningExperiments;
-(void) updateActionSpecifications:(NSArray*) newActionSpecifications RemoveAllNotifications:(BOOL) remveAll;
-(void) addExperimentToAvailableStore:(PAExperimentDAO*) experiment;
-(BOOL) isExperimentIdLive:(NSString*) experimentId;
-(BOOL) isExperimentLive:(PAExperimentDAO*) experiment;



/* join & unjoin */
-(ValidatorExecutionStatus) stopRunningExperiment:(NSString*) experimentId;
-(ValidatorExecutionStatus) startRunningExperiment:(NSString*) experimentIdId;

-(ValidatorExecutionStatus) startRunningExperimentRegenerate:(NSString*) experimentId;

/* registration methods*/
 
-(void) registerWillStartValidators:(NSArray*) validators;
-(void) registerDidStopNotifier:(id<PacoExperimentDidStopVerificatonProtocol>) notifier;
-(void) registerWillStopValidator:(id<PacoExperimentWillStopVerificatonProtocol>) notifier;
-(void) registerDidStartNotifiers:(NSArray*) notifiers;
-(void) registerDidStartNotifier:(id<PacoExerimentDidStartVerificationProtocol>) notifier;
-(void) registerWillStartValidator:(id<PacoExerimentDidStartVerificationProtocol>) validator;
@end
