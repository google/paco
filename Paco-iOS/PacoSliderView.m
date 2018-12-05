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

#import "PacoSliderView.h"

#import "UIColor+Paco.h"
#import "UIFont+Paco.h"
#import "PacoLayout.h"

NSInteger kSliderViewHeight = 60;

NSString * const kStrPacoSliderChanged = @"kPacoNotificationSliderChanged";

@interface PacoSliderView ()
@property (nonatomic, retain) UILabel *valueLabel;
@property (nonatomic, retain) UISlider *slider;
@end

@implementation PacoSliderView


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.backgroundColor = [UIColor pacoBackgroundWhite];
    self.valueLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    self.valueLabel.text = NSLocalizedString(@"Value", nil);
    self.valueLabel.font = [UIFont pacoTableCellFont];
    self.valueLabel.backgroundColor = [UIColor clearColor];
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.valueLabel.font = [UIFont pacoTableCellFont];
    self.slider = [[UISlider alloc] initWithFrame:CGRectZero];
    self.slider.minimumValue = 1.0;
    self.slider.maximumValue = 30.0;
    self.slider.continuous = NO;
    self.slider.backgroundColor = [UIColor pacoBackgroundWhite];
    self.slider.minimumTrackTintColor = [UIColor pacoDarkBlue];
    self.slider.maximumTrackTintColor = [UIColor pacoDarkBlue];
    self.slider.thumbTintColor = [UIColor pacoDarkBlue];
    [self.slider setValue:1.0];
    [self.slider addTarget:self action:@selector(valueChanged:event:) forControlEvents:UIControlEventTouchUpInside|UIControlEventTouchDragInside];

    [self addSubview:self.valueLabel];
    [self addSubview:self.slider];
    [self.valueLabel sizeToFit];
    [self.slider sizeToFit];
  }
  return self;
}

- (void)setFormat:(NSString *)format {
  _format = format;
  self.valueLabel.text = [NSString stringWithFormat:format, (int)self.slider.value];
  [self.valueLabel sizeToFit];
  [self setNeedsLayout];
}

- (void)setValue:(NSNumber *)value {
  _value = value;
  self.slider.value = [_value intValue];
  if (value && _format) {
    self.valueLabel.text = [NSString stringWithFormat:_format, [value intValue]];
  }

  [self.valueLabel sizeToFit];
  [self setNeedsLayout];
}

- (void)setMaxValue:(double)maxValue {
  _maxValue = maxValue;
  self.slider.maximumValue = maxValue;
}

- (void)setMinValue:(double)minValue {
  _minValue = minValue;
  self.slider.minimumValue = minValue;
}

- (CGSize)sizeThatFits:(CGSize)size {
  return CGSizeMake(300, kSliderViewHeight);
}

+ (NSNumber *)heightForData:(id)data {
  return @(kSliderViewHeight);
}

- (void)valueChanged:(UISlider *)sliderUI event:(UIEvent *)event {
  float fvalue = sliderUI.value;
  int ivalue = (int)fvalue;
  self.value = @(ivalue);
  self.valueLabel.text = [NSString stringWithFormat:_format, (int)sliderUI.value];
  [self.valueLabel sizeToFit];
  [self setNeedsLayout];
  [self.tableDelegate dataUpdated:self rowData:[NSNumber numberWithInt:sliderUI.value] reuseId:self.reuseId];
  if ([self.delegate respondsToSelector:@selector(onSliderChanged:)]) {
    [self.delegate onSliderChanged:self];
  }
}

- (void)layoutSubviews {
  [super layoutSubviews];

  CGRect top,bottom;
  [PacoLayout splitVerticalRect:self.bounds percent:0.5 topOut:&top bottomOut:&bottom];

  CGSize sizeLabel = self.valueLabel.frame.size;
  CGSize sizeSlider = self.slider.frame.size;
  CGRect labelRect = [PacoLayout centerRect:sizeLabel inRect:top];
  sizeSlider.width = 240;
  CGRect sliderRect = [PacoLayout centerRect:sizeSlider inRect:bottom];
  self.valueLabel.frame = labelRect;
  self.slider.frame = sliderRect;
}

@end
