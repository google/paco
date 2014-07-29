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

#import "PacoEditScheduleAfterJoinController.h"
#import "PacoExperimentSchedule.h"
#import "PacoScheduleEditView.h"

@interface PacoEditScheduleAfterJoinController ()

@property(nonatomic, copy) PacoExperimentSchedule* schedule;
@property(nonatomic, copy) EditScheduleCompletionBlock completionBlock;

@end

@implementation PacoEditScheduleAfterJoinController

- (instancetype)initWithSchedule:(PacoExperimentSchedule*)schedule
                 completionBlock:(EditScheduleCompletionBlock)block {
  self = [super initWithNibName:nil bundle:nil];
  if (self) {
    _schedule = schedule;
    _completionBlock = [block copy];

    self.navigationItem.rightBarButtonItem =
        [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Submit", nil)
                                         style:UIBarButtonItemStyleDone
                                        target:self
                                        action:@selector(onDone:)];
    self.navigationItem.leftBarButtonItem =
        [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel
                                                      target:self
                                                      action:@selector(onCancel:)];
  }
  return self;
}

+ (instancetype)controllerWithSchedule:(PacoExperimentSchedule*)schedule
                       completionBlock:(EditScheduleCompletionBlock)block {
  PacoEditScheduleAfterJoinController* controller =
      [[[self class] alloc] initWithSchedule:schedule completionBlock:block];
  return controller;
}


- (void)viewDidLoad {
  [super viewDidLoad];
  self.view = [PacoScheduleEditView viewWithFrame:CGRectZero schedule:self.schedule];
}

- (void)dismissWithStatus:(PacoEditScheduleStatus)status {
  if (self.completionBlock) {
    self.completionBlock(status, ((PacoScheduleEditView*)self.view).schedule);
  }
  [self.navigationController popViewControllerAnimated:YES];
}

- (void)onDone:(id)sender {
  if ([self.schedule isExactlyEqualToSchedule:((PacoScheduleEditView*)self.view).schedule]) {
    [self dismissWithStatus:PacoEditScheduleStatusUnchanged];
  } else {
    [self dismissWithStatus:PacoEditScheduleStatusChanged];
  }
}

- (void)onCancel:(id)sender {
  [self dismissWithStatus:PacoEditScheduleStatusCancelled];
}


@end
