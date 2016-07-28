//
//  HubPaginatedTableViewController.m
//  Paco
//
//  Created by Northrop O'brien on 3/6/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "HubPaginatedTableViewController.h"
#import "PacoTestViewer.h"
#import "PacoService.h"
#import "PacoPublicDefinitionLoader.h"
#import "PacoEnumerator.h"
#import "PacoService.h"
#import "PacoSerializer.h"
#import "PacoSerializeUtil.h"
#import "NSObject+J2objcKVO.h" 
#import "Paco-Swift.h"
#import "PacoMediator.h"
#import "NSMutableArray+PacoModel.h"






@interface HubPaginatedTableViewController ()

@property(nonatomic, strong) id<PacoEnumerator> myExperimentsIterator;
@property(nonatomic, strong) id<PacoEnumerator> publicExperimentIterator;
@property(nonatomic, strong) PacoService* service;




@property(nonatomic, strong) NSMutableArray* experiments;


// pass enumerators as arguments. 
@property(nonatomic, strong) PacoPublicDefinitionLoader* loader;

@end

@implementation HubPaginatedTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    

    self.tabBarController.navigationController.delegate  = self;
    
    _experiments = [NSMutableArray new];
    _publicExperimentIterator =  [PacoPublicDefinitionLoader  publicExperimentsEnumerator];
    _myExperimentsIterator = [PacoPublicDefinitionLoader  myExperimentsEnumerator];
    _service = [[PacoService alloc] init];
    _loader  = [[PacoPublicDefinitionLoader alloc] init];
    
      [self.tableView registerClass:[UITableViewCell class] forCellReuseIdentifier:@"Cell"];
     // [self.tableView registerClass:[PacoMyExpermementTitleCellTableViewCell class] forCellReuseIdentifier:@"Cell2"];
   
     [self.tableView registerNib:[UINib nibWithNibName:@"PacoMyExpermementTitleCellTableViewCell"  bundle:nil]forCellReuseIdentifier:@"Cell2"];
    
    [_publicExperimentIterator loadNextPage:^(NSArray *array, NSError * error) {
        
      
        [self.experiments addObjectsFromArray:array];
        
        
        NSArray* startedExperiments =    [PacoMediator sharedInstance].startedExperiments;
        [self.experiments removeObjectsInArray:startedExperiments];
        
        
        
        [[PacoMediator sharedInstance].hubExperiments addObjectsFromArray:array];
        
        
        [self.tableView reloadData];
        
    }];
    
    
 
}


- (void)navigationController:(UINavigationController *)navigationController
      willShowViewController:(UIViewController *)viewController animated:(BOOL)animated {
    
    
    long  viewCount  = [[self.tabBarController.navigationController viewControllers] count];
    
    
    if(viewCount ==1)
    {
        
        [self viewWillAppear:YES];
        
    }
    
    NSLog(@" will show delegeate %lu",  viewCount) ;
 
}


-(void) viewDidAppear:(BOOL)animated
{
    
      NSArray* startedExperiments =    [PacoMediator sharedInstance].startedExperiments;
      [self.experiments removeObjectsInArray:startedExperiments];
      [self.experiments removeExperiments:startedExperiments];
      [self.tableView reloadData];
    

    
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
    
    if(_experiments)
    {
 
        return [_experiments count]+1;
    }
    else
    {
        return 1;
        
        
    }
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    
    PacoMyExpermementTitleCellTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Cell2"  forIndexPath:indexPath];
    
 
   if( [self.experiments count] ==0 && indexPath.row >=[self.experiments count]   )
   {
       cell.textLabel.text=  @"Loading...";
       
   }
   else   if( [self.experiments count] !=0 && indexPath.row >=[self.experiments count]   )
   {
       cell.textLabel.text=  @"Loading...";
       
       [_publicExperimentIterator loadNextPage:^(NSArray *array, NSError * error) {
           
           
           [self.experiments addObjectsFromArray:array];
           [self.tableView reloadData];
           
       }];
       
   }
   else
   {
         cell.textLabel.text = [_experiments[indexPath.row]  valueForKeyEx:@""];
       
      
       NSDictionary  *  dao = _experiments[indexPath.row];
       NSString* title;
       NSString*  organization;
       NSString*  email;
       NSString* description;
       
       if ( [dao valueForKeyEx:@"title"] != nil)
       {
           title = [dao valueForKeyEx:@"title"];
       }
       if  ( [dao valueForKeyEx:@"description"]  != nil)
       {
           description = [dao valueForKeyEx:@"description"] ;
       }
       if ( [dao valueForKeyEx:@"organization"])
       {
           organization =  [dao valueForKeyEx:@"organization"];
       }
       else
       {
           organization = @" ";
       }
       if  ([dao valueForKeyEx:@"contactEmail"] != nil)
       {
           email = [dao valueForKeyEx:@"contactEmail"];
       }
       else
       {
           email = @" ";
           
       }
       
       
       NSArray* array = [PacoSerializeUtil getClassNames];
       PacoSerializer* serializer =
       [[PacoSerializer alloc] initWithArrayOfClasses:array
                             withNameOfClassAttribute:@"nameOfClass"];
       //JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildModelObject:dao];
      // IOSObjectArray * iosArray = [resultArray toArray];
       
       PAExperimentDAO * DAO =  [serializer buildModelObject:dao];
       
       // cell.parent = self;
       cell.experiment = DAO;
       cell.experimentTitle.text = title;
       cell.subtitle.text =  [NSString stringWithFormat:@"%@ %@",organization, email ] ;
       cell.selectionStyle  = UITableViewCellSelectionStyleNone ;
 
   }
 
    return cell;
}


- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 60;
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
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    // Navigation logic may go here, for example:
    // Create the next view controller.
    
    PacoMyExpermementTitleCellTableViewCell*  cell = (PacoMyExpermementTitleCellTableViewCell*)  [self.tableView cellForRowAtIndexPath:indexPath];
    
    PacoExperimentDetailController *detailViewController = [[PacoExperimentDetailController alloc] initWithNibName:@"PacoExperimentDetailController" bundle:nil];
    
    
    
 
    
    
    
    if (   [cell.experiment valueForKeyEx:@"title"] != nil)
    {
        detailViewController.title   =  [cell.experiment valueForKeyEx:@"title"];
    }
    
    detailViewController.experiment = cell.experiment;
    [self.tabBarController.navigationController pushViewController:detailViewController animated:TRUE];
  
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
