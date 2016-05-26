//
//  PacoConfigurationViewController.m
//  Paco
//
//  Created by Northrop O'brien on 5/24/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "PacoConfigurationViewController.h"
#import "PacoMediator.h" 
#import "PacoNetwork.h" 
#import "PacoConfigDetailController.h"
#import <MessageUI/MessageUI.h>



@interface PacoConfigurationViewController ()


@property (nonatomic,strong) NSArray*  cellNames;

@end

@implementation PacoConfigurationViewController

- (void)viewDidLoad {
    [super viewDidLoad];
   
    [self.tableView registerClass:[UITableViewCell class] forCellReuseIdentifier:@"identifier"];
    _cellNames = @[@"Refresh Experiments",@"Settings",@"User Guide", @"Email Paco Team", @"About Paco", @"User Agreement", @"Open Source Libraries"];

}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
 
      return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
 
       return [_cellNames count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    
     UITableViewCell *cell  = [tableView dequeueReusableCellWithIdentifier:@"identifier"  forIndexPath:indexPath];
    cell.textLabel.text = _cellNames[indexPath.row];
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



/*
 func sendMail(experiment:PAExperimentDAO) {
 
 
 self.picker  = MFMailComposeViewController()
 self.picker!.mailComposeDelegate = self
 
 if  experiment.valueForKeyEx("contactEmail")  != nil
 {
 var  email  = (experiment.valueForKeyEx("contactEmail")  as? String)!
 var toRecipents = [email]
 self.picker!.setToRecipients(toRecipents)
 
 }
 
 
 
 picker!.setSubject("subject")
 picker!.setMessageBody("body", isHTML: true)
 presentViewController(picker!, animated: true, completion: nil)
 
 
 }
 
 
 
 
 
 */







#pragma mark - Table view delegate

// In a xib-based application, navigation from a table can be handled in -tableView:didSelectRowAtIndexPath:
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    
    
    switch (indexPath.row) {
        case 0:
            
            [[PacoNetwork sharedInstance] update];

            break;
            
          case 1:
        {
            
            
    
            break;
        }
        case 2:
        {
            
            PacoConfigDetailController* ctrl = [[PacoConfigDetailController alloc] initWithNibName:@"PacoConfigDetailController" bundle:nil];
             ctrl.message = @"user guiede";
            [self.tabBarController.navigationController pushViewController:ctrl  animated:true];
            
            break;
            
        }
        case 3:
        {
            MFMailComposeViewController * mail   = [[MFMailComposeViewController alloc] init];
            mail.mailComposeDelegate = self;
            [self presentViewController:mail animated:YES completion:NULL];
            break;
            // [mail setSubject:@"Sample Subject"];
            // [mail setMessageBody:@"Here is some main text in the email!" isHTML:NO];
            // [mail setToRecipients:@[@"testingEmail@example.com"]];
            
            
        }
        case 4:
        {
            
            PacoConfigDetailController* ctrl = [[PacoConfigDetailController alloc] initWithNibName:@"PacoConfigDetailController" bundle:nil];
             ctrl.message = @"about paco";
            [self.tabBarController.navigationController pushViewController:ctrl  animated:true];
            
            break;
            
        }
        case 5:
        {
            
            PacoConfigDetailController* ctrl = [[PacoConfigDetailController alloc] initWithNibName:@"PacoConfigDetailController" bundle:nil];
             ctrl.message = @"user agreement";
            [self.tabBarController.navigationController pushViewController:ctrl  animated:true];
            
            break;
            
        }
   
        case 6:
        {
            
            PacoConfigDetailController* ctrl = [[PacoConfigDetailController alloc] initWithNibName:@"PacoConfigDetailController" bundle:nil];
            ctrl.message = @"open source libraries";
            [self.tabBarController.navigationController pushViewController:ctrl  animated:true];
            
            break;
            
        }
        default:
        {
            
             PacoConfigDetailController* ctrl = [[PacoConfigDetailController alloc] initWithNibName:@"PacoConfigDetailController" bundle:nil];
             [self.tabBarController.navigationController pushViewController:ctrl  animated:true];
            
            break;
        }
            
           
    }
    
    
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
