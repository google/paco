//
//  NSArray+PacoPersistence.h
//  Paco
//
//  Created by northropo on 10/14/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSMutableArray (PacoPersistence)

-(void) store:(NSString*) key;
-(void) refreshFromStore:(NSString*) key;


@end
