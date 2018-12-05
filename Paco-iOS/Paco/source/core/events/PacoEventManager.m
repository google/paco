/* Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "PacoEventManager.h"
#import "PacoEvent.h"
#import "PacoEventUploader.h"
#import "NSString+Paco.h"
#import "NSError+Paco.h"
#import "PacoClient.h"

static NSString* const kPendingEventsFileName = @"pendingEvents.plist";
static NSString* const kAllEventsFileName = @"allEvents.plist";


@interface PacoParticipateStatus ()
@property(nonatomic) NSUInteger numberOfNotifications;
@property(nonatomic) NSUInteger numberOfParticipations;
@property(nonatomic) NSUInteger numberOfSelfReports;
@property(nonatomic) float percentageOfParticipation;
@property(nonatomic, copy) NSString *percentageText;

@end

@implementation PacoParticipateStatus

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
  for (; index >= 0; index--) {
    PacoEventType eventType = [(PacoEvent *)events[index] type];
    if (eventType == PacoEventTypeJoin || eventType == PacoEventTypeStop) {
      break;
    }
    if (eventType == PacoEventTypeSurvey) {
      numOfParticipations++;
    } else if (eventType == PacoEventTypeMiss) {
      numOfMiss++;
    } else if (eventType == PacoEventTypeSelfReport) {
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



@interface PacoEventManager () <PacoEventUploaderDelegate>
//array of PacoEvent
@property(atomic, strong) NSMutableArray* pendingEvents;
//dictionary: key is experiment's instanceId, value is an array of events, ordered by responseTime,
//the first event in this array is the oldest
@property(atomic, strong) NSMutableDictionary* eventsDict;

@property(atomic, strong) PacoEventUploader* uploader;

@end


@implementation PacoEventManager

- (id)init {
  self = [super init];
  if (self) {
    _uploader = [PacoEventUploader uploaderWithDelegate:self];
  }
  return self;
}

+ (PacoEventManager*)defaultManager {
  return [[PacoEventManager alloc] init];
}


#pragma mark Private methods
- (id)loadJsonObjectFromFile:(NSString*)fileName {
  NSString* filePath = [NSString pacoDocumentDirectoryFilePathWithName:fileName];
  NSError* error = nil;
  NSData* jsonData = [NSData dataWithContentsOfFile:filePath
                                            options:NSDataReadingMappedIfSafe
                                              error:&error];
  if (error != nil && ![error pacoIsFileNotExistError]) {
    DDLogError(@"[Error]Failed to load %@: %@",
          fileName,
          error.description ? error.description : @"unknown error");
    return nil;
  }
  
  if (jsonData == nil) {
    return nil;
  }
  NSError *jsonError = nil;
  id jsonObj = [NSJSONSerialization JSONObjectWithData:jsonData
                                               options:NSJSONReadingAllowFragments
                                                 error:&jsonError];
  if (jsonError) {
    DDLogError(@"[Error]Failed to serialize %@: %@",
          fileName,
          error.description ? error.description : @"unknown error");
    return nil;
  }
  return jsonObj;
}

- (NSError*)saveJsonObject:(id)jsonObject toFile:(NSString*)fileName {
  NSError* jsonError = nil;
  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:jsonObject
                                                     options:NSJSONWritingPrettyPrinted
                                                       error:&jsonError];
  if (jsonError) {
    DDLogError(@"[ERROR]Failed to serialize %@ to NSData: %@", fileName ,jsonError);
    return jsonError;
  }
  if (!jsonData) {
    DDLogError(@"jsonData is nil!");
  }
  NSAssert(jsonData != nil, @"jsonData should not be nil!");
  
  NSError* saveError = nil;
  [jsonData writeToFile:[NSString pacoDocumentDirectoryFilePathWithName:fileName]
                options:NSDataWritingAtomic
                  error:&saveError];
  if (saveError) {
    DDLogError(@"[ERROR]Failed to save %@: %@", fileName ,saveError);
  }else {
    DDLogInfo(@"Succeeded to save %@.", fileName);
  }
  return saveError;
}


- (NSMutableArray*)deserializedEvents:(id)jsonEvents {
  NSAssert(jsonEvents != nil, @"jsonEvents should not be nil!");  
  NSAssert([jsonEvents isKindOfClass:[NSArray class]],
           @"jsonEvents should be a NSArray!");
  
  NSMutableArray* deserializedEvents = [NSMutableArray arrayWithCapacity:[jsonEvents count]];
  for (id eventJson in jsonEvents) {
    PacoEvent* event = [PacoEvent pacoEventFromJSON:eventJson];
    NSAssert(event != nil, @"event should not be nil!");
    [deserializedEvents addObject:event];
  }
  return deserializedEvents;
}


- (void)fetchAllEventsIfNecessary {
  @synchronized(self) {
    if (self.eventsDict == nil) {
      NSDictionary* dict = [self loadJsonObjectFromFile:kAllEventsFileName];
      NSAssert(!(dict != nil && ![dict isKindOfClass:[NSDictionary class]]),
               @"dict should be a dictionary!");
      
      NSMutableDictionary* allEventsDict = [NSMutableDictionary dictionary];
      for (NSString* definitionId in dict) {
        id events = dict[definitionId];
        allEventsDict[definitionId] = [self deserializedEvents:events];
      }
      DDLogInfo(@"Fetched all events.");
      self.eventsDict = allEventsDict;
    }
  }
}

- (void)fetchPendingEventsIfNecessary {
  @synchronized(self) {
    if (self.pendingEvents == nil) {
      NSArray* events = [self loadJsonObjectFromFile:kPendingEventsFileName];
      NSAssert(!(events != nil && ![events isKindOfClass:[NSArray class]]),
               @"events should be an array");
      
      NSMutableArray* pendingEvents = [NSMutableArray array];
      if (events != nil) {
        pendingEvents = [self deserializedEvents:events];
      }
      DDLogInfo(@"Fetched %lu pending events.", (unsigned long)[pendingEvents count]);
      self.pendingEvents = pendingEvents;
    }
  }
}

- (NSMutableArray*)jsonArrayFromEvents:(NSArray*)events {
  NSMutableArray* jsonArr = [NSMutableArray arrayWithCapacity:[self.pendingEvents count]];
  for (PacoEvent* event in events) {
    id json = [event generateJsonObject];
    NSAssert(json != nil, @"json should not be nil!");
    [jsonArr addObject:json];
  }
  return jsonArr;
}

- (void)saveAllEventsToFile {
  @synchronized(self) {
    //If eventsDict is never loaded, then no need to save anything
    if (self.eventsDict == nil) {
      return;
    }
    
    NSMutableDictionary* jsonDict = [NSMutableDictionary dictionary];
    for (NSString* definitionId in self.eventsDict) {
      NSMutableArray* eventsArr = [self jsonArrayFromEvents:(self.eventsDict)[definitionId]];
      NSAssert(eventsArr != nil, @"eventsArr should not be nil!");
      jsonDict[definitionId] = eventsArr;
    }
    [self saveJsonObject:jsonDict toFile:kAllEventsFileName];
  }
}


- (void)savePendingEventsToFile {
  //If pendingEvents is never loaded, then no need to save anything
  if (self.pendingEvents == nil) {
    return;
  }
  DDLogInfo(@"Saving %lu pending events", (unsigned long)[self.pendingEvents count]);
  NSMutableArray* jsonArr = [self jsonArrayFromEvents:self.pendingEvents];
  [self saveJsonObject:jsonArr toFile:kPendingEventsFileName];
}



#pragma mark PacoEventUploaderDelegate 
- (BOOL)hasPendingEvents {
  @synchronized(self) {
    [self fetchPendingEventsIfNecessary];
    return [self.pendingEvents count] > 0;
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
    for (PacoEvent* event in events) {
      NSUInteger index = [self.pendingEvents indexOfObject:event];
      if (index == NSNotFound) {
        DDLogError(@"[ERROR]: Can't mark event complete since it's not in the pending events list!");
      }
      [self.pendingEvents removeObject:event];
    }
    
    [self savePendingEventsToFile];
    DDLogInfo(@"[Mark Complete] %lu events! ", (unsigned long)[events count]);
    DDLogInfo(@"[Pending Events] %lu.", (unsigned long)[self.pendingEvents count]);
  }
}



#pragma mark Public API
- (void)saveEvent:(PacoEvent*)event {
  NSAssert(event != nil, @"nil event cannot be saved!");
  [self saveEvents:@[event]];
}

- (void)saveEvents:(NSArray*)events {
  @synchronized(self) {
    NSAssert([events count] > 0, @"events should have more than one element");
    
    [self fetchAllEventsIfNecessary];
    [self fetchPendingEventsIfNecessary];
    
    for (PacoEvent* event in events) {
      NSString* experimentId = event.experimentId;
      NSAssert([experimentId length] > 0, @"experimentId should not be empty!");
      
      NSMutableArray* currentEvents = (self.eventsDict)[experimentId];
      if (currentEvents == nil) {
        currentEvents = [NSMutableArray array];
      }
      [currentEvents addObject:event];
      (self.eventsDict)[experimentId] = currentEvents;
      
      //add this event to pendingEvent list too
      [self.pendingEvents addObject:event];
    }
    [self saveDataToFile];
  }
}

- (void)saveAndUploadEvent:(PacoEvent*)event {
  [self saveEvent:event];
  [self startUploadingEvents];
}

- (void)saveJoinEventWithDefinition:(PacoExperimentDefinition*)definition
                       withSchedule:(PacoExperimentSchedule*)schedule {
  PacoEvent* joinEvent = [PacoEvent joinEventForDefinition:definition withSchedule:schedule];
  DDLogInfo(@"Save a join event");
  [self saveAndUploadEvent:joinEvent];
}

//YMZ:TODO: should we remove all the events for a stopped experiment?
- (void)saveStopEventWithExperiment:(PacoExperiment*)experiment {
  PacoEvent* event = [PacoEvent stopEventForExperiment:experiment];
  DDLogInfo(@"Save a stop event");
  [self saveAndUploadEvent:event];
}

- (void)saveSelfReportEventWithDefinition:(PacoExperimentDefinition*)definition
                                andInputs:(NSArray*)visibleInputs {
  PacoEvent* surveyEvent = [PacoEvent selfReportEventForDefinition:definition
                                                        withInputs:visibleInputs];
  DDLogInfo(@"Save a self-report event");
  [self saveAndUploadEvent:surveyEvent];
}


- (void)saveSurveySubmittedEventForDefinition:(PacoExperimentDefinition*)definition
                                   withInputs:(NSArray*)inputs
                             andScheduledTime:(NSDate*)scheduledTime {
  PacoEvent* surveyEvent = [PacoEvent surveySubmittedEventForDefinition:definition
                                                             withInputs:inputs
                                                       andScheduledTime:scheduledTime];
  DDLogInfo(@"Save a survey submitted event");
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
      DDLogInfo(@"No pending events to upload.");
      return;
    }
    UIApplicationState state = [[UIApplication sharedApplication] applicationState];
    if (state == UIApplicationStateActive) {
      DDLogInfo(@"There are %lu pending events to upload.", (unsigned long)[pendingEvents count]);
      [self.uploader startUploadingWithBlock:nil];
    } else {
      DDLogInfo(@"Won't upload %lu pending events since app is inactive.", (unsigned long)[pendingEvents count]);
    }
  }
}


- (void)startUploadingEventsInBackgroundWithBlock:(void(^)(UIBackgroundFetchResult))completionBlock {
  @synchronized(self) {
    NSArray* pendingEvents = [self allPendingEvents];
    if ([pendingEvents count] == 0) {
      DDLogInfo(@"No pending events to upload.");
      if (completionBlock) {
        completionBlock(UIBackgroundFetchResultNewData);
        DDLogInfo(@"Background fetch finished!");
      }
      return;
    }
    
    DDLogInfo(@"There are %lu pending events to upload.", (unsigned long)[pendingEvents count]);
    UIApplicationState state = [[UIApplication sharedApplication] applicationState];
    if (state == UIApplicationStateActive) {
      DDLogInfo(@"App State:UIApplicationStateActive");
    } else if (state == UIApplicationStateBackground) {
      DDLogInfo(@"App State:UIApplicationStateBackground");
    } else {
      DDLogInfo(@"App State:UIApplicationStateInActive");
    }
    [self.uploader startUploadingWithBlock:^(BOOL success) {
      if (completionBlock) {
        completionBlock(UIBackgroundFetchResultNewData);
        DDLogInfo(@"Background fetch finished!");
      }
    }];
  }
}

- (void)stopUploadingEvents {
  [self.uploader stopUploading];
}


#pragma mark participation stats
- (PacoParticipateStatus*)statsForExperiment:(NSString*)experimentId {
  if (!experimentId) {
    return nil;
  }
  [self fetchAllEventsIfNecessary];
  return [PacoParticipateStatus statusWithEvents:self.eventsDict[experimentId]];
}


@end
