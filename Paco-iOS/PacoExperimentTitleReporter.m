//
//  PacoExperimentTitleReporter.m
//  Paco
//
//  Created by northropo on 9/25/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoExperimentTitleReporter.h"
#include "ExperimentDAO.h"
#import "NSObject+J2objcKVO.h"

@implementation PacoExperimentTitleReporter

-(void)  notifyDidStart:(PAExperimentDAO*) experiment
{
    
    
    NSString* title = [experiment valueForKeyEx:@"title"];
    NSLog(@"Title : %@", title);
    
}

@end
