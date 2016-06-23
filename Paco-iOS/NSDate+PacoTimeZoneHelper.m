//
//  NSDate+PacoTimeZoneHelper.m
//  Paco
//
//  Created by Timo on 10/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "NSDate+PacoTimeZoneHelper.h"
#import "org/joda/time/DateTime.h"


@implementation NSDate (PacoTimeZoneHelper)


-(NSString*) dateToStringLocalTimezonePrettyPrint
{
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"YYYY, MM-dd HH:mm"];
    
  
    [dateFormatter setTimeZone:[NSTimeZone localTimeZone]];
    
    NSString* dateStr = [dateFormatter  stringFromDate:self];
    return dateStr;
    
}

-(NSString*) toPacoFormatedString
{
    
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat:@"yyyy/MM/dd HH:mm:ssZ"];
     NSString *dateString = [dateFormat stringFromDate:self];
    return dateString;
    
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


+(long)  millisecondsSinceMidnight:(NSDate *)date
{
    NSCalendar *gregorian = [[NSCalendar alloc]
                             initWithCalendarIdentifier:NSGregorianCalendar];
    
    
    
   [gregorian setTimeZone:[NSTimeZone localTimeZone]];
    
    
    unsigned unitFlags =  NSHourCalendarUnit | NSMinuteCalendarUnit | NSCalendarUnitSecond;
    NSDateComponents *components = [gregorian components:unitFlags fromDate:date];
    
    
    
   
    NSTimeZone *timeZone = [NSTimeZone localTimeZone];
    NSString *tzName = [timeZone name];
    
 
    
    int secondsSinceMidnightGMT = ((60 * [components hour] + [components minute])*60 + [components second])*1000;
    
    long  millisecondsFromGMT =  [timeZone secondsFromGMTForDate:date]*1000;
     
    
    long millisecondsFromMidnight = ((60 *  [components hour] + [components minute])*60 + [components second])*1000;
    
    return millisecondsFromMidnight;
}


+(int)  secondsSinceMidnight:(NSDate *)date
{
    NSCalendar *gregorian = [[NSCalendar alloc]
                             initWithCalendarIdentifier:NSGregorianCalendar];
    unsigned unitFlags =  NSHourCalendarUnit | NSMinuteCalendarUnit | NSCalendarUnitSecond;
    NSDateComponents *components = [gregorian components:unitFlags fromDate:date];
    
    return (60 * [components hour] + [components minute])*60 + [components second];
}

+(int)  minutesSinceMidnight:(NSDate *)date
{
    NSCalendar *gregorian = [[NSCalendar alloc]
                             initWithCalendarIdentifier:NSGregorianCalendar];
    unsigned unitFlags =  NSHourCalendarUnit | NSMinuteCalendarUnit;
    
    
    
    
    NSDateComponents *components = [gregorian components:unitFlags fromDate:date];
    
    return 60 * [components hour] + [components minute];
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
