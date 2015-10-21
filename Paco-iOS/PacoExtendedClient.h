//
//  PacoExtendedClient.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/12/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

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
