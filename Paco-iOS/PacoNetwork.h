//
//  PacoNetwork.h
//  Paco
//
//  Created by northropo on 10/15/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DDLog.h"
#import <UIKit/UIKit.h>

@class PacoAuthenticator;
@class PacoLocation;
@class PacoService;
@class Reachability;

typedef void(^PacoRefreshCompletionBlock)(NSError* error);
typedef void(^LoginCompletionBlock)(NSError* error);
#define SERVER_DOMAIN_FLAG 2


@interface PacoNetwork : NSObject

@property (nonatomic, retain, readonly) PacoAuthenticator *authenticator;
@property (nonatomic, retain, readonly) PacoLocation *location;
@property (nonatomic,strong) NSString* serverDomain;
@property (nonatomic, retain, readonly) PacoService *service;
@property (nonatomic, strong, readonly) Reachability* reachability;

- (BOOL)isLoggedIn;
- (NSString*)userEmail;

+ (PacoNetwork*)sharedInstance;

- (void)loginWithCompletionBlock:(LoginCompletionBlock)block;
- (void)loginWithOAuth2CompletionHandler:(void (^)(NSError *))completionHandler;
- (void)uploadPendingEventsInBackground;
- (void)configurePacoServerAddress:(NSString *)serverAddress;
- (BOOL)isLoggedIn;


@end
