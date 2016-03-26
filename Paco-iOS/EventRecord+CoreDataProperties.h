//
//  EventRecord+CoreDataProperties.h
//  Paco
//
//  Created by Northrop O'brien on 3/25/16.
//  Copyright © 2016 Paco. All rights reserved.
//
//  Choose "Create NSManagedObject Subclass…" from the Core Data editor menu
//  to delete and recreate this implementation file for your updated model.
//

#import "EventRecord.h"

NS_ASSUME_NONNULL_BEGIN

@interface EventRecord (CoreDataProperties)

@property (nullable, nonatomic, retain) NSNumber *actionTriggerId;
@property (nullable, nonatomic, retain) NSData *eventBlob;
@property (nullable, nonatomic, retain) NSNumber *experimentId;
@property (nullable, nonatomic, retain) NSString *groupName;
@property (nullable, nonatomic, retain) NSNumber *isUploaded;
@property (nullable, nonatomic, retain) NSString *scheduledTime;
@property (nullable, nonatomic, retain) NSNumber *scheduleId;
@property (nullable, nonatomic, retain) NSNumber *type;
@property (nullable, nonatomic, retain) NSString *guid;

@end

NS_ASSUME_NONNULL_END
