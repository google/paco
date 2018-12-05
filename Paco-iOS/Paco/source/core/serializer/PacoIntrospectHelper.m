/* Copyright 2015  Google
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

#import "PacoIntrospectHelper.h"
#import "NSObject+J2objcKVO.h"
#include "java/util/ArrayList.h"
#include "java/lang/Boolean.h"
#include "java/lang/Long.h"
#include "java/lang/Integer.h"
#include "java/lang/Float.h"
#include "java/lang/Double.h"
#include "java/lang/Boolean.h"
#include "java/lang/Short.h"
#include "java/Util/Date.h"
#include "java/lang/Character.h"


@implementation PacoIntrospectHelper

/*
  method can be optimized.
 */
+(EncodingEnumType) encodingForType:(NSString*) ivarType
{
    
  NSString* sub = [self getSub:ivarType];
  EncodingEnumType type = [self encodingTypeFromSub:sub];
  return type;
    
    
}


/*
 EncodingTypeUnsigndLongLong,
 EncodingTypeJavaLangBoolean,
 EncodingTypeJavaLangInteger,
 EncodingTypeJavaLangBigInteger,
 EncodingTypeJavaLangCharacter,
 EncodingTypeJavaLangDouble,
 EncodingTypeJavaLangFloat,
 EncodingTypeJavaLangLong,
 EncodingTypeJavaLangShort,
 EncodingTypeNSString
 
 */


+(NSDate*) toNSDate:(JavaUtilDate*) date
{
    
    NSLog(@" %i", [date getDate]);
    return [NSDate date];
    
}
+(NSArray *) parseIvar:(Ivar) ivar Parent:(NSObject*) parent
{
    
  
    const char* c = ivar_getTypeEncoding(ivar);
    const char* name =  ivar_getName(ivar);
    NSString* ivarName =
    [NSString stringWithCString:name
                       encoding:NSUTF8StringEncoding];
    NSString  *ivarType = [NSString stringWithCString:c encoding:[NSString defaultCStringEncoding]];
    
    NSLog(@" ivar name --->   %@  type --> %@ ", ivarName,ivarType);
          
          
          
          
 
    NSArray* returnArray =nil;
    NSObject  * returnObject =nil;
  
    if ([ivarType rangeOfString:@"@"].location != NSNotFound)
    {
        id   value   =  object_getIvar(parent, ivar);
        
        EncodingEnumType javaEncodingType   =   [self encodingForType:ivarType];
        switch (javaEncodingType) {
 
            case EncodingTypeJavaLangBoolean:
                returnObject= (value) ?[NSNumber numberWithBool:[( (JavaLangBoolean*) value) booleanValue]]:[NSNull null];
                break;
            case EncodingTypeJavaLangInteger:
               returnObject=  (value) ?[NSNumber numberWithInt:[((JavaLangInteger * )value) intValue]]:[NSNull null];
                break;
            case EncodingTypeJavaLangCharacter:
                returnObject=  (value) ?[NSNumber numberWithChar:[((JavaLangCharacter * )value) charValue]]:[NSNull null];
                break;
            case EncodingTypeJavaLangDouble:
                returnObject=  (value) ?[NSNumber numberWithDouble:[((JavaLangDouble * )value) doubleValue]]:[NSNull null];
                break;
            case EncodingTypeJavaLangLong:
                returnObject=  (value) ?[NSNumber numberWithDouble:[((JavaLangLong * )value) longLongValue]]:[NSNull null];
                break;
            case EncodingTypeJavaLangFloat:
                  returnObject=  (value) ?[NSNumber numberWithDouble:[((JavaLangFloat  * )value) floatValue]]:[NSNull null];
                break;
            case EncodingTypeNSString:
                 returnObject= (value) ?(( NSString*)value):[NSNull null];
                break;
            case EncodingTypeJavaUtilDate:
                returnObject= (value) ?[self  toNSDate:value]:[NSNull null];
                break;
            case EncodingTypeJavaUtilArrayList :
                returnObject= (value) ? value :[NSNull null];
                break;
            case EncodingTypeNotFound :
                returnObject= (value) ? value :[NSNull null];
                break;
          
                break;
            default:
                assert(false);
                
                break;
        }
        
        returnArray=@[returnObject,ivarType];
        
    }else
    {
        
        /* using object_getIvar with primatives can cause sig failure,  EXC_BAD_ACCESS rumored to be caused by ARC
         */
        
        if ([ivarType length] == 1 )
        {
            
            NSNumber * num =nil;
            
            switch (c[0])
            {
                case 'c':
                {
                    char c = (char)object_getIvar(parent, ivar);
                    num = @(c);
                    break;
                }

                case 'i':
                {
                    
                    int c = (int)object_getIvar(parent,ivar);
                    num = @(c);
                    break;
                }
                    
                case 's':
                {
                    short c = (short)object_getIvar(parent, ivar);
                    num = @(c);
    

                }
                   
                case 'l':
                {
                    long c = (long)object_getIvar(parent,ivar);
                    num = @(c);
                    break;

                }
                   
                case 'q':
                {
                   long long c = (long long) class_getInstanceVariable( [parent class]  ,name);
                    num = @(c);
         
                   break;

                }
                case 'C':
                {
                    unsigned char c = (unsigned char)object_getIvar(parent, ivar);
                    num = @(c);
                
            
                   break;
                }
               
                case 'I':
                {
                    unsigned int c = (unsigned int)object_getIvar(parent, ivar);
                    num = @(c);
         

                }
                    break;
                case 'S':
                {
                    unsigned short c = (unsigned short)object_getIvar(self, ivar);
                    num = @(c);
       

                }
                    break;
                case 'L':
                {
                    unsigned long c = (unsigned long)object_getIvar(parent, ivar);
                    num = @(c);
            

                }
                    break;
                case 'Q':
                {
                    unsigned long long c = (unsigned long long)object_getIvar(parent, ivar);
                    num = @(c);
             

                }
                    break;
                case 'f':
                {

                   
                       // float c = (float) object_getIvar(parent, ivar);
                        //num = @(c);
 

                }
                    break;
                case 'd':
                    // double c = (double) object_getIvar(parent, ivar);
                    // num = @(c);
                    break;
                case 'B':
                {
                    bool  c = (bool )object_getIvar(parent, ivar);
                     num = @(c);
                }
                    break;
                default:
                    break;
            }
            returnArray=@[num, ivarType];
        }
     
    }
    
    NSLog(@" return array -> %@", returnArray);
    return returnArray;
}



@end
