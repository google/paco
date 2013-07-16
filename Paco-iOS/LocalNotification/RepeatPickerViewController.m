//
//  RepeatPickerViewController.m
//  LocalNotification
//
//  Created by Tom Pennings on 12/4/12.
//  Copyright (c) 2012 Lautumar. All rights reserved.
//

#import "RepeatPickerViewController.h"

@interface RepeatPickerViewController ()

@end

@implementation RepeatPickerViewController {
	NSArray *repeatTimes;
	NSUInteger selectedIndex;
}
@synthesize delegate;
@synthesize repeat;

- (id)initWithStyle:(UITableViewStyle)style {
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad{
    [super viewDidLoad];
    
    repeatTimes = [NSArray arrayWithObjects:
             @"Never",
             @"Every Minute",
             @"Every Hour",
             @"Every Day",
            nil];

    selectedIndex = [repeatTimes indexOfObject:self.repeat];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)viewDidUnload {
	[super viewDidUnload];
	repeatTimes = nil;
}

- (void)didReceiveMemoryWarning {
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
    return [repeatTimes count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"RepeatCell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier forIndexPath:indexPath];
        
    // Configure the cell...
	cell.textLabel.text = [repeatTimes objectAtIndex:indexPath.row];
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
	NSString *theRepeat = [repeatTimes objectAtIndex:indexPath.row];
	[self.delegate repeatPickerViewController:self
                              didSelectRepeat:theRepeat];
}

@end
