//
//  ScheduleTestViewController.h
//  Paco
//
//  Created by northropo on 8/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PacoNotificationManager.h"
#import "PacoScheduleDelegate.h"

@class PacoScheduler;
@class PacoScheduleDelegate;
@class PacoExtendedClient;

@interface ScheduleTestViewController : UIViewController<PacoNotificationManagerDelegate>
@property (weak, nonatomic) IBOutlet UITextField *firstTime;
@property (weak, nonatomic) IBOutlet UITextField *secondTime;
@property (nonatomic, retain) PacoScheduler *scheduler;
@property (nonatomic, retain) PacoScheduleDelegate* schedulerDelegate;
@property (nonatomic, retain) PacoExtendedClient*   client;


@end
