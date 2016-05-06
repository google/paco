//
//  PacoEventExtended.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/13/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Schedule.h" 
#import "ExperimentDAO.h"
#import "EventInterface.h" 
#import "java/util/ArrayList.h"


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


@class PAActionSpecification;
@interface PacoEventExtended : NSObject<NSCopying,NSCoding,PAEventInterface>



@property (nonatomic, copy) NSString *who;
@property (nonatomic, retain) NSString  *when;
@property (nonatomic, assign) NSNumber* latitude;
@property (nonatomic, assign) NSNumber*  longitude;
@property (nonatomic, retain) NSDate   *responseTime;
@property (nonatomic, retain) NSString  *scheduledTime;
@property (nonatomic, readonly, copy) NSString *appId;
@property (nonatomic, readonly, copy) NSString *pacoVersion;
@property (nonatomic, copy)   NSNumber  *experimentId;
@property (nonatomic, copy)   NSString *experimentName;
@property (nonatomic, copy)   NSNumber*   experimentVersion;
@property (nonatomic, retain) JavaUtilArrayList  *responses;
@property (nonatomic,strong)  NSNumber* scheduleId;
@property (nonatomic,strong)  NSNumber* actionTriggerId;
@property (nonatomic,strong)  NSNumber* actionId;
@property (nonatomic,strong)  NSNumber* actionTriggerSpecId;
@property (nonatomic,strong)  NSString* groupName;
@property (nonatomic,strong)  NSString* serverExperimentId;
@property (nonatomic,strong)  PAActionSpecification* actionSpecification;


- (id)payloadJsonWithImageString;

- (PacoEventTypeExtended)type;

+ (PacoEventExtended*)stopEventForExperiment:(PacoExperimentExtended*)experiment;

 
+ (PacoEventExtended*)joinEventForActionSpecificatonWithServerExperimentId:(PAExperimentDAO*) actionSpecification serverExperimentId:(NSString*) serverExperimentId;

+ (PacoEventExtended*)joinEventForActionSpecificaton:(PAActionSpecification*) actionSpecification;

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
