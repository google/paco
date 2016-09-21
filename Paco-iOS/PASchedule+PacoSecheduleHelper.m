//
//  PASchedule+PacoSecheduleHelper.m
//  Paco
//
//  Created by Northrop O'brien on 9/19/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "PASchedule+PacoSecheduleHelper.h"
#import "java/lang/Long.h"


@implementation PASchedule (PacoSecheduleHelper)

-(void) setESMSStartTime:(int) time
{
    JavaLangLong * ll  = [[JavaLangLong alloc] initWithLong:time];
    [self setEsmStartHourWithJavaLangLong:ll];
    
}


-(void) setESMSEndime:(int) time
{
    JavaLangLong * ll  = [[JavaLangLong alloc] initWithLong:time];
    [self  setEsmEndHourWithJavaLangLong:ll];
    
    
    
}



@end
