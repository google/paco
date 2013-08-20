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

#import "GoogleClientLogin.h"
#import "GoogleAppEngineAuth.h"
#import "GTMOAuth2Authentication.h"
#import "GTMOAuth2SignIn.h"
#import "GTMOAuth2ViewControllerTouch.h"
#import "PacoClient.h"
#import "SSKeychain.h"


NSString* const kPacoService = @"com.google.paco";


typedef void (^PacoAuthenticationBlock)(NSError *);

@interface PacoAuthenticator () <GoogleClientLoginDelegate>
@property(nonatomic, readwrite, retain) GoogleAppEngineAuth *appEngineAuth;
@property(nonatomic, readwrite, retain) GTMOAuth2ViewControllerTouch *authUI;
@property(nonatomic, readwrite, retain) GTMOAuth2Authentication *auth;
@property(nonatomic, readwrite, copy) PacoAuthenticationBlock completionHandler;
@property(nonatomic, readwrite, copy) NSString *cookie;
@property(nonatomic, readwrite, assign) BOOL userLoggedIn;

@property(nonatomic, readwrite, strong) NSString* accountEmail;
@property(nonatomic, readwrite, strong) NSString* accountPassword;


@end

@implementation PacoAuthenticator

#pragma mark - log in status
- (NSString*)fetchUserEmailFromKeyChain {
  NSArray* accounts = [SSKeychain accountsForService:kPacoService];
  if (0 == [accounts count]) {
    return nil;
  }
  NSAssert([accounts count] == 1, @"should only have one account!");
  NSDictionary* accountDict = [accounts objectAtIndex:0];
  NSString* email = [accountDict objectForKey:kSSKeychainAccountKey];
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

- (void)storeAccount {
  NSAssert([self.accountEmail length] > 0 && [self.accountPassword length] > 0,
           @"There isn't any valid user account to stored!");
  
  BOOL success = [SSKeychain setPassword:self.accountPassword
                              forService:kPacoService
                                 account:self.accountEmail];
  if (!success) {
    NSLog(@"[ERROR] Failed to store account in keychain!");
  }
}

- (void)deleteAccount {
  BOOL success = [SSKeychain deletePasswordForService:kPacoService account:self.accountEmail];
  if (!success) {
    NSLog(@"[ERROR] Failed to delete password and account in keychain!");
  }
  self.accountEmail = nil;
  self.accountPassword = nil;  
}

- (BOOL)isLoggedIn
{
  return self.userLoggedIn;
}

- (BOOL)setupWithCookie {
  NSURL* url = [NSURL URLWithString:[PacoClient sharedInstance].serverDomain];
  NSArray* cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookiesForURL:url];
  if (0 == [cookies count]) {
    return NO;
  }
  NSHTTPCookie* cookie = [cookies objectAtIndex:0];
  NSDate* expireDate = cookie.expiresDate;
  if (expireDate == nil) {
    return NO;
  }
  NSTimeInterval interval = [expireDate timeIntervalSinceNow];
  if (interval > 0) {
    self.userLoggedIn = YES;
    self.cookie = cookie.value;
  } else {
    self.userLoggedIn = NO;
    self.cookie = nil;
  }
  return self.userLoggedIn;
}


- (NSString*)userEmail {
  if (self.accountEmail == nil) {
    self.accountEmail = [self fetchUserEmailFromKeyChain];
  }
  return self.accountEmail;
}

- (NSString*)userPassword {
  if (self.accountPassword == nil) {
    self.accountPassword = [SSKeychain passwordForService:kPacoService account:[self userEmail]];
  }
  return self.accountPassword;
}


#pragma mark - ClientLogin

- (void)authenticateWithClientLogin:(NSString *)email
                           password:(NSString *)password
                  completionHandler:(void (^)(NSError *))completionHandler {
  self.accountEmail = email;
  self.accountPassword = password;
  self.completionHandler = completionHandler;
  _appEngineAuth = [[GoogleAppEngineAuth alloc] initWithDelegate:self
                                                       andAppURL:[NSURL URLWithString:[PacoClient sharedInstance].serverDomain]];
  [_appEngineAuth authWithUsername:email
                       andPassword:password
                        withSource:@"Paco-Paco-testIOS"];
}

#pragma mark - OAuth2

- (void)authenticateWithOAuth2WithCompletionHandler:(void (^)(NSError *))completionHandler {
  // Standard OAuth2 login flow.
  // See: https://code.google.com/apis/console/#project:406945030854:access
  
  /*
  
  
Client ID:	
1051938716780.apps.googleusercontent.com
Client secret:	
1tdZTggWAzBo7NgDOx49KFKZ
Redirect URIs:	urn:ietf:wg:oauth:2.0:oob
http://localhost
Application type:	iOS
Bundle ID:	com.paco.Paco
Deep Linking:	Enabled
  
  */
  
  NSString *scopes = @"https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";
  NSString *clientId = @"1051938716780.apps.googleusercontent.com";//@"406945030854.apps.googleusercontent.com";
  NSString *clientSecret = @"";//@"1tdZTggWAzBo7NgDOx49KFKZ";//nil;//@"rD9_oQ5rbubfkgoFYfy0Pcjl";

  GTMOAuth2Authentication *keychainAuth =
      [GTMOAuth2ViewControllerTouch
          authForGoogleFromKeychainForName:@"PacoKeychain2"
          clientID:clientId
          clientSecret:clientSecret];
  
  if (keychainAuth && [keychainAuth.parameters objectForKey:@"refresh_token"]) {
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
          self.auth = auth;
          if (auth && !error) {
            NSLog(@"PACO OAUTH2 LOGIN AUTH SUCCEEDED [%@]", auth.tokenURL.absoluteString);
            self.userLoggedIn = YES;
          } else {
            NSLog(@"PACO OAUTH2 LOGIN AUTH FAILED [%@]", error);
            self.userLoggedIn = NO;
          }
          if (completionHandler) {
            completionHandler(nil);
          }
          
          [[UIApplication sharedApplication].keyWindow.rootViewController
              dismissViewControllerAnimated:NO completion:^{}];
      }];
  [[UIApplication sharedApplication].keyWindow.rootViewController
      presentViewController:_authUI animated:NO completion:^{
        self.authUI = nil;
      }];
}


#pragma mark - GoogleClientLoginDelegate

-(void)authSucceeded:(NSString *)authKey {
  NSLog(@"PACO CLIENT LOGIN AUTH SUCCEEDED [%@]", authKey);
  self.userLoggedIn = YES;
  
  self.cookie = [NSString stringWithFormat:@"SACSID=%@", authKey];
  
  [self storeAccount];

  if (self.completionHandler) {
    self.completionHandler(nil);
  }
}

-(void)authFailed:(NSString *)error {
  NSLog(@"PACO CLIENT LOGIN AUTH FAILED [%@]", error);
  self.userLoggedIn = NO;
  
  [self deleteAccount];
  
  if (self.completionHandler) {
    self.completionHandler([NSError errorWithDomain:error code:-1 userInfo:nil]);
  }
}

-(void)authCaptchaTestNeededFor:(NSString *)captchaToken withCaptchaURL:(NSURL *)captchaURL {
  NSLog(@"PACO CLIENT LOGIN AUTH CAPTCHA TEST NEEDED FOR %@ %@", captchaToken, captchaURL);
  self.userLoggedIn = NO;
  [self deleteAccount];
  if (self.completionHandler) {
    self.completionHandler([NSError errorWithDomain:@"NEEDS CAPTCHA" code:-1 userInfo:nil]);
  }
}

@end
