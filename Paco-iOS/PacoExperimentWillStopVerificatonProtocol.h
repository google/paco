//
//  PacoExperimentStopVerificatonProtocol.h
//  Paco
//
//  Created by northropo on 9/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ValidatorConsts.h"

@protocol PacoExperimentWillStopVerificatonProtocol <NSObject>

-(ValidatorExecutionStatus) shouldStop:(PAExperimentDAO*) experiment;

@end
