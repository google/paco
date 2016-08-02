/* Copyright 2015  Google
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

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
@class PAExperimentDAO;


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

- (PacoEventManagerExtended*) fetchEventManager:(NSString*) str;

-(NSMutableArray*) hubExperiments;
-(void) setHudExperiments:(NSMutableArray*) newArray;

- (void)submitSurveyWithDefinition:(PAExperimentDAO*) definition
                      surveyInputs:(NSArray*)surveyInputs
                      notification:(UILocalNotification*)notification;

/* return experiment object for the id */
-(PAExperimentDAO *) experimentForId:(NSString*) experimentId;


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
-(void) clearRunningExperimentsSynchronous;
-(void) cleanExpiredNotifications;




@end
