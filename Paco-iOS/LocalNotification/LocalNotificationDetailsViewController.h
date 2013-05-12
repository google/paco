//
//  LocalNotificationDetailsViewController.h
//  LocalNotification
//
//  Created by Tom Pennings on 12/4/12.
//  Copyright (c) 2012 Lautumar. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RepeatPickerViewController.h"
#import "StartPickerViewController.h"

@class LocalNotificationDetailsViewController;

@protocol LocalNotificationDetailsViewControllerDelegate <NSObject>
    - (void)localNotificationDetailsViewControllerDidCancel:(LocalNotificationDetailsViewController *) controller;
    - (void)localNotificationDetailsViewController:(LocalNotificationDetailsViewController *) controller didAddLocalNotification:(UILocalNotification *) localNotification;
@end

@interface LocalNotificationDetailsViewController : UITableViewController <StartPickerViewControllerDelegate,RepeatPickerViewControllerDelegate>
    @property (nonatomic, weak) id <LocalNotificationDetailsViewControllerDelegate> delegate;
    @property (strong, nonatomic) IBOutlet UITextField *messageTextField;
    @property (strong, nonatomic) IBOutlet UILabel *detailStartLabel;
    @property (strong, nonatomic) IBOutlet UILabel *detailRepeatLabel;

    - (IBAction)cancel:(id)sender;
    - (IBAction)done:(id)sender;
@end

