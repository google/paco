//
//  PAExperimentDAO+Helper.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/11/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "ExperimentDAO.h"
@class OrgJodaTimeDateTime;
@class PAInput2;
@class PAExperimentGroup;

typedef NS_ENUM(NSInteger, PacoFeedbackType) {
    PacoFeedbackTypeStaticMessage,
    PacoFeedbackTypeRetrospective,
    PacoFeedbackTypeResponsive,
    PacoFeedbackTypeCustomCode,
    PacoFeedbackTypeDisableMessage
};

@interface PAExperimentDAO (Helper)

-(NSString*) instanceId;
-(NSString*) scheduleString;
-(BOOL) isSelfReport;
-(NSString*) earliestStartDate;
-(NSString*) lastEndDate;
-(NSArray*) getTableCellModelObjects;
-(NSDictionary* ) inputs;
-(NSString*) jsonStringForJavascript;
-(PAInput2*) inputWithId:(NSString*) inputID;
-(NSArray*) fetchAllExperimentGroups;
-(NSDictionary*) fetchExperimentGroupDictionary;
-(int) numberOfGroups;
-(PAExperimentGroup*) soloGroup;
-(PAExperimentGroup*) groupWithName:(NSString*) groupName;

@end
