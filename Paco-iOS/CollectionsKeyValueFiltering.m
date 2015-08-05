 

#import "CollectionsKeyValueFiltering.h"

@implementation NSArray (KeyValueFiltering)

- (id) firstObjectWithValue:(id)value forKeyPath:(NSString*)key
{
	for (id object in self) {
		if( [[object valueForKeyPath:key] isEqual:value] )
			return object;
	}
	return nil;
}

- (NSArray*) filteredArrayWithValue:(id)value forKeyPath:(NSString*)key
{
	NSMutableArray * objects = [NSMutableArray arrayWithCapacity:[self count]];
	
	for (id object in self) {
		if( [[object valueForKeyPath:key] isEqual:value] )
			[objects addObject:object];
	}
	
	return [NSArray arrayWithArray:objects];
}

@end


@implementation NSSet (KeyValueFiltering)

- (id) anyObjectWithValue:(id)value forKeyPath:(NSString*)key
{
	for (id object in self) {
		if( [[object valueForKeyPath:key] isEqual:value] )
			return object;
	}
	return nil;
}

- (NSSet*) filteredSetWithValue:(id)value forKeyPath:(NSString*)key
{
	NSMutableSet * objects = [NSMutableSet setWithCapacity:[self count]];
	
	for (id object in self) {
		if( [[object valueForKeyPath:key] isEqual:value] )
			[objects addObject:object];
	}
	
	return [NSSet setWithSet:objects];
}

@end

