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
#import "PacoDateUtility.h"

@interface PacoDatePickerView ()

@property (nonatomic, copy) NSDate *date;
@property (nonatomic, retain) UIDatePicker* picker;
@property (nonatomic, retain) UILabel* setTimeLabel;

@end

@implementation PacoDatePickerView

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
      UIToolbar* pickerToolbar = [[UIToolbar alloc] initWithFrame:CGRectZero];
      pickerToolbar.barStyle = UIBarStyleDefault;
      [pickerToolbar setUserInteractionEnabled:YES];
      self.setTimeLabel = [[UILabel alloc] initWithFrame:CGRectZero];
      UIBarButtonItem* cancelButton = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Cancel", nil)
                                                                      style:UIBarButtonItemStyleBordered
                                                                     target:self
                                                                     action:@selector(cancelDateEdit)];
      UIBarButtonItem* spaceItem =
          [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace
                                                        target:nil
                                                        action:nil];
      UIBarButtonItem* titleItem =  [[UIBarButtonItem alloc] initWithCustomView:self.setTimeLabel];
      UIBarButtonItem* doneButton = [[UIBarButtonItem alloc]initWithTitle:NSLocalizedString(@"Done", nil)
                                                                    style:UIBarButtonItemStyleBordered
                                                                   target:self
                                                                   action:@selector(saveDateEdit)];
      
      pickerToolbar.items = @[cancelButton, spaceItem, titleItem, spaceItem, doneButton];
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


- (void)setDateNumber:(NSNumber*)dateNumber {
  self.date = [NSDate dateWithTimeIntervalSince1970:([dateNumber longLongValue] / 1000)];
}

- (NSNumber*)dateNumber {
  return [NSNumber numberWithLongLong:[self.date timeIntervalSince1970] * 1000];
}


- (void)setDate:(NSDate *)date {
  if (![_date isEqualToDate:date]) {
    _date = [date copy];
    self.picker.date = date;
  }
}


- (NSString*)dateString {
  return [PacoDateUtility timeStringAMPMFromMilliseconds:[[self dateNumber] longLongValue]];
}


- (void)setTitle:(NSString *)title {
  if (![_title isEqualToString:title]) {
    _title = [title copy];
    self.setTimeLabel.text = title;
    [self.setTimeLabel sizeToFit];
  }
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
