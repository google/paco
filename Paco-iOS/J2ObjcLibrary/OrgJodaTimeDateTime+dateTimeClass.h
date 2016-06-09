//
//  OrgJodaTimeDateTime+dateTimeClass.h
//  J2ObjcLibrary
//
//  Copyright © 2016 Northrop O'brien. All rights reserved.
//

#import "DateTime.h"

@interface OrgJodaTimeDateTime (dateTimeClass)

-(BOOL) isGreaterThan:(OrgJodaTimeDateTime*) otherTime;
-(BOOL) isLessThan:(OrgJodaTimeDateTime*) otherTime;

@end
