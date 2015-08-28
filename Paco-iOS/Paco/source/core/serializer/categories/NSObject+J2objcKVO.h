//
//  NSObject+J2objcKVO.h
//  Paco
//
//  Created by northropo on 8/5/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#include "java/util/ArrayList.h"


/* extended type information */
typedef NS_ENUM(NSInteger, EncodingEnumType) {
    EncodingTypeLong,
    EncodingTypeLongLong,
    EncodingTypeInt,
    EncodingTypeFloat,
    EncodingTypeDouble,
    EncodingTypeShort,
    EncodingTypeBOOL,
    EncodingTypeClass,
    EncodingTypeJavaUtilArrayList,
    EncodingTypeJavaUtilDate,
    EncodingTypeUnsigndChar,
    EncodingTypeUnsigndInt,
    EncodingTypeUnsigndShort,
    EncodingTypeUnsigndLong,
    EncodingTypeUnsigndLongLong,
    EncodingTypeJavaLangBoolean,
    EncodingTypeJavaLangInteger,
    EncodingTypeJavaLangBigInteger,
    EncodingTypeJavaLangCharacter,
    EncodingTypeJavaLangDouble,
    EncodingTypeJavaLangFloat,
    EncodingTypeJavaLangLong,
    EncodingTypeJavaLangShort,
    EncodingTypeNSString,
    EncodingTypeNotFound
};


@interface NSObject (J2objcKVO)

- (id)valueForKeyEx:(NSString *)key;
- (id)valueForKeyPathEx:(NSString *)keyPath;
- (void)setValueEx:(id)value forKey:(NSString *)key;
- (void)setValueEx:(id)value forKeyPath:(NSString *)keyPath;
- (id)valueForKeyAndIndex:(int)index Key:(NSString *)key;
- (NSArray *)makeCommonAttributeOperationName:(NSString *)attributeName
                                       Object:(NSObject *)object;
- (BOOL)setModalAttribute:(NSString *)attributeName
                   Object:(NSObject *)object
                 Argument:(NSObject *)argument;


/* move these methods into a helper class */
-(NSString*) getSub:(NSString*) ivarType;
- (EncodingEnumType) encodingTypeFromSub:(NSString*) sub;

@end
