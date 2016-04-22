//
//  PacoNetwork.m
//  Paco
//
//  Created by Timo on 10/15/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoNetwork.h"
#import "PacoAuthenticator.h"
#import "PacoMediator.h"
#import "PacoService.h" 
#import "PacoAppDelegate.h" 
#import "Reachability.h" 
#import "PacoEventManagerExtended.h"
#import "PacoSerializeUtil.h"
#import "PacoSerializer.h" 
#import "PacoEnumerator.h" 
#import "PacoPublicDefinitionLoader.h"





static NSString* const kPacoNotificationSystemTurnedOn = @"paco_notification_system_turned_on";
static NSString* const kPacoServerConfigAddress = @"paco_server_configuration_address";
static NSString* const kPacoProductionServerAddress = @"quantifiedself.appspot.com";
static NSString* const kPacoLocalServerAddress = @"127.0.0.1:8888";  //@"127.0.0.1";
static NSString* const kPacoStagingServerAddress = @"quantifiedself-staging.appspot.com";

@interface PacoNetwork()


@property (nonatomic,strong) id<PacoEnumerator> publicExperimentIterator;
@property (nonatomic, strong) PacoEventManagerExtended* eventManager;



@end





@implementation PacoNetwork


- (id)init {
    self = [super init];
    if (self) {
   
         PacoAppDelegate*  appDelegate  = (PacoAppDelegate *) [UIApplication sharedApplication].delegate;
         _authenticator = [[PacoAuthenticator alloc] initWithFirstLaunchFlag:appDelegate.isFirstLaunch|appDelegate.isFirstOAuth2];

         _service = [[PacoService alloc] init];
         _reachability = [Reachability reachabilityWithHostname:@"www.google.com"];
        
        // Start the notifier, which will cause the reachability object to retain itself!
        [_reachability startNotifier];
        _eventManager = [PacoEventManagerExtended defaultManager];
        
        [self setupServerDomain];
        
        // by default keep notification system on.
        [self triggerNotificationSystem];
        
        
        
        
        
        
        _publicExperimentIterator =  [PacoPublicDefinitionLoader  publicExperimentsEnumerator];
        
    }
    return self;
}


/*
 
 PacoMediator is a singleton instnace and should only use sharedInstance
 to create/fetch  and instance
 
 */
+ (PacoNetwork*)sharedInstance
{
    static dispatch_once_t once;
    static PacoNetwork *sharedInstance;
    dispatch_once(&once, ^ {
        sharedInstance = [[self alloc] init];
        
    });
    
    return sharedInstance;
}


- (BOOL)isLoggedIn {
    
    return [_authenticator isLoggedIn];
}

- (BOOL)isUserAccountStored {
    return [_authenticator isUserAccountStored];
}

- (NSString*)userEmail {
    return [self.authenticator userEmail];
}



- (BOOL)isNotificationSystemOn {
    BOOL turnedOn = [[NSUserDefaults standardUserDefaults] boolForKey:kPacoNotificationSystemTurnedOn];
    return turnedOn;
}


- (void)setupServerDomain {
    NSString* serverAddress = [[NSUserDefaults standardUserDefaults] objectForKey:kPacoServerConfigAddress];
    if (!serverAddress) {
        switch (SERVER_DOMAIN_FLAG) {
            case 0: //production server
                serverAddress = kPacoProductionServerAddress;
                break;
                
            case 1: //local server
                serverAddress = kPacoLocalServerAddress;
                break;
                
            case 2: //staging server
                serverAddress = kPacoStagingServerAddress;
                break;
                
            default:
                NSAssert(NO, @"wrong server address");
                break;
        }
    }
    [self updateServerDomainWithAddress:serverAddress];
}

- (void)updateServerDomainWithAddress:(NSString*)serverAddress {
    NSString* prefix = [serverAddress isEqualToString:kPacoLocalServerAddress] ? @"http://" : @"https://";
    self.serverDomain = [NSString stringWithFormat:@"%@%@", prefix, serverAddress];
}

- (void)configurePacoServerAddress:(NSString *)serverAddress {
    [self updateServerDomainWithAddress:serverAddress];
    [[NSUserDefaults standardUserDefaults] setObject:serverAddress forKey:kPacoServerConfigAddress];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

- (void)triggerNotificationSystem {
    @synchronized(self) {
        if (![self isNotificationSystemOn]) {
          
            [[NSUserDefaults standardUserDefaults] setBool:YES forKey:kPacoNotificationSystemTurnedOn];
            [[NSUserDefaults standardUserDefaults] synchronize];
            //set background fetch min internval to be 15 minutes
            [[UIApplication sharedApplication] setMinimumBackgroundFetchInterval:15 * 60];
        }
    }
}


- (void)disableBackgroundFetch {
    
    [[UIApplication sharedApplication] setMinimumBackgroundFetchInterval:UIApplicationBackgroundFetchIntervalNever];
}

- (void)shutDownNotificationSystem  {
    @synchronized(self) {
        
     
        if ([self isNotificationSystemOn]) {
            
            [[NSUserDefaults standardUserDefaults] setBool:NO forKey:kPacoNotificationSystemTurnedOn];
            [[NSUserDefaults standardUserDefaults] synchronize];
            [[PacoMediator sharedInstance] clearRunningExperiments];
            [self disableBackgroundFetch];
        }
    }
}


- (void)loginWithCompletionBlock:(LoginCompletionBlock)block {
    if ([self isLoggedIn]) {
        if (block) {
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


- (void)reAuthenticateUserWithBlock:(LoginCompletionBlock)block {
    //If there is an account stored, and the internet is offline, then we should allow user to use
    //our app, so we need to prefetch definitions and experiments. When the internet is reacheable,
    //we will re-authenticate user
    
    
  
    if (!self.reachability.isReachable) {
        
        
       // [self prefetchInBackground]; we assume the experiments are available when the app exists.
        
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


- (void)startWorkingAfterLogIn {
    // Authorize the service.
    self.service.authenticator = self.authenticator;
    
    // Fetch the experiment definitions and the events of joined experiments.
    //[self prefetchInBackground];
    [self uploadPendingEventsInBackground];
}



- (void)showLoginScreenWithCompletionBlock:(LoginCompletionBlock)block {
    [self loginWithOAuth2CompletionHandler:block];
}



- (void)loginWithOAuth2CompletionHandler:(void (^)(NSError *))completionHandler {
    if ([self isLoggedIn]) {
        
        NSLog(@"PacoClient-- loginWithOAuth2CompletionHandler ");
        if (completionHandler != nil) {
            completionHandler(nil);
        }
    }else{
        [self.authenticator authenticateWithOAuth2WithCompletionHandler:^(NSError *error) {
            if (!error) {
                // Authorize the service.
                self.service.authenticator = self.authenticator;
                // Fetch the experiment definitions and the events of joined experiments.
               // [self prefetchInBackground];
                completionHandler(nil);
            } else {
                completionHandler(error);
            }
        }];
    }
}


- (void)uploadPendingEventsInBackground {
    
    
    [self.eventManager startUploadingEvents];
}




/*
    move this method. to much higher level logic in class for lower level logic.
 
 
 */
-(void) hudReload
{
    
    PacoNetwork * network = [PacoNetwork sharedInstance];
    NSMutableArray  * mutableArray = [NSMutableArray new];
    
    if(!_isFetching)
    {
        [network loginWithCompletionBlock:^(NSError* error) {
            
            
            
            [_publicExperimentIterator loadNextPage:^(NSArray * array, NSError * error) {
                
                
                NSArray* classNames = [PacoSerializeUtil getClassNames];
                PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:classNames withNameOfClassAttribute:@"nameOfClass"];
                
                
                PAExperimentDAO * dao;
                for(NSDictionary* dict in array)
                {
                     dao  = (PAExperimentDAO*)  [serializer buildModelObject:dict];
                    
                    [mutableArray addObject:dao];
                }

                PacoMediator * mediator =  [PacoMediator sharedInstance];
                [mediator setHudExperiments:mutableArray];
 
                
            }];
            
            
            
            
            [[NSNotificationCenter defaultCenter]
             postNotificationName:@"HudUpdated"
             object:self];
            
            
            
         }];

        
        
        
        
        
    }
    
    
    
    
    
    
}


-(void) update
{
    
    PacoNetwork * network = [PacoNetwork sharedInstance];
    
    if(!_isFetching)
    {
        _isFetching   =YES;
    /* log in if not needed */
    [network loginWithCompletionBlock:^(NSError* error) {
        
        if (error) {
            
            [[PacoMediator sharedInstance] refreshRunningExperiments];
            NSLog(@"Unable to log in with completion block");
            _isFetching=NO;
            
        } else {
            
            [network.service loadMyFullDefinitionListWithBlock:^(NSDictionary * experiments, NSError* error) {
                if (!error) {
                    
                    PacoMediator* mediator = [PacoMediator sharedInstance];
                    [mediator  replaceAllExperiments:experiments];
                     _isFetching=NO;
                    [[NSNotificationCenter defaultCenter]
                     postNotificationName:@"MyExperiments"
                     object:nil];

                } else
                {
                    [[PacoMediator sharedInstance] refreshRunningExperiments];
                     NSLog(@"Unable to log in with completion block");
                     _isFetching=NO;
                    
                    // unable to load definition
                }
                
            }];
            
            
        }
    }];
    
    }
 
    
    
}


- (void)prefetchInBackground {
    NSLog(@"PacoClient-- Refresh prefetchInBackground");
    @synchronized(self) {
    
        
             [self uploadPendingEventsInBackground];
              [self.service loadMyFullDefinitionListWithBlock:^(NSDictionary* definitions, NSError* error) {
                if (!error) {
                    
                    
                    
                    [[PacoMediator sharedInstance] replaceAllExperiments:definitions];
                   
                } else {
                    
                   // DDLogError(@"Failed to prefetch definitions: %@", [error description]);
                    
                }
                
                //[[NSNotificationCenter defaultCenter] postNotificationName:kPacoNotificationLoadedMyDefinitions
                                                                  //  object:error];
            }];
        }
}


#pragma mark Private methods






- (NSString*)serverAddress {
    NSString* endOfPrefix = @"//";
    NSRange range = [self.serverDomain rangeOfString:endOfPrefix];
    NSUInteger index = range.location + [endOfPrefix length];
    return [self.serverDomain substringFromIndex:index];
}

/*
- (void)submitSurveyWithDefinition:(PacoExperimentDefinition*)definition
                      surveyInputs:(NSArray*)surveyInputs
                      notification:(UILocalNotification*)notification {
    
    DDLogInfo(@"PacoClient-- submitSurvayWithDefinition ");
    if (notification) {
        [self.eventManager saveSurveySubmittedEventForDefinition:definition
                                                      withInputs:surveyInputs
                                                andScheduledTime:[notification pacoFireDate]];
        [self.scheduler handleRespondedNotification:notification];
    } else {
        [self.eventManager saveSelfReportEventWithDefinition:definition andInputs:surveyInputs];
    }
    
    DDLogInfo(@"PacoClient-- submitSurvayWithDefinition ");
}
 */



@end
