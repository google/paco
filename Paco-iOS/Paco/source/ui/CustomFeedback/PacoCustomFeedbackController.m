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
#import "PacoExperimentFeedback.h"
#import "EasyJSWebView.h"
#import "JavascriptEventLoader.h"
#import "PacoExperimentDefinition.h"
#import "Environment.h"

@interface JavascriptExperimentLoader : NSObject
@property(nonatomic, strong) PacoExperiment* experiment;
@end

@implementation JavascriptExperimentLoader

- (id)initWithExperiment:(PacoExperiment*)experiment {
  self = [super init];
  if (self) {
    _experiment = experiment;
  }
  return self;
}

+ (instancetype)loaderWithExperiment:(PacoExperiment*)experiment {
  return [[[self class] alloc] initWithExperiment:experiment];
}

- (NSString*)getExperiment {
  
}
@end


@interface PacoCustomFeedbackController ()
@property(nonatomic, strong) EasyJSWebView* webView;
@property(nonatomic, strong) PacoExperiment* experiment;
@property(nonatomic, strong) Environment* env;
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
  self.webView = [[EasyJSWebView alloc] initWithFrame:self.view.frame];
  self.webView.scalesPageToFit = YES;
  [self.view addSubview:self.webView];

  [self injectObjectsToJavascriptEnvironment];

  NSString* htmlPath = [[NSBundle mainBundle] pathForResource:@"skeleton" ofType:@"html"];
  NSURLRequest* request = [NSURLRequest requestWithURL:[NSURL fileURLWithPath:htmlPath]];
  [self.webView loadRequest:request];
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
  // Dispose of any resources that can be recreated.
}

- (void)injectObjectsToJavascriptEnvironment {
  //TODO: email, experimentLoader
  
  //additions
  [self.webView addJavascriptInterfaces:[self.experiment feedback].text WithName:@"additions"];
  
  //db/eventLoader(deprecated)
  JavascriptEventLoader* eventLoader = [JavascriptEventLoader loaderForExperiment:self.experiment];
  [self.webView addJavascriptInterfaces:eventLoader WithName:@"eventLoader"];
  [self.webView addJavascriptInterfaces:eventLoader WithName:@"db"];
  
  //env
  NSMutableDictionary* dict = [NSMutableDictionary dictionary];
  dict[@"title"] = self.experiment.definition.title;
//  dict[@"experiment"] = [self.experiment jsonStringForJavascript]; //TODO
  dict[@"lastResponse"] = [eventLoader jsonStringForLastEvent];
  dict[@"test"] = @"false";
  self.env = [Environment environmentWithDictionary:dict];
  [self.webView addJavascriptInterfaces:self.env WithName:@"env"];
}



@end
