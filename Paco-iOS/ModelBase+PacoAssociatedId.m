//
//  ModelBase+PacoAssociatedId.m
//  Paco
//
//  Created by northropo on 9/11/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "ModelBase+PacoAssociatedId.h"
#import "ModelBase.h"
#import  <objc/runtime.h>



@implementation PAModelBase (PacoAssociatedId)


@dynamic uuid;


/*
     need to be careful not to call setUuid with the exisiting uuid and retaining uuid twice.
 */
- (void)setUuid:(NSString*) uniqueId {
    
    objc_setAssociatedObject(self, @selector(uuid), uniqueId, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (NSString*) getUuid {
    
    NSString *uniqueId =  objc_getAssociatedObject(self, @selector(uuid));
    if(uniqueId.length ==0)
    {
        NSString* newId = [[NSProcessInfo processInfo] globallyUniqueString];
        [self setUuid: newId];
        
        uniqueId= newId;
    }
    
    return uniqueId;
    
}

@end
