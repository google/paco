//
//  NSNumber+PacoJ2OBCType.m
//  Paco
//
//  Created by northropo on 10/7/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "NSNumber+PacoJ2OBCType.h"
#import "java/lang/Long.h"

@implementation NSNumber (PacoJ2OBCType)


-(JavaLangLong*) toJLL
{
    return   [JavaLangLong valueOfWithLong:[self longValue]];
}
            
            
            
            
            
@end
