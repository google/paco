//
//  ScheduleTestViewController.h
//  Paco
//
//  Created by northropo on 8/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PacoNotificationManager.h"

@interface ScheduleTestViewController : UIViewController<PacoNotificationManagerDelegate>
@property (weak, nonatomic) IBOutlet UITextField *firstTime;
@property (weak, nonatomic) IBOutlet UITextField *secondTime;


@end
