/*
 Copyright (c) 2010-2013 Sam Soffes.
 
 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:
 
 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **/

#import "SSKeychainQuery.h"
#import "SSKeychain.h"

@implementation SSKeychainQuery

@synthesize account = _account;
@synthesize service = _service;
@synthesize label = _label;
@synthesize passwordData = _passwordData;

#if __IPHONE_3_0 && TARGET_OS_IPHONE
@synthesize accessGroup = _accessGroup;
#endif

#ifdef SSKEYCHAIN_SYNCHRONIZABLE_AVAILABLE
@synthesize synchronizable = _synchronizable;
#endif

#pragma mark - Public

- (BOOL)save:(NSError *__autoreleasing *)error {
    OSStatus status = SSKeychainErrorBadArguments;
    if (!self.service || !self.account || !self.passwordData) {
		if (error) {
			*error = [[self class] errorWithCode:status];
		}
		return NO;
	}

    [self deleteItem:nil];

    NSMutableDictionary *query = [self query];
    [query setObject:self.passwordData forKey:(__bridge id)kSecValueData];
    if (self.label) {
        [query setObject:self.label forKey:(__bridge id)kSecAttrLabel];
    }
#if __IPHONE_4_0 && TARGET_OS_IPHONE
	CFTypeRef accessibilityType = [SSKeychain accessibilityType];
    if (accessibilityType) {
        [query setObject:(__bridge id)accessibilityType forKey:(__bridge id)kSecAttrAccessible];
    }
#endif
    status = SecItemAdd((__bridge CFDictionaryRef)query, NULL);

	if (status != errSecSuccess && error != NULL) {
		*error = [[self class] errorWithCode:status];
	}

	return (status == errSecSuccess);
}


- (BOOL)deleteItem:(NSError *__autoreleasing *)error {
    OSStatus status = SSKeychainErrorBadArguments;
    if (!self.service || !self.account) {
		if (error) {
			*error = [[self class] errorWithCode:status];
		}
		return NO;
	}

    NSMutableDictionary *query = [self query];
#if TARGET_OS_IPHONE
    status = SecItemDelete((__bridge CFDictionaryRef)query);
#else
    CFTypeRef result = NULL;
    [query setObject:@YES forKey:(__bridge id)kSecReturnRef];
    status = SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);
    if (status == errSecSuccess) {
        status = SecKeychainItemDelete((SecKeychainItemRef)result);
        CFRelease(result);
    }
#endif

    if (status != errSecSuccess && error != NULL) {
        *error = [[self class] errorWithCode:status];
    }

    return (status == errSecSuccess);
}


- (NSArray *)fetchAll:(NSError *__autoreleasing *)error {
    OSStatus status = SSKeychainErrorBadArguments;
    NSMutableDictionary *query = [self query];
    [query setObject:@YES forKey:(__bridge id)kSecReturnAttributes];
    [query setObject:(__bridge id)kSecMatchLimitAll forKey:(__bridge id)kSecMatchLimit];

	CFTypeRef result = NULL;
    status = SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);
    if (status != errSecSuccess && error != NULL) {
		*error = [[self class] errorWithCode:status];
		return nil;
	}

    return (__bridge NSArray *)result;
}


- (BOOL)fetch:(NSError *__autoreleasing *)error {
    OSStatus status = SSKeychainErrorBadArguments;
	if (!self.service || !self.account) {
		if (error) {
			*error = [[self class] errorWithCode:status];
		}
		return NO;
	}

	CFTypeRef result = NULL;
	NSMutableDictionary *query = [self query];
    [query setObject:@YES forKey:(__bridge_transfer id)kSecReturnData];
    [query setObject:(__bridge id)kSecMatchLimitOne forKey:(__bridge id)kSecMatchLimit];
    status = SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);

	if (status != errSecSuccess && error != NULL) {
		*error = [[self class] errorWithCode:status];
		return NO;
	}

    self.passwordData = (__bridge_transfer NSData *)result;
    return YES;
}


#pragma mark - Accessors

- (void)setPasswordObject:(id<NSCoding>)object {
    self.passwordData = [NSKeyedArchiver archivedDataWithRootObject:object];
}


- (id<NSCoding>)passwordObject {
    if ([self.passwordData length]) {
        return [NSKeyedUnarchiver unarchiveObjectWithData:self.passwordData];
    }
    return nil;
}


- (void)setPassword:(NSString *)password {
    self.passwordData = [password dataUsingEncoding:NSUTF8StringEncoding];
}


- (NSString *)password {
    if ([self.passwordData length]) {
        return [[NSString alloc] initWithData:self.passwordData encoding:NSUTF8StringEncoding];
    }
    return nil;
}


#pragma mark - Private

- (NSMutableDictionary *)query {
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionaryWithCapacity:3];
    [dictionary setObject:(__bridge id)kSecClassGenericPassword forKey:(__bridge id)kSecClass];

    if (self.service) {
        [dictionary setObject:self.service forKey:(__bridge id)kSecAttrService];
    }

    if (self.account) {
        [dictionary setObject:self.account forKey:(__bridge id)kSecAttrAccount];
    }

#if __IPHONE_3_0 && TARGET_OS_IPHONE
#if !(TARGET_IPHONE_SIMULATOR)
    if (self.accessGroup) {
        [dictionary setObject:self.accessGroup forKey:(__bridge id)kSecAttrAccessGroup];
    }
#endif
#endif
    
#ifdef SSKEYCHAIN_SYNCHRONIZABLE_AVAILABLE
    if (self.isSynchronizable) {
        [dictionary setObject:@YES forKey:(__bridge id)(kSecAttrSynchronizable)];
    }
#endif

    return dictionary;
}


+ (NSError *)errorWithCode:(OSStatus) code {
    NSString *message = nil;
    switch (code) {
        case errSecSuccess: return nil;
        case SSKeychainErrorBadArguments: message = NSLocalizedStringFromTable(@"SSKeychainErrorBadArguments", @"SSKeychain", nil); break;

#if TARGET_OS_IPHONE
        case errSecUnimplemented: {
			message = NSLocalizedStringFromTable(@"errSecUnimplemented", @"SSKeychain", nil);
			break;
		}
        case errSecParam: {
			message = NSLocalizedStringFromTable(@"errSecParam", @"SSKeychain", nil);
			break;
		}
        case errSecAllocate: {
			message = NSLocalizedStringFromTable(@"errSecAllocate", @"SSKeychain", nil);
			break;
		}
        case errSecNotAvailable: {
			message = NSLocalizedStringFromTable(@"errSecNotAvailable", @"SSKeychain", nil);
			break;
		}
        case errSecDuplicateItem: {
			message = NSLocalizedStringFromTable(@"errSecDuplicateItem", @"SSKeychain", nil);
			break;
		}
        case errSecItemNotFound: {
			message = NSLocalizedStringFromTable(@"errSecItemNotFound", @"SSKeychain", nil);
			break;
		}
        case errSecInteractionNotAllowed: {
			message = NSLocalizedStringFromTable(@"errSecInteractionNotAllowed", @"SSKeychain", nil);
			break;
		}
        case errSecDecode: {
			message = NSLocalizedStringFromTable(@"errSecDecode", @"SSKeychain", nil);
			break;
		}
        case errSecAuthFailed: {
			message = NSLocalizedStringFromTable(@"errSecAuthFailed", @"SSKeychain", nil);
			break;
		}
        default: {
			message = NSLocalizedStringFromTable(@"errSecDefault", @"SSKeychain", nil);
		}
#else
        default:
            message = (__bridge_transfer NSString *)SecCopyErrorMessageString(code, NULL);
#endif
    }

    NSDictionary *userInfo = nil;
    if (message != nil) {
        userInfo = @{ NSLocalizedDescriptionKey : message };
    }
    return [NSError errorWithDomain:kSSKeychainErrorDomain code:code userInfo:userInfo];
}

@end
