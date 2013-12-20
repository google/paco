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

#import "PacoLoginScreenViewController.h"
#import <QuartzCore/QuartzCore.h>

#import "PacoAppDelegate.h"
#import "PacoClient.h"
#import "PacoColor.h"
#import "PacoLayout.h"
#import "PacoMainViewController.h"
#import "PacoTitleView.h"
#import "PacoLoadingView.h"
#import "PacoFont.h"

@interface PacoClient ()
- (void)loginWithClientLogin:(NSString *)email
                    password:(NSString *)password
           completionHandler:(void (^)(NSError *))completionHandler;
@end


@interface PacoLoginScreenViewController () <UITextFieldDelegate>

@property(strong, nonatomic) UITextField* emailField;
@property(strong, nonatomic) UITextField* pwdField;
@property(copy, nonatomic) LoginCompletionBlock completionBlock;

@end

@implementation PacoLoginScreenViewController

+ (id)controllerWithCompletionBlock:(LoginCompletionBlock)block
{
  return [[PacoLoginScreenViewController alloc] initWithNibName:nil bundle:nil completionBlock:block];
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil completionBlock:(LoginCompletionBlock)block
{
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    PacoTitleView *title = [PacoTitleView viewWithDefaultIconAndText:@"Login"];
    self.navigationItem.titleView = title;
    self.completionBlock = block;
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];
	// Do any additional setup after loading the view.
  self.view.backgroundColor = [UIColor whiteColor];

  UIButton *login = [UIButton buttonWithType:UIButtonTypeRoundedRect];
  [login setTitle:@"Login" forState:UIControlStateNormal];
  [login setTitleColor:[PacoColor pacoBlue] forState:UIControlStateNormal];
  [login.titleLabel setFont:[UIFont fontWithName:@"Helvetica Bold" size:18]];
  [login addTarget:self action:@selector(onLogin) forControlEvents:UIControlEventTouchUpInside];
  login.frame = CGRectMake(0, 0, 100, 40);
  [self.view addSubview:login];

  UITextField *textField = [[UITextField alloc] initWithFrame:CGRectZero];
  textField.textColor = [PacoColor pacoBlue];
  textField.text = @"";
  textField.placeholder = @"<email>";
  textField.keyboardType = UIKeyboardTypeEmailAddress;
  textField.autocorrectionType = UITextAutocorrectionTypeNo;
  textField.autocapitalizationType = UITextAutocapitalizationTypeNone;
  textField.backgroundColor = [PacoColor pacoBackgroundBlue];
  textField.delegate = self;
  [self.view addSubview:textField];
  [textField sizeToFit];
  CGRect frame = textField.frame;
  frame.size.width = 200;
  frame.size.height = 36;
  textField.font = [PacoFont pacoTableCellFont];
  textField.frame = frame;
  textField.clipsToBounds = YES;
  textField.layer.cornerRadius = 5.;

  self.emailField = textField;
  

  UITextField *textField2 = [[UITextField alloc] initWithFrame:CGRectZero];
  textField2.textColor = [PacoColor pacoBlue];
  textField2.text = @"";
  textField2.placeholder = @"<password>";
  textField2.keyboardType = UIKeyboardTypeDefault;
  textField.autocorrectionType = UITextAutocorrectionTypeNo;
  textField.autocapitalizationType = UITextAutocapitalizationTypeNone;
  textField2.secureTextEntry = YES;
  textField2.backgroundColor = [PacoColor pacoBackgroundBlue];
  textField2.delegate = self;
  [self.view addSubview:textField2];
  [textField2 sizeToFit];
  frame = textField2.frame;
  frame.size.width = 200;
  frame.size.height = 36;
  textField2.frame = frame;
  textField2.font = [PacoFont pacoTableCellFont];
  textField2.clipsToBounds = YES;
  textField2.layer.cornerRadius = 5.;
  self.pwdField = textField2;
  
  UILabel* label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 300, 100)];
  label.numberOfLines = 3;
  label.center = self.view.center;
  label.backgroundColor = [UIColor clearColor];
  label.textColor = [PacoColor pacoBlue];
  [label setText:@"Hi, Log in with a Google account\n\n  Run Your Paco Experiment Today!"];
  label.textAlignment = NSTextAlignmentCenter;
  [self.view addSubview:label];

  CGRect layoutRect = self.view.bounds;
  layoutRect.origin.y += 40;
  layoutRect.size.height = 200;

  layoutRect = CGRectInset(layoutRect, 20, 5);
  NSArray *elements = [NSArray arrayWithObjects:textField, textField2, login, nil];
  [PacoLayout layoutViews:elements inGridWithWidth:1 gridHeight:4 inRect:layoutRect];  
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
  // Dispose of any resources that can be recreated.
}

- (void)onLogin {
  NSString* emailStr = [self.emailField.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
  NSString* pwdStr = [self.pwdField.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
  if (emailStr.length == 0 || pwdStr.length == 0) {
    [[[UIAlertView alloc] initWithTitle:@"Oops"
                                message:@"Please input valid email and password."
                               delegate:nil
                      cancelButtonTitle:@"OK"
                      otherButtonTitles:nil] show];
  }else{
    if ([self.emailField isFirstResponder]) {
      [self.emailField resignFirstResponder];
    }
    if ([self.pwdField isFirstResponder]) {
      [self.pwdField resignFirstResponder];
    }
    [self loginWithEmail:emailStr password:pwdStr];
  }
}

- (void)loginWithEmail:(NSString*)email password:(NSString*)password {
  [[PacoLoadingView sharedInstance] showLoadingScreen];
  [[PacoClient sharedInstance] loginWithClientLogin:email password:password completionHandler:^(NSError *error) {
    [[PacoLoadingView sharedInstance] dismissLoadingScreen];
    if (!error) {
      NSLog(@"PACO LOGIN SUCCESS!");
      [((PacoAppDelegate *)[UIApplication sharedApplication].delegate).viewController dismissViewControllerAnimated:YES completion:^{
        if (self.completionBlock) {
          self.completionBlock(nil);
        }
      }];
    } else {
      NSLog(@"PACO LOGIN FAILURE! %@", error);
      if (self.completionBlock) {
        self.completionBlock(error);
      }
    }
  }];
}

- (void)onLogout {

}

#pragma mark - UITextFieldDelegate
// called when 'return' key pressed. return NO to ignore.
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
  [self onLogin];
  return YES;
}

@end
