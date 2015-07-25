//
//  PacoPacoSerializer.m
//  Paco
//
//  Created by northropo on 7/23/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoSerializer.h"
#import <Foundation/Foundation.h>
#import <objc/runtime.h>

#define MATCHER_SUCCESS_RATIO .4


@implementation PacoSerializer



- (instancetype)initWithArrayOfClasses:(NSArray*) classes
{
    self = [super init];
    if (self) {
        
        _classes = classes;
    }
    return self;
}


/*
    this method should be executed on the results of a json serializer. 
    parses the colection tree in order invoke the block with PacoParserType events.
 
 
 */

-(void)  recurseJason:(id ) recurseObject Level:(int) level Block:( void   ( ^ )( id  data , PacoParserType type) ) block
{
    if( [recurseObject isKindOfClass:[NSDictionary class]]  )
    {
        NSString* matchedClass = [self matchClassForDictionary:recurseObject];
        block(@{matchedClass:recurseObject}, kInputTypeNewDictionary);
    
        NSArray * arrayOfKeys = [recurseObject allKeys];
        for( NSString* key in arrayOfKeys )
        {
            id  newObject = [recurseObject objectForKey:key];
            block(@{key:newObject},kInputTypeDictionaryEntry);
            [self recurseJason:newObject Level:level+1  Block:block];
            
        }
        
        block(recurseObject,kInputTypeEndNewDictionary);
        
    }
    else  if( [recurseObject isKindOfClass:[NSArray class]]  )
    {
        block(recurseObject,kInputTypeStartArray);
        for( NSObject* obj in recurseObject )
        {
             block(obj ,kInputTypeArraytEntry);
             [self recurseJason:obj Level:level+1  Block:block];
        }
        
        block(recurseObject,kInputTypeEndArray);
    }
 
    
}


/*
     loops over knonwn classes and checks if the iVars of the class match the keyd of the dictionary
      brut force will be replaced with a more elegant method later.
 
 */

-(NSString*) matchClassForDictionary:(NSDictionary*) dictionary
{
    NSString* returnValue = @"No Match";
    
    for(NSString* className in _classes)
    {
        
        NSString* withPrefix = [NSString stringWithFormat:@"PA%@",className];
        if( [self matchesClass:dictionary ClassName:withPrefix])
        {
            
            returnValue = className;
            
        }
        
    }
    
    return returnValue;
    
}

/*
 
   brut force compars the keys in the dictioanry with the ivar of a class.
   returns true if match ratio is larger than MATCHER_SUCCESS_RATIO
 */
-(BOOL) matchesClass:(NSDictionary*) dictionary ClassName:(NSString*) className
{
    
    NSArray * array = [dictionary allKeys];
    NSMutableArray * noArray = [NSMutableArray new];
    NSMutableArray * yesArray = [NSMutableArray new ];
    
    Class theClass = NSClassFromString(className);
    id object = [[theClass alloc] init];
    
    
    NSArray* resultsArray =   [self arrayOfIvarsFromInstance:object];
    
    NSLog(@" filename %@", className);
    
    for(NSString* string  in array)
    {
        NSString * str =  [NSString stringWithFormat:@"%@_",string];
        if([resultsArray  containsObject:str])
        {
            
           // [object setValue:[dictionary objectForKey:string]   forKeyPath:string];
            
            [yesArray addObject:str];
        }
        else{
            
            [noArray addObject:str];
            
            
            
        }
        
    }
    
    float  successRatio =  (float) [yesArray count]/[array count];
    return (successRatio> MATCHER_SUCCESS_RATIO);
    
    
}


/*
 
   get an array of ivars from a class instance
 
 */

-(NSArray*) arrayOfIvarsFromInstance:(id) object
{
    NSMutableArray  * resultsArray = [NSMutableArray  new];
    
    unsigned int numIvars = 0;
    Ivar * ivars = class_copyIvarList([object class], &numIvars);
    NSLog(@" number of ivars %i",numIvars ) ;
    
    
    for (int i = 0; i < numIvars; ++i) {
        
        Ivar ivar = ivars[i];
        NSString * ivarName = [NSString stringWithCString:ivar_getName(ivar) encoding:NSUTF8StringEncoding];
        [resultsArray addObject:ivarName];
        NSLog(@"ivarName %@ %s", ivarName, ivar_getTypeEncoding(ivar)) ;
        
    }
    
    
    Class  superclass = class_getSuperclass([object class] );
    
    id superclassObject = [[superclass alloc] init];
    
    if( [superclassObject isKindOfClass:[NSObject class]] )
    {
         [resultsArray addObjectsFromArray:[self arrayOfIvarsFromInstance:superclass]];
    }
    
    
    free(ivars);
    return resultsArray;
}


@end
