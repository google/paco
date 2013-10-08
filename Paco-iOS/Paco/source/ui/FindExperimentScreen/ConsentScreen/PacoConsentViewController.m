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
#import "PacoFont.h"

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
  if ([self respondsToSelector:@selector(edgesForExtendedLayout)]){//for ios7, to adjust layout
    self.edgesForExtendedLayout = UIRectEdgeNone;
  }
  self.view.backgroundColor = [PacoColor pacoBackgroundWhite];

  UILabel* boldText = [[UILabel alloc] initWithFrame:CGRectMake(10, 10, 300, 30)];
  boldText.text = @"Data Handling & Privacy Agreement between You and the Experiment Creator";
  boldText.font = [PacoFont pacoConsentBoldFont];
  boldText.textColor = [UIColor blackColor];
  boldText.backgroundColor = [UIColor clearColor];
  boldText.numberOfLines = 0;
  [self.view addSubview:boldText];
  [boldText sizeToFit];
  CGRect boldTextFrame = boldText.frame;
  boldTextFrame.origin.x = 10;
  boldTextFrame.origin.y = 10;
  boldText.frame = boldTextFrame;

  UILabel* consentText = [[UILabel alloc] initWithFrame:CGRectMake(10, boldText.frame.origin.y + boldText.frame.size.height + 10, 300, 0)];
  consentText.text = @"By joining this experiment, you may be sharing data with the creator and administrators of this experiment. Read the data handling policy thay have provided below to decide on whether you want to participate in this experiment.";
  consentText.font = [PacoFont pacoTableCellDetailFont];
  consentText.textColor = [UIColor blackColor];
  consentText.backgroundColor = [UIColor clearColor];
  consentText.numberOfLines = 0;
  [self.view addSubview:consentText];
  [consentText sizeToFit];
  CGRect textFrame = consentText.frame;
  textFrame.origin.x = 10;
  textFrame.origin.y = boldText.frame.origin.y + boldText.frame.size.height + 10;
  consentText.frame = textFrame;

  UITextView* textExpView = [[UITextView alloc] initWithFrame:CGRectMake(10, consentText.frame.origin.y + consentText.frame.size.height + 15, self.view.frame.size.width - 20, 180)];
  textExpView.backgroundColor = [UIColor whiteColor];
  textExpView.font = [UIFont fontWithName:@"HelveticaNeue" size:16];
  textExpView.textColor = [PacoColor pacoDarkBlue];
  textExpView.text = self.experiment.informedConsentForm;
  textExpView.editable = NO;
  [self.view addSubview:textExpView];
  [textExpView sizeToFit];

  UIButton* iConsent = [UIButton buttonWithType:UIButtonTypeRoundedRect];
  iConsent.frame = CGRectMake((self.view.frame.size.width - 120) / 2, self.view.frame.size.height - 45 - self.navigationController.navigationBar.frame.size.height, 120, 35);
  [iConsent setTitle:@"I Consent" forState:UIControlStateNormal];
  if (IS_IOS_7) {
    iConsent.frame = CGRectMake((self.view.frame.size.width - 120) / 2, self.view.frame.size.height - 65 - self.navigationController.navigationBar.frame.size.height, 120, 35);
    iConsent.titleLabel.font = [PacoFont pacoNormalButtonFont];
  }
  [iConsent addTarget:self action:@selector(onAccept) forControlEvents:UIControlEventTouchUpInside];
  [self.view addSubview:iConsent];
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
