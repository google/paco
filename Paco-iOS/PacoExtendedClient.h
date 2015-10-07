//
//  PacoExtendedClient.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/12/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>

@class PacoScheduler;
@class PAExperimentDAO;
@class PASchedule;
@class PacoEventManagerExtended;
@class PacoAuthenticator;
@class PAActionSpecification;



@interface PacoExtendedClient : NSObject

 
@property (nonatomic, assign, readonly) BOOL firstLaunch;
@property (nonatomic, strong, readonly) PacoEventManagerExtended* eventManager;
@property (nonatomic, retain, readonly) PacoAuthenticator *authenticator;
@property (nonatomic, assign, readonly) BOOL firstOAuth2;

- (void)joinExperimentWithDefinition:(PAActionSpecification*) specification
                     completionBlock:(void(^)())completionBlock;

+ (PacoExtendedClient *)sharedInstance;
- (NSString*)userEmail;


@end
