//
//  RepeatPickerViewController.h
//  LocalNotification
//
//  Created by Tom Pennings on 12/4/12.
//  Copyright (c) 2012 Lautumar. All rights reserved.
//

#import <UIKit/UIKit.h>

@class RepeatPickerViewController;

@protocol RepeatPickerViewControllerDelegate <NSObject>
    - (void)repeatPickerViewController:(RepeatPickerViewController *) controller didSelectRepeat:(NSString *)repeatTime;
@end

@interface RepeatPickerViewController : UITableViewController

    @property (nonatomic, weak) id <RepeatPickerViewControllerDelegate> delegate;
    @property (nonatomic, strong) NSString *repeat;

@end
