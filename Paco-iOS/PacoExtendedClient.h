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

#import <Foundation/Foundation.h>
#import "DDLog.h"


@class PacoScheduler;
@class PAExperimentDAO;
@class PASchedule;
@class PacoEventManagerExtended;
@class PacoAuthenticator;
@class PAActionSpecification;
@class Reachability;
@class PacoLocation;
@class PacoAuthenticator;




//production server: 0
//local server: 1
//staging server: 2
#define SERVER_DOMAIN_FLAG 2

#ifdef DEBUG
static const int ddLogLevel = LOG_LEVEL_VERBOSE;
#else
static const int ddLogLevel = LOG_LEVEL_VERBOSE;
#endif




#define IS_IOS_7 ([[[UIDevice currentDevice] systemVersion] floatValue] >= 7.0)



@interface PacoExtendedClient : NSObject

 

@property (nonatomic, strong, readonly) PacoEventManagerExtended* eventManager;
@property (nonatomic, retain, readonly) PacoAuthenticator *authenticator;
@property (nonatomic, retain, readonly) PacoLocation *location;
@property (nonatomic, retain, readonly) PacoScheduler *scheduler;
@property (nonatomic, strong, readonly) Reachability* reachability;
@property (nonatomic, retain, readonly) NSString *serverDomain;




+ (PacoExtendedClient *)sharedInstance;
- (NSString*)userEmail;
 

//call this method when we get authentication error
//1. Set isLoggedIn to NO
//2. delete cookie and account in keychain
//3. pop up the log-in dialog to ask user re-logIn
- (void)invalidateUserAccount;




@end
