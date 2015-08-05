//
//  NSObject+ITNDescription.m
//  Jukebox
//
//  Created by Martin Kiss on 29.1.14.
//  Copyright (c) 2014 iAdverti. All rights reserved.
//
#import <Foundation/Foundation.h>


#import <stdlib.h>
#import <string.h>
#import "NSObject+ITNDescription.h"
#import <Foundation/Foundation.h>
#import <objc/runtime.h>




@implementation NSObject (ITNDescription)


- (NSString *)itn_shortDescription {
    return [self itn_descriptionWithIndentation:0 depth:0];
}


- (NSString *)itn_longDescription {
    return [self itn_descriptionWithIndentation:0 depth:2];
}


- (NSString *)itn {
    return [self itn_longDescription];
}


- (NSString *)itn_descriptionWithDepth:(NSUInteger)depth {
    return [self itn_descriptionWithIndentation:0 depth:depth];
}


- (NSString *)itn_descriptionWithIndentation:(NSUInteger)indent depth:(NSUInteger)depth {
    //TODO: Shorten these default descriptions.
    
    if ([self itn_overridesSelector:@selector(debugDescription)]) {
        // Use overriden debugDescription.
        return [self debugDescription];
    }
    else if ([self itn_overridesSelector:@selector(description)]) {
        // Use overriden description.
        return [self description];
    }
    else {
        // Continue with basic implementation.
    }
    
    NSMutableString *d = [NSMutableString stringWithFormat:@"<%@: %p>", self.class, self];
    if (depth == 0) return d; // Short description.
    
    [d appendString:@" {"];
    NSUInteger numberOfEntries = 0;
    for (Class class in [self itn_classes]) {
        unsigned int count = 0;
        objc_property_t *properties = class_copyPropertyList(class, &count);
        for (unsigned int i = 0; i < count; i++) {
            objc_property_t property = properties[i];
            
            char *ivar = property_copyAttributeValue(property, "V");
            if (ivar && strlen(ivar)) {
                if (numberOfEntries == 0) [d appendString:@"\n"];
                
                numberOfEntries ++;
                
                for (NSUInteger i = 0; i < indent + ITN_INDENT; i++) [d appendString:@" "];
                
                NSString *key = @(property_getName(property));
                id value = [self valueForKey:key];
                NSString *subdescription = @"nil";
                if (value) {
                    subdescription = [value itn_descriptionWithIndentation:(indent + ITN_INDENT) depth:(depth - 1)];
                }
                if ( ! subdescription.length) {
                    subdescription = @"(no description)";
                }
                [d appendFormat:@"%s = %@,\n", ivar, subdescription];
            }
            free(ivar);
        }
        free(properties);
    }
    if (numberOfEntries > 0) {
        for (NSUInteger i = 0; i < indent; i++) [d appendString:@" "];
    }
    [d appendString:@"}"];
    return d;
}


- (NSArray *)itn_classes {
    NSMutableArray *classes = [[NSMutableArray alloc] init];
    Class class = self.class;
    while (class) {
        [classes insertObject:class atIndex:0];
        class = [class superclass];
    }
    return classes;
}


- (BOOL)itn_overridesSelector:(SEL)selector {
    Method baseMethod = class_getInstanceMethod([NSObject class], selector);
    Method implementedMethod = class_getInstanceMethod([self class], selector);
    return (baseMethod != implementedMethod);
}





@end





@implementation NSString (ITNDescription)

- (NSString *)itn_descriptionWithIndentation:(NSUInteger)indent depth:(NSUInteger)depth {
    return [NSString stringWithFormat:@"“%@”", self];
}


@end





@implementation NSNumber (ITNDescription)


- (NSString *)itn_descriptionWithIndentation:(NSUInteger)indent depth:(NSUInteger)depth {
    if (self == (id)kCFBooleanTrue
        || self == (id)kCFBooleanFalse) {
        return ([self boolValue]? @"Yes" : @"No");
    }
    else return [self description];
}


@end





@implementation NSArray (ITNDescription)


- (NSString *)itn_descriptionWithIndentation:(NSUInteger)indent depth:(NSUInteger)depth {
    NSMutableString *d = [NSMutableString stringWithFormat:@"%lu object%@", (unsigned long)self.count, (self.count == 1? @"" : @"s")];
    if (depth == 0 || self.count == 0) return d; // Short description.
    
    [d appendString:@" (\n"];
    
    for (id value in self) {
        for (NSUInteger i = 0; i < indent + ITN_INDENT; i++) [d appendString:@" "];
        
        NSString *subdescription = [value itn_descriptionWithIndentation:(indent + ITN_INDENT) depth:depth - 1];
        if ( ! subdescription.length) {
            subdescription = @"(no description)";
        }
        [d appendFormat:@"%@,\n", subdescription];
    }
    for (NSUInteger i = 0; i < indent; i++) [d appendString:@" "];
    [d appendString:@")"];
    return d;
}


@end





@implementation NSDictionary (ITNDescription)


- (NSString *)itn_descriptionWithIndentation:(NSUInteger)indent depth:(NSUInteger)depth {
    NSMutableString *d = [NSMutableString stringWithFormat:@"%lu pair%@", (unsigned long)self.count, (self.count == 1? @"" : @"s")];
    if (depth == 0 || self.count == 0) return d; // Short description.
    
    [d appendString:@" {\n"];
    
    for (id key in self) {
        for (NSUInteger i = 0; i < indent + ITN_INDENT; i++) [d appendString:@" "];
        
        NSString *keyDescription = [key itn_descriptionWithIndentation:(indent + ITN_INDENT) depth:0];
        if ( ! keyDescription.length) {
            keyDescription = @"(no description)";
        }
        id value = [self objectForKey:key];
        NSString *valueDescription = [value itn_descriptionWithIndentation:(indent + ITN_INDENT) depth:(depth - 1)];
        if ( ! valueDescription.length) {
            valueDescription = @"(no description)";
        }
        [d appendFormat:@"%@ = %@,\n", keyDescription, valueDescription];
    }
    for (NSUInteger i = 0; i < indent; i++) [d appendString:@" "];
    [d appendString:@"}"];
    return d;
}


@end




