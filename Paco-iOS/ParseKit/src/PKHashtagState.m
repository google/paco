//
//  PKHashtagState.m
//  ParseKit
//
//  Created by Todd Ditchendorf on 10/22/11.
//  Copyright 2011 Todd Ditchendorf. All rights reserved.
//

#if PK_PLATFORM_TWITTER_STATE
#if PEGKIT
#import <PEGKit/PKHashtagState.h>
#import <PEGKit/PKReader.h>
#import <PEGKit/PKTokenizer.h>
#import <PEGKit/PKToken.h>
#else
#import <ParseKit/PKHashtagState.h>
#import <ParseKit/PKReader.h>
#import <ParseKit/PKTokenizer.h>
#import <ParseKit/PKToken.h>
#endif

@interface PKToken ()
@property (nonatomic, readwrite) NSUInteger offset;
@end

@interface PKTokenizerState ()
- (void)resetWithReader:(PKReader *)r;
- (void)append:(PKUniChar)c;
- (NSString *)bufferedString;
- (PKTokenizerState *)nextTokenizerStateFor:(PKUniChar)c tokenizer:(PKTokenizer *)t;
@end

@interface PKHashtagState ()

@end

@implementation PKHashtagState

- (id)init {
    self = [super init];
    if (self) {
        
    }
    return self;
}


- (void)dealloc {
    [super dealloc];
}


- (PKToken *)nextTokenFromReader:(PKReader *)r startingWith:(PKUniChar)cin tokenizer:(PKTokenizer *)t {
    NSParameterAssert(r);
    [self resetWithReader:r];
    
    BOOL matched = NO;
    
    [self append:cin]; // '#'
    PKUniChar c = [r read];
    
    while (isdigit(c) || islower(c) || isupper(c) || '_' == c) {
        matched = YES;
        [self append:c];
        c = [r read];
    }
    
    if (PKEOF != c) {
        [r unread];
    }
    
    NSString *s = [self bufferedString];
    if (matched) {
        PKToken *tok = [PKToken tokenWithTokenType:PKTokenTypeHashtag stringValue:s floatValue:0.0];
        tok.offset = offset;
        return tok;
    } else {
        [r unread:[s length] - 1];
        return [[self nextTokenizerStateFor:cin tokenizer:t] nextTokenFromReader:r startingWith:cin tokenizer:t];
    }
}

@end
#endif
