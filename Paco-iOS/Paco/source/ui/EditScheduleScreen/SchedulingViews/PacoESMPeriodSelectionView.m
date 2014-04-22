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

#import "PacoESMPeriodSelectionView.h"

#import "UIColor+Paco.h"

@implementation PacoESMPeriodSelectionView

- (id)init {
  assert(0);
  return nil;
}

- (id)initWithFrame:(CGRect)frame {
  assert(0);
  return nil;
}

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.radioStyle = YES;
    self.optionLabels = @[NSLocalizedString(@"Day", nil), NSLocalizedString(@"Week", nil), NSLocalizedString(@"Month", nil)];
  }
  return self;
}

- (void)layoutSubviews {
  [super layoutSubviews];
  self.backgroundColor = [UIColor pacoBackgroundWhite];
}

@end
