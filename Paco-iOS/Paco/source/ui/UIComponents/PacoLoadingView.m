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

#import "PacoLoadingView.h"

@interface PacoLoadingView ()
@property(nonatomic, weak) UIViewController* parentViewController;
@property(nonatomic, strong) UIActivityIndicatorView* spinner;
@end

@implementation PacoLoadingView

- (id)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    self.backgroundColor = [UIColor grayColor];
    self.alpha = .7;
    _spinner =
        [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    [self addSubview:_spinner];
  }
  return self;
}

+ (PacoLoadingView*)sharedInstance {
  static PacoLoadingView* sharedLoadingView = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    CGRect bounds = [[UIScreen mainScreen] bounds];
    sharedLoadingView = [[PacoLoadingView alloc] initWithFrame:bounds];
  });
  return sharedLoadingView;
}

- (void)showLoadingScreenForController:(UIViewController*)parentController {
  if (!parentController) {
    return;
  }
  if (self.parentViewController) {
    [self dismissLoadingScreen];
  }
  self.spinner.center = self.center;
  [parentController.view addSubview:self];
  [self.spinner startAnimating];
  self.parentViewController = parentController;
}

- (void)dismissLoadingScreen {
  if (!self.parentViewController) {
    return;
  }
  [self.spinner stopAnimating];
  [self removeFromSuperview];
  self.parentViewController = nil;
}


@end
