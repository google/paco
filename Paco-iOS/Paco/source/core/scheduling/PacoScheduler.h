/* Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import <Foundation/Foundation.h>

#import "PacoModel.h"


extern NSString* const kExperimentHasFiredKey;

@class PacoExperiment;

@protocol PacoSchedulerDelegate
@required
- (void)handleNotificationTimeOut:(NSString*)experimentInstanceId
               experimentFireDate:(NSDate*)scheduledTime;

- (void)updateTimerInterval:(NSTimeInterval)newInterval;

@end

// The PacoScheduler schedules local notifications via UILocalNotification.  The
// experiment schedule is used to decide when to fire local notifications.  The
// local notification system can have at most 64 scheduled notifications per
// app. This means that there is a limit in how many experiments can be
// scheduled at once.
@interface PacoScheduler : NSObject

+ (PacoScheduler*)schedulerWithDelegate:(id<PacoSchedulerDelegate>)delegate;

- (void)handleNotification:(UILocalNotification *)notification
               experiments:(NSArray*) experiments;

// call this when joining an experiment
-(void)startSchedulingForExperimentIfNeeded:(PacoExperiment*)experiment;

// call this when leaving an experiment
- (void)stopSchedulingForExperiment:(PacoExperiment*)experiment;

// see which Notifications have expired, and schedule new ones
-(void)update:(NSArray *)experiments;

// call this when the application goes to InActive to make sure
// we can persist the notifications state
- (BOOL)saveNotificationsToFile;

@end
