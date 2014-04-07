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

#import "UIColor+Paco.h"
#import "PacoClient.h"
#import "PacoEditScheduleViewController.h"
#import "PacoModel.h"
#import "PacoService.h"
#import "PacoExperimentDefinition.h"
#import "PacoFont.h"
#import "PacoExperimentSchedule.h"
#import "PacoAlertView.h"

@interface PacoConsentViewController () <UIAlertViewDelegate>
@property (nonatomic, retain) PacoExperimentDefinition* definition;
@end

@implementation PacoConsentViewController
@synthesize definition = _definition;

+ (PacoConsentViewController*)controllerWithDefinition:(PacoExperimentDefinition *)definition {
  PacoConsentViewController* controller =
      [[PacoConsentViewController alloc] initWithNibName:nil bundle:nil];
  controller.definition = definition;
  controller.navigationItem.title = definition.title;
  return controller;
}


- (void)viewDidLoad {
  [super viewDidLoad];
  if ([self respondsToSelector:@selector(edgesForExtendedLayout)]) {
    self.edgesForExtendedLayout = UIRectEdgeNone;
  }
  self.view.backgroundColor = [UIColor pacoBackgroundWhite];

  UILabel* boldTitle = [[UILabel alloc] initWithFrame:CGRectMake(10, 10, self.view.frame.size.width - 20, 30)];
  boldTitle.text = NSLocalizedString(@"Data Handling & Privacy Agreement between You and the Experiment Creator", nil);
  boldTitle.font = [PacoFont pacoBoldFont];
  boldTitle.textColor = [UIColor blackColor];
  boldTitle.backgroundColor = [UIColor clearColor];
  boldTitle.numberOfLines = 0;
  [self.view addSubview:boldTitle];
  [boldTitle sizeToFit];

  UILabel* consentText = [[UILabel alloc] initWithFrame:CGRectMake(10, boldTitle.frame.origin.y + boldTitle.frame.size.height + 10, self.view.frame.size.width - 20, 0)];
  consentText.text = NSLocalizedString(@"Consent Text", nil);
  consentText.font = [PacoFont pacoTableCellDetailFont];
  consentText.textColor = [UIColor blackColor];
  consentText.backgroundColor = [UIColor clearColor];
  consentText.numberOfLines = 0;
  [self.view addSubview:consentText];
  [consentText sizeToFit];

  UITextView* expViewText = [[UITextView alloc] initWithFrame:CGRectMake(10, consentText.frame.origin.y + consentText.frame.size.height + 15, self.view.frame.size.width - 20, 180)];
  expViewText.backgroundColor = [UIColor whiteColor];
  expViewText.font = [UIFont fontWithName:@"HelveticaNeue" size:16];
  expViewText.textColor = [UIColor pacoDarkBlue];
  expViewText.text = self.definition.informedConsentForm;
  expViewText.editable = NO;
  [self.view addSubview:expViewText];

  UIButton* iConsent = [UIButton buttonWithType:UIButtonTypeRoundedRect];
  [iConsent setTitle:NSLocalizedString(@"I Consent", nil) forState:UIControlStateNormal];
  if (IS_IOS_7) {
    iConsent.titleLabel.font = [PacoFont pacoNormalButtonFont];
  }
  [iConsent addTarget:self action:@selector(onAccept) forControlEvents:UIControlEventTouchUpInside];
  [self.view addSubview:iConsent];
  [iConsent sizeToFit];
  CGRect frame = iConsent.frame;
  frame.origin.x = (self.view.frame.size.width - frame.size.width) / 2;
  frame.origin.y = self.view.frame.size.height - 65 - self.navigationController.navigationBar.frame.size.height;
  iConsent.frame = frame;
}

- (void)onAccept {
  if (!self.definition.schedule.userEditable) {
    [[PacoClient sharedInstance] joinExperimentWithDefinition:self.definition
                                                  andSchedule:self.definition.schedule];

    NSString* title = NSLocalizedString(@"Congratulations!", nil);
    NSString* message = NSLocalizedString(@"You've successfully joined this experiment!", nil);
    [PacoAlertView showAlertWithTitle:title
                              message:message
                         dismissBlock:^(NSInteger buttonIndex) {
                           [self.navigationController popToRootViewControllerAnimated:YES];
                         }
                    cancelButtonTitle:@"OK"
                    otherButtonTitles:nil];
  } else {
    PacoEditScheduleViewController *edit = [[PacoEditScheduleViewController alloc] init];
    edit.definition = self.definition;
    [self.navigationController pushViewController:edit animated:YES];
  }
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
}

@end
