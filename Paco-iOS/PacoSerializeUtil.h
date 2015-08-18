//
//  PacoSerializeUtil.h
//  Paco
//
//  Created by northropo on 8/14/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>


@class PASchedule;
@class PAExperimentDAO;

@interface PacoSerializeUtil : NSObject

@property(strong, readonly) NSArray* classes;


+(PASchedule*) getScheduleAtIndex:(PAExperimentDAO *)  experiment   GroupIndex:(int) groupIndex actionTriggerIndex:(int) actionTriggerIndex  scheduleIndex:(int) scheduleIndex;


+ (NSArray*)getClassNames;



@end
