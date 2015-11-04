//
//  OrgJodaTimeDateMidnight+PacoDateHelper.m
//  Paco
//
//  Created by northropo on 10/22/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "OrgJodaTimeDateMidnight+PacoDateHelper.h"

@implementation OrgJodaTimeDateMidnight (PacoDateHelper)


-(NSDate*) nsDateValue
{
    
    /* divide by one thousand to convert to seconds since epoch*/
    NSDate * date =  [NSDate dateWithTimeIntervalSince1970:[self getMillis]/1000];
    return  date;
}

 
@end
