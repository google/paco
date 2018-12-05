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

#import "PacoPublicExperimentController.h"
#import "PacoPublicDefinitionLoader.h"
#import "PacoEnumerator.h"
#import "UIColor+Paco.h"
#import "PacoExperimentDetailsViewController.h"
#import "PacoClient.h"
#import "PacoService.h"
#import "UIFont+Paco.h"
#import "PacoAlertView.h"

@interface PacoPublicExperimentController () <UITableViewDelegate, UITableViewDataSource>
@property(nonatomic, strong) UITableView* tableView;
@property(nonatomic, strong) NSMutableArray* definitions;
@property(nonatomic, strong) id<PacoEnumerator> enumerator;
@property(nonatomic, assign) BOOL isLoading;

@end

@implementation PacoPublicExperimentController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    _definitions = [NSMutableArray arrayWithCapacity:100];
    _enumerator = [PacoPublicDefinitionLoader enumerator];

    self.edgesForExtendedLayout = UIRectEdgeNone;
    
    self.navigationItem.title = NSLocalizedString(@"Public Experiments", nil);
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Main",nil)
                                                                             style:UIBarButtonItemStylePlain
                                                                            target:self
                                                                            action:@selector(gotoMainPage)];

    CGRect frame = self.view.frame;
    CGFloat adjustedHeight = frame.size.height - 64; //minus navigation bar
    frame = CGRectMake(0, 0, frame.size.width, adjustedHeight);
    _tableView = [[UITableView alloc] initWithFrame:frame style:UITableViewStylePlain];
    _tableView.delegate = self;
    _tableView.dataSource = self;
    [self.view addSubview:_tableView];
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];
  [self loadNextPage];
}

- (void)gotoMainPage {
  [self.navigationController popToRootViewControllerAnimated:YES];
}


-(void)loadNextPage {
  if (self.isLoading) {
    return;
  }
  self.isLoading = YES;
  
  //deselect the load more cell.
  [self.tableView deselectRowAtIndexPath:[self.tableView indexPathForSelectedRow] animated:NO];
  [self updateLoadMoreCell:nil];
  
  [self.enumerator loadNextPage:^(NSArray* items, NSError* error) {
    self.isLoading = NO;
    dispatch_async(dispatch_get_main_queue(), ^{
      [self handleDataItems:items error:error];
      [self updateLoadMoreCell:nil];
    });

  }];
}


-(void)handleDataItems:(NSArray*)dataItems error:(NSError*)error {
  if (error) {
    [PacoAlertView showAlertWithError:error];
  }
  
  if(dataItems.count > 0) {
    int currentIndex = [self rowOfLastCell];
    [self.definitions addObjectsFromArray:dataItems];
    
    NSMutableArray* paths = [NSMutableArray array];
    for (int index = currentIndex; index < [self rowOfLastCell]; index++) {
      [paths addObject:[NSIndexPath indexPathForRow:index inSection:0]];
    }
    [self.tableView beginUpdates];
    [self.tableView insertRowsAtIndexPaths:paths withRowAnimation:UITableViewRowAnimationFade];
    [self.tableView endUpdates];
  }
}


- (int)rowOfLastCell {
  return (int)[self.definitions count];
}

-(void)updateLoadMoreCell:(UITableViewCell*)loadMoreCell{
  if (!loadMoreCell) {
    NSIndexPath* path = [NSIndexPath indexPathForRow:[self rowOfLastCell] inSection:0];
    loadMoreCell = [self.tableView cellForRowAtIndexPath:path];
  }
  
  if (self.isLoading) {
    loadMoreCell.userInteractionEnabled = NO;
    loadMoreCell.textLabel.textColor = [UIColor lightGrayColor];
    loadMoreCell.textLabel.text = @"Loading...";
  } else {
    if ([self.enumerator hasMoreItems]) {
      loadMoreCell.userInteractionEnabled = YES;
      loadMoreCell.textLabel.textColor = [UIColor pacoSystemButtonBlue];
      loadMoreCell.textLabel.text = @"Load next 20 experiments";
    } else {
      loadMoreCell.userInteractionEnabled = NO;
      loadMoreCell.textLabel.textColor = [UIColor lightGrayColor];
      loadMoreCell.textLabel.text = @"No more to load";
    }
  }
}

- (void)goToDefinitionDetailControllerWithDefinition:(PacoExperimentDefinition*)definition {
  PacoExperimentDetailsViewController* details =
      [PacoExperimentDetailsViewController controllerWithExperiment:definition];
  [self.navigationController pushViewController:details animated:YES];
}


#pragma mark UITableViewDataSource
-(NSInteger)numberOfSectionsInTableView:(UITableView*)tableView {
  return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return [self.definitions count] + 1;
}

-(UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath*)indexPath {
  //last cell: load more
  if (indexPath.row == [self rowOfLastCell]) {
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:@"LoadMoreCell"];
    if (!cell) {
      cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"LoadMoreCell"];
      cell.textLabel.textAlignment = NSTextAlignmentCenter;
      cell.textLabel.font = [UIFont pacoNormalButtonFont];
    }
    [self updateLoadMoreCell:cell];
    return cell;
  }
  
  UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:@"experimentCell"];
  if (!cell) {
    cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                  reuseIdentifier:@"experimentCell"];

    cell.textLabel.font = [UIFont pacoTableCellFont];
    cell.textLabel.textColor = [UIColor pacoSystemButtonBlue];
    cell.textLabel.numberOfLines = 2;
    cell.detailTextLabel.font = [UIFont pacoTableCellDetailFont];
    cell.detailTextLabel.textColor = [UIColor darkGrayColor];
  }
  NSDictionary* dict = (self.definitions)[indexPath.row];
  NSAssert([dict isKindOfClass:[NSDictionary class]], @"definition should be a dictionary");
  cell.textLabel.text = [NSString stringWithFormat:@"%ld. %@", (long)(indexPath.row + 1), dict[@"title"]];
  cell.detailTextLabel.text = dict[@"creator"];
  return cell;
}

#pragma mark UITableViewDelegate
-(CGFloat)tableView:(UITableView*)tableView heightForRowAtIndexPath:(NSIndexPath*)indexPath {
  return 60.0f;
}

-(void)tableView:(UITableView*)tableView didSelectRowAtIndexPath:(NSIndexPath*)indexPath {
  if (indexPath.row == [self rowOfLastCell]) {
    [self loadNextPage];
  } else {
    NSDictionary* dict = (self.definitions)[indexPath.row];
    NSAssert([dict isKindOfClass:[NSDictionary class]], @"definition should be a dictionary");
    NSString* definitionId = [NSString stringWithFormat:@"%lld",
                              [dict[@"id"] longLongValue]];
    
    [[PacoClient sharedInstance].service
        loadFullDefinitionWithID:definitionId
                        andBlock:^(PacoExperimentDefinition* definition, NSError* error) {
                          dispatch_async(dispatch_get_main_queue(), ^{
                            if (error) {
                              [PacoAlertView showAlertWithError:error];
                            } else {
                              [self goToDefinitionDetailControllerWithDefinition:definition];
                            }
                          });
                        }];
  }
}





@end
