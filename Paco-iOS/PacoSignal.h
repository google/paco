//
//  PacoSignal.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 10/1/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface PacoSignal : NSManagedObject

@property (nonatomic, retain) NSNumber * experimentId;
@property (nonatomic, retain) NSString * groupName;
@property (nonatomic, retain) NSNumber * actionTriggerId;
@property (nonatomic, retain) NSNumber * scheduleId;
@property (nonatomic, retain) NSNumber * date;
@property (nonatomic, retain) NSNumber * alarmTime;

@end
