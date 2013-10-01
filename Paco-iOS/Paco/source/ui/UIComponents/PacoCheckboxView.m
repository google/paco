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

#import "PacoCheckboxView.h"

#import "PacoColor.h"
#import "PacoFont.h"
#import "PacoLayout.h"

NSString * const kStrPacoCheckboxChanged = @"kPacoNotificationCheckboxChanged";

@interface PacoCheckboxView ()
@property (nonatomic, retain) NSArray *buttons;
@property (nonatomic, retain) NSArray *labels;
@end
@implementation PacoCheckboxView


- (void)reloadDays {
  unsigned int days = [self.bitFlags unsignedIntValue];
  int buttonIndex = 0;
  if (!self.buttons) {
    [self rebuildCheckboxes];
  }
  for (UIButton *button in self.buttons) {
    unsigned int flag = (1 << buttonIndex);
    if (days & flag) {
      UIImage *checked = [UIImage imageNamed:@"uicheckbox_checked.png"];
      [button setBackgroundImage:checked forState:UIControlStateNormal];
      [button setBackgroundImage:checked forState:UIControlStateHighlighted];
    } else {
      UIImage *unchecked = [UIImage imageNamed:@"uicheckbox_unchecked.png"];
      [button setBackgroundImage:unchecked forState:UIControlStateNormal];
      [button setBackgroundImage:unchecked forState:UIControlStateHighlighted];
    }
    [button setNeedsLayout];
    [button setNeedsDisplay];
    ++buttonIndex;
  }
}

- (void)onButton:(UIButton *)button {
  NSInteger buttonIndex = [self.buttons indexOfObject:button];
  assert(buttonIndex != NSNotFound);
  unsigned int days = [self.bitFlags unsignedIntValue];
  unsigned int flag = (1 << buttonIndex);
  if (self.radioStyle) {
    days = flag;
  } else {
    if (days & flag) {
      unsigned int clearMask = ~flag;
      days &= clearMask;
    } else {
      days |= flag;
    }
  }
  self.bitFlags = [NSNumber numberWithUnsignedInt:days];
  [self.tableDelegate dataUpdated:self rowData:self.bitFlags reuseId:self.reuseId];
  [self reloadDays];
  if ([self.delegate respondsToSelector:@selector(onCheckboxChanged:)]) {
    [self.delegate onCheckboxChanged:self];
  }
}

- (void)setBitFlags:(NSNumber *)flags {
  _bitFlags = flags;
  [self reloadDays];
}

- (void)rebuildCheckboxes {
  NSMutableArray *buttonArray = [NSMutableArray array];
  NSMutableArray *labelArray = [NSMutableArray array];
  for (NSString *labelString in self.optionLabels) {
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectZero];
    [label setTextColor:[UIColor blackColor]];
    [label setText:labelString];
    label.backgroundColor = [UIColor clearColor];
    label.font = [PacoFont pacoTableCellFont];
    [self addSubview:label];
    [label sizeToFit];
    [labelArray addObject:label];
    
    UIButton *button = [[UIButton alloc] initWithFrame:CGRectZero];
    button.backgroundColor = [UIColor lightGrayColor];
    UIImage *unchecked = [UIImage imageNamed:@"uicheckbox_unchecked.png"];
    [button setBackgroundImage:unchecked forState:UIControlStateNormal];
    [button setBackgroundImage:unchecked forState:UIControlStateHighlighted];
    [button addTarget:self action:@selector(onButton:) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:button];
    [button sizeToFit];
    [buttonArray addObject:button];
  }
  self.labels = labelArray;
  self.buttons = buttonArray;
}


- (void)setOptionLabels:(NSArray *)optionLabels {
  _optionLabels = optionLabels;
  [self rebuildCheckboxes];
}


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    self.backgroundColor = [PacoColor pacoBackgroundWhite];
    self.selectionStyle = UITableViewCellSelectionStyleNone;
    self.vertical = NO;
  }
  return self;
}

- (CGSize)sizeThatFits:(CGSize)size {
  return CGSizeMake(300, 78);
}

+ (NSNumber *)heightForData:(id)data {
  return [NSNumber numberWithInt:78];
}

- (void)layoutSubviews {
  [super layoutSubviews];

  if (self.vertical) {
    CGRect left, right;
    CGRect rect = CGRectInset(self.bounds, 50, 10);
    [PacoLayout splitHorizontalRect:rect percent:0.25 leftOut:&left rightOut:&right];
    NSArray *leftSections = [PacoLayout splitRectVertically:left numSections:self.optionLabels.count];
    NSArray *rightSections = [PacoLayout splitRectVertically:right numSections:self.optionLabels.count];
    
    for (int i = 0; i < self.optionLabels.count; ++i) {
      CGRect leftRect = [[leftSections objectAtIndex:i] CGRectValue];
      CGRect rightRect = [[rightSections objectAtIndex:i] CGRectValue];
      UILabel *label = [self.labels objectAtIndex:i];
      UIButton *button = [self.buttons objectAtIndex:i];
      button.frame = [PacoLayout centerRect:button.frame.size inRect:leftRect];
      label.frame = [PacoLayout centerRect:label.frame.size inRect:rightRect];
    }
  } else {
    CGRect top, bottom;
    CGRect rect = CGRectInset(self.bounds, 50, 10);
    [PacoLayout splitVerticalRect:rect percent:0.5 topOut:&top bottomOut:&bottom];
    NSArray *topSections = [PacoLayout splitRectHorizontally:top numSections:self.optionLabels.count];
    NSArray *bottomSections = [PacoLayout splitRectHorizontally:bottom numSections:self.optionLabels.count];
    
    for (int i = 0; i < self.optionLabels.count; ++i) {
      CGRect topRect = [[topSections objectAtIndex:i] CGRectValue];
      CGRect bottomRect = [[bottomSections objectAtIndex:i] CGRectValue];
      UILabel *label = [self.labels objectAtIndex:i];
      UIButton *button = [self.buttons objectAtIndex:i];

      label.frame = [PacoLayout centerRect:label.frame.size inRect:topRect];
      button.frame = [PacoLayout centerRect:button.frame.size inRect:bottomRect];
    }
  }
}


- (BOOL)hasCheckedBox {
  return 0 < [self.bitFlags intValue];
}

@end
