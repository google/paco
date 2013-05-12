//
//  StartPickerViewController.m
//  LocalNotification
//
//  Created by Tom Pennings on 12/4/12.
//  Copyright (c) 2012 Lautumar. All rights reserved.
//

#import "StartPickerViewController.h"

@interface StartPickerViewController ()

@end

@implementation StartPickerViewController{
	NSArray *startTimes;
	NSUInteger selectedIndex;
}
@synthesize delegate;
@synthesize start;

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    startTimes = [NSArray arrayWithObjects:
                   @"In Ten Seconds",
                   @"In Thirty Seconds",
                   @"In One Minute",
                   @"In Five Minutes",
                   @"In Ten Minutes",
                   @"In Fifteen Minutes",
                   @"In Thirty Minutes",
                   @"In One Hour",
                   nil];
    
    selectedIndex = [startTimes indexOfObject:self.start];

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)viewDidUnload {
	[super viewDidUnload];
	startTimes = nil;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return [startTimes count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"StartCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
    
    // Configure the cell...
	cell.textLabel.text = [startTimes objectAtIndex:indexPath.row];
    if (indexPath.row == selectedIndex) {
		cell.accessoryType = UITableViewCellAccessoryCheckmark;
	} else {
		cell.accessoryType = UITableViewCellAccessoryNone;
    }
    
    return cell;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	[tableView deselectRowAtIndexPath:indexPath animated:YES];
	if (selectedIndex != NSNotFound)
	{
		UITableViewCell *cell = [tableView
                                 cellForRowAtIndexPath:[NSIndexPath
                                                        indexPathForRow:selectedIndex inSection:0]];
		cell.accessoryType = UITableViewCellAccessoryNone;
	}
	selectedIndex = indexPath.row;
	UITableViewCell *cell =
    [tableView cellForRowAtIndexPath:indexPath];
	cell.accessoryType = UITableViewCellAccessoryCheckmark;
	NSString *theStart = [startTimes objectAtIndex:indexPath.row];
	[self.delegate startPickerViewController:self
                              didSelectStart:theStart];
}

@end
