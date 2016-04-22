//
//  NSDate+PacoTimeZoneHelper.h
//  Paco
//
//  Created by Timo on 10/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DateTime.h"


@interface NSDate (PacoTimeZoneHelper)



+(NSString*) dateToStringLocalTimezonePrettyPrint;
+(NSDate*) stringToDateLocalTimeZone:(NSString*) dateStr;
+(NSDate*) stringToDateWithTimeZone:(NSString*) dateStr TimeZone:(NSTimeZone*) timeZone;
-(NSString*) dateToStringWithTimezone:(NSTimeZone*) timeZone;
-(NSString*) dateToStringLocalTimezone;
+(int)  minutesSinceMidnight:(NSDate *)date;
+(long)  millisecondsSinceMidnight:(NSDate *)date;
+(int)  secondsSinceMidnight:(NSDate *)date;

-(OrgJodaTimeDateTime*) joda;
+(OrgJodaTimeDateTime*) jodaFromString:(NSString*) dateStr;
@end
