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

#import "PacoLayout.h"

@implementation PacoLayout


+ (void)splitHorizontalRect:(CGRect)rect percent:(CGFloat)percent leftOut:(CGRect *)leftOut rightOut:(CGRect *)rightOut {
  CGFloat w = rect.size.width * percent;
  *leftOut = CGRectMake(rect.origin.x, rect.origin.y, w, rect.size.height);
  *rightOut = CGRectMake(rect.origin.x + w, rect.origin.y, rect.size.width - w, rect.size.height);
  *leftOut = CGRectIntegral(*leftOut);
  *rightOut = CGRectIntegral(*rightOut);
}

+ (void)splitHorizontalRect:(CGRect)rect xOffset:(CGFloat)xOffset leftOut:(CGRect *)leftOut rightOut:(CGRect *)rightOut {
  CGFloat w = xOffset;
  *leftOut = CGRectMake(rect.origin.x, rect.origin.y, w, rect.size.height);
  *rightOut = CGRectMake(rect.origin.x + w, rect.origin.y, rect.size.width - w, rect.size.height);
  *leftOut = CGRectIntegral(*leftOut);
  *rightOut = CGRectIntegral(*rightOut);
}

+ (void)splitVerticalRect:(CGRect)rect percent:(CGFloat)percent topOut:(CGRect *)topOut bottomOut:(CGRect *)bottomOut {
  CGFloat h = rect.size.height * percent;
  *topOut = CGRectMake(rect.origin.x, rect.origin.y, rect.size.width, h);
  *bottomOut = CGRectMake(rect.origin.x, rect.origin.y + h, rect.size.width, rect.size.height - h);
  *topOut = CGRectIntegral(*topOut);
  *bottomOut = CGRectIntegral(*bottomOut);
}

+ (void)splitVerticalRect:(CGRect)rect yOffset:(CGFloat)yOffset topOut:(CGRect *)topOut bottomOut:(CGRect *)bottomOut {
  CGFloat h = yOffset;
  *topOut = CGRectMake(rect.origin.x, rect.origin.y, rect.size.width, h);
  *bottomOut = CGRectMake(rect.origin.x, rect.origin.y + h, rect.size.width, rect.size.height - h);
  *topOut = CGRectIntegral(*topOut);
  *bottomOut = CGRectIntegral(*bottomOut);
}

+ (CGRect)centerRect:(CGSize)rectSize inRect:(CGRect)parent {
  int width = rectSize.width;
  int height = rectSize.height;
  int leftoverX = parent.size.width - width;
  int leftoverY = parent.size.height - height;
  return CGRectIntegral(CGRectMake(parent.origin.x + (leftoverX / 2),
                                   parent.origin.y + (leftoverY / 2),
                                   width,
                                   height));
}

+ (CGRect)leftAlignRect:(CGSize)rectSize inRect:(CGRect)parent {
  int width = rectSize.width;
  int height = rectSize.height;
  int leftoverY = parent.size.height - height;
  return CGRectIntegral(CGRectMake(parent.origin.x,
                                   parent.origin.y + (leftoverY / 2),
                                   width,
                                   height));
}

+ (CGRect)rightAlignRect:(CGSize)rectSize inRect:(CGRect)parent {
  int width = rectSize.width;
  int height = rectSize.height;
  int leftoverX = parent.size.width - width;
  int leftoverY = parent.size.height - height;
  return CGRectIntegral(CGRectMake(leftoverX,
                                   parent.origin.y + (leftoverY / 2),
                                   width,
                                   height));
}


+ (NSArray *)splitRectHorizontally:(CGRect)rect numSections:(NSUInteger)numSections {
  CGFloat width = rect.size.width / (CGFloat)numSections;
  NSMutableArray *array = [NSMutableArray array];
  for (int i = 0; i < numSections; ++i) {
    CGRect r = CGRectMake(rect.origin.x + (width * i), rect.origin.y, width, rect.size.height);
    r = CGRectIntegral(r);
    NSValue *value = [NSValue valueWithCGRect:r];
    [array addObject:value];
  }
  return array;
}

+ (NSArray *)splitRectVertically:(CGRect)rect numSections:(NSUInteger)numSections {
  CGFloat height = rect.size.height / (CGFloat)numSections;
  NSMutableArray *array = [NSMutableArray array];
  for (int i = 0; i < numSections; ++i) {
    CGRect r = CGRectMake(rect.origin.x, rect.origin.y + (height * i), rect.size.width, height);
    r = CGRectIntegral(r);
    NSValue *value = [NSValue valueWithCGRect:r];
    [array addObject:value];
  }
  return array;
}

+ (void)layoutViews:(NSArray *)views inGridWithWidth:(int)numColumns gridHeight:(int)numRows inRect:(CGRect)rect {
  assert([views count] <= (numColumns * numRows));
  NSArray *rowRects = [PacoLayout splitRectVertically:rect numSections:numRows];
  int viewIndex = 0;
  for (int row = 0; row < numRows; ++row) {
    if (viewIndex >= [views count]) {
      break;
    }
    NSValue *rowValue = rowRects[row];
    CGRect rowRect = [rowValue CGRectValue];
    NSArray *cellRects = [PacoLayout splitRectHorizontally:rowRect numSections:numColumns];
    for (int col = 0; col < numColumns; ++col) {
      NSValue *colValue = cellRects[col];
      CGRect cellRect = [colValue CGRectValue];
      if (viewIndex >= [views count]) {
        break;
      }
      UIView *view = views[viewIndex];
      view.frame = [PacoLayout centerRect:view.frame.size inRect:cellRect];
      viewIndex++;
    }
  }
}

+ (CGSize)textSizeToFitSize:(CGSize)bounds
                       text:(NSString *)text
                       font:(UIFont *)font {
  UIView *parent = [[UIView alloc] initWithFrame:CGRectMake(0, 0, bounds.width, FLT_MAX)];
  UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, bounds.width, FLT_MAX)];
  label.numberOfLines = 0;
  [parent addSubview:label];
  label.text = text;
  if (font) {
    label.font = font;
  }
  [label sizeToFit];
  return label.frame.size;
}

@end
