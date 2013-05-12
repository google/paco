//
//  StartPickerViewController.h
//  LocalNotification
//
//  Created by Tom Pennings on 12/4/12.
//  Copyright (c) 2012 Lautumar. All rights reserved.
//

#import <UIKit/UIKit.h>

@class StartPickerViewController;

@protocol StartPickerViewControllerDelegate <NSObject>
- (void)startPickerViewController:(StartPickerViewController *) controller didSelectStart:(NSString *)startTime;
@end

@interface StartPickerViewController : UITableViewController

@property (nonatomic, weak) id <StartPickerViewControllerDelegate> delegate;
@property (nonatomic, strong) NSString *start;

@end

