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

#import "PacoTitleView.h"

#import "UIColor+Paco.h"
#import "PacoFont.h"
#import "PacoLayout.h"

@implementation PacoTitleView


+ (PacoTitleView*)viewWithText:(NSString*)text {
  return [[PacoTitleView alloc] initWithText:text andIcon:nil];
}

+ (PacoTitleView*)viewWithDefaultIconAndText:(NSString*)text {
  return [[PacoTitleView alloc] initWithText:text andIcon:@"paco32.png"];
}

- (id)initWithText:(NSString*)text andIcon:(NSString*)iconName{
  self = [super initWithFrame:CGRectZero];
  if (self) {
    if (iconName) {
      _icon = [[UIImageView alloc] initWithImage:[UIImage imageNamed:iconName]];
      [self addSubview:_icon];
      [_icon sizeToFit];
    }
    _title = [[UILabel alloc] initWithFrame:CGRectZero];
    _title.text = text;
    _title.font = [PacoFont pacoNavbarTitleFont];
    _title.textColor = [UIColor pacoDarkBlue];
    _title.backgroundColor = [UIColor clearColor];
    _title.adjustsFontSizeToFitWidth = YES;
    _title.minimumScaleFactor = 0.8;
    [self addSubview:_title];
    [_title sizeToFit];
  }
  return self;
}

- (CGSize)sizeThatFits:(CGSize)size {
  CGFloat width = _title.frame.size.width + (_icon ? _icon.frame.size.width : 0) + 10;
  return CGSizeMake(width > size.width ? size.width : (_icon ? width : 200), 30);
}

- (void)layoutSubviews {
  if (!_icon) {
    _title.frame = [PacoLayout centerRect:_title.frame.size inRect:self.bounds];
    return;
  }
  
  CGRect left, right;
  CGRect bounds2 = self.frame;
  bounds2.origin = CGPointZero;
  bounds2.size = [self sizeThatFits:CGSizeMake(200,30)];
  CGRect frame = self.frame;
  frame.origin.y = 8;
  frame.size = bounds2.size;
  self.frame = frame;
  [PacoLayout splitHorizontalRect:bounds2 xOffset:(_icon.frame.size.width + 20) leftOut:&left rightOut:&right];
  CGRect iconFrame = [PacoLayout centerRect:_icon.frame.size inRect:left];
  CGRect titleFrame = [PacoLayout centerRect:_title.frame.size inRect:right];
  _icon.frame = iconFrame;
  _title.frame = titleFrame;
}

@end
