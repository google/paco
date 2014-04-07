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

#import "PacoByWeekOrMonthSelectionView.h"

#import "UIColor+Paco.h"

@implementation PacoByWeekOrMonthSelectionView


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.optionLabels = @[NSLocalizedString(@"By Week", nil),
                         NSLocalizedString(@"By Month", nil)];
    self.radioStyle = YES;
  }
  return self;
}

- (BOOL)byWeek {
  return (self.bitFlags.unsignedIntValue & 1);
}

- (void)setByWeek:(BOOL)isByWeek {
  self.bitFlags = [NSNumber numberWithUnsignedInt:(isByWeek ? 1 : 2)];
}

- (void)layoutSubviews {
  [super layoutSubviews];
  self.backgroundColor = [UIColor pacoBackgroundWhite];
}

@end
