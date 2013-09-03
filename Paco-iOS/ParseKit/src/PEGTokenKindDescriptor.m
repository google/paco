//
//  PEGTokenKindDescriptor.m
//  PEGKit
//
//  Created by Todd Ditchendorf on 3/27/13.
//
//

#import "PEGTokenKindDescriptor.h"
#import <PEGKit/PEGParser.h>

static NSMutableDictionary *sCache = nil;

@implementation PEGTokenKindDescriptor

+ (void)initialize {
    if ([PEGTokenKindDescriptor class] == self) {
        sCache = [[NSMutableDictionary alloc] init];
    }
}


+ (PEGTokenKindDescriptor *)descriptorWithStringValue:(NSString *)s name:(NSString *)name {
    NSParameterAssert(s);
    NSParameterAssert(name);
    
    PEGTokenKindDescriptor *desc = sCache[name];
    
    if (!desc) {
        desc = [[[PEGTokenKindDescriptor alloc] init] autorelease];
        desc.stringValue = s;
        desc.name = name;
        
        sCache[name] = desc;
    }
    
    return desc;
}


+ (PEGTokenKindDescriptor *)anyDescriptor {
    return [PEGTokenKindDescriptor descriptorWithStringValue:@"TOKEN_KIND_BUILTIN_ANY" name:@"TOKEN_KIND_BUILTIN_ANY"];
}


+ (PEGTokenKindDescriptor *)eofDescriptor {
    return [PEGTokenKindDescriptor descriptorWithStringValue:@"TOKEN_KIND_BUILTIN_EOR" name:@"TOKEN_KIND_BUILTIN_EOF"];
}


+ (void)clearCache {
    [sCache removeAllObjects];
}


- (void)dealloc {
    self.stringValue = nil;
    self.name = nil;
    [super dealloc];
}


- (NSString *)description {
    return [NSString stringWithFormat:@"<%@ %p '%@' %@>", [self class], self, _stringValue, _name];
}


- (BOOL)isEqual:(id)obj {
    if (![obj isMemberOfClass:[self class]]) {
        return NO;
    }
    
    PEGTokenKindDescriptor *that = (PEGTokenKindDescriptor *)obj;
    
    if (![_stringValue isEqualToString:that->_stringValue]) {
        return NO;
    }
    
    NSAssert([_name isEqualToString:that->_name], @"if the stringValues match, so should the names");
    
    return YES;
}

@end
