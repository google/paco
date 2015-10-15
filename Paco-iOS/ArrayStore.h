//
//  ArrayStore.h
//  Paco
//
//  Created by northropo on 10/13/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ArrayStore : NSObject


-(void) updateOrInsert:(NSString*) recordType   Array:(NSArray*) array;
-(NSArray*) fetchArray:(NSString*) key;
-(void) deleteArray:(NSString*) key;



@end
