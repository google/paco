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

#import "PacoFeedbackWebViewController.h"
#import "PacoExperiment.h"
#import "PacoExperimentFeedback.h"
#import "EasyJSWebView.h"
#import "JavascriptEventLoader.h"
#import "PacoExperimentDefinition.h"
#import "Environment.h"
#import "PAExperimentGroup+PacoGroupHelper.h" 
#import "NSObject+J2objcKVO.h"
#import "ExperimentDAO.h" 



@interface JavascriptExperimentLoader : NSObject
@property(nonatomic, strong) PAExperimentGroup* group;
@end

@implementation JavascriptExperimentLoader

- (id)initWithExperiment:(PAExperimentGroup*)group {
  self = [super init];
  if (self) {
      
   self.group   = group;
  }
  return self;
}

+ (instancetype)loaderWithExperiment:(PAExperimentGroup*) group {
  return [[[self class] alloc] initWithExperiment:group];
}

- (NSString*) getJavascript  {
    
  return [self.group jsonStringForJavascript];
}
@end


@interface PacoFeedbackWebViewController ()
@property(nonatomic, strong) EasyJSWebView* webView;
@property(nonatomic, strong) PAExperimentGroup* group;
@property(nonatomic, strong)  PAExperimentDAO* experiment;
@property(nonatomic, strong) Environment* env;
@property(nonatomic, copy) NSString* htmlName;
@property(nonatomic, copy) PacoFeedbackWebViewDismissBlock dismissBlock;
@end


@implementation PacoFeedbackWebViewController
+ (id)controllerWithExperimentGroup:(PAExperimentGroup*) group
                              withExperiment:(PAExperimentDAO*) experiment
                              htmlName:(NSString*)htmlName
                              dismissBlock:(PacoFeedbackWebViewDismissBlock)dismissBlock {
    
  PacoFeedbackWebViewController* controller =
      [[PacoFeedbackWebViewController alloc] initWithNibName:nil bundle:nil];
       controller.group  = group;
       controller.experiment = experiment;
       controller.htmlName = htmlName;
       controller.dismissBlock = dismissBlock;
  return controller;
    
}

- (void)viewDidLoad {
  [super viewDidLoad];

  UIBarButtonItem *backButton = [[UIBarButtonItem alloc] initWithTitle:@"Done"
                                                                 style:UIBarButtonItemStyleDone
                                                                target:self
                                                                action:@selector(goBack:)];
  self.navigationItem.leftBarButtonItem = backButton;

  self.webView = [[EasyJSWebView alloc] initWithFrame:self.view.frame];
  self.webView.scalesPageToFit = YES;
  [self.view addSubview:self.webView];

  [self injectObjectsToJavascriptEnvironment];

  NSString* htmlPath = [[NSBundle mainBundle] pathForResource:self.htmlName ofType:@"html"];
  NSURLRequest* request = [NSURLRequest requestWithURL:[NSURL fileURLWithPath:htmlPath]];
  [self.webView loadRequest:request];
}

- (void)goBack:(id)sender {
  if (self.dismissBlock) {
    self.dismissBlock();
  }
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
  // Dispose of any resources that can be recreated.
}

- (void)injectObjectsToJavascriptEnvironment
{
    
  //db/eventLoader(deprecated)
    JavascriptEventLoader* eventLoader = [JavascriptEventLoader loaderForExperiment:self.experiment group:_group];
  [self.webView addJavascriptInterfaces:eventLoader WithName:@"eventLoader"];
  [self.webView addJavascriptInterfaces:eventLoader WithName:@"db"];

  JavascriptExperimentLoader *experimentLoader = [JavascriptExperimentLoader loaderWithExperiment:self.group];
  [self.webView addJavascriptInterfaces:experimentLoader WithName:@"experimentLoader"];

  //env
  NSMutableDictionary* dict = [NSMutableDictionary dictionary];
  dict[@"title"] = [self.experiment valueForKeyEx:@"title"];
  dict[@"experiment"] = [self.group jsonStringForJavascript];
  dict[@"lastResponse"] = [eventLoader jsonStringForLastEvent];
  dict[@"test"] = @"false";
    
   //insert feedback string into env, since addJavascriptInterfaces will add it as an object,
   //instead of string
   dict[@"additions"] = [self.group jsonStringForJavascript];
   self.env = [Environment environmentWithDictionary:dict];
  [self.webView addJavascriptInterfaces:self.env WithName:@"env"];
}



@end
