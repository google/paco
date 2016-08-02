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

#import "PacoMediator.h"
#import "PacoSignalStore.h"
#import "PacoEventStore.h"
#import "ExperimentDAO.h"
#import "PAExperimentDAO+Helper.h" 
#import "PacoExerimentDidStartVerificationProtocol.h"
#import "PacoExerimentWillStartVerificationProtocol.h"
#import "PacoExperimentDidStopVerificatonProtocol.h"
#import "PacoExperimentWillStopVerificatonProtocol.h"
#import "ValidatorConsts.h"
#import "UILocalNotification+PacoExteded.h"
#import "PacoNotificationManager.h"
#import "PacoExperimentWillBeModifiedProtocol.h" 
#import "PacoExperimentHasBeenModified.h" 
#import "NSMutableArray+PacoPersistence.h"
#import "NSMutableArray+PacoPersistence.h"
#import "PacoSchedulingUtil.h"
#import "PacoGenerateEventValidator.h"
#import "UILocalNotification+PacoExteded.h"
#import "PacoEventManagerExtended.h"
#import "PacoPublicDefinitionLoader.h"
#import "PAExperimentDAO+Helper.h"
#import "PacoSerializer.h"
#import "PacoSerializeUtil.h"
#import "NSMutaBleArray+PacoModel.h"
#import "PacoEventExtended.h"
#import  "NSMutableArray+PacoModel.h"
#import "PacoNetwork.h" 
#import "PacoAuthenticator.h" 
#import "PacoEventManagerExtended.h"
#import "PacoNetwork.h" 


#define KEY_RUNNING_EXERIMENTS @"running_experiments"




@interface PacoMediator ()


@property (strong,nonatomic)   PacoPublicDefinitionLoader * definitionsLoader;
@property (strong,nonatomic )  PacoNotificationManager* notificationManager;
@property (strong,nonatomic ) NSMutableArray*  hubExperiments;
@property (strong,nonatomic ) NSMutableArray* allExperiments;
@property (strong,nonatomic)   NSMutableArray* runningExperiments;
@property (strong,nonatomic)  NSMutableArray* actionSpecifications;
@property (strong,nonatomic ) NSMutableArray* oldActionSpecifications;
@property (strong,nonatomic)  NSMutableDictionary * actionDefinitionsDictionary;

/* verifitcation protocols */
@property (strong,nonatomic ) NSMutableArray*  willStartVerifiers;
@property (strong,nonatomic ) NSMutableArray*  didStartNotifiers;

@property (strong,nonatomic ) NSMutableArray*  willStopVerifiers;
@property (strong,nonatomic ) NSMutableArray*  didStopNotifiers;

@property (strong,nonatomic ) NSMutableArray*  willModifyVerifiers;
@property (strong,nonatomic ) NSMutableArray*  didModifyNotifiers;
@property(nonatomic, strong) id<PacoEnumerator> publicExperimentIterator;

@end
 
static dispatch_queue_t serialQueue;
static dispatch_group_t group;

@implementation PacoMediator



- (instancetype)init
{
    self = [super init];
    
    if (self) {
        
        self.hubExperiments               = [[NSMutableArray alloc] init];
        self.allExperiments               = [[NSMutableArray alloc] init];
        self.runningExperiments           = [[NSMutableArray alloc] init];
        self.actionSpecifications         = [[NSMutableArray alloc] init];
        self.willStartVerifiers           = [[NSMutableArray alloc] init];
        self.didStartNotifiers            = [[NSMutableArray alloc] init];
        self.willStopVerifiers            = [[NSMutableArray alloc] init];
        self.didStopNotifiers             = [[NSMutableArray alloc] init];
        self.signalStore                  = [[PacoSignalStore alloc] init];
        self.eventStore                   = [[PacoEventStore alloc] init];
        self.actionDefinitionsDictionary  = [NSMutableDictionary new];
        self.notificationManager =   [PacoNotificationManager managerWithDelegate:self firstLaunchFlag:NO];
        self.eventManager  = [PacoEventManagerExtended defaultManager];
        
        PacoGenerateEventValidator* verifyer =  [PacoGenerateEventValidator new];
        [self.willStartVerifiers addObject:verifyer];
        
        _publicExperimentIterator =  [PacoPublicDefinitionLoader  publicExperimentsEnumerator];
        
        
      

         [self refreshRunningExperiments];
        
         [NSTimer scheduledTimerWithTimeInterval:5.0
                                          target:self
                                    selector:@selector(refreshNotifications:)
                                        userInfo:nil
                                         repeats:YES];
        
    }
    return self;
}


-(IBAction) refreshNotifications:(id) object
{
    
    [self cleanExpiredNotifications];
    
}






/* 
 
  PacoMediator is a singleton instnace and should only use sharedInstance
  to create/fetch  and instance
 
 */
+ (PacoMediator*)sharedInstance
{
    static dispatch_once_t once;
    static PacoMediator *sharedInstance;
    dispatch_once(&once, ^ {
        
        serialQueue = dispatch_queue_create("org.paco.SerialQueue", NULL);
        sharedInstance = [[self alloc] init];
        
    });
    
    return sharedInstance;
}


 


/*
     fetch all the experiments that are currently running
 */


-(NSMutableArray*) startedExperiments
{
    return   _runningExperiments ;
}


 

/*
   return all running experiments.
 
 */
-(NSMutableArray*) experiments
{
    NSMutableArray*  returnedExperiments = [[NSMutableArray alloc] initWithArray:_allExperiments copyItems:NO];
    [returnedExperiments  removeExperiments:_runningExperiments];
    return   returnedExperiments ;
}



-(void) setHudExperiments:(NSMutableArray*) newArray
{
    _hubExperiments    = newArray;
    
    
}

-(NSMutableArray*) hubExperiments
{
    
    
    return _hubExperiments;
    
    
}


/*
    need to recalculate  experiments
 */
-(void)  recalculateExperiments:(BOOL) shouldCancelAllExperiments
{
    dispatch_async(serialQueue, ^{
    
        [PacoSchedulingUtil    updateNotifications:self.runningExperiments ActionSpecificationsDictionary:self.actionDefinitionsDictionary ShouldCancelAllNotifications:shouldCancelAllExperiments];
    });
}

/*
      modify the experiments with id
 
 */
-(ValidatorExecutionStatus) modifyExperimentRegenerate:(NSString*) experimentId
{
    
    ValidatorExecutionStatus runStatus = ValidatorExecutionStatusFail;
    
    /* locate the experiment */
    PAExperimentDAO * experiment  = [self.allExperiments findExperiment:experimentId];
    
    if(!experiment)
    {
        experiment = [self.hubExperiments findExperiment:experimentId];
        
        
    }
    if(experiment)
    {
       
        
        /* buld the actionSpecifications for this one experiment, this will help us  perform validation on the experiment*/
        NSArray* array = [PacoSchedulingUtil buildActionSpecifications:@[experiment]  IsDryRun:YES ActionSpecificationsDictionary:self.actionDefinitionsDictionary] ;
        
        runStatus = [self willModifyRunningExperiment:experiment  Specificatons:array];
        if( runStatus & ValidatorExecutionStatusSuccess )
        {
            
            
            [self.runningExperiments addUniqueExperiemnt:experiment];
            [self recalculateExperiments:YES];
            /* synchronous*/
            [self didModifyExperiment:experiment];
            
        }
    }
    
    
    
    return runStatus;
    
    
    
}


- (PacoEventManagerExtended*) fetchEventManager:(NSString*) str;
{
    return _eventManager;
    
    
}

-(PAExperimentDAO *) experimentForId:(NSString*) experimentId
{
    
    PAExperimentDAO * experiment  = [self.allExperiments findExperiment:experimentId];
    return experiment;
    
}



/*
 
calculate the action specifications and reset the based upon the most recent version
 
*/
-(ValidatorExecutionStatus) startRunningExperimentRegenerate:(NSString*) experimentId
{
     ValidatorExecutionStatus runStatus = ValidatorExecutionStatusFail;
    [self cleanExpiredNotifications];
    
    /* locate the experiment */
    PAExperimentDAO * experiment  = [self.allExperiments findExperiment:experimentId];
    if(experiment==nil)
    {
        
        // need to refactor to only get class names onece and use appropreate types. 
         NSDictionary * dao  = (NSDictionary*) [self.hubExperiments findExperiment:experimentId];
        
        NSArray* classNames = [PacoSerializeUtil getClassNames];
        PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:classNames withNameOfClassAttribute:@"nameOfClass"];
        
     
            experiment  = (PAExperimentDAO*)  [serializer buildModelObject:dao];
      
        
        
        
    }
    
    NSLog(@"%@",experiment);
    if(experiment)
    {
        
        /* buld the actionSpecifications for this one experiment, this will help us  perform validation on the experiment*/
        NSArray* array = [PacoSchedulingUtil buildActionSpecifications:@[experiment]  IsDryRun:YES ActionSpecificationsDictionary:self.actionDefinitionsDictionary] ;
        
        runStatus = [self willStartRunningExperiment:experiment  Specificatons:array];
        if( runStatus & ValidatorExecutionStatusSuccess )
        {
      
            [self.runningExperiments addUniqueExperiemnt:experiment];
            [self recalculateExperiments:YES];
            /* synchronous*/
            [self didStartStartRunningExperiment:experiment];
        }
    }
    
    
    
    return runStatus;
}


/*
    calculate the action specifications and reset the based upon the most recent experiment added
 */
-(ValidatorExecutionStatus) startRunningExperiment:(NSString*) experimentId
{
    ValidatorExecutionStatus runStatus = ValidatorExecutionStatusFail;
    [self cleanExpiredNotifications];
    
                /* locate the experiment */
                PAExperimentDAO * experiment  = [self.allExperiments findExperiment:experimentId];
               if( experiment == nil)
               {
                   NSDictionary * dao = (NSDictionary*)   [self.hubExperiments findExperiment:experimentId];
                   NSArray* classNames = [PacoSerializeUtil getClassNames];
                   PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:classNames withNameOfClassAttribute:@"nameOfClass"];
                   
                   
                   experiment  = (PAExperimentDAO*)  [serializer buildModelObject:dao];
                   
                  
                   
               }
    
    
                 if(experiment)
                 {
                     
                     /* buld the actionSpecifications for this one experiment, this will help us  perform validation on the experiment*/
                     NSArray* array = [PacoSchedulingUtil buildActionSpecifications:@[experiment]  IsDryRun:YES ActionSpecificationsDictionary:self.actionDefinitionsDictionary] ;
                     runStatus = [self willStartRunningExperiment:experiment  Specificatons:array];
                     if( runStatus & ValidatorExecutionStatusSuccess )
                     {
                         
                           dispatch_async(serialQueue, ^{
                               
                          /* add the experiment new experiment to the list of running experiments */
                          [_runningExperiments addObject:experiment];
                               
                         
                         
                               
                               
                            /* now lets get all action specifications accross all experiments */
                            NSArray* actionSpecifications  = [PacoSchedulingUtil buildActionSpecifications:self.runningExperiments
                              IsDryRun:NO ActionSpecificationsDictionary:[PacoMediator sharedInstance].actionDefinitionsDictionary];
                               
                               
                         
                             NSArray* notifications = [UILocalNotification pacoNotificationsForExperimentSpecifications:actionSpecifications];
                               
                             [self.notificationManager scheduleNotifications:notifications];
                         
                              /* add object to running experiments */
                               
                           });
                         
                          /* synchronous*/
                          [self didStartStartRunningExperiment:experiment];
                     }
                 }
  
   
     
    return runStatus;
}





-(ValidatorExecutionStatus) stopRunningExperiment:(NSString*) experimentId
{
    ValidatorExecutionStatus runStatus = ValidatorExecutionStatusFail;
    
    
    [self cleanExpiredNotifications];
    
    
            PAExperimentDAO * experiment  = [self.runningExperiments findExperiment:experimentId];
           [self.runningExperiments removeExperiment:experimentId];
            [self addExperimentToAvailableStore:experiment];
    
    
           [self saveRunningExperiments];
    
    
            if(experiment)
            {
                
              runStatus =   [self willStopRunningExperiment:experiment];
                
              if(runStatus & ValidatorExecutionStatusSuccess  )
              {
                  dispatch_async(serialQueue, ^{
                      
                  
                    
                      
                      /* cancell the notifications for this exeriment */
                      [self.notificationManager  cancelNotificationsForExperiment:experimentId];
                      
                       });
                  
              }
                
             /* should run inside or outside the asynch thread?*/
             [self didStopRunningExperiment:experiment];
             
             
             }
   
    
    return runStatus;
  
}



-(ValidatorExecutionStatus) stopRunningExperimentRegenerate:(NSString*) experimentId
{
    ValidatorExecutionStatus runStatus = ValidatorExecutionStatusFail;
     // [self cleanExpiredNotifications];
    
    PAExperimentDAO * experiment  = [self.allExperiments findExperiment:experimentId];
    if(experiment)
    {

        runStatus =   [self willStopRunningExperiment:experiment];
        
        if(runStatus & ValidatorExecutionStatusSuccess  )
        {
            [self.runningExperiments removeObject:experiment];
            [self recalculateExperiments:YES];
            [self didStopRunningExperiment:experiment];
            
        }
        
        /* should run inside or outside the asynch thread?*/
    }
    
    return runStatus;
}


-(void) addExperimentToAvailableStore:(PAExperimentDAO*) experiment
{
    [_allExperiments addObject:experiment];
   
}

-(void) cleanExpiredNotifications
{
    
    [_notificationManager cleanExpiredNotifications];
    
}


-(void) replaceAllExperiments:(NSArray*) experiments
{
    
 
        _allExperiments  = [[NSMutableArray alloc] initWithArray:experiments];
    
}

/* this method should only be used for testing */
-(void) clearRunningExperimentsSynchronous
{
 
        
        
        [self.notificationManager cancelAllPacoNotifications];
        [self.runningExperiments removeAllObjects];
        [self.allExperiments removeAllObjects];
    
}


-(void) clearRunningExperiments
{
     dispatch_sync(serialQueue, ^{
         
        
         [self.notificationManager cancelAllPacoNotifications];
         [self.runningExperiments removeAllObjects];
         
      });
}


-(void) updateActionSpecifications:(NSArray*) newActionSpecifications RemoveAllNotifications:(BOOL) remveAll
{
    
          _oldActionSpecifications= _actionSpecifications;
          _actionSpecifications= [[NSMutableArray alloc] initWithArray:newActionSpecifications];
          NSArray* notifications = [UILocalNotification pacoNotificationsForExperimentSpecifications:_actionSpecifications];
          [self.notificationManager scheduleNotifications:notifications];
   
   
}


#pragma mark -  validator methods 

-(ValidatorExecutionStatus) willStartRunningExperiment:(PAExperimentDAO*) experiment Specificatons:(NSArray*) specifications
{
    
    ValidatorExecutionStatus  shouldStartExperiment =YES;
    
    for(id<PacoExerimentWillStartVerificationProtocol> validator in  self.willStartVerifiers)
    {
        ValidatorExecutionStatus  shouldStart =  [validator shouldStart:experiment Specifications:specifications];
        if( shouldStart & ValidatorExecutionStatusSuccess)
        {
            break;
        }
    }
                            
    return shouldStartExperiment;
    
    
}

/* need to create notifications. replicate the logic in PacoClient */


/*
 
 
 
 + (PacoEventExtended*)surveyMissedEventForDefinition:(PAExperimentDAO*)definition
 withScheduledTime:(NSDate*)scheduledTime
 groupName:(NSString*) groupName
 actionTriggerId:(NSString*) actionTri
 
 */


- (void)handleExpiredNotifications:(NSArray*)expiredNotifications
{
 
 
 
     if([expiredNotifications count]>0)
     {
         
        
            for (UILocalNotification* notification in expiredNotifications) {
                NSDictionary* userInfo = notification.userInfo;
                NSString* groupName = userInfo[@"groupName"];
                NSString* actionTriggerId = userInfo[@"actionTriggerId"];
                
             NSString* experimentId = [notification pacoExperimentIdExt];
             PAExperimentDAO * experiment  = [self.allExperiments findExperiment:experimentId];
             NSDate * fireDate =  [notification fireDate];
          
                NSString* actionTriggerSpecId =@"dummy";
                NSString* email =   [[PacoNetwork sharedInstance].authenticator userEmail];
             
                PacoEventExtended* eventExtended =   [PacoEventExtended surveyMissedEventForDefinition:experiment
                                                                                    withScheduledTime:fireDate
                                                                                    groupName:groupName
                                                                                    actionId:@"myactionId"
                                                                                    actionTriggerId:actionTriggerId
                                                                                    actionTriggerSpecId:actionTriggerSpecId
                                                                                    userEmail:email ];
                
                [[PacoEventManagerExtended defaultManager] saveEvent:eventExtended];
               
            }
            
            
             [[PacoEventManagerExtended defaultManager] startUploadingEvents];
     }
    
 
    
}



-(void) didStartStartRunningExperiment:(PAExperimentDAO*) experiment
{
    for(id<PacoExerimentDidStartVerificationProtocol> notifier  in  self.didStartNotifiers)
    {
        [notifier notifyDidStart:experiment];
    }
}

-(ValidatorExecutionStatus) willStopRunningExperiment:(PAExperimentDAO*) experiment
{
    
    
    ValidatorExecutionStatus shouldStartExperiment =YES;
    
    for(id<PacoExperimentWillStopVerificatonProtocol> validator in  self.willStopVerifiers)
    {
       ValidatorExecutionStatus shouldStart =  [validator shouldStop:experiment];
        if( shouldStart && ValidatorExecutionStatusSuccess)
        {
            shouldStartExperiment  = shouldStart;
            break;
        }
        
    }
    
    return shouldStartExperiment;
}


-(BOOL) isExperimentIdLive:(NSString*) experimentId
{
    
    BOOL hasExperiment = [self.runningExperiments  hasExperiment:experimentId];
    return hasExperiment;
}


-(BOOL) isExperimentLive:(PAExperimentDAO*) experiment
{
    NSString* experimentId = [experiment instanceId];
    BOOL hasExperiment = [self.runningExperiments  hasExperiment:experimentId];
    return hasExperiment;
}

-(void) didStopRunningExperiment:(PAExperimentDAO*) experiment
{
    
    
    for(id<PacoExerimentDidStartVerificationProtocol> notifier  in  self.didStartNotifiers)
    {
        [notifier notifyDidStart:experiment];
    }
    
    
}


-(void) didModifyExperiment:(PAExperimentDAO*) experiment
{
    for(id<PacoExperimentHasBeenModified> notifier  in  self.didStartNotifiers)
    {
        [notifier notifyWasModified:experiment];
    }
}




-(ValidatorExecutionStatus) willModifyRunningExperiment:(PAExperimentDAO*) experiment Specificatons:(NSArray*) specifications
{
    
     BOOL shouldModifyExperiment =YES;
    for(id<PacoExperimentWillBeModifiedProtocol> notifier  in  self.didModifyNotifiers)
    {
        shouldModifyExperiment = [notifier shouldModify:experiment  Specifications:specifications];
        shouldModifyExperiment =NO;
        break;
    }
    
    return shouldModifyExperiment;
    
}


-(void) clearVerifiersAndNotifieer
{
     dispatch_sync(serialQueue, ^{
         
         [self.willStartVerifiers removeAllObjects];
         [self.didStartNotifiers  removeAllObjects];
         [self.willStopVerifiers removeAllObjects];
         [self.didStopNotifiers removeAllObjects];
         
      });
    
}


#pragma mark = registration methods 

-(void) registerWillStartValidator:(id<PacoExerimentDidStartVerificationProtocol>) validator
{
    [self.willStartVerifiers addObject:validator];
    
}

-(void) registerDidStartNotifier:(id<PacoExerimentDidStartVerificationProtocol>) notifier
{
    [self.didStartNotifiers addObject:notifier];
    
}

-(void) registerDidStartNotifiers:(NSArray*) notifiers
{
    [self.didStartNotifiers addObjectsFromArray:notifiers];
    
}

-(void) registerWillModifyValidators:(NSArray*) validators
{
    [self.didModifyNotifiers addObjectsFromArray:validators];
    
}


-(void) registerDidModifyVNotifiers:(NSArray*) notifiers
{
    [self.didModifyNotifiers addObjectsFromArray:notifiers];
    
}



- (void)submitSurveyWithDefinition:(PAExperimentDAO*) definition
                      surveyInputs:(NSArray*)surveyInputs
                      notification:(UILocalNotification*)notification
{
    
    if (notification) {
        
        NSDictionary * dict  = notification.userInfo;
        
        
        NSString* actionTriggerId = dict[@"actionTriggerId"];
        NSString* groupId = dict[@"groupId"];
        NSString* groupName = dict[@"groupName"];
        NSString* notificationActionId = dict[@"notificationActionId"];
        NSString* actionTriggerSpecId = dict[@"actionTriggerSpecId"];
        
        
          NSString* email =   [[PacoNetwork sharedInstance].authenticator userEmail];
        
        [self.eventManager saveSurveySubmittedEventForDefinition:definition  withInputs:surveyInputs  andScheduledTime:[notification pacoFireDateExt] groupName:groupName  actionTriggerId:actionTriggerId actionId:notificationActionId actionTriggerSpecId:actionTriggerSpecId  userEmail:email];
        
        
        
       /* [self.eventManager saveSurveySubmittedEventForDefinition:definition
                                                      withInputs:surveyInputs
                                                andScheduledTime:[notification pacoFireDateExt]];*/
        

        [self.notificationManager handleRespondedNotification:notification];
        
    } else
    {
        
        [self.eventManager saveSelfReportEventWithDefinition:definition andInputs:surveyInputs];
    }
    
}



-(void) registerWillStartValidators:(NSArray*) validators
{
    
    [self.willStartVerifiers addObjectsFromArray:validators];
}


-(void) registerWillStopValidator:(id<PacoExperimentWillStopVerificatonProtocol>) notifier
{
    [self.willStopVerifiers addObject:notifier];
    
}

-(void) registerDidStopNotifier:(id<PacoExperimentDidStopVerificatonProtocol>) notifier
{
    [self.didStopNotifiers addObject:notifier];
    
}

-(void) registerDidStopNotifiers:(NSArray*) notifiers
{
    [self.didStopNotifiers addObjectsFromArray:notifiers];
    
}


-(void) saveRunningExperiments
{
    [self.runningExperiments store:KEY_RUNNING_EXERIMENTS];
}


-(void) refreshRunningExperiments
{
    [self.runningExperiments refreshFromStore:KEY_RUNNING_EXERIMENTS];
    
}


-(void) cleanup
{
    
    [self saveRunningExperiments];
    
}
    
 


@end
