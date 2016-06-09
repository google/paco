//
//  OrgJodaTimeDateTime+dateTimeClass.h
//  J2ObjcLibrary
//
//  Created by Northrop O'brien on 4/27/16.
//  Copyright © 2016 Northrop O'brien. All rights reserved.
//

#import "DateTime.h"

@interface OrgJodaTimeDateTime (dateTimeClass)

-(BOOL) isGreaterThan:(OrgJodaTimeDateTime*) otherTime;
-(BOOL) isLessThan:(OrgJodaTimeDateTime*) otherTime;

@end
