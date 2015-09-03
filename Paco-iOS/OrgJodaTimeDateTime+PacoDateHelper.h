//
//  OrgJodaTimeDateTime+PacoDateHelper.h
//  Paco
//
//  Created by northropo on 8/31/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "DateTime.h"

@interface OrgJodaTimeDateTime (PacoDateHelper)

-(NSDate*) dateValue;
-(BOOL) isGreaterThan:(OrgJodaTimeDateTime*) otherTime;
-(BOOL) isLessThan:(OrgJodaTimeDateTime*) otherTime;

@end
