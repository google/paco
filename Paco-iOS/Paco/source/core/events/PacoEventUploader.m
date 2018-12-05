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
#import "NSString+Paco.h"
#import "PacoEvent.h"
#import "UIImage+Paco.h"

static int const kMaxNumOfEventsToUpload = 50;

@interface PacoEventUploader ()

@property(atomic, assign) BOOL isWorking;
@property(nonatomic, copy) UploadCompletionBlock completionBlock;

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


- (void)uploadEvents {
  @synchronized(self) {
    if (![[PacoClient sharedInstance].reachability isReachable]) {
      DDLogWarn(@"[Reachable]: Offline Now, won't upload events.");
      if (self.completionBlock) {
        self.completionBlock(NO);
      }
      return;
    }
    
    NSArray* pendingEvents = [self.delegate allPendingEvents];
    NSAssert([pendingEvents count] > 0, @"there should be pending events!");
    
    self.isWorking = YES;
    [self submitAllPendingEvents:pendingEvents];
  }
}


- (void)submitAllPendingEvents:(NSArray*)allPendingEvents {
  int totalNumOfEvents = (int)[allPendingEvents count];
  int start = 0;
  int size = MIN(totalNumOfEvents, kMaxNumOfEventsToUpload);
  
  __block int numOfFinishedEvents = 0;
  __block int numOfSuccessUploading = 0;
  
  while (size > 0) {
    NSRange range = NSMakeRange(start, size);
    NSArray* events = [allPendingEvents subarrayWithRange:range];
    NSAssert([events count] > 0, @"events should have at least one element!");
    
    void(^finalBlock)(NSArray*, NSError*) = ^(NSArray* successEventIndexes, NSError* error){
      NSAssert([successEventIndexes count] <= [events count],
               @"successEventIndexes count is not correct!");
      
      numOfFinishedEvents += [events count];
      
      if (error) {
        //offline error, authentication error, server 500 error, client 400 error, etc.
        DDLogError(@"Failed to upload %lu events! Error: %@", (unsigned long)[events count], [error description]);
      } else {
        if ([successEventIndexes count] < [events count]) {
          DDLogError(@"[Error]%lu events successfully uploaded, %lu events failed!",
                     (unsigned long)[successEventIndexes count], (unsigned long)([events count] - [successEventIndexes count]));
        } else {
          DDLogInfo(@"%lu events successfully uploaded!", (unsigned long)[successEventIndexes count]);
        }
        
        NSMutableArray* successEvents = [NSMutableArray arrayWithCapacity:[successEventIndexes count]];
        for (NSNumber* indexNum in successEventIndexes) {
          [successEvents addObject:events[[indexNum intValue]]];
        }
        if ([successEvents count] > 0) {
          [self.delegate markEventsComplete:successEvents];
          numOfSuccessUploading += [successEvents count];
        }
      }
      
      if (numOfFinishedEvents == totalNumOfEvents) {
        DDLogInfo(@"Finished uploading!");
        [self stopUploading];
        BOOL success = (numOfSuccessUploading > 0) ? YES : NO;
        if (self.completionBlock) {
          self.completionBlock(success);
        }
      }
    };
    
    [[PacoClient sharedInstance].service submitEventList:events
                                     withCompletionBlock:finalBlock];
    
    start += size;
    size = MIN(totalNumOfEvents - start, kMaxNumOfEventsToUpload);
  }

}


#pragma mark Public API
- (void)startUploadingWithBlock:(UploadCompletionBlock)completionBlock {
  @synchronized(self) {
    if (self.isWorking) {
      DDLogWarn(@"EventUploading is already working.");
      if (completionBlock) {
        completionBlock(NO);
      }
      return;
    }

    if (![self.delegate hasPendingEvents]) {
      DDLogWarn(@"EventUploader won't start uploading since there isn't any pending events");
      if (completionBlock) {
        completionBlock(YES);
      }
      return;
    }
    DDLogInfo(@"EventUploader starts uploading ...");
    self.completionBlock = completionBlock;
    [self uploadEvents];
  }
}


- (void)stopUploading {
  @synchronized(self) {
    self.isWorking = NO;
  }
}



@end
