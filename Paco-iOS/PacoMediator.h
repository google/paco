//
//  PacoMediator.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/10/15.
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
@class PacoEventManagerExtended;
 


@interface PacoMediator : NSObject<PacoNotificationManagerDelegate>


@property (strong,readonly )  PacoNotificationManager* notificationManager;
@property (strong,nonatomic) PacoSignalStore * signalStore;
@property (strong,nonatomic) PacoEventStore * eventStore;
@property (strong,nonatomic) PacoEventManagerExtended * eventManager;

+ (PacoMediator*) sharedInstance;


-(NSMutableArray*) experiments;
-(NSMutableArray*) startedExperiments;


-(void) clearRunningExperiments;
-(void) updateActionSpecifications:(NSArray*) newActionSpecifications RemoveAllNotifications:(BOOL) remveAll;
-(void) addExperimentToAvailableStore:(PAExperimentDAO*) experiment;
-(BOOL) isExperimentIdLive:(NSString*) experimentId;
-(BOOL) isExperimentLive:(PAExperimentDAO*) experiment;
-(void) replaceAllExperiments:(NSArray*) experiments;



-(NSMutableArray*) hubExperiments;
-(void) setHudExperiments:(NSMutableArray*) newArray;

- (void)submitSurveyWithDefinition:(PAExperimentDAO*) definition
                      surveyInputs:(NSArray*)surveyInputs
                      notification:(UILocalNotification*)notification;
 



/* join & unjoin */
-(ValidatorExecutionStatus) stopRunningExperiment:(NSString*) experimentId;
-(ValidatorExecutionStatus) startRunningExperiment:(NSString*) experimentIdId;

-(ValidatorExecutionStatus) startRunningExperimentRegenerate:(NSString*) experimentId;
-(ValidatorExecutionStatus) stopRunningExperimentRegenerate:(NSString*) experimentId;



/* registration methods*/
-(void) registerWillStartValidators:(NSArray*) validators;
-(void) registerDidStopNotifier:(id<PacoExperimentDidStopVerificatonProtocol>) notifier;
-(void) registerWillStopValidator:(id<PacoExperimentWillStopVerificatonProtocol>) notifier;
-(void) registerDidStartNotifiers:(NSArray*) notifiers;
-(void) registerDidStartNotifier:(id<PacoExerimentDidStartVerificationProtocol>) notifier;
-(void) registerWillStartValidator:(id<PacoExerimentDidStartVerificationProtocol>) validator;


/* state management methods */
-(void) refreshRunningExperiments;
-(void) cleanup;



@end
