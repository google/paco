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

#import "PacoRunningExperimentsViewController.h"

#import "PacoClient.h"
#import "UIColor+Paco.h"
#import "UIFont+Paco.h"
#import "PacoLoadingTableCell.h"
#import "PacoTableView.h"
#import "PacoQuestionScreenViewController.h"
#import "PacoExperiment.h"
#import "PacoAlertView.h"
#import "PacoSubtitleTableCell.h"
#import "PacoScheduler.h"
#import "UILocalNotification+Paco.h"
#import "PacoFindMyExperimentsViewController.h"
#import "PacoLoadingView.h"
#import "PacoExperimentDefinition.h"
#import "PacoModel.h"
#import "PacoEditScheduleAfterJoinController.h"
#import "PacoExperimentSchedule.h"
#import "PacoEventManager.h"
#import "PacoExperiment.h"

@interface PacoRunningExperimentsViewController () <UIAlertViewDelegate, PacoTableViewDelegate>

@property(nonatomic, strong) PacoExperiment* selectedExperiment;

@property(nonatomic, strong) UILabel* msgLabel;
@property(nonatomic, strong) UIButton* goToDefinitionButton;
@property(nonatomic, strong) UIBarButtonItem* refreshButton;


@end

@implementation PacoRunningExperimentsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.navigationItem.title = NSLocalizedString(@"Running Experiments", nil);
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Main",nil)
                                                                             style:UIBarButtonItemStylePlain
                                                                            target:self
                                                                            action:@selector(gotoMainPage)];
  }
  return self;
}

- (void)dealloc {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}


- (void)gotoMainPage {
  [self.navigationController popToRootViewControllerAnimated:YES];
}


- (void)viewDidLoad {
  [super viewDidLoad];
  
  PacoTableView* table = [[PacoTableView alloc] init];
  table.delegate = self;
  [table registerClass:[PacoSubtitleTableCell class] forStringKey:nil dataClass:[PacoExperiment class]];
  table.backgroundColor = [UIColor pacoBackgroundWhite];
  self.view = table;
}


- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];
  
  BOOL finishLoading = [[PacoClient sharedInstance].model hasLoadedRunningExperiments];
  if (finishLoading) {
    [self updateUI];
  } else {
    [(PacoTableView*)self.view setLoadingSpinnerEnabledWithLoadingText:NSLocalizedString(@"Loading Running Experiments ...", nil)];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(experimentsLoaded:)
                                                 name:kPacoNotificationLoadedRunningExperiments
                                               object:nil];
  }
}


- (void)updateUI {
  if (![[PacoClient sharedInstance].model hasLoadedRunningExperiments]) {
    DDLogError(@"Try to update view controller without running experiments loaded.");
    return;
  }
  
  dispatch_async(dispatch_get_main_queue(), ^{
    //update table view data
    ((PacoTableView*)self.view).data = [PacoClient sharedInstance].model.runningExperiments;
    
    //update label and button when there is no running experiment
    [self updateLabelAndButtons:[[PacoClient sharedInstance].model hasRunningExperiments]];
  });
}


- (void)updateLabelAndButtons:(BOOL)hasExperiments {
  //lazy initialization
  if (hasExperiments) {
    if (!self.refreshButton) {
      UIBarButtonItem* button = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Refresh", nil)
                                                                 style:UIBarButtonItemStyleDone
                                                                target:self
                                                                action:@selector(onClickRefresh)];
      self.refreshButton = button;
    }
  } else {
    if (!self.msgLabel) {
      UILabel *msgLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 300, 100)];
      [msgLabel setText:NSLocalizedString(@"You haven't joined any experiment yet.", nil)];
      [msgLabel setFont:[UIFont fontWithName:@"HelveticaNeue" size:14]];
      [msgLabel setTextColor:[UIColor darkGrayColor]];
      msgLabel.textAlignment = NSTextAlignmentCenter;
      [msgLabel sizeToFit];
      msgLabel.center = CGPointMake(self.view.center.x, self.view.center.y - 50);
      [self.view addSubview:msgLabel];
      self.msgLabel = msgLabel;
    }
    if (!self.goToDefinitionButton) {
      UIButton* msgButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
      [msgButton setTitle:NSLocalizedString(@"Go to Find My Experiments", nil) forState:UIControlStateNormal];
      msgButton.titleLabel.numberOfLines = 2;
      msgButton.titleLabel.textAlignment = NSTextAlignmentCenter;
      [msgButton addTarget:self action:@selector(goToFindMyExperiments:) forControlEvents:UIControlEventTouchUpInside];
      [self.view addSubview:msgButton];
      [msgButton sizeToFit];
      msgButton.center = self.view.center;
      self.goToDefinitionButton = msgButton;
    }
  }
  //update the hidden properties
  self.navigationItem.rightBarButtonItem = hasExperiments ? self.refreshButton : nil;
  self.msgLabel.hidden = hasExperiments;
  self.goToDefinitionButton.hidden = hasExperiments;
}


- (void)onClickRefresh {
  [[PacoLoadingView sharedInstance] showLoadingScreen];
  [[PacoClient sharedInstance] refreshRunningExperimentsWithBlock:^(NSError* error) {
    [[PacoLoadingView sharedInstance] dismissLoadingScreen];
    if (!error) {
      [self updateUI];
    } else {
      //if there is an error during refresh, keep the original data but show an error alert
      [PacoAlertView showRefreshErrorAlert];
    }
  }];
}


- (void)experimentsLoaded:(NSNotification*)notification {
  [self updateUI];
  
  NSError* error = (NSError*)notification.object;
  NSAssert([error isKindOfClass:[NSError class]] || error == nil, @"The notification should send an error!");
  if (error) {
    [PacoAlertView showGeneralErrorAlert];
  }
}


- (void)goToFindMyExperiments:(UIButton*)button {
  UINavigationController* navigationController = self.navigationController;
  [navigationController popToRootViewControllerAnimated:NO];
  PacoFindMyExperimentsViewController* controller = [[PacoFindMyExperimentsViewController alloc] init];
  [navigationController pushViewController:controller animated:NO];
}


//when app becomes active, viewWillAppear is not called, but we need to give running experiments a
//chance to update the Time to Participate label
- (void)appBecomeActive {
  if ([[PacoClient sharedInstance].model hasLoadedRunningExperiments]) {
    [self updateUI];
  } else {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(experimentsLoaded:)
                                                 name:kPacoNotificationLoadedRunningExperiments
                                               object:nil];
  }
}



#pragma mark - PacoTableViewDelegate

- (void)initializeCell:(UITableViewCell *)cell
              withData:(id)rowData
            forReuseId:(NSString *)reuseId {
  if ([rowData isKindOfClass:[PacoExperiment class]]) {
    PacoExperiment *experiment = rowData;
    cell.backgroundColor = [UIColor pacoBackgroundWhite];
    cell.imageView.image = [UIImage imageNamed:@"calculator.png"];
    cell.textLabel.font = [UIFont pacoTableCellFont];
    cell.textLabel.textColor = [UIColor pacoBlue];
    cell.textLabel.text = experiment.definition.title;
    cell.textLabel.numberOfLines = 2;
    if ([experiment isScheduledExperiment] &&
        [[PacoClient sharedInstance].scheduler hasActiveNotificationForExperiment:experiment.instanceId]) {
      cell.detailTextLabel.text = NSLocalizedString(@"Time to participate!", nil);
      cell.detailTextLabel.textColor = [UIColor colorWithRed:65./256. green:186./256. blue:34./256. alpha:.85];
      cell.detailTextLabel.font = [UIFont pacoBoldFont];
    } else {
      cell.detailTextLabel.text = nil;
    }
  } else {
    assert([rowData isKindOfClass:[NSArray class]]);
    NSArray *keyAndValue = rowData;
    NSString *key = keyAndValue[0];
    assert([key isEqualToString:@"LOADING"]);
    PacoLoadingTableCell *loading = (PacoLoadingTableCell *)cell;
    NSString *loadingText = keyAndValue[1];
    loading.loadingText = loadingText;
  }
}

- (BOOL)allowEditingSchedule {
  return self.selectedExperiment.schedule.userEditable;
}

- (void)cellSelected:(UITableViewCell *)cell rowData:(id)rowData reuseId:(NSString *)reuseId {
  if ([rowData isKindOfClass:[PacoExperiment class]]) { //YMZ: is this necessary?
    self.selectedExperiment = rowData;
    //TODO:@"Explore Data"
    NSString* cancelStr = NSLocalizedString(@"Cancel", nil);
    NSString* participateStr = NSLocalizedString(@"Participate", nil);
    NSString* modifyStr = NSLocalizedString(@"Modify Schedule", nil);
    NSString* stopStr = NSLocalizedString(@"Stop Experiment", nil);
    UIAlertView *alert = nil;

    NSString *title = self.selectedExperiment.definition.title;
    NSString *experimentId = self.selectedExperiment.instanceId;
    PacoParticipateStatus *stats =
        [[PacoClient sharedInstance].eventManager statsForExperiment:experimentId];
    if ([self.selectedExperiment isScheduledExperiment]) {
      title = [NSString stringWithFormat:@"%lu pings", (unsigned long)stats.numberOfNotifications];
      if (stats.numberOfNotifications > 0) {
        title = [title stringByAppendingFormat:@", %lu responses\n%@ response rate",
                 (unsigned long)stats.numberOfParticipations,
                 stats.percentageText];
      }
    }
    if (stats.numberOfSelfReports > 0) {
      title = [title stringByAppendingFormat:@"\n%lu self reports", (unsigned long)stats.numberOfSelfReports];
    }
    if ([self allowEditingSchedule]) {
      alert =  [[UIAlertView alloc] initWithTitle:title
                                          message:nil
                                         delegate:self
                                cancelButtonTitle:cancelStr
                                otherButtonTitles:participateStr, modifyStr, stopStr, nil];
    } else {
      alert =  [[UIAlertView alloc] initWithTitle:title
                                          message:nil
                                         delegate:self
                                cancelButtonTitle:cancelStr
                                otherButtonTitles:participateStr, stopStr, nil];
    }
    [alert show];
  }else{
    self.selectedExperiment = nil;
  }
}

- (void)dataUpdated:(UITableViewCell *)cell rowData:(id)rowData reuseId:(NSString *)reuseId {

}

- (void)reloadTable {

}

- (void)showParticipateController
{
  if (self.selectedExperiment == nil) {
    return;
  }

  PacoQuestionScreenViewController *questions =
      [PacoQuestionScreenViewController controllerWithExperiment:self.selectedExperiment];
  [self.navigationController pushViewController:questions animated:YES];
}

- (void)showEditScheduleController {
  EditScheduleCompletionBlock block = ^(PacoEditScheduleStatus status, PacoExperimentSchedule *schedule) {
    if (status == PacoEditScheduleStatusChanged) {
      void(^completionBlock)() = ^ {
        NSString* title = self.selectedExperiment.definition.title;
        NSString* msg = NSLocalizedString(@"Schedule is changed successfully.", nil);
        [PacoAlertView showAlertWithTitle:title
                                  message:msg
                        cancelButtonTitle:@"OK"];
      };
      [[PacoClient sharedInstance] changeScheduleForExperiment:self.selectedExperiment
                                                   newSchedule:schedule
                                               completionBlock:completionBlock];
    } else if (status == PacoEditScheduleStatusUnchanged) {
      [PacoAlertView showAlertWithTitle:nil
                                message:NSLocalizedString(@"No change was made.", nil)
                      cancelButtonTitle:@"OK"];
    }
  };

  PacoEditScheduleAfterJoinController* controller =
      [PacoEditScheduleAfterJoinController controllerWithSchedule:self.selectedExperiment.schedule
                                                  completionBlock:block];
  [self.navigationController pushViewController:controller animated:YES];
}

- (void)stopExperiment {
  //stop an experiment and update UI
  [[PacoClient sharedInstance] stopExperiment:self.selectedExperiment withBlock:^{
    dispatch_async(dispatch_get_main_queue(), ^{
      NSString* title = NSLocalizedString(@"Success", nil);
      NSString* message = NSLocalizedString(@"The experiment was stopped.", nil);
      [PacoAlertView showAlertWithTitle:title
                                message:message
                           dismissBlock:^(NSInteger buttonIndex) {
                             [self updateUI];
                           }
                      cancelButtonTitle:@"OK"
                      otherButtonTitles:nil];
    });
  }];
}

- (void)showStopConfirmAlert
{
  PacoAlertViewDidDismissBlock dismissBlock = ^(NSInteger buttonIndex){
    switch (buttonIndex) {
      case 0://cancel
        break;

      case 1://stop an experiment
        [self stopExperiment];
        break;

      default:
        NSAssert(NO, @"buttonIndex has to be 0 or 1");
        break;
    }
  };

  [PacoAlertView showAlertWithTitle:NSLocalizedString(@"Are you sure?", nil)
                            message:NSLocalizedString(@"All your data will be deleted permanently with this experiment.", nil)
                       dismissBlock:dismissBlock
                  cancelButtonTitle:NSLocalizedString(@"Cancel", nil)
                  otherButtonTitles:NSLocalizedString(@"Absolutely Sure!", nil), nil];
}


#pragma mark - UIAlertViewDelegate

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
  switch (buttonIndex) {
    case 0: // Cancel
      break;

    case 1: // Participate
      [self showParticipateController];
      break;

    case 2: { //modify schedule or stop
      if ([self allowEditingSchedule]) {
        [self showEditScheduleController];
      } else {
        [self showStopConfirmAlert];
      }
      break;
    }

    case 3: // Stop
      [self showStopConfirmAlert];
      break;

    default:
      NSAssert(NO, @"Error!");
      break;
  }
}

@end
