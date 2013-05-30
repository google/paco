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

#import "PacoFont.h"

@implementation PacoFont

+ (UIFont *)pacoTableCellFont {
  return [UIFont fontWithName:@"HelveticaNeue" size:18];
}

+ (UIFont *)pacoTableCellDetailFont {
  return [UIFont fontWithName:@"HelveticaNeue" size:12];
}

+ (UIFont *)pacoMenuButtonFont {
  return [UIFont fontWithName:@"HelveticaNeue" size:12];
}

+ (UIFont *)pacoNavbarTitleFont {
  return [UIFont fontWithName:@"Courier New" size:18];
}

@end
