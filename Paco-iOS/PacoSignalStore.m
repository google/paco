//
//  PacoSignalStore.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoSignalStore.h"

#include "ActionScheduleGenerator.h"
#include "ActionSpecification.h"
#include "ActionTrigger.h"
#include "DateMidnight.h"
#include "DateTime.h"
#include "EsmGenerator2.h"
#include "EsmSignalStore.h"
#include "EventStore.h"
#include "ExperimentDAO.h"
#include "ExperimentGroup.h"
#include "Interval.h"
#include "J2ObjC_source.h"
#include "NonESMSignalGenerator.h"
#include "PacoAction.h"
#include "PacoNotificationAction.h"
#include "Schedule.h"
#include "ScheduleTrigger.h"
#include "SignalTime.h"
#include "TimeUtil.h"
#include "java/lang/Boolean.h"
#include "java/lang/IllegalStateException.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/Collections.h"
#include "java/util/List.h"
#include  "Consts.h"
#import "PacoAppDelegate.h" 
#import "PacoSignal.h"
#import  "org/joda/time/DateTime.h"




@interface PacoSignalStore()


@property(nonatomic, retain, readwrite) PacoAppDelegate* appDelegate;
@property(nonatomic, retain, readwrite) NSManagedObjectContext * context;

@end

@implementation PacoSignalStore

- (instancetype)init
{
    self = [super init];
    if (self) {
 
         _appDelegate  = (PacoAppDelegate *) [UIApplication sharedApplication].delegate;
         _context =  _appDelegate.managedObjectContext;
    }
    return self;
}


- (void)storeSignalWithJavaLangLong:(JavaLangLong *)date
                   withJavaLangLong:(JavaLangLong *)experimentId
                   withJavaLangLong:(JavaLangLong *)alarmTime
                       withNSString:(NSString *)groupName
                   withJavaLangLong:(JavaLangLong *)actionTriggerId
                   withJavaLangLong:(JavaLangLong *)scheduleId
{
    
    
    PacoSignal*  pacoSignal = [NSEntityDescription
                       insertNewObjectForEntityForName:@"PacoSignal"
                       inManagedObjectContext:[self.appDelegate managedObjectContext]];
    
  
    
    
     pacoSignal.date  =   date;
     pacoSignal.experimentId = experimentId;
     pacoSignal.groupName =groupName;
     pacoSignal.actionTriggerId =actionTriggerId;
     pacoSignal.scheduleId = scheduleId;
     pacoSignal.alarmTime = alarmTime;
    
    
    NSError *error;
    if (![self.context save:&error])
    {
        NSLog(@"fail: %@", [error localizedDescription]);
        assert(FALSE);
    }
    
}




-(NSArray*)  matchRecords:(JavaLangLong *)date
  withJavaLangLong:(JavaLangLong *)experimentId
  withJavaLangLong:(JavaLangLong *)alarmTime
      withNSString:(NSString *)groupName
  withJavaLangLong:(JavaLangLong *)actionTriggerId
  withJavaLangLong:(JavaLangLong *)scheduleId
{

    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"PacoSignal" inManagedObjectContext:self.context];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(experimentId==%@) AND (date==%@) AND   (alarmTime == %@)  AND  (groupName LIKE %@)   AND   (actionTriggerId==%@) AND (scheduleId==%@)",experimentId,date, alarmTime,groupName,actionTriggerId,scheduleId];
    [fetchRequest setEntity:entity];
    [fetchRequest setPredicate:predicate];
    
    
    NSError *error;
    NSArray *signals  = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if (error) {
        
               NSLog(@"%@, %@", error, error.localizedDescription);
        
           } else
           {
         
               
              
               
           }
    
    
 
    return signals;
}




- (id<JavaUtilList>)getSignalsWithJavaLangLong:(JavaLangLong *)experimentId
                              withJavaLangLong:(JavaLangLong *)periodStart
                                  withNSString:(NSString *)groupName
                              withJavaLangLong:(JavaLangLong *)actionTriggerId
                              withJavaLangLong:(JavaLangLong *)scheduleId
{
    
    
    JavaUtilArrayList  * arrayList = [[JavaUtilArrayList alloc] init];
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"PacoSignal" inManagedObjectContext:self.context];
    
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(experimentId==%@) AND (date==%@) AND   (groupName LIKE %@)   AND   (actionTriggerId==%@) AND (scheduleId==%@)",experimentId, periodStart,groupName,actionTriggerId,scheduleId];
    
    [fetchRequest setEntity:entity];
    [fetchRequest setPredicate:predicate];
    
    
    NSError *error;
    NSArray *signals = [self.context executeFetchRequest:fetchRequest error:&error];
    if (error) {
        
        NSLog(@"%@, %@", error, error.localizedDescription);
        
    } else
    {
        for(PacoSignal* signal in signals)
        {
            OrgJodaTimeDateTime* dateTime = [[OrgJodaTimeDateTime  alloc] initWithLong:signal.alarmTime];
         [  arrayList addWithId:dateTime];
        }
        
    }
        
    return arrayList;
}


- (void)deleteAllObjectsWithEntityName:(NSString *)entityName
                             inContext:(NSManagedObjectContext *)context
{
    NSFetchRequest *fetchRequest =
    [NSFetchRequest fetchRequestWithEntityName:entityName];
    fetchRequest.includesPropertyValues = NO;
    fetchRequest.includesSubentities = NO;
    
    NSError *error;
    NSArray *items = [context executeFetchRequest:fetchRequest error:&error];
    
    for (NSManagedObject *managedObject in items) {
        [context deleteObject:managedObject];
        NSLog(@"Deleted %@", entityName);
    }
}

- (void) deleteAll
{
    
     NSFetchRequest *fetchRequest =
    [NSFetchRequest fetchRequestWithEntityName:@"PacoSignal"];
    fetchRequest.includesPropertyValues = NO;
    fetchRequest.includesSubentities = NO;
    
    NSError *error;
    NSArray *items = [self.context executeFetchRequest:fetchRequest error:&error];
    
    for (NSManagedObject *managedObject in items)
    {
        [self.context deleteObject:managedObject];
         NSLog(@"Deleted %@", @"PacoSignal");
    }
    
}




- (void)deleteAllSignalsForSurveyWithJavaLangLong:(JavaLangLong *)experimentId
{
    NSFetchRequest *fetchRequest =
    [NSFetchRequest fetchRequestWithEntityName:@"PacoSignal"];
    fetchRequest.includesPropertyValues = NO;
    fetchRequest.includesSubentities = NO;
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(experimentId==%@)",experimentId];
    [fetchRequest setPredicate:predicate];
    
    NSError *error;
    NSArray *pacoSignals = [self.context executeFetchRequest:fetchRequest error:&error];
    
    for (NSManagedObject *managedObject in pacoSignals)
    {
        [self.context deleteObject:managedObject];
        NSLog(@"Deleted %@", @"PacoSignal");
    }
}




- (void)deleteSignalsForPeriodWithJavaLangLong:(JavaLangLong *)experimentId
                              withJavaLangLong:(JavaLangLong *)periodStart
                                  withNSString:(NSString *)groupName
                              withJavaLangLong:(JavaLangLong *)actionTriggerId
                              withJavaLangLong:(JavaLangLong *)scheduleId
{
    
    NSFetchRequest *fetchRequest =
    [NSFetchRequest fetchRequestWithEntityName:@"PacoSignal"];
    fetchRequest.includesPropertyValues = NO;
    fetchRequest.includesSubentities = NO;
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(experimentId==%@) AND (date==%@) AND   (groupName LIKE %@)   AND   (actionTriggerId==%@) AND (scheduleId==%@)",experimentId, periodStart,groupName,actionTriggerId,scheduleId];
    [fetchRequest setPredicate:predicate];
    
    NSError *error;
    NSArray *pacoSignals = [self.context executeFetchRequest:fetchRequest error:&error];
    
    for (NSManagedObject *managedObject in pacoSignals)
    {
        [self.context deleteObject:managedObject];
        NSLog(@"Deleted %@", @"PacoSignal");
    }
    
}


@end
