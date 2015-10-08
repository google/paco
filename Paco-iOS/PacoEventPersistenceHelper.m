//
//  PacoEventPersistenceHelper.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 10/5/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoEventPersistenceHelper.h"
#import "EventInterface.h" 
#import "PacoAppDelegate.h"
#import "EventRecord.h"
#import "PacoEventExtended.h"
#import "PacoSerializer.h"
#import "PacoSerializeUtil.h"
#import "PacoSerializer.h"
#import "OrgJodaTimeDateTime+PacoDateHelper.h"
#import "NSDate+PacoTimeZoneHelper.h"

@interface PacoEventPersistenceHelper()


@property(nonatomic, retain, readwrite) PacoAppDelegate* appDelegate;
@property(nonatomic, retain, readwrite) NSManagedObjectContext * context;

@end



@implementation PacoEventPersistenceHelper

- (instancetype)init
{
    self = [super init];
    if (self) {
 
        _appDelegate  = (PacoAppDelegate *) [UIApplication sharedApplication].delegate;
        _context =  _appDelegate.managedObjectContext;
    }
    return self;
}

- (id<PAEventInterface>)getEventWithJavaLangLong:(JavaLangLong *)experimentId
                         withOrgJodaTimeDateTime:(OrgJodaTimeDateTime *)scheduledTime
                                    withNSString:(NSString *)groupName
                                withJavaLangLong:(JavaLangLong *)actionTriggerId
                                withJavaLangLong:(JavaLangLong *)scheduleId
{
    
    
    
    NSString* dateTime =  [[scheduledTime nsDateValue] dateToStringLocalTimezone];
    
    
    PacoEventExtended * returnedEvent=nil;
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EventRecord" inManagedObjectContext:self.context];
    NSPredicate* predicate =  [NSPredicate predicateWithFormat:@"(experimentId==%@) AND (scheduledTime==%@) AND   (groupName LIKE %@)   AND   (actionTriggerId==%@) AND (scheduleId==%@) ",experimentId,[[scheduledTime nsDateValue] dateToStringLocalTimezone] ,groupName,actionTriggerId,scheduleId ];
    
    
    
    //  NSPredicate* predicate =  [NSPredicate predicateWithFormat:@"(experimentId==%@) AND   (groupName LIKE %@)   AND   (actionTriggerId==%@) AND (scheduleId==%@)",experimentId, groupName,actionTriggerId,scheduleId];
    
     [fetchRequest setPredicate:predicate];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *arrayWithEvent  = [self.context executeFetchRequest:fetchRequest error:&error];
    
 
    EventRecord*  uniqueSearchedForRecord =nil;;
    
    if (error) {
        
        NSLog(@"%@, %@", error, error.localizedDescription);
        
    } else
    {
        uniqueSearchedForRecord=  [arrayWithEvent firstObject];
        NSData* data  =  uniqueSearchedForRecord.eventBlob;
        NSArray* array = [PacoSerializeUtil getClassNames];
        PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:array withNameOfClassAttribute:@"nameOfClass"];
        NSString* str =  [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];

        JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
        IOSObjectArray * iosArray = [resultArray toArray];
        PacoEventExtended  * event =  [iosArray objectAtIndex:0];
        
        returnedEvent = event;
        
    }
    
    return returnedEvent;
}


-(void) markUploaded:(id<PAEventInterface>)correspondingEvent
{
     EventRecord*  record   = [self fetchRecord:correspondingEvent];
     record.isUploaded = [NSNumber numberWithBool:YES];
    
    
}

/*
 
  update or create method.  deletes the existing record and  inserts a new record.
 
 */
- (void)updateEventWithPAEventInterface:(id<PAEventInterface>)correspondingEvent
{
    
    EventRecord*  record   = [self fetchRecord:correspondingEvent];
    /* tx boundary set */
    [self.context.undoManager beginUndoGrouping];
    
    BOOL isUploaded = NO;
    
    if(record !=nil)
    {
        isUploaded = record.isUploaded;
       [self.context deleteObject:record];
        isUploaded = record.isUploaded;
    }
    [self insertRecord:correspondingEvent];
    if(isUploaded)
        [self markUploaded:correspondingEvent];
 
    /* tx boundary end */
    [self.context.undoManager endUndoGrouping];
    
     NSError *error;
    if (![self.context save:&error])
    {
        NSLog(@"fail: %@", [error localizedDescription]);
        [self.context.undoManager undo];
    }
 
}


-(void) insertRecord:(id<PAEventInterface>)event
{
    NSAssert([event isKindOfClass:[PacoEventExtended class]], @"event should be of type PacoEventClass" );
    
    
    PacoEventExtended* theEvent = (PacoEventExtended*) event;
    
    EventRecord*  eventRecord = [NSEntityDescription
                                 insertNewObjectForEntityForName:@"EventRecord"
                                 inManagedObjectContext:[self.appDelegate managedObjectContext]];
    
    NSString* timeDate = theEvent.scheduledTime;
    
    eventRecord.experimentId  = theEvent.experimentId;
    eventRecord.scheduledTime = theEvent.scheduledTime;
    eventRecord.groupName =theEvent.groupName;
    eventRecord.actionTriggerId = theEvent.actionTriggerId;
    eventRecord.scheduleId  = theEvent.scheduleId;
    eventRecord.isUploaded =[NSNumber numberWithBool:NO];
    
    NSArray* array = [PacoSerializeUtil getClassNames];
    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:array withNameOfClassAttribute:@"nameOfClass"];
    NSData* data = [serializer toJSONobject:theEvent];
    eventRecord.eventBlob = data;
    
    
    NSError *error;
    if (![self.context save:&error])
    {
        NSLog(@"fail: %@", [error localizedDescription]);
        assert(FALSE);
    }
    
    
    
    
}





#pragma mark - helper methods

-(EventRecord*) fetchRecord:(id<PAEventInterface>) correspondingEvent
{
    PacoEventExtended* theEvent = ( PacoEventExtended* )  correspondingEvent;
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EventRecord" inManagedObjectContext:self.context];
    NSPredicate* predicate =  [NSPredicate predicateWithFormat:@"(experimentId==%@) AND (scheduledTime==%@) AND   (groupName LIKE %@)   AND   (actionTriggerId==%@) AND (scheduleId==%@)",theEvent.experimentId,theEvent.scheduledTime ,theEvent.groupName,theEvent.actionTriggerId,theEvent.scheduleId];
    
    [fetchRequest setPredicate:predicate];
    [fetchRequest setEntity:entity];
    NSError *error;
    NSArray *arrayWithEvent  = [self.context executeFetchRequest:fetchRequest error:&error];
    EventRecord*  record   = [arrayWithEvent firstObject];
    return record;
}

-(NSArray*) eventsForUpload
{
    
 
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EventRecord" inManagedObjectContext:self.context];
    NSPredicate* predicate =  [NSPredicate predicateWithFormat:@"isUploaded==NO"];
    
    [fetchRequest setPredicate:predicate];
    [fetchRequest setEntity:entity];
    NSError *error;
    NSArray *eventRecords = [self.context executeFetchRequest:fetchRequest error:&error];
    NSMutableArray* mutableArray = [NSMutableArray new];
    
     NSArray* array = [PacoSerializeUtil getClassNames];
    for(EventRecord* eventRecord in  eventRecords)
    {
        
        NSData* data  =  eventRecord.eventBlob;
        PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:array withNameOfClassAttribute:@"nameOfClass"];
        NSString* str =  [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
        IOSObjectArray * iosArray = [resultArray toArray];
        PacoEventExtended  * event =  [iosArray objectAtIndex:0];
        [mutableArray addObject:event];
        
    }
    return mutableArray;
}


-(NSArray*) allEvents
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EventRecord" inManagedObjectContext:self.context];
   [fetchRequest setEntity:entity];
    NSError *error;
    NSArray *eventRecords = [self.context executeFetchRequest:fetchRequest error:&error];
    NSMutableArray* mutableArray = [NSMutableArray new];
    NSArray* array = [PacoSerializeUtil getClassNames];
    for(EventRecord* eventRecord in  eventRecords)
    {
        
        NSData* data  =  eventRecord.eventBlob;
        PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:array withNameOfClassAttribute:@"nameOfClass"];
        NSString* str =  [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
        JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
        IOSObjectArray * iosArray = [resultArray toArray];
        PacoEventExtended  * event =  [iosArray objectAtIndex:0];
        [mutableArray addObject:event];
        
    }
    return mutableArray;
    
    
}


- (void)insertEventWithPAEventInterface:(id<PAEventInterface>)event
{
    [self insertRecord:event];
}


- (void) deleteAllEvents
{
    
    NSFetchRequest *fetchRequest =
    [NSFetchRequest fetchRequestWithEntityName:@"EventRecord"];
    fetchRequest.includesPropertyValues = NO;
    fetchRequest.includesSubentities = NO;
    
    NSError *error;
    NSArray *items = [self.context executeFetchRequest:fetchRequest error:&error];
    
    for (NSManagedObject *managedObject in items)
    {
        [self.context deleteObject:managedObject];
        NSLog(@"Deleted %@", @"EventRecord");
    }
    
}

@end
