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

#import "PacoLoadingTableCell.h"

#import "PacoColor.h"
#import "pacoFont.h"
#import "PacoLayout.h"

@interface PacoLoadingTableCell ()
@property (retain) UIActivityIndicatorView *spinner;
@property (retain) UILabel *loadingTextLabel;
@end

@implementation PacoLoadingTableCell

@synthesize loadingText = loadingText_;
@synthesize loadingTextLabel = loadingTextLabel_;
@synthesize spinner = spinner_;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    loadingText_ = @"Loading ...";
    spinner_ = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    spinner_.hidesWhenStopped = NO;
    spinner_.color = [PacoColor pacoDarkBlue];
    spinner_.frame = CGRectMake(0, 0, 80, 80);
    [self addSubview:spinner_];
    [spinner_ startAnimating];
    
    loadingTextLabel_ = [[UILabel alloc] initWithFrame:CGRectZero];
    loadingTextLabel_.text = loadingText_;
    loadingTextLabel_.textColor = [UIColor blackColor];
    loadingTextLabel_.backgroundColor = [UIColor clearColor];
    [self addSubview:loadingTextLabel_];
    [loadingTextLabel_ sizeToFit];

  }
  return self;
}

- (CGSize)sizeThatFits:(CGSize)size {
  CGSize textSize = [PacoLayout textSizeToFitSize:self.frame.size text:loadingText_ font:nil];
  CGSize spinnerSize = spinner_.frame.size;
  return CGSizeMake(size.width,
                    10 + textSize.height + 10 + spinnerSize.height + 10);
}

+ (NSNumber *)heightForData:(id)data {
  CGSize textSize = [PacoLayout textSizeToFitSize:CGSizeMake(320,480) text:@"Loading ..." font:nil];
  CGSize spinnerSize = CGSizeMake(40,40);
  return [NSNumber numberWithDouble:(10 + textSize.height + 10 + spinnerSize.height + 10)];
}

- (void)layoutSubviews {
  self.backgroundColor = [PacoColor pacoLightBlue];
  loadingTextLabel_.textColor = [PacoColor pacoDarkBlue];
  loadingTextLabel_.font = [PacoFont pacoTableCellFont];

  NSArray *rowData = self.rowData;
  self.loadingText = [rowData objectAtIndex:1];
  loadingTextLabel_.text = self.loadingText;

  CGSize textSize = [PacoLayout textSizeToFitSize:self.frame.size text:loadingText_ font:[PacoFont pacoTableCellFont]];
  CGRect textCenterRect = CGRectMake(0, 0, self.bounds.size.width, textSize.height + 20);
  loadingTextLabel_.frame = [PacoLayout centerRect:textSize inRect:textCenterRect];

  CGSize spinnerSize = spinner_.frame.size;
  CGRect spinnerCenterRect = CGRectMake(0, textSize.height, self.bounds.size.width, self.bounds.size.height - textSize.height);
  CGRect spinnerRect = [PacoLayout centerRect:spinnerSize inRect:spinnerCenterRect];
  spinner_.frame = spinnerRect;
  [super layoutSubviews];
}

@end
