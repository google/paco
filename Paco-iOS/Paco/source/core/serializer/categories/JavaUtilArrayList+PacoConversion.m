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

#import "JavaUtilArrayList+PacoConversion.h"

@implementation JavaUtilArrayList (PacoConversion)



+ (instancetype) arrayListWithValues:(NSArray*) arrayOfValues
{
    
    JavaUtilArrayList * arrayList = [[JavaUtilArrayList alloc] init];
    [arrayList addArrayElements:arrayOfValues];
    return arrayList;

}

-(NSArray*) toNSArrayCopy
{
    
    NSMutableArray* mutableArray = [[NSMutableArray alloc] init];
    
    for(NSObject* o in self)
    {
        [mutableArray addObject:[o copy]];
    }
    return mutableArray;
    
}


-(NSArray*) toNSArray
{
    
    NSMutableArray* mutableArray = [[NSMutableArray alloc] init];
    
    for(NSObject* o in self)
    {
        [mutableArray addObject:o];
    }
    return mutableArray;
    
}



-(void) addArrayElements:(NSArray*) array
{
   
    for(id ob in array)
    {
         [self addWithId:ob];
    }
   
}



@end
