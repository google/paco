//
//  PacoRefreshProtocol.h
//  Paco
//
//  Created by Timo on 10/8/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExperimentDAO.h"

@protocol PacoRefreshProtocol <NSObject>

-(void) refresh;
-(void) joinExperiment:(PAExperimentDAO*) dao;

-(void) leaveExperiment:(PAExperimentDAO*) dao;
@end
