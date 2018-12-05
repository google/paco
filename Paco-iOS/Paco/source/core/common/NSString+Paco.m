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

#import "NSString+Paco.h"
#import "PacoClient.h"

@implementation NSString (Paco)

+ (NSString*)pacoDocumentDirectory {
  static NSString* documentDirectory = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    documentDirectory = paths[0];
  });
  return documentDirectory;
}

+ (NSString*)pacoLogDirectory {
  static NSString* logPath = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    NSArray* pathsArray = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    logPath = [[pathsArray firstObject] stringByAppendingPathComponent:@"Logs"];
  });
  return logPath;
}

+ (NSString*)pacoDocumentDirectoryFilePathWithName:(NSString*)fileName {
  NSString* fileFullPath = [[NSString pacoDocumentDirectory] stringByAppendingPathComponent:fileName];
  return fileFullPath;
}

+ (NSString*)pacoImageFolderInDocumentsDirectory:(NSString*)fileName {
  NSString* folderPath = [[NSString pacoDocumentDirectory] stringByAppendingPathComponent:@"img"];
  NSError* error;
  if (![[NSFileManager defaultManager] fileExistsAtPath:folderPath]) {
    [[NSFileManager defaultManager] createDirectoryAtPath:folderPath withIntermediateDirectories:NO attributes:nil error:&error];
    if (error) {
      DDLogError(@"Failed to create image dir: %@", [error description]);
    } else {
      DDLogInfo(@"Succeeded to create image dir.");
    }
  }
  NSString* fullFilePath = [folderPath stringByAppendingPathComponent:fileName];
  return fullFilePath;
}
@end
