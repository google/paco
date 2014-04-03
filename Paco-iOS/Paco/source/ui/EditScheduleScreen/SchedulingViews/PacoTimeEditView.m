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

#import "PacoTimeEditView.h"
#import "PacoTableView.h"
#import "PacoScheduleEditView.h"
#import "PacoLayout.h"
#import "PacoFont.h"
#import "PacoDateUtility.h"

@interface PacoTimeEditView ()

@property (nonatomic, retain) UIDatePicker* picker;
@property (nonatomic, retain) UILabel* timeLabel;
@property (nonatomic, retain) UILabel* titleLabel;
@property (nonatomic, retain) UIButton* editButton;

@end

@implementation PacoTimeEditView

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    UILabel* titleLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    titleLabel.backgroundColor = [UIColor clearColor];
    [self addSubview:titleLabel];
    self.titleLabel = titleLabel;
    [self.titleLabel sizeToFit];

    UILabel* timeLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    timeLabel.backgroundColor = [UIColor clearColor];
    [self addSubview:timeLabel];
    self.timeLabel = timeLabel;
    [self.timeLabel sizeToFit];

    UIButton* editButton = [UIButton buttonWithType:UIButtonTypeSystem];
    [editButton setTitle:NSLocalizedString(@"Edit", nil) forState:UIControlStateNormal];
    [editButton.titleLabel setFont:[PacoFont pacoTableCellFont]];
    [editButton addTarget:self action:@selector(onEditTime:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:editButton];
    self.editButton = editButton;
    [self.editButton sizeToFit];
  }
  return self;
}

- (void)setTitle:(NSString *)title {
  if ([_title isEqualToString:title]) {
    return;
  }
  _title = [title copy];
  self.titleLabel.text = _title;
  [self.titleLabel sizeToFit];

}

- (void)setTime:(NSNumber*)time {
  if ([_time isEqualToNumber:time]) {
    return;
  }
  _time = time;
  self.timeLabel.text = [PacoDateUtility timeStringAMPMFromMilliseconds:[_time longLongValue]];
  [self.timeLabel sizeToFit];
}

- (void)onEditTime:(id)sender {
  //lazy initialization
  if (!self.picker) {
    UIDatePicker *picker = [[UIDatePicker alloc] initWithFrame:CGRectZero];
    [picker addTarget:self action:@selector(onTimeChange) forControlEvents:UIControlEventValueChanged];
    picker.datePickerMode = UIDatePickerModeTime;
    [picker setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
    self.picker = picker;
  }
  //update picker's date before showing it
  self.picker.date = [NSDate dateWithTimeIntervalSince1970:[self.time longLongValue] / 1000];
  [[self pacoTableView] presentDatePicker:self.picker forCell:self];
}

- (void)onTimeChange {
  self.time = [NSNumber numberWithLongLong:self.picker.date.timeIntervalSince1970 * 1000];
  [self.tableDelegate dataUpdated:self rowData:self.time reuseId:self.reuseId];
}

static CGFloat CELL_HEIGHT = 51;
- (CGSize)sizeThatFits:(CGSize)size {
  return CGSizeMake(300, CELL_HEIGHT);
}

+ (NSNumber *)heightForData:(id)data {
  return [NSNumber numberWithInt:CELL_HEIGHT];
}

- (void)layoutSubviews {
  [super layoutSubviews];

  NSArray* frames = [PacoLayout splitRectHorizontally:self.bounds numSections:3];
  self.titleLabel.frame = [PacoLayout rightAlignRect:self.titleLabel.frame.size
                                              inRect:[frames[0] CGRectValue]];
  self.timeLabel.frame = [PacoLayout centerRect:self.timeLabel.frame.size
                                         inRect:[frames[1] CGRectValue]];
  self.editButton.frame = [PacoLayout leftAlignRect:self.editButton.frame.size
                                             inRect:[frames[2] CGRectValue]];
}


@end
