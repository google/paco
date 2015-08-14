//
//  Created by Alex Rudyak on 3/10/15.
//  Copyright (c) 2015 *instinctools. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSMutableDictionary (ITExtension)

- (void)it_appendValue:(id)value forKey:(id)key;
- (void)it_mergeWithDictionary:(NSDictionary *)dictionary;

- (id)it_valueForKey:(id)key;

@property(strong, nonatomic) id it_defaultValue;

@end
