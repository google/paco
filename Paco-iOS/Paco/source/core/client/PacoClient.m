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

#import "PacoClient.h"

#import "PacoAuthenticator.h"
#import "PacoLocation.h"
#import "PacoModel.h"
#import "PacoScheduler.h"
#import "PacoService.h"

@interface PacoClient ()
@property (retain, readwrite) PacoAuthenticator *authenticator;
@property (retain, readwrite) PacoLocation *location;
@property (retain, readwrite) PacoModel *model;
@property (retain, readwrite) PacoScheduler *scheduler;
@property (retain, readwrite) PacoService *service;
- (void)prefetch;
@end

@implementation PacoClient

@synthesize authenticator;
@synthesize location;
@synthesize model;
@synthesize scheduler;
@synthesize service;

+ (PacoClient *)sharedInstance {
  static PacoClient *client = nil;
  if (!client) {
    client = [[PacoClient alloc] init];
  }
  return client;
}

- (id)init {
  self = [super init];
  if (self) {
    self.authenticator = [[PacoAuthenticator alloc] init];
    self.location = nil;//[[PacoLocation alloc] init];
    self.scheduler = [[PacoScheduler alloc] init];
    self.service = [[PacoService alloc] init];
    
  }
  return self;
}

- (void)loginWithClientLogin:(NSString *)email
                    password:(NSString *)password
           completionHandler:(void (^)(NSError *))completionHandler {
  [self.authenticator authenticateWithClientLogin:email//@"paco.test.gv@gmail.com"
                                         password:password//@"qwertylkjhgf"
                                completionHandler:^(NSError *error) {
      if (!error) {
        // Authorize the service.
        self.service.authenticator = self.authenticator;
        // Fetch the experiment definitions and the events of joined experiments.
        [self prefetch];
        completionHandler(nil);
      } else {
        completionHandler(error);
      }
  }];
}

- (void)loginWithOAuth2CompletionHandler:(void (^)(NSError *))completionHandler {
  [self.authenticator authenticateWithOAuth2WithCompletionHandler:^(NSError *error) {
      if (!error) {
        // Authorize the service.
        self.service.authenticator = self.authenticator;
        // Fetch the experiment definitions and the events of joined experiments.
        [self prefetch];
        completionHandler(nil);
      } else {
        completionHandler(error);
      }
  }];
}

- (void)prefetch {
  [self refreshModelWithCompletionHandler:^(NSError *error) {
    if (error) {
      NSLog(@"Error on prefetch %@", error);
    }
  }];
}

- (void)refreshModelWithCompletionHandler:(void (^)(NSError *))completionHandler {
  if (!self.service) {
    self.service = [[PacoService alloc] init];
  }
  // Authorize the service.
  self.service.authenticator = self.authenticator;

  // Load the experiment definitions.
  self.model = [PacoModel pacoModelFromFile];
  if (!self.model) {
    [self.service loadAllExperimentsWithCompletionHandler:^(NSArray *experiments, NSError *error) {
        if (!error) {
          NSLog(@"Loaded %d experiments", [experiments count]);
          // Convert the JSON response into an object model.
          self.model = [PacoModel pacoModelFromDefinitionJSON:experiments
                                                 instanceJSON:nil];

          // Load events for each known experiment, if events exist then this
          // will indicate that the user has joined this experiment.
          
          //for (PacoExperiment *experiment in self.model.experimentInstances) {
          for (PacoExperimentDefinition *experimentDefinition in self.model.experimentDefinitions) {
          
              [self.service loadEventsForExperiment:experimentDefinition
                              withCompletionHandler:^(NSArray *events, NSError *error) {
                  if ([events count]) {
                    NSLog(@"\tFound %d events in experiment \"%@\"", [events count], experimentDefinition.title);
                    // Convert the JSON events into event objects.
                    NSMutableArray *pacoEvents = [NSMutableArray array];
                    for (id jsonEvent in events) {
                      PacoEvent *pacoEvent = [PacoEvent pacoEventFromJSON:jsonEvent];
                      [pacoEvents addObject:pacoEvent];
                    }
                    if ([pacoEvents count]) {
                      NSArray *instances = [[PacoClient sharedInstance].model instancesForExperimentId:experimentDefinition.experimentId];
                      // Expecting no existing instances in model
                      assert([instances count] == 0);
                      //need to split events out into each instance via the experiment name == experiment instance id

                      NSMutableDictionary *map = [NSMutableDictionary dictionary];
                      for (PacoEvent *event in pacoEvents) {
                        NSString *instanceId = event.experimentName;
                        NSMutableArray *instanceEvents = [map objectForKey:instanceId];
                        if (!instanceEvents) {
                          instanceEvents = [NSMutableArray array];
                          [map setObject:instanceEvents forKey:instanceId];
                        }
                        [instanceEvents addObject:event];
                      }

                      // Make experiment instances for each instance id.
                      NSArray *instanceIds = [map allKeys];
                      NSLog(@"FOUND %d INSTANCES OF EXPERIMENT %@ %@", instanceIds.count, experimentDefinition.experimentId, experimentDefinition.title);

                      for (NSString *instanceId in instanceIds) {
                        NSArray *instanceEvents = [map objectForKey:instanceId];
                        NSLog(@"\tFOUND %d EVENTS FOR INSTANCE %@", instanceEvents.count, instanceId);
                        PacoExperiment *experiment =
                            [[PacoClient sharedInstance].model
                                addExperimentInstance:experimentDefinition
                                             schedule:experimentDefinition.schedule
                                               events:instanceEvents];

                        // Use the instance id from the sorted event map.
                        experiment.instanceId = instanceId;
                      }
                    }
                  }
              }];
          }
          completionHandler(nil);
        } else {
          completionHandler(error);
        }
    }];
  } else {
    completionHandler(nil);
  }
}


@end
