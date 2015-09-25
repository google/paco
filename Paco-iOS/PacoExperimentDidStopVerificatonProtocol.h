//
//  PacoExperimentDidStopVerificatonProtocol.h
//  Paco
//
//  Created by northropo on 9/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
 
@protocol PacoExperimentDidStopVerificatonProtocol <NSObject>

-(void) notifyDidStop:(PAExperimentDAO*) experiment;

@end
