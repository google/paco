//
//  NSNumber+PacoJ2OBCType.h
//  Paco
//
//  Created by Timo on 10/7/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "java/lang/Long.h"


@interface NSNumber (PacoJ2OBCType)

-(JavaLangLong*) toJLL;

@end
