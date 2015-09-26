//
//  PacoData.m
//  Paco
//
//  Created by northropo on 9/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoMediator.h"
#import "PacoSignalStore.h"
#import "PacoEventStore.h"
#import "ExperimentDAO.h" 
#import "NSArray+PacoModel.h"
#import "PAExperimentDAO+Helper.h" 
#import "PacoExerimentDidStartVerificationProtocol.h"
#import "PacoExerimentWillStartVerificationProtocol.h"
#import "PacoExperimentDidStopVerificatonProtocol.h"
#import "PacoExperimentWillStopVerificatonProtocol.h"
#import "PacoSchedulingUtil.h"
#import "ValidatorConsts.h"

@interface PacoMediator ()

@property (strong,nonatomic ) NSMutableArray* allExperiments;
@property (strong,nonatomic)   NSMutableArray* runningExperiments;
@property (strong,nonatomic)  NSMutableArray* actionSpecifications;
@property (strong,nonatomic ) NSMutableArray* oldActionSpecifications;


/* verifitcation protocols */
@property (strong,nonatomic ) NSMutableArray* willStartVerifiers;
@property (strong,nonatomic ) NSMutableArray*  didStartNotifiers;
@property (strong,nonatomic ) NSMutableArray*  willStopVerifiers;
@property (strong,nonatomic ) NSMutableArray*  didStopNotifiers;
    
@end
 
static dispatch_queue_t serialQueue;


@implementation PacoMediator



- (instancetype)init
{
    self = [super init];
    
    if (self) {
        
        self.allExperiments               = [[NSMutableArray alloc] init];
        self.runningExperiments           = [[NSMutableArray alloc] init];
        self.actionSpecifications         = [[NSMutableArray alloc] init];
        self.willStartVerifiers           = [[NSMutableArray alloc] init];
        self.didStartNotifiers            = [[NSMutableArray alloc] init];
        self.willStopVerifiers            = [[NSMutableArray alloc] init];
        self.didStopNotifiers             = [[NSMutableArray alloc] init];
        self.signalStore                  = [[PacoSignalStore alloc] init];
        self.eventStore                   = [[PacoEventStore alloc] init];
    }
    return self;
}


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



-(NSArray*) runningExperiments
{
    
    return  _runningExperiments;
    
}


-(ValidatorExecutionStatus) startRunningExperiment:(NSString*) experimentId
{
   __block  ValidatorExecutionStatus runStatus = ValidatorExecutionStatusFail;
    
    
     dispatch_sync(serialQueue, ^{
         
         
   
         
                PAExperimentDAO * experiment  = [self.allExperiments findExperiment:experimentId];
                 if(experiment)
                 {
                     
                     
                     NSArray* array = [PacoSchedulingUtil buildActionSpecifications:@[experiment]  IsDryRun:YES];
                     runStatus = [self willStartRunningExperiment:experiment  Specificatons:array];
                     if( runStatus & ValidatorExecutionStatusSuccess )
                     {
                         
                         [_runningExperiments addObject:experiment];
                         [self didStartStartRunningExperiment:experiment];
                         
                     }
                   
                 }
                
         
    });
    
    return runStatus;
}



-(ValidatorExecutionStatus) stopRiunningExperiment:(NSString*) experimentId
{
    __block  ValidatorExecutionStatus runStatus = ValidatorExecutionStatusFail;
    dispatch_sync(serialQueue, ^{
            PAExperimentDAO * experiment  = [self.allExperiments findExperiment:experimentId];
            if(experiment)
            {
                
                
                
              runStatus =   [self willStopRunningExperiment:experiment];
                
              if(runStatus & ValidatorExecutionStatusSuccess  )
              {
                  
                  
                  [self.runningExperiments removeObject:experiment];
                  
                  // do work here.
              }
                
                
                [self didStopRunningExperiment:experiment];
             
             
             }
    });
    
  
}



-(BOOL) addExperimentToAvailableStore:(PAExperimentDAO*) experiment
{
    BOOL retVal = YES;
    
    [_allExperiments addObject:experiment];
    
    return retVal;
    
}

-(void) clearRunningExperiments
{
     dispatch_sync(serialQueue, ^{
         
    [_runningExperiments removeAllObjects];
      });
}


-(void) updateActionSpecifications:(NSArray*) newActionSpecifications
{
      dispatch_sync(serialQueue, ^{
          
          
    _oldActionSpecifications= _actionSpecifications;
    _actionSpecifications= [[NSMutableArray alloc] initWithArray:newActionSpecifications];
      });
}



#pragma mark - will start

-(ValidatorExecutionStatus) willStartRunningExperiment:(PAExperimentDAO*) experiment Specificatons:(NSArray*) specifications
{
    
    BOOL shouldStartExperiment =YES;
    
    for(id<PacoExerimentWillStartVerificationProtocol> validator in  self.willStartVerifiers)
    {
        BOOL shouldStart =  [validator shouldStart:experiment Specifications:specifications];
        if(!shouldStart)
        {
                                
            shouldStartExperiment =NO;
            break;
            
        }
        
    }
                            
    return shouldStartExperiment;
    
    
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
    
    
    BOOL shouldStartExperiment =YES;
    
    for(id<PacoExperimentWillStopVerificatonProtocol> validator in  self.willStopVerifiers)
    {
        BOOL shouldStart =  [validator shouldStop:experiment];
        if(!shouldStart)
        {
            shouldStartExperiment =NO;
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

-(void) clearVerifiersAndNotifieer
{
     dispatch_sync(serialQueue, ^{
         
         [self.willStartVerifiers removeAllObjects];
         [self.didStartNotifiers  removeAllObjects];
         [self.willStopVerifiers removeAllObjects];
         [self.didStopNotifiers removeAllObjects];
         
      });
    
}

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


@end
