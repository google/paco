


//
//  PacoViewController.m
//  Paco
//
//  Created by Northrop O'brien on 3/23/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "PacoJoinSummary.h"
#import "PacoEventManagerExtended.h"
#import "ExperimentDAO.h"
#import "PacoEventExtended.h" 
#import "TestUtil.h"
#import "PacoMediator.h" 
#import "PAExperimentDAO+Helper.h"




@interface PacoJoinSummary ()


@property (nonatomic,strong) PacoEventManagerExtended* eventManager;
@end

@implementation PacoJoinSummary

-(void) viewWillDisappear:(BOOL)animated
{
    
 
    
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    

    _eventManager   = [PacoEventManagerExtended defaultManager];
    UIBarButtonItem * doneBtn = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSave target:self action:@selector(donePressed:)];
    
    
    self.tabBarController.navigationItem.leftBarButtonItem  = doneBtn;
    self.tabBarController.navigationItem.rightBarButtonItems  = nil;
    
    
    
    
    self.navigationItem.rightBarButtonItem = doneBtn;
    self.summary.text = [self.experiment description];

    // Do any additional setup after loading the view from its nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(IBAction)donePressed:(UIBarButtonItem*) button
{
    
    PacoMediator* mediator = [PacoMediator sharedInstance];
    [mediator startRunningExperiment:[self.experiment instanceId]];
    
    
    
    [self backByN:5];
    
   //  PacoEventExtended* event = [PacoEventExtended joinEventForActionSpecificatonWithServerExperimentId:self.experiment  serverExperimentId:@"not applicable"];
    
    
    
}



-(void) backByN:(int) numberBack
{
     NSArray * controllers = [self navigationController].viewControllers;
    [self.navigationController
        popToViewController:controllers[controllers.count -numberBack] animated:YES];
    
    
}



//
//- (IBAction)go:(id)sender {
//    
//    
//    
//    NSDate *today = [NSDate date];
//    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
//    [dateFormat setDateFormat:@"yyyy/MM/dd HH:mm:ssZ"];
//    NSString *dateString = [dateFormat stringFromDate:today];
//    NSLog(@"date: %@", dateString);
//    
//   
//    
//    PAExperimentDAO * experiment =  [TestUtil buildExperiment:dataSource];
//    
//    NSString* thisdescription = [experiment description];
//    
//    
//    
//   // NSLog(@" descriptions %@", [experiment description]);
//    
//    
//    PacoEventExtended* event = [PacoEventExtended joinEventForActionSpecificatonWithServerExperimentId:experiment //serverExperimentId:@"not applicable"];
//    
//    
//    self.summary.text =  thisdescription;
//    
//    
//    
//                                
//                                
//                                
// [PacoEventExtended stopEventForActionSpecificatonWithServerExperimentId:experiment serverExperimentId:@"not applicable"];
// 
//    
//    //  PacoEventExtended* event2 = [PacoEventExtended stopEventForExperiment:experiment];
//    
//    
//    [_eventManager startUploadingEventsInBackgroundWithBlock:^(UIBackgroundFetchResult result ) {
//        NSLog(@" result %i",result );
//    }];
//   
//    
//    
//}



@end
