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
#import "PacoAppDelegate.h"
#import "NSError+Paco.h"

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
@property (nonatomic, strong, readwrite) PacoEventManager* eventManager;
@property (nonatomic, retain, readwrite) PacoScheduler *scheduler;
@property (nonatomic, retain, readwrite) PacoService *service;
@property (nonatomic, strong) Reachability* reachability;
@property (nonatomic, retain, readwrite) NSString *serverDomain;
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
    
    _eventManager = [PacoEventManager defaultManager];
    
    self.prefetchState = [[PacoPrefetchState alloc] init];
    
    if (SERVER_DOMAIN_FLAG == 0) {//production
      self.serverDomain = @"https://quantifiedself.appspot.com";
    }else{//localserver
      self.serverDomain = @"http://127.0.0.1";
    }
  }
  return self;
}

- (void)dealloc {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark Public methods
- (BOOL)isLoggedIn
{
  return [self.authenticator isLoggedIn];
}

- (BOOL)isUserAccountStored {
  return [self.authenticator isUserAccountStored];
}

- (BOOL)hasJoinedExperimentWithId:(NSString*)definitionId {
  return [self.model isExperimentJoined:definitionId];
}

- (NSString*)userEmail {
  return [self.authenticator userEmail];
}


#pragma mark bring up login flow if necessary
- (void)showLoginScreenWithCompletionBlock:(LoginCompletionBlock)block
{
  PacoLoginScreenViewController *loginViewController =
      [PacoLoginScreenViewController controllerWithCompletionBlock:block];
  
  UINavigationController* navi = (UINavigationController*)
      ((PacoAppDelegate*)[UIApplication sharedApplication].delegate).window.rootViewController;
  [navi presentViewController:loginViewController animated:YES completion:nil];
}

- (void)startWorkingAfterLogIn {
  // Authorize the service.
  self.service.authenticator = self.authenticator;
  
  // Fetch the experiment definitions and the events of joined experiments.
  [self prefetch];
  
  [self uploadPendingEventsInBackground];
}


- (void)reachabilityChanged:(NSNotification*)notification {
  Reachability* reach = (Reachability*)[notification object];
  if ([reach isReachable]) {      
    [self.authenticator reAuthenticateWithBlock:^(NSError* error) {
      [[NSNotificationCenter defaultCenter] removeObserver:self];

      if (error == nil) {
        // Authorize the service.
        self.service.authenticator = self.authenticator;
        [self uploadPendingEventsInBackground];
      } else {
        NSLog(@"[ERROR]: failed to re-authenticate user!!!");
        [self showLoginScreenWithCompletionBlock:nil];
      }
    }];
    
    NSLog(@"[Reachable]: Online Now!");
  }else {
    NSLog(@"[Reachable]: Offline Now!");
  }
}

- (void)reAuthenticateUserWithBlock:(LoginCompletionBlock)block {
  //If there is an account stored, and the internet is offline, then we should allow user to use
  //our app, so we need to prefetch definitions and experiments. When the internet is reacheable,
  //we will re-authenticate user
  if (!self.reachability.isReachable) {
    [self prefetch];
    if (block != nil) {
      block(nil);
    }
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(reachabilityChanged:)
                                                 name:kReachabilityChangedNotification
                                               object:nil];
  } else {
    [self.authenticator reAuthenticateWithBlock:^(NSError* error) {
      if (error == nil) {
        [self startWorkingAfterLogIn];
        if (block != nil) {
          block(nil);
        }
      } else {
        [self showLoginScreenWithCompletionBlock:block];
      }
    }];
  }
}


- (void)loginWithCompletionBlock:(LoginCompletionBlock)block {
  if ([self isLoggedIn]) {
    if (block) {
      block(nil);
    }
    return;
  }
  
  if ([self.authenticator setupWithCookie]) {
    NSLog(@"Valid cookie detected, no need to log in!");
    [self startWorkingAfterLogIn];
    
    if (block != nil) {
      block(nil);
    }
    return;
  }
  
  if (![self isUserAccountStored]) {
    [self showLoginScreenWithCompletionBlock:block];
  } else {
    [self reAuthenticateUserWithBlock:block];
  }
}


- (void)loginWithClientLogin:(NSString *)email
                    password:(NSString *)password
           completionHandler:(void (^)(NSError *))completionHandler {
  NSAssert(![self isLoggedIn], @"user should not be logged in!");
  [self.authenticator authenticateWithClientLogin:email
                                         password:password
                                completionHandler:^(NSError *error) {
                                  if (!error) {
                                    [self startWorkingAfterLogIn];
                                    completionHandler(nil);
                                  } else {
                                    completionHandler(error);
                                  }
                                }];    
}


- (void)loginWithOAuth2CompletionHandler:(void (^)(NSError *))completionHandler {
  if ([self isLoggedIn]) {
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
    [self.eventManager startUploadingEvents];
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
