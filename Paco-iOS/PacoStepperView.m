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

#import "PacoStepperView.h"

#import "UIColor+Paco.h"
#import "UIFont+Paco.h"
#import "PacoLayout.h"

@interface PacoStepperView()<UITextFieldDelegate>

@property (nonatomic, retain) UITextField *valueLabel;
@property (nonatomic, retain) UIStepper *stepper;

@end

@implementation PacoStepperView

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.backgroundColor = [UIColor pacoBackgroundWhite];
    self.valueLabel = [[UITextField alloc] initWithFrame:CGRectZero];
    self.valueLabel.borderStyle = UITextBorderStyleRoundedRect;
    self.valueLabel.text = @"0";
    self.valueLabel.delegate = self;
    self.valueLabel.font = [UIFont pacoTableCellFont];
    self.valueLabel.backgroundColor = [UIColor clearColor];
    self.valueLabel.textAlignment = NSTextAlignmentCenter;
    [self.valueLabel addTarget:self
                        action:@selector(textFieldDidChange:)
              forControlEvents:UIControlEventEditingChanged];
    self.valueLabel.keyboardType = UIKeyboardTypeNumberPad;
    self.valueLabel.font = [UIFont pacoTableCellFont];

    UIToolbar* numberToolbar = [[UIToolbar alloc]initWithFrame:CGRectMake(200, 0, 320, 50)];
    numberToolbar.barStyle = UIBarStyleDefault;
    numberToolbar.items = @[[[UIBarButtonItem alloc]
                        initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace
                                             target:nil
                                             action:nil],
              [[UIBarButtonItem alloc]initWithTitle:@"Done"
                                              style:UIBarButtonItemStyleBordered
                                             target:self
                                             action:@selector(cancelNumberPad)]];
    [numberToolbar sizeToFit];
    self.valueLabel.inputAccessoryView = numberToolbar;

    self.stepper = [[UIStepper alloc] initWithFrame:CGRectZero];
    self.stepper.stepValue = 1.0;
    self.stepper.wraps = NO;
    self.stepper.autorepeat = YES;
    self.stepper.continuous = YES;
    self.stepper.backgroundColor = [UIColor pacoBackgroundWhite];
    [self.stepper addTarget:self
                     action:@selector(valueChanged:)
           forControlEvents:UIControlEventValueChanged];
    [self addSubview:self.valueLabel];
    [self addSubview:self.stepper];
    [self.stepper sizeToFit];

    [self layoutSubviews];
  }
  return self;
}

- (void)setFormat:(NSString *)format {
  _format = format;
  self.valueLabel.text = [NSString stringWithFormat:format, (long long)self.stepper.value];
}

- (void)setValue:(NSNumber *)value {
  _value = value;
  self.stepper.value = [_value longLongValue];
  if (value && _format) {
    self.valueLabel.text = [NSString stringWithFormat:_format, [value longLongValue]];
  }
}

- (void)setMaxValue:(long long)maxValue {
  _maxValue = maxValue;
  self.stepper.maximumValue = maxValue;
}

- (void)setMinValue:(long long)minValue {
  _minValue = minValue;
  self.stepper.minimumValue = minValue;
}

- (BOOL)valueChangedInRange:(long long)value {
  if (value > self.maxValue){
    return NO;
  }
  return YES;
}

- (void)valueChanged:(UIStepper *)stepper {
  long long valueIs = stepper.value;
  self.value = @(valueIs);
  self.valueLabel.text = [NSString stringWithFormat:_format,valueIs];
  [self.tableDelegate dataUpdated:self
                          rowData:[NSNumber numberWithLongLong:self.stepper.value]
                          reuseId:self.reuseId];
  if ([self.delegate respondsToSelector:@selector(onStepperValueChanged:)]) {
    [self.delegate onStepperValueChanged:self];
  }
  [self layoutSubviews];
}

- (void)cancelNumberPad {
  [self.valueLabel resignFirstResponder];
}

#pragma mark--- UITextFieldDelegate

- (void)textFieldDidChange:(UITextField *)textField {
  self.value = @(textField.text.longLongValue);
  self.valueLabel.text = textField.text;
  self.stepper.value = textField.text.longLongValue;
  [self.tableDelegate dataUpdated:self
                          rowData:[NSNumber numberWithLongLong:self.stepper.value]
                          reuseId:self.reuseId];
  if ([self.delegate respondsToSelector:@selector(onStepperValueChanged:)]) {
    [self.delegate onStepperValueChanged:self];
  }
  [self layoutSubviews];
}

- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range
replacementString:(NSString *)string{
  NSString *str = [textField.text stringByAppendingString:string];
  BOOL edit = (([self valueChangedInRange:str.longLongValue]) && (str.length <= 15));
  if ((!edit) && (![string isEqualToString:@""])) {
    return NO;
  }
  return YES;
}

- (void)textFieldDidBeginEditing:(UITextField *)textField {
  self.value =@(textField.text.longLongValue);
  if ([self.delegate respondsToSelector:@selector(onTextFieldEditBegan:)]) {
    [self.delegate onTextFieldEditBegan:textField];
  }
}

- (BOOL)textFieldShouldEndEditing:(UITextField *)textField {
  return YES;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
  [textField endEditing:YES];
  return YES;
}

- (void)layoutSubviews {
  [super layoutSubviews];

  CGRect top,bottom;
  [PacoLayout splitVerticalRect:self.bounds percent:0.5 topOut:&top bottomOut:&bottom];
  CGSize sizeLabel = self.valueLabel.frame.size;
  CGSize sizeSlider = self.stepper.frame.size;
  CGRect labelRect = [PacoLayout centerRect:sizeLabel inRect:top];
  CGRect sliderRect = [PacoLayout centerRect:sizeSlider inRect:bottom];
  self.valueLabel.frame = CGRectMake(0, labelRect.origin.y, self.frame.size.width, 30);
  self.stepper.frame = sliderRect;
}

@end
