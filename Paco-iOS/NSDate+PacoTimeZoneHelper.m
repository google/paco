//
//  NSDate+PacoTimeZoneHelper.m
//  Paco
//
//  Created by northropo on 10/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "NSDate+PacoTimeZoneHelper.h"

@implementation NSDate (PacoTimeZoneHelper)



-(NSString*) dateToStringLocalTimezone
{
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    [dateFormatter setTimeZone:[NSTimeZone localTimeZone]];
    
    NSString* dateStr = [dateFormatter  stringFromDate:self];
    return dateStr;
    
}

-(NSString*) dateToStringLocalTimezone:(NSTimeZone*) timeZone
{
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    [dateFormatter setTimeZone:timeZone];
    
    NSString* dateStr = [dateFormatter  stringFromDate:self];
    return dateStr;
    
}

+(NSDate*) stringToDateWithTimeZone:(NSString*) dateStr TimeZone:(NSTimeZone*) timeZone
{
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    [dateFormatter setTimeZone:timeZone];
    NSDate *date = [dateFormatter dateFromString:dateStr];
    return date;
    
}

+(NSDate*) stringToDateLocalTimeZone:(NSString*) dateStr
{
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    [dateFormatter setTimeZone:[NSTimeZone localTimeZone]];
    NSDate *date = [dateFormatter dateFromString:dateStr];
    return date;
    
}


@end
