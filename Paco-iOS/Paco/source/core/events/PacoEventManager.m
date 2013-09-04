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

static NSString* const kPendingEventsFileName = @"pendingEvents.plist";
static NSString* const kAllEventsFileName = @"allEvents.plist";

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
- (NSString*)documentPathForFile:(NSString*)fileName {
  NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString* documentsDirectory = [paths objectAtIndex:0];
  NSString* fullPath = [NSString stringWithFormat:@"%@/%@", documentsDirectory, fileName];
  return fullPath;
}

//check to see if the error is "No such file or directory"
- (BOOL)isFileNotExistError:(NSError*)error {
  if (error == nil) {
    return NO;
  }
  
  NSError* underlyingError = [error.userInfo objectForKey:NSUnderlyingErrorKey];
  if ([underlyingError.domain isEqualToString:NSPOSIXErrorDomain]
      && underlyingError.code == ENOENT) {
    return YES;
  }else {
    return NO;
  }
}

- (id)loadJsonObjectFromFile:(NSString*)fileName {
  NSString* filePath = [self documentPathForFile:fileName];
  NSError* error = nil;
  NSData* jsonData = [NSData dataWithContentsOfFile:filePath
                                            options:NSDataReadingMappedIfSafe
                                              error:&error];
  if (error != nil && ![self isFileNotExistError:error]) {
    NSLog(@"[Error]Failed to load %@: %@",
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
    NSLog(@"[Error]Failed to serialize %@: %@",
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
    NSLog(@"[ERROR]Failed to serialize %@ to NSData: %@", fileName ,jsonError);
    return jsonError;
  }
  NSAssert(jsonData != nil, @"jsonData should not be nil!");
  
  NSError* saveError = nil;
  [jsonData writeToFile:[self documentPathForFile:fileName]
                options:NSDataWritingFileProtectionComplete
                  error:&saveError];
  if (saveError) {
    NSLog(@"[ERROR]Failed to save %@: %@", fileName ,saveError);
  }else {
    NSLog(@"Succeeded to save %@.", fileName);    
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
  if (self.eventsDict == nil) {
    NSDictionary* dict = [self loadJsonObjectFromFile:kAllEventsFileName];
    NSAssert(!(dict != nil && ![dict isKindOfClass:[NSDictionary class]]),
             @"dict should be a dictionary!");
    
    NSMutableDictionary* allEventsDict = [NSMutableDictionary dictionary];
    for (NSString* definitionId in dict) {
      id events = [dict objectForKey:definitionId];
      [allEventsDict setObject:[self deserializedEvents:events] forKey:definitionId];
    }      
    self.eventsDict = allEventsDict;
  }
}

- (void)fetchPendingEventsIfNecessary {
  if (self.pendingEvents == nil) {
    NSArray* events = [self loadJsonObjectFromFile:kPendingEventsFileName];
    NSAssert(!(events != nil && ![events isKindOfClass:[NSArray class]]),
             @"events should be an array");

    NSMutableArray* pendingEvents = [NSMutableArray array];
    if (events != nil) {
      pendingEvents = [self deserializedEvents:events];
    }
    self.pendingEvents = pendingEvents;
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
  //If eventsDict is never loaded, then no need to save anything
  if (self.eventsDict == nil) {
    return;
  }
  
  NSMutableDictionary* jsonDict = [NSMutableDictionary dictionary];
  for (NSString* definitionId in self.eventsDict) {
    NSMutableArray* eventsArr = [self jsonArrayFromEvents:[self.eventsDict objectForKey:definitionId]];
    NSAssert(eventsArr != nil, @"eventsArr should not be nil!");
    [jsonDict setObject:eventsArr forKey:definitionId];
  }
  [self saveJsonObject:jsonDict toFile:kAllEventsFileName];
}


- (void)savePendingEventsToFile {
  //If pendingEvents is never loaded, then no need to save anything
  if (self.pendingEvents == nil) {
    return;
  }
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
    NSLog(@"[WARNING]: events list should not be empty!");
    return;
  }
  
  @synchronized(self) {
    NSAssert(self.pendingEvents != nil, @"pending events should have already loaded!");
    for (PacoEvent* event in events) {
      int index = [self.pendingEvents indexOfObject:event];
      if (index == NSNotFound) {
        NSLog(@"[ERROR]: Can't mark event complete since it's not in the pending events list!");
      }
      [self.pendingEvents removeObject:event];
    }
    
    NSLog(@"[Mark Complete] %d events! ", [events count]);
    NSLog(@"[Pending Events] %d.", [self.pendingEvents count]);
  }
}



#pragma mark Public API
- (void)saveEvent:(PacoEvent*)event {
  NSAssert(event != nil, @"nil event cannot be saved!");
  NSAssert([[event.experimentId
             stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]]
            length] > 0, @"experimentId should not be empty!");
  
  [self fetchAllEventsIfNecessary];
  
  NSMutableArray* events = [self.eventsDict objectForKey:event.experimentId];
  if (events == nil) {
    events = [NSMutableArray array];
  }
  [events addObject:event];
  [self.eventsDict setObject:events forKey:event.experimentId];
  
  //add this event to pendingEvent list too
  [self fetchPendingEventsIfNecessary];
  [self.pendingEvents addObject:event];
  
  //submit this event to server
  [self startUploadingEvents];
}

- (void)saveJoinEventWithDefinition:(PacoExperimentDefinition*)definition
                       withSchedule:(PacoExperimentSchedule*)schedule {
  PacoEvent* joinEvent = [PacoEvent joinEventForDefinition:definition withSchedule:schedule];
  [self saveEvent:joinEvent];
}

//YMZ:TODO: should we remove all the events for a stopped experiment?
- (void)saveStopEventWithExperiment:(PacoExperiment*)experiment {
  PacoEvent* event = [PacoEvent stopEventForExperiment:experiment];
  [self saveEvent:event];
}

- (void)saveSelfReportEventWithDefinition:(PacoExperimentDefinition*)definition
                                andInputs:(NSArray*)visibleInputs {
  PacoEvent* surveyEvent = [PacoEvent selfReportEventForDefinition:definition
                                                        withInputs:visibleInputs];
  [self saveEvent:surveyEvent];
}


- (void)saveSurveySubmittedEventForDefinition:(PacoExperimentDefinition*)definition
                                   withInputs:(NSArray*)inputs
                             andScheduledTime:(NSDate*)scheduledTime {
  PacoEvent* surveyEvent = [PacoEvent surveySubmittedEventForDefinition:definition
                                                             withInputs:inputs
                                                       andScheduledTime:scheduledTime];
  [self saveEvent:surveyEvent];
}


- (void)saveSurveyMissedEventForDefinition:(PacoExperimentDefinition*)definition
                         withScheduledTime:(NSDate*)scheduledTime {
  PacoEvent* surveyEvent = [PacoEvent surveyMissedEventForDefinition:definition
                                                   withScheduledTime:scheduledTime];
  [self saveEvent:surveyEvent];  
}

- (void)saveDataToFile {
  [self savePendingEventsToFile];
  [self saveAllEventsToFile];
}

- (void)startUploadingEvents {
  [self.uploader startUploading];
}
- (void)stopUploadingEvents {
  [self.uploader stopUploading];
}




@end
