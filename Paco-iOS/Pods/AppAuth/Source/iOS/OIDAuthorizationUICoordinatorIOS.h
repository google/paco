/*! @file OIDAuthorizationUICoordinator.h
    @brief AppAuth iOS SDK
    @copyright
        Copyright 2016 Google Inc. All Rights Reserved.
    @copydetails
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

#import <UIKit/UIKit.h>

#import "OIDAuthorizationUICoordinator.h"

@class SFSafariViewController;

NS_ASSUME_NONNULL_BEGIN

/*! @brief Allows library consumers to bootstrap an @c SFSafariViewController as they see fit.
    @remarks Useful for customizing tint colors and presentation styles.
 */
@protocol OIDSafariViewControllerFactory

/*! @brief Creates and returns a new @c SFSafariViewController.
    @param URL The URL which the @c SFSafariViewController should load initially.
 */
- (SFSafariViewController *)safariViewControllerWithURL:(NSURL *)URL;

@end

/*! @brief An iOS specific authorization UI Coordinator that uses a \SFSafariViewController to
        present an authorization request.
 */
@interface OIDAuthorizationUICoordinatorIOS : NSObject<OIDAuthorizationUICoordinator>

/*! @brief Allows library consumers to change the @c OIDSafariViewControllerFactory used to create
        new instances of @c SFSafariViewController.
    @remarks Useful for customizing tint colors and presentation styles.
    @param factory The @c OIDSafariViewControllerFactory to use for creating new instances of
        @c SFSafariViewController.
 */
+ (void)setSafariViewControllerFactory:(id<OIDSafariViewControllerFactory>)factory;

/*! @internal
    @brief Unavailable. Please use @c initWithPresentingViewController:
 */
- (nullable instancetype)init NS_UNAVAILABLE;

/*! @brief The designated initializer.
    @param parentViewController The view controller from which to present the
        \SFSafariViewController.
 */
- (nullable instancetype)initWithPresentingViewController:(UIViewController *)parentViewController
    NS_DESIGNATED_INITIALIZER;

@end

NS_ASSUME_NONNULL_END
