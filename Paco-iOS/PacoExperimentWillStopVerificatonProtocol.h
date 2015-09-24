//
//  PacoExperimentStopVerificatonProtocol.h
//  Paco
//
//  Created by northropo on 9/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol PacoExperimentWillStopVerificatonProtocol <NSObject>

-(BOOL) shouldStop:(PAExperimentDAO*) experiment;

@end
