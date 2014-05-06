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

#import "PacoMainViewController.h"
#import <MessageUI/MessageUI.h>

#import "UIColor+Paco.h"
#import "PacoFindExperimentsViewController.h"
#import "PacoLayout.h"
#import "PacoMenuButton.h"
#import "PacoRunningExperimentsViewController.h"
#import "PacoTitleView.h"
#import "PacoClient.h"
#import "PacoLoginScreenViewController.h"
#import "PacoContactUsViewController.h"
#import "PacoWebViewController.h"
#import "GoogleClientLogin.h"
#import "JCNotificationCenter.h"
#import "JCNotificationBannerPresenterSmokeStyle.h"
#import "PacoPublicExperimentController.h"
#import "PacoAlertView.h"
#import "NSString+Paco.h"
#import "PacoOpenSourceLibViewController.h"

@interface PacoMainViewController ()<MFMailComposeViewControllerDelegate>

@property (nonatomic, retain) PacoWebViewController* webViewController;

@end

@implementation PacoMainViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    PacoTitleView *title = [PacoTitleView viewWithDefaultIconAndText:@"Paco"];
    self.navigationItem.titleView = title;

    UIButton* infoButton = [UIButton buttonWithType:UIButtonTypeInfoLight];
    [infoButton addTarget:self action:@selector(onInfoSelect:) forControlEvents:UIControlEventTouchUpInside];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithCustomView:infoButton];
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];

  //fix the layout of menu buttons on iOS7
  if ([self respondsToSelector:@selector(edgesForExtendedLayout)]) {
    self.edgesForExtendedLayout = UIRectEdgeNone;
  }

  UIView *view = self.view;
  assert(view);
  view.backgroundColor = [UIColor pacoBackgroundWhite];

  //if user has running experiments, load RunningExperimentsViewController
  if ([[PacoClient sharedInstance] hasRunningExperiments]) {
    [self onRunningExperiments];
  }

  PacoMenuButton *buttonFind = [[PacoMenuButton alloc] init];
  buttonFind.text.text = NSLocalizedString(@"Find My Experiments",nil);
  [buttonFind.button setBackgroundImage:[UIImage imageNamed:@"find_experiments_normal.png"] forState:UIControlStateNormal];
  [buttonFind.button setBackgroundImage:[UIImage imageNamed:@"find_experiments_pressed.png"] forState:UIControlStateHighlighted];
  [buttonFind.button setBackgroundImage:[UIImage imageNamed:@"find_experiments_disabled.png"] forState:UIControlStateDisabled];
  [buttonFind.button addTarget:self action:@selector(onFindAllExperiments) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonFind];
  [buttonFind sizeToFit];

  PacoMenuButton *buttonRunningExperiment = [[PacoMenuButton alloc] init];
  buttonRunningExperiment.text.text = NSLocalizedString(@"Current Experiments",nil);
  [buttonRunningExperiment.button setBackgroundImage:[UIImage imageNamed:@"experiment_normal.png"] forState:UIControlStateNormal];
  [buttonRunningExperiment.button setBackgroundImage:[UIImage imageNamed:@"experiment_pressed.png"] forState:UIControlStateHighlighted];
  [buttonRunningExperiment.button setBackgroundImage:[UIImage imageNamed:@"experiment_disabled.png"] forState:UIControlStateDisabled];
  [buttonRunningExperiment.button addTarget:self action:@selector(onRunningExperiments) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonRunningExperiment];
  [buttonRunningExperiment sizeToFit];

  
  PacoMenuButton* publicExperimentButton = [[PacoMenuButton alloc] init];
  publicExperimentButton.text.text = NSLocalizedString(@"Find Public Experiments",nil);
  [publicExperimentButton.button setBackgroundImage:[UIImage imageNamed:@"find_experiments_normal.png"]
                                           forState:UIControlStateNormal];
  [publicExperimentButton.button setBackgroundImage:[UIImage imageNamed:@"find_experiments_pressed.png"]
                                           forState:UIControlStateHighlighted];
  [publicExperimentButton.button setBackgroundImage:[UIImage imageNamed:@"find_experiments_disabled.png"]
                                           forState:UIControlStateDisabled];
  [publicExperimentButton.button addTarget:self
                                     action:@selector(onExplorePublicExperiments)
                           forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:publicExperimentButton];
  [publicExperimentButton sizeToFit];

  PacoMenuButton *buttonExploreData = [[PacoMenuButton alloc] init];
  buttonExploreData.text.text = NSLocalizedString(@"Explore Your Data",nil);
  [buttonExploreData.button setBackgroundImage:[UIImage imageNamed:@"current_experiments_normal.png"] forState:UIControlStateNormal];
  [buttonExploreData.button setBackgroundImage:[UIImage imageNamed:@"current_experiments_pressed.png"] forState:UIControlStateHighlighted];
  [buttonExploreData.button setBackgroundImage:[UIImage imageNamed:@"current_experiments_disabled.png"] forState:UIControlStateDisabled];
  [buttonExploreData.button addTarget:self action:@selector(onExploreData) forControlEvents:UIControlEventTouchUpInside];
  //  [view addSubview:buttonExploreData];
  [buttonExploreData sizeToFit];

  PacoMenuButton *buttonCreateExperiment = [[PacoMenuButton alloc] init];
  buttonCreateExperiment.text.text = NSLocalizedString(@"Create an experiment",nil);
  [buttonCreateExperiment.button setBackgroundImage:[UIImage imageNamed:@"experiment_normal.png"] forState:UIControlStateNormal];
  [buttonCreateExperiment.button setBackgroundImage:[UIImage imageNamed:@"experiment_pressed.png"] forState:UIControlStateHighlighted];
  [buttonCreateExperiment.button setBackgroundImage:[UIImage imageNamed:@"experiment_disabled.png"] forState:UIControlStateDisabled];
  [buttonCreateExperiment.button addTarget:self action:@selector(onCreateAnExperiment) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonCreateExperiment];
  [buttonCreateExperiment sizeToFit];

  PacoMenuButton *buttonUserGuide = [[PacoMenuButton alloc] init];
  buttonUserGuide.text.text = NSLocalizedString(@"User Guide",nil);
  [buttonUserGuide.button setBackgroundImage:[UIImage imageNamed:@"question.png"] forState:UIControlStateNormal];
  [buttonUserGuide.button addTarget:self action:@selector(onUserGuide) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonUserGuide];
  [buttonUserGuide sizeToFit];

  PacoMenuButton *buttonFeedback = [[PacoMenuButton alloc] init];
  buttonFeedback.text.text = NSLocalizedString(@"Contact us",nil);
  [buttonFeedback.button setBackgroundImage:[UIImage imageNamed:@"feedback_normal.png"] forState:UIControlStateNormal];
  //  [buttonFeedback.button setBackgroundImage:[UIImage imageNamed:@"feedback_pressed.png"] forState:UIControlStateHighlighted];
  //  [buttonFeedback.button setBackgroundImage:[UIImage imageNamed:@"feedback_disabled.png"] forState:UIControlStateDisabled];
  [buttonFeedback.button addTarget:self action:@selector(onSendFeedback) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonFeedback];
  [buttonFeedback sizeToFit];

  CGRect layoutRect = CGRectInset(view.bounds, 15, 0);
  layoutRect.size.height -= 60;
  NSArray *buttons = @[buttonFind, buttonRunningExperiment, publicExperimentButton, buttonCreateExperiment, buttonUserGuide, buttonFeedback];
  [PacoLayout layoutViews:buttons inGridWithWidth:2 gridHeight:3 inRect:layoutRect];

  [view setNeedsLayout];

  [[PacoClient sharedInstance] loginWithCompletionBlock:^(NSError *error) {
    NSString* message = NSLocalizedString(@"You are logged in successfully!", nil);
    if (error) {
      message = [GoogleClientLogin descriptionForError:error.domain];
      if (0 == [message length]) {//just in case
        message = NSLocalizedString(@"Something went wrong, please try again.", nil);
      }
    }
    [JCNotificationCenter sharedCenter].presenter = [[JCNotificationBannerPresenterSmokeStyle alloc] init];
    JCNotificationBanner* banner = [[JCNotificationBanner alloc] initWithTitle:@""
                                                                       message:message
                                                                       timeout:2.
                                                                    tapHandler:nil];
    [[JCNotificationCenter sharedCenter] enqueueNotification:banner];
  }];
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
  // Dispose of any resources that can be recreated.
}

#pragma mark - Button Callbacks

- (void)onRunningExperiments {
  PacoRunningExperimentsViewController *controller = [[PacoRunningExperimentsViewController alloc] init];
  [self.navigationController pushViewController:controller animated:YES];
}

- (void)onExplorePublicExperiments {
  PacoPublicExperimentController* controller = [[PacoPublicExperimentController alloc] initWithNibName:nil bundle:nil];
  [self.navigationController pushViewController:controller animated:YES];
}

- (void)onExploreData {
}

- (void)onCreateAnExperiment {
  NSString* msg = NSLocalizedString(@"How to Message", nil);
  [[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"How to Create an Experiment",nil)
                              message:msg
                             delegate:nil
                    cancelButtonTitle:@"OK"
                    otherButtonTitles:nil] show];
}

- (void)onUserGuide {
  [self loadWebView:NSLocalizedString(@"User Guide",nil) andHTML:@"help"];
}

- (void)onFindAllExperiments {
  PacoFindExperimentsViewController *controller = [[PacoFindExperimentsViewController alloc] init];
  [self.navigationController pushViewController:controller animated:YES];
}

- (void)onSendFeedback {
  PacoContactUsViewController *controller = [[PacoContactUsViewController alloc] init];
  [self.navigationController pushViewController:controller animated:YES];
}

- (void)onInfoSelect:(UIButton *)sender {
  NSString* version = [[NSBundle mainBundle] infoDictionary][(NSString*)kCFBundleVersionKey];
  NSString* title = [NSString stringWithFormat:@"%@ %@", NSLocalizedString(@"Version", nil), version];
  UIActionSheet* actionSheet = [[UIActionSheet alloc] initWithTitle:title
                                                           delegate:self
                                                  cancelButtonTitle:NSLocalizedString(@"Close", nil)
                                             destructiveButtonTitle:nil
                                                  otherButtonTitles:NSLocalizedString(@"About Paco", nil),
                                                                    NSLocalizedString(@"Send Logs to Paco Team", nil),
                                                                    NSLocalizedString(@"Open Source Libraries", nil), nil];
  [actionSheet showInView:self.view];
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex {
  switch (buttonIndex) {
    case 0: {
      [self loadWebView:NSLocalizedString(@"About Paco",nil) andHTML:@"welcome_paco"];
      break;
    }
    case 1: {
      [self openMailViewController];
      break;
    }
    case 2: {
      [self opensourceCreditsPage];
      break;
    }
    default:
      break;
  }
}

- (void)opensourceCreditsPage {
  PacoOpenSourceLibViewController* creditsViewController = [[PacoOpenSourceLibViewController alloc] init];
  [self.navigationController pushViewController:creditsViewController animated:YES];
}

- (void)loadWebView:(NSString*)title andHTML:(NSString*)htmlName {
  NSString* urlString = [[NSBundle mainBundle] pathForResource:htmlName ofType:@"html"];
  self.webViewController = [[PacoWebViewController alloc] initWithNibName:nil bundle:nil];
  [self.webViewController setTitle:title];
  [self.webViewController loadWebView:[NSURL fileURLWithPath:urlString]];
  UIBarButtonItem* backBarButton = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Main", nil)
                                                                    style:UIBarButtonItemStylePlain
                                                                   target:self
                                                                   action:@selector(removeWebView)];
  self.webViewController.navigationItem.backBarButtonItem = backBarButton;
  [self.navigationController pushViewController:self.webViewController animated:YES];
}

- (void)removeWebView {
    [self.webViewController.navigationController popToRootViewControllerAnimated:YES];
}

- (void)openMailViewController {
  if ([MFMailComposeViewController canSendMail]) {
    MFMailComposeViewController* mailer = [[MFMailComposeViewController alloc] init];
    mailer.mailComposeDelegate = self;
    [mailer setSubject:NSLocalizedString(@"Paco Logs", nil)];
    NSArray* toRecipients = @[@"paco-support@googlegroups.com"];
    [mailer setToRecipients:toRecipients];
    NSError* error;
    NSArray* contents = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:[NSString pacoLogDirectory] error:&error];
    if (error) {
      DDLogError(@"Failed to fetch filenames from Logs directory: %@", [error description]);
      return;
    }
    for (NSString* fileName in contents) {
      NSString* path = [[NSString pacoLogDirectory] stringByAppendingFormat:@"/%@", fileName];
      NSData* data = [NSData dataWithContentsOfFile:path];
      [mailer addAttachmentData:data mimeType:@"text/plain" fileName:fileName];
    }
    [self presentViewController:mailer animated:YES completion:nil];
  }
  else {
    NSString* title = NSLocalizedString(@"Email not configured", nil);
    NSString* message = NSLocalizedString(@"Configure email message", nil);
    [PacoAlertView showAlertWithTitle:title message:message cancelButtonTitle:@"OK"];
  }
}

#pragma mark --
#pragma mark Dismiss Mail ViewController
- (void)mailComposeController:(MFMailComposeViewController *)controller
          didFinishWithResult:(MFMailComposeResult)result
                        error:(NSError *)error {
  NSString* resultString;
  switch (result) {
    case MFMailComposeResultCancelled: {
      resultString = NSLocalizedString(@"Email Cancelled", nil);
      break;
    }
    case MFMailComposeResultSaved: {
      resultString = NSLocalizedString(@"Email Saved", nil);
      break;
    }
    case MFMailComposeResultSent: {
      resultString = NSLocalizedString(@"Email Sent", nil);
      break;
    }
    case MFMailComposeResultFailed: {
      resultString = NSLocalizedString(@"Email Failed", nil);
      break;
    }

    default:
      break;
  }
  [self dismissViewControllerAnimated:YES completion:nil];
  [PacoAlertView showAlertWithTitle:NSLocalizedString(@"Mail Status", nil)
                            message:resultString
                  cancelButtonTitle:@"OK"];
}

@end
