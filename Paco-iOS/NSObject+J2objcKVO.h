//
//  NSObject+J2objcKVO.h
//  Paco
//
//  Created by northropo on 8/5/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#include "java/util/ArrayList.h"
@interface NSObject (J2objcKVO)

- (id)valueForKeyEx:(NSString *)key;
- (id)valueForKeyPathEx:(NSString *)keyPath;
- (void) setValueEx:(id)value forKey:(NSString *)key;
- (void)setValueEx:(id)value forKeyPath:(NSString *)keyPath;
- (id) valueForKeyAndIndex:(int)index  Key:(NSString*) key;

@end







