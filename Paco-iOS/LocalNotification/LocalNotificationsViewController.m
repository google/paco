//
//  LocalNotificationsViewController.m
//  LocalNotification
//
//  Created by Tom Pennings on 12/3/12.
//  Copyright (c) 2012 Lautumar. All rights reserved.
//
// TODO: Implement an observer on the MSMutableArray scheduledLocalNotifications such that the UI automatically updates when a Local Notification gets added or expires

#import "LocalNotificationsViewController.h"

@interface LocalNotificationsViewController ()

@end

@implementation LocalNotificationsViewController {
    NSTimer *aTimer;
}

@synthesize locationManager;
@synthesize localNotifications;

- (id)initWithStyle:(UITableViewStyle)style {
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad {
    NSLog(@"viewDidLoad...");
    [super viewDidLoad];
    
    self.localNotifications= [NSMutableArray arrayWithArray:[UIApplication sharedApplication].scheduledLocalNotifications];
    
    locationManager = [[CLLocationManager alloc] init];
    [locationManager setDelegate:self];
    [locationManager setDesiredAccuracy:kCLLocationAccuracyThreeKilometers];
  //  [locationManager startUpdatingLocation];
    
//    UIBackgroundTaskIdentifier bgTask;
//    
//    // Register to execute task in background if required
//    UIApplication *app = [UIApplication sharedApplication];
//        
//    bgTask = [app beginBackgroundTaskWithExpirationHandler:^{
//            // If you’re worried about exceeding 10 minutes, handle it here
//            [app endBackgroundTask:bgTask];
//            bgTask = UIBackgroundTaskInvalid;
//        }];
//    }
    
    UIBackgroundTaskIdentifier bgTask = 0;
    UIApplication  *app = [UIApplication sharedApplication];
    bgTask = [app beginBackgroundTaskWithExpirationHandler:^{
        [app endBackgroundTask:bgTask];
    }];

    aTimer = [NSTimer scheduledTimerWithTimeInterval:30.0
                                              target:self
                                            selector:@selector(timerFired:)
                                            userInfo:nil
                                             repeats:YES];
    
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

-(void)timerFired:(NSTimer *) theTimer {
    NSLog(@"timerFired @ %@", [theTimer fireDate]);
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    NSLog(@"View appeared");
    [self refreshUI];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    // Log when this View get's allocated in memory
	if ((self = [super initWithCoder:aDecoder])) {
		NSLog(@"init LocalNotificationsViewController");
	}
	return self;
}

- (void)dealloc {
    // Log when this View get's deallocated in memory
	NSLog(@"dealloc LocalNotificationsViewController");
}

- (IBAction)clearNotifications:(id)sender {
    NSLog(@"Clearing all notifications");
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
    [locationManager stopUpdatingLocation];
    [self refreshUI];
}

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
    CLLocationCoordinate2D currentCoordinates = newLocation.coordinate;
    NSLog(@"Entered new Location with the coordinates Latitude: %f Longitude: %f", currentCoordinates.latitude, currentCoordinates.longitude);
}


- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
    NSLog(@"Unable to start location manager. Error:%@", [error description]);
}

#pragma mark - Table view data source

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return [self.localNotifications count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"LocalNotificationCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
    
    UILocalNotification *localNotification = [self.localNotifications objectAtIndex:indexPath.row];
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"EEEE d MMM yyyy HH:mm:ss z"];
    //Optionally for time zone converstions
    [formatter setTimeZone:localNotification.timeZone];
    NSString *stringFromDate = [formatter stringFromDate:localNotification.fireDate];
    
    // Configure the cell
    cell.textLabel.text = localNotification.alertBody;
    cell.detailTextLabel.text = stringFromDate;
    
    return cell;
}

- (void) refreshUI {
    self.localNotifications= [NSMutableArray arrayWithArray:[UIApplication sharedApplication].scheduledLocalNotifications];
    NSLog(@"Total notifications: %d", self.localNotifications.count);
    if (self.tableView == nil) {
        NSLog(@"Houston we have a problem");
    }
    [self.tableView reloadData];
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
 }
 else if (editingStyle == UITableViewCellEditingStyleInsert) {
 // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
 }
 }
 */

/*
 // Override to support rearranging the table view.
 - (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
 {
 }
 */

/*
 // Override to support conditional rearranging of the table view.
 - (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
 {
 // Return NO if you do not want the item to be re-orderable.
 return YES;
 }
 */

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
	if ([segue.identifier isEqualToString:@"AddLocalNotification"]) {
		UINavigationController *navigationController = segue.destinationViewController;
		LocalNotificationDetailsViewController *localNotificationDetailsViewController = [[navigationController viewControllers]objectAtIndex:0];
		localNotificationDetailsViewController.delegate = self;
	}
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    // Navigation logic may go here. Create and push another view controller.
    /*
     <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
     [self.navigationController pushViewController:detailViewController animated:YES];
     */
}

#pragma mark - PlayerDetailsViewControllerDelegate

- (void)localNotificationDetailsViewControllerDidCancel:(LocalNotificationDetailsViewController *) controller {
    [self dismissViewControllerAnimated:YES completion:nil];
}
- (void)localNotificationDetailsViewController:(LocalNotificationDetailsViewController *) controller didAddLocalNotification:(UILocalNotification *) localNotification; {
    
    // This replaces [self.tableView reloadData], and provides a nicer animation
    [self.localNotifications addObject:localNotification];
    NSIndexPath *indexPath = [NSIndexPath indexPathForRow:[self.localNotifications count] - 1 inSection:0];
	[self.tableView insertRowsAtIndexPaths: [NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationAutomatic];
	[self dismissViewControllerAnimated:YES completion:nil];
}

@end
