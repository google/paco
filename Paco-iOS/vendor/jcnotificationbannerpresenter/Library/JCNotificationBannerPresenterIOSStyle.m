#import "JCNotificationBannerPresenterIOSStyle.h"
#import "JCNotificationBannerPresenter_Private.h"
#import "JCNotificationBannerWindow.h"
#import "JCNotificationBannerViewIOSStyle.h"
#import "JCNotificationBannerView.h"
#import "JCNotificationBannerViewController.h"

@implementation JCNotificationBannerPresenterIOSStyle

- (void) presentNotification:(JCNotificationBanner *)notification
                    inWindow:(JCNotificationBannerWindow*)window
                    finished:(JCNotificationBannerPresenterFinishedBlock)finished {
  JCNotificationBannerView* banner = [self newBannerViewForNotification:notification];

  JCNotificationBannerViewController* bannerViewController = [JCNotificationBannerViewController new];
  window.rootViewController = bannerViewController;
  UIView* originalControllerView = bannerViewController.view;

  UIView* containerView = [self newContainerViewForNotification:notification];
  [containerView addSubview:banner];
  bannerViewController.view = containerView;

  window.bannerView = banner;

  containerView.bounds = originalControllerView.bounds;
  containerView.transform = originalControllerView.transform;
  [banner getCurrentPresentingStateAndAtomicallySetPresentingState:YES];

  CGSize statusBarSize = [[UIApplication sharedApplication] statusBarFrame].size;
  CGFloat width = 320.0;
  CGFloat height = 60.0;
  CGFloat x = (MAX(statusBarSize.width, statusBarSize.height) - width) / 2.0;
  CGFloat y = -60.0;
  banner.frame = CGRectMake(x, y, width, height);

  JCNotificationBannerTapHandlingBlock originalTapHandler = notification.tapHandler;
  JCNotificationBannerTapHandlingBlock wrappingTapHandler = ^{
    if ([banner getCurrentPresentingStateAndAtomicallySetPresentingState:NO]) {
      if (originalTapHandler) {
        originalTapHandler();
      }

      [banner removeFromSuperview];
      finished();
    }
    // Break the retain cycle
    notification.tapHandler = nil;
  };
  notification.tapHandler = wrappingTapHandler;

  double startOpacity = 1.0;
  double endOpacity = 1.0;
  double animationDuration = 0.5;

  CGRect bannerFrameAfterTransition = banner.frame;
  bannerFrameAfterTransition.origin.y = MIN(statusBarSize.width, statusBarSize.height);
  UIImage* image = [self captureWindowPartWithRect:bannerFrameAfterTransition];

  // Prepare view transform
  CALayer* layer = banner.layer;
  banner.alpha = startOpacity;
  banner.frame = CGRectOffset(banner.frame, 0.0, banner.frame.size.height);
  banner.alpha = endOpacity;
  layer.anchorPointZ = 0.5f * banner.frame.size.height;
  [self rotateLayer:layer fromAngle:-90.0 toAngle:0.0 duration:animationDuration onCompleted:^(){}];

  // Add image of background to layer.
  CALayer* imageLayer = [CALayer layer];
  imageLayer.frame = banner.frame;
  imageLayer.anchorPointZ = 0.5f * banner.frame.size.height;
  imageLayer.contents = (id)image.CGImage;
  imageLayer.shadowOffset = CGSizeMake(0.0, 1.0);
  imageLayer.shadowColor = [UIColor darkGrayColor].CGColor;
  imageLayer.shadowRadius = 3.0;
  imageLayer.shadowOpacity = 0.8;
  [self rotateLayer:imageLayer fromAngle:0.0 toAngle:90.0 duration:animationDuration onCompleted:^(){} ];
  [containerView.layer addSublayer:imageLayer];

  // On timeout, slide it up while fading it out.
  if (notification.timeout > 0.0) {
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, notification.timeout * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
      // Add image of background to layer.
      CALayer* imageLayer = [CALayer layer];
      imageLayer.frame =  banner.frame;
      imageLayer.anchorPointZ = 0.5f * banner.frame.size.height;
      imageLayer.contents = (id)image.CGImage;
      imageLayer.shadowOffset = CGSizeMake(0, 1);
      imageLayer.shadowColor = [UIColor darkGrayColor].CGColor;
      imageLayer.shadowRadius = 3.0;
      imageLayer.shadowOpacity = 0.8;
      [self rotateLayer:imageLayer fromAngle:-90.0 toAngle:0.0 duration:animationDuration onCompleted:^(){} ];
      [[containerView layer] addSublayer:imageLayer];
      
      CALayer* layer = [banner layer];
      [self rotateLayer:layer fromAngle:0.0 toAngle:90.0 duration:animationDuration onCompleted:^(){
        if ([banner getCurrentPresentingStateAndAtomicallySetPresentingState:NO]) {
          [banner removeFromSuperview];
          [containerView removeFromSuperview];
          finished();
        }
        // Break the retain cycle
        notification.tapHandler = nil;
      }];
    });
  }
}

#pragma mark - View helpers

- (UIView*) newContainerViewForNotification:(JCNotificationBanner*)notification {
  UIView* view = [super newContainerViewForNotification:notification];
  view.autoresizesSubviews = YES;
  return view;
}

- (JCNotificationBannerView*) newBannerViewForNotification:(JCNotificationBanner*)notification {
  JCNotificationBannerView* view = [[JCNotificationBannerViewIOSStyle alloc]
                                    initWithNotification:notification];
  view.userInteractionEnabled = YES;
  view.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin
  | UIViewAutoresizingFlexibleLeftMargin
  | UIViewAutoresizingFlexibleRightMargin;
  return view;
}

#pragma mark - Screenshot

/**
 * @returns part of the keyWindow screenshot rotated by 180 degrees.
 */
- (UIImage*) captureWindowPartWithRect:(CGRect)rect {
  UIWindow* keyWindow = [[UIApplication sharedApplication] keyWindow];

  CGRect firstCaptureRect = keyWindow.bounds;

  UIGraphicsBeginImageContextWithOptions(firstCaptureRect.size, YES, 0.0);
  CGContextRef context = UIGraphicsGetCurrentContext();
  [keyWindow.layer renderInContext:context];
  UIImage* capturedImage = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();

  CGRect contentRectToCrop = rect;
  CGFloat rotationNeeded = 0;
  CGRect originalRect = rect;
  switch ([UIApplication sharedApplication].statusBarOrientation) {
    case UIInterfaceOrientationLandscapeLeft:
      rotationNeeded = 90;
      contentRectToCrop.origin.x = originalRect.origin.y;
      contentRectToCrop.origin.y = keyWindow.bounds.size.height - originalRect.origin.x - originalRect.size.width;
      contentRectToCrop.size.width = originalRect.size.height;
      contentRectToCrop.size.height = originalRect.size.width;
      break;

    case UIInterfaceOrientationLandscapeRight:
      rotationNeeded = -90;
      contentRectToCrop.origin.x = keyWindow.bounds.size.width - originalRect.origin.y - originalRect.size.height;
      contentRectToCrop.origin.y = keyWindow.bounds.size.height - originalRect.origin.x - originalRect.size.width ;
      contentRectToCrop.size.width = originalRect.size.height;
      contentRectToCrop.size.height = originalRect.size.width;
      break;

    case UIInterfaceOrientationPortrait:
    case UIInterfaceOrientationUnknown:
      break;

    case UIInterfaceOrientationPortraitUpsideDown:
      rotationNeeded = 180;
      contentRectToCrop.origin.x = originalRect.origin.x;
      contentRectToCrop.origin.y = keyWindow.bounds.size.height - originalRect.origin.y - originalRect.size.height;
      contentRectToCrop.size.width = originalRect.size.width;
      contentRectToCrop.size.height = originalRect.size.height;
      break;
  }

  contentRectToCrop.origin.x *= capturedImage.scale;
  contentRectToCrop.origin.y *= capturedImage.scale;
  contentRectToCrop.size.width *= capturedImage.scale;
  contentRectToCrop.size.height *= capturedImage.scale;

  CGImageRef imageRef = CGImageCreateWithImageInRect([capturedImage CGImage], contentRectToCrop);
  UIImage* croppedImage = [UIImage imageWithCGImage:imageRef scale:capturedImage.scale orientation: UIImageOrientationUp];
  CGImageRelease(imageRef);

  if (rotationNeeded) {
    croppedImage = [self.class rotateImage:croppedImage byDegrees:rotationNeeded];
  }

  return croppedImage;
}

// -----------------------------------------------------------------------
// UIImage Extensions for preparing screenshots under banner by HardyMacia
// (Catamount Software).
// http://www.catamount.com/forums/viewtopic.php?f=21&t=967

CGFloat DegreesToRadians(CGFloat degrees) { return degrees * M_PI / 180.0; };
CGFloat RadiansToDegrees(CGFloat radians) { return radians * 180.0 / M_PI; };

+ (UIImage*) rotateImage:(UIImage*)image byDegrees:(CGFloat)degrees {
  // Calculate the size of the rotated view's containing box for our drawing space
  UIView* rotatedViewBox = [[UIView alloc] initWithFrame:CGRectMake(0.0, 0.0, image.size.width, image.size.height)];
  CGAffineTransform transform = CGAffineTransformMakeRotation(DegreesToRadians(degrees));
  rotatedViewBox.transform = transform;
  CGSize rotatedSize = rotatedViewBox.frame.size;

  // Create the bitmap context
  UIGraphicsBeginImageContext(rotatedSize);
  CGContextRef bitmap = UIGraphicsGetCurrentContext();

  // Move the origin to the middle of the image so we will rotate and scale around the center.
  CGContextTranslateCTM(bitmap, rotatedSize.width/2, rotatedSize.height/2);

  // Rotate the image context
  CGContextRotateCTM(bitmap, DegreesToRadians(degrees));

  // Now, draw the rotated/scaled image into the context
  CGContextScaleCTM(bitmap, 1.0, -1.0);
  CGRect imageRect = CGRectMake(-image.size.width / 2.0, -image.size.height / 2.0, image.size.width, image.size.height);
  CGContextDrawImage(bitmap, imageRect, image.CGImage);

  UIImage* newImage = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();

  return newImage;
}

// -----------------------------------------------------------------------

- (void) rotateLayer:(CALayer*)imageLayer
           fromAngle:(CGFloat)fromAngle
             toAngle:(CGFloat)toAngle
            duration:(CFTimeInterval)duration
         onCompleted:(void (^)())onCompletedBlock {
  CGFloat fromInRadians = fromAngle * M_PI / 180.0f;
  CGFloat toInRadians = toAngle * M_PI / 180.0f;

  // Create animation that rotates by to the end rotation.
  CABasicAnimation* animation = [CABasicAnimation animationWithKeyPath:@"transform.rotation.x"];
  animation.delegate = self;
  animation.duration = duration;
  animation.fromValue = @(fromInRadians);
  animation.toValue = @(toInRadians);
  animation.fillMode = kCAFillModeForwards;
  animation.removedOnCompletion = NO;
  [animation setValue:[onCompletedBlock copy] forKey:@"onCompleted"];
  [imageLayer addAnimation:animation forKey:@"transform.rotation.x"];
}

- (void) animationDidStop:(CAAnimation*)animation finished:(BOOL)finished {
  if (finished) {
    void(^onCompletedBlock)() = [animation valueForKey:@"onCompleted"];
    if (onCompletedBlock)
      onCompletedBlock();
  }
  [animation setValue:nil forKey:@"onCompleted"];
}

@end
