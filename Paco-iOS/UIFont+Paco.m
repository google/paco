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

#import "UIFont+Paco.h"

@implementation UIFont (Paco)

+ (UIFont *)pacoTableCellFont {
  static UIFont* font = nil;
  
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    font = [UIFont fontWithName:@"HelveticaNeue" size:18];
  });
  return font;
}

+ (UIFont *)pacoTableCellDetailFont {
  static UIFont* font = nil;
  
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    font = [UIFont fontWithName:@"HelveticaNeue" size:12];
  });
  return font;
}

+ (UIFont *)pacoMenuButtonFont {
  static UIFont* font = nil;
  
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    font = [UIFont fontWithName:@"HelveticaNeue" size:12];
  });
  return font;
}

+ (UIFont *)pacoNavbarTitleFont {
  static UIFont* font = nil;
  
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    font = [UIFont fontWithName:@"HelveticaNeue-Medium" size:23];
  });
  return font;
}

+ (UIFont *)pacoNormalButtonFont {
  static UIFont* font = nil;
  
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:15];
  });
  return font;
}

+ (UIFont *)pacoBoldFont {
  static UIFont* font = nil;

  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    font = [UIFont fontWithName:@"HelveticaNeue-Bold" size:12];
  });
  return font;
}

@end
