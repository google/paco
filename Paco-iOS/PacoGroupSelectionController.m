//
//  PacoGroupSelectionController.m
//  Paco
//
//  Created by Northrop O'brien on 7/13/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "PacoGroupSelectionController.h"
#import "PacoFeedbackWebViewController.h" 
#import "ExperimentDAO.h" 
#import "PacoQuestionScreenViewController.h" 
#import "PacoExperiment.h" 





@interface PacoGroupSelectionController ()

@property (nonatomic,strong) NSDictionary* groups;
@property (nonatomic,strong) NSArray * groupNames;

@end


static NSString *CellIdentifier = @"Cell";

@implementation PacoGroupSelectionController




- (void)viewDidLoad {
    
    [super viewDidLoad];
    [self.tableView registerClass:[UITableViewCell class]  forCellReuseIdentifier:CellIdentifier];
  
}


-(id) initWithNibNameAndGroups:(NSDictionary*) groups  experiment:(PAExperimentDAO*) experiment  nibName:(NSString*) nibName
{
    self = [super initWithNibName:nibName bundle:nil];
    
    if(self )
    {
        self.groups = groups;
        self.groupNames = [groups allKeys];
        self.experiment = experiment;
    }
    
    return self;
    
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
 
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
 
    return  [self.groupNames count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
    cell.textLabel.text =  self.groupNames[indexPath.row];
    return cell;
    
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


#pragma mark - Table view delegate

// In a xib-based application, navigation from a table can be handled in -tableView:didSelectRowAtIndexPath:
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{

    NSString * name =  self.groupNames[indexPath.row];
    PAExperimentGroup* group =  [self.groups objectForKey:name];
    
    
    
    PacoExperiment * experiment = [PacoExperiment experimentWithExperimentDao:_experiment];
    _questions  = [PacoQuestionScreenViewController controllerWithExperiment:experiment   group:group];
    
    /*_feedback  = [PacoFeedbackWebViewController controllerWithExperimentGroup:group  withExperiment:self.experiment  htmlName:@"skeleton" dismissBlock:^{
        
        
    }];*/
 
    [self.navigationController pushViewController:_questions   animated:YES];
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
