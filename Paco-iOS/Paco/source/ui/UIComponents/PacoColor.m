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

#import "PacoColor.h"

@implementation PacoColor

+ (UIColor *)pacoBlue {
  return [UIColor colorWithRed:(4.0/255.0) green:(114.0/255.0) blue:(219.0/255.0) alpha:1.0];
}

+ (UIColor *)pacoBackgroundWhite {
  static UIColor* color = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    color = [UIColor whiteColor];
  });
  return color;
}

+ (UIColor *)pacoDarkBlue {
  return [UIColor colorWithRed:(2.0/255.0) green:(48.0/255.0) blue:(93.0/255.0) alpha:1.0];
}

+ (UIColor *)pacoBackgroundBlue {
  static UIColor* color = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    color = [UIColor colorWithRed:(144.0/255.0) green:(182.0/255.0) blue:(219.0/255.0) alpha:.6];
  });
  return color;
}

@end
