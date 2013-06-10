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

#import "PacoColor.h"
#import "PacoFindExperimentsViewController.h"
#import "PacoLayout.h"
#import "PacoMenuButton.h"
#import "PacoRunningExperimentsViewController.h"
#import "PacoTitleView.h"
#import "PacoClient.h"
#import "PacoLoginScreenViewController.h"

@implementation PacoMainViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
      PacoTitleView *title = [[PacoTitleView alloc] initIconAndText:@"Paco"];
      self.navigationItem.titleView = title;
    }
    return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];

  UIView *view = self.view;
  assert(view);
  view.backgroundColor = [PacoColor pacoLightBlue];

  PacoMenuButton *buttonFind = [[PacoMenuButton alloc] init];
  buttonFind.text.text = @"Find Experiments";
  [buttonFind.button setBackgroundImage:[UIImage imageNamed:@"find_experiments_normal.png"] forState:UIControlStateNormal];
  [buttonFind.button setBackgroundImage:[UIImage imageNamed:@"find_experiments_pressed.png"] forState:UIControlStateHighlighted];
  [buttonFind.button setBackgroundImage:[UIImage imageNamed:@"find_experiments_disabled.png"] forState:UIControlStateDisabled];
  [buttonFind.button addTarget:self action:@selector(onFindAllExperiments) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonFind];
  [buttonFind sizeToFit];

  PacoMenuButton *buttonRunningExperiment = [[PacoMenuButton alloc] init];
  buttonRunningExperiment.text.text = @"Current Experiments";
  [buttonRunningExperiment.button setBackgroundImage:[UIImage imageNamed:@"experiment_normal.png"] forState:UIControlStateNormal];
  [buttonRunningExperiment.button setBackgroundImage:[UIImage imageNamed:@"experiment_pressed.png"] forState:UIControlStateHighlighted];
  [buttonRunningExperiment.button setBackgroundImage:[UIImage imageNamed:@"experiment_disabled.png"] forState:UIControlStateDisabled];
  [buttonRunningExperiment.button addTarget:self action:@selector(onRunningExperiments) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonRunningExperiment];
  [buttonRunningExperiment sizeToFit];

  PacoMenuButton *buttonExploreData = [[PacoMenuButton alloc] init];
  buttonExploreData.text.text = @"Explore Your Data";
  [buttonExploreData.button setBackgroundImage:[UIImage imageNamed:@"current_experiments_normal.png"] forState:UIControlStateNormal];
  [buttonExploreData.button setBackgroundImage:[UIImage imageNamed:@"current_experiments_pressed.png"] forState:UIControlStateHighlighted];
  [buttonExploreData.button setBackgroundImage:[UIImage imageNamed:@"current_experiments_disabled.png"] forState:UIControlStateDisabled];
  [buttonExploreData.button addTarget:self action:@selector(onExploreData) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonExploreData];
  [buttonExploreData sizeToFit];

  PacoMenuButton *buttonFeedback = [[PacoMenuButton alloc] init];
  buttonFeedback.text.text = @"Contact us";
  [buttonFeedback.button setBackgroundImage:[UIImage imageNamed:@"feedback_normal.png"] forState:UIControlStateNormal];
//  [buttonFeedback.button setBackgroundImage:[UIImage imageNamed:@"feedback_pressed.png"] forState:UIControlStateHighlighted];
//  [buttonFeedback.button setBackgroundImage:[UIImage imageNamed:@"feedback_disabled.png"] forState:UIControlStateDisabled];
  [buttonFeedback.button addTarget:self action:@selector(onSendFeedback) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonFeedback];
  [buttonFeedback sizeToFit];

  PacoMenuButton *buttonUserGuide = [[PacoMenuButton alloc] init];
  buttonUserGuide.text.text = @"User Guide";
  [buttonUserGuide.button setBackgroundImage:[UIImage imageNamed:@"question.png"] forState:UIControlStateNormal];
  [buttonUserGuide.button addTarget:self action:@selector(onUserGuide) forControlEvents:UIControlEventTouchUpInside];
  [view addSubview:buttonUserGuide];
  [buttonUserGuide sizeToFit];

  CGRect layoutRect = CGRectInset(view.bounds, 15, 0);
  layoutRect.size.height -= 60;
  NSArray *buttons = [NSArray arrayWithObjects:buttonFind, buttonRunningExperiment, buttonExploreData, buttonFeedback, buttonUserGuide, nil];
  [PacoLayout layoutViews:buttons inGridWithWidth:2 gridHeight:3 inRect:layoutRect];

  [view setNeedsLayout];
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
  // Dispose of any resources that can be recreated.
}

#pragma mark - Button Callbacks

- (void)onRunningExperiments {
  void(^finishBlock)() = ^{
    PacoRunningExperimentsViewController *controller = [[PacoRunningExperimentsViewController alloc] init];
    [self.navigationController pushViewController:controller animated:YES];
  };
  
  if ([[PacoClient sharedInstance] isLoggedIn]) {
    finishBlock();
  }else{
    [self loginWithCompletionBlock:^(NSError *error) {
      if (!error) {
        finishBlock();
      }
    }];
  }
}

- (void)onExploreData {
}

- (void)onUserGuide {
}

- (void)onFindAllExperiments {
  void(^finishBlock)() = ^{
    PacoFindExperimentsViewController *controller = [[PacoFindExperimentsViewController alloc] init];
    [self.navigationController pushViewController:controller animated:YES];
  };
  
  if ([[PacoClient sharedInstance] isLoggedIn]) {
    finishBlock();
  }else{
    [self loginWithCompletionBlock:^(NSError *error) {
      if (!error) {
        finishBlock();
      }
    }];
  }
}

- (void)onSendFeedback {
}

#pragma mark bring up login flow if necessary
- (void)showLoginScreenWithCompletionBlock:(LoginCompletionBlock)block
{
  PacoLoginScreenViewController *loginViewController = [PacoLoginScreenViewController controllerWithCompletionBlock:block];
  [self.navigationController presentViewController:loginViewController animated:YES completion:nil];
}


- (void)loginWithCompletionBlock:(LoginCompletionBlock)block
{
  if ([[PacoClient sharedInstance] isLoggedIn]) {
    if (block) {
      block(nil);
    }
    return;
  }
  
  if ([[PacoClient sharedInstance] isUserAccountStored]) {
    [[PacoClient sharedInstance] loginWithCompletionHandler:^(NSError* error) {
      if (error) {
        [self showLoginScreenWithCompletionBlock:block];
      }else{
        if (block != nil) {
          block(nil);
        }
      }
    }];
  }else{
    [self showLoginScreenWithCompletionBlock:block];
  }
  
  
  
  // Attempt a PACO login.
  /*
   [[PacoClient sharedInstance] loginWithOAuth2CompletionHandler:^(NSError *error) {
   if (!error) {
   NSLog(@"PACO LOGIN SUCCESS!");
   
   UILocalNotification *notification = [launchOptions objectForKey:UIApplicationLaunchOptionsLocalNotificationKey];
   if (notification) {
   [[PacoClient sharedInstance].scheduler handleLocalNotification:notification];
   }
   } else {
   NSLog(@"PACO LOGIN FAILURE! %@", error);
   }
   }];
   */
}


@end
