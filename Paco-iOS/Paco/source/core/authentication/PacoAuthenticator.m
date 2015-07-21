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

#import "PacoAuthenticator.h"

#import "GTMOAuth2Authentication.h"
#import "GTMOAuth2SignIn.h"
#import "GTMOAuth2ViewControllerTouch.h"
#import "PacoClient.h"
#import "SSKeychain.h"


NSString* const kPacoService = @"com.google.paco";


typedef void (^PacoAuthenticationBlock)(NSError *);

@interface PacoAuthenticator ()
@property(nonatomic, readwrite, retain) GTMOAuth2ViewControllerTouch *authUI;
@property(nonatomic, readwrite, retain) GTMOAuth2Authentication *auth;
@property(nonatomic, readwrite, copy) PacoAuthenticationBlock completionHandler;
@property(nonatomic, readwrite, assign) BOOL userLoggedIn;

@property(nonatomic, readwrite, strong) NSString* accountEmail;


@end

@implementation PacoAuthenticator

- (id)initWithFirstLaunchFlag:(BOOL)firstLaunch {
  self = [super init];
  if (self) {
    if (firstLaunch) {
      [self deleteAccount];
    }
  }
  return self;
}

#pragma mark - log in status
- (NSString*)fetchUserEmailFromKeyChain {
  NSArray* accounts = [SSKeychain accountsForService:kPacoService];
  if (0 == [accounts count]) {
    NSLog(@"No email stored in Keychain");
    return nil;
  }
  NSAssert([accounts count] == 1, @"should only have one account!");
  NSDictionary* accountDict = accounts[0];
  NSString* email = accountDict[kSSKeychainAccountKey];
  NSLog(@"Fetched an email from Keychain");
  return email;
}

- (BOOL)isUserAccountStored {
  NSString* email = [self fetchUserEmailFromKeyChain];
  NSString* pwd = [SSKeychain passwordForService:kPacoService account:email];
  if ([email length] > 0 && [pwd length] > 0) {
    return YES;
  }
  return NO;
}

- (BOOL)hasAccountInKeyChain {
  NSArray* accounts = [SSKeychain accountsForService:kPacoService];
  return [accounts count] > 0;
}

- (void)deleteAllAccountsFromKeyChain {
  NSArray* accounts = [NSArray arrayWithArray:[SSKeychain accountsForService:kPacoService]];
  for (NSDictionary* accountDict in accounts) {
    NSString* email = accountDict[kSSKeychainAccountKey];
    BOOL success = [SSKeychain deletePasswordForService:kPacoService account:email];
    if (!success) {
      NSLog(@"[ERROR] Failed to delete password and account in keychain!");
    }
  }
}

- (void)deleteAccount {
  self.accountEmail = nil;
  if ([self hasAccountInKeyChain]) {
    [self deleteAllAccountsFromKeyChain];
  }
  // Remove auth from keychain to prevent future auto sign-in as (null).
  [GTMOAuth2ViewControllerTouch removeAuthFromKeychainForName:@"PacoKeychain2"];
  [GTMOAuth2ViewControllerTouch revokeTokenForGoogleAuthentication:self.auth];
}

- (BOOL)isLoggedIn
{
  return self.userLoggedIn;
}

- (NSString*)userEmail {
  if (self.accountEmail == nil) {
    self.accountEmail = [self fetchUserEmailFromKeyChain];
  }
  return self.accountEmail;
}

- (void)invalidateCurrentAccount {
  self.userLoggedIn = NO;
  [self deleteAccount];
}

- (void)reAuthenticateWithBlock:(void(^)(NSError*))completionBlock {
  [self authenticateWithOAuth2WithCompletionHandler:completionBlock];
}


#pragma mark - OAuth2

- (void)authenticateWithOAuth2WithCompletionHandler:(void (^)(NSError *))completionHandler {
  // Standard OAuth2 login flow.
  // See: https://code.google.com/apis/console/#project:406945030854:access
  
  NSString *scopes = @"https://www.googleapis.com/auth/userinfo.email";
  NSString *clientId = @"1051938716780.apps.googleusercontent.com";

  // ispiro: Apparently the clientSecret parameter can be empty and auth still succeeds.
  NSString *clientSecret = @"";
  GTMOAuth2Authentication *keychainAuth =
      [GTMOAuth2ViewControllerTouch
          authForGoogleFromKeychainForName:@"PacoKeychain2"
          clientID:clientId
          clientSecret:clientSecret];
  
  if (keychainAuth && (keychainAuth.parameters)[@"refresh_token"]) {
    self.auth = keychainAuth;
    if (completionHandler) {
      completionHandler(nil);
    }
    return;
  }


  _authUI = [[GTMOAuth2ViewControllerTouch alloc]
      initWithScope:scopes
      clientID:clientId
      clientSecret:clientSecret
      keychainItemName:@"PacoKeychain2"
      completionHandler:^(GTMOAuth2ViewControllerTouch *viewController,
                          GTMOAuth2Authentication *auth,
                          NSError *error) {
          BOOL result = [GTMOAuth2ViewControllerTouch saveParamsToKeychainForName:@"PacoKeychain2"
                                                                   authentication:auth];
          assert(result);
          // TODO(ispiro): If user presses cancel at the final screen, assert will fail. Should return to splash screen.
          self.auth = auth;
          if (auth && !error) {
            [SSKeychain setPassword:@""
                         forService:kPacoService
                            account:auth.userEmail];
            NSLog(@"PACO OAUTH2 LOGIN AUTH SUCCEEDED [%@]", auth.tokenURL.absoluteString);
            self.userLoggedIn = YES;
          } else {
            NSLog(@"PACO OAUTH2 LOGIN AUTH FAILED [%@]", error);
            self.userLoggedIn = NO;
          }
          if (completionHandler) {
            completionHandler(nil);
          }
          // TODO(ispiro): Find a way to hide this window faster.
          [[UIApplication sharedApplication].keyWindow.rootViewController
              dismissViewControllerAnimated:NO completion:^{}];
      }];
  [[UIApplication sharedApplication].keyWindow.rootViewController
      presentViewController:_authUI animated:NO completion:^{
        self.authUI = nil;
      }];
}

@end
