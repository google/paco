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

#import <AppAuth/AppAuth.h>
#import <GTMAppAuth/GTMAppAuth.h>
#import "GTMAppAuthFetcherAuthorization.h"
#import "GTMOAuth2KeychainCompatibility.h"
#import "PacoAppDelegate.h"

#import "PacoClient.h"

NSString * const gtmAppAuthKeyChainName = @"PacoGTMAppAuthKeychain";
NSString * const oldKeyChainName = @"PacoKeychain2";

/*! @brief The OIDC issuer from which the configuration will be discovered.
 */
static NSString *const kIssuer = @"https://accounts.google.com";

/*! @brief The OAuth client ID.
 @discussion For Google, register your client at
 https://console.developers.google.com/apis/credentials?project=_
 The client should be registered with the "iOS" type.
 */
static NSString *const kClientID = @"619519633889-f79aogqhj44eut1u75e8jaa5eav8p3eu.apps.googleusercontent.com";

/*! @brief The OAuth redirect URI for the client @c kClientID.
 @discussion With Google, the scheme of the redirect URI is the reverse DNS notation of the
 client ID. This scheme must be registered as a scheme in the project's Info
 property list ("CFBundleURLTypes" plist key). Any path component will work, we use
 'oauthredirect' here to help disambiguate from any other use of this scheme.
 */
static NSString *const kRedirectURI =
@"com.googleusercontent.apps.619519633889-f79aogqhj44eut1u75e8jaa5eav8p3eu:/oauthredirect";

/*! @brief @c NSCoding key for the authState property.
 */
static NSString *const kExampleAuthorizerKey = @"authorization";

typedef void (^PacoAuthenticationBlock)(NSError *);

@interface PacoAuthenticator ()
//@property(nonatomic, readwrite, retain) GTMOAuth2ViewControllerTouch *authUI;
@property(nonatomic, readwrite, retain) GTMAppAuthFetcherAuthorization *auth;
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
  GTMAppAuthFetcherAuthorization *authorization = [GTMAppAuthFetcherAuthorization authorizationFromKeychainForName:gtmAppAuthKeyChainName];
  if (!authorization) {
    NSLog(@"No email stored in Keychain");
    return nil;
  }
  return authorization.userEmail;
}

- (BOOL)isUserAccountStored {
  NSString* email = [self fetchUserEmailFromKeyChain];
  return [email length] > 0;
}

- (BOOL)hasAccountInKeyChain {
  GTMAppAuthFetcherAuthorization *authorization = [GTMAppAuthFetcherAuthorization authorizationFromKeychainForName:gtmAppAuthKeyChainName];
  return authorization;
}

- (void)deleteAllAccountsFromKeyChain {
  [GTMAppAuthFetcherAuthorization removeAuthorizationFromKeychainForName:gtmAppAuthKeyChainName];
}

- (void)deleteAccount {
  self.accountEmail = nil;
  if ([self hasAccountInKeyChain]) {
    [self deleteAllAccountsFromKeyChain];
  }
}

- (BOOL)isLoggedIn {
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
  [self authenticateWithGTMAppAuthWithCompletionHandler:completionBlock];
}

#pragma mark - GTMAppAuth

- (void)authenticateWithGTMAppAuthWithCompletionHandler:(void (^)(NSError *))completionHandler {
  // Attempt to deserialize from Keychain in GTMAppAuth format.
  GTMAppAuthFetcherAuthorization *authorization = [GTMAppAuthFetcherAuthorization authorizationFromKeychainForName:gtmAppAuthKeyChainName];
  self.auth = authorization;

  
  // If no data found in the new format, try to deserialize data from GTMOAuth2
  if (!authorization) {
    // Tries to load the data serialized by GTMOAuth2 using old keychain name.
    // If you created a new client id, be sure to use the *previous* client id and secret here.
    authorization =
    [GTMOAuth2KeychainCompatibility authForGoogleFromKeychainForName:oldKeyChainName
                                                            clientID:@"1051938716780.apps.googleusercontent.com"
                                                        clientSecret:@""];
    if (authorization) {
      // Remove previously stored GTMOAuth2-formatted data.
      [GTMOAuth2KeychainCompatibility removeAuthFromKeychainForName:oldKeyChainName];
      // Serialize to Keychain in GTMAppAuth format.
      [GTMAppAuthFetcherAuthorization saveAuthorization:(GTMAppAuthFetcherAuthorization *)authorization
                                      toKeychainForName:gtmAppAuthKeyChainName];
      if (completionHandler) {
        completionHandler(nil);
      }
    } else {
              NSURL *issuer = [NSURL URLWithString:kIssuer];
        NSURL *redirectURI = [NSURL URLWithString:kRedirectURI];
        
        NSLog(@"Fetching configuration for issuer: %@", issuer);
        
        // discovers endpoints
        [OIDAuthorizationService
         discoverServiceConfigurationForIssuer:issuer
         completion:^(OIDServiceConfiguration *_Nullable configuration, NSError *_Nullable error) {
           
           if (!configuration) {
             NSLog(@"Error retrieving discovery document: %@", [error localizedDescription]);
             self.auth = nil;
             return;
           }
           
           NSLog(@"Got configuration", nil);
           
           // builds authentication request
           OIDAuthorizationRequest *request =
             [[OIDAuthorizationRequest alloc] initWithConfiguration:configuration
                                                         clientId:kClientID
                                                           scopes:@[OIDScopeEmail]
                                                      redirectURL:redirectURI
                                                     responseType:OIDResponseTypeCode
                                             additionalParameters:nil];
           // performs authentication request
           PacoAppDelegate *appDelegate = (PacoAppDelegate *)[[UIApplication sharedApplication] delegate];
           NSLog(@"Initiating authorization request with scope: %@", request.scope);
           
           UIViewController *controller = [UIApplication sharedApplication].keyWindow.rootViewController;
                         
           appDelegate.currentAuthorizationFlow =
              [OIDAuthState authStateByPresentingAuthorizationRequest:request
                                          presentingViewController:controller
                                                          callback:^(OIDAuthState *_Nullable authState,
                                                                     NSError *_Nullable error) {
                                                            if (authState) {
                                                              GTMAppAuthFetcherAuthorization *authorization =
                                                              [[GTMAppAuthFetcherAuthorization alloc] initWithAuthState:authState];
                                                              
                                                              self.auth = authorization;
                                                              NSLog(@"Got authorization tokens. ", nil); //authState.lastTokenResponse.accessToken);
                                                              [GTMAppAuthFetcherAuthorization saveAuthorization:(GTMAppAuthFetcherAuthorization *)authorization
                                                                                              toKeychainForName:gtmAppAuthKeyChainName];
                                                              NSLog(@"PACO OAUTH2 LOGIN AUTH SUCCEEDED [%@]", authorization.authState.refreshToken);
                                                              self.userLoggedIn = YES;
                                                              if (completionHandler) {
                                                                completionHandler(nil);
                                                              }
                                                              
                                                            } else {
                                                              self.auth = nil;
                                                              NSLog(@"Authorization error: %@", [error localizedDescription]);
                                                              NSLog(@"PACO OAUTH2 LOGIN AUTH FAILED [%@]", error);
                                                              self.userLoggedIn = NO;
                                                            }
                                                          }];
         }];
      
    }
  } else {
    self.auth = authorization;
    if (completionHandler) {
      completionHandler(nil);
    }
  }
}

@end
