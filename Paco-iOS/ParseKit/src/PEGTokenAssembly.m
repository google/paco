//  Copyright 2010 Todd Ditchendorf
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

#import <PEGKit/PEGTokenAssembly.h>
#import <PEGKit/PKTokenizer.h>
#import <PEGKit/PKToken.h>

@interface PKAssembly ()
@property (nonatomic, readwrite, retain) NSString *defaultCursor;
@end

@interface PEGTokenAssembly ()
- (void)consume:(PKToken *)tok;
@property (nonatomic, retain) PKTokenizer *tokenizer;
@property (nonatomic, retain) NSMutableArray *tokens;
@end

@implementation PEGTokenAssembly

+ (PEGTokenAssembly *)assemblyWithTokenizer:(PKTokenizer *)t {
    return [[[self alloc] initWithTokenzier:t] autorelease];
}


- (id)initWithTokenzier:(PKTokenizer *)t {
    self = [super initWithString:nil];
    if (self) {
        self.tokenizer = t;
#if defined(NDEBUG)
        self.gathersConsumedTokens = NO;
        self.defaultCursor = @"";
#else
        self.gathersConsumedTokens = YES;
#endif
    }
    return self;
}


- (void)dealloc {
    self.tokenizer = nil;
    self.tokens = nil;
    [super dealloc];
}


- (id)copyWithZone:(NSZone *)zone {
    NSAssert2(0, @"%s why are you copying me??? %@", __PRETTY_FUNCTION__, [self class]);
    
    PEGTokenAssembly *a = (PEGTokenAssembly *)[super copyWithZone:zone];
    a->_tokenizer = nil; // optimization
    a->_preservesWhitespaceTokens = _preservesWhitespaceTokens;
    if (_tokens) a->_tokens = [_tokens mutableCopy];
    return a;
}


- (void)consume:(PKToken *)tok {
    if (_preservesWhitespaceTokens || !tok.isWhitespace) {
        [self push:tok];
        ++index;

        if (_gathersConsumedTokens) {
            if (!_tokens) {
                self.tokens = [NSMutableArray array];
            }
            [_tokens addObject:tok];
        }
    }
}


- (id)peek {
    NSAssert1(0, @"cannot call %s", __PRETTY_FUNCTION__);
    return nil;
}


- (id)next {
    NSAssert1(0, @"cannot call %s", __PRETTY_FUNCTION__);
    return nil;
}


- (BOOL)hasMore {
    return YES;
}


- (NSUInteger)length {
    return NSNotFound;
} 


- (NSUInteger)objectsConsumed {
    return index;
}


- (NSUInteger)objectsRemaining {
    return NSNotFound;
}


- (NSString *)consumedObjectsJoinedByString:(NSString *)delimiter {
    NSParameterAssert(delimiter);
    return [self objectsFrom:0 to:self.objectsConsumed separatedBy:delimiter];
}


- (NSString *)remainingObjectsJoinedByString:(NSString *)delimiter {
    NSParameterAssert(delimiter);
    return @"";
}


- (NSString *)lastConsumedObjects:(NSUInteger)len joinedByString:(NSString *)delimiter {
    NSParameterAssert(delimiter);
    
    if (!_gathersConsumedTokens) return @"";

    NSUInteger end = self.objectsConsumed;

    len = MIN(end, len);
    NSUInteger loc = end - len;

    NSAssert(loc < [_tokens count], @"");
    NSAssert(len <= [_tokens count], @"");
    NSAssert(loc + len <= [_tokens count], @"");
    
    NSRange r = NSMakeRange(loc, len);
    NSArray *objs = [_tokens subarrayWithRange:r];
    
    NSString *s = [objs componentsJoinedByString:delimiter];
    return s;
}


#pragma mark -
#pragma mark Private

- (NSString *)objectsFrom:(NSUInteger)start to:(NSUInteger)end separatedBy:(NSString *)delimiter {
    NSParameterAssert(delimiter);
    NSParameterAssert(start <= end);
    
    if (!_gathersConsumedTokens) return @"";

    NSMutableString *s = [NSMutableString string];

    NSParameterAssert(end <= [_tokens count]);

    for (NSInteger i = start; i < end; i++) {
        PKToken *tok = [_tokens objectAtIndex:i];
        if (PKTokenTypeEOF != tok.tokenType) {
            [s appendString:tok.stringValue];
            if (end - 1 != i) {
                [s appendString:delimiter];
            }
        }
    }
    
    return [[s copy] autorelease];
}

@end
