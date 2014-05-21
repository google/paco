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
- (id)initWithTitle:(NSString*)title {
  self = [super initWithNibName:nil bundle:nil];
  if (self) {
    self.title = title;
  }
  return self;
}

+ (instancetype)controllerWithTitle:(NSString*)title {
  return [[PacoWebViewController alloc] initWithTitle:title];
}

- (void)viewDidLoad {
  [super viewDidLoad];
  self.edgesForExtendedLayout = UIRectEdgeNone;
  UIWebView* webView = [[UIWebView alloc] initWithFrame:CGRectMake(0,
                                                                   0,
                                                                   self.view.frame.size.width,
                                                                   self.view.frame.size.height)];
  self.view = webView;
}

- (void)loadStaticHtmlWithName:(NSString*)htmlName {
  NSString* htmlPath = [[NSBundle mainBundle] pathForResource:htmlName ofType:@"html"];
  [self startLoadingURL:[NSURL fileURLWithPath:htmlPath]];
}

- (void)loadURL:(NSString*)urlString {
  [self startLoadingURL:[NSURL URLWithString:urlString]];
}

- (void)startLoadingURL:(NSURL*)url {
  NSURLRequest* request = [NSURLRequest requestWithURL:url];
  [(UIWebView*)self.view loadRequest:request];
}


@end
