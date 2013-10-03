#import "JCNotificationBannerPresenterSmokeStyle.h"
#import "JCNotificationBannerPresenter_Private.h"
#import "JCNotificationBannerView.h"
#import "JCNotificationBannerViewController.h"
#define IS_IOS7 [[UIDevice currentDevice].systemVersion hasPrefix:@"7"]

@implementation JCNotificationBannerPresenterSmokeStyle

- (id) init {
  if (self = [super init]) {
    self.minimumHorizontalMargin = 10.0;
    self.bannerMaxWidth = 350.0;
    self.bannerHeight = 60.0;
  }
  return self;
}

- (void) presentNotification:(JCNotificationBanner*)notification
                    inWindow:(JCNotificationBannerWindow*)window
                    finished:(JCNotificationBannerPresenterFinishedBlock)finished {
  JCNotificationBannerView* banner = [self newBannerViewForNotification:notification];

  JCNotificationBannerViewController* bannerViewController = [JCNotificationBannerViewController new];
  window.rootViewController = bannerViewController;

  UIView* containerView = [self newContainerViewForNotification:notification];

  window.bannerView = banner;

  [containerView addSubview:banner];
  bannerViewController.view = containerView;

  UIView* view = ((UIView*)[[[[UIApplication sharedApplication] keyWindow] subviews] objectAtIndex:0]);
  containerView.bounds = view.bounds;
  containerView.transform = view.transform;
  [banner getCurrentPresentingStateAndAtomicallySetPresentingState:YES];

  CGSize statusBarSize = [[UIApplication sharedApplication] statusBarFrame].size;
  // Make the banner fill the width of the screen, minus any requested margins,
  // up to self.bannerMaxWidth.
  CGSize bannerSize = CGSizeMake(MIN(self.bannerMaxWidth, view.bounds.size.width - self.minimumHorizontalMargin * 2.0), self.bannerHeight);
  // Center the banner horizontally.
  CGFloat x = (MAX(statusBarSize.width, statusBarSize.height) / 2) - (bannerSize.width / 2);
  // Position the banner offscreen vertically.
  CGFloat y = -self.bannerHeight;
  if (!IS_IOS7) {
    y -= (MIN(statusBarSize.width, statusBarSize.height));
  }
  banner.frame = CGRectMake(x, y, bannerSize.width, bannerSize.height);

  JCNotificationBannerTapHandlingBlock originalTapHandler = banner.notificationBanner.tapHandler;
  JCNotificationBannerTapHandlingBlock wrappingTapHandler = ^{
    if ([banner getCurrentPresentingStateAndAtomicallySetPresentingState:NO]) {
      if (originalTapHandler) {
        originalTapHandler();
      }

      [banner removeFromSuperview];
      finished();
      // Break the retain cycle
      notification.tapHandler = nil;
    }
  };
  banner.notificationBanner.tapHandler = wrappingTapHandler;

  // Slide it down while fading it in.
  banner.alpha = 0;
  [UIView animateWithDuration:0.5 delay:0
                      options:UIViewAnimationOptionAllowUserInteraction | UIViewAnimationOptionCurveEaseOut
                   animations:^{
                     CGRect newFrame = CGRectOffset(banner.frame, 0, banner.frame.size.height);
                     banner.frame = newFrame;
                     banner.alpha = 0.9;
                   } completion:^(BOOL finished) {
                     // Empty.
                   }];


  // On timeout, slide it up while fading it out.
  if (notification.timeout > 0.0) {
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, notification.timeout * NSEC_PER_SEC);
    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
      [UIView animateWithDuration:0.5 delay:0 options:UIViewAnimationOptionCurveEaseIn
                       animations:^{
                         banner.frame = CGRectOffset(banner.frame, 0, -banner.frame.size.height);
                         banner.alpha = 0;
                       } completion:^(BOOL didFinish) {
                         if ([banner getCurrentPresentingStateAndAtomicallySetPresentingState:NO]) {
                           [banner removeFromSuperview];
                           finished();
                           // Break the retain cycle
                           notification.tapHandler = nil;
                         }
                       }];
    });
  }
}

#pragma mark - View helpers

- (JCNotificationBannerWindow*) newWindow {
  JCNotificationBannerWindow* window = [super newWindow];
  window.windowLevel = UIWindowLevelStatusBar;
  return window;
}

@end
