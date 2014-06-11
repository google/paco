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

#import "PacoFindMyExperimentsViewController.h"

#import "UIColor+Paco.h"
#import "PacoClient.h"
#import "PacoExperimentDetailsViewController.h"
#import "UIFont+Paco.h"
#import "PacoModel.h"
#import "PacoExperimentDefinition.h"
#import "PacoAlertView.h"
#import "PacoQuestionScreenViewController.h"
#import "PacoLoadingTableCell.h"
#import "PacoTableView.h"
#import "PacoLoadingView.h"
#import "PacoSubtitleTableCell.h"

@interface PacoFindMyExperimentsViewController () <PacoTableViewDelegate>

@property (nonatomic, retain) UILabel* createExperimentLabel;

@end

@implementation PacoFindMyExperimentsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.navigationItem.title = NSLocalizedString(@"My Experiments", nil);
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
  [table registerClass:[PacoSubtitleTableCell class] forStringKey:nil dataClass:[PacoExperimentDefinition class]];
  table.backgroundColor = [UIColor pacoBackgroundWhite];
  self.view = table;
  BOOL finishLoading = [[PacoClient sharedInstance].model hasLoadedMyDefinitions];
  if (!finishLoading) {
    [table setLoadingSpinnerEnabledWithLoadingText:[NSString stringWithFormat:@"%@ ...", NSLocalizedString(@"Finding Experiments", nil)]];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(definitionsUpdate:)
                                                 name:kPacoNotificationLoadedMyDefinitions
                                               object:nil];
  } else {
    [self updateUI];
  }
}


- (void)gotoMainPage {
  [self.navigationController popToRootViewControllerAnimated:YES];
}


- (void)onClickRefresh {
  [[PacoLoadingView sharedInstance] showLoadingScreen];
  [[PacoClient sharedInstance] refreshMyDefinitionsWithBlock:^(NSError *error) {
    [self updateUI];
    if (error) {
      [self handleErrorWithRefreshFlag:YES];
    }
    [[PacoLoadingView sharedInstance] dismissLoadingScreen];
  }];
}


- (void)updateUI {
  dispatch_async(dispatch_get_main_queue(), ^{
    NSArray* myDefinitions = [PacoClient sharedInstance].model.myDefinitions;
    
    //update tableview's data
    ((PacoTableView*)self.view).data = myDefinitions;
    
    //update experiments not found label
    if ([myDefinitions count] > 0 && !self.createExperimentLabel) {
      self.createExperimentLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 300, 400)];
      [self.createExperimentLabel setText:NSLocalizedString(@"Paco CreateExperiments Message", nil)];
      [self.createExperimentLabel setFont:[UIFont fontWithName:@"HelveticaNeue" size:14]];
      [self.createExperimentLabel setTextColor:[UIColor darkGrayColor]];
      self.createExperimentLabel.textAlignment = NSTextAlignmentCenter;
      self.createExperimentLabel.numberOfLines = 0;
      [self.createExperimentLabel sizeToFit];
      self.createExperimentLabel.center = self.view.center;
      [self.view addSubview:self.createExperimentLabel];
    }
    [self.createExperimentLabel setHidden:([myDefinitions count] > 0)];

    //add a refresh button
    if (!self.navigationItem.rightBarButtonItem) {
      UIBarButtonItem* button = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Refresh", nil)
                                                                 style:UIBarButtonItemStyleDone
                                                                target:self
                                                                action:@selector(onClickRefresh)];
      self.navigationItem.rightBarButtonItem = button;
    }
  });
}

- (void)handleErrorWithRefreshFlag:(BOOL)isRefresh {
  dispatch_async(dispatch_get_main_queue(), ^{
    if (!isRefresh) {
      [PacoAlertView showGeneralErrorAlert];
    } else {
      [PacoAlertView showRefreshErrorAlert];
    }
  });
}


- (void)definitionsUpdate:(NSNotification*)notification {
  NSError* error = (NSError*)notification.object;
  NSAssert([error isKindOfClass:[NSError class]] || error == nil, @"The notification should send an error!");
  [self updateUI];
  if (error) {
    [self handleErrorWithRefreshFlag:NO];
  }
}

#pragma mark - PacoTableViewDelegate

- (void)initializeCell:(UITableViewCell *)cell
              withData:(id)rowData
            forReuseId:(NSString *)reuseId {
  if ([rowData isKindOfClass:[PacoExperimentDefinition class]]) {
    PacoExperimentDefinition *experiment = rowData;
    cell.backgroundColor = [UIColor pacoBackgroundWhite];
    cell.imageView.image = [UIImage imageNamed:@"calculator.png"];
    if (![experiment isCompatibleWithIOS]) {
      cell.imageView.image = [UIImage imageNamed:@"incompatible"];
    }
    cell.textLabel.font = [UIFont pacoTableCellFont];
    cell.textLabel.numberOfLines = 2;
    cell.detailTextLabel.font = [UIFont pacoTableCellDetailFont];
    cell.textLabel.text = experiment.title;
    cell.textLabel.textColor = [UIColor pacoBlue];
    cell.detailTextLabel.text = experiment.creator;
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

@end
