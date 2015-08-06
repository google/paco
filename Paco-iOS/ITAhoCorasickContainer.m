//
//  Created by Alex Rudyak on 3/10/15.
//  Copyright (c) 2015 *instinctools. All rights reserved.
//

#import "ITAhoCorasickContainer.h"
#import "NSMutableDictionary+ITExtension.h"
#import <Foundation/Foundation.h>

@interface ITBohrVertex : NSObject {
 @public
  NSMutableDictionary *next_vrtx;
  NSMutableDictionary *auto_move;
  int pat_num;
  int suff_link;
  int par;
  int suff_flink;
  bool flag;
  unichar symb;
}

- (instancetype)initWithPat:(int)p
                       char:(unichar)ch
    __attribute__((objc_designated_initializer));
+ (instancetype)bohrVertexWithPat:(int)p char:(unichar)ch;

@end

@interface ITBohr : NSObject {
 @public
  NSMutableOrderedSet *patterns;
  NSMutableArray *bohr;
}

- (void)addString:(NSString *)string;
- (BOOL)isContainsString:(NSString *)pattern;
- (int)autoMoveWithPos:(int)pos char:(unichar)ch;
- (int)sufficientLinkWithPos:(int)pos;
- (int)sufficientFLinkWithPos:(int)pos;
- (NSMutableDictionary *)checkVertexWithPos:(int)v idx:(int)i;
- (NSDictionary *)findMatches:(NSString *)source;

@end

@interface ITAhoCorasickContainer ()

@property(strong, nonatomic) ITBohr *bohr;
@property(strong, nonatomic) NSMutableArray *mArray;

@end

@implementation ITAhoCorasickContainer

- (instancetype)init {
  self = [super init];
  if (self) {
    self.bohr = [[ITBohr alloc] init];
    _mArray = [NSMutableArray new];
    ;
  }
  return self;
}

- (void)addStringPattern:(NSString *)pattern {
  [self.mArray addObject:pattern];
  [self.bohr addString:pattern];
}

- (NSArray *)getTestArray {
  return self.mArray;
}

- (void)addStringPatterns:(NSArray *)patterns {
  for (NSString *pattern in patterns) {
    [self.bohr addString:pattern];
  }
}

- (NSDictionary *)findAllMatches:(NSString *)source {
  return [self.bohr findMatches:source];
}

@end

@implementation ITBohrVertex

- (instancetype)initWithPat:(int)p char:(unichar)ch {
  self = [super init];
  if (self) {
    next_vrtx = [NSMutableDictionary dictionary];
    next_vrtx.it_defaultValue = @(-1);
    auto_move = [NSMutableDictionary dictionary];
    auto_move.it_defaultValue = @(-1);
    flag = NO;
    suff_link = -1;
    par = p;
    symb = ch;
    suff_flink = -1;
  }

  return self;
}

+ (instancetype)bohrVertexWithPat:(int)p char:(unichar)ch {
  return [[ITBohrVertex alloc] initWithPat:p char:ch];
}

@end

@implementation ITBohr

- (instancetype)init {
  self = [super init];
  if (self) {
    patterns = [NSMutableOrderedSet orderedSet];
    bohr = [NSMutableArray array];

    [bohr addObject:[ITBohrVertex bohrVertexWithPat:0 char:'$']];
  }
  return self;
}

- (void)addString:(NSString *)string {
  if ([patterns containsObject:string]) {
    return;
  }

  int num = 0;
  ITBohrVertex *v;
  for (int i = 0; i < [string length]; i++) {
    id ch = @([string characterAtIndex:i]);
    v = bohr[num];
    if ([[v->next_vrtx it_valueForKey:ch] intValue] == -1) {
      [bohr addObject:[ITBohrVertex bohrVertexWithPat:num char:[ch intValue]]];
      v->next_vrtx[ch] = @([bohr count] - 1);
    }
    num = [[v->next_vrtx it_valueForKey:ch] intValue];
  }
  v = bohr[num];
  v->flag = true;
  [patterns addObject:string];
  v->pat_num = (int)[patterns count] - 1;
}

- (BOOL)isContainsString:(NSString *)pattern {
  int num = 0;
  for (uint i = 0; i < pattern.length; i++) {
    id ch = @([pattern characterAtIndex:i]);
    ITBohrVertex *v = bohr[num];
    if ([[v->next_vrtx it_valueForKey:ch] intValue] == -1) {
      return NO;
    }
    num = [[v->next_vrtx it_valueForKey:ch] intValue];
  }
  return YES;
}

- (int)autoMoveWithPos:(int)pos char:(unichar)ch {
  id c = @(ch);
  ITBohrVertex *v = bohr[pos];

  if ([[v->auto_move it_valueForKey:c] intValue] == -1) {
    if ([[v->next_vrtx it_valueForKey:c] intValue] != -1) {
      v->auto_move[c] = [v->next_vrtx it_valueForKey:c];
    } else {
      if (pos == 0) {
        v->auto_move[c] = @(0);
      } else {
        v->auto_move[c] =
            @([self autoMoveWithPos:[self sufficientLinkWithPos:pos] char:ch]);
      }
    }
  }

  return [[v->auto_move it_valueForKey:c] intValue];
}

- (int)sufficientLinkWithPos:(int)pos {
  ITBohrVertex *v = bohr[pos];
  if (v->suff_link == -1) {
    if (pos == 0 || v->par == 0) {
      v->suff_link = 0;
    } else {
      v->suff_link = [self autoMoveWithPos:[self sufficientLinkWithPos:v->par]
                                      char:v->symb];
    }
  }
  return v->suff_link;
}

- (int)sufficientFLinkWithPos:(int)pos {
  ITBohrVertex *v = bohr[pos];
  if (v->suff_flink == -1) {
    int u = [self sufficientLinkWithPos:pos];
    if (u == 0) {
      v->suff_flink = 0;
    } else {
      v->suff_flink = v->flag ? u : [self sufficientFLinkWithPos:u];
    }
  }
  return v->suff_flink;
}

- (NSMutableDictionary *)checkVertexWithPos:(int)pos idx:(int)i {
  NSMutableDictionary *result = [NSMutableDictionary dictionary];
  ITBohrVertex *v;
  for (int u = pos; u != 0; u = [self sufficientFLinkWithPos:u]) {
    v = bohr[u];
    if (v->flag) {
      int foundPosition = i - (int)[patterns[v->pat_num] length];
      NSString *foundPattern = patterns[v->pat_num];
      NSValue *matchRange = [NSValue
          valueWithRange:NSMakeRange(foundPosition, [foundPattern length])];
      [result it_appendValue:matchRange forKey:foundPattern];
    }
  }

  return result;
}

- (NSDictionary *)findMatches:(NSString *)source {
  NSMutableDictionary *result = [NSMutableDictionary dictionary];
  int u = 0;
  for (int i = 0; i < [source length]; i++) {
    u = [self autoMoveWithPos:u char:([source characterAtIndex:i])];
    [result it_mergeWithDictionary:[self checkVertexWithPos:u idx:(i + 1)]];
  }

  return [NSDictionary dictionaryWithDictionary:result];
}

@end
