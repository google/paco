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



@class GTMOAuth2Authentication;

@interface PacoAuthenticator : NSObject

@property(nonatomic, readonly, retain) GTMOAuth2Authentication *auth;

- (id)initWithFirstLaunchFlag:(BOOL)firstLaunch;

- (void)reAuthenticateWithBlock:(void(^)(NSError*))completionBlock;

- (void)authenticateWithOAuth2WithCompletionHandler:(void (^)(NSError *))completionHandler;

- (BOOL)isLoggedIn;

- (BOOL)isUserAccountStored;

- (NSString*)userEmail;

- (void)invalidateCurrentAccount;
@end

