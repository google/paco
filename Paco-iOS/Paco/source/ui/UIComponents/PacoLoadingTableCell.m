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

#import "UIColor+Paco.h"
#import "UIFont+Paco.h"
#import "PacoLayout.h"

@interface PacoLoadingTableCell ()
@property (nonatomic, retain) UIActivityIndicatorView *spinner;
@property (nonatomic, retain) UILabel *loadingTextLabel;
@end

@implementation PacoLoadingTableCell


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
  self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
  if (self) {
    _loadingText = @"Loading ...";
    _spinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    _spinner.hidesWhenStopped = NO;
    _spinner.color = [UIColor pacoDarkBlue];
    _spinner.frame = CGRectMake(0, 0, 80, 80);
    [self addSubview:_spinner];
    [_spinner startAnimating];

    _loadingTextLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    _loadingTextLabel.text = _loadingText;
    _loadingTextLabel.textColor = [UIColor blackColor];
    _loadingTextLabel.backgroundColor = [UIColor clearColor];
    [self addSubview:_loadingTextLabel];
    [_loadingTextLabel sizeToFit];

  }
  return self;
}

- (CGSize)sizeThatFits:(CGSize)size {
  CGSize textSize = [PacoLayout textSizeToFitSize:self.frame.size text:_loadingText font:nil];
  CGSize spinnerSize = _spinner.frame.size;
  return CGSizeMake(size.width,
                    10 + textSize.height + 10 + spinnerSize.height + 10);
}

+ (NSNumber *)heightForData:(id)data {
  CGSize textSize = [PacoLayout textSizeToFitSize:CGSizeMake(320,480)
                                             text:NSLocalizedString(@"Loading", nil) font:nil];
  CGSize spinnerSize = CGSizeMake(40,40);
  return @(10 + textSize.height + 10 + spinnerSize.height + 10);
}

- (void)layoutSubviews {
  self.backgroundColor = [UIColor pacoBackgroundWhite];
  _loadingTextLabel.textColor = [UIColor pacoDarkBlue];
  _loadingTextLabel.font = [UIFont pacoTableCellFont];

  NSArray *rowData = self.rowData;
  self.loadingText = rowData[1];
  _loadingTextLabel.text = self.loadingText;

  CGSize textSize = [PacoLayout textSizeToFitSize:self.frame.size text:_loadingText font:[UIFont pacoTableCellFont]];
  CGRect textCenterRect = CGRectMake(0, 0, self.bounds.size.width, textSize.height + 20);
  _loadingTextLabel.frame = [PacoLayout centerRect:textSize inRect:textCenterRect];

  CGSize spinnerSize = _spinner.frame.size;
  CGRect spinnerCenterRect = CGRectMake(0, textSize.height, self.bounds.size.width, self.bounds.size.height - textSize.height);
  CGRect spinnerRect = [PacoLayout centerRect:spinnerSize inRect:spinnerCenterRect];
  _spinner.frame = spinnerRect;
  [super layoutSubviews];
}

@end
