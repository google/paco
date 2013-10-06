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

#import "PacoMenuButton.h"

#import "PacoColor.h"
#import "PacoFont.h"
#import "PacoLayout.h"

@implementation PacoMenuButton


- (id)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    _button = [UIButton buttonWithType:UIButtonTypeCustom];
    _text = [[UILabel alloc] initWithFrame:CGRectZero];
    [self addSubview:_button];
    [self addSubview:_text];
    _text.font = [PacoFont pacoMenuButtonFont];
    _text.textColor = [PacoColor pacoBlue];
    _text.backgroundColor = [UIColor clearColor];
    _text.clipsToBounds = NO;
    self.clipsToBounds = NO;
  }
  return self;
}

- (CGSize)sizeThatFits:(CGSize)size {
  [_button sizeToFit];
  [_text sizeToFit];
  float totalHeight = _button.frame.size.height + _text.frame.size.height + 10;
  return CGSizeMake((size.width == 0 ? _button.frame.size.width : MIN(size.width, _button.frame.size.width)),
                    (size.height == 0 ? totalHeight : MIN(size.height, totalHeight)));
}

- (void)layoutSubviews {
  [super layoutSubviews];
  CGSize buttonSize = _button.frame.size;
  CGRect top, bottom;
  [PacoLayout splitVerticalRect:self.bounds
                        yOffset:(buttonSize.height + 10)
                         topOut:&top
                      bottomOut:&bottom];
  _button.frame = [PacoLayout centerRect:_button.frame.size inRect:top];
  _text.frame = [PacoLayout centerRect:_text.frame.size inRect:bottom];
}

@end
