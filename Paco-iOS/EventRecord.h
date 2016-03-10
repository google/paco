//
//  EventRecord.h
//  Paco
//
//  Created by Timo on 10/7/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EventRecord : NSManagedObject

@property (nonatomic, retain) NSNumber * actionTriggerId;
@property (nonatomic, retain) NSData * eventBlob;
@property (nonatomic, retain) NSNumber * experimentId;
@property (nonatomic, retain) NSString * groupName;
@property (nonatomic, retain) NSString * scheduledTime;
@property (nonatomic, retain) NSNumber * scheduleId;
@property (nonatomic, retain) NSNumber * isUploaded;

@end
