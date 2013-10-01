#import "JCAppDelegate.h"
#import "JCNotificationPresenterDemoViewController.h"

@implementation JCAppDelegate

- (BOOL) application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions {
  self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];

  UIStoryboard* storyboard = [UIStoryboard storyboardWithName:@"DemoStoryboard" bundle:nil];
  JCNotificationPresenterDemoViewController* demoController;
  demoController = (JCNotificationPresenterDemoViewController*)[storyboard instantiateInitialViewController];
  self.window.rootViewController = demoController;

  self.window.backgroundColor = [UIColor whiteColor];
  [self.window makeKeyAndVisible];

  return YES;
}

@end
