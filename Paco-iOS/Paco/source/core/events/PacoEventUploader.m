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
#import "NSError+Paco.h"

static int const kMaxNumOfEventsToUpload = 50;

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
    NSArray* pendingEvents = [self.delegate allPendingEvents];
    NSAssert([pendingEvents count] > 0, @"there should be pending events!");
    
    self.isWorking = YES;
    if ([[PacoClient sharedInstance].reachability isReachable]) {
      [self stopObserveReachability];
      
      dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [self submitAllPendingEvents:pendingEvents];
      });
    }else {
      [self startObserveReachability];
    }
  }
}

- (BOOL)isOfflineError:(NSError*)error {
  if (error == nil) {
    return NO;
  } else {
    return [error isOfflineError];
  }
}

- (void)submitAllPendingEvents:(NSArray*)allPendingEvents {
  int totalNumOfEvents = [allPendingEvents count];
  int start = 0;
  int size = MIN(totalNumOfEvents, kMaxNumOfEventsToUpload);
  
  __block int numOfFinishedEvents = 0;

  while (size > 0) {
    NSRange range = NSMakeRange(start, size);
    NSArray* events = [allPendingEvents subarrayWithRange:range];
    NSAssert([events count] > 0, @"events should have at least one element!");
    
    void(^finalBlock)(NSArray*, NSError*) = ^(NSArray* successEventIndexes, NSError* error){
      //Since this block is fired on main thread, send it to a background thread
      dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSAssert([successEventIndexes count] <= [events count],
                 @"successEventIndexes count is not correct!");
        
        //If the error is not nil and is an offline error, we will wait until internet is online
        //and refetch all pending events and try to re-submit them again 
        if (![self isOfflineError:error]) {
          numOfFinishedEvents += [events count];
        }
        
        if (error == nil) {
          if ([successEventIndexes count] < [events count]) {
            NSLog(@"[Error]%d events uploaded, but %d events failed!",
                  [successEventIndexes count], [events count] - [successEventIndexes count]);
          } else {
            NSLog(@"%d events successfully uploaded!", [successEventIndexes count]);
          }
          
          NSMutableArray* successEvents = [NSMutableArray arrayWithCapacity:[successEventIndexes count]];
          for (NSNumber* indexNum in successEventIndexes) {
            [successEvents addObject:[events objectAtIndex:[indexNum intValue]]];
          }
          [self.delegate markEventsComplete:successEvents];          
        } else {
          if ([self isOfflineError:error]) {
            [self startObserveReachability];
          } else {
            //authentication error, server 500 error, client 400 error, etc.
            NSLog(@"Failed to upload %d events! Error: %@", [events count], [error description]);
          }
        }
        
        if (numOfFinishedEvents == totalNumOfEvents) {
          NSLog(@"Finished uploading!");
          [self stopUploading];
        }
        
      });
    };
    
    [[PacoClient sharedInstance].service submitEventList:events
                                     withCompletionBlock:finalBlock];
    
    start += size;
    size = MIN(totalNumOfEvents - start, kMaxNumOfEventsToUpload);
  }

}


#pragma mark Public API
- (void)startUploading {
  //if user is not logged in yet, wait until log in finishes
  if (![[PacoClient sharedInstance] isLoggedIn]) {
    return;
  }

  @synchronized(self) {
    if (self.isWorking) {
      return;
    }
    
    if (![self.delegate hasPendingEvents]) {
      return;
    }
    
    [self uploadEvents];
  }
}


- (void)stopUploading {
  @synchronized(self) {
    self.isWorking = NO;
    [self stopObserveReachability];
  }
}



@end
