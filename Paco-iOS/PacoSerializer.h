//
//  PacoPacoSerializer.h
//  Paco
//
//  Created by Timothy  Northrop O'Brien on 7/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>


typedef NS_ENUM(NSInteger, PacoParserType) {
    kInputTypeArraytEntry,
    kInputTypeDictionaryEntry,
    kInputTypeNewDictionary,
    kInputTypeEndNewDictionary,
    kInputTypeStartArray,
    kInputTypeEndArray,
};

@interface PacoSerializer : NSObject


@property (nonatomic,strong) NSArray* classes;

- (id) init __attribute__((unavailable("init with array of class names")));
- (instancetype)initWithArrayOfClasses:(NSArray*) classes;
-(void)  recurseJason:(id ) recurseObject Level:(int) level Block:( void   ( ^ )( id  data , PacoParserType type) ) block;



@end
