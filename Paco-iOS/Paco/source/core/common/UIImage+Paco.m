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


#import "UIImage+Paco.h"
#import "NSString+Paco.h"


NSString* const kPacoImageNamePrefix = @"[PacoImageName]";
static CGFloat kPacoImageCompressionQuality = .5;


@implementation UIImage (Paco)


+ (NSString*)pacoImageNameFromBoxedName:(NSString*)boxedName {
  if (![boxedName hasPrefix:kPacoImageNamePrefix]) {
    return nil;
  }
  NSString* imageName = [boxedName substringFromIndex:[kPacoImageNamePrefix length]];
  if (0 == [imageName length]) {
    imageName = nil;
  }
  return imageName;
}


+ (NSString*)pacoBoxedNameFromImageName:(NSString*)imageName {
  if (imageName == nil) {
    return nil;
  }
  return [NSString stringWithFormat:@"%@%@",kPacoImageNamePrefix, imageName];
}


+ (NSString*)imageNameForExperiment:(NSString*)experimentId
                            inputId:(NSString*)inputId {
  NSAssert([experimentId length] > 0 &&
           [inputId length] > 0, @"experimentId and inputId should both be valid");
  NSTimeInterval timeStamp = [[NSDate date] timeIntervalSince1970];
  NSString* name = [NSString stringWithFormat:@"%@_%@_%f.jpg", experimentId, inputId, timeStamp];
  return name;
}


- (NSString*)pacoBase64String {
  NSData *imageData = UIImageJPEGRepresentation(self, kPacoImageCompressionQuality);
  NSString* imageStr = [imageData base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
  if (0 == [imageStr length]) {
    imageStr = nil;
  }
  return imageStr;
}


+ (NSString*)pacoSaveImageToDocumentDir:(UIImage*)image
                          forDefinition:(NSString*)definitionId
                                inputId:(NSString*)inputId {
  NSAssert(definitionId && inputId, @"definitionId and inputId should be valid");
  if (image == nil) {
    return nil;
  }
  NSString* imageName = [self imageNameForExperiment:definitionId inputId:inputId];
  NSString* imagePath = [NSString pacoDocumentDirectoryFilePathWithName:imageName];
  
  NSData *imageData = UIImageJPEGRepresentation(image, kPacoImageCompressionQuality);
  BOOL success = [imageData writeToFile:imagePath atomically:NO];
  return success ? imageName : nil;
}

+ (NSString*)pacoBase64StringWithImageName:(NSString*)imageName {
  if (0 == [imageName length]) {
    return nil;
  }
  NSString* imagePath = [NSString pacoDocumentDirectoryFilePathWithName:imageName];
  UIImage* image = [UIImage imageWithContentsOfFile:imagePath];
  if (image == nil) {
    return nil;
  }
  NSString* imageString = [image pacoBase64String];
  return imageString;
}

@end
