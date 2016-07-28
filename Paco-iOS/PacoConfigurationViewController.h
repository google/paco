//
//  PacoConfigurationViewController.h
//  Paco
//
//  Created by Northrop O'brien on 5/24/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MessageUI/MessageUI.h>


@class MFMailComposeViewController;

@interface PacoConfigurationViewController : UITableViewController<MFMailComposeViewControllerDelegate> 

@property (nonatomic, strong) MFMailComposeViewController * mailController;

@end
