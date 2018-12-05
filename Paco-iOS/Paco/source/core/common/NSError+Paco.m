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

#import "NSError+Paco.h"

@implementation NSError (Paco)

- (BOOL)isOfflineError {
  if ([self.domain isEqualToString:NSURLErrorDomain]) {
    NSError* underlyingError = (self.userInfo)[NSUnderlyingErrorKey];
    if ([underlyingError.domain isEqualToString:(NSString*)kCFErrorDomainCFNetwork] &&
        underlyingError.code == NSURLErrorNotConnectedToInternet) {
      return YES;
    }
  }
  return NO;
}


//error of "No such file or directory"
- (BOOL)pacoIsFileNotExistError {
  NSError* underlyingError = (self.userInfo)[NSUnderlyingErrorKey];
  if ([underlyingError.domain isEqualToString:NSPOSIXErrorDomain]
      && underlyingError.code == ENOENT) {
    return YES;
  } else {
    return NO;
  }
}

@end
