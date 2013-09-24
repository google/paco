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

#import "PacoConsentViewController.h"

#import "PacoColor.h"
#import "PacoClient.h"
#import "PacoEditScheduleViewController.h"
#import "PacoModel.h"
#import "PacoService.h"
#import "PacoExperimentDefinition.h"

@interface PacoConsentViewController () <UIAlertViewDelegate>
@property (nonatomic, retain) PacoExperimentDefinition *experiment;
@end

@implementation PacoConsentViewController
@synthesize experiment = _experiment;

+ (PacoConsentViewController*)controllerWithExperiment:(PacoExperimentDefinition *)experiment {
  PacoConsentViewController* controller =
      [[PacoConsentViewController alloc] initWithNibName:nil bundle:nil];
  controller.experiment = experiment;
  controller.navigationItem.title = experiment.title;
  controller.navigationItem.hidesBackButton = NO;
  return controller;
}


- (void)viewDidLoad {
  [super viewDidLoad];
  
  self.view.backgroundColor = [PacoColor pacoBackgroundWhite];

  UILabel *label = [[UILabel alloc] initWithFrame:CGRectZero];
  label.text = self.experiment.informedConsentForm;
  label.backgroundColor = [UIColor clearColor];
  label.numberOfLines = 0
  ;
  label.lineBreakMode = NSLineBreakByWordWrapping;
  [self.view addSubview:label];
  [label sizeToFit];
  CGRect frame = label.frame;
  frame.origin.x = 15;
  frame.origin.y += 75;
  frame.size.width = 285;
  label.frame = frame;
  [label sizeToFit];
  frame = label.frame;

  UIButton *accept = [UIButton buttonWithType:UIButtonTypeRoundedRect];
  [accept setTitle:@"I Consent" forState:UIControlStateNormal];
  [accept addTarget:self action:@selector(onAccept) forControlEvents:UIControlEventTouchUpInside];
  [self.view addSubview:accept];
  [accept sizeToFit];
  frame = accept.frame;
  frame.origin.x = (320 - frame.size.width) / 2;
  frame.origin.y = 420 - (frame.size.height / 2) - 25;
  accept.frame = frame;
}

- (void)onAccept {
  PacoEditScheduleViewController *edit = [[PacoEditScheduleViewController alloc] init];
  edit.experiment = self.experiment;
  [self.navigationController pushViewController:edit animated:YES];
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
}

@end
