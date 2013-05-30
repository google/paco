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

@synthesize button = button_;
@synthesize text = text_;

- (id)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    button_ = [UIButton buttonWithType:UIButtonTypeCustom];
    text_ = [[UILabel alloc] initWithFrame:CGRectZero];
    [self addSubview:button_];
    [self addSubview:text_];
    text_.font = [PacoFont pacoMenuButtonFont];
    text_.textColor = [PacoColor pacoBlue];
    text_.backgroundColor = [UIColor clearColor];
    text_.clipsToBounds = NO;
    self.clipsToBounds = NO;
  }
  return self;
}

- (CGSize)sizeThatFits:(CGSize)size {
  [button_ sizeToFit];
  [text_ sizeToFit];
  float totalHeight = button_.frame.size.height + text_.frame.size.height + 10;
  return CGSizeMake((size.width == 0 ? button_.frame.size.width : MIN(size.width, button_.frame.size.width)),
                    (size.height == 0 ? totalHeight : MIN(size.height, totalHeight)));
}

- (void)layoutSubviews {
  [super layoutSubviews];
  CGSize buttonSize = button_.frame.size;
  CGRect top, bottom;
  [PacoLayout splitVerticalRect:self.bounds
                        yOffset:(buttonSize.height + 10)
                         topOut:&top
                      bottomOut:&bottom];
  button_.frame = [PacoLayout centerRect:button_.frame.size inRect:top];
  text_.frame = [PacoLayout centerRect:text_.frame.size inRect:bottom];
}

@end
