//
//  PacoExperimentTableCellTableViewCell.m
//  Paco
//
//  Created by Timo on 10/8/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoExperimentTableCellTableViewCell.h"
#import "ExperimentDAO.h"
#import "TempStorage.h"
#import "PacoMediator.h"
#import "PAExperimentDAO+Helper.h"


@implementation PacoExperimentTableCellTableViewCell

- (void)awakeFromNib {
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}
- (IBAction)removeExperiment:(id)sender
{
    [self.parent leaveExperiment:self.dao];
    
    [NSThread sleepForTimeInterval:2];
    [[PacoMediator sharedInstance].experiments removeObject:self.dao];
    [self.parent refresh];
}
- (IBAction)JoinOrLeave:(id)sender {
    
   if( ![[PacoMediator sharedInstance].startedExperiments containsObject:self.dao] )
   {
      // [[PacoMediator sharedInstance] stopRunningExperimentRegenerate:[self.dao instanceId]];
       [self.parent  joinExperiment:self.dao];
       
   }
   else
   {
       [self.parent leaveExperiment:self.dao];
       // [[PacoMediator sharedInstance] startRunningExperimentRegenerate:[self.dao instanceId]];
       
   }
    
}

@end
