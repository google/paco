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
@property (nonatomic, retain) PacoWebViewController* webViewController;

@end

@implementation PacoOpenSourceLibViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
      self.view.backgroundColor = [UIColor whiteColor];
      self.edgesForExtendedLayout = UIRectEdgeNone;
      self.navigationItem.title = NSLocalizedString(@"Open Source Libraries", nil);
      self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Back", nil)
                                                                               style:UIBarButtonItemStylePlain
                                                                              target:self
                                                                              action:@selector(gotoMainPage)];

      CGRect headerFrame = CGRectMake(10, 10, self.view.frame.size.width - 20, self.view.frame.size.height);
      UILabel* headerTitle = [[UILabel alloc] initWithFrame:headerFrame];
      [headerTitle setText:NSLocalizedString(@"OpenSource Library View Header title", nil)];
      [headerTitle setFont:[UIFont fontWithName:@"HelveticaNeue" size:12.0]];
      [headerTitle setNumberOfLines:0];
      [headerTitle sizeToFit];
      [self.view addSubview:headerTitle];

      UIView* lineView = [[UIView alloc] initWithFrame:CGRectMake(0,
                                                                  headerTitle.frame.size.height + 20,
                                                                  self.view.frame.size.width,
                                                                  1)];
      lineView.backgroundColor = [UIColor lightGrayColor];
      lineView.alpha = 0.5;
      [self.view addSubview:lineView];

      CGRect frame = self.view.frame;
      CGFloat adjustedHeight = frame.size.height - 64;
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

- (void)gotoMainPage {
  [self.navigationController popToRootViewControllerAnimated:YES];
}

- (void)loadCreditsTableView {
  
  NSArray* array = @[@"Google-Toolbox-For-Mac",
                     @"TouchEngine",
                     @"SSKeyChain",
                     @"JCNotificationBannerPresenter",
                     @"CocoaLumberjack",
                     @"ParseKit"];

  NSArray* linkURL = @[@"https://code.google.com/p/google-toolbox-for-mac/",
                       @"https://code.google.com/p/touchengine/",
                       @"https://github.com/soffes/sskeychain",
                       @"https://github.com/jcoleman/JCNotificationBannerPresenter",
                       @"https://github.com/CocoaLumberjack/CocoaLumberjack",
                       @"https://code.google.com/p/parsekit/source/detail?r=1543"];

  _creditsArray = [[NSArray alloc] initWithArray:array];
  _urlArray = [[NSArray alloc] initWithArray:linkURL];
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
  cell.textLabel.text = [NSString stringWithFormat:@"%@", [_creditsArray objectAtIndex:indexPath.row]];
  return cell;
}

#pragma mark UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
  return 55.0f;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
  NSString* title = [self.creditsArray objectAtIndex:indexPath.row];
  NSString* urlString = [self.urlArray objectAtIndex:indexPath.row];
  self.webViewController = [[PacoWebViewController alloc] initWithNibName:nil bundle:nil];
  [self.webViewController setTitle:title];
  UIBarButtonItem* backBarButton = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Back", nil)
                                                                    style:UIBarButtonItemStylePlain
                                                                    target:self
                                                                    action:@selector(removeWebView)];
  self.webViewController.navigationItem.backBarButtonItem = backBarButton;
  [self.webViewController loadWebView:[NSURL URLWithString:urlString]];
  [self.navigationController pushViewController:self.webViewController animated:YES];
}

- (void)removeWebView {
  [self.webViewController.navigationController popViewControllerAnimated:YES];
}

- (void)tableView:(UITableView *)tableView willDisplayHeaderView:(UIView *)view forSection:(NSInteger)section {

}

@end
