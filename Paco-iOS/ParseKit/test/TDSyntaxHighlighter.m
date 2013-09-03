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

#import "TDSyntaxHighlighter.h"
#import <ParseKit/ParseKit.h>
#import "PKParserFactory.h"
#import "TDMiniCSSAssembler.h"
#import "TDGenericAssembler.h"

@interface TDSyntaxHighlighter ()
- (NSMutableDictionary *)attributesForGrammarNamed:(NSString *)grammarName;
- (PKParser *)parserForGrammarNamed:(NSString *)grammarName;

// all of the ivars for these properties are lazy loaded in the getters.
// thats so that if an application has syntax highlighting turned off, this class will
// consume much less memory/fewer resources.
@property (nonatomic, retain) PKParserFactory *parserFactory;
@property (nonatomic, retain) PKParser *miniCSSParser;
@property (nonatomic, retain) TDMiniCSSAssembler *miniCSSAssembler;
@property (nonatomic, retain) TDGenericAssembler *genericAssembler;
@property (nonatomic, retain) NSMutableDictionary *parserCache;
@property (nonatomic, retain) NSMutableDictionary *tokenizerCache;
@end

@implementation TDSyntaxHighlighter

- (id)init {
    self = [super init];
    if (self) {

    }
    return self;
}


- (void)dealloc {
    PKReleaseSubparserTree(miniCSSParser);
    for (PKParser *p in parserCache) {
        PKReleaseSubparserTree(p);
    }
    
    self.parserFactory = nil;
    self.miniCSSParser = nil;
    self.miniCSSAssembler = nil;
    self.genericAssembler = nil;
    self.parserCache = nil;
    self.tokenizerCache = nil;
    [super dealloc];
}


- (PKParserFactory *)parserFactory {
    if (!parserFactory) {
        self.parserFactory = [PKParserFactory factory];
    }
    return parserFactory;
}


- (TDMiniCSSAssembler *)miniCSSAssembler {
    if (!miniCSSAssembler) {
        self.miniCSSAssembler = [[[TDMiniCSSAssembler alloc] init] autorelease];
    }
    return miniCSSAssembler;
}


- (PKParser *)miniCSSParser {
    if (!miniCSSParser) {
        // create mini-css parser
        NSString *path = [[NSBundle bundleForClass:[self class]] pathForResource:@"mini_css" ofType:@"grammar"];
        NSString *grammarString = [NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:nil];

        self.miniCSSParser = [self.parserFactory parserFromGrammar:grammarString assembler:self.miniCSSAssembler error:nil];
    } 
    return miniCSSParser;
}


- (TDGenericAssembler *)genericAssembler {
    if (!genericAssembler) {
        self.genericAssembler = [[[TDGenericAssembler alloc] init] autorelease];
    }
    return genericAssembler;
}


- (NSMutableDictionary *)parserCache {
    if (!parserCache) {
        self.parserCache = [NSMutableDictionary dictionary];
    }
    return parserCache;
}


- (NSMutableDictionary *)attributesForGrammarNamed:(NSString *)grammarName {
    // parse CSS
    NSString *path = [[NSBundle bundleForClass:[self class]] pathForResource:grammarName ofType:@"css"];
    NSString *s = [NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:nil];
    PKAssembly *a = [PKTokenAssembly assemblyWithString:s];
    [self.miniCSSParser bestMatchFor:a]; // produce dict of attributes from the CSS
    return self.miniCSSAssembler.attributes;
}


- (PKParser *)parserForGrammarNamed:(NSString *)grammarName {
    // create parser or the grammar requested or fetch parser from cache
    PKParser *parser = nil;
    if (cacheParsers) {
        parser = [self.parserCache objectForKey:grammarName];
    }
    
    if (!parser) {
        // get attributes from css && give to the generic assembler
        parserFactory.assemblerSettingBehavior = PKParserFactoryAssemblerSettingBehaviorAll;
        self.genericAssembler.attributes = [self attributesForGrammarNamed:grammarName];
        
        NSString *path = [[NSBundle bundleForClass:[self class]] pathForResource:grammarName ofType:@"grammar"];
        NSString *grammarString = [NSString stringWithContentsOfFile:path encoding:NSUTF8StringEncoding error:nil];

        // generate a parser for the requested grammar
        parserFactory.assemblerSettingBehavior = PKParserFactoryAssemblerSettingBehaviorTerminals;
        parser = [self.parserFactory parserFromGrammar:grammarString assembler:self.genericAssembler error:nil];
        
        if (cacheParsers) {
            [self.parserCache setObject:parser forKey:grammarName];
            [self.tokenizerCache setObject:parser.tokenizer forKey:grammarName];
        }
    }

    return parser;
}


- (NSAttributedString *)highlightedStringForString:(NSString *)s ofGrammar:(NSString *)grammarName {    
    // create or fetch the parser & tokenizer for this grammar
    PKParser *parser = [self parserForGrammarNamed:grammarName];
    
    // parse the string. take care to preseve the whitespace and comments in the string
    parser.tokenizer.string = s;
    parser.tokenizer.whitespaceState.reportsWhitespaceTokens = YES;
    parser.tokenizer.commentState.reportsCommentTokens = YES;

    PKTokenAssembly *a = [PKTokenAssembly assemblyWithTokenizer:parser.tokenizer];
    a.preservesWhitespaceTokens = YES;
    
    PKAssembly *resultAssembly = [parser completeMatchFor:a]; // finally, parse the input. stores attributed string in resultAssembly.target
    
    if (!cacheParsers) {
        PKReleaseSubparserTree(parser);
    }
    
    id result = [[resultAssembly.target copy] autorelease];
    return result;
}

@synthesize parserFactory;
@synthesize miniCSSParser;
@synthesize miniCSSAssembler;
@synthesize genericAssembler;
@synthesize cacheParsers;
@synthesize parserCache;
@synthesize tokenizerCache;
@end
