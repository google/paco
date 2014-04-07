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

#import "PacoInfoView.h"
#import "UIColor+Paco.h"

@implementation PacoInfoView

- (id)initWithFrame:(CGRect)frame
{
  self = [super initWithFrame:frame];
  if (self) {
    self.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.4];
    [self initUI];
  }
  return self;
}

- (void)initUI {
  UIButton* closeButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
  closeButton.frame = CGRectMake(10, self.frame.size.height - 60, self.frame.size.width - 20, 50);
  [closeButton setTitle:NSLocalizedString(@"Close", nil) forState:UIControlStateNormal];
  closeButton.titleLabel.font = [UIFont fontWithName:@"Helvetica-Bold" size:18];
  [closeButton setTitleColor:[UIColor pacoBlue] forState:UIControlStateNormal];
  [closeButton setBackgroundColor:[UIColor whiteColor]];
  closeButton.layer.cornerRadius = 5;
  [closeButton addTarget:self action:@selector(closeInfoView:) forControlEvents:UIControlEventTouchUpInside];
  [self addSubview:closeButton];

  _aboutPacoBtn = [UIButton buttonWithType:UIButtonTypeRoundedRect];
  _aboutPacoBtn.frame = CGRectMake(10, closeButton.frame.origin.y - 55, self.frame.size.width - 20, 50);
  [_aboutPacoBtn setTitle:NSLocalizedString(@"About Paco", nil) forState:UIControlStateNormal];
  _aboutPacoBtn.titleLabel.font = [UIFont fontWithName:@"Helvetica" size:19];
  [_aboutPacoBtn setTitleColor:[UIColor pacoBlue] forState:UIControlStateNormal];
  [_aboutPacoBtn setBackgroundColor:[UIColor whiteColor]];
  _aboutPacoBtn.layer.cornerRadius = 5;
  [_aboutPacoBtn addTarget:self action:@selector(openAboutPaco:) forControlEvents:UIControlEventTouchUpInside];
  [self addSubview:self.aboutPacoBtn];

  NSDictionary *infoDictionary = [[NSBundle mainBundle]infoDictionary];
  NSString *version = infoDictionary[(NSString*)kCFBundleVersionKey];

  UIButton* versionInfoBtn = [UIButton buttonWithType:UIButtonTypeRoundedRect];
  versionInfoBtn.frame = CGRectMake(10, self.aboutPacoBtn.frame.origin.y - 50.5, self.frame.size.width - 20, 50);
  [versionInfoBtn setTitle:[NSString stringWithFormat:@"%@: %@", NSLocalizedString(@"Version", nil), version] forState:UIControlStateNormal];
  versionInfoBtn.titleLabel.font = [UIFont fontWithName:@"Helvetica" size:12];
  [versionInfoBtn setTitleColor:[UIColor grayColor] forState:UIControlStateNormal];
  [versionInfoBtn setBackgroundColor:[UIColor whiteColor]];
  versionInfoBtn.layer.cornerRadius = 5;
  [self addSubview:versionInfoBtn];
}

- (void)closeInfoView:(UIButton *)sender {
  for (UIView* v in self.subviews) {
    [v removeFromSuperview];
  }
  [self removeFromSuperview];
}

- (void)openAboutPaco:(UIButton *)sender {
  [self closeInfoView:sender];
}

@end
