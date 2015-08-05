//
//  NSObject+J2objcKVO.m
//  Paco
//
//  Created by northropo on 8/5/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "NSObject+J2objcKVO.h"
#import "ModelBase.h" 
#import <objc/runtime.h>

@implementation NSObject (J2objcKVO)


-(BOOL) isJ2Objc
{
    return [self isKindOfClass:[PAModelBase class]];
}

- (id)valueForKeyEX: (NSString *)key
{
    
    if([self isJ2Objc])
    {
        [self getModalAttribute:key Object:self];
    }
    else
    {
        [self valueForKey:key];
    }
 
}

- (void )setValueEx: (id)value forKey: (NSString *)key
{
  if([self isJ2Objc])
  {
      [self setModalAttribute:key Object:self Argument:value];
  }
  else
  {
      [self setValue:value  forKey:key];
  }
}

- (id)valueForKeyPathEx: (NSString *)keyPath
{
    NSRange range = [keyPath rangeOfString: @"."];
    if(range.location == NSNotFound)
        return [self valueForKeyEx: keyPath];
    
    NSString *key = [keyPath substringToIndex: range.location];
    NSString *rest = [keyPath substringFromIndex: NSMaxRange(range)];
    
    id next = [self valueForKeyEx: key];
    return [next valueForKeyPathEx: rest];
}

- (void)setValueForKeyPathEx: (id)value forKeyPath: (NSString *)keyPath
{
    NSRange range = [keyPath rangeOfString: @"."];
    if(range.location == NSNotFound)
    {
        [self setValueEx: value forKey: keyPath];
        return;
    }
    
    NSString *key = [keyPath substringToIndex: range.location];
    NSString *rest = [keyPath substringFromIndex: NSMaxRange(range)];
    id next = [self valueForKeyEx: key];
    [next setValueEx: value forKeyPath: rest];
    
}


#pragma mark - helper methods


#pragma mark - helper methods

-(NSObject*) getModalAttribute:(NSString*) attributeName Object:(NSObject*) object
{
    NSObject*  retVal = nil;
    NSString *newAttributeName  = [attributeName stringByReplacingCharactersInRange:NSMakeRange(0,1)
                                                                         withString:[[attributeName substringToIndex:1]
                                                                                     
                                                                                     capitalizedString]];
    NSString * methodName  = [NSString stringWithFormat:@"get%@",newAttributeName ];
    SEL sel = NSSelectorFromString(methodName);
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
    retVal = [object performSelector:sel];
#pragma clang diagnostic pop

    return retVal;
}


/*
 
 
 set the attribute on a modal objects. reconstructs the setter name based on the attribute name and attribute type.
 
 
 */

-(BOOL) setModalAttribute:(NSString*) attributeName Object:(NSObject*) object Argument:(NSObject*) argument
{
    BOOL retVal = FALSE;
    NSString *rootString = [self makeCommonAttributeOperationName:attributeName  Object:object];
    if([rootString length] !=0)
    {
        NSString * methodName  = [NSString stringWithFormat:@"set%@:",rootString ];
        SEL sel = NSSelectorFromString(methodName);
        if ([object respondsToSelector:sel])
        {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
            [object performSelector:sel withObject:argument];
#pragma clang diagnostic pop
            retVal = TRUE;
        }
        else
        {
            retVal = NO;
        }
    }
    else
    {
        retVal = FALSE;
    }
    return retVal;
    
}


/*
 helps  manufactures names  j2object names like setXXXWithJavaUtilInt by creating
 the end part such as WithJavaUtilInt.
 
 Handle various sepcial cases e.g
 attribute name might end with '_' or '__'
 
 Attribute type format might be;
 a) enclosed in angular bracketts  "<type>"
 b) enclosed by escaped string     "\"type\"
 c) a simple string
 d) match the encoding for a primative type such as long long or long
 
 
 The method will likely be incomplete as it does not handle primative types for in, bool, float...
 So far these primatives have not appeared in j2obc generated code.
 
 
 */
-(NSString*) makeCommonAttributeOperationName:(NSString*) attributeName Object:(NSObject*) object
{
    NSString* methodName=nil;
    NSString * stringWithUnderscore=nil;
    if([attributeName isEqualToString:@"id"] || [attributeName isEqualToString:@"idescription"])
    {
        
        stringWithUnderscore = [NSString stringWithFormat:@"%@__",attributeName];
    }
    else
    {
        stringWithUnderscore = [NSString stringWithFormat:@"%@_",attributeName];
    }
    
    Ivar ivar = class_getInstanceVariable( [object class],[stringWithUnderscore cStringUsingEncoding:[NSString defaultCStringEncoding]]);
    if(ivar)
    {
        NSString * ivarType = [NSString stringWithUTF8String:ivar_getTypeEncoding(ivar)];
        NSString* sub;
        NSRange r1 = [ivarType rangeOfString:@"<"];
        NSRange r2 = [ivarType rangeOfString:@">"];
        
        if(r1.length!=0 && r2.length !=0)
        {
            NSRange rSub = NSMakeRange(r1.location + r1.length, r2.location - r1.location - r1.length);
            sub = [ivarType substringWithRange:rSub];
        }
        else
        {
            NSRange r1 = [ivarType rangeOfString:@"\""];
            NSRange r2 = [ivarType rangeOfString:@"\"" options:NSBackwardsSearch];
            
            if(r1.length!=0 && r2.length !=0)
            {
                NSRange rSub = NSMakeRange(r1.location + r1.length, r2.location - r1.location - r1.length);
                sub = [ivarType substringWithRange:rSub];
            }
            else
            {
                if((strcmp(ivar_getTypeEncoding(ivar), @encode(long long))) == 0)
                {
                    sub = @"Long";
                }
                if((strcmp(ivar_getTypeEncoding(ivar), @encode(long))) == 0)
                {
                    sub = @"Long";
                }
                
            }
        }
        
        NSString *newAttributeName  = [attributeName stringByReplacingCharactersInRange:NSMakeRange(0,1) withString:[[attributeName substringToIndex:1] capitalizedString]];
        methodName = [NSString stringWithFormat:@"%@With%@",newAttributeName,sub];
    }
    return methodName;
}




@end
