#import "JCNotificationPresenterDemoViewController.h"
#import "JCNotificationCenter.h"
#import "JCNotificationBannerPresenterSmokeStyle.h"
#import "JCNotificationBannerPresenterIOSStyle.h"

@interface JCNotificationPresenterDemoViewController ()

@property (weak, nonatomic) IBOutlet UITextField* titleTextField;
@property (weak, nonatomic) IBOutlet UITextView* messageTextView;
@property (weak, nonatomic) IBOutlet UISegmentedControl* styleSwitch;

@end

@implementation JCNotificationPresenterDemoViewController

- (IBAction) presentNotificationButtonTapped:(id)sender {
  if (self.styleSwitch.selectedSegmentIndex) {
    [JCNotificationCenter sharedCenter].presenter = [JCNotificationBannerPresenterIOSStyle new];
  } else {
    [JCNotificationCenter sharedCenter].presenter = [JCNotificationBannerPresenterSmokeStyle new];
  }

  [JCNotificationCenter
   enqueueNotificationWithTitle:self.titleTextField.text
   message:self.messageTextView.text
   tapHandler:^{
     UIAlertView* alert = [[UIAlertView alloc]
                           initWithTitle:@"Tapped notification"
                           message:@"Perform some custom action on notification tap event..."
                           delegate:nil
                           cancelButtonTitle:@"OK"
                           otherButtonTitles:nil];
     [alert show];
   }];
}

- (void) viewDidUnload {
  [self setMessageTextView:nil];
  [self setTitleTextField:nil];
  [super viewDidUnload];
}

@end
