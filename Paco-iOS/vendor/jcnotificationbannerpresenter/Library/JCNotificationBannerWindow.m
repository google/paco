#import "JCNotificationBannerWindow.h"

@implementation JCNotificationBannerWindow

@synthesize bannerView;

- (id) initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    // Initialization code
  }
  return self;
}

- (UIView*) hitTest:(CGPoint)point withEvent:(UIEvent *)event {
  UIView* superHitView = [super hitTest:point withEvent:event];
  if (superHitView == bannerView) {
    return bannerView;
  } else {
    UIWindow* nextWindow;
    BOOL useNextWindow = NO;
    for (UIWindow* window in [[UIApplication sharedApplication].windows reverseObjectEnumerator]) {
      if (useNextWindow) {
        nextWindow = window;
        break;
      }

      if ([window isKindOfClass:[JCNotificationBannerWindow class]]) {
        useNextWindow = YES;
      }
    }

    if (nextWindow) {
      NSAssert(![nextWindow isKindOfClass:[JCNotificationBannerWindow class]], @"Did not expect multiple notification windows.");
      return [nextWindow hitTest:point withEvent:event];
    } else {
      return superHitView;
    }
  }
}

@end
