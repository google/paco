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

#import "PacoFirstDayOfMonthSelectionView.h"

#import "UIColor+Paco.h"

@implementation PacoFirstDayOfMonthSelectionView

@synthesize firstDayOfMonth = _firstDayOfMonth;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.optionLabels = @[@"1st", @"2nd", @"3rd", @"4th", @"5th"];
  }
  return self;
}

- (void)setFirstDayOfMonth:(NSNumber *)firstDay {
  _firstDayOfMonth = firstDay;
  self.bitFlags = [NSNumber numberWithUnsignedInt:(1<<(firstDay.intValue - 1))];
}

- (NSNumber *)firstDayOfMonth {
  unsigned int flags = [self.bitFlags unsignedIntValue];
  for (int i = 0; i < self.optionLabels.count; ++i) {
    if (flags & (1<<i)) {
      return @(i + 1);
    }
  }
  return nil;
}

- (void)layoutSubviews {
  [super layoutSubviews];
  self.backgroundColor = [UIColor pacoBackgroundWhite];
}

@end
