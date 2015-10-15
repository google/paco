//
//  PacoExtendedClient.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/12/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoExtendedClient.h"
#import "ExperimentDAO.h"
#import "Schedule.h" 

#import "PacoEventManagerExtended.h" 
#import "PacoAuthenticator.h" 


@interface PacoExtendedClient ()


@property (nonatomic, retain) PacoAuthenticator *authenticator;

@end

@implementation PacoExtendedClient


+ (PacoExtendedClient *)sharedInstance {
    
    static PacoExtendedClient *client = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        client = [[PacoExtendedClient alloc] init];
    });
    return client;
}


- (id)init {
    self = [super init];
    if (self) {
        
        
    
        
      
    }
    return self;
}





#pragma mark join an experiment









- (NSString*)userEmail {
    return [self.authenticator userEmail];
}





#pragma mark - scheduler delegate methods


- (void)updateNotificationSystem
{
 
}


- (NSArray*)nextNotificationsToSchedule
{
    return nil;
}

@end
