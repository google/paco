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

#import "PacoTimeSelectionView.h"

#import "UIColor+Paco.h"
#import "PacoLayout.h"
#import "PacoDateUtility.h"
#import "PacoTableView.h"
#import "PacoDateUtility.h"
#import "PacoScheduleEditView.h"
#import "PacoDatePickerView.h"

@interface PacoTimeSelectionView ()<PacoDatePickerDelegate> {
  NSArray* _initialTimes;
}

@property (nonatomic, retain) NSMutableArray *timePickers;
@property (nonatomic, retain) PacoDatePickerView *datePicker;
@property (nonatomic, retain) NSMutableArray *timeEditButtons;
@property (nonatomic, retain) UILabel *label;
@property (nonatomic, assign) NSInteger editIndex;

@end



@implementation PacoTimeSelectionView
@synthesize times = _times;
- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    _editIndex = NSNotFound;
    _timePickers = [NSMutableArray array];
    _timeEditButtons = [NSMutableArray array];
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectZero];
    label.text = NSLocalizedString(@"Signal Time(s)", nil);
    label.backgroundColor = [UIColor clearColor];
    _label = label;
    [self addSubview:_label];
    [_label sizeToFit];
  }
  return self;
}

- (NSArray *)times {
  return _times;
}

- (void)setTimes:(NSArray *)times {
  @synchronized(self) {
    _times = times;
    if (!_initialTimes) {
      _initialTimes = _times;
    }
    [self rebuildTimes];
  }
}

- (void)rebuildTimes {
  [self.timeEditButtons removeAllObjects];
  [self.timePickers removeAllObjects];

  NSMutableArray *timeViews = [NSMutableArray array];
  for (NSNumber *time in self.times) {
    UIButton *button = [[UIButton alloc] initWithFrame:CGRectZero];
    button.backgroundColor = [UIColor pacoBlue];
    [button setTitle:[PacoDateUtility timeStringAMPMFromMilliseconds:[time longLongValue]]
            forState:UIControlStateNormal];
    [button setTitle:[PacoDateUtility timeString24hrFromMilliseconds:[time longLongValue]]
            forState:UIControlStateHighlighted];
    [button setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [button setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];

    [self addSubview:button];
    [button sizeToFit];
    CGRect frame = button.frame;
    frame.size.width = 140;
    button.frame = frame;
    [timeViews addObject:button];

    button = [[UIButton alloc] initWithFrame:CGRectZero];
    button.backgroundColor = [UIColor pacoBlue];
    [button setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [button setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
    [button setTitle:NSLocalizedString(@"Edit", nil) forState:UIControlStateNormal];
    [button setTitle:NSLocalizedString(@"Edit", nil) forState:UIControlStateHighlighted];
    [button addTarget:self action:@selector(onEdit:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:button];
    [self.timeEditButtons addObject:button];
    [button sizeToFit];
    [timeViews addObject:button];
  }
  self.timePickers = timeViews;
  [self setNeedsLayout];
}


- (void)onEdit:(UIButton *)button {
  NSUInteger timeIndex = [self.timeEditButtons indexOfObject:button];
  self.editIndex = timeIndex;
  assert(timeIndex != NSNotFound);
  if (!self.datePicker) {
    PacoDatePickerView* datePickerView = [[PacoDatePickerView alloc] initWithFrame:CGRectZero];
    datePickerView.delegate = self;
    datePickerView.title = NSLocalizedString(@"Set Time", nil);
    self.datePicker = datePickerView;
  }
  [self.datePicker setDateNumber:(self.times)[timeIndex]];
  [[self pacoTableView] presentPacoDatePicker:self.datePicker forCell:self];
}


#pragma mark - PacoDatePickerViewDelegate
- (void)onDateChanged:(PacoDatePickerView *)datePickerView {
  if (self.editIndex != NSNotFound) {
    NSMutableArray* timesArray = [NSMutableArray arrayWithArray:self.times];
    timesArray[self.editIndex] = [self.datePicker dateNumber];
    self.times = timesArray;
    [self.tableDelegate dataUpdated:self rowData:self.times reuseId:self.reuseId];
  }
}

- (void)cancelDateEdit {
  self.times = _initialTimes;
  [self.tableDelegate dataUpdated:self rowData:self.times reuseId:self.reuseId];
  if (self.completionBlock) {
    self.completionBlock();
  }
}

- (void)saveDateEdit {
  _initialTimes = self.times;
  if (self.completionBlock) {
    self.completionBlock();
  }
}


#pragma mark - ui layout
- (CGSize)sizeThatFits:(CGSize)size {
  return CGSizeMake(300, 340);
}

+ (NSNumber *)heightForData:(id)data {
  return @340;
}

- (void)layoutSubviews {
  [super layoutSubviews];

  self.backgroundColor = [UIColor pacoBackgroundWhite];

  CGRect labelFrame = [PacoLayout centerRect:self.label.frame.size inRect:CGRectMake(0, 10, self.frame.size.width, self.label.frame.size.height)];
  self.label.frame = labelFrame;

  if ([self.timePickers count]) {
    int yStart = self.label.frame.size.height + 10 + 10;
    int xStart =  0;
    int i = 0;
    for (UIButton *button in self.timePickers) {
      if ((i % 2) == 0) {
        xStart = (self.frame.size.width - button.frame.size.width) / 2;
        button.frame = CGRectMake(xStart, yStart, button.frame.size.width, button.frame.size.height);
      } else {
        xStart += 140 + 10;
        button.frame = CGRectMake(xStart, yStart, button.frame.size.width, button.frame.size.height);
        yStart += button.frame.size.height + 10;
      }
      ++i;
    }
  }
}

@end
