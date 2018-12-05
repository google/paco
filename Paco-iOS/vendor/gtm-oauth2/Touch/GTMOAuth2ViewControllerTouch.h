/* Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
// GTMOAuth2ViewControllerTouch.h
//
// This view controller for iPhone handles sign-in via OAuth to Google or
// other services.
//
// This controller is not reusable; create a new instance of this controller
// every time the user will sign in.
//

#if GTM_INCLUDE_OAUTH2 || !GDATA_REQUIRE_SERVICE_INCLUDES

#import <Foundation/Foundation.h>

#if TARGET_OS_IPHONE

#import <UIKit/UIKit.h>

#import "GTMOAuth2Authentication.h"

#undef _EXTERN
#undef _INITIALIZE_AS
#ifdef GTMOAUTH2VIEWCONTROLLERTOUCH_DEFINE_GLOBALS
#define _EXTERN
#define _INITIALIZE_AS(x) =x
#else
#define _EXTERN extern
#define _INITIALIZE_AS(x)
#endif

_EXTERN NSString* const kGTMOAuth2KeychainErrorDomain       _INITIALIZE_AS(@"com.google.GTMOAuthKeychain");


@class GTMOAuth2SignIn;
@class GTMOAuth2ViewControllerTouch;

@interface GTMOAuth2ViewControllerTouch : UIViewController<UINavigationControllerDelegate, UIWebViewDelegate> {
 @private
  UIButton *backButton_;
  UIButton *forwardButton_;
  UIView *navButtonsView_;
  UIBarButtonItem *rightBarButtonItem_;
  UIWebView *webView_;

  // The object responsible for the sign-in networking sequence; it holds
  // onto the authentication object as well.
  GTMOAuth2SignIn *signIn_;

  // the page request to load when awakeFromNib occurs
  NSURLRequest *request_;

  // The user we're calling back
  //
  // The delegate is retained only until the callback is invoked
  // or the sign-in is canceled
  id delegate_;
  SEL finishedSelector_;

#if NS_BLOCKS_AVAILABLE
  void (^completionBlock_)(GTMOAuth2ViewControllerTouch *, GTMOAuth2Authentication *, NSError *);
#endif

  NSString *keychainItemName_;

  // if non-nil, the html string to be displayed immediately upon opening
  // of the web view
  NSString *initialHTMLString_;

  // if non-nil, the URL for which cookies will be deleted when the
  // browser view is dismissed
  NSURL *browserCookiesURL_;

  id userData_;
  NSMutableDictionary *properties_;

  // We delegate the decision to our owning NavigationController (if any).
  // But, the NavigationController will call us back, and ask us.
  // BOOL keeps us from infinite looping.
  BOOL isInsideShouldAutorotateToInterfaceOrientation_;

  // YES, when view first shown in this signIn session.
  BOOL isViewShown_;

  // YES, after the view has fully transitioned in.
  BOOL didViewAppear_;

  // To prevent us from calling our delegate's selector more than once.
  BOOL hasCalledFinished_;

  // Set in a webView callback.
  BOOL hasDoneFinalRedirect_;

  // Set during the pop initiated by the sign-in object
  BOOL isPoppingSelf_;
}

// the application and service name to use for saving the auth tokens
// to the keychain
@property (nonatomic, copy) NSString *keychainItemName;

// optional html string displayed immediately upon opening the web view
//
// This string is visible just until the sign-in web page loads, and
// may be used for a "Loading..." type of message or to set the
// initial view color
@property (nonatomic, copy) NSString *initialHTMLString;

// the underlying object to hold authentication tokens and authorize http
// requests
@property (nonatomic, retain, readonly) GTMOAuth2Authentication *authentication;

// the underlying object which performs the sign-in networking sequence
@property (nonatomic, retain, readonly) GTMOAuth2SignIn *signIn;

// user interface elements
@property (nonatomic, retain) IBOutlet UIButton *backButton;
@property (nonatomic, retain) IBOutlet UIButton *forwardButton;
@property (nonatomic, retain) IBOutlet UIView *navButtonsView;
@property (nonatomic, retain) IBOutlet UIBarButtonItem *rightBarButtonItem;
@property (nonatomic, retain) IBOutlet UIWebView *webView;

// the default timeout for an unreachable network during display of the
// sign-in page is 10 seconds; set this to 0 to have no timeout
@property (nonatomic, assign) NSTimeInterval networkLossTimeoutInterval;

// if set, cookies are deleted for this URL when the view is hidden
//
// For Google sign-ins, this is set by default to https://google.com/accounts
// but it may be explicitly set to nil to disable clearing of browser cookies
@property (nonatomic, retain) NSURL *browserCookiesURL;

// userData is retained for the convenience of the caller
@property (nonatomic, retain) id userData;

// Stored property values are retained for the convenience of the caller
- (void)setProperty:(id)obj forKey:(NSString *)key;
- (id)propertyForKey:(NSString *)key;

@property (nonatomic, retain) NSDictionary *properties;

// init method for authenticating to Google services
//
// scope is the requested scope of authorization
//   (like "http://www.google.com/m8/feeds")
//
// keychain item name is used for storing the token on the keychain,
//   keychainItemName should be like "My Application: Google Latitude"
//   (or set to nil if no persistent keychain storage is desired)
//
// the delegate is retained only until the finished selector is invoked
//   or the sign-in is canceled
//
// If you don't like the default nibName and bundle, you can change them
// using the UIViewController properties once you've made one of these.
//
// finishedSelector is called after authentication completes. It should follow
// this signature.
//
// - (void)viewController:(GTMOAuth2ViewControllerTouch *)viewController
//       finishedWithAuth:(GTMOAuth2Authentication *)auth
//                  error:(NSError *)error;
//
#if !GTM_OAUTH2_SKIP_GOOGLE_SUPPORT
- (id)initWithScope:(NSString *)scope
           clientID:(NSString *)clientID
       clientSecret:(NSString *)clientSecret
   keychainItemName:(NSString *)keychainItemName
           delegate:(id)delegate
   finishedSelector:(SEL)finishedSelector;

#if NS_BLOCKS_AVAILABLE
- (id)initWithScope:(NSString *)scope
           clientID:(NSString *)clientID
       clientSecret:(NSString *)clientSecret
   keychainItemName:(NSString *)keychainItemName
  completionHandler:(void (^)(GTMOAuth2ViewControllerTouch *viewController, GTMOAuth2Authentication *auth, NSError *error))handler;
#endif
#endif

// init method for authenticating to non-Google services, taking
//   explicit endpoint URLs and an authentication object
//
// this is the designated initializer
- (id)initWithAuthentication:(GTMOAuth2Authentication *)auth
            authorizationURL:(NSURL *)authorizationURL
            keychainItemName:(NSString *)keychainItemName
                    delegate:(id)delegate
            finishedSelector:(SEL)finishedSelector;

#if NS_BLOCKS_AVAILABLE
- (id)initWithAuthentication:(GTMOAuth2Authentication *)auth
            authorizationURL:(NSURL *)authorizationURL
            keychainItemName:(NSString *)keychainItemName
           completionHandler:(void (^)(GTMOAuth2ViewControllerTouch *viewController, GTMOAuth2Authentication *auth, NSError *error))handler;
#endif

// Override default in UIViewController. If we have a navigationController, ask
// it. else default result (i.e., Portrait mode only).
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation;

// subclasses may override authNibName to specify a custom name
+ (NSString *)authNibName;

- (void)cancelSigningIn;

// revocation of an authorized token from Google
#if !GTM_OAUTH2_SKIP_GOOGLE_SUPPORT
+ (void)revokeTokenForGoogleAuthentication:(GTMOAuth2Authentication *)auth;
#endif

//
// Keychain
//

// create an authentication object for Google services from the access
// token and secret stored in the keychain; if no token is available, return
// an unauthorized auth object
#if !GTM_OAUTH2_SKIP_GOOGLE_SUPPORT
+ (GTMOAuth2Authentication *)authForGoogleFromKeychainForName:(NSString *)keychainItemName
                                                     clientID:(NSString *)clientID
                                                 clientSecret:(NSString *)clientSecret;
#endif

// add tokens from the keychain, if available, to the authentication object
//
// returns YES if the authentication object was authorized from the keychain
+ (BOOL)authorizeFromKeychainForName:(NSString *)keychainItemName
                      authentication:(GTMOAuth2Authentication *)auth;

// method for deleting the stored access token and secret, useful for "signing
// out"
+ (BOOL)removeAuthFromKeychainForName:(NSString *)keychainItemName;

// method for saving the stored access token and secret; typically, this method
// is used only by this.
+ (BOOL)saveParamsToKeychainForName:(NSString *)keychainItemName
                     authentication:(GTMOAuth2Authentication *)auth;

@end

// To function, GTMOAuth2ViewControllerTouch needs a certain amount of access
// to the iPhone's keychain. To keep things simple, its keychain access is
// broken out into a helper class. We declare it here in case you'd like to use
// it too, to store passwords.

enum {
  kGTMOAuth2KeychainErrorBadArguments = -1301,
  kGTMOAuth2KeychainErrorNoPassword = -1302
};


@interface GTMOAuth2Keychain : NSObject

+ (GTMOAuth2Keychain *)defaultKeychain;

// OK to pass nil for the error parameter.
- (NSString *)passwordForService:(NSString *)service
                         account:(NSString *)account
                           error:(NSError **)error;

// OK to pass nil for the error parameter.
- (BOOL)removePasswordForService:(NSString *)service
                         account:(NSString *)account
                           error:(NSError **)error;

// OK to pass nil for the error parameter.
- (BOOL)setPassword:(NSString *)password
         forService:(NSString *)service
            account:(NSString *)account
              error:(NSError **)error;

// For unit tests: allow setting a mock object
+ (void)setDefaultKeychain:(GTMOAuth2Keychain *)keychain;

@end

#endif // TARGET_OS_IPHONE

#endif // #if GTM_INCLUDE_OAUTH2 || !GDATA_REQUIRE_SERVICE_INCLUDES
