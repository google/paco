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
#import "PacoEventManagerExtended.h"
#import "ExperimentDAO.h"
#import "PacoEventExtended.h" 
#import "PacoEventUploader.h"
#import "NSString+Paco.h"
#import "NSError+Paco.h"
#import "PacoSerializer.h"
#import  "ActionSpecification.h"
#import "PacoEventPersistenceHelper.h"
#import "PacoSerializeUtil.h" 
#import "NSMutableArray+PacoEvent.h"
#import "PacoSerializer.h" 
#include "java/util/ArrayList.h"
#import "JavaUtilArrayList+PacoConversion.h" 
#import "PacoEventPersistenceHelper.h"



static NSString* const kPendingEventsFileName = @"pendingEvents.plist";
static NSString* const kAllEventsFileName = @"allEvents.plist";


@interface PacoParticipateStatusExtended ()
@property(nonatomic) NSUInteger numberOfNotifications;
@property(nonatomic) NSUInteger numberOfParticipations;
@property(nonatomic) NSUInteger numberOfSelfReports;
@property(nonatomic) float percentageOfParticipation;
@property(nonatomic, copy) NSString *percentageText;

@end





/*
 
 Reporting helper container class describes the number of each type of notification as well as
 the number of notifications.
 
 */
@implementation PacoParticipateStatusExtended

- (instancetype)initWithNotificationNumber:(NSUInteger)numOfNotifications
                       participationNumber:(NSUInteger)numOfParticipations
                          selfReportNumber:(NSUInteger)numOfSelfReports {
    self = [super init];
    if (self) {
        _numberOfNotifications = numOfNotifications;
        _numberOfParticipations = numOfParticipations;
        _numberOfSelfReports = numOfSelfReports;
 
        
        if (_numberOfNotifications > 0) {
            _percentageOfParticipation = (float)_numberOfParticipations / (float)_numberOfNotifications;
            long int percentage = lroundf(_percentageOfParticipation * 100);
            _percentageText = [[NSString stringWithFormat:@"%ld%%", percentage] copy];
        }
    }
    return self;
}

+ (instancetype)statusWithNotificationNumber:(NSUInteger)numOfNotifications
                         participationNumber:(NSUInteger)numOfParticipations
                            selfReportNumber:(NSUInteger)numOfSelfReports {
    
    return [[self alloc] initWithNotificationNumber:numOfNotifications
                                participationNumber:numOfParticipations
                                   selfReportNumber:numOfSelfReports];
    
}

//assume events are ordered
+ (instancetype)statusWithEvents:(NSArray*)events {
    if (0 == [events count]) {
        return [self statusWithNotificationNumber:0 participationNumber:0 selfReportNumber:0];
    }
    int numOfMiss = 0;
    int numOfParticipations = 0;
    int numOfSelfReports = 0;
    
    NSInteger index = [events count] - 1;
    
    // remember we assume the events are sorted.
    for (; index >= 0; index--) {
        PacoEventTypeExtended eventType = [(PacoEventExtended *) events[index] type];
        if (eventType == PacoEventTypeJoinExtended || eventType == PacoEventTypeStopExtended) {
            break;
        }
        if (eventType == PacoEventTypeSurveyExtended) {
            numOfParticipations++;
        } else if (eventType == PacoEventTypeMissExtended) {
            numOfMiss++;
        } else if (eventType == PacoEventTypeSelfReportExtended) {
            numOfSelfReports++;
        } else {
            NSAssert(NO, @"invalid type");
        }
    }
    return [self statusWithNotificationNumber:(numOfMiss + numOfParticipations)
                          participationNumber:numOfParticipations
                             selfReportNumber:numOfSelfReports];
}

@end


/*
 
   create and persist event objects  for a descrete number of events. 
   upload events and when upload is successful mark them as successful.
 
 
 */
@interface PacoEventManagerExtended () <PacoEventUploaderDelegate>
//array of PacoEvent
@property(atomic, strong) NSMutableArray* pendingEvents;
//dictionary: key is experiment's instanceId, value is an array of events, ordered by responseTime,
//the first event in this array is the oldest
@property(atomic, strong) NSMutableDictionary* eventsDict;

@property(atomic, strong) PacoEventUploader* uploader;


@property(nonatomic, strong) PacoEventPersistenceHelper *persistenceHelper;
@end


@implementation PacoEventManagerExtended

- (id)init {
    self = [super init];
    if (self) {
        _uploader = [PacoEventUploader uploaderWithDelegate:self];
        _persistenceHelper = [PacoEventPersistenceHelper new];
        _pendingEvents = [NSMutableArray new];
    }
    return self;
}

+ (PacoEventManagerExtended*)defaultManager {
    
        static dispatch_once_t once;
        static PacoEventManagerExtended *sharedInstance;
        dispatch_once(&once, ^ {
            
            sharedInstance = [[self alloc] init];
            
        });
        
        return sharedInstance;
}


#pragma mark Private methods







/* 
 
   fetches all events populates dict using experimentId as key
   for the list of events belonging to a given expeirment.
 
 
 */
- (void)fetchAllEventsIfNecessary {
    @synchronized(self) {
        if (self.eventsDict == nil) {
            
            NSMutableDictionary* dict = [NSMutableDictionary new];
            NSArray* events =  [self.persistenceHelper allEvents];
            
            for(PacoEventExtended* event  in events)
            {
                if ([[dict allKeys] containsObject:event.experimentId])
                {
                    [dict setObject:[NSMutableArray new] forKey:event.experimentId];
                }
                
                [[dict objectForKey:event.experimentId] addObject:event];
            }
            self.eventsDict = dict;
        }
    }
}

- (void)fetchPendingEventsIfNecessary {
    @synchronized(self) {
        if (self.pendingEvents == nil) {
            self.pendingEvents =  [[NSMutableArray  alloc] initWithArray:[self.persistenceHelper eventsForUploadNative]];
        }
    }
}



- (void)saveAllEventsToFile {
    @synchronized(self) {
        
        //If eventsDict is never loaded, then no need to save anything
        if (self.eventsDict == nil) {
            return;
        }
        
        NSArray* arrayOfArray = [self.eventsDict allValues];
 
       for(NSArray* array in arrayOfArray)
       {
          for(PacoEventExtended* event in array)
          {
             [_persistenceHelper updateEventWithPAEventInterface:event];
          }
       }
        
        
      
    }
}


- (void)savePendingEventsToFile {
    
    if (self.pendingEvents == nil) {
        return;
    }
    for(PacoEventExtended* event in self.pendingEvents)
    {
        [self.persistenceHelper updateEventWithPAEventInterface:event];
        
    }
  
}



#pragma mark PacoEventUploaderDelegate
- (BOOL)hasPendingEvents {
    @synchronized(self) {
        
    return ([[self.persistenceHelper eventsForUpload] count] >0);
     
    }
}

- (NSArray*)allPendingEvents {
    @synchronized(self) {
        
        
        [self fetchPendingEventsIfNecessary];
        NSArray* result = [NSArray arrayWithArray:self.pendingEvents];
        return result;
        
    }
}

- (void)markEventsComplete:(NSArray*)events {
    if (0 == [events count]) {
        return;
    }
    
    @synchronized(self) {
        NSAssert(self.pendingEvents != nil, @"pending events should have already loaded!");
        for (NSDictionary * event in events)
        {
            [self.persistenceHelper markUploaded:event];
            [self.pendingEvents removeEvent:event];
        }
        
     
    }
}



#pragma mark Public API
- (void)saveEvent:(PacoEventExtended*)event {
  NSAssert(event != nil, @"nil event cannot be saved!");
   
   
    NSDictionary* dictionary =   [event generateJsonObject];
    NSLog(@" dictionary %@", dictionary);
    
    
    
  //
  [self.persistenceHelper insertEventWithPAEventInterface:event];
    
  //add this event to pendingEvent list  as well
  [self.pendingEvents addObject:dictionary ];
    
}




//- (void)saveEvents:(NSArray*)events {
//  @synchronized(self) {
//    NSAssert([events count] > 0, @"events should have more than one element");
//    for (PacoEventExtended* event in events) {
//      [self.persistenceHelper updateEventWithPAEventInterface:event];
//    }
//    [self saveDataToFile];
//  }
//}

- (void)saveAndUploadEvent:(PacoEventExtended*)event {
    
    [self saveEvent:event];
    [self startUploadingEvents];
}

- (void)saveJoinEventWithActionSpecification:(PAActionSpecification*) actionSpecification
{
    
    PacoEventExtended* joinEvent = [PacoEventExtended joinEventForActionSpecificaton:actionSpecification];
    [self saveAndUploadEvent:joinEvent];
}

//YMZ:TODO: should we remove all the events for a stopped experiment?
- (void)saveStopEventWithExperiment:(PacoExperimentExtended*)experiment {
    PacoEventExtended* event = [PacoEventExtended stopEventForExperiment:experiment];
    NSLog(@"Save a stop event");
    [self saveAndUploadEvent:event];
}

- (void)saveSelfReportEventWithDefinition:(PAExperimentDAO*)definition
                                    group:(PAExperimentGroup*) group
                                andInputs:(NSArray*)visibleInputs {
    
    PacoEventExtended* surveyEvent = [PacoEventExtended selfReportEventForDefinition:definition
                                                                               group:group
                                                           withInputs:visibleInputs];
    [self saveAndUploadEvent:surveyEvent];
    
    
}


- (void)saveSurveySubmittedEventForDefinition:(PAExperimentDAO*)definition
                                   withInputs:(NSArray*)inputs
                             andScheduledTime:(NSDate*)scheduledTime
                                 responseTime:(int) secondsToResponse
                                    groupName:(NSString*) groupName
                              actionTriggerId:(NSString*) actionTriggerId
                                     actionId:(NSString*) actionId
                          actionTriggerSpecId:(NSString*) actionTriggerSpecId
                                    userEmail:(NSString*)userEmail
                                 responseTime:(NSNumber*) responseTime


{
    
    
    PacoEventExtended* surveyEvent = [PacoEventExtended surveySubmittedEventForDefinition:definition  withInputs:inputs  ScheduledTime:scheduledTime  groupName:groupName actionTriggerId:actionTriggerId actionId:actionId actionTriggerSpecId:actionTriggerSpecId userEmail:userEmail responseTime:responseTime];
    
    
    
    NSLog(@"Save a survey submitted event");
    [self saveAndUploadEvent:surveyEvent];
}


- (void)saveDataToFile {
    @synchronized(self) {
        [self savePendingEventsToFile];
        [self saveAllEventsToFile];
    }
}

- (void)startUploadingEvents {
    @synchronized(self) {
        NSArray* pendingEvents = [self allPendingEvents];
        if ([pendingEvents count] == 0) {
            NSLog(@"No pending events to upload.");
            return;
        }
        UIApplicationState state = [[UIApplication sharedApplication] applicationState];
        if (state == UIApplicationStateActive) {
            NSLog(@"There are %lu pending events to upload.", (unsigned long)[pendingEvents count]);
            [self.uploader startUploadingWithBlock:nil];
        } else {
            NSLog(@"Won't upload %lu pending events since app is inactive.", (unsigned long)[pendingEvents count]);
        }
    }
}


-(NSArray*) allEventsForExperiment:(NSNumber*) experimentId
{
 

    PacoEventPersistenceHelper* helper =  [[PacoEventPersistenceHelper alloc] init];
    NSArray * all_events =  [helper eventsForExperimentId:[experimentId longValue]];
    
    return all_events;
    
}



- (void)startUploadingEventsInBackgroundWithBlock:(void(^)(UIBackgroundFetchResult))completionBlock {
    @synchronized(self) {
        NSArray* pendingEvents = [self allPendingEvents];
        
        
        if ([pendingEvents count] == 0) {
            NSLog(@"No pending events to upload.");
            if (completionBlock) {
                completionBlock(UIBackgroundFetchResultNewData);
                NSLog(@"Background fetch finished!");
            }
            return;
        }
        
        NSLog(@"There are %lu pending events to upload.", (unsigned long)[pendingEvents count]);
        
        UIApplicationState state = [[UIApplication sharedApplication] applicationState];
        if (state == UIApplicationStateActive) {
            NSLog(@"App State:UIApplicationStateActive");
        } else if (state == UIApplicationStateBackground) {
            NSLog(@"App State:UIApplicationStateBackground");
        } else {
            NSLog(@"App State:UIApplicationStateInActive");
        }
        [self.uploader startUploadingWithBlock:^(BOOL success) {
            if (completionBlock) {
                completionBlock(UIBackgroundFetchResultNewData);
                NSLog(@"Background fetch finished!");
            }
        }];
    }
}


- (void)stopUploadingEvents {
    [self.uploader stopUploading];
}



#pragma mark participation stats
- (PacoParticipateStatusExtended*)statsForExperiment:(NSString*)experimentId {
    if (!experimentId) {
        return nil;
    }
    [self fetchAllEventsIfNecessary];
    return [PacoParticipateStatusExtended  statusWithEvents:self.eventsDict[experimentId]];
}


@end
