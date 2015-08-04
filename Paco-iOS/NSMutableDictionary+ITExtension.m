//
//  Created by Alex Rudyak on 3/10/15.
//  Copyright (c) 2015 *instinctools. All rights reserved.
//

#import "NSMutableDictionary+ITExtension.h"
#import <objc/runtime.h>

static char *const kITDefaultValueKey = "ITDefaultValueKey";

@implementation NSMutableDictionary (ITExtension)

- (void)it_appendValue:(id)value forKey:(id)key
{
    NSAssert([self[key] isKindOfClass:[NSMutableArray class]] || !self[key], @"Method may only be applied for values of NSMutableArray class");
    
    NSMutableArray *arr = self[key];
    if (!arr) {
        arr = [NSMutableArray array];
    }
    
    if ([value isKindOfClass:[NSArray class]] ||
        [value isKindOfClass:[NSMutableArray class]]) {
        [arr addObjectsFromArray:value];
    } else {
        [arr addObject:value];
    }
    
    self[key] = arr;
}

- (void)it_mergeWithDictionary:(NSDictionary *)dictionary
{
    for (id key in [dictionary keyEnumerator]) {
        [self it_appendValue:dictionary[key] forKey:key];
    }
}

- (id)it_valueForKey:(id)key
{
    if (self.it_defaultValue && !self[key]) {
        return self.it_defaultValue;
    }
    
    return self[key];
}

- (void)setIt_defaultValue:(id)it_defaultValue
{
    objc_setAssociatedObject(self, kITDefaultValueKey, it_defaultValue, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (id)it_defaultValue
{
    return objc_getAssociatedObject(self, kITDefaultValueKey);
}

@end
