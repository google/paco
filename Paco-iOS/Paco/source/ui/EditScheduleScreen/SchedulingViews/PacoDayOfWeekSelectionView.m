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

#import "PacoDayOfWeekSelectionView.h"

#import "UIColor+Paco.h"
#import "PacoLayout.h"

@interface PacoDayOfWeekSelectionView ()
@end

@implementation PacoDayOfWeekSelectionView


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.backgroundColor = [UIColor pacoBackgroundWhite];
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.optionLabels = @[NSLocalizedString(@"S", nil), NSLocalizedString(@"M", nil), NSLocalizedString(@"T", nil), NSLocalizedString(@"W", nil), NSLocalizedString(@"T", nil), NSLocalizedString(@"F", nil), NSLocalizedString(@"S", nil)];
  }
  return self;
}

- (void)setDaysOfWeek:(NSNumber *)daysOfWeek {
  self.bitFlags = daysOfWeek;
}

- (NSNumber *)daysOfWeek {
  return self.bitFlags;
}

- (void)setOnlyAllowOneDay:(BOOL)onlyAllowOne {
  self.radioStyle = onlyAllowOne;
}

- (BOOL)onlyAllowOneDay {
  return self.radioStyle;
}

- (void)layoutSubviews {
  [super layoutSubviews];
  self.backgroundColor = [UIColor pacoBackgroundWhite];
}

@end
