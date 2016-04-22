//
//  PacoViewController.h
//  Paco
//
//  Created by Northrop O'brien on 3/23/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
@class PAExperimentDAO;

@interface PacoJoinSummary : UIViewController
@property (weak, nonatomic) IBOutlet UITextView *summary;
@property (strong, nonatomic)  PAExperimentDAO *experiment;

@end
