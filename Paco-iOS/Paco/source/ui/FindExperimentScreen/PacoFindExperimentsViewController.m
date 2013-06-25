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
#import "PacoTitleView.h"
#import "PacoExperimentDefinition.h"




//TODO temp
#import "PacoQuestionScreenViewController.h"
#import "PacoLoadingTableCell.h"
#import "PacoTableView.h"

@interface PacoFindExperimentsViewController () <PacoTableViewDelegate>

@end

@implementation PacoFindExperimentsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.navigationItem.titleView = [[PacoTitleView alloc] initText:@"Find Experiments"];
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
  [table registerClass:[UITableViewCell class] forStringKey:nil dataClass:[PacoExperimentDefinition class]];
  table.backgroundColor = [PacoColor pacoLightBlue];
  self.view = table;
  int numExperiments = [[PacoClient sharedInstance].model.experimentDefinitions count];
  if (numExperiments == 0) {
    [table setLoadingSpinnerEnabledWithLoadingText:@"Finding Experiments ..."];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(definitionsUpdate:) name:PacoExperimentDefinitionUpdateNotification object:nil];
  } else {
    table.data = [PacoClient sharedInstance].model.experimentDefinitions;
  }
}


- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
  // Dispose of any resources that can be recreated.
}

- (void)definitionsUpdate:(NSNotification*)notification
{
  NSArray* definitions = (NSArray*)notification.object;
  NSAssert([definitions isKindOfClass:[NSArray class]], @"definitions should be an array!");
  PacoTableView* tableView = (PacoTableView*)self.view;
  tableView.data = definitions;
}

#pragma mark - PacoTableViewDelegate

- (void)initializeCell:(UITableViewCell *)cell
              withData:(id)rowData
            forReuseId:(NSString *)reuseId {
  if ([rowData isKindOfClass:[PacoExperimentDefinition class]]) {
    PacoExperimentDefinition *experiment = rowData;
    cell.backgroundColor = [PacoColor pacoLightBlue];
    cell.imageView.image = [UIImage imageNamed:@"calculator.png"];
    cell.textLabel.font = [PacoFont pacoTableCellFont];
    cell.detailTextLabel.font = [PacoFont pacoTableCellDetailFont];
    cell.textLabel.text = experiment.title;
    cell.detailTextLabel.text = [experiment.admins objectAtIndex:0];
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
    PacoExperimentDetailsViewController *details = [[PacoExperimentDetailsViewController alloc] init];
    details.experiment = experiment;
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
