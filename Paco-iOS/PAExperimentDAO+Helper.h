//
//  PAExperimentDAO+Helper.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/11/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "ExperimentDAO.h"
@class OrgJodaTimeDateTime;
@interface PAExperimentDAO (Helper)

-(NSString*) instanceId;
-(NSString*) scheduleString;
-(BOOL) isSelfReport;
-(NSString*) earliestStartDate;
-(NSString*) lastEndDate;



@end
