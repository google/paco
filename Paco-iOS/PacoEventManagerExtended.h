//
//  PacoEventManagerExtended.h
//  Paco
//
//  Created by northropo on 8/13/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>

@class PAExperimentDAO;
@class PASchedule;
@class PacoEventExtended;
@class PacoExperimentExtended;


@interface PacoParticipateStatusExtended : NSObject

@property(nonatomic, readonly) NSUInteger numberOfNotifications;
@property(nonatomic, readonly) NSUInteger numberOfParticipations;
@property(nonatomic, readonly) NSUInteger numberOfSelfReports;
@property(nonatomic, readonly) float percentageOfParticipation; //0.867
@property(nonatomic, copy, readonly) NSString *percentageText; //87%

@end


@class PAActionSpecification;

//YMZ:TODO: fully testing
//YMZ:TODO: thread safe
//YMZ:TODO: use async design
//YMZ:TODO: use core data
//YMZ:TODO: error handling of file operation
@interface PacoEventManagerExtended : NSObject

+ (PacoEventManagerExtended*)defaultManager;

- (void)saveEvent:(PacoEventExtended*)event;
- (void)saveEvents:(NSArray*)events;

- (void)startUploadingEvents;

//When background fetch API triggers or location significantly changes, call this method
//to upload events in a limited time frame, we are allowed to finish our tasks in 30 seconds.
- (void)startUploadingEventsInBackgroundWithBlock:(void(^)(UIBackgroundFetchResult))completionBlock;

- (void)stopUploadingEvents;


- (void)saveStopEventWithExperiment:(PacoExperimentExtended*)experiment;
- (void)saveSelfReportEventWithDefinition:(PAExperimentDAO*)definition
                                andInputs:(NSArray*)visibleInputs;
- (void)saveSurveySubmittedEventForDefinition:(PAExperimentDAO*)definition
                                   withInputs:(NSArray*)inputs
                             andScheduledTime:(NSDate*)scheduledTime;

- (PacoParticipateStatusExtended*)statsForExperiment:(NSString*)experimentId;

- (void)saveJoinEventWithActionSpecification:(PAActionSpecification*) actionSpecification;
@end
