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

#import "UIColor+Paco.h"
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

@property (nonatomic, retain) UILabel* createExperimentLabel;

@end

@implementation PacoFindExperimentsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.navigationItem.title = NSLocalizedString(@"Find My Experiments", nil);
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
  BOOL finishLoading = [[PacoClient sharedInstance] prefetchedDefinitions];
  if (!finishLoading) {
    [table setLoadingSpinnerEnabledWithLoadingText:[NSString stringWithFormat:@"%@ ...", NSLocalizedString(@"Finding Experiments", nil)]];
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


- (void)gotoMainPage {
  [self.navigationController popToRootViewControllerAnimated:YES];
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
      tableView.data = @[];
      [[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Sorry", nil)
                                  message:NSLocalizedString(@"Something went wrong, please try again later.", nil)
                                 delegate:nil
                        cancelButtonTitle:@"OK"
                        otherButtonTitles:nil] show];
    }else{
      tableView.data = [PacoClient sharedInstance].model.experimentDefinitions;
      if ([tableView.data count] > 0) {
        [self.createExperimentLabel setHidden:YES];
      } else {
        //lazy initialization
        if (!self.createExperimentLabel) {
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
        [self.createExperimentLabel setHidden:NO];
      }
    }

    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Refresh", nil)
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
    cell.backgroundColor = [UIColor pacoBackgroundWhite];
    cell.imageView.image = [UIImage imageNamed:@"calculator.png"];
    if (![experiment isCompatibleWithIOS]) {
      cell.imageView.image = [UIImage imageNamed:@"incompatible"];
    }
    cell.textLabel.font = [PacoFont pacoTableCellFont];
    cell.detailTextLabel.font = [PacoFont pacoTableCellDetailFont];
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
