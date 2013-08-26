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
@class PacoLocation;
@class PacoModel;
@class PacoScheduler;
@class PacoService;
@class PacoExperiment;
@class PacoExperimentDefinition;
@class Reachability;
@class PacoEventManager;

//production server: 0
//local server: 1
#define SERVER_DOMAIN_FLAG 0


@interface PacoClient : NSObject

@property (nonatomic, retain, readonly) PacoAuthenticator *authenticator;
@property (nonatomic, retain, readonly) PacoLocation *location;
@property (nonatomic, retain, readonly) PacoModel *model;
@property (nonatomic, strong, readonly) PacoEventManager* eventManager;
@property (nonatomic, retain, readonly) PacoScheduler *scheduler;
@property (nonatomic, strong, readonly) Reachability* reachability;
@property (nonatomic, retain, readonly) PacoService *service;
@property (nonatomic, retain, readonly) NSString *serverDomain;

//YMZ: the following needs to be removed after we migrate to OAuth2
@property (nonatomic, retain, readonly) NSString* userEmail;

+ (PacoClient *)sharedInstance;

- (BOOL)isLoggedIn;

- (BOOL)hasJoinedExperimentWithId:(NSString*)definitionId;

- (BOOL)isUserAccountStored;
- (void)loginWithClientLogin:(NSString *)email
                    password:(NSString *)password
           completionHandler:(void (^)(NSError *))completionHandler;
- (void)loginWithCompletionHandler:(void (^)(NSError *))completionHandler;


- (void)loginWithOAuth2CompletionHandler:(void (^)(NSError *))completionHandler;

- (BOOL)prefetchedDefinitions;
- (NSError*)errorOfPrefetchingDefinitions;
- (BOOL)prefetchedExperiments;
- (NSError*)errorOfPrefetchingexperiments;

- (void)deleteExperimentFromCache:(PacoExperiment*)experiment;

- (void)startLocationTimerIfNeeded;
@end
