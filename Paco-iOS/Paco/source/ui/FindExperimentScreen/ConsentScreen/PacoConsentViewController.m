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
#import "UIFont+Paco.h"
#import "PacoExperimentSchedule.h"
#import "PacoAlertView.h"
#import "PacoFindMyExperimentsViewController.h"
#import "PacoPublicExperimentController.h"

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
  self.view = [[UIScrollView alloc] initWithFrame:self.view.frame];
  self.view.backgroundColor = [UIColor pacoBackgroundWhite];
  self.automaticallyAdjustsScrollViewInsets = NO;

  CGFloat xPosition = 10;
  CGFloat yPosition = 10;
  CGFloat width = self.view.frame.size.width - 20;
  
  CGRect titleFrame = CGRectMake(xPosition, yPosition, width, 0);
  UILabel* boldTitle = [[UILabel alloc] initWithFrame:titleFrame];
  boldTitle.text = NSLocalizedString(@"Data Handling & Privacy Agreement between You and the Experiment Creator", nil);
  boldTitle.font = [UIFont pacoBoldFont];
  boldTitle.textColor = [UIColor blackColor];
  boldTitle.backgroundColor = [UIColor clearColor];
  boldTitle.numberOfLines = 0;
  [self.view addSubview:boldTitle];
  [boldTitle sizeToFit];
  CGRect frame = boldTitle.frame;
  boldTitle.frame = CGRectMake(xPosition, yPosition, frame.size.width, frame.size.height);
  yPosition += boldTitle.frame.size.height + 10;

  CGRect consentTextFrame = CGRectMake(xPosition, yPosition, width, 0);
  UILabel* consentText = [[UILabel alloc] initWithFrame:consentTextFrame];
  consentText.text = NSLocalizedString(@"Consent Text", nil);
  consentText.font = [UIFont pacoTableCellDetailFont];
  consentText.textColor = [UIColor blackColor];
  consentText.backgroundColor = [UIColor clearColor];
  consentText.numberOfLines = 0;
  [self.view addSubview:consentText];
  [consentText sizeToFit];
  frame = consentText.frame;
  consentText.frame = CGRectMake(xPosition, yPosition, frame.size.width, frame.size.height);
  yPosition += consentText.frame.size.height + 15;

  CGRect expViewFrame = CGRectMake(xPosition, yPosition, width, 0);
  UILabel* expViewText = [[UILabel alloc] initWithFrame:expViewFrame];
  expViewText.backgroundColor = [UIColor whiteColor];
  expViewText.font = [UIFont fontWithName:@"HelveticaNeue" size:16];
  expViewText.textColor = [UIColor pacoDarkBlue];
  expViewText.text = self.definition.informedConsentForm;
  expViewText.numberOfLines = 0;
  [self.view addSubview:expViewText];
  [expViewText sizeToFit];
  frame = expViewText.frame;
  expViewText.frame = CGRectMake(xPosition, yPosition, frame.size.width, frame.size.height);
  yPosition += expViewText.frame.size.height + 15;

  UIButton* iConsent = [UIButton buttonWithType:UIButtonTypeRoundedRect];
  [iConsent setTitle:NSLocalizedString(@"I Consent", nil) forState:UIControlStateNormal];
  if (IS_IOS_7) {
    iConsent.titleLabel.font = [UIFont pacoNormalButtonFont];
  }
  [iConsent addTarget:self action:@selector(onAccept) forControlEvents:UIControlEventTouchUpInside];
  [self.view addSubview:iConsent];
  [iConsent sizeToFit];
  frame = iConsent.frame;
  frame.origin.x = (self.view.frame.size.width - frame.size.width) / 2;
  frame.origin.y = yPosition;
  iConsent.frame = frame;
  yPosition += iConsent.frame.size.height + 10;

  [(UIScrollView*)self.view setContentSize:CGSizeMake(self.view.frame.size.width, yPosition)];
}

- (void)onAccept {
  if (!self.definition.schedule.userEditable) {
    void(^completionBlock)() = ^{
      dispatch_async(dispatch_get_main_queue(), ^{
        NSString* title = NSLocalizedString(@"Congratulations!", nil);
        NSString* message = NSLocalizedString(@"You've successfully joined this experiment!", nil);
        [[[UIAlertView alloc] initWithTitle:title
                                    message:message
                                   delegate:self
                          cancelButtonTitle:@"OK"
                          otherButtonTitles:nil] show];
      });
    };
    [[PacoClient sharedInstance] joinExperimentWithDefinition:self.definition
                                                     schedule:self.definition.schedule
                                              completionBlock:completionBlock];
  } else {
    PacoEditScheduleViewController *edit =
        [PacoEditScheduleViewController controllerWithDefinition:self.definition];
    [self.navigationController pushViewController:edit animated:YES];
  }
}

- (void)goBack {
  UIViewController* controllerToGoBack = nil;
  for (UIViewController *controller in [self.navigationController viewControllers]) {
    if ([controller isKindOfClass:[PacoFindMyExperimentsViewController class]] ||
        [controller isKindOfClass:[PacoPublicExperimentController class]]) {
      controllerToGoBack = controller;
      break;
    }
  }
  NSAssert(controllerToGoBack, @"should have a valid controller to go back to");
  [self.navigationController popToViewController:controllerToGoBack animated:YES];
}

- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex {
  [self goBack];
}



@end
