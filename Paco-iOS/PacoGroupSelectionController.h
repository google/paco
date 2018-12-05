//
//  PacoGroupSelectionController.h
//  Paco
//
//  Created by Northrop O'brien on 7/13/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>

@class  PacoFeedbackWebViewController;
@class  PAExperimentDAO;
@class  PacoQuestionScreenViewController;

@interface PacoGroupSelectionController : UITableViewController

@property (nonatomic, strong)   PacoFeedbackWebViewController * feedback;
@property (nonatomic, strong)   PacoQuestionScreenViewController* questions;
@property (nonatomic, strong)   PAExperimentDAO * experiment;


-(id) initWithNibNameAndGroups:(NSDictionary*) groups  experiment:(PAExperimentDAO*) experiment  nibName:(NSString*) nibName;


@end
