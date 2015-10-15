//
//  PacoNetwork.m
//  Paco
//
//  Created by northropo on 10/15/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoNetwork.h"
#import "PacoAuthenticator.h"
#import "PacoMediator.h"
#import "PacoService.h" 
#import "PacoAppDelegate.h" 
#import "Reachability.h" 
#import "PacoEventManagerExtended.h"





static NSString* const kPacoNotificationSystemTurnedOn = @"paco_notification_system_turned_on";
static NSString* const kPacoServerConfigAddress = @"paco_server_configuration_address";
static NSString* const kPacoProductionServerAddress = @"quantifiedself.appspot.com";
static NSString* const kPacoLocalServerAddress = @"127.0.0.1";
static NSString* const kPacoStagingServerAddress = @"quantifiedself-staging.appspot.com";

@interface PacoNetwork()

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

- (void)uploadPendingEventsInBackground {
    
    [self.eventManager startUploadingEvents];
}

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
