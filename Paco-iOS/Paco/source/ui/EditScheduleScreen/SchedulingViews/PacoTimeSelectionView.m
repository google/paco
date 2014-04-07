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

@interface PacoTimeSelectionView ()
@property (nonatomic, retain) NSMutableArray *timePickers;
@property (nonatomic, retain) UIDatePicker *picker;
@property (nonatomic, retain) NSMutableArray *timeEditButtons;
@property (nonatomic, retain) UILabel *label;
@property (nonatomic, retain) UIButton *addButton;
@property (nonatomic, assign) NSInteger editIndex;

@end

@implementation PacoTimeSelectionView
@synthesize times = _times;

- (void)dealloc {
  [self.addButton removeTarget:self action:@selector(onAddTime) forControlEvents:UIControlEventTouchUpInside];
}

- (void)onAddTime {
  NSMutableArray *array = [NSMutableArray arrayWithArray:self.times];
  [array addObject:@0LL];
  self.times = array;
  [self.tableDelegate dataUpdated:self rowData:self.times reuseId:self.reuseId];
}

- (void)updateTime:(UIButton *)button {
  NSNumber *time = [NSNumber numberWithLongLong:(self.picker.date.timeIntervalSince1970 * 1000)];
  [button setTitle:[PacoDateUtility timeStringAMPMFromMilliseconds:[time longLongValue]]
          forState:UIControlStateNormal];
  [button setTitle:[PacoDateUtility timeStringAMPMFromMilliseconds:[time longLongValue]]
          forState:UIControlStateHighlighted];
  if (self.editIndex != NSNotFound) {
    [self performSelector:@selector(updateTime:) withObject:button afterDelay:0.5];
  }
}

- (void)onEdit:(UIButton *)button {
  NSUInteger timeIndex = [self.timeEditButtons indexOfObject:button];
  self.editIndex = timeIndex;
  assert(timeIndex != NSNotFound);
  NSNumber *time = (self.times)[timeIndex];
  [self.picker setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
  [self.picker setDate:[NSDate dateWithTimeIntervalSince1970:(time.longLongValue / 1000)]];

  [self performSelector:@selector(updateTime:) withObject:button afterDelay:0.5];

  UITableView *table = [self tableView];
  PacoTableView *pacoTable = [self pacoTableView];
  pacoTable.footer = self.picker;
  NSIndexPath *indexPath = [table indexPathForCell:self];
  [table scrollToRowAtIndexPath:indexPath atScrollPosition:UITableViewScrollPositionBottom animated:YES];
}

- (void)onDateChange {
  if (_editIndex != NSNotFound) {
    NSMutableArray *timesArray = [NSMutableArray arrayWithArray:self.times];
    timesArray[self.editIndex] = [NSNumber numberWithLongLong:(self.picker.date.timeIntervalSince1970 * 1000)];
    self.times = timesArray;
    [self.tableDelegate dataUpdated:self rowData:self.times reuseId:self.reuseId];
  }
}

- (void)finishTimeSelection {
  NSMutableArray *timesArray = [NSMutableArray arrayWithArray:self.times];
  if (_editIndex != NSNotFound) {
    timesArray[self.editIndex] = [NSNumber numberWithLongLong:(self.picker.date.timeIntervalSince1970 * 1000)];
    self.times = timesArray;
    PacoTableView *pacoTable = [self pacoTableView];
    pacoTable.footer = nil;
    [pacoTable setNeedsLayout];
    [self.tableDelegate dataUpdated:self rowData:self.times reuseId:self.reuseId];
  }
}

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.editIndex = NSNotFound;
    self.timePickers = [NSMutableArray array];
    self.timeEditButtons = [NSMutableArray array];
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectZero];
    label.text = NSLocalizedString(@"Signal Time(s)", nil);
    label.backgroundColor = [UIColor clearColor];
    self.label = label;
    [self addSubview:label];
    [self.label sizeToFit];

    UIButton *addButton = [[UIButton alloc] initWithFrame:CGRectZero];
    [addButton setTitle:@"+" forState:UIControlStateNormal];
    [addButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [addButton setTitle:@"+" forState:UIControlStateHighlighted];
    [addButton setTitleColor:[UIColor blackColor] forState:UIControlStateHighlighted];
    addButton.backgroundColor = [UIColor pacoBlue];
    self.addButton = addButton;
    //disable the ability to add more times for now, we may need it in the future.
    //and a minus button is needed if we enable the add button
    //[self addSubview:addButton];
    [self.addButton sizeToFit];
    [addButton addTarget:self action:@selector(onAddTime) forControlEvents:UIControlEventTouchUpInside];

    UIDatePicker *picker = [[UIDatePicker alloc] initWithFrame:CGRectZero];
    [picker addTarget:self action:@selector(onDateChange) forControlEvents:UIControlEventValueChanged];
    picker.datePickerMode = UIDatePickerModeTime;
    [self.superview.superview addSubview:picker];
    [picker sizeToFit];
    [picker removeFromSuperview];
    self.picker = picker;
  }
  return self;
}

- (void)rebuildTimes {
  [self.timeEditButtons removeAllObjects];
  [self.timePickers removeAllObjects];

  if ([self.times count] == 0) {
    self.times = @[[NSDate dateWithTimeIntervalSince1970:0]];
    UIButton *button = [[UIButton alloc] initWithFrame:CGRectZero];
    [button setTitle:NSLocalizedString(@"Enter a time", nil) forState:UIControlStateNormal];
    [self addSubview:button];
    self.timePickers = [NSMutableArray arrayWithObject:button];
    [self setNeedsLayout];
    return;
  }
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
    [_timeEditButtons addObject:button];
    [button sizeToFit];
    [timeViews addObject:button];
  }
  self.timePickers = timeViews;
  [self setNeedsLayout];
}

- (NSArray *)times {
  return _times;
}

- (void)setTimes:(NSArray *)times {
  @synchronized(self) {
    _times = times;
    [self rebuildTimes];
  }
}

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
  CGRect addButtonFrame = labelFrame;
  addButtonFrame.origin.x = addButtonFrame.size.width + addButtonFrame.origin.x + 10;
  addButtonFrame.size.width = 60;
  self.addButton.frame = addButtonFrame;

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
