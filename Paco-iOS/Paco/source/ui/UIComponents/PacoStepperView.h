//
//  PacoStepperView.h
//  Paco
//
//  Created by Dhanya Chengappa on 08/10/13.
//  Copyright (c) 2013 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "PacoTableCell.h"

@class PacoStepperView;
@protocol PacoStepperViewDelegate <NSObject>

@optional
- (void)onStepperValueChanged:(PacoStepperView *)stepper;

@end

@interface PacoStepperView : PacoTableCell
@property (nonatomic, assign) id<PacoStepperViewDelegate> delegate;
@property (nonatomic, retain) NSString *format;
@property (nonatomic, retain) NSNumber *value;
@property (nonatomic, assign) double minValue;
@property (nonatomic, assign) double maxValue;
@end
