//
//  NSObject+ITNDescription.h
//  Jukebox
//
//  Created by Martin Kiss on 29.1.14.
//  Copyright (c) 2014 iAdverti. All rights reserved.
//

 #import <Foundation/Foundation.h>


#define ITN_INDENT  4





@interface NSObject (ITNDescription)


- (NSString *)itn_shortDescription;
- (NSString *)itn_longDescription;
- (NSString *)itn;

- (NSString *)itn_descriptionWithDepth:(NSUInteger)depth;

- (NSString *)itn_descriptionWithIndentation:(NSUInteger)indent depth:(NSUInteger)depth;


@end


