#import "JCNotificationBannerViewIOSStyle.h"

@implementation JCNotificationBannerViewIOSStyle

- (id) initWithNotification:(JCNotificationBanner*)notification {
  self = [super initWithNotification:notification];
  if (self) {
    self.titleLabel.textColor = [UIColor darkTextColor];
    self.messageLabel.textColor = [UIColor darkTextColor];
    self.backgroundColor = [UIColor whiteColor];

    self.layer.shadowOffset = CGSizeMake(0, 1);
    self.layer.shadowColor = [UIColor darkGrayColor].CGColor;
    self.layer.shadowRadius = 3.0;
    self.layer.shadowOpacity = 0.8;
  }
  return self;
}

/** Overriden to do no custom drawing */
- (void) drawRect:(CGRect)rect {
}

@end
