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
#import <UIKit/UIKit.h>

@class PacoAuthenticator;
@class PacoLocation;
@class PacoService;
@class Reachability;

typedef void(^PacoRefreshCompletionBlock)(NSError* error);
typedef void(^LoginCompletionBlock)(NSError* error);


#define SERVER_DOMAIN_FLAG 0


@interface PacoNetwork : NSObject

@property (nonatomic, retain, readonly) PacoAuthenticator *authenticator;
@property (nonatomic, retain, readonly) PacoLocation *location;
@property (nonatomic,strong) NSString* serverDomain;
@property (nonatomic, retain, readonly) PacoService *service;
@property (nonatomic, strong, readonly) Reachability* reachability;
@property  BOOL isFetching;



- (BOOL)isLoggedIn;
- (NSString*)userEmail;

+ (PacoNetwork*)sharedInstance;

- (void)loginWithCompletionBlock:(LoginCompletionBlock)block;
- (void)loginWithOAuth2CompletionHandler:(void (^)(NSError *))completionHandler;
- (void)uploadPendingEventsInBackground;
- (void)configurePacoServerAddress:(NSString *)serverAddress;
- (void) update;
- (void) hudReload;





@end
