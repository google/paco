//
//  NSDate+PacoTimeZoneHelper.h
//  Paco
//
//  Created by northropo on 10/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSDate (PacoTimeZoneHelper)

+(NSDate*) dateToStringLocalTimeZone:(NSString*) dateStr;
+(NSDate*) dateToStringWithTimeZone:(NSString*) dateStr TimeZone:(NSTimeZone*) timeZone;
-(NSString*) dateToStringLocalTimezone:(NSTimeZone*) timeZone;
-(NSString*) dateToStringLocalTimezone;

@end
