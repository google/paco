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

//the maximum photo size to upload is 1MB = 1024KB = 1024 * 1024Bytes
static CGFloat kPacoMaxBytesOfImageSize = 1024. * 1024.;


@implementation UIImage (Paco)

#pragma mark Class Methods
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

+ (NSString*)pacoSaveImageToDocumentDir:(UIImage*)image
                          forDefinition:(NSString*)definitionId
                                inputId:(NSString*)inputId {
  NSAssert(definitionId && inputId, @"definitionId and inputId should be valid");
  if (image == nil) {
    return nil;
  }
  NSString* imageName = [self imageNameForExperiment:definitionId inputId:inputId];
  NSString* imagePath = [NSString pacoImageFolderInDocumentsDirectory:imageName];
  UIImage* scaledImage = [image pacoScaleToScreenSize];
  NSData* imageData = [scaledImage pacoImageDataWithMaxSize:kPacoMaxBytesOfImageSize];
  if (!imageData) {
    return nil;
  }
  BOOL success = [imageData writeToFile:imagePath atomically:NO];
  return success ? imageName : nil;
}

+ (NSString*)pacoBase64StringWithImageName:(NSString*)imageName {
  if (0 == [imageName length]) {
    return nil;
  }
  NSString* imagePath = [NSString pacoImageFolderInDocumentsDirectory:imageName];
  UIImage* image = [UIImage imageWithContentsOfFile:imagePath];
  if (image == nil) {
    return nil;
  }
  NSString* imageString = [image pacoBase64String];
  return imageString;
}

+ (UIImage *)scaleImage:(UIImage *)image toSize:(CGSize)size {
  CGFloat horizontalRatio = size.width / image.size.width;
  CGFloat verticalRatio = size.height / image.size.height;
  CGFloat ratio = MIN(horizontalRatio, verticalRatio);
  return [image pacoScaledImageByRatio:ratio];
}


#pragma mark Instance Methods
- (UIImage*)pacoScaledImageByRatio:(CGFloat)scaleRatio {
  NSAssert(scaleRatio > 0, @"scaleRatio should be larger than 0");
  
  CGSize newSize = CGSizeMake(floorf(self.size.width * scaleRatio),
                              floorf(self.size.height * scaleRatio));
  UIGraphicsBeginImageContext(newSize);
  [self drawInRect:CGRectMake(0, 0, newSize.width, newSize.height)];
  UIImage* scaledImage = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();
  return scaledImage;
}

//scale the image so that the shorter side of the image will be equal to or less than
//the shorter side of the screen size (320 for all iPhone devices so far)
- (UIImage*)pacoScaleToScreenSize {
  CGRect screenBounds = [[UIScreen mainScreen] bounds];
  CGFloat screenShorterSideLength = MIN(screenBounds.size.width, screenBounds.size.height);
  CGFloat imageShorterSideLength = MIN(self.size.width, self.size.height);
  if (imageShorterSideLength <= screenShorterSideLength) {
    return self;
  }
  CGFloat ratio = screenShorterSideLength / imageShorterSideLength;
  return [self pacoScaledImageByRatio:ratio];
}


- (NSData*)pacoImageDataWithMaxSize:(CGFloat)maxBytes {
  NSData* imageData = UIImageJPEGRepresentation(self, 1.0);
  if ([imageData length] < maxBytes) {
    return imageData;
  }
  CGFloat compressionQuality = 0.5;
  CGFloat MIN_COMPRESSION_QUALITY = 0.005;
  while (compressionQuality >= MIN_COMPRESSION_QUALITY) {
    imageData = UIImageJPEGRepresentation(self, compressionQuality);
    if ([imageData length] < maxBytes) {
      break;
    }
    compressionQuality /= 2.0;
  }
  if ([imageData length] < maxBytes) {
    return imageData;
  } else {
    return nil;
  }
}

- (NSString*)pacoBase64String {
  NSData *imageData = [self pacoImageDataWithMaxSize:kPacoMaxBytesOfImageSize];
  NSString* imageStr = [imageData base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
  if (0 == [imageStr length]) {
    imageStr = nil;
  }
  return imageStr;
}



@end
