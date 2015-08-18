//
//  PacoExtendedClient.h
//  Paco
//
//  Created by northropo on 8/12/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>

@class PacoScheduler;
@class PAExperimentDAO;
@class PASchedule;
@class PacoEventManagerExtended;
@class PacoAuthenticator;


@interface PacoExtendedClient : NSObject


@property (nonatomic, assign, readonly) BOOL firstLaunch;
@property (nonatomic, strong, readonly) PacoEventManagerExtended* eventManager;
@property (nonatomic, retain, readonly) PacoAuthenticator *authenticator;
@property (nonatomic, assign, readonly) BOOL firstOAuth2;

- (void)joinExperimentWithDefinition:(PAExperimentDAO*)definition
                            schedule:(PASchedule*) schedule
                     completionBlock:(void(^)())completionBlock;

+ (PacoExtendedClient *)sharedInstance;


@end
