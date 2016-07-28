//
//  PacoHasRelevantActionSpecifications.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/24/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PacoExerimentWillStartVerificationProtocol.h" 
#import "ValidatorConsts.h" 



@interface PacoHasRelevantActionSpecifications : NSObject<PacoExerimentWillStartVerificationProtocol>


-(ValidatorExecutionStatus) shouldStart:(PAExperimentDAO*) experiment Specifications:(NSArray*) specifications;
@end
