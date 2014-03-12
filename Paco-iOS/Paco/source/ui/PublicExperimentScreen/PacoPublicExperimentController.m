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
#import "PacoColor.h"

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
    CGFloat adjustedHeight = frame.size.height - 20. - 64; //minus status bar and navigation bar
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
    [[[UIAlertView alloc] initWithTitle:@"Oops"
                                message:@"Something went wrong, please try again"
                               delegate:nil
                      cancelButtonTitle:@"OK"
                      otherButtonTitles:nil] show];
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
  return [self.definitions count];
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
      loadMoreCell.textLabel.textColor = [PacoColor pacoSystemButtonBlue];
      loadMoreCell.textLabel.text = @"Load next 20 experiments";
    } else {
      loadMoreCell.userInteractionEnabled = NO;
      loadMoreCell.textLabel.textColor = [UIColor lightGrayColor];
      loadMoreCell.textLabel.text = @"No more to load";
    }
  }
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
    }
    [self updateLoadMoreCell:cell];
    return cell;
  }
  
  UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:@"experimentCell"];
  if (!cell) {
    cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleSubtitle
                                  reuseIdentifier:@"experimentCell"];
    cell.textLabel.textColor = [UIColor darkGrayColor];
  }
  NSDictionary* dict = [self.definitions objectAtIndex:indexPath.row];
  NSAssert([dict isKindOfClass:[NSDictionary class]], @"definition should be a dictionary");
  cell.textLabel.text = [dict objectForKey:@"title"];
  cell.detailTextLabel.text = [dict objectForKey:@"creator"];
  return cell;
}

#pragma mark UITableViewDelegate
-(void)tableView:(UITableView*)tableView didSelectRowAtIndexPath:(NSIndexPath*)indexPath {
  if (indexPath.row == [self rowOfLastCell]) {
    [self loadNextPage];
  }
}




@end
