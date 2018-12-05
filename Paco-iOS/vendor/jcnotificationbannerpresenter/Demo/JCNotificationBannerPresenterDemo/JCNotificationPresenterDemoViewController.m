#import "JCNotificationPresenterDemoViewController.h"
#import "JCNotificationCenter.h"
#import "JCNotificationBannerPresenterSmokeStyle.h"
#import "JCNotificationBannerPresenterIOSStyle.h"
#import "JCNotificationBannerPresenterIOS7Style.h"

@interface JCNotificationPresenterDemoViewController ()

@property (weak, nonatomic) IBOutlet UITextField* titleTextField;
@property (weak, nonatomic) IBOutlet UITextView* messageTextView;
@property (weak, nonatomic) IBOutlet UISegmentedControl* styleSwitch;

@end

@implementation JCNotificationPresenterDemoViewController

- (IBAction) presentNotificationButtonTapped:(id)sender {
  switch (self.styleSwitch.selectedSegmentIndex) {
    case 0:
      [JCNotificationCenter sharedCenter].presenter = [JCNotificationBannerPresenterSmokeStyle new];
      break;
    case 1:
      [JCNotificationCenter sharedCenter].presenter = [JCNotificationBannerPresenterIOSStyle new];
      break;
    case 2:
    default:
      [JCNotificationCenter sharedCenter].presenter = [JCNotificationBannerPresenterIOS7Style new];
      break;
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
