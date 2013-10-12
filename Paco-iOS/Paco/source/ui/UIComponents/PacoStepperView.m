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

#import "PacoColor.h"
#import "PacoFont.h"
#import "PacoLayout.h"

@interface PacoStepperView()

@property (nonatomic, retain) UILabel *valueLabel;
@property (nonatomic, retain) UIStepper *stepper;

@end

@implementation PacoStepperView

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.backgroundColor = [PacoColor pacoBackgroundWhite];
    self.valueLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    self.valueLabel.text = @"Value";
    self.valueLabel.font = [PacoFont pacoTableCellFont];
    self.valueLabel.backgroundColor = [UIColor clearColor];
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.valueLabel.font = [PacoFont pacoTableCellFont];
    self.stepper = [[UIStepper alloc] initWithFrame:CGRectZero];
    self.stepper.minimumValue = 0;
    self.stepper.maximumValue = NSUIntegerMax;
    self.stepper.stepValue = 1.0;
    self.stepper.wraps = NO;
    self.stepper.autorepeat = YES;
    self.stepper.continuous = YES;
    self.stepper.backgroundColor = [PacoColor pacoBackgroundWhite];
    [self.stepper addTarget:self
                     action:@selector(valueChanged:)
           forControlEvents:UIControlEventValueChanged];
    [self addSubview:self.valueLabel];
    [self addSubview:self.stepper];
    [self.valueLabel sizeToFit];
    [self.stepper sizeToFit];
  }
  return self;
}

- (void)setFormat:(NSString *)format {
  _format = format;
  self.valueLabel.text = [NSString stringWithFormat:format, (int)self.stepper.value];
  [self.valueLabel sizeToFit];
  [self setNeedsLayout];
}

- (void)setValue:(NSNumber *)value {
  _value = value;
  self.stepper.value = [_value intValue];
  if (value && _format) {
    self.valueLabel.text = [NSString stringWithFormat:_format, [value intValue]];
  }
  [self.valueLabel sizeToFit];
  [self setNeedsLayout];
}

- (void)setMaxValue:(double)maxValue {
  _maxValue = maxValue;
  self.stepper.maximumValue = maxValue;
}

- (void)setMinValue:(double)minValue {
  _minValue = minValue;
  self.stepper.minimumValue = minValue;
}

- (void)valueChanged:(UIStepper *)stepper{
  int value = stepper.value;
  self.value = [NSNumber numberWithInt:value];
  self.valueLabel.text = [NSString stringWithFormat:_format, value];
  [self.valueLabel sizeToFit];
  [self setNeedsLayout];
  [self.tableDelegate dataUpdated:self
                          rowData:[NSNumber numberWithInt:stepper.value]
                          reuseId:self.reuseId];
  if ([self.delegate respondsToSelector:@selector(onStepperValueChanged:)]) {
    [self.delegate onStepperValueChanged:self];
  }
}

- (void)layoutSubviews {
  [super layoutSubviews];

  CGRect top,bottom;
  [PacoLayout splitVerticalRect:self.bounds percent:0.5 topOut:&top bottomOut:&bottom];
  CGSize sizeLabel = self.valueLabel.frame.size;
  CGSize sizeSlider = self.stepper.frame.size;
  CGRect labelRect = [PacoLayout centerRect:sizeLabel inRect:top];
  CGRect sliderRect = [PacoLayout centerRect:sizeSlider inRect:bottom];
  self.valueLabel.frame = labelRect;
  self.stepper.frame = sliderRect;
}

@end
