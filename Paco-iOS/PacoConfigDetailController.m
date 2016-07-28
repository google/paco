//
//  PacoConfigDetailController.m
//  Paco
//
//  Created by Northrop O'brien on 5/25/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "PacoConfigDetailController.h"

@interface PacoConfigDetailController ()

@end

@implementation PacoConfigDetailController

- (void)viewDidLoad {
    [super viewDidLoad];
   
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


-(void) viewWillAppear:(BOOL)animated
{
    self.txtContent.text = self.message;
    
    
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
