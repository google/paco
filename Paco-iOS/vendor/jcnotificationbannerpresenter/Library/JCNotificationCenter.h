#import <Foundation/Foundation.h>
#import "JCNotificationBanner.h"

@class JCNotificationBannerPresenter;

@interface JCNotificationCenter : NSObject

@property (nonatomic) JCNotificationBannerPresenter* presenter;

+ (JCNotificationCenter*) sharedCenter;

/** Adds notification to queue with given parameters. */
+ (void) enqueueNotificationWithTitle:(NSString*)title
                              message:(NSString*)message
                           tapHandler:(JCNotificationBannerTapHandlingBlock)tapHandler;

- (void) enqueueNotificationWithTitle:(NSString*)title
                              message:(NSString*)message
                           tapHandler:(JCNotificationBannerTapHandlingBlock)tapHandler;

- (void) enqueueNotification:(JCNotificationBanner*)notification;

@end
