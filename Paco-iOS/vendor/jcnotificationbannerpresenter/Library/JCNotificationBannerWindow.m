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
    BOOL encounteredBannerWindow = NO;
    for (UIWindow* window in [[UIApplication sharedApplication].windows reverseObjectEnumerator]) {
      if (encounteredBannerWindow && ![window isKindOfClass:[JCNotificationBannerWindow class]]) {
        nextWindow = window;
        break;
      }

      if (!encounteredBannerWindow && [window isKindOfClass:[JCNotificationBannerWindow class]]) {
        encounteredBannerWindow = YES;
      }
    }

    if (nextWindow) {
      return [nextWindow hitTest:point withEvent:event];
    } else {
      return superHitView;
    }
  }
}

@end
