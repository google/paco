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

#import "PacoWebViewController.h"

@implementation PacoWebViewController

+ (PacoWebViewController*)controllerWithTitle:(NSString*)title andHtml:(NSString*)htmlName {
  PacoWebViewController* controller = [[PacoWebViewController alloc] initWithNibName:nil bundle:nil];
  controller.title = title;
  [controller loadWebViewWithHTML:htmlName];
  return controller;
}

- (void)viewDidLoad {
  [super viewDidLoad];
  self.edgesForExtendedLayout = UIRectEdgeNone;
}

- (void)loadWebViewWithHTML:(NSString*)htmlName {
  NSString *htmlFile = [[NSBundle mainBundle] pathForResource:htmlName ofType:@"html"];
  UIWebView *webView = [[UIWebView alloc] initWithFrame:CGRectMake(0,
                                                                   0,
                                                                   self.view.frame.size.width,
                                                                   self.view.frame.size.height)];
  [webView loadRequest:[NSURLRequest requestWithURL:[NSURL fileURLWithPath:htmlFile]]];
  self.view = webView;
}


@end
