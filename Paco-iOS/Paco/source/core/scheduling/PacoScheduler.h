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

@class PacoExperiment;

// The PacoScheduler schedules local notifications via UILocalNotification.  The
// experiment schedule is used to decide when to fire local notifications.  The
// local notification system can have at most 64 scheduled notifications per
// app. This means that there is a limit in how many experiments can be
// scheduled at once.
//
// The scheduler will create 3 local notifications for the experiment.  Each
// time the user opens the app with one of the notifications then the event
// is re-scheduled.  If the user fails to open 3 notifications in a row then
// the event will not be rescheduled until the next time they open the app.
@interface PacoScheduler : NSObject

// Creates 3 UILocalNotifications per experiment.
//- (void)registerScheduleWithOS:(PacoExperiment *)experiment;
//- (void)registerSchedulesWithOS:(NSArray *)experiments;

// Call from your app delegate to handle the local notification that the app
// was opened with.
- (void)handleLocalNotification:(UILocalNotification *)notification;

// Cancel all scheduled notifications for this experiment.
- (void)canceliOSNotificationsForExperimentId:(NSString *)experimentId;

// see which Notifications have expired, and schedule new ones
- (void)refreshiOSNotifications: (NSArray *)experiments;

@end
