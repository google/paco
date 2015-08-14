//
//  PacoEventExtended.h
//  Paco
//
//  Created by northropo on 8/13/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Schedule.h" 
#import "ExperimentDAO.h"

@class PacoScheduler;
@class PAExperimentDAO;
@class PASchedule;
@class PacoExperimentExtended;



extern NSString* const kPacoResponseKeyNameExtended;
extern NSString* const kPacoResponseKeyAnswerExtended;
extern NSString* const kPacoResponseKeyInputIdExtended;

typedef NS_ENUM(NSInteger, PacoEventTypeExtended) {
    PacoEventTypeJoinExtended,
    PacoEventTypeStopExtended,
    PacoEventTypeSurveyExtended,
    PacoEventTypeMissExtended,
    PacoEventTypeSelfReportExtended
};


@interface PacoEventExtended : NSObject

@property (nonatomic, copy) NSString *who;
@property (nonatomic, retain) NSDate *when;
@property (nonatomic, assign) long long latitude;
@property (nonatomic, assign) long long longitude;
@property (nonatomic, retain) NSDate *responseTime;
@property (nonatomic, retain) NSDate *scheduledTime;
@property (nonatomic, readonly, copy) NSString *appId;
@property (nonatomic, readonly, copy) NSString *pacoVersion;
@property (nonatomic, copy) NSString *experimentId;
@property (nonatomic, copy) NSString *experimentName;
@property (nonatomic, assign) int experimentVersion;
@property (nonatomic, retain) NSArray *responses;  // <NSDictionary>
+ (id)pacoEventForIOS;
+ (id)pacoEventFromJSON:(id)jsonObject;
- (id)generateJsonObject;
- (id)payloadJsonWithImageString;

- (PacoEventTypeExtended)type;

+ (PacoEventExtended*)stopEventForExperiment:(PacoExperimentExtended*)experiment;

+ (PacoEventExtended*)joinEventForDefinition:(PAExperimentDAO*)definition
                        withSchedule:(PASchedule*)schedule;



+ (PacoEventExtended*)selfReportEventForDefinition:(PAExperimentDAO*)definition
                                withInputs:(NSArray*)inputs;
+ (PacoEventExtended*)surveySubmittedEventForDefinition:(PAExperimentDAO*)definition
                                     withInputs:(NSArray*)inputs
                               andScheduledTime:(NSDate*)scheduledTime;
+ (PacoEventExtended*)surveyMissedEventForDefinition:(PAExperimentDAO*)definition
                           withScheduledTime:(NSDate*)scheduledTime;
+ (PacoEventExtended*)surveyMissedEventForDefinition:(PAExperimentDAO*)definition
                           withScheduledTime:(NSDate*)scheduledTime
                                   userEmail:(NSString*)userEmail;



@end
