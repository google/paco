//
//  PacoExperimentWillBeModified.h
//  Paco
//
//  Created by northropo on 9/30/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ValidatorConsts.h"

@protocol PacoExperimentWillBeModifiedProtocol <NSObject>

-(ValidatorExecutionStatus) shouldModify:(PAExperimentDAO*) experiment Specifications:(NSArray*) specifications;

@end
