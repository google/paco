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

#import "PacoExperimentDetailsViewController.h"

#import "PacoColor.h"
#import "PacoFont.h"
#import "PacoConsentViewController.h"
#import "PacoModel.h"
#import "PacoTitleView.h"
#import "PacoExperimentDefinition.h"

@interface PacoExperimentDetailsViewController ()

@end

@implementation PacoExperimentDetailsViewController

@synthesize experiment = experiment_;

+ (NSString *)stringFromData:(NSData *)data {
  const char *bytes = [data bytes];
  char *dst = malloc([data length] + 1);
  memset(dst, 0, [data length] + 1);
  memcpy(dst, bytes, [data length]);
  NSString *converted = [NSString stringWithUTF8String:dst];
  free(dst);
  return converted;
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.navigationItem.titleView = [[PacoTitleView alloc] initText:@"Details"];
  }
  return self;
}

- (void)setExperiment:(PacoExperimentDefinition *)experiment {
  experiment_ = experiment;
  PacoTitleView *titleView = (PacoTitleView *)self.navigationItem.titleView;
  titleView.title.text = experiment.title;
}

- (NSString *)jsonStringFromObj:(id)jsonObject {
  NSError *jsonError = nil;
  NSData *jsonData =
      [NSJSONSerialization dataWithJSONObject:jsonObject
                                      options:NSJSONWritingPrettyPrinted
                                        error:&jsonError];
  if (jsonError) {
    return nil;
  }
  return [[self class] stringFromData:jsonData];
}

- (void)viewDidLoad {
  [super viewDidLoad];

  self.view.backgroundColor = [PacoColor pacoLightBlue];

  UILabel *label = [[UILabel alloc] initWithFrame:CGRectZero];
  NSString *labelText = [NSString stringWithFormat:@"Experiment Name:\n\n     %@\n\nExperiment Description:\n\n\t     %@", self.experiment.title, self.experiment.experimentDescription];
  label.text = labelText;
  label.font = [PacoFont pacoTableCellFont];
  label.textColor = [PacoColor pacoDarkBlue];
  label.backgroundColor = [UIColor clearColor];
  label.numberOfLines = 0;
  [self.view addSubview:label];
  CGRect frame = label.frame;
  frame.size.width = 300;
  frame.size.height = 480;
  label.frame = frame;
  [label sizeToFit];
  frame = label.frame;
  frame.origin.y += 100;
  label.frame = frame;

  UIButton *join = [UIButton buttonWithType:UIButtonTypeRoundedRect];
  [join setTitle:@"Join Experiment" forState:UIControlStateNormal];
  [join addTarget:self action:@selector(onJoin) forControlEvents:UIControlEventTouchUpInside];
  [self.view addSubview:join];
  [join sizeToFit];
  frame = join.frame;
  frame.origin.x = (320 - frame.size.width) / 2;
  join.frame = frame;
}

- (void)onJoin {
  PacoConsentViewController *consent = [[PacoConsentViewController alloc] init];
  consent.experiment = self.experiment;
  [self.navigationController pushViewController:consent animated:YES];
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
  // Dispose of any resources that can be recreated.
}

@end
