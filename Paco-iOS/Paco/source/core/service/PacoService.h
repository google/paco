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

#import <Foundation/Foundation.h>

@class PacoAuthenticator;
@class PacoEvent;
@class PacoExperimentDefinition;
@class PacoExperimentSchedule;

@interface PacoService : NSObject

@property (nonatomic, retain) PacoAuthenticator *authenticator;

// Load all experiement definitions from the server.
- (void)loadAllExperimentsWithCompletionHandler:(void (^)(NSArray *, NSError *))completionHandler;

// Load the events for the user for a given experiment
- (void)loadEventsForExperiment:(PacoExperimentDefinition *)experiment
          withCompletionHandler:(void (^)(NSArray *, NSError *))completionHandler;

// Join an Experiment
- (void)joinExperiment:(PacoExperimentDefinition *)experiment
              schedule:(PacoExperimentSchedule *)schedule
     completionHandler:(void (^)(PacoEvent *, NSError *))completionHandler;

// Submit an event to the server.
- (void)submitEvent:(PacoEvent *)event withCompletionHandler:(void (^)(NSError *))completionHandler;

// Submit question answers to the server.
- (void)submitAnswers:(PacoExperimentDefinition *)experiment
    completionHandler:(void (^)(NSError *))completionHandler;
@end
