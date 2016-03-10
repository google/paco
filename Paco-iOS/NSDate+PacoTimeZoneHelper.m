//
//  NSDate+PacoTimeZoneHelper.m
//  Paco
//
//  Created by Timo on 10/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "NSDate+PacoTimeZoneHelper.h"
#import "DateTime.h"

@implementation NSDate (PacoTimeZoneHelper)


-(NSString*) dateToStringLocalTimezonePrettyPrint
{
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"YYYY, MM-dd HH:mm"];
    
  
    [dateFormatter setTimeZone:[NSTimeZone localTimeZone]];
    
    NSString* dateStr = [dateFormatter  stringFromDate:self];
    return dateStr;
    
}


-(NSString*) dateToStringLocalTimezone
{
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss"];
    [dateFormatter setTimeZone:[NSTimeZone localTimeZone]];
    
    NSString* dateStr = [dateFormatter  stringFromDate:self];
    return dateStr;
    
}

-(NSString*) dateToStringWithTimeZone:(NSTimeZone*) timeZone
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

-(OrgJodaTimeDateTime*) joda
{

    NSTimeInterval ll = [self timeIntervalSince1970];
    OrgJodaTimeDateTime* jud = [[OrgJodaTimeDateTime alloc] initWithLong:ll*1000];
    return jud;
    
 
}

+(OrgJodaTimeDateTime*) jodaFromString:(NSString*) dateStr
{
    NSDate* date = [NSDate stringToDateLocalTimeZone:dateStr];
    return [date joda];
    
}


@end
