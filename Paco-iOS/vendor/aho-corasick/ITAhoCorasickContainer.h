//
//  Created by Alex Rudyak on 3/10/15.
//  Copyright (c) 2015 *instinctools. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ITAhoCorasickContainer : NSObject

/**
 *  Adding pattern string for searching
 *
 *  @param pattern string
 */
- (void)addStringPattern:(NSString *)pattern;

/**
 *  Adding list of patterns for searching
 *
 *  @param patterns collection of strings
 */
- (void)addStringPatterns:(NSArray *)patterns;

/**
 *  Perform searching of patterns in source string
 *
 *  @param source <#source description#>
 *
 *  @return <#return value description#>
 */
- (NSDictionary *)findAllMatches:(NSString *)source;

- (NSArray *)getTestArray;

@end
