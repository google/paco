/* Copyright 2015  Google
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

#import "EventRecord.h"

NS_ASSUME_NONNULL_BEGIN

@interface EventRecord (CoreDataProperties)

@property (nullable, nonatomic, retain) NSNumber *actionTriggerId;
@property (nullable, nonatomic, retain) NSData *eventBlob;
@property (nullable, nonatomic, retain) NSNumber *experimentId;
@property (nullable, nonatomic, retain) NSString *groupName;
@property (nullable, nonatomic, retain) NSString *guid;
@property (nullable, nonatomic, retain) NSNumber *isUploaded;
@property (nullable, nonatomic, retain) NSString *scheduledTime;
@property (nullable, nonatomic, retain) NSNumber *scheduleId;
@property (nullable, nonatomic, retain) NSNumber *type;
@property (nullable, nonatomic, retain) NSData *pacoExperimentBlog;

@end

NS_ASSUME_NONNULL_END
