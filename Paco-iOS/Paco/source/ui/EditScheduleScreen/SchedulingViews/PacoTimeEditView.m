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
#import "UIFont+Paco.h"
#import "PacoDateUtility.h"
#import "PacoDatePickerView.h"

@interface PacoTimeEditView ()<PacoDatePickerDelegate>{
  NSNumber* defaultTime;
}

@property (nonatomic, retain) PacoDatePickerView *datePicker;
@property (nonatomic, retain) UILabel* timeLabel;
@property (nonatomic, retain) UILabel* titleLabel;
@property (nonatomic, retain) UIButton* editButton;

@end

@implementation PacoTimeEditView
@synthesize time = _time;
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
    [editButton.titleLabel setFont:[UIFont pacoTableCellFont]];
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
  self.titleLabel.text = [NSString stringWithFormat:@"%@: ", _title];
  [self.titleLabel sizeToFit];
}

- (NSNumber *)time {
  return _time;
}

- (void)setTime:(NSNumber*)time {
  if ([_time isEqualToNumber:time]) {
    return;
  }
  _time = time;
  if (!defaultTime) {
    defaultTime = _time;
  }
  self.timeLabel.text = [PacoDateUtility timeStringAMPMFromMilliseconds:[_time longLongValue]];
  [self.timeLabel sizeToFit];
}

- (void)onEditTime:(id)sender {
  if (!self.datePicker) {
    PacoDatePickerView* datePickerView = [[PacoDatePickerView alloc] initWithFrame:CGRectZero];
    NSString* localizedStr = NSLocalizedString(@"Set %@", nil);
    [datePickerView setTitle:[NSString stringWithFormat:localizedStr, self.title]];
    datePickerView.delegate = self;
    self.datePicker = datePickerView;
  }
  [self.datePicker setDateNumber:self.time];
  [[self pacoTableView] presentPacoDatePicker:self.datePicker forCell:self];
}

#pragma mark - PacoDatePickerViewDelegate
- (void)onDateChanged:(PacoDatePickerView *)datePickerView {
  self.time = datePickerView.dateNumber;
  [self.tableDelegate dataUpdated:self rowData:self.time reuseId:self.reuseId];
}

- (void)cancelDateEdit {
  self.time = defaultTime;
  [self.tableDelegate dataUpdated:self rowData:self.time reuseId:self.reuseId];
  if (self.completionBlock) {
    self.completionBlock();
  }
}

- (void)saveDateEdit {
  defaultTime = self.time;
  if (self.completionBlock) {
    self.completionBlock();
  }
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
