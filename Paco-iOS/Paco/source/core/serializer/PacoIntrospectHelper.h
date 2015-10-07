//
//  PacoIntrospectHelper.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/26/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <objc/runtime.h>

@interface PacoIntrospectHelper : NSObject

+(NSArray *) parseIvar:(Ivar) ivar Parent:(NSObject*) parent;

@end
