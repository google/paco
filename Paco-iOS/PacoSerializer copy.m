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
#include "java/util/HashMap.h"
#include "java/util/ArrayList.h"

/* number of matched ivars / total number of ivars */
#define PACO_MATCHER_SUCCESS_RATIO .4
#define PACO_LIST_PARENT  @"LISTPARENTPACPO"
#define PACO_OBJECT_NO_MATCH @"NO MATCH"

@implementation PacoSerializer



/*
     instantiae with collection heirarchy
 
 */
- (instancetype)initWithArrayOfClasses:(NSArray*) classes
{
    self = [super init];
    if (self) {
        
        _classes =  classes;
        _objectTracking = [NSMutableArray new];
    }
    return self;
}


/*
 
    parses the colection tree in order, building the model tree. 
 
 
 */

-(void)  recurseJason:(id ) recurseObject Block:( void   ( ^ )(id   data , PacoParserType type) ) block
{
    
  
  
    
    if( [recurseObject[1] isKindOfClass:[NSDictionary class]]  )
    {
      
        
        /*
            1) is this dictionary a model or a bona fide dictionary. 
            2) is the object that holds this dictionary or was this dictionary found in an array.
         
         
         */
        
       
        
        /* answer (1) and (2) above
         
         i)  look at parent item to see if recurseObject[0] is an object and fetch the class type. limited but performent.
         ii) fetch the class type by comparing the attributes with the ivars of classes. handles the general case but not performent.
         
       */
        
        
         
        NSString* matchedClass = [self matchClassForDictionary:recurseObject[1]];
       block(@{matchedClass: recurseObject[1] }, kInputTypeNewDictionary);
        
        Class theClass = NSClassFromString(matchedClass);
        id object = [[theClass alloc] init];
        
        
      
        
        if(![matchedClass isEqualToString:PACO_OBJECT_NO_MATCH ])
        {
           NSLog(@"push object %@ for key %@",matchedClass, recurseObject[0]);
          [self push:object];
        }
        
        

    
        NSArray * arrayOfKeys = [recurseObject[1]  allKeys];
        for( NSString* key in arrayOfKeys )
        {
            id  newObject = [recurseObject[1] objectForKey:key];
           
           // block(@{key:newObject},kInputTypeDictionaryEntry);
            
            if(![matchedClass isEqualToString:PACO_OBJECT_NO_MATCH ])
            {
               [self recurseJason:@[key,newObject]   Block:block];
            }
            
        }
        
         NSLog(@"pop  object %@ for key %@",matchedClass, recurseObject[0]);
        
        if(![matchedClass isEqualToString:PACO_OBJECT_NO_MATCH ])
        {
            [self pop];
        }
       // block(recurseObject,kInputTypeEndNewDictionary);
        
    }
    
    
    
    else  if( [recurseObject[1]  isKindOfClass:[NSArray class]]  )
    {
   
        
        
        JavaUtilArrayList * arrayList = [[ JavaUtilArrayList alloc] initWithInt:20];
 
        
         NSLog(@"push array for key %@",recurseObject[0]);
          [self push:arrayList];
        
        /*
         
          case this is a list attribute.
         */
        
        if([recurseObject[0] isEqualToString:PACO_LIST_PARENT])
        {
            
            
            NSLog(@"is child element");
            
        }
        
        
        
     
        
        
        //block(recurseObject,kInputTypeStartArray);
        
        for( NSObject* obj in recurseObject[1] )
        {
           //  block(@{recurseObject[0]:obj} ,kInputTypeArraytEntry);
             [self recurseJason:@[recurseObject[0]  ,obj]   Block:block];
        }
        NSLog(@"pop  array for key %@",recurseObject[0]);
        [self pop];
       // block(recurseObject, kInputTypeEndArray);
    }

   
}


/*
     loops over knonwn classes and checks if the iVars of the class match the keyd of the dictionary
      brut force will be replaced with a more elegant method later.
 
 */

-(NSString*) matchClassForDictionary:(NSDictionary*) dictionary
{
    NSString* returnValue = PACO_OBJECT_NO_MATCH;
    
    for(NSString* className in _classes)
    {
        
        NSString* withPrefix = [NSString stringWithFormat:@"PA%@",className];
        if( [self matchesClass:dictionary ClassName:withPrefix])
        {
            
            returnValue = withPrefix;
            
        }
        
    }
    
    
  // NSLog(@" class name %@", returnValue);
    
    
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
    
  //  NSLog(@" Class Name %@", className);
    
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
    return (successRatio> PACO_MATCHER_SUCCESS_RATIO);
    
    
}


/*
 set
 */

/*
Ivar ivar = class_getInstanceVariable([self class], "_rate");
((void (*)(id, Ivar, CGFloat))object_setIvar)(self, ivar, rate);
 */

/*
 get
 */
/*
ptrdiff_t offset = ivar_getOffset(ivar);
unsigned char* bytes = (unsigned char *)(__bridge void*)self;
CGFloat floatValue = *((CGFloat *)(bytes+offset));
 */





-(NSArray*)  fetchInfo:(id) object
{
    
    NSMutableArray  * resultsArray = [NSMutableArray  new];
    
    unsigned int numIvars = 0;
    Ivar * ivars = class_copyIvarList([object class], &numIvars);
    
    for (int i = 0; i < numIvars; ++i) {
        
         Ivar ivar = ivars[i];
         NSString * ivarName = [NSString stringWithCString:ivar_getName(ivar) encoding:NSUTF8StringEncoding];
         NSString * ivarType = [NSString stringWithCString:ivar_getTypeEncoding(ivar)  encoding:NSUTF8StringEncoding];
    
        
       // NSLog(@"ivarName %@ %s", ivarName, ivar_getTypeEncoding(ivar)) ;
        
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


/*
 
   get an array of ivars from a class instance
 
 */

-(NSArray*) arrayOfIvarsFromInstance:(id) object
{
    NSMutableArray  * resultsArray = [NSMutableArray  new];
    
    unsigned int numIvars = 0;
    Ivar * ivars = class_copyIvarList([object class], &numIvars);
    
    for (int i = 0; i < numIvars; ++i) {
        
        Ivar ivar = ivars[i];
        NSString * ivarName = [NSString stringWithCString:ivar_getName(ivar) encoding:NSUTF8StringEncoding];
        [resultsArray addObject:ivarName];
      //  NSLog(@"ivarName %@ %s", ivarName, ivar_getTypeEncoding(ivar)) ;
        
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

#pragma mark - Stack Methods


- (void) push: (id)item {
    
  
     [_objectTracking addObject:item];
}


-(NSObject*) parent
{
    return  [self peek];
}

- (id) pop {
  
    id item = nil;
    if ([_objectTracking  count] != 0) {
        item = [_objectTracking  lastObject];
        [_objectTracking removeLastObject];
    }
    return item;
}

- (id) peek {
    id item = nil;
    if ([_objectTracking  count] != 0) {
        item =  [_objectTracking lastObject];
    }
    return item;
}

- (void) replaceTop: (id)item {
    if ([_objectTracking count] == 0) {
        [_objectTracking addObject:item];
    } else {
        [_objectTracking removeLastObject];
        [_objectTracking addObject:item];
    }
}


@end
