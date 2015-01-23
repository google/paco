#import <UIKit/UIKit.h>
#import <QuartzCore/QuartzCore.h>
#import "JCNotificationBanner.h"

@interface JCNotificationBannerView : UIView

@property (nonatomic) JCNotificationBanner* notificationBanner;
@property (nonatomic) UIImageView* iconImageView;
@property (nonatomic) UILabel* titleLabel;
@property (nonatomic) UILabel* messageLabel;

- (id) initWithNotification:(JCNotificationBanner*)notification;
- (BOOL) getCurrentPresentingStateAndAtomicallySetPresentingState:(BOOL)state;

@end
