//
//  ScheduleTestViewController.m
//  Paco
//
//  Created by northropo on 8/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "ScheduleTestViewController.h"
#import "PacoNotificationManager.h"
#import "UILocalNotification+Paco.h"




@interface ScheduleTestViewController ()

@end

@implementation ScheduleTestViewController
{
    PacoNotificationManager*  notificationManager;
    
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
}
- (IBAction)firePointFive:(id)sender
{
   
    NSLog(@" fire in .5 seconds");
    notificationManager =[PacoNotificationManager managerWithDelegate:self firstLaunchFlag:NO];
    [_firstTime.text intValue];
    NSDate* firstFireDate = [NSDate dateWithTimeIntervalSinceNow:[_firstTime.text intValue]]; //active
    
    /* schedule nofitification */
    NSTimeInterval timeoutInterval = 479*60;
    NSDate* secondFireDate = [NSDate dateWithTimeIntervalSinceNow:[_secondTime.text intValue]]; //active
    NSDate* secondTimeout = [NSDate dateWithTimeInterval:timeoutInterval sinceDate:firstFireDate];
    NSString* experimentId3 = @"3";
    NSString* title3 = @"title3";
    UILocalNotification* secondNoti = [UILocalNotification pacoNotificationWithExperimentId:experimentId3
                                                                            experimentTitle:title3
                                                                                   fireDate:secondFireDate
                                                                                timeOutDate:secondTimeout];
    
    /* end this */
    
    
    [notificationManager scheduleNotifications:@[secondNoti]];
    
    
    
    
    /*
    UILocalNotification *localNotif = [[UILocalNotification alloc] init];
    if (localNotif == nil) return;
    NSDate *fireTime = [[NSDate date] addTimeInterval:.5]; // adds 10 secs
    localNotif.fireDate = fireTime;
    localNotif.alertBody = @"Alert!";
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotif];
     */
    
}


- (void)handleExpiredNotifications:(NSArray*)expiredNotifications
{
    
    NSLog(@" handle expired notification");
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
