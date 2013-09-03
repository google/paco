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

/**
 Error code specific to SSKeychain that can be returned in NSError objects.
 For codes returned by the operating system, refer to SecBase.h for your
 platform.
 */
typedef enum {

	/** Some of the arguments were invalid. */
	SSKeychainErrorBadArguments = -1001,

} SSKeychainErrorCode;

/** SSKeychain error domain */
extern NSString *const kSSKeychainErrorDomain;

/** Account name. */
extern NSString *const kSSKeychainAccountKey;

/**
 Time the item was created.

 The value will be a string.
 */
extern NSString *const kSSKeychainCreatedAtKey;

/** Item class. */
extern NSString *const kSSKeychainClassKey;

/** Item description. */
extern NSString *const kSSKeychainDescriptionKey;

/** Item label. */
extern NSString *const kSSKeychainLabelKey;

/** Time the item was last modified.

 The value will be a string.
 */
extern NSString *const kSSKeychainLastModifiedKey;

/** Where the item was created. */
extern NSString *const kSSKeychainWhereKey;

/**
 Simple wrapper for accessing accounts, getting passwords, setting passwords, and deleting passwords using the system
 Keychain on Mac OS X and iOS.

 This was originally inspired by EMKeychain and SDKeychain (both of which are now gone). Thanks to the authors.
 SSKeychain has since switched to a simpler implementation that was abstracted from [SSToolkit](http://sstoolk.it).
 */
@interface SSKeychain : NSObject

#pragma mark - Classic methods

/**
 Returns a string containing the password for a given account and service, or `nil` if the Keychain doesn't have a
 password for the given parameters.

 @param serviceName The service for which to return the corresponding password.

 @param account The account for which to return the corresponding password.

 @return Returns a string containing the password for a given account and service, or `nil` if the Keychain doesn't
 have a password for the given parameters.
 */
+ (NSString *)passwordForService:(NSString *)serviceName account:(NSString *)account;
+ (NSString *)passwordForService:(NSString *)serviceName account:(NSString *)account error:(NSError **)error;


/**
 Deletes a password from the Keychain.

 @param serviceName The service for which to delete the corresponding password.

 @param account The account for which to delete the corresponding password.

 @return Returns `YES` on success, or `NO` on failure.
 */
+ (BOOL)deletePasswordForService:(NSString *)serviceName account:(NSString *)account;
+ (BOOL)deletePasswordForService:(NSString *)serviceName account:(NSString *)account error:(NSError **)error;


/**
 Sets a password in the Keychain.

 @param password The password to store in the Keychain.

 @param serviceName The service for which to set the corresponding password.

 @param account The account for which to set the corresponding password.

 @return Returns `YES` on success, or `NO` on failure.
 */
+ (BOOL)setPassword:(NSString *)password forService:(NSString *)serviceName account:(NSString *)account;
+ (BOOL)setPassword:(NSString *)password forService:(NSString *)serviceName account:(NSString *)account error:(NSError **)error;


/**
 Returns an array containing the Keychain's accounts, or `nil` if the Keychain has no accounts.

 See the `NSString` constants declared in SSKeychain.h for a list of keys that can be used when accessing the
 dictionaries returned by this method.

 @return An array of dictionaries containing the Keychain's accounts, or `nil` if the Keychain doesn't have any
 accounts. The order of the objects in the array isn't defined.
 */
+ (NSArray *)allAccounts;


/**
 Returns an array containing the Keychain's accounts for a given service, or `nil` if the Keychain doesn't have any
 accounts for the given service.

 See the `NSString` constants declared in SSKeychain.h for a list of keys that can be used when accessing the
 dictionaries returned by this method.

 @param serviceName The service for which to return the corresponding accounts.

 @return An array of dictionaries containing the Keychain's accountsfor a given `serviceName`, or `nil` if the Keychain
 doesn't have any accounts for the given `serviceName`. The order of the objects in the array isn't defined.
 */
+ (NSArray *)accountsForService:(NSString *)serviceName;


#pragma mark - Configuration

#if __IPHONE_4_0 && TARGET_OS_IPHONE
/**
 Returns the accessibility type for all future passwords saved to the Keychain.

 @return Returns the accessibility type.

 The return value will be `NULL` or one of the "Keychain Item Accessibility
 Constants" used for determining when a keychain item should be readable.

 @see setAccessibilityType
 */
+ (CFTypeRef)accessibilityType;

/**
 Sets the accessibility type for all future passwords saved to the Keychain.

 @param accessibilityType One of the "Keychain Item Accessibility Constants"
 used for determining when a keychain item should be readable.

 If the value is `NULL` (the default), the Keychain default will be used.

 @see accessibilityType
 */
+ (void)setAccessibilityType:(CFTypeRef)accessibilityType;
#endif

@end
