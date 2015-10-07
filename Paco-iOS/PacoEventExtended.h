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
@interface PacoEventExtended : NSObject<PAEventInterface,NSCopying,NSCoding>



@property (nonatomic, copy) NSString *who;
@property (nonatomic, retain) NSString  *when;
@property (nonatomic, assign) NSNumber* latitude;
@property (nonatomic, assign) NSNumber*  longitude;
@property (nonatomic, retain) NSString  *responseTime;
@property (nonatomic, retain) NSString  *scheduledTime;
@property (nonatomic, readonly, copy) NSString *appId;
@property (nonatomic, readonly, copy) NSString *pacoVersion;
@property (nonatomic, copy)   NSString *experimentId;
@property (nonatomic, copy)   NSString *experimentName;
@property (nonatomic, copy)   NSNumber*   experimentVersion;
@property (nonatomic, retain) JavaUtilArrayList  *responses;
@property (nonatomic,strong)  NSString* scheduleId;
@property (nonatomic,strong)  NSString* actionTriggerId;
@property (nonatomic,strong)  NSString* groupName;




+ (id)pacoEventForIOS;
+ (id)pacoEventFromJSON:(id)jsonObject;
- (id)generateJsonObject;
- (id)payloadJsonWithImageString;

- (PacoEventTypeExtended)type;

+ (PacoEventExtended*)stopEventForExperiment:(PacoExperimentExtended*)experiment;

 

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
