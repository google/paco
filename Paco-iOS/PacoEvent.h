#import <Foundation/Foundation.h>

@class PacoExperiment;
@class PacoExperimentDefinition;
@class PacoExperimentSchedule;


extern NSString* const kPacoResponseKeyName;
extern NSString* const kPacoResponseKeyAnswer;
extern NSString* const kPacoResponseKeyInputId;


typedef NS_ENUM(NSInteger, PacoEventType) {
    PacoEventTypeJoin,
    PacoEventTypeStop,
    PacoEventTypeSurvey,
    PacoEventTypeMiss,
    PacoEventTypeSelfReport
};

@interface PacoEvent : NSObject

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

- (PacoEventType)type;

+ (PacoEvent*)stopEventForExperiment:(PacoExperiment*)experiment;
+ (PacoEvent*)joinEventForDefinition:(PacoExperimentDefinition*)definition
                        withSchedule:(PacoExperimentSchedule*)schedule;
+ (PacoEvent*)selfReportEventForDefinition:(PacoExperimentDefinition*)definition
                                withInputs:(NSArray*)inputs;
+ (PacoEvent*)surveySubmittedEventForDefinition:(PacoExperimentDefinition*)definition
                                     withInputs:(NSArray*)inputs
                               andScheduledTime:(NSDate*)scheduledTime;
+ (PacoEvent*)surveyMissedEventForDefinition:(PacoExperimentDefinition*)definition
                           withScheduledTime:(NSDate*)scheduledTime;
+ (PacoEvent*)surveyMissedEventForDefinition:(PacoExperimentDefinition*)definition
                           withScheduledTime:(NSDate*)scheduledTime
                                   userEmail:(NSString*)userEmail;


@end