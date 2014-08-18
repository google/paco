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

#import "PacoOpenSourceLibViewController.h"
#import "UIColor+Paco.h"
#import "UIFont+Paco.h"
#import "PacoWebViewController.h"

@interface PacoOpenSourceLibViewController ()<UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, retain) UITableView* tableView;
@property (nonatomic, retain) NSArray* creditsArray;
@property (nonatomic, retain) NSArray* urlArray;

@end

@implementation PacoOpenSourceLibViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
      self.view.backgroundColor = [UIColor whiteColor];
      self.edgesForExtendedLayout = UIRectEdgeNone;
      self.navigationItem.title = NSLocalizedString(@"Open Source Libraries", nil);

      CGFloat yOffset = 10.;
      CGRect headerFrame = CGRectMake(10,
                                      yOffset,
                                      self.view.frame.size.width - 20,
                                      self.view.frame.size.height);
      UILabel* headerTitle = [[UILabel alloc] initWithFrame:headerFrame];
      [headerTitle setText:NSLocalizedString(@"OpenSource Library View Header title", nil)];
      [headerTitle setFont:[UIFont fontWithName:@"HelveticaNeue" size:12.0]];
      [headerTitle setNumberOfLines:0];
      [headerTitle sizeToFit];
      [self.view addSubview:headerTitle];

      CGFloat yOrigin = headerTitle.frame.origin.y + headerTitle.frame.size.height + yOffset;
      UIView* lineView = [[UIView alloc] initWithFrame:CGRectMake(0,
                                                                  yOrigin,
                                                                  self.view.frame.size.width,
                                                                  1)];
      lineView.backgroundColor = [UIColor lightGrayColor];
      lineView.alpha = 0.5;
      [self.view addSubview:lineView];

      CGRect frame = self.view.frame;
      CGFloat adjustedHeight = frame.size.height - (lineView.frame.origin.y + lineView.frame.size.height);
      frame = CGRectMake(0, lineView.frame.origin.y + 1, frame.size.width, adjustedHeight);

      _tableView = [[UITableView alloc] initWithFrame:frame style:UITableViewStylePlain];
      _tableView.delegate = self;
      _tableView.dataSource = self;
      [self.view addSubview:_tableView];
    }
    return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];
  [self loadCreditsTableView];
}


- (void)loadCreditsTableView {
  self.creditsArray = @[@"Google-Toolbox-For-Mac",
                        @"TouchEngine",
                        @"SSKeyChain",
                        @"JCNotificationBannerPresenter",
                        @"CocoaLumberjack",
                        @"ParseKit"];
  
  self.urlArray = @[@"https://code.google.com/p/google-toolbox-for-mac/",
                    @"https://code.google.com/p/touchengine/",
                    @"https://github.com/soffes/sskeychain",
                    @"https://github.com/jcoleman/JCNotificationBannerPresenter",
                    @"https://github.com/CocoaLumberjack/CocoaLumberjack",
                    @"https://code.google.com/p/parsekit/source/detail?r=1543"];
}


#pragma mark UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView*)tableView {
  return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return [self.creditsArray count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:@"creditsCell"];
  if (!cell) {
    cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault
                                  reuseIdentifier:@"creditsCell"];
    cell.textLabel.font = [UIFont pacoTableCellFont];
    cell.textLabel.textColor = [UIColor pacoSystemButtonBlue];
    cell.detailTextLabel.font = [UIFont pacoTableCellDetailFont];
    cell.detailTextLabel.textColor = [UIColor darkGrayColor];
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
  }
  cell.textLabel.text = [self.creditsArray objectAtIndex:indexPath.row];
  return cell;
}

#pragma mark UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
  return 55.0f;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
  NSString* title = [self.creditsArray objectAtIndex:indexPath.row];
  NSString* urlString = [self.urlArray objectAtIndex:indexPath.row];
  PacoWebViewController* webViewController = [PacoWebViewController controllerWithTitle:title];
  [webViewController loadURL:urlString];
  [self.navigationController pushViewController:webViewController animated:YES];
}



@end
