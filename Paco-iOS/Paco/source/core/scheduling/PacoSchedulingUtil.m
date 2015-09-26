//
//  PacoSchedulingUtil.m
//  Paco
//
//  Created by northropo on 9/9/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoSchedulingUtil.h"
#include "ActionSpecification.h"
#import "ExperimentDAO.h"
#include "java/lang/Long.h"
#include "DateTime.h"
#import "OrgJodaTimeDateTime+PacoDateHelper.h"
#import "UILocalNotification+Paco.h"
#include "EsmSignalStore.h"
#include "EventStore.h"
#import "ActionScheduleGenerator.h" 
#import "PacoMediator.h" 
#include "EsmSignalStore.h"
#include "EventStore.h"
#import  "PacoSignalStore.h"
#import   "PacoEventStore.h"
#import  "OrgJodaTimeDateTime+PacoDateHelper.h"

 

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



+(NSArray*) makeAlarms:(NSArray*) specifications
{
    
    NSMutableArray* alerts = [[NSMutableArray alloc] init];
    for(PAActionSpecification* specification in specifications)
    {
        
        NSTimeInterval timeoutInterval = 20;
        UILocalNotification* notification  =    [UILocalNotification pacoNotificationWithExperimentId:[NSString stringWithFormat:@"%lli",[specification->experiment_->id__ longLongValue]]
                                                                                      experimentTitle:specification->experiment_->title_
                                                                                             fireDate:[specification->time_ nsDateValue]
                                                                                          timeOutDate:[NSDate dateWithTimeInterval:timeoutInterval sinceDate:[specification->time_ nsDateValue]]];
        
        [alerts addObject:notification];
    }
    return alerts;
}



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




+(NSArray*) buildActionSpecifications:(NSArray*) experiments IsDryRun:(BOOL) isTryRun
{
 
    NSArray* runningExperiments =   experiments;
    PacoSignalStore* signalStore = [PacoMediator sharedInstance].signalStore;
    PacoEventStore* eventStore = [PacoMediator sharedInstance].eventStore;
    
    
    NSMutableDictionary * specifications = [[NSMutableDictionary alloc] initWithCapacity:[runningExperiments count]];
   
    
    for(PAExperimentDAO* dao in runningExperiments)
    {
        [specifications setObject:[NSMutableArray new] forKey:[self uniqueId:dao]];
    }
    

    
    for(PAExperimentDAO* dao in runningExperiments)
    {
        [self  getFireTimes:dao results:specifications SignalStore:signalStore   EventStore:eventStore];
         
    }
    NSArray* results = [PacoSchedulingUtil sortAlarmTimes:specifications];
     return   [results subarrayWithRange:NSMakeRange(0, MIN(60, results.count))]  ;
}



+(void ) getFireTimes:(PAExperimentDAO*)  definition  results:(NSMutableDictionary*) results  SignalStore:( id<PAEsmSignalStore>)signalStore EventStore:( id<PAEventStore>)eventStore
{
    
    OrgJodaTimeDateTime *  nextTime =  [OrgJodaTimeDateTime  now];
    PAActionSpecification *actionSpecification ;
    int count  =0;
    do {
        
        PAActionScheduleGenerator *actionScheduleGenerator = [[PAActionScheduleGenerator alloc] initWithPAExperimentDAO:definition];
        
        
        actionSpecification   = [actionScheduleGenerator getNextTimeFromNowWithOrgJodaTimeDateTime:nextTime withPAEsmSignalStore:signalStore withPAEventStore:eventStore];
        
        if( actionSpecification )
        {
            
            nextTime = [actionSpecification->time_ plusMinutesWithInt:1];
            NSMutableArray* mArray =[results objectForKey:[self uniqueId:definition]];
            [mArray  addObject:actionSpecification];
            NSLog(@" added  %@", nextTime);
        }
        
        
    } while (actionSpecification !=nil &&  count++ <= 60 );
    
    
}


/*
 
 return a unique id for an object
 */
+(NSValue*) uniqueId:(NSObject*) actionSpecification
{
    return [NSValue valueWithPointer:(__bridge const void *)(actionSpecification)];
}


#pragma mark -  notification handelers



- (void)handleExpiredNotifications:(NSArray*)expiredNotifications
{
    
    
    
}
- (BOOL)isDoneInitializationForMajorTask
{
    
    return YES;
    
}
- (BOOL)needsNotificationSystem
{
    return NO;
}


- (void)updateNotificationSystem
{
    
    
}



- (NSArray*) fetchNotifications;
{
    NSArray * runningExperiments =  [[PacoMediator sharedInstance] runningExperiments];
    NSArray* newActionSpecifications  = [PacoSchedulingUtil buildActionSpecifications:runningExperiments  IsDryRun:NO];
    [[PacoMediator sharedInstance] updateActionSpecifications:newActionSpecifications];
    return newActionSpecifications;
    
}

@end
