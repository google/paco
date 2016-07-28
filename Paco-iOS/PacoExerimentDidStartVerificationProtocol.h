//
//  PacoExerimentDidStartVerificationProtocol.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
@class PAExperimentDAO;
@protocol PacoExerimentDidStartVerificationProtocol <NSObject>


-(void)  notifyDidStart:(PAExperimentDAO*) experiment;


@end
