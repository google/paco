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

@interface PacoClient ()
@property (nonatomic, retain, readwrite) PacoAuthenticator *authenticator;
@property (nonatomic, retain, readwrite) PacoLocation *location;
@property (nonatomic, retain, readwrite) PacoModel *model;
@property (nonatomic, retain, readwrite) PacoScheduler *scheduler;
@property (nonatomic, retain, readwrite) PacoService *service;
@property (nonatomic, retain, readwrite) NSString *serverDomain;
@property (nonatomic, retain, readwrite) NSString* userEmail;

- (void)prefetch;
@end

@implementation PacoClient

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
    
    if (SERVER_DOMAIN_FLAG == 0) {//production
      self.serverDomain = @"https://quantifiedself.appspot.com";
    }else{//localserver
      self.serverDomain = @"http://127.0.0.1";
    }
  }
  return self;
}

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

- (void)prefetch {
  [self refreshModelWithCompletionHandler:^(NSError *error) {
    if (error) {
      NSLog(@"Error on prefetch %@", error);
    }
  }];
}

- (void)refreshModelWithCompletionHandler:(void (^)(NSError *))completionHandler {
  if (!self.service) {
    self.service = [[PacoService alloc] init];
  }
  // Authorize the service.
  self.service.authenticator = self.authenticator;

  // Load the experiment definitions.
  self.model = [PacoModel pacoModelFromFile];
  
  if (self.model) {
    completionHandler(nil);
    return;
  }
  
  [self.service loadAllExperimentsWithCompletionHandler:^(NSArray *experiments, NSError *error) {
    if (error) {
      completionHandler(error);
      return;
    }
    
    NSLog(@"Loaded %d experiments", [experiments count]);
    // Convert the JSON response into an object model.
    self.model = [PacoModel pacoModelFromDefinitionJSON:experiments
                                           instanceJSON:nil];

    // Load events for each known experiment, if events exist then this
    // will indicate that the user has joined this experiment.
    
    //for (PacoExperiment *experiment in self.model.experimentInstances) {
    for (PacoExperimentDefinition *experimentDefinition in self.model.experimentDefinitions) {
      [self processExperimentDefinition:experimentDefinition];
    }
    completionHandler(nil);
  }];
}

- (void)processExperimentDefinition:(PacoExperimentDefinition*)definition
{
  NSAssert(definition != nil, @"definition should NOT be nil!");
  [self.service loadEventsForExperiment:definition
                  withCompletionHandler:^(NSArray *events, NSError *error) {
                    //YMZ: TODO error handling interface
                    if (error != nil) {
                      NSLog(@"Error fetching events: %@", [error description]);
                      return;
                    }
                    
                    if ([events count] == 0) {
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
                      return;
                    }
                    
                    [[PacoClient sharedInstance].model addExperimentsWithDefinition:definition events:pacoEvents];
                  }];

}


@end
