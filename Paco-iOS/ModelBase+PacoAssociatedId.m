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
