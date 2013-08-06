/* Copyright 2013 Google Inc. All Rights Reserved.
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

#import "PacoEventUploader.h"
#import "Reachability.h"
#import "PacoClient.h"
#import "PacoService.h"

@interface PacoEventUploader ()

@property(atomic, assign) BOOL isWorking;

@end

@implementation PacoEventUploader

- (id)initWithDelegate:(id<PacoEventUploaderDelegate>)delegate {
  self = [super init];
  if (self) {
    _delegate = delegate;
  }
  return self;
}

+ (PacoEventUploader*)uploaderWithDelegate:(id<PacoEventUploaderDelegate>)delegate {
  return [[PacoEventUploader alloc] initWithDelegate:delegate];
}

- (void)dealloc {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)startObserveReachability {
  @synchronized(self) {
    if (!self.isWorking) {
      return;
    }
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(reachabilityChanged:)
                                                 name:kReachabilityChangedNotification
                                               object:nil];
  }
}

- (void)stopObserveReachability {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)reachabilityChanged:(NSNotification*)notification {
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
    Reachability* reach = (Reachability*)[notification object];
    
    @synchronized(self) {
      if ([reach isReachable] && self.isWorking) {
        NSLog(@"[Reachable]: Online Now!");
        [self uploadEvents];
      }else {
        NSLog(@"[Reachable]: Offline Now!");
      }
    }    
  }); 
}

- (void)uploadEvents {
  @synchronized(self) {
    NSArray* pendingEvents = [self.delegate currentPendingEvents];
    NSAssert([pendingEvents count] > 0, @"there should be pending events!");
    
    self.isWorking = YES;
    if ([[PacoClient sharedInstance].reachability isReachable]) {
      [self stopObserveReachability];
      [self submitEvents:pendingEvents];
    }else {
      [self startObserveReachability];
    }
  }
}

- (void)stopUploading {
  @synchronized(self) {
    self.isWorking = NO;
    [self stopObserveReachability];
  }
}

- (BOOL)isOfflineError:(NSError*)error {
  if (error == nil) {
    return NO;
  }

  if ([error.domain isEqualToString:NSURLErrorDomain]) {
    NSError* underlyingError = [error.userInfo objectForKey:NSUnderlyingErrorKey];
    if ([underlyingError.domain isEqualToString:(NSString*)kCFErrorDomainCFNetwork] &&
        underlyingError.code == NSURLErrorNotConnectedToInternet) {
      return YES;
    }
  }
  return NO;
}

- (void)submitEvents:(NSArray*)pendingEvents {
  void(^completionBlock)(NSError*) = ^(NSError* error){
    //Since this block is fired on main thread, send it to a background thread
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
      if (error != nil && [self isOfflineError:error]) {
        [self startObserveReachability];
        return;
      }
      
      [self stopUploading];
      
      //TODO: other proper error handling
      [self.delegate markEventsComplete:pendingEvents];
    });
  };
  
  [[PacoClient sharedInstance].service submitEventList:pendingEvents
                                   withCompletionBlock:completionBlock];
}


#pragma mark Public API
- (void)startUploading {
  @synchronized(self) {
    if (self.isWorking) {
      return;
    }
    
    NSArray* pendingEvents = [self.delegate currentPendingEvents];
    if (0 == [pendingEvents count]) {
      return;
    }
    [self uploadEvents];
  }
}

@end
