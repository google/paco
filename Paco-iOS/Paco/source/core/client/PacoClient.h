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
#import "DDLog.h"

@class PacoAuthenticator;
@class PacoLocation;
@class PacoModel;
@class PacoScheduler;
@class PacoService;
@class PacoExperiment;
@class PacoExperimentDefinition;
@class Reachability;
@class PacoEventManager;
@class PacoExperimentSchedule;



#ifdef DEBUG
static const int ddLogLevel = LOG_LEVEL_VERBOSE;
#else
static const int ddLogLevel = LOG_LEVEL_VERBOSE;
#endif


//production server: 0
//local server: 1
//staging server: 2
#define SERVER_DOMAIN_FLAG 0


#define IS_IOS_7 ([[[UIDevice currentDevice] systemVersion] floatValue] >= 7.0)

typedef void(^PacoRefreshCompletionBlock)(NSError* error);
typedef void(^LoginCompletionBlock)(NSError* error);

@interface PacoClient : NSObject

@property (nonatomic, retain, readonly) PacoAuthenticator *authenticator;
@property (nonatomic, retain, readonly) PacoLocation *location;
@property (nonatomic, retain, readonly) PacoModel *model;
@property (nonatomic, strong, readonly) PacoEventManager* eventManager;
@property (nonatomic, retain, readonly) PacoScheduler *scheduler;
@property (nonatomic, strong, readonly) Reachability* reachability;
@property (nonatomic, retain, readonly) PacoService *service;
@property (nonatomic, retain, readonly) NSString *serverDomain;
@property (nonatomic, assign, readonly) BOOL firstLaunch;
@property (nonatomic, assign, readonly) BOOL firstOAuth2;


+ (PacoClient *)sharedInstance;

//server domain without the prefix of https:// or http://
- (NSString*)serverAddress;

- (NSString*)userEmail;
- (NSString*)userName;

- (BOOL)isLoggedIn;

//call this method when we get authentication error
//1. Set isLoggedIn to NO
//2. delete cookie and account in keychain
//3. pop up the log-in dialog to ask user re-logIn
- (void)invalidateUserAccount;

- (void)loginWithCompletionBlock:(LoginCompletionBlock)block;

- (void)loginWithOAuth2CompletionHandler:(void (^)(NSError *))completionHandler;

- (BOOL)hasJoinedExperimentWithId:(NSString*)definitionId;

- (void)backgroundFetchStartedWithBlock:(void(^)(UIBackgroundFetchResult))completionBlock;

- (void)executeRoutineMajorTaskIfNeeded;
- (void)uploadPendingEventsInBackground;

- (void)joinExperimentWithDefinition:(PacoExperimentDefinition*)definition
                            schedule:(PacoExperimentSchedule*)schedule
                     completionBlock:(void(^)())completionBlock;

- (void)changeScheduleForExperiment:(PacoExperiment*)experiment
                        newSchedule:(PacoExperimentSchedule*)newSchedule
                    completionBlock:(void(^)())completionBlock;

- (void)stopExperiment:(PacoExperiment*)experiment withBlock:(void(^)())completionBlock;

- (void)submitSurveyWithDefinition:(PacoExperimentDefinition*)definition
                      surveyInputs:(NSArray*)surveyInputs
                      notification:(UILocalNotification*)notification;

//refresh definitions published to the current user
- (void)refreshMyDefinitionsWithBlock:(PacoRefreshCompletionBlock)completionBlock;

//refresh all running experiments' definitions
- (void)refreshRunningExperimentsWithBlock:(PacoRefreshCompletionBlock)completionBlock;

- (void)configurePacoServerAddress:(NSString *)serverAddress;

@end
