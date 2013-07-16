//
//  LocalNotificationsViewController.h
//  LocalNotification
//
//  Created by Tom Pennings on 12/3/12.
//  Copyright (c) 2012 Lautumar. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreLocation/CoreLocation.h>
#import "LocalNotificationDetailsViewController.h"

@interface LocalNotificationsViewController : UITableViewController<LocalNotificationDetailsViewControllerDelegate, CLLocationManagerDelegate> {
    NSMutableArray *localNotifications;
    CLLocationManager *locationManager;
}

@property(nonatomic, retain) CLLocationManager *locationManager;
@property (nonatomic, strong) NSMutableArray *localNotifications;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *clearButton;

- (void) refreshUI;

- (IBAction)clearNotifications:(id)sender;

@end
