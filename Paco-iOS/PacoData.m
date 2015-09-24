//
//  PacoData.m
//  Paco
//
//  Created by northropo on 9/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoData.h"
#import "PacoSignalStore.h"
#import "PacoEventStore.h"
#import "ExperimentDAO.h" 
#import "NSArray+PacoModel.h"
#import "PAExperimentDAO+Helper.h" 
#import "PacoExerimentDidStartVerificationProtocol.h"
#import "PacoExerimentWillStartVerificationProtocol.h"
#import "PacoExperimentDidStopVerificatonProtocol.h"
#import "PacoExperimentWillStopVerificatonProtocol.h"


@interface PacoData ()

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


@implementation PacoData


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


+ (PacoData*)sharedInstance
{
    static dispatch_once_t once;
    static PacoData *sharedInstance;
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


-(void) startRunningExperiment:(NSString*) experimentId
{
     dispatch_sync(serialQueue, ^{
         
                PAExperimentDAO * experiment  = [self.allExperiments findExperiment:experimentId];
                 if(experiment)
                 {
                     
                    BOOL shouldRun = [self willStartRunningExperiment:experiment];
                     if(shouldRun)
                     {
                         
                         [_runningExperiments addObject:experiment];
                          [self didStartStartRunningExperiment:experiment];
                         
                     }
                   
                 }
                
        
    });
}



-(void) stopRiunningExperiment:(NSString*) experimentId
{
   
    dispatch_sync(serialQueue, ^{
            PAExperimentDAO * experiment  = [self.allExperiments findExperiment:experimentId];
            if(experiment)
            {
                [self willStopRunningExperiment:experiment];
                
                // do work here
                
                
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

-(BOOL) willStartRunningExperiment:(PAExperimentDAO*) experiment
{
    
    BOOL shouldStartExperiment =YES;
    
    for(id<PacoExerimentWillStartVerificationProtocol> validator in  self.willStartVerifiers)
    {
        BOOL shouldStart =  [validator shouldStart:experiment];
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

-(BOOL) willStopRunningExperiment:(PAExperimentDAO*) experiment
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

@end
