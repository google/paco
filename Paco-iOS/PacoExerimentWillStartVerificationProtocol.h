//
//  PacoExerimentVerificationProtocol.h
//  Paco
//
//  Created by northropo on 9/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>


@class PAExperimentDAO;
@protocol PacoExerimentWillStartVerificationProtocol <NSObject>

-(BOOL) shouldStart:(PAExperimentDAO*) experiment;

@end
 