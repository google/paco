//
//  PacoTestCustomFeedBackViewController.h
//  Paco
//
//  Created by Northrop O'brien on 7/10/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
@class PacoFeedbackWebViewController;
@class PacoGroupSelectionController;

@interface PacoTestCustomFeedBackViewController : UIViewController

@property (nonatomic, strong)  UINavigationController* navigation;
@property (nonatomic, strong)   PacoFeedbackWebViewController * feedback;
@property (nonatomic, strong)   PacoGroupSelectionController * groupSelection;

@end
