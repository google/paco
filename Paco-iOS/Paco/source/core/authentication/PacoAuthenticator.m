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

typedef void (^PacoAuthenticationBlock)(NSError *);

@interface PacoAuthenticator () <GoogleClientLoginDelegate>
@property(nonatomic, readwrite, retain) GoogleAppEngineAuth *appEngineAuth;
@property(nonatomic, readwrite, retain) GTMOAuth2ViewControllerTouch *authUI;
@property(nonatomic, readwrite, retain) GTMOAuth2Authentication *auth;
@property(nonatomic, readwrite, copy) PacoAuthenticationBlock completionHandler;
@property(nonatomic, readwrite, copy) NSString *cookie;
@property(nonatomic, readwrite, assign) BOOL userLoggedIn;
@end

@implementation PacoAuthenticator


#pragma mark - ClientLogin

- (void)authenticateWithClientLogin:(NSString *)email
                           password:(NSString *)password
                  completionHandler:(void (^)(NSError *))completionHandler {
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


- (BOOL)isLoggedIn
{
  //YMZ:TODO:
  return self.userLoggedIn;
}


#pragma mark - GoogleClientLoginDelegate

-(void)authSucceeded:(NSString *)authKey {
  NSLog(@"PACO CLIENT LOGIN AUTH SUCCEEDED [%@]", authKey);
  self.userLoggedIn = YES;
  
  self.cookie = [NSString stringWithFormat:@"SACSID=%@", authKey];
  if (self.completionHandler) {
    self.completionHandler(nil);
  }
}

-(void)authFailed:(NSString *)error {
  NSLog(@"PACO CLIENT LOGIN AUTH FAILED [%@]", error);
  self.userLoggedIn = NO;
  
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
