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
#import "PacoTitleView.h"
#import "PacoQuestionScreenViewController.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperiment.h"

@interface PacoRunningExperimentsViewController () <UIAlertViewDelegate, PacoTableViewDelegate>

@property(nonatomic, strong) PacoExperiment* selectedExperiment;

@end

@implementation PacoRunningExperimentsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.navigationItem.titleView = [[PacoTitleView alloc] initText:@"Running Experiments"];
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
  [table registerClass:[UITableViewCell class] forStringKey:nil dataClass:[PacoExperiment class]];
  table.backgroundColor = [PacoColor pacoLightBlue];
  self.view = table;
  BOOL finishLoading = [[PacoClient sharedInstance] prefetchedExperiments];
  if (!finishLoading) {
    [table setLoadingSpinnerEnabledWithLoadingText:@"Loading Current Experiments ..."];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(experimentsUpdate:) name:PacoExperimentInstancesUpdateNotification object:nil];
  } else {
    table.data = [PacoClient sharedInstance].model.experimentInstances;
  }
}

- (void)experimentsUpdate:(NSNotification*)notification
{
  NSError* error = (NSError*)notification.object;
  NSAssert([error isKindOfClass:[NSError class]] || error == nil, @"The notification should send an error!");
  
  PacoTableView* tableView = (PacoTableView*)self.view;
  if (error) {
    //TODO: ymz
    tableView.data = [NSArray array];
  }else{
    tableView.data = [PacoClient sharedInstance].model.experimentInstances;
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
    cell.backgroundColor = [PacoColor pacoLightBlue];
    cell.imageView.image = [UIImage imageNamed:@"calculator.png"];
    cell.textLabel.font = [PacoFont pacoTableCellFont];
    cell.detailTextLabel.font = [PacoFont pacoTableCellDetailFont];
    cell.textLabel.text = experiment.definition.title;
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
    UIAlertView *alert =
      [[UIAlertView alloc] initWithTitle:[NSString stringWithFormat:@"Modify %@", self.selectedExperiment.definition.title]
                                 message:nil
                                delegate:self
                       cancelButtonTitle:@"Cancel"
                       otherButtonTitles:@"Participate", @"Edit Schedule", @"Stop Experiment", @"Explore Data", nil];
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
  
  PacoQuestionScreenViewController *questions = [[PacoQuestionScreenViewController alloc] init];
  questions.experiment = self.selectedExperiment;
  [self.navigationController pushViewController:questions animated:YES];
}


#pragma mark - UIAlertViewDelegate

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
  NSLog(@"BUTTON INDEX + %d", buttonIndex);
  switch (buttonIndex) {
    case 0: // Cancel
      break;
    case 1: // Participate
      [self showParticipateController];
      break;
      
    case 2: // Edit
      break;
    case 3: // Stop
      break;
    case 4: // Explore
      break;
    default:
      NSAssert(NO, @"Error!");
      break;
  }
}

@end
