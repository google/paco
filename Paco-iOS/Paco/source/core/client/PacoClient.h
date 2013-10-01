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
#import "PacoLoginScreenViewController.h"

@class PacoAuthenticator;
@class PacoLocation;
@class PacoModel;
@class PacoScheduler;
@class PacoService;
@class PacoExperiment;
@class PacoExperimentDefinition;
@class Reachability;
@class PacoEventManager;

/*
 Set both ADD_TEST_DEFINITION and SKIP_LOG_IN to 1 
 will run the test definition only, and is easier to test notification
 **/
//Load a test experiment definition for test
#define ADD_TEST_DEFINITION 0
//Skip log in flow so that you can focus on testing notification
#define SKIP_LOG_IN 0


//production server: 0
//local server: 1
#define SERVER_DOMAIN_FLAG 0


#define IS_IOS_7 ([[[UIDevice currentDevice] systemVersion] floatValue] >= 7.0)


@interface PacoClient : NSObject

@property (nonatomic, retain, readonly) PacoAuthenticator *authenticator;
@property (nonatomic, retain, readonly) PacoLocation *location;
@property (nonatomic, retain, readonly) PacoModel *model;
@property (nonatomic, strong, readonly) PacoEventManager* eventManager;
@property (nonatomic, retain, readonly) PacoScheduler *scheduler;
@property (nonatomic, strong, readonly) Reachability* reachability;
@property (nonatomic, retain, readonly) PacoService *service;
@property (nonatomic, retain, readonly) NSString *serverDomain;


+ (PacoClient *)sharedInstance;

- (NSString*)userEmail;
- (BOOL)isLoggedIn;

//call this method when we get authentication error
//1. Set isLoggedIn to NO
//2. delete cookie and account in keychain
//3. pop up the log-in dialog to ask user re-logIn
- (void)invalidateUserAccount;

- (BOOL)hasJoinedExperimentWithId:(NSString*)definitionId;

- (void)loginWithCompletionBlock:(LoginCompletionBlock)block;

- (void)loginWithOAuth2CompletionHandler:(void (^)(NSError *))completionHandler;

- (BOOL)prefetchedDefinitions;
- (NSError*)errorOfPrefetchingDefinitions;
- (BOOL)prefetchedExperiments;
- (NSError*)errorOfPrefetchingexperiments;

- (void)deleteExperimentFromCache:(PacoExperiment*)experiment;

- (void)startLocationTimerIfNeeded;
@end
