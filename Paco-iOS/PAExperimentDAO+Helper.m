//
//  PAExperimentDAO+Helper.m
//  Paco
//
//  Created by northropo on 9/11/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PAExperimentDAO+Helper.h"
#import "java/lang/Long.h"


@implementation PAExperimentDAO (Helper)

-(NSString*) instanceId
{
    NSString* retValue = nil;
    
    if(self->id__ !=nil)
    {
      retValue = [self->id__ stringValue];
    }
    
    return  retValue;
    
}

@end
