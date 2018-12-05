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

#import "NSMutableArray+PacoPersistence.h"
#import "ArrayStore.h" 



@implementation NSMutableArray (PacoPersistence)

-(void) store:(NSString*) key
{
    @synchronized(self)
    {
    
       ArrayStore  * store = [[ArrayStore alloc] init];
       [store updateOrInsert:key Array:self];
    }
    
}


-(void) refreshFromStore:(NSString*) key
{
    
   @synchronized(self)
    {
            [self removeAllObjects];
            ArrayStore  * store = [[ArrayStore alloc] init];
            NSArray * array = [store fetchArray:key];
            for(NSObject* o in array)
            {
                
                
                [self addObject:o];
            }
    
    }
}

@end
