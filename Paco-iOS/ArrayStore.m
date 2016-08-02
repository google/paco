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

#import "ArrayStore.h"
#import "GenericArray.h"
#import "PacoAppDelegate.h" 
#import "PAExperimentDAO+PacoCoder.h"

@interface ArrayStore()


@property(nonatomic, retain, readwrite) PacoAppDelegate* appDelegate;
@property(nonatomic, retain, readwrite) NSManagedObjectContext * context;

@end

#define TYPE_EXPERIMENT @"experiment";


@implementation ArrayStore


- (instancetype)init
{
    self = [super init];
    if (self) {
        
        _appDelegate  = (PacoAppDelegate *) [UIApplication sharedApplication].delegate;
        _context =  _appDelegate.managedObjectContext;
    }
    return self;
}


-(void) deleteArray:(NSString*) type
{
 
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"GenericArray" inManagedObjectContext:self.context];
    NSPredicate* predicate =  [NSPredicate predicateWithFormat:@"type==%@",type ];
    
    
    [fetchRequest setPredicate:predicate];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *items = [self.context executeFetchRequest:fetchRequest error:&error];
    
    
    for (NSManagedObject *managedObject in items)
    {
         [self.context deleteObject:managedObject];
    }
 
    
  
    
    
    
}

-(void) removeObjectFromArray:(NSString*) type  Object:(NSObject*) object
{
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"GenericArray" inManagedObjectContext:self.context];
    NSPredicate* predicate =  [NSPredicate predicateWithFormat:@"type==%@",type ];
    
    
    [fetchRequest setPredicate:predicate];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *items = [self.context executeFetchRequest:fetchRequest error:&error];
    
    
    for (GenericArray* genericArray in items)
    {
        NSArray* array =    [NSKeyedUnarchiver unarchiveObjectWithData:genericArray.blob];
        
        NSMutableArray* mutableArray = [[NSMutableArray alloc] initWithArray:array];
        [mutableArray removeObject:object];
        NSData *data = [NSKeyedArchiver archivedDataWithRootObject:mutableArray];
        genericArray.blob = data;
    }
    
    
    if (![self.context save:&error])
    {
        NSLog(@"fail: %@", [error localizedDescription]);
        assert(FALSE);
    }
    
    
    
    
}





-(NSArray*) fetchArray:(NSString*) type
{
    
 
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"GenericArray" inManagedObjectContext:self.context];
    NSPredicate* predicate =  [NSPredicate predicateWithFormat:@"type==%@",type ];
    

    [fetchRequest setPredicate:predicate];
    [fetchRequest setEntity:entity];
    
     NSError *error;
     NSArray * records   = [self.context executeFetchRequest:fetchRequest error:&error];
     NSAssert( [records count] < 2, @" there should be only one object with a given key");
    
     GenericArray * ga = [records firstObject];
    
    NSData* data = ga.blob;
    NSArray* array = [NSKeyedUnarchiver unarchiveObjectWithData:data];

    return array;
}


-(void) updateOrInsert:(NSString*) recordType   Array:(NSArray*) array
{
    [self deleteArray:recordType];
    [self insertRecord:recordType array:array];
    
}


-(void) insertRecord:(NSString*) recordType array:(NSArray*) array
{
    
    
    NSArray* ar = [self fetchArray:recordType];
    NSAssert([ar count]==0,@"database already has an object with that key");
    NSData *data = [NSKeyedArchiver archivedDataWithRootObject:array];
    GenericArray*  genericArray = [NSEntityDescription
                                 insertNewObjectForEntityForName:@"GenericArray"
                                 inManagedObjectContext:[self.appDelegate managedObjectContext]];
    genericArray.blob = data;
    genericArray.type=recordType;
    NSError *error;
    if (![self.context save:&error])
    {
        NSLog(@"fail: %@", [error localizedDescription]);
        assert(FALSE);
    }
    
    
}
@end
