//
//  PacoSerializeUtil.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/14/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>


@class PASchedule;
@class PAExperimentDAO;

@interface PacoSerializeUtil : NSObject

@property(strong, readonly) NSArray* classes;


+(PASchedule*) getScheduleAtIndex:(PAExperimentDAO *)  experiment   GroupIndex:(int) groupIndex actionTriggerIndex:(int) actionTriggerIndex  scheduleIndex:(int) scheduleIndex;


+ (NSArray*)getClassNames;

+ (NSString*) jsonFromDefinition:(PAExperimentDAO*) description;
+ (NSString*) jsonFromSchedule:(PASchedule*) shedule;

@end
