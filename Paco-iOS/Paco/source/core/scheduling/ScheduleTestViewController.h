//
//  ScheduleTestViewController.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PacoNotificationManager.h"
 

@class PacoScheduler;
@class PacoSchedulingUtil;
@class PacoExtendedClient;

@interface ScheduleTestViewController : UIViewController

@property (weak, nonatomic) IBOutlet UITextView *jsonField;
@property (strong,nonatomic) NSMutableArray* mutableArray;

@end
