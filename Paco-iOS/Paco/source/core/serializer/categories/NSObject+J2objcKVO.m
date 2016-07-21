//
//  NSObject+J2objcKVO.m
//  Paco
//
//  Created by Tim timothy obrien  on 8/5/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

/*

     ToDo:
     1) bounds checking for java array
 */

#import "NSObject+J2objcKVO.h"
#import "ModelBase.h"
#import <objc/runtime.h>
#include "java/util/ArrayList.h"
#include "java/util/Iterator.h"
#include "java/lang/Boolean.h"
#include "java/lang/Long.h"
#include "java/lang/Integer.h"
#include "java/lang/Float.h"
#include "java/lang/Double.h"
#include "java/lang/Boolean.h"
#include "java/lang/Short.h"
#include "java/lang/Character.h"
#include "J2ObjC_header.h"

@implementation NSObject (J2objcKVO)

#pragma mark - key methods
- (void)setValueEx:(id)value forKey:(NSString *)key {
  @try {
    if ([self isJ2Objc]) {
      [self setModelAttribute:key Object:self Argument:value];
    } else {
        
        
        /* for some reason an '_' is added to non j2OBJC object attributes when persiting to the database. 
           idealy we would like to trim the '_' when writing to the database. For now lets here and fix later. 
         */
       
        if ([key hasPrefix:@"_"] && [key length] > 1) {
            key = [key substringFromIndex:1];
        }
        
      [self setValue:value forKey:key];
    }
  } @catch (NSException *exception) {
    NSLog(@"error excoeption %@", exception.reason);
  }
}

- (id)valueForKeyEx:(NSString *)key {
  id retVal = nil;

  @try {
    if ([self isJ2Objc]) {
      if ([self isIndexed:key]) {
        int index = [self getIndex:key];
        retVal = [self valueForKeyAndIndex:index Key:key];
      }
      else if ([self isCount:key])
      {
          if ( [ [self valueForKey:[self trimCount:key]]  isKindOfClass:[JavaUtilArrayList class]])
          {
              JavaUtilArrayList*   list =  [self valueForKeyEx:[self trimCount:key]];
              
              
              //id<JavaUtilIterator>) terator = list.ite
              
              id<JavaUtilIterator> iter = list.iterator;
              
              int count =[list size];
              
              while([iter hasNext])
              {
                 NSObject*  obj =  [iter next];
                  
                  NSString* name = [obj valueForKeyEx:@"name"];
                  count++;
    
              }
              
              
              
              int arraySize = [list  size];
              retVal = [NSNumber numberWithInt:arraySize];
          }
      }
      
      else {
          
        retVal = [self getModelAttribute:key Object:self];
          
      }
    } else {
      retVal = [self valueForKey:key];
    }
  } @catch (NSException *exception) {
    NSLog(@"error excoeption %@", exception.reason);
  } @finally {
    return retVal;
  }
}

#pragma mark - keypath methods

- (id)valueForKeyPathEx:(NSString *)keyPath {
  @try {
    NSRange range = [keyPath rangeOfString:@"."];
    if (range.location == NSNotFound) return [self valueForKeyEx:keyPath];
    NSString *key = [keyPath substringToIndex:range.location];
    NSString *rest = [keyPath substringFromIndex:NSMaxRange(range)];
    id next = [self valueForKeyEx:key];
    return [next valueForKeyPathEx:rest];
  } @catch (NSException *exception) {
    NSLog(@"error excoeption %@", exception.reason);
  }
}

- (void)setValueForKeyPathEx:(id)value forKeyPath:(NSString *)keyPath {
  @try {
    NSRange range = [keyPath rangeOfString:@"."];
    if (range.location == NSNotFound) {
      [self setValueEx:value forKey:keyPath];
      return;
    }

    NSString *key = [keyPath substringToIndex:range.location];
    NSString *rest = [keyPath substringFromIndex:NSMaxRange(range)];
    id next = [self valueForKeyEx:key];
    [next setValueForKeyPathEx:value forKeyPath:rest];
  } @catch (NSException *exception) {
    NSLog(@"error excoeption %@", exception.reason);
  }
}

#pragma mark - helper methods

- (NSObject *)getModelAttribute:(NSString *)attributeName
                         Object:(NSObject *)object {
  NSObject *retVal = nil;

  @try {
    NSString *newAttributeName = [attributeName
        stringByReplacingCharactersInRange:NSMakeRange(0, 1)
                                withString:[[attributeName substringToIndex:1]

                                               capitalizedString]];
    NSString *methodName =
        [NSString stringWithFormat:@"get%@", newAttributeName];
    SEL sel = NSSelectorFromString(methodName);
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
    retVal = [object performSelector:sel];
#pragma clang diagnostic pop
  } @catch (NSException *exception) {
    NSLog(@"error excoeption %@", exception.reason);
  } @finally {
    return retVal;
  }
}







/*


 set the attribute on a model objects. reconstructs the setter name based on the
 attribute name and attribute type.


 */

- (BOOL)setModelAttribute:(NSString *)attributeName
                   Object:(NSObject *)object
                 Argument:(NSObject *)argument {
  BOOL retVal = FALSE;

  NSArray *ivarInfo =
      [self makeCommonAttributeOperationName:attributeName Object:object];
    
    
  if ([ivarInfo[0] length] != 0) {
    NSString *methodName = [NSString stringWithFormat:@"set%@:", ivarInfo[0]];
    SEL sel = NSSelectorFromString(methodName);
    if ([object respondsToSelector:sel] ) {
        
        EncodingEnumType encodingType = (EncodingEnumType) [ivarInfo[2] intValue];
        
        id typedArg;
        switch (encodingType) {
            case EncodingTypeLong :
                
                
               // typedArg = [[JavaLangLong alloc] initWithLongLong:[((NSNumber*) argument) longLongValue]];
                
                typedArg = create_JavaLangLong_initWithLong_([((NSNumber*) argument) longLongValue]);
                
                
                break;
            case  EncodingTypeLongLong:
                
                typedArg = create_JavaLangLong_initWithLong_([((NSNumber*) argument) longLongValue]);
                //typedArg = [[JavaLangLong alloc] initWithLongLong:[((NSNumber*) argument) longValue]];
                break;
            case  EncodingTypeInt:
                typedArg = [[JavaLangInteger alloc] initWithInteger:[((NSNumber*) argument) integerValue]];
                break;
            case  EncodingTypeFloat:
                typedArg = [[JavaLangFloat alloc] initWithFloat:[((NSNumber*) argument) floatValue]];
                break;
            case  EncodingTypeDouble:
                typedArg = [[JavaLangDouble alloc] initWithDouble:[((NSNumber*) argument) doubleValue]];
                break;
            case  EncodingTypeBOOL:
                typedArg = [[JavaLangBoolean alloc] initWithBoolean:[((NSNumber*) argument) boolValue]];
                break;
            case  EncodingTypeJavaLangBoolean:
                typedArg = [[JavaLangBoolean alloc] initWithBoolean:[((NSNumber*) argument) boolValue]];
                break;
            case  EncodingTypeJavaLangCharacter:
                typedArg = [[JavaLangCharacter   alloc] initWithChar:[((NSNumber*) argument) charValue]];
                break;
            case  EncodingTypeJavaLangDouble:
                typedArg = [[JavaLangDouble alloc] initWithDouble:[((NSNumber*) argument) doubleValue]];
                break;
            case  EncodingTypeJavaLangFloat:
                typedArg = [[JavaLangFloat alloc] initWithFloat:[((NSNumber*) argument) floatValue]];
                break;
            case  EncodingTypeJavaLangLong:
                typedArg = [[JavaLangLong alloc] initWithLong:[((NSNumber*) argument) longLongValue]];
                break;
            case  EncodingTypeJavaLangShort:
                typedArg = [[JavaLangShort alloc] initWithShort:[((NSNumber*) argument) shortValue]];
                break;
            case  EncodingTypeJavaLangInteger:
                typedArg = [[JavaLangInteger alloc] initWithInt:[((NSNumber*) argument) intValue]];
                break;
            case  EncodingTypeNSString:
                /* already an NSString*/
                typedArg = argument;
                break;
            case  EncodingTypeClass:
                
                typedArg=argument;
                break;
              case  EncodingTypeJavaUtilArrayList:
                typedArg=(JavaUtilArrayList*) argument;
                
                
            default:
                typedArg=argument;
                break;
        }
        
     
        
     [object performSelector:sel withObject:typedArg];

 
      retVal = TRUE;
    } else {
      retVal = NO;
    }
  } else {
    retVal = FALSE;
  }
  return retVal;
}


- (EncodingEnumType) encodingTypeFromSub:(NSString*) sub
{
    
    EncodingEnumType encodingType= EncodingTypeNotFound;
    
    if([sub isEqualToString:@"JavaUtilArrayList"] || [sub isEqualToString:@"JavaUtilList"])
    {
        encodingType =  EncodingTypeJavaUtilArrayList;
        
    }
    else if([sub isEqualToString:@"JavaUtilDate"])
    {
        encodingType =  EncodingTypeJavaUtilDate;
        
    }
   else  if([sub isEqualToString:@"JavaLangBoolean"])
    {
        encodingType =  EncodingTypeJavaLangBoolean;
        
    }
    else if(  [sub isEqualToString:@"JavaLangInteger"])
    {
        encodingType =  EncodingTypeJavaLangInteger;
        
    }
    else if(  [sub isEqualToString:@"JavaMathBigInteger"])
    {
        
        encodingType =  EncodingTypeJavaLangBigInteger;
    }
    else if(  [sub isEqualToString:@"JavaLangCharacter"])
    {
        
        encodingType =  EncodingTypeJavaLangCharacter;
        
    }
    else if(  [sub isEqualToString:@"JavaLangDouble"])
    {
        
        encodingType =  EncodingTypeJavaLangDouble;
        
    }
    else if(  [sub isEqualToString:@"JavaLangFloat"])
    {
        
        
    }
    else if(  [sub isEqualToString:@"JavaLangLong"])
    {
        
        encodingType =  EncodingTypeJavaLangLong;
    }
    else if(  [sub isEqualToString:@"JavaLangShort"])
    {
        
        encodingType =  EncodingTypeJavaLangShort;
    }
    else if(  [sub isEqualToString:@"NSString"])
    {
        
        encodingType =  EncodingTypeNSString;
    }
    
    return encodingType;
    
}


 

/*
 helps  manufactures names  j2object names like setXXXWithJavaUtilInt by
 creating
 the end part such as WithJavaUtilInt.

 Handle various sepcial cases e.g
 attribute name might end with '_' or '__'

 Attribute type format might be;
 a) enclosed in angular bracketts  "<type>"
 b) enclosed by escaped string     "\"type\"
 c) a simple string
 d) match the encoding for a primative type such as long long or long


 The method will likely be incomplete as it does not handle primative types for
 in, bool, float...
 So far these primatives have not appeared in j2obc generated code.


 */
- (NSArray *)makeCommonAttributeOperationName:(NSString *)attributeName
                                        Object:(NSObject *)object {
  NSString *methodName = nil;
        NSString *sub;
    
    
    EncodingEnumType  encodingType =EncodingTypeClass;
  NSString *stringWithUnderscore = nil;
  if ([attributeName isEqualToString:@"id"] ||
      [attributeName isEqualToString:@"description"]) {
    stringWithUnderscore = [NSString stringWithFormat:@"%@__", attributeName];
  } else {
    stringWithUnderscore = [NSString stringWithFormat:@"%@_", attributeName];
  }

  Ivar ivar = class_getInstanceVariable(
      [object class],
      [stringWithUnderscore
          cStringUsingEncoding:[NSString defaultCStringEncoding]]);
  if (ivar) {
    NSString *ivarType =
        [NSString stringWithUTF8String:ivar_getTypeEncoding(ivar)];
      
      sub = [self getSub:ivarType];
      
      if([sub length] !=0)
      {
          
           encodingType = [self encodingTypeFromSub:sub];
      }
 
      else
      {
          
          const char* c = ivar_getTypeEncoding(ivar);
                          
         //handle underlying nativ type of 'long' or 'long long'
        if ((strcmp(c, @encode(long long))) == 0) {
            
            encodingType=EncodingTypeLong;
            
          sub = @"Long";
        }
        else if ( (strcmp(c, @encode(long))) == 0) {
            
          sub = @"Long";
            encodingType=EncodingTypeLongLong;
            
        }
        else if ((strcmp(c, @encode(int))) == 0) {
            encodingType=EncodingTypeInt;
              sub = @"Integer";
              
        }
        else if ((strcmp(c, @encode(float))) == 0) {
            encodingType=EncodingTypeFloat;
            sub = @"Float";
            
        }
        else if ((strcmp(c, @encode(unsigned int))) == 0) {
            encodingType=EncodingTypeUnsigndInt;
            sub = @"Integer";
            
        }
        else if ((strcmp(c, @encode(unsigned short))) == 0) {
            encodingType=EncodingTypeUnsigndShort;
            sub = @"Short";
            
        }
        else if ((strcmp(c, @encode(unsigned long))) == 0) {
            encodingType=EncodingTypeUnsigndLong;
            sub = @"Long";
            
        }
        else if ((strcmp(c, @encode(unsigned long long))) == 0) {
            encodingType=EncodingTypeUnsigndLongLong;
            sub = @"Long";
            
        }
        else if ((strcmp(c, @encode(BOOL))) == 0) {
            encodingType=EncodingTypeBOOL;
            sub = @"Boolean";
            
        }
  
      
    }

    NSString *newAttributeName = [attributeName
        stringByReplacingCharactersInRange:NSMakeRange(0, 1)
                                withString:
                                    [[attributeName
                                        substringToIndex:1] capitalizedString]];
    methodName = [NSString stringWithFormat:@"%@With%@", newAttributeName, sub];
  }
    if([methodName length] !=0 )
    {
         return @[methodName,sub,[NSNumber numberWithInt:encodingType]];
    }
    else
    {
        
        return nil;
    }
}

-(NSString*) getSub:(NSString*) ivarType
{
    NSString* sub=nil;
    NSRange r1 = [ivarType rangeOfString:@"<"];
    NSRange r2 = [ivarType rangeOfString:@">"];
    
    if (r1.length != 0 && r2.length != 0)
    {
        NSRange rSub = NSMakeRange(r1.location + r1.length,
                                   r2.location - r1.location - r1.length);
        sub = [ivarType substringWithRange:rSub];
    }
    else
    {
        NSRange r1 = [ivarType rangeOfString:@"\""];
        NSRange r2 = [ivarType rangeOfString:@"\"" options:NSBackwardsSearch];
        
        if (r1.length != 0 && r2.length != 0)
        {
            NSRange rSub = NSMakeRange(r1.location + r1.length,
                                       r2.location - r1.location - r1.length);
            sub = [ivarType substringWithRange:rSub];
        }
       
    }
    return sub;
}




- (id)valueForKeyAndIndex:(int)index Key:(NSString *)key {
  id retVal = nil;
  ;
  if ([self isKindOfClass:[JavaUtilArrayList class]]) {
    int arraySize = [((JavaUtilArrayList *)self)size];

    if (arraySize > index) {
      retVal = [((JavaUtilArrayList *)self)getWithInt:index];
    }

  } else {
    key = [self stripIndex:key];
    NSObject *object = [self valueForKeyEx:key];
    if ([object isKindOfClass:[JavaUtilArrayList class]]) {
      int arraySize = [((JavaUtilArrayList *)object)size];
      if (arraySize > index) {
        retVal = [((JavaUtilArrayList *)object)getWithInt:index];
      }
    }
  }
  return retVal;
}

- (NSString *)stripIndex:(NSString *)attributeName {
  NSRange r1 = [attributeName rangeOfString:@"["];
  NSString *substring = [attributeName substringToIndex:r1.location];
  return substring;
}

- (int)getIndex:(NSString *)attributeName {
  int retValue = -1;
  NSRange r1 = [attributeName rangeOfString:@"["];
  NSRange r2 = [attributeName rangeOfString:@"]"];
  NSRange rSub = NSMakeRange(r1.location + r1.length,
                             r2.location - r1.location - r1.length);
  rSub.length += 1;
  rSub.location -= 1;

  NSString *sub = [attributeName substringWithRange:rSub];
  sub = [sub substringFromIndex:1];

  retValue = [sub intValue];
  return retValue;
}

- (BOOL)isIndexed:(NSString *)attributeName {
  BOOL retVal;
  NSRange r = [attributeName rangeOfString:@"["];
  if (r.location == NSNotFound) {
    retVal = NO;
  } else {
    retVal = YES;
  }
  return retVal;
}


- (BOOL)isCount:(NSString *)attributeName {
    BOOL retVal;
    NSRange r = [attributeName rangeOfString:@"#"];
    if (r.location == NSNotFound) {
        retVal = NO;
    } else {
        retVal = YES;
    }
    return retVal;
}


- (NSString*)trimCount:(NSString *)str {
    
   NSString * subString =  [str  substringToIndex:str.length-(str.length>0)];
   return subString;
   
}


- (BOOL)isJ2Objc {
    
    BOOL isSubclass;
     isSubclass = [self isKindOfClass:[PAModelBase class]];
         //isSubclass = ([self isKindOfClass:[PAModelBase class]] || [self isKindOfClass:[JavaUtilAbstractCollection class]])  ;
    return isSubclass;
}

@end
