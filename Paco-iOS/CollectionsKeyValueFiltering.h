 

#import <Foundation/Foundation.h>
#include "java/util/AbstractList.h"
#include "java/util/AbstractSet.h"
@interface JavaUtilAbstractCollection (KeyValueFiltering)


 
- (id) firstObjectWithValue:(id)value forKeyPath:(NSString*)keypath;
- (NSArray*) filteredArrayWithValue:(id)value forKeyPath:(NSString*)keypath;

@end


 

