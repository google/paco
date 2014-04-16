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

#import "PacoDatePickerView.h"
#import "PacoTableView.h"
#import "PacoTableViewDelegate.h"

@interface PacoDatePickerView ()

@property (nonatomic, retain) UIDatePicker* picker;
@property (nonatomic, retain) UILabel* setTimeLabel;

@end

@implementation PacoDatePickerView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
      UIToolbar* pickerToolbar = [[UIToolbar alloc]initWithFrame:CGRectZero];
      pickerToolbar.barStyle = UIBarStyleDefault;
      [pickerToolbar setUserInteractionEnabled:YES];
      self.setTimeLabel = [[UILabel alloc] initWithFrame:CGRectZero];
      pickerToolbar.items = [NSArray arrayWithObjects:
                             [[UIBarButtonItem alloc]initWithTitle:NSLocalizedString(@"Cancel", nil)
                                                             style:UIBarButtonItemStyleBordered
                                                            target:self
                                                            action:@selector(cancelDateEdit)],
                             [[UIBarButtonItem alloc]
                              initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace
                              target:nil
                              action:nil],
                             [[UIBarButtonItem alloc] initWithCustomView:self.setTimeLabel],
                             [[UIBarButtonItem alloc]
                              initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace
                              target:nil
                              action:nil],
                             [[UIBarButtonItem alloc]initWithTitle:NSLocalizedString(@"Done", nil)
                                                             style:UIBarButtonItemStyleBordered
                                                            target:self
                                                            action:@selector(saveDateEdit)],nil];
      [pickerToolbar sizeToFit];
      [self addSubview:pickerToolbar];

      self.picker = [[UIDatePicker alloc] initWithFrame:CGRectMake(0, pickerToolbar.frame.size.height, 0, 0)];
      [self.picker addTarget:self action:@selector(onDateChanged:) forControlEvents:UIControlEventValueChanged];
      self.picker.datePickerMode = UIDatePickerModeTime;
      [self.picker setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:0]];
      [self.picker sizeToFit];
      [self addSubview:self.picker];
      self.frame = CGRectMake(0,
                              0,
                              320,
                              self.picker.frame.size.height + pickerToolbar.frame.size.height);
    }
    return self;
}

- (void)setDate:(NSDate *)date {
  _date = date;
  self.picker.date = date;
}

- (void)setTitle:(NSString *)title {
  _title = title;
  self.setTimeLabel.text = title;
  [self.setTimeLabel sizeToFit];
}

- (void)cancelDateEdit {
  if ([self.delegate respondsToSelector:@selector(cancelDateEdit)]) {
    [self.delegate cancelDateEdit];
  }
}

- (void)saveDateEdit {
  if ([self.delegate respondsToSelector:@selector(saveDateEdit)]) {
    [self.delegate saveDateEdit];
  }
}

- (void)onDateChanged:(UIDatePicker *)datePicker {
  if ([self.delegate respondsToSelector:@selector(onDateChanged:)]) {
    self.date = datePicker.date;
    [self.delegate onDateChanged:self];
  }
}

@end
