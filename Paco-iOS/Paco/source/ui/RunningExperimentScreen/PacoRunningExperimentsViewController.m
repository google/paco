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

@interface PacoRunningExperimentsViewController () <UIAlertViewDelegate, PacoTableViewDelegate>

@property(nonatomic, strong) PacoExperiment* selectedExperiment;

@end

@implementation PacoRunningExperimentsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.navigationItem.title = @"Running Experiments";
    self.navigationItem.hidesBackButton = NO;

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
  BOOL finishLoading = [[PacoClient sharedInstance] prefetchedExperiments];
  if (!finishLoading) {
    [table setLoadingSpinnerEnabledWithLoadingText:@"Loading Current Experiments ..."];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(experimentsUpdate:) name:PacoFinishLoadingExperimentNotification object:nil];
  } else {
    NSError* prefetchError = [[PacoClient sharedInstance] errorOfPrefetchingexperiments];
    [self updateUIWithError:prefetchError];
  }
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(experimentsUpdate:)
                                               name:PacoFinishRefreshing
                                             object:nil];
}


- (void)updateUIWithError:(NSError*)error
{
  //send UI update to main thread to avoid potential crash
  dispatch_async(dispatch_get_main_queue(), ^{
    PacoTableView* tableView = (PacoTableView*)self.view;
    if (error) {
      tableView.data = [NSArray array];
      [PacoAlertView showGeneralErrorAlert];
    }else{
      if ([[PacoClient sharedInstance].model.experimentInstances count] == 0) {
        UILabel *msgLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 300, 100)];
        [msgLabel setText:@"You haven't joined any experiment yet."];
        [msgLabel setFont:[UIFont systemFontOfSize:15.0]];
        msgLabel.textAlignment = NSTextAlignmentCenter;
        [msgLabel sizeToFit];
        msgLabel.center = CGPointMake(self.view.center.x, self.view.center.y - 35);
        [self.view addSubview:msgLabel];

        UILabel *subLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 300, 100)];
        subLabel.numberOfLines = 2;
        [subLabel setText:@"Go to Find My Experiments \nto select an experiment to join"];
        [subLabel setFont:[UIFont systemFontOfSize:12.0]];
        [subLabel setTextColor:[UIColor darkGrayColor]];
        subLabel.textAlignment = NSTextAlignmentCenter;
        [subLabel sizeToFit];
        subLabel.center = CGPointMake(self.view.center.x, self.view.center.y);
        [self.view addSubview:subLabel];
      }
      tableView.data = [PacoClient sharedInstance].model.experimentInstances;
    }
  });
}


- (void)experimentsUpdate:(NSNotification*)notification
{
  NSError* error = (NSError*)notification.object;
  NSAssert([error isKindOfClass:[NSError class]] || error == nil, @"The notification should send an error!");
  [self updateUIWithError:error];  
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
    cell.detailTextLabel.font = [PacoFont pacoTableCellDetailFont];
    cell.textLabel.textColor = [PacoColor pacoBlue];
    cell.textLabel.text = experiment.definition.title;
    if ([experiment isSelfReportExperiment]) {
      cell.detailTextLabel.text = @"Self-Report";
      cell.detailTextLabel.textColor = [UIColor brownColor];
    } else {
      UILocalNotification *notification =
      [[PacoClient sharedInstance].scheduler activeNotificationForExperiment:experiment.instanceId];
      if (!notification) {
        cell.detailTextLabel.text = @"Inactive";
        cell.detailTextLabel.textColor = [UIColor grayColor];
      } else {
        NSDate *now = [NSDate date];
        NSInteger minutes = floor([now timeIntervalSinceDate:[notification pacoFireDate]] / 60);
        NSString *minuteString = minutes > 1 ? @"minutes" : @"minute";
        cell.detailTextLabel.text = [NSString stringWithFormat:@"Active: Last notified %d %@ ago", minutes, minuteString];
        cell.detailTextLabel.textColor = [UIColor redColor];
      }
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
      [[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"Modify %@", self.selectedExperiment.definition.title]
                                 message:nil
                                delegate:self
                       cancelButtonTitle:@"Cancel"
                       otherButtonTitles:@"Participate", @"Stop Experiment", nil];
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

  PacoTableView* tableView = (PacoTableView*)self.view;
  tableView.data = [PacoClient sharedInstance].model.experimentInstances;

  NSString* title = @"Success";
  NSString* message = @"The experiment was stopped.";
  [PacoAlertView showAlertWithTitle:title
                            message:message
                  cancelButtonTitle:@"OK"];
  if ([[PacoClient sharedInstance].model.experimentInstances count] == 0) {
    [self updateUIWithError:nil];
  }
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
  
  [PacoAlertView showAlertWithTitle:@"Are you sure?"
                            message:@"All your data will be deleted permanently with this experiment."
                       dismissBlock:dismissBlock
                  cancelButtonTitle:@"Cancel"
                  otherButtonTitles:@"Absolutely Sure!", nil];
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
