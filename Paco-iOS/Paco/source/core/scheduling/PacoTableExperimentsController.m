//
//  PacoTableExperimentsController.m
//  Paco
//
//  Created by northropo on 10/8/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoTableExperimentsController.h"
#import "TempStorage.h"
#import "PacoExperimentTableCellTableViewCell.h"
#import "ScheduleTestViewController.h" 
#import "PAExperimentDAO+Helper.h" 
#import  "NSObject+J2objcKVO.h"
#import  "PacoMediator.h"
#import "PacoNotificationManager.h"
#import "PacoNortificationsForExperiment.h"

@interface PacoNotificationManager()

@property (atomic, retain) NSMutableDictionary* notificationDict;
@property (readwrite) BOOL isJoin;
@end



@interface PacoTableExperimentsController ()

@end

@implementation PacoTableExperimentsController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    
      UIBarButtonItem * addExperiment =  [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
                                                  target:self  action:@selector(addExperiment:)];
      self.navigationItem.rightBarButtonItem = addExperiment;
    
 
    
    [self.tableView registerNib:[UINib nibWithNibName:@"PacoExperimentTableCellTableViewCell"  bundle:nil] forCellReuseIdentifier:@"ExperimentCell"];
    
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

-(void) refresh
{
    [self.tableView reloadData];
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source


-(IBAction)addExperiment:(id)sender
{
    ScheduleTestViewController* addExperiment = [[ScheduleTestViewController alloc] initWithNibName:@"ScheduleTestViewController" bundle:nil];
    
    [self presentViewController:addExperiment animated:YES completion:nil];
    
    
   
    //[self.navigationController pushViewController:addExperiment animated:YES];
    NSLog(@"add experiment");
    
}


-(void) joinExperiment:(PAExperimentDAO*) dao
{
    
    [[PacoMediator sharedInstance] startRunningExperiment:[dao instanceId]];
    
     [NSThread sleepForTimeInterval:1];
     [self.tableView reloadData];
}


-(void) leaveExperiment:(PAExperimentDAO*) dao
{
    
    [[PacoMediator sharedInstance] stopRunningExperiment:[dao instanceId]];
    [NSThread sleepForTimeInterval:1];
     [self.tableView reloadData];
}


-(void) viewWillAppear:(BOOL)animated
{
    
    [self.tableView reloadData];
    
}


- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
 
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
 
    return [[PacoMediator sharedInstance].experiments count];
    
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    PacoExperimentTableCellTableViewCell *cell = (PacoExperimentTableCellTableViewCell*) [tableView dequeueReusableCellWithIdentifier:@"ExperimentCell" forIndexPath:indexPath];
    
    
    PAExperimentDAO* dao =  [[PacoMediator sharedInstance].experiments objectAtIndex:indexPath.row];
    
    cell.experimentId.text = [dao instanceId];
    cell.name.text  = [dao valueForKeyEx:@"title"];
    cell.dao=dao;
    cell.parent=self;
    
    
    if([[PacoMediator sharedInstance].notificationManager.notificationDict objectForKey:[dao instanceId]])
    {
        NSLog(@"hello this is it");
        
        
    }
    
     if( [[PacoMediator sharedInstance].startedExperiments containsObject:dao] )
     {
        [ cell.joinBtn setTitle:@"Leave" forState:UIControlStateNormal];
         cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
         int count=  [[[PacoMediator sharedInstance].notificationManager.notificationDict objectForKey:[dao instanceId]] count];
         cell.status.text = [NSString stringWithFormat:@"%i Notifications",count];
         
     }
    else
    {
        
        [cell.joinBtn setTitle:@"Join" forState:UIControlStateNormal];;
        cell.accessoryType = UITableViewCellAccessoryNone;
        cell.status.text =[NSString stringWithFormat:@"%i Notifications",0];
       
        
        
       
        
    }
    
    return cell;
}



- (void)tableView:(UITableView *  )tableView
didSelectRowAtIndexPath:(NSIndexPath *  )indexPath
{
    
    PAExperimentDAO* dao =  [[PacoMediator sharedInstance].experiments objectAtIndex:indexPath.row];
    NSArray * notifications = [[PacoMediator sharedInstance].notificationManager.notificationDict objectForKey:[dao instanceId]];
    PacoNortificationsForExperiment * controller = [[PacoNortificationsForExperiment alloc] initWithNibName:@"PacoNortificationsForExperiment" bundle:nil];
    controller.notficatons  =notifications;
    [self.navigationController pushViewController:controller animated:YES];
}


/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

/*
#pragma mark - Table view delegate

// In a xib-based application, navigation from a table can be handled in -tableView:didSelectRowAtIndexPath:
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    // Navigation logic may go here, for example:
    // Create the next view controller.
    <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:<#@"Nib name"#> bundle:nil];
    
    // Pass the selected object to the new view controller.
    
    // Push the view controller.
    [self.navigationController pushViewController:detailViewController animated:YES];
}
*/

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
