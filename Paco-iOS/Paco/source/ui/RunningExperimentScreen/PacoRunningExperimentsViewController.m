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
#import "PacoColor.h"
#import "PacoFont.h"
#import "PacoLoadingTableCell.h"
#import "PacoModel.h"
#import "PacoService.h"
#import "PacoTableView.h"
#import "PacoQuestionScreenViewController.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperiment.h"
#import "PacoAlertView.h"
#import "PacoEvent.h"
#import "PacoEventManager.h"
#import "PacoSubtitleTableCell.h"
#import "PacoScheduler.h"
#import "UILocalNotification+Paco.h"
#import "PacoFindExperimentsViewController.h"

@interface PacoRunningExperimentsViewController () <UIAlertViewDelegate, PacoTableViewDelegate>

@property(nonatomic, strong) PacoExperiment* selectedExperiment;

@property(nonatomic, strong) UILabel* msgLabel;
@property(nonatomic, strong) UIButton* goToDefinitionButton;

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

- (void)dealloc
{
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)viewDidLoad {
  [super viewDidLoad];

  PacoTableView* table = [[PacoTableView alloc] init];
  table.delegate = self;
  [table registerClass:[PacoSubtitleTableCell class] forStringKey:nil dataClass:[PacoExperiment class]];
  table.backgroundColor = [PacoColor pacoBackgroundWhite];
  self.view = table;
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(experimentsUpdate:)
                                               name:PacoFinishRefreshing
                                             object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(appBecomeActive)
                                               name:PacoAppBecomeActive
                                             object:nil];
}


- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];
  
  BOOL finishLoading = [[PacoClient sharedInstance] prefetchedExperiments];
  if (!finishLoading) {
    [(PacoTableView*)self.view setLoadingSpinnerEnabledWithLoadingText:NSLocalizedString(@"Loading Current Experiments ...", nil)];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(experimentsUpdate:) name:PacoFinishLoadingExperimentNotification object:nil];
  } else {
    NSError* prefetchError = [[PacoClient sharedInstance] errorOfPrefetchingexperiments];
    if (prefetchError) {
      [self updateUIWithError:prefetchError];
    } else {
      [self updateUIWithExperiments];
    }
  }
}

- (void)gotoMainPage {
  [self.navigationController popToRootViewControllerAnimated:YES];
}


- (void)updateUIWithError:(NSError*)error {
  NSAssert(error, @"error should be valid");
  //send UI update to main thread to avoid potential crash
  dispatch_async(dispatch_get_main_queue(), ^{
    PacoTableView* tableView = (PacoTableView*)self.view;
    tableView.data = [NSArray array];
    [PacoAlertView showGeneralErrorAlert];
  });
}


- (void)updateLabelAndButton:(BOOL)visible {
  if (visible && !self.msgLabel && !self.goToDefinitionButton) {
    UILabel *msgLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 300, 100)];
    [msgLabel setText:NSLocalizedString(@"You haven't joined any experiment yet.", nil)];
    [msgLabel setFont:[UIFont fontWithName:@"HelveticaNeue" size:14]];
    [msgLabel setTextColor:[UIColor darkGrayColor]];
    msgLabel.textAlignment = NSTextAlignmentCenter;
    [msgLabel sizeToFit];
    msgLabel.center = CGPointMake(self.view.center.x, self.view.center.y - 50);
    [self.view addSubview:msgLabel];
    self.msgLabel = msgLabel;
    
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
  self.msgLabel.hidden = !visible;
  self.goToDefinitionButton.hidden = !visible;
}


- (void)updateUIWithExperiments {
  if (![[PacoClient sharedInstance].model areRunningExperimentsLoaded]) {
    DDLogError(@"Try to update view controller without running experiments loaded.");
    return;
  }
  
  //send UI update to main thread to avoid potential crash
  dispatch_async(dispatch_get_main_queue(), ^{
    PacoTableView* tableView = (PacoTableView*)self.view;
    BOOL visible = [[PacoClient sharedInstance].model hasRunningExperiments] ? NO : YES;
    [self updateLabelAndButton:visible];
    tableView.data = [PacoClient sharedInstance].model.experimentInstances;
  });
}

- (void)goToFindMyExperiments:(UIButton*)button {
  UINavigationController* navigationController = self.navigationController;
  [navigationController popToRootViewControllerAnimated:NO];
  PacoFindExperimentsViewController* controller = [[PacoFindExperimentsViewController alloc] init];
  [navigationController pushViewController:controller animated:NO];
}

- (void)experimentsUpdate:(NSNotification*)notification
{
  NSError* error = (NSError*)notification.object;
  NSAssert([error isKindOfClass:[NSError class]] || error == nil, @"The notification should send an error!");
  if (error) {
    [self updateUIWithError:error];
  } else {
    [self updateUIWithExperiments];
  }
}


- (void)appBecomeActive {
  BOOL finishLoading = [[PacoClient sharedInstance] prefetchedExperiments];
  NSError* prefetchError = [[PacoClient sharedInstance] errorOfPrefetchingexperiments];
  if (finishLoading && !prefetchError) {
    [self updateUIWithExperiments];
  }
}


- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
}


#pragma mark - PacoTableViewDelegate

- (void)initializeCell:(UITableViewCell *)cell
              withData:(id)rowData
            forReuseId:(NSString *)reuseId {
  if ([rowData isKindOfClass:[PacoExperiment class]]) {
    PacoExperiment *experiment = rowData;
    cell.backgroundColor = [PacoColor pacoBackgroundWhite];
    cell.imageView.image = [UIImage imageNamed:@"calculator.png"];
    cell.textLabel.font = [PacoFont pacoTableCellFont];
    cell.textLabel.textColor = [PacoColor pacoBlue];
    cell.textLabel.text = experiment.definition.title;
    if ([experiment isScheduledExperiment] &&
        [[PacoClient sharedInstance].scheduler hasActiveNotificationForExperiment:experiment.instanceId]) {
      cell.detailTextLabel.text = NSLocalizedString(@"Time to participate!", nil);
      cell.detailTextLabel.textColor = [UIColor colorWithRed:65./256. green:186./256. blue:34./256. alpha:.85];
      cell.detailTextLabel.font = [PacoFont pacoBoldFont];
    } else {
      cell.detailTextLabel.text = nil;
    }
  } else {
    assert([rowData isKindOfClass:[NSArray class]]);
    NSArray *keyAndValue = rowData;
    NSString *key = [keyAndValue objectAtIndex:0];
    assert([key isEqualToString:@"LOADING"]);
    PacoLoadingTableCell *loading = (PacoLoadingTableCell *)cell;
    NSString *loadingText = [keyAndValue objectAtIndex:1];
    loading.loadingText = loadingText;
  }
}

- (void)cellSelected:(UITableViewCell *)cell rowData:(id)rowData reuseId:(NSString *)reuseId {
  if ([rowData isKindOfClass:[PacoExperiment class]]) { //YMZ: is this necessary?
    self.selectedExperiment = rowData;
    //TODO: @"Edit Schedule",@"Explore Data"
    UIAlertView *alert =
    [[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"%@ %@", NSLocalizedString(@"Modify", nil), self.selectedExperiment.definition.title]
                               message:nil
                              delegate:self
                     cancelButtonTitle:NSLocalizedString(@"Cancel", nil)
                     otherButtonTitles:NSLocalizedString(@"Participate", nil), NSLocalizedString(@"Stop Experiment", nil), nil];
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

- (void)stopExperiment {
  //stop an experiment and update UI
  [[PacoClient sharedInstance] stopExperiment:self.selectedExperiment];

  dispatch_async(dispatch_get_main_queue(), ^{
    NSString* title = NSLocalizedString(@"Success", nil);
    NSString* message = NSLocalizedString(@"The experiment was stopped.", nil);
    [PacoAlertView showAlertWithTitle:title
                              message:message
                         dismissBlock:^(NSInteger buttonIndex) {
                           [self updateUIWithExperiments];
                         }
                    cancelButtonTitle:@"OK"
                    otherButtonTitles:nil];
  });
}

- (void)showStopConfirmAlert
{
  PacoAlertViewDidDismissBlock dismissBlock = ^(NSInteger buttonIndex){
    switch (buttonIndex) {
      case 0://cancel
        break;

      case 1://confirm
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

    case 2: // Stop
      [self showStopConfirmAlert];
      break;

    default:
      NSAssert(NO, @"Error!");
      break;
  }
}

@end
