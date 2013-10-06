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

#import "PacoEditScheduleViewController.h"

#import "PacoClient.h"
#import "PacoModel.h"
#import "PacoScheduleEditView.h"
#import "PacoScheduler.h"
#import "PacoService.h"
#import "PacoTableView.h"
#import "PacoExperimentDefinition.h"
#import "PacoEventManager.h"
#import "PacoEvent.h"
#import "PacoEventUploader.h"

@interface PacoEditScheduleViewController ()<UIAlertViewDelegate>

@property(nonatomic, assign) BOOL isJoinSuccessful;

@end

@implementation PacoEditScheduleViewController
@synthesize experiment = _experiment;


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.navigationItem.title = @"Scheduling";
    self.navigationItem.hidesBackButton = NO;
  }
  return self;
}
- (void)viewDidLoad {
  [super viewDidLoad];

  PacoScheduleEditView *schedule = [[PacoScheduleEditView alloc] initWithFrame:CGRectZero];
  [schedule.joinButton addTarget:self action:@selector(onJoin) forControlEvents:UIControlEventTouchUpInside];
  self.view = schedule;

  schedule.experiment = self.experiment;
}

- (void)setExperiment:(PacoExperimentDefinition *)experiment {
  _experiment = experiment;
  self.title = experiment.title;
  [(PacoScheduleEditView *)self.view setExperiment:experiment];
}

- (void)onJoin {
  if (!ADD_TEST_DEFINITION) {
    [[PacoClient sharedInstance].eventManager saveJoinEventWithDefinition:self.experiment
                                                             withSchedule:nil];
  }
  
  //create a new experiment and save it to cache
  PacoExperiment *experiment = [[PacoClient sharedInstance].model
                                addExperimentInstance:self.experiment
                                schedule:self.experiment.schedule
                                events:nil]; //TODO: events will be removed from this method
  
  //start scheduling notifications for this joined experiment
  [[PacoClient sharedInstance].scheduler startSchedulingForExperimentIfNeeded:experiment];
  

  NSString* title = @"Congratulations!";
  NSString* message = @"You've successfully joined this experiment!";
  [[[UIAlertView alloc] initWithTitle:title
                              message:message
                             delegate:self
                    cancelButtonTitle:@"OK"
                    otherButtonTitles:nil] show];
}

- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex;  // after animation
{
  [self.navigationController popToRootViewControllerAnimated:YES];
}


- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
}

@end
