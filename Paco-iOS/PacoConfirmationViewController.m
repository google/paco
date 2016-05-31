//
//  PacoConfirmationViewController.m
//  Paco
//
//  Created by Northrop O'brien on 5/26/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import    "PacoConfirmationViewController.h"
#include    "ExperimentDAO.h"
#import     "Paco-Swift.h"
#import     "PacoMediator.h"
#import     "PacoMediator.h"
#import    "PacoEventExtended.h" 
#import    "PacoEventManagerExtended.h"







@interface PacoConfirmationViewController ()

@end

@implementation PacoConfirmationViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
         self.navigationItem.hidesBackButton=TRUE;
    
    
    self.navigationItem.rightBarButtonItem =
    [[UIBarButtonItem alloc]
      initWithBarButtonSystemItem:UIBarButtonSystemItemDone
      target:self
      action:@selector(experimentAdded:)]  ;
    
    
    
    
    dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
     
        PacoMediator* mediator = [PacoMediator sharedInstance];
        [mediator startRunningExperiment:[self.experiment instanceId]];
        PacoEventExtended* event = [PacoEventExtended joinEventForActionSpecificatonWithServerExperimentId:self.experiment  serverExperimentId:@"not applicable"];
   
        [mediator.eventManager saveEvent:event];
        [mediator.eventManager startUploadingEvents];
    });

    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
  
}
- (IBAction)editSchedule:(id)sender
{
    
    
     ScheduleEditor * scheduleEditor = [[ScheduleEditor alloc] initWithNibName:@"ScheduleEditor" bundle:nil];
     NSArray * cells = [_experiment getTableCellModelObjects];
     scheduleEditor.cells = cells;
     scheduleEditor.isWizard = true;
     UINavigationController * controller = [[UINavigationController alloc] initWithRootViewController:scheduleEditor];
    [self presentModalViewController:controller  animated:TRUE];
    
    
    }




-(IBAction)experimentAdded:(id)sender
{
  //  int numberOfControllers =  [self.navigationController.viewControllers count];
    [self.navigationController popToRootViewControllerAnimated:YES];
    
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
