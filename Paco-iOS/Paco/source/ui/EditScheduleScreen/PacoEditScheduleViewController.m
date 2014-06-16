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
#import "PacoExperimentSchedule.h"
#import "PacoFindMyExperimentsViewController.h"
#import "PacoPublicExperimentController.h"

@interface PacoEditScheduleViewController ()<UIAlertViewDelegate>

@property (nonatomic, retain) PacoExperimentDefinition *definition;
@property(nonatomic, assign) BOOL isJoinSuccessful;

@end

@implementation PacoEditScheduleViewController

- (instancetype)initWithDefinition:(PacoExperimentDefinition*)definition {
  self = [super initWithNibName:nil bundle:nil];
  if (self) {
    _definition = definition;
    self.title = _definition.title;
    self.navigationItem.rightBarButtonItem =
      [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Join", nil)
                                       style:UIBarButtonItemStyleDone
                                      target:self
                                      action:@selector(onJoin)];
  }
  return self;
}

+ (instancetype)controllerWithDefinition:(PacoExperimentDefinition*)definition {
  return [[[self class] alloc] initWithDefinition:definition];
}

- (void)viewDidLoad {
  [super viewDidLoad];
  self.view = [PacoScheduleEditView viewWithFrame:CGRectZero schedule:self.definition.schedule];
}

- (void)onJoin {
  NSString* errorMsg = [[(PacoScheduleEditView*)self.view schedule] validate];
  if (errorMsg) {
    [[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Oops", nil)
                                message:errorMsg
                               delegate:nil
                      cancelButtonTitle:@"OK"
                      otherButtonTitles:nil] show];
    return;
  }

  
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
  
  PacoExperimentSchedule* modifiedSchedule = [(PacoScheduleEditView*)self.view schedule];
  [[PacoClient sharedInstance] joinExperimentWithDefinition:self.definition
                                                   schedule:modifiedSchedule
                                            completionBlock:completionBlock];
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


- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
}

@end
