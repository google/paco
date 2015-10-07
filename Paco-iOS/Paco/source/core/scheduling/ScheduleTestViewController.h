//
//  ScheduleTestViewController.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PacoNotificationManager.h"
#import "PacoSchedulingUtil.h"

@class PacoScheduler;
@class PacoSchedulingUtil;
@class PacoExtendedClient;

@interface ScheduleTestViewController : UIViewController<PacoNotificationManagerDelegate>
@property (weak, nonatomic) IBOutlet UITextField *firstTime;
@property (weak, nonatomic) IBOutlet UITextField *secondTime;
@property (nonatomic, retain) PacoScheduler *scheduler;
@property (nonatomic, retain) PacoSchedulingUtil* schedulerDelegate;
@property (nonatomic, retain) PacoExtendedClient*   client;


@end
