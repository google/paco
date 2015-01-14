#import "JCNotificationBannerViewIOS7Style.h"

@implementation JCNotificationBannerViewIOS7Style

- (id) initWithNotification:(JCNotificationBanner*)notification {
  self = [super initWithNotification:notification];
  if (self) {
    self.titleLabel.textColor = [UIColor whiteColor];
    self.messageLabel.textColor = [UIColor whiteColor];
    self.backgroundColor = [UIColor colorWithWhite:0.0 alpha:0.9];

    /*self.layer.shadowOffset = CGSizeMake(0, 1);
    self.layer.shadowColor = [UIColor darkGrayColor].CGColor;
    self.layer.shadowRadius = 3.0;
    self.layer.shadowOpacity = 0.8;*/
  }
  return self;
}

/** Overriden to do no custom drawing */
- (void) drawRect:(CGRect)rect {
}

@end
