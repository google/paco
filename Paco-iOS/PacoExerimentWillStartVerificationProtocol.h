//
//  PacoExerimentVerificationProtocol.h
//  Paco
//
//  Created by northropo on 9/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ValidatorConsts.h"


@class PAExperimentDAO;
@protocol PacoExerimentWillStartVerificationProtocol <NSObject>

-(ValidatorExecutionStatus) shouldStart:(PAExperimentDAO*) experiment Specifications:(NSArray*) specifications;

@end
 #import "ValidatorConsts.h"