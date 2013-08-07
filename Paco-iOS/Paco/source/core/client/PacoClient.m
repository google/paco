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

#import "PacoClient.h"

#import "PacoAuthenticator.h"
#import "PacoLocation.h"
#import "PacoModel.h"
#import "PacoScheduler.h"
#import "PacoService.h"
#import "PacoExperimentDefinition.h"
#import "PacoEvent.h"
#import "Reachability.h"
#import "PacoEventManager.h"
#import "PacoEventUploader.h"


static NSString* const kUserEmail = @"PacoClient.userEmail";
static NSString* const kUserPassword = @"PacoClient.userPassword";

@interface PacoPrefetchState : NSObject
@property(atomic, readwrite, assign) BOOL finishLoadingDefinitions;
@property(atomic, readwrite, strong) NSError* errorLoadingDefinitions;

@property(atomic, readwrite, assign) BOOL finishLoadingExperiments;
@property(atomic, readwrite, strong) NSError* errorLoadingExperiments;
@end

@implementation PacoPrefetchState

- (void)reset
{
  self.finishLoadingDefinitions = NO;
  self.errorLoadingDefinitions = NO;
  
  self.finishLoadingExperiments = NO;
  self.errorLoadingExperiments = nil;
}

@end




@interface PacoModel ()
- (BOOL)loadExperimentDefinitionsFromFile;
- (NSError*)loadExperimentInstancesFromFile;
- (void)applyDefinitionJSON:(id)jsonObject;
- (void)deleteExperiment:(PacoExperiment*)experiment;
@end

@interface PacoClient ()
@property (nonatomic, retain, readwrite) PacoAuthenticator *authenticator;
@property (nonatomic, retain, readwrite) PacoLocation *location;
@property (nonatomic, retain, readwrite) PacoModel *model;
@property (nonatomic, retain, readwrite) PacoScheduler *scheduler;
@property (nonatomic, retain, readwrite) PacoService *service;
@property (nonatomic, strong) Reachability* reachability;
@property (nonatomic, retain, readwrite) NSString *serverDomain;
@property (nonatomic, retain, readwrite) NSString* userEmail;
@property (nonatomic, retain, readwrite) PacoPrefetchState *prefetchState;

- (void)prefetch;
@end

@implementation PacoClient

#pragma mark Object Life Cycle
+ (PacoClient *)sharedInstance {
  static PacoClient *client = nil;
  if (!client) {
    client = [[PacoClient alloc] init];
  }
  return client;
}

- (id)init {
  self = [super init];
  if (self) {
    self.authenticator = [[PacoAuthenticator alloc] init];
    self.location = nil;//[[PacoLocation alloc] init];
    self.scheduler = [[PacoScheduler alloc] init];
    self.service = [[PacoService alloc] init];
    _reachability = [Reachability reachabilityWithHostname:@"www.google.com"];
    // Start the notifier, which will cause the reachability object to retain itself!
    [_reachability startNotifier];

    self.model = [[PacoModel alloc] init];
    self.prefetchState = [[PacoPrefetchState alloc] init];
    
    if (SERVER_DOMAIN_FLAG == 0) {//production
      self.serverDomain = @"https://quantifiedself.appspot.com";
    }else{//localserver
      self.serverDomain = @"http://127.0.0.1";
    }
  }
  return self;
}

#pragma mark Public methods
- (BOOL)isLoggedIn
{
  return [self.authenticator isLoggedIn];
}


//YMZ: TODO: we need to store user email and address inside keychain
//However, if we migrate to OAuth2, it looks like GTMOAuth2ViewControllerTouch
//already handles keychain storage
- (BOOL)isUserAccountStored
{
  NSString* email = [[NSUserDefaults standardUserDefaults] objectForKey:kUserEmail];
  NSString* pwd = [[NSUserDefaults standardUserDefaults] objectForKey:kUserPassword];
  if ([email length] > 0 && [pwd length] > 0) {
    return YES;
  }
  return NO;
}

- (BOOL)hasJoinedExperimentWithId:(NSString*)definitionId {
  return [self.model isExperimentJoined:definitionId];
}

- (void)storeEmail:(NSString*)email password:(NSString*)password
{
  NSAssert([email length] > 0 && [password length] > 0, @"There isn't any valid user account to stored!");
  [[NSUserDefaults standardUserDefaults] setObject:email forKey:kUserEmail];
  [[NSUserDefaults standardUserDefaults] setObject:password forKey:kUserPassword];
}

- (void)loginWithCompletionHandler:(void (^)(NSError *))completionHandler
{
  NSString* email = [[NSUserDefaults standardUserDefaults] objectForKey:kUserEmail];
  NSAssert([email length] > 0, @"There isn't any valid user email stored to use!");
  
  NSString* password = [[NSUserDefaults standardUserDefaults] objectForKey:kUserPassword];
  NSAssert([password length] > 0, @"There isn't any valid user password stored to use!");
  
  [self loginWithClientLogin:email password:password completionHandler:completionHandler];
}


- (void)loginWithClientLogin:(NSString *)email
                    password:(NSString *)password
           completionHandler:(void (^)(NSError *))completionHandler {
  if ([self.authenticator isLoggedIn] && [self.userEmail isEqualToString:email]) {
    if (completionHandler != nil) {
      completionHandler(nil);
    }
  }else{
    [self.authenticator authenticateWithClientLogin:email//@"paco.test.gv@gmail.com"
                                           password:password//@"qwertylkjhgf"
                                  completionHandler:^(NSError *error) {
                                    if (!error) {
                                      // Authorize the service.
                                      self.service.authenticator = self.authenticator;
                                      self.userEmail = email;
                                      
                                      [self storeEmail:email password:password];
                                      
                                      // Fetch the experiment definitions and the events of joined experiments.
                                      [self prefetch];
                                      
                                      [self uploadPendingEventsInBackground];
                                      
                                      completionHandler(nil);
                                    } else {
                                      completionHandler(error);
                                    }
                                  }];    
  }
}

- (void)loginWithOAuth2CompletionHandler:(void (^)(NSError *))completionHandler {
  if ([self.authenticator isLoggedIn]) {
    if (completionHandler != nil) {
      completionHandler(nil);
    }
  }else{
    [self.authenticator authenticateWithOAuth2WithCompletionHandler:^(NSError *error) {
      if (!error) {
        // Authorize the service.
        self.service.authenticator = self.authenticator;
        // Fetch the experiment definitions and the events of joined experiments.
        [self prefetch];
        completionHandler(nil);
      } else {
        completionHandler(error);
      }
    }];
  }
}

- (void)uploadPendingEventsInBackground {
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    [[PacoEventManager sharedInstance].uploader startUploading];
  });
}

- (BOOL)prefetchedDefinitions
{
  return self.prefetchState.finishLoadingDefinitions;
}

- (NSError*)errorOfPrefetchingDefinitions
{
  return self.prefetchState.errorLoadingDefinitions;
}

- (BOOL)prefetchedExperiments
{
  return self.prefetchState.finishLoadingExperiments;
}

- (NSError*)errorOfPrefetchingexperiments
{
  return self.prefetchState.errorLoadingExperiments;
}


#pragma mark Private methods
- (void)definitionsLoadedWithError:(NSError*)error
{
  self.prefetchState.finishLoadingDefinitions = YES;
  self.prefetchState.errorLoadingDefinitions = error;
  [[NSNotificationCenter defaultCenter] postNotificationName:PacoFinishLoadingDefinitionNotification object:error];
}


//TODO: ymz: need to send this to background
- (void)prefetch {
  [self.prefetchState reset];
  
  // Load the experiment definitions.  
  BOOL success = [self.model loadExperimentDefinitionsFromFile];
  if (success) {
    [self definitionsLoadedWithError:nil];
    [self prefetchExperiments];
    return;
  }
  
  [self.service loadAllExperimentsWithCompletionHandler:^(NSArray *experiments, NSError *error) {
    if (error) {
      NSLog(@"Failed to prefetch definitions: %@", [error description]);
      [self definitionsLoadedWithError:error];
      return;
    }
    
    NSLog(@"Loaded %d experiments", [experiments count]);
    // Convert the JSON response into an object model.
    [self.model applyDefinitionJSON:experiments];
    [self definitionsLoadedWithError:nil];
    
    [self prefetchExperiments];
  }];
}


- (void)experimentsLoadedWithError:(NSError*)error
{
  self.prefetchState.finishLoadingExperiments = YES;
  self.prefetchState.errorLoadingExperiments = error;
  [[NSNotificationCenter defaultCenter] postNotificationName:PacoFinishLoadingExperimentNotification object:error];
}


- (void)prefetchExperiments
{
  NSError* error = [self.model loadExperimentInstancesFromFile];
  [self experimentsLoadedWithError:error];
}


#pragma mark stop an experiment
- (void)deleteExperimentFromCache:(PacoExperiment*)experiment
{
  //remove experiment from local cache
  [self.model deleteExperiment:experiment];
  
  //TODO: ymz: clear all scheduled notifications and anything else
}




@end
