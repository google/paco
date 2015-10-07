//
//  PacoExperimentExtended.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/12/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Schedule.h" 


@class PAExperimentDAO;
@class PASchedule;



@interface PacoExperimentExtended : NSObject

@property (nonatomic, strong) PAExperimentDAO *definition;
@property (nonatomic, strong) NSNumber  *instanceId;

//the exact time that user joins the experiment
@property(nonatomic, strong, readonly) NSDate* joinTime;
@property (nonatomic, assign) long long lastEventQueryTime;
@property (nonatomic, strong) PASchedule *schedule;


+ (PacoExperimentExtended*)experimentWithDefinition:(PAExperimentDAO*)definition
                                   schedule:(PASchedule*)schedule
                                   joinTime:(NSDate*)joinTime;

- (id)serializeToJSON;
- (void)deserializeFromJSON:(id)json;

- (BOOL)shouldScheduleNotificationsFromNow;
- (BOOL)shouldScheduleNotificationsFromDate:(NSDate*)fromDate;

- (BOOL)isSelfReportExperiment;
- (BOOL)isScheduledExperiment;

- (BOOL)isExperimentValidSinceDate:(NSDate*)fromDate;

- (BOOL)isFixedLength;
- (BOOL)isOngoing;

//return all esm dates generated and stored for an esm experiment that are later than fromDate,
//if there aren't any esm dates generated yet, return nil
- (NSArray*)ESMSchedulesFromDate:(NSDate*)fromDate;

//The last ESM schedule date generated and stored for an esm experiment
//we use it to determine the next esm cycle start date to generate future esm schedule dates
//if there aren't any esm dates generated yet, return nil
- (NSDate*)lastESMScheduleDate;

- (NSDate*)startDate;
- (NSDate*)endDate;

//return the midnight of joinTime
- (NSDate*)joinDate;

//when definition is refreshed, refresh experiment's schedule
//but keep the esmStartHour, esmEndHour, or times configured specifically by user
//return YES if the schedule is changed, NO if the schedule doesn't need to be updated
- (BOOL)refreshSchedule:(PASchedule*) newSchedule;

- (void)configureSchedule:(PASchedule*)newSchedule;

@end
