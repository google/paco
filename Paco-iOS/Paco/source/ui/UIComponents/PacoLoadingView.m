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

- (BOOL)isVisible {
  return self.superview != nil;
}

- (void)showLoadingScreen {
  if ([self isVisible]) {
    return;
  }
  dispatch_async(dispatch_get_main_queue(), ^{
    self.spinner.center = self.center;
    [[[UIApplication sharedApplication] keyWindow] addSubview:self];
    [[[UIApplication sharedApplication] keyWindow] bringSubviewToFront:self];
    [self.spinner startAnimating];
  });
}

- (void)dismissLoadingScreen {
  if (![self isVisible]) {
    return;
  }
  dispatch_async(dispatch_get_main_queue(), ^{
    [self.spinner stopAnimating];
    [self removeFromSuperview];
  });
}


@end
