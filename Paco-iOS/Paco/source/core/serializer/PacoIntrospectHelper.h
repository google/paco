//
//  PacoIntrospectHelper.h
//  Paco
//
//  Created by northropo on 8/26/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <objc/runtime.h>

@interface PacoIntrospectHelper : NSObject

+(NSArray *) parseIvar:(Ivar) ivar;

@end
