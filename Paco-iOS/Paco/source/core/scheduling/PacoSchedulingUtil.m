//
//  PacoSchedulingUtil.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/9/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoSchedulingUtil.h"
#include "ActionSpecification.h"
#import "ExperimentDAO.h"
#include "java/lang/Long.h"
#include "DateTime.h"
#import "OrgJodaTimeDateTime+PacoDateHelper.h"
#include "EsmSignalStore.h"
#include "EventStore.h"
#import "ActionScheduleGenerator.h" 
#import "PacoMediator.h" 
#include "EsmSignalStore.h"
#include "EventStore.h"
#import  "PacoSignalStore.h"
#import   "PacoEventStore.h"
#import  "OrgJodaTimeDateTime+PacoDateHelper.h"
#import "NSObject+J2objcKVO.h"
#include "ActionSpecification.h"
#import "PAExperimentDAO+Helper.h"
#import "NSMutableArray+PacoModel.h"




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

@implementation PacoSchedulingUtil




/*
 
 merge action specification for each active experiment. take top 60.
 Note that the actions specification for each active experiment must be sorted
 before sending a message to this method.
 
 */
+(NSArray*) sortAlarmTimes:(NSDictionary*) fireTimes
{
    
    NSMutableArray * unionOfAllTimes = [[NSMutableArray alloc] init];
    NSArray* allValues = [fireTimes allValues];
    
    for(NSMutableArray * definitions in allValues)
    {
        [unionOfAllTimes  addObjectsFromArray:definitions];
    }
    
    
    NSArray *sortedArray;
    sortedArray = [unionOfAllTimes sortedArrayUsingComparator:^NSComparisonResult(id a, id b) {
        
        PAActionSpecification *actionDefinitionA =(PAActionSpecification*) a;
        PAActionSpecification *actionDefinitionB =(PAActionSpecification*) b;
        if( [actionDefinitionA->time_ isGreaterThan:actionDefinitionB->time_] )
        {
            return  NSOrderedDescending;
        }
        else
        {
            return  NSOrderedAscending;
        }
    }];
    
    return sortedArray;
}


/*
 
    iterates over array of Experiments to build dictionary of ActionSpecificatioins.
 */

+(NSArray*) buildActionSpecifications:(NSArray*) experiments IsDryRun:(BOOL) isTryRun  ActionSpecificationsDictionary:(NSMutableDictionary*) specificationsDictionary;
{
 
  
    PacoSignalStore* signalStore = [PacoMediator sharedInstance].signalStore;
    PacoEventStore* eventStore = [PacoMediator sharedInstance].eventStore;
  //  NSMutableDictionary * specifications = [[NSMutableDictionary alloc] initWithCapacity:[runningExperiments count]];
   
    for(PAExperimentDAO* dao in experiments)
    {
        
        if(![[PacoMediator sharedInstance].runningExperiments hasExperiment:[dao instanceId]])
        {
          [specificationsDictionary setObject:[NSMutableArray new] forKey:[dao instanceId]];
        }
        else
        {
            // remove all existing specifications for this object  as they will be recalculated
            [[specificationsDictionary objectForKey:[dao instanceId]] removeAllObjects];
            
        }
    }
    for(PAExperimentDAO* dao in experiments)
    {
        [self  getFireTimes:dao results:specificationsDictionary SignalStore:signalStore   EventStore:eventStore];
    }
    
       NSArray* results = [PacoSchedulingUtil sortAlarmTimes:specificationsDictionary];
       return   [results subarrayWithRange:NSMakeRange(0, MIN(60, results.count))]  ;
}

/*
    builds list of actionDefinitions add add to the results dictionary.
 */

+(void ) getFireTimes:(PAExperimentDAO*)  definition  results:(NSMutableDictionary*) results  SignalStore:( id<PAEsmSignalStore>)signalStore EventStore:( id<PAEventStore>)eventStore
{
    
    OrgJodaTimeDateTime *  nextTime =  [OrgJodaTimeDateTime  now];
    PAActionSpecification *actionSpecification ;
    int count  =0;
    do {
        
        PAActionScheduleGenerator *actionScheduleGenerator = [[PAActionScheduleGenerator alloc] initWithPAExperimentDAO:definition];
        
        // po [definition valueForKeyPathEx:@"groups[0].actionTriggers[0].actions[0].timeout"]
        /*  
          seems to return nil for action
         */
        actionSpecification   = [actionScheduleGenerator getNextTimeFromNowWithOrgJodaTimeDateTime:nextTime withPAEsmSignalStore:signalStore withPAEventStore:eventStore];
        
  
        
        if( actionSpecification )
        {
            
            if(actionSpecification->action_ == nil)
            {
                
                NSLog(@"actionSpecification->action+ == nil");
            }
            
            
            // ----> should store time then increment
            nextTime = [actionSpecification->time_ plusMinutesWithInt:1];
            NSMutableArray* mArray =[results objectForKey:[definition instanceId]];
            [mArray  addObject:actionSpecification];
            NSLog(@" added  %@", nextTime);
        }
        
        
    } while (actionSpecification !=nil &&  count++ <= 60 );
    
    
}





#pragma mark -  notification handelers


- (void)handleExpiredNotifications:(NSArray*)expiredNotifications
{
    
    
    
}


 


- (BOOL)needsNotificationSystem
{
    return NO;
}

/* 
      resets the notifications dictionary; updates action specification array and restst the notifications.
 
 */
+ (void)  updateNotifications:(NSArray*) experimentsToRun
             ActionSpecificationsDictionary:(NSMutableDictionary*) actionSpecificationsDictionary
 ShouldCancelAllNotifications:(BOOL) shouldCancellAllNotifications
{
    NSArray* newActionSpecifications  = [PacoSchedulingUtil buildActionSpecifications:experimentsToRun  IsDryRun:NO ActionSpecificationsDictionary:actionSpecificationsDictionary];
    
    [[PacoMediator sharedInstance] updateActionSpecifications:newActionSpecifications RemoveAllNotifications:shouldCancellAllNotifications];
}

@end
