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

#import "PacoCustomAlertPopUp.h"
#import "PacoFont.h"

@implementation PacoCustomAlertPopUp

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
      [self initViewObjects];
    }
    return self;
}

- (void)initViewObjects {
  UIView* bgLayer = [[UIView alloc] init];
  bgLayer.frame = self.frame;
  [bgLayer setUserInteractionEnabled:YES];
  bgLayer.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
  [self addSubview:bgLayer];

  UIView* container = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width * 0.8, self.frame.size.height * 0.6)];
  container.backgroundColor = [UIColor grayColor];
  container.center = self.center;
  [self addSubview:container];

  UIView* labelsHolder = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width * 0.8, container.frame.size.height * 0.8)];
  labelsHolder.backgroundColor = [UIColor whiteColor];
  [container addSubview:labelsHolder];

  UILabel* headerLbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, container.frame.size.width, container.frame.size.height * 0.2)];
  headerLbl.numberOfLines = 1;
  headerLbl.textAlignment = NSTextAlignmentCenter;
  headerLbl.backgroundColor = [UIColor clearColor];
  headerLbl.textColor = [UIColor blackColor];
  headerLbl.font = [UIFont boldSystemFontOfSize:15];
  headerLbl.text = @"How to Create an Experiment";
  [labelsHolder addSubview:headerLbl];

  UILabel* messageLbl = [[UILabel alloc] initWithFrame:CGRectMake(7, container.frame.size.height * 0.15, container.frame.size.width - 14, container.frame.size.height * 0.6)];
  messageLbl.numberOfLines = 0;
  messageLbl.textAlignment = NSTextAlignmentLeft;
  messageLbl.backgroundColor = [UIColor clearColor];
  messageLbl.textColor = [UIColor blackColor];
  messageLbl.font = [UIFont systemFontOfSize:13];
  messageLbl.text = @"Since creating experiments involves a fair amount of text entry, a phone is not so well-suited to creating experiments.\n\nPlease point your browser to\nhttp://pacoapp.com/ to create an experiment.";
  [labelsHolder addSubview:messageLbl];

  UIButton* confirmBtn = [UIButton buttonWithType:UIButtonTypeRoundedRect];
  [confirmBtn setTitle:@"OK" forState:UIControlStateNormal];
  if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 7.0) {
    confirmBtn.titleLabel.font = [PacoFont pacoNormalButtonFont];
  }
  confirmBtn.backgroundColor = [UIColor whiteColor];
  confirmBtn.frame = CGRectMake(0, 1 + container.frame.size.height * 0.8, container.frame.size.width, container.frame.size.height * 0.2);
  confirmBtn.titleLabel.textAlignment = NSTextAlignmentCenter;
  [confirmBtn addTarget:self action:@selector(removePopUp) forControlEvents:UIControlEventTouchUpInside];
  [container addSubview:confirmBtn];
}

- (void)removePopUp {
  for (UIView *v in self.subviews) {
      [v removeFromSuperview];
  }
  [self removeFromSuperview];
}


@end
