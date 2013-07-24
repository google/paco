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
- (BOOL)loadExperimentInstancesFromFile;
- (void)applyDefinitionJSON:(id)jsonObject;
@end

@interface PacoClient () <PacoLocationDelegate>
@property (nonatomic, retain, readwrite) PacoAuthenticator *authenticator;
@property (nonatomic, retain, readwrite) PacoLocation *location;
@property (nonatomic, retain, readwrite) PacoModel *model;
@property (nonatomic, retain, readwrite) PacoScheduler *scheduler;
@property (nonatomic, retain, readwrite) PacoService *service;
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
        self.location = nil;
        self.scheduler = [[PacoScheduler alloc] init];
        self.service = [[PacoService alloc] init];
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
- (BOOL)isLoggedIn {
  return [self.authenticator isLoggedIn];
}

- (void)timerUpdated {
  [self.scheduler updateiOSNotifications:self.model.experimentInstances];
  NSLog(@"Paco Timer fired");
}

//YMZ: TODO: we need to store user email and address inside keychain
//However, if we migrate to OAuth2, it looks like GTMOAuth2ViewControllerTouch
//already handles keychain storage
- (BOOL)isUserAccountStored {
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
                                      [self prefetchBackground:^{
                                        // let's handle setting up the notifications after that thread completes
                                        NSLog(@"Paco loginWithClientLogin experiments load has completed.");
                                        // if we have experiments, then initialize PacoLocation (no use to use energy heavy location if no experiment exists)
                                        if (self.model.experimentInstances.count > 0) {
                                          self.location = [[PacoLocation alloc] init];
                                          self.location.delegate = self;
                                        }
                                      }];
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
        [self prefetchBackground:^{
          // let's handle setting up the notifications after that thread completes
          NSLog(@"Paco loginWithOAuth2CompletionHandler experiments load has completed.");
          // if we have experiments, then initialize PacoLocation (no use to use energy heavy location if no experiment exists)
          if (self.model.experimentInstances.count > 0) {
            self.location = [[PacoLocation alloc] init];
            self.location.delegate = self;
          }
        }];
        completionHandler(nil);
      } else {
        completionHandler(error);
      }
    }];
  }
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

- (void)prefetchBackground:(void (^)(void))completionHandler {
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    [[PacoClient sharedInstance] prefetch];
    if (completionHandler) {
      dispatch_async(dispatch_get_main_queue(), ^{
        // prefetch is done
        completionHandler();
      });
    }
  });
}

- (void)prefetch {
  [self.prefetchState reset];
  
  // we need to send do this in the background as we don't want the launch of the App on the same thread
  
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
  BOOL success = [self.model loadExperimentInstancesFromFile];
  if (success) {
    [self experimentsLoadedWithError:nil];
    return;
  }
  
  // Load events for each known experiment, if events exist then this
  // will indicate that the user has joined this experiment.
  int numOfDefinitions = [self.model.experimentDefinitions count];
  __block int numOfResponses = 0;
  __block NSError* resultError = nil;
  void(^finishBlock)(NSError*) = ^(NSError* error){
    numOfResponses++;
    //record the first error for now
    if (resultError == nil && error != nil) {
      resultError = error;
    }
    
    if(numOfDefinitions == numOfResponses){
      [self experimentsLoadedWithError:resultError];
    }
  };
  
  
  for (PacoExperimentDefinition *experimentDefinition in self.model.experimentDefinitions) {
    [self fetchExperimentsForDefinition:experimentDefinition completionBlock:^(NSError *error) {
      finishBlock(error);
    }];
  }
}

- (void)fetchExperimentsForDefinition:(PacoExperimentDefinition*)definition completionBlock:(void(^)(NSError*))completionBlock
{
  NSAssert(definition != nil, @"definition should NOT be nil!");
  
  [self.service loadEventsForExperiment:definition
                  withCompletionHandler:^(NSArray *events, NSError *error) {
                    //YMZ: TODO error handling interface
                    if (error != nil) {
                      NSLog(@"Error fetching events: %@", [error description]);
                      completionBlock(error);
                      return;
                    }
                    
                    if ([events count] == 0) {
                      completionBlock(nil);
                      return;
                    }
                    
                    NSLog(@"\tFound %d events in experiment \"%@\"", [events count], definition.title);
                    // Convert the JSON events into event objects.
                    NSMutableArray *pacoEvents = [NSMutableArray array];
                    for (id jsonEvent in events) {
                      PacoEvent *pacoEvent = [PacoEvent pacoEventFromJSON:jsonEvent];
                      [pacoEvents addObject:pacoEvent];
                    }
                    
                    if ([pacoEvents count] == 0) {
                      completionBlock(nil);
                      return;
                    }
                    
                    [[PacoClient sharedInstance].model addExperimentsWithDefinition:definition events:pacoEvents];
                    completionBlock(nil);
                  }];
  
}


@end
