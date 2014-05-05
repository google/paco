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

#import "PacoCustomFeedbackController.h"
#import "PacoExperiment.h"

@interface JavascriptExperimentLoader : NSObject
@property(nonatomic, strong) PacoExperiment* experiment;
@end

@implementation JavascriptExperimentLoader

- (NSString*)getExperiment {
}
@end


@interface PacoCustomFeedbackController ()
@property(nonatomic, strong) UIWebView* webView;
@property(nonatomic, strong) PacoExperiment* experiment;
@end


@implementation PacoCustomFeedbackController

+ (id)controllerWithExperiment:(PacoExperiment*)experiment {
  PacoCustomFeedbackController* controller =
      [[PacoCustomFeedbackController alloc] initWithNibName:nil bundle:nil];
  controller.experiment = experiment;
  return controller;
}

- (void)viewDidLoad {
  [super viewDidLoad];
  self.webView = [[UIWebView alloc] initWithFrame:self.view.frame];
  self.webView.scalesPageToFit = YES;
  [self.view addSubview:self.webView];
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
  // Dispose of any resources that can be recreated.
}



@end
