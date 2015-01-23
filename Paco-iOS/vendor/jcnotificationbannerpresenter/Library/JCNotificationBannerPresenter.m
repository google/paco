#import "JCNotificationBannerPresenter.h"
#import "JCNotificationBannerPresenter_Private.h"
#import "JCNotificationBannerPresenterIOSStyle.h"
#import "JCNotificationBannerViewIOSStyle.h"
#import "JCNotificationBannerView.h"
#import "JCNotificationBannerWindow.h"
#import "JCNotificationBannerViewController.h"
#import <QuartzCore/QuartzCore.h>

@implementation JCNotificationBannerPresenter

// JCNotificationCenter calls this whenever a presenter
// is about to be used to present one or more notifications.
// It is guaranteed to be called exactly once before presentNotification:finished:.
- (void) willBeginPresentingNotifications {
  bannerWindow = [self newWindow];
}

// JCNotificationCenter calls this whenever it has finished
// using a presenter to present notifications.
// It is guaranteed to be called exactly once after
// one or more calls to presentNotification:finished:.
- (void) didFinishPresentingNotifications {
  bannerWindow.hidden = YES;
  [bannerWindow removeFromSuperview];
  bannerWindow.rootViewController = nil;
  bannerWindow = nil;
}

// Override this method in your subclass if your notification
// style uses a window.
- (void) presentNotification:(JCNotificationBanner*)notification
                    inWindow:(JCNotificationBannerWindow*)window
                    finished:(JCNotificationBannerPresenterFinishedBlock)finished {
  // Abstract. Override this and call finished() whenever you are
  // done showing the notification.
}

// JCNotificationCenter calls this each time a notification should be presented.
// You can take as long as you like, but make sure you call finished() whenever
// you are ready to display the next notification, if any.
//
// If you do not require a window, override -willBeginPresentingNotifications,
// -didFinishPresentingNotifications, and do whatever windowless thing you like.
- (void) presentNotification:(JCNotificationBanner*)notification
                    finished:(JCNotificationBannerPresenterFinishedBlock)finished {
  [self presentNotification:notification
                   inWindow:bannerWindow
                   finished:finished];

}

#pragma mark - View helpers

- (JCNotificationBannerWindow*) newWindow {
  JCNotificationBannerWindow* window = [[JCNotificationBannerWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  window.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
  window.userInteractionEnabled = YES;
  window.autoresizesSubviews = YES;
  window.opaque = NO;
  window.hidden = NO;
  return window;
}

- (UIView*) newContainerViewForNotification:(JCNotificationBanner*)notification {
  UIView* container = [UIView new];
  container.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
  container.userInteractionEnabled = YES;
  container.opaque = NO;
  return container;
}

- (JCNotificationBannerView*) newBannerViewForNotification:(JCNotificationBanner*)notification {
  JCNotificationBannerView* view = [[JCNotificationBannerView alloc]
                                    initWithNotification:notification];
  view.userInteractionEnabled = YES;
  view.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin
                        | UIViewAutoresizingFlexibleLeftMargin
                        | UIViewAutoresizingFlexibleRightMargin;
  return view;
}

@end
