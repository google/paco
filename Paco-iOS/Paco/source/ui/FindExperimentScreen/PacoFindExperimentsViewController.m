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

#import "PacoFindExperimentsViewController.h"

#import "PacoColor.h"
#import "PacoClient.h"
#import "PacoExperimentDetailsViewController.h"
#import "PacoFont.h"
#import "PacoModel.h"
#import "PacoExperimentDefinition.h"
#import "PacoAlertView.h"
#import "PacoQuestionScreenViewController.h"
#import "PacoLoadingTableCell.h"
#import "PacoTableView.h"
#import "PacoLoadingView.h"
#import "PacoSubtitleTableCell.h"

@interface PacoFindExperimentsViewController () <PacoTableViewDelegate>

@end

@implementation PacoFindExperimentsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.navigationItem.title = @"Find My Experiments";
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
  [table registerClass:[PacoSubtitleTableCell class] forStringKey:nil dataClass:[PacoExperimentDefinition class]];
  table.backgroundColor = [PacoColor pacoBackgroundWhite];
  self.view = table;
  BOOL finishLoading = [[PacoClient sharedInstance] prefetchedDefinitions];
  if (!finishLoading) {
    [table setLoadingSpinnerEnabledWithLoadingText:@"Finding Experiments ..."];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(definitionsUpdate:) name:PacoFinishLoadingDefinitionNotification object:nil];
  } else {
    NSError* prefetchError = [[PacoClient sharedInstance] errorOfPrefetchingDefinitions];
    [self updateUIWithError:prefetchError];
  }
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(refreshFinished:)
                                               name:PacoFinishRefreshing
                                             object:nil];
}


- (void)onClickRefresh {
  [[PacoLoadingView sharedInstance] showLoadingScreen];
  [[PacoClient sharedInstance] refreshDefinitions];
}


- (void)updateUIWithError:(NSError*)error
{
  //send UI update to main thread to avoid potential crash
  dispatch_async(dispatch_get_main_queue(), ^{
    PacoTableView* tableView = (PacoTableView*)self.view;
    if (error) {
      tableView.data = [NSArray array];
      [[[UIAlertView alloc] initWithTitle:@"Sorry"
                                  message:@"Something went wrong, please try again later."
                                 delegate:nil
                        cancelButtonTitle:@"OK"
                        otherButtonTitles:nil] show];
    }else{
      tableView.data = [PacoClient sharedInstance].model.experimentDefinitions;
    }
    
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Refresh"
                                                                              style:UIBarButtonItemStyleDone
                                                                             target:self
                                                                             action:@selector(onClickRefresh)];
  });
}

- (void)refreshFinished:(NSNotification*)notification {
  [self definitionsUpdate:notification];
  [[PacoLoadingView sharedInstance] dismissLoadingScreen];
}

- (void)definitionsUpdate:(NSNotification*)notification {
  NSError* error = (NSError*)notification.object;
  NSAssert([error isKindOfClass:[NSError class]] || error == nil, @"The notification should send an error!");
  [self updateUIWithError:error];
}

#pragma mark - PacoTableViewDelegate

- (void)initializeCell:(UITableViewCell *)cell
              withData:(id)rowData
            forReuseId:(NSString *)reuseId {
  if ([rowData isKindOfClass:[PacoExperimentDefinition class]]) {
    PacoExperimentDefinition *experiment = rowData;
    cell.backgroundColor = [PacoColor pacoBackgroundWhite];
    cell.imageView.image = [UIImage imageNamed:@"calculator.png"];
    cell.textLabel.font = [PacoFont pacoTableCellFont];
    cell.detailTextLabel.font = [PacoFont pacoTableCellDetailFont];
    cell.textLabel.text = experiment.title;
    cell.textLabel.textColor = [PacoColor pacoBlue];
    cell.detailTextLabel.text = experiment.creator;
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
  if ([rowData isKindOfClass:[PacoExperimentDefinition class]]) {
  
    PacoExperimentDefinition *experiment = rowData;
    if (!experiment) {
      // Must be loading...
      return;
    }
    PacoExperimentDetailsViewController *details =
        [PacoExperimentDetailsViewController controllerWithExperiment:experiment];
    [self.navigationController pushViewController:details animated:YES];
  }
}

- (void)dataUpdated:(UITableViewCell *)cell rowData:(id)rowData reuseId:(NSString *)reuseId {

}

- (void)reloadTable {
  NSLog(@" ");
}

/*
#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  int numExperiments = [[PacoClient sharedInstance].model.experimentDefinitions count];
  NSLog(@"EXPERIMENTS SCREEN HAS %d EXPERIMENTS", numExperiments);
  return numExperiments == 0 ? 1 : numExperiments;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  PacoExperimentDefinition *experiment = [[PacoClient sharedInstance].model.experimentDefinitions objectAtIndex:indexPath.row];
  if (!experiment) {
    PacoLoadingTableCell *loadingCell = [[PacoLoadingTableCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"experiment list loading cell"];
    loadingCell.loadingText = @"Loading Experiments...";
    return loadingCell;
  }
  UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"experiment list cell"];
  if (!cell) {
    cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"experiment list cell"];
    cell.backgroundColor = [PacoColor pacoLightBlue];
    cell.imageView.image = [UIImage imageNamed:@"calculator.png"];
    cell.textLabel.font = [PacoFont pacoTableCellFont];
    cell.detailTextLabel.font = [PacoFont pacoTableCellDetailFont];
  }
  cell.textLabel.text = experiment.title;
  cell.detailTextLabel.text = [experiment.admins objectAtIndex:0];
  return cell;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
  PacoExperimentDefinition *experiment = [[PacoClient sharedInstance].model.experimentDefinitions objectAtIndex:indexPath.row];
  if (!experiment) {
    // Must be loading...
    return;
  }
  PacoExperimentDetailsViewController *details = [[PacoExperimentDetailsViewController alloc] init];
  details.experiment = experiment;
  [self.navigationController pushViewController:details animated:YES];
  //PacoQuestionScreenViewController *questions = [[PacoQuestionScreenViewController alloc] init];
  //questions.experiment = experiment;
  //[self.navigationController pushViewController:questions animated:YES];
  
}
*/
@end
