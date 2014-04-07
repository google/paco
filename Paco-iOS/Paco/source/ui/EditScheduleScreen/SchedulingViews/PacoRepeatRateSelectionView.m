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

#import "PacoRepeatRateSelectionView.h"

#import "UIColor+Paco.h"
#import "PacoLayout.h"

@interface PacoRepeatRateSelectionView ()
@end

@implementation PacoRepeatRateSelectionView

@synthesize repeatStyle = _repeatStyle;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.minValue = 1;
    self.maxValue = 30;
    self.value = @1;
    self.format = NSLocalizedString(@"Repeat every %d day(s)", nil);
  }
  return self;
}

- (void)setRepeatStyle:(PacoScheduleRepeatStyle)repeatStyle {
  _repeatStyle = repeatStyle;
  switch (_repeatStyle) {
    case kPacoScheduleRepeatDays:
      self.format = NSLocalizedString(@"Repeat every %d day(s)", nil);
      self.minValue = 1;
      self.maxValue = 30;
      self.value = @1;
      break;
    case kPacoScheduleRepeatWeeks:
      self.format = NSLocalizedString(@"Repeat every %d weeks.", nil);
      self.minValue = 1;
      self.maxValue = 52;
      self.value = @1;
      break;
    case kPacoScheduleRepeatMonths:
      self.format = NSLocalizedString(@"Repeat every %d month(s).", nil);
      self.minValue = 1;
      self.maxValue = 12;
      self.value = @1;
      break;
  }
}

- (void)layoutSubviews {
  [super layoutSubviews];
  self.backgroundColor = [UIColor pacoBackgroundWhite];
}

@end
