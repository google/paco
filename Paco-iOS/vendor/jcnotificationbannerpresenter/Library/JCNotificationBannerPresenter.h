#import <Foundation/Foundation.h>
#import "JCNotificationBanner.h"

typedef void (^JCNotificationBannerPresenterFinishedBlock)();

@class JCNotificationBannerWindow;

@interface JCNotificationBannerPresenter : NSObject

- (void)willBeginPresentingNotifications;
- (void)didFinishPresentingNotifications;
- (void) presentNotification:(JCNotificationBanner*)notification
                    finished:(JCNotificationBannerPresenterFinishedBlock)finished;

@end
