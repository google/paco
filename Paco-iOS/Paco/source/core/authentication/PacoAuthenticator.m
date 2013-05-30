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

typedef void (^PacoAuthenticationBlock)(NSError *);

@interface PacoAuthenticator () <GoogleClientLoginDelegate>
@property(readwrite, retain) GoogleAppEngineAuth *appEngineAuth;
@property(readwrite, retain) GTMOAuth2ViewControllerTouch *authUI;
@property(readwrite, retain) GTMOAuth2Authentication *auth;
@property(readwrite, copy) PacoAuthenticationBlock completionHandler;
@property(readwrite, copy) NSString *cookie;
@end

@implementation PacoAuthenticator

@synthesize appEngineAuth = appEngineAuth_;
@synthesize auth = auth_;
@synthesize authUI = authUI_;
@synthesize completionHandler = completionHandler_;
@synthesize cookie = cookie_;

#pragma mark - ClientLogin

- (void)authenticateWithClientLogin:(NSString *)email
                           password:(NSString *)password
                  completionHandler:(void (^)(NSError *))completionHandler {
  self.completionHandler = completionHandler;
  appEngineAuth_ = [[GoogleAppEngineAuth alloc] initWithDelegate:self
                                                       andAppURL:[NSURL URLWithString:@"https://quantifiedself.appspot.com"]];
  [appEngineAuth_ authWithUsername:email
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


  authUI_ = [[GTMOAuth2ViewControllerTouch alloc]
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
          } else {
            NSLog(@"PACO OAUTH2 LOGIN AUTH FAILED [%@]", error);
          }
          if (completionHandler) {
            completionHandler(nil);
          }
          
          [[UIApplication sharedApplication].keyWindow.rootViewController
              dismissViewControllerAnimated:NO completion:^{}];
      }];
  [[UIApplication sharedApplication].keyWindow.rootViewController
      presentViewController:authUI_ animated:NO completion:^{
        self.authUI = nil;
      }];
}

#pragma mark - GoogleClientLoginDelegate

-(void)authSucceeded:(NSString *)authKey {
  NSLog(@"PACO CLIENT LOGIN AUTH SUCCEEDED [%@]", authKey);
  self.cookie = [NSString stringWithFormat:@"SACSID=%@", authKey];
  if (self.completionHandler) {
    self.completionHandler(nil);
  }
}

-(void)authFailed:(NSString *)error {
  NSLog(@"PACO CLIENT LOGIN AUTH FAILED [%@]", error);
  if (self.completionHandler) {
    self.completionHandler([NSError errorWithDomain:error code:-1 userInfo:nil]);
  }
}

-(void)authCaptchaTestNeededFor:(NSString *)captchaToken withCaptchaURL:(NSURL *)captchaURL {
  NSLog(@"PACO CLIENT LOGIN AUTH CAPTCHA TEST NEEDED FOR %@ %@", captchaToken, captchaURL);
  if (self.completionHandler) {
    self.completionHandler([NSError errorWithDomain:@"NEEDS CAPTCHA" code:-1 userInfo:nil]);
  }
}

@end
