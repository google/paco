//
//  OrgJodaTimeDateTime+dateTimeClass.m
//  J2ObjcLibrary
//
//  Created by Northrop O'brien on 4/27/16.
//  Copyright © 2016 Northrop O'brien. All rights reserved.
//





#import "DateTime.h"



@implementation OrgJodaTimeDateTime (dateTimeClass)


-(BOOL) isGreaterThan:(OrgJodaTimeDateTime*) otherTime 
{
    
    return ([self getMillis] > [otherTime getMillis]);
}

@end
