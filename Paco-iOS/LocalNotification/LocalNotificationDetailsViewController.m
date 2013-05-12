//
//  LocalNotificationDetailsViewController.m
//  LocalNotification
//
//  Created by Tom Pennings on 12/4/12.
//  Copyright (c) 2012 Lautumar. All rights reserved.
//

#import "LocalNotificationDetailsViewController.h"

@interface LocalNotificationDetailsViewController ()

@end

@implementation LocalNotificationDetailsViewController {
	NSString *repeat;
    NSString *start;
}

@synthesize delegate;

- (id)initWithStyle:(UITableViewStyle)style {
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.detailStartLabel.text = start;
    self.detailRepeatLabel.text = repeat;

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (id)initWithCoder:(NSCoder *)aDecoder {
	if ((self = [super initWithCoder:aDecoder])) {
		NSLog(@"init LocalNotificationDetailsViewController");
		repeat = @"Never";
        start =  @"In Ten Seconds";
	}
	return self;
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
	if ([segue.identifier isEqualToString:@"PickRepeat"]) {
		RepeatPickerViewController *repeatPickerViewController =
        segue.destinationViewController;
		repeatPickerViewController.delegate = self;
		repeatPickerViewController.repeat = repeat;
	}
    if ([segue.identifier isEqualToString:@"PickStart"]) {
		StartPickerViewController *startPickerViewController =
        segue.destinationViewController;
		startPickerViewController.delegate = self;
		startPickerViewController.start = start;
	}

}

#pragma mark - Table view data source

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    if (indexPath.section == 0) {
		[self.messageTextField becomeFirstResponder];
    }
}

#pragma mark - Controller IBActions

- (IBAction)cancel:(id)sender {
    [self.delegate localNotificationDetailsViewControllerDidCancel:self];
}
- (IBAction)done:(id)sender {
    UILocalNotification *localNotification = [[UILocalNotification alloc] init]; //Create the localNotification object
    
    double startIntervalSinceNow = 30.0;
    if([start isEqualToString:@"In Ten Seconds"]) {
        startIntervalSinceNow = 10.0;
    } else if([start isEqualToString:@"In Thirty Seconds"]) {
        startIntervalSinceNow = 30.0;
    } else if([start isEqualToString:@"In One Minute"]) {
        startIntervalSinceNow = 60.0;
    } else if([start isEqualToString:@"In Five Minutes"]) {
        startIntervalSinceNow = 60.0 * 5;
    } else if([start isEqualToString:@"In Ten Minutes"]) {
        startIntervalSinceNow = 60.0 * 10;
    } else if([start isEqualToString:@"In Fifteen Minutes"]) {
        startIntervalSinceNow = 60.0 * 15;
    } else if([start isEqualToString:@"In Thirty Minutes"]) {
        startIntervalSinceNow = 60.0 * 30;
    } else if([start isEqualToString:@"In One Hour"]) {
        startIntervalSinceNow = 60.0 * 60;
    }
    
    if([repeat isEqualToString:@"Never"]) {
        [localNotification setRepeatInterval:0];
    } else if([repeat isEqualToString:@"Every Minute"]) {
        [localNotification setRepeatInterval:kCFCalendarUnitMinute];
    } else if([repeat isEqualToString:@"Every Hour"]) {
        [localNotification setRepeatInterval:kCFCalendarUnitHour];
    } else if([repeat isEqualToString:@"Every Day"]) {
        [localNotification setRepeatInterval:kCFCalendarUnitDay];
    }
    
    [localNotification setFireDate:[NSDate dateWithTimeIntervalSinceNow:startIntervalSinceNow]];; //Set the date when the alert will be launched using the date adding the time the user selected on the timer
    [localNotification setAlertAction:@"Launch"]; //The button's text that launches the application and is shown in the alert
    [localNotification setAlertBody:[_messageTextField text]]; //Set the message in the notification from the textField's text
    [localNotification setHasAction: YES]; //Set that pushing the button will launch the application
    [localNotification setApplicationIconBadgeNumber:[[UIApplication sharedApplication] applicationIconBadgeNumber]+1]; //Set the Application Icon Badge Number of the application's icon to the current Application Icon Badge Number plus 1
    localNotification.soundName = @"deepbark_trial.mp3";
    localNotification.timeZone = [NSTimeZone systemTimeZone];
    // systemTimeZone is the timezone of the device
    // localTimeZone is the timezon of the application
    
    NSDictionary *infoDict = [NSDictionary dictionaryWithObjectsAndKeys:@"Bob", @"key1", @"Evans", @"key2", nil];
    localNotification.userInfo = infoDict;
    
    
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotification]; //Schedule the notification with the system
    
    [self.delegate localNotificationDetailsViewController:self didAddLocalNotification:localNotification];
}

#pragma mark - RepeatPickerViewControllerDelegate

- (void)repeatPickerViewController:
(RepeatPickerViewController *)controller
                   didSelectRepeat:(NSString *)theRepeat {
	repeat = theRepeat;
	self.detailRepeatLabel.text = repeat;
	[self.navigationController popViewControllerAnimated:YES];
}

#pragma mark - StartPickerViewControllerDelegate

- (void)startPickerViewController:
(RepeatPickerViewController *)controller
                   didSelectStart:(NSString *)theStart {
	start = theStart;
	self.detailStartLabel.text = start;
	[self.navigationController popViewControllerAnimated:YES];
}


@end
