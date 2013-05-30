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

#import "PacoColor.h"
#import "PacoFont.h"
#import "PacoLayout.h"

@implementation PacoTitleView

@synthesize icon = icon_;
@synthesize title = title_;

- (id)initIconAndText:(NSString *)text {
  self = [super initWithFrame:CGRectZero];
  if (self) {
    icon_ = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"paco32.png"]];
    title_ = [[UILabel alloc] initWithFrame:CGRectZero];
    title_.text = text;
    title_.font = [PacoFont pacoNavbarTitleFont];
    title_.textColor = [PacoColor pacoDarkBlue];
    title_.backgroundColor = [UIColor clearColor];
    title_.adjustsFontSizeToFitWidth = YES;
    title_.minimumScaleFactor = 0.8;
    [self addSubview:icon_];
    [self addSubview:title_];
    [icon_ sizeToFit];
    [title_ sizeToFit];
  }
  return self;
}

- (id)initText:(NSString *)text {
  self = [super initWithFrame:CGRectZero];
  if (self) {
    title_ = [[UILabel alloc] initWithFrame:CGRectZero];
    title_.text = text;
    title_.font = [PacoFont pacoNavbarTitleFont];
    title_.textColor = [PacoColor pacoDarkBlue];
    title_.backgroundColor = [UIColor clearColor];
    title_.adjustsFontSizeToFitWidth = YES;
    title_.minimumScaleFactor = 0.8;
    [self addSubview:title_];
    [title_ sizeToFit];
  }
  return self;
}

- (CGSize)sizeThatFits:(CGSize)size {
  CGFloat width = title_.frame.size.width + (icon_ ? icon_.frame.size.width : 0) + 10;
  return CGSizeMake(width > size.width ? size.width : (icon_ ? width : 200), 30);
}

- (void)layoutSubviews {
  if (!icon_) {
    title_.frame = [PacoLayout centerRect:title_.frame.size inRect:self.bounds];
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
  [PacoLayout splitHorizontalRect:bounds2 xOffset:(icon_.frame.size.width + 20) leftOut:&left rightOut:&right];
  CGRect iconFrame = [PacoLayout centerRect:icon_.frame.size inRect:left];
  CGRect titleFrame = [PacoLayout centerRect:title_.frame.size inRect:right];
  icon_.frame = iconFrame;
  title_.frame = titleFrame;
}

@end
