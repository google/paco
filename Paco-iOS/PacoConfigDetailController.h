//
//  PacoConfigDetailController.h
//  Paco
//
//  Created by Northrop O'brien on 5/25/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PacoConfigDetailController : UIViewController

@property (weak, nonatomic) IBOutlet UITextView *txtContent;
@property (strong,nonatomic) NSString* message;
@end
