//
//  GenericArray.h
//  Paco
//
//  Created by Timo on 10/13/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface GenericArray : NSManagedObject

@property (nonatomic, retain) NSString * type;
@property (nonatomic, retain) NSData * blob;

@end
