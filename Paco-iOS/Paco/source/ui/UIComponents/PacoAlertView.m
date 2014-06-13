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


#import "PacoAlertView.h"

@interface PacoAlertView()

@property(nonatomic, copy) PacoAlertViewDidDismissBlock dismissBlock;

@end



@implementation PacoAlertView

//designated initializer
- (id)initWithTitle:(NSString*)title
            message:(NSString*)message
       dismissBlock:(PacoAlertViewDidDismissBlock)dismissBlock
  cancelButtonTitle:(NSString*)cancelButtonTitle {
  self = [super initWithTitle:title
                      message:message
                     delegate:self
            cancelButtonTitle:cancelButtonTitle
            otherButtonTitles:nil];
  if (self) {
    _dismissBlock = [dismissBlock copy];
  }
  return self;
}

+ (void)showAlertWithTitle:(NSString*)title
                   message:(NSString*)message
              dismissBlock:(PacoAlertViewDidDismissBlock)dismissBlock
         cancelButtonTitle:(NSString*)cancelButtonTitle
         otherButtonTitles:(NSString*)otherButtonTitles, ... NS_REQUIRES_NIL_TERMINATION {
    PacoAlertView* alertView = [[PacoAlertView alloc] initWithTitle:title
                                                            message:message
                                                       dismissBlock:dismissBlock
                                                  cancelButtonTitle:cancelButtonTitle];

    va_list args;
    va_start(args, otherButtonTitles);
    for (NSString* buttonTitle = otherButtonTitles;
         buttonTitle != nil;
         buttonTitle = va_arg(args, NSString*)) {
      [alertView addButtonWithTitle:buttonTitle];
    }
    va_end(args);
    
    dispatch_async(dispatch_get_main_queue(), ^{
      [alertView show];
    });
}

+ (void)showAlertWithTitle:(NSString *)title
                   message:(NSString *)message
         cancelButtonTitle:(NSString *)cancelButtonTitle {
  dispatch_async(dispatch_get_main_queue(), ^{
    [[[UIAlertView alloc] initWithTitle:title
                                message:message
                               delegate:nil
                      cancelButtonTitle:cancelButtonTitle
                      otherButtonTitles:nil] show];
  });
}

+ (void)showGeneralErrorAlert {
  dispatch_async(dispatch_get_main_queue(), ^{
    [[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Sorry", nil)
                                message:NSLocalizedString(@"Something went wrong, please try again later.", nil)
                               delegate:nil
                      cancelButtonTitle:@"OK"
                      otherButtonTitles:nil] show];
  });
}

+ (void)showRefreshErrorAlert {
  dispatch_async(dispatch_get_main_queue(), ^{
    [[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Oops", nil)
                                message:NSLocalizedString(@"Failed to refresh, please try again later.", nil)
                               delegate:nil
                      cancelButtonTitle:@"OK"
                      otherButtonTitles:nil] show];
  });
}

+ (void)showAlertWithError:(NSError*)error {
  dispatch_async(dispatch_get_main_queue(), ^{
    if (![error localizedDescription]) {
      [self showGeneralErrorAlert];
    } else {
      [[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Oops", nil)
                                  message:[error localizedDescription]
                                 delegate:nil
                        cancelButtonTitle:@"OK"
                        otherButtonTitles:nil] show];
    }
  });
}

#pragma mark UIAlertViewDelegate implementation
- (void)alertView:(UIAlertView*)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex {
  if (self != alertView) {
    return;
  }

  if (self.dismissBlock != nil) {
    self.dismissBlock(buttonIndex);
    self.dismissBlock = nil;
  }
}



@end
