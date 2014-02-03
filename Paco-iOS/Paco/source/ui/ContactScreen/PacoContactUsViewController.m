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

#import "PacoContactUsViewController.h"

#import "PacoColor.h"
#import "PacoMenuButton.h"
#import "PacoLayout.h"
#import <MessageUI/MessageUI.h>
#import "PacoAlertView.h"
#import "PacoAlertView.h"

static NSString *const browseUserGroupURL = @"https://groups.google.com/forum/m/#!forum/paco-users";
static NSString *const browsePacoWebsite = @"https://quantifiedself.appspot.com/main.jsp";

@interface PacoContactUsViewController ()<MFMailComposeViewControllerDelegate>

@end

@implementation PacoContactUsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
      self.navigationItem.title = @"Ways to talk to Paco";
    }
    return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];

  if ([self respondsToSelector:@selector(edgesForExtendedLayout)]) {
    self.edgesForExtendedLayout = UIRectEdgeNone;
  }

  UIView* view = self.view;
  assert(view);
  view.backgroundColor = [PacoColor pacoBackgroundWhite];

  PacoMenuButton* buttonEmailUserGroup = [[PacoMenuButton alloc] init];
  buttonEmailUserGroup.text.text = @"Email user group";
  [buttonEmailUserGroup.button setBackgroundImage:[UIImage imageNamed:@"feedback_normal.png"] forState:UIControlStateNormal];
  [buttonEmailUserGroup.button addTarget:self action:@selector(onEmailUserGroup) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonEmailUserGroup];
  [buttonEmailUserGroup sizeToFit];
  
  PacoMenuButton* buttonBrowseUserGroup = [[PacoMenuButton alloc] init];
  buttonBrowseUserGroup.text.text = @"Browse user group";
  [buttonBrowseUserGroup.button setBackgroundImage:[UIImage imageNamed:@"feedback_normal.png"] forState:UIControlStateNormal];
  [buttonBrowseUserGroup.button addTarget:self action:@selector(onBrowseUserGroup) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonBrowseUserGroup];
  [buttonBrowseUserGroup sizeToFit];

  PacoMenuButton* buttonBrowseWebsite = [[PacoMenuButton alloc] init];
  buttonBrowseWebsite.text.text = @"Browse Website";
  [buttonBrowseWebsite.button setBackgroundImage:[UIImage imageNamed:@"feedback_normal.png"] forState:UIControlStateNormal];
  [buttonBrowseWebsite.button addTarget:self action:@selector(onBrowseWebsite) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonBrowseWebsite];
  [buttonBrowseWebsite sizeToFit];

  [view setNeedsLayout];

  CGRect layoutRect = CGRectMake(15, 30, view.frame.size.width - 30, buttonBrowseUserGroup.frame.size.height);
  NSArray* array = [PacoLayout splitRectHorizontally:layoutRect numSections:2];
  NSArray* buttons = [NSArray arrayWithObjects:buttonEmailUserGroup, buttonBrowseUserGroup, nil];
  for (int i = 0; i < [buttons count]; i++) {
    PacoMenuButton* button = [buttons objectAtIndex:i];
    NSValue* valueRect = [array objectAtIndex:i];
    CGRect rect = [valueRect CGRectValue];
    button.frame = rect;
  }
  CGRect centerInRect = CGRectMake(0, 40, view.frame.size.width, view.frame.size.height / 2);
  CGRect rect = [PacoLayout centerRect:buttonBrowseWebsite.frame.size inRect:centerInRect];
  buttonBrowseWebsite.frame = rect;
}

- (void)onEmailUserGroup {
  if ([MFMailComposeViewController canSendMail]) {
    MFMailComposeViewController* mailer = [[MFMailComposeViewController alloc] init];
    mailer.mailComposeDelegate = self;
    [mailer setSubject:@"Paco Feedback"];
    NSArray* toRecipients = [NSArray arrayWithObject:@"paco-users@googlegroups.com"];
    [mailer setToRecipients:toRecipients];
    [self presentViewController:mailer animated:YES completion:nil];
  }
  else {
    NSString* title = @"Email not configured";
    NSString* message = @"Please configure your email account to send messages";
    [PacoAlertView showAlertWithTitle:title message:message cancelButtonTitle:@"OK"];
  }
}

- (void)onBrowseUserGroup {
  [PacoAlertView showAlertWithTitle:@"Note"
                            message:@"You are about to leave Paco and visit Safari."
                       dismissBlock:^(NSInteger buttonIndex) {
                         if (buttonIndex == 1) {
                           [self launchBrowserWithURL:browseUserGroupURL];
                         }
                       }
                  cancelButtonTitle:@"Cancel"
                  otherButtonTitles:@"OK", nil];
}

- (void)onBrowseWebsite {
  [PacoAlertView showAlertWithTitle:@"Note"
                            message:@"You are about to leave Paco and visit Safari."
                       dismissBlock:^(NSInteger buttonIndex) {
                         if (buttonIndex == 1) {
                           [self launchBrowserWithURL:browsePacoWebsite];
                         }
                       }
                  cancelButtonTitle:@"Cancel"
                  otherButtonTitles:@"OK", nil];
}

- (void)launchBrowserWithURL:(NSString *)url {
  [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

#pragma mark --
#pragma mark Dismiss Mail ViewController
- (void)mailComposeController:(MFMailComposeViewController *)controller
          didFinishWithResult:(MFMailComposeResult)result
                        error:(NSError *)error {
  NSString* resultString;
  switch (result) {
    case MFMailComposeResultCancelled: {
      resultString = @"Email sending cancelled";
      break;
    }
    case MFMailComposeResultSaved: {
      resultString = @"Email successfully saved in drafts folder";
      break;
    }
    case MFMailComposeResultSent: {
      resultString = @"Email successfully sent";
      break;
    }
    case MFMailComposeResultFailed: {
      resultString = @"Something went wrong, please try again later.";
      break;
    }

    default:
      break;
  }
  [self dismissViewControllerAnimated:YES completion:nil];
  [PacoAlertView showAlertWithTitle:@"Mail Status" message:resultString cancelButtonTitle:@"OK"];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
