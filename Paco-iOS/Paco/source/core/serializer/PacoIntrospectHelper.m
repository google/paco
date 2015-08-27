//
//  PacoIntrospectHelper.m
//  Paco
//
//  Created by northropo on 8/26/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoIntrospectHelper.h"

@implementation PacoIntrospectHelper

+(NSArray *) parseIvar:(Ivar) ivar
{
    const char* c = ivar_getTypeEncoding(ivar);
    NSString* ivarName =
    [NSString stringWithCString:ivar_getName(ivar)
                       encoding:NSUTF8StringEncoding];
    NSString  *ivarType = [NSString stringWithCString:c encoding:[NSString defaultCStringEncoding]];
    NSString  *strInfo = nil;
    if ([ivarType rangeOfString:@"@"].location != NSNotFound)
    {
        id value = object_getIvar(self, ivar);
        strInfo = [strInfo stringByAppendingFormat:@" %@: %@",ivarName,value];
        //  NSLog();
    }else
    {
        if ([ivarType length] == 1)
        {
    switch (c[0])
    {
        case 'c':
        {
            char c = (char)object_getIvar(self, ivar);
            //  NSLog(@"%@: %c",ivarName,c);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %c",ivarName,c];

        }

            break;
        case 'i':
        {
            int c = (int)object_getIvar(self,ivar);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %i",ivarName,c];
            // NSLog(@"%@: %i",ivarName,c);

        }
            break;
        case 's':
        {
            short c = (short)object_getIvar(self, ivar);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %i",ivarName,c];
            // NSLog(@"%@: %i",ivarName,c);

        }
            break;
        case 'l':
        {
            long c = (long)object_getIvar(self,ivar);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %ld",ivarName,c];
            // NSLog(@"%@: %ld",ivarName,c);

        }
            break;
        case 'q':
        {
            long long c = (long long)object_getIvar(self, ivar);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %lld",ivarName,c];
            //   NSLog(@"%@: %lld",ivarName,c);

        }

            break;
        case 'C':
        {
            unsigned char c = (unsigned char)object_getIvar(self, ivar);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %c",ivarName,c];
            //  NSLog(@"%@: %c",ivarName,c);

        }
            break;
        case 'I':
        {
            unsigned int c = (unsigned int)object_getIvar(self, ivar);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %d",ivarName,c];
            //  NSLog(@"%@: %d",ivarName,c);

        }
            break;
        case 'S':
        {
            unsigned short c = (unsigned short)object_getIvar(self, ivar);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %d",ivarName,c];
            //  NSLog(@"%@: %d",ivarName,c);

        }
            break;
        case 'L':
        {
            unsigned long c = (unsigned long)object_getIvar(self, ivar);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %ld",ivarName,c];
            // NSLog(@"%@: %ld",ivarName,c);

        }
            break;
        case 'Q':
        {
            unsigned long long c = (unsigned long long)object_getIvar(self, ivar);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %lld",ivarName,c];
            // NSLog(@"%@: %lld",ivarName,c);

        }
            break;
        case 'f':
        {

            float  value = 0;



           /*
            float c = (float) object_getIvar(self, ivars[i]);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %lld",ivarName,c];
            NSLog(@"value:%f",value);
            //  float c = (float)object_getIvar(self, ivar);
            //  NSLog(@"%@: %f",ivarName,c);
            */

        }
            break;
        case 'd':
            break;
        case 'B':
        {
            int c = (int)object_getIvar(self, ivar);
            strInfo = [strInfo stringByAppendingFormat:@" %@: %d",ivarName,c];
            //NSLog(@"%@: %d",ivarName,c);
            
        }
            break;
        default:
            break;
    }
        }
    }
    
    return @[strInfo];
}



@end
