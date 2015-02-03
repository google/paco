/* Copyright (c) 2010 Google Inc.
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
//  GTMHTTPFetcherService.m
//

#import "GTMHTTPFetcherService.h"

@interface GTMHTTPFetcher (ServiceMethods)
- (BOOL)beginFetchMayDelay:(BOOL)mayDelay
              mayAuthorize:(BOOL)mayAuthorize;
@end

@interface GTMHTTPFetcherService ()
@property (retain, readwrite) NSDictionary *delayedHosts;
@property (retain, readwrite) NSDictionary *runningHosts;
@end

@implementation GTMHTTPFetcherService

@synthesize maxRunningFetchersPerHost = maxRunningFetchersPerHost_,
            runLoopModes = runLoopModes_,
            credential = credential_,
            proxyCredential = proxyCredential_,
            cookieStorageMethod = cookieStorageMethod_,
            shouldFetchInBackground = shouldFetchInBackground_,
            fetchHistory = fetchHistory_;

- (id)init {
  self = [super init];
  if (self) {
    fetchHistory_ = [[GTMHTTPFetchHistory alloc] init];
    delayedHosts_ = [[NSMutableDictionary alloc] init];
    runningHosts_ = [[NSMutableDictionary alloc] init];
    cookieStorageMethod_ = kGTMHTTPFetcherCookieStorageMethodFetchHistory;

    // The default limit is 10 simultaneous fetchers targeting each host
    maxRunningFetchersPerHost_ = 10;
}
  return self;
}

- (void)dealloc {
  [delayedHosts_ release];
  [runningHosts_ release];
  [fetchHistory_ release];
  [runLoopModes_ release];
  [credential_ release];
  [proxyCredential_ release];
  [authorizer_ release];

  [super dealloc];
}

#pragma mark Generate a new fetcher

- (GTMHTTPFetcher *)fetcherWithRequest:(NSURLRequest *)request {
  GTMHTTPFetcher *fetcher = [GTMHTTPFetcher fetcherWithRequest:request];

  fetcher.fetchHistory = self.fetchHistory;
  fetcher.runLoopModes = self.runLoopModes;
  fetcher.cookieStorageMethod = self.cookieStorageMethod;
  fetcher.credential = self.credential;
  fetcher.proxyCredential = self.proxyCredential;
  fetcher.shouldFetchInBackground = self.shouldFetchInBackground;
  fetcher.authorizer = self.authorizer;
  fetcher.service = self;

  return fetcher;
}

- (GTMHTTPFetcher *)fetcherWithURL:(NSURL *)requestURL {
  return [self fetcherWithRequest:[NSURLRequest requestWithURL:requestURL]];
}

- (GTMHTTPFetcher *)fetcherWithURLString:(NSString *)requestURLString {
  return [self fetcherWithURL:[NSURL URLWithString:requestURLString]];
}

#pragma mark Queue Management

- (void)addRunningFetcher:(GTMHTTPFetcher *)fetcher
                  forHost:(NSString *)host {
  // Add to the array of running fetchers for this host, creating the array
  // if needed
  NSMutableArray *runningForHost = [runningHosts_ objectForKey:host];
  if (runningForHost == nil) {
    runningForHost = [NSMutableArray arrayWithObject:fetcher];
    [runningHosts_ setObject:runningForHost forKey:host];
  } else {
    [runningForHost addObject:fetcher];
  }
}

- (void)addDelayedFetcher:(GTMHTTPFetcher *)fetcher
                  forHost:(NSString *)host {
  // Add to the array of delayed fetchers for this host, creating the array
  // if needed
  NSMutableArray *delayedForHost = [delayedHosts_ objectForKey:host];
  if (delayedForHost == nil) {
    delayedForHost = [NSMutableArray arrayWithObject:fetcher];
    [delayedHosts_ setObject:delayedForHost forKey:host];
  } else {
    [delayedForHost addObject:fetcher];
  }
}

- (BOOL)fetcherShouldBeginFetching:(GTMHTTPFetcher *)fetcher {
  // Entry point from the fetcher
  @synchronized(self) {
    NSString *host = [[[fetcher mutableRequest] URL] host];

    if ([host length] == 0) {
#if DEBUG
      NSAssert1(0, @"%@ lacks host", fetcher);
#endif
      return YES;
    }

    NSMutableArray *runningForHost = [runningHosts_ objectForKey:host];
    if (runningForHost != nil
        && [runningForHost indexOfObjectIdenticalTo:fetcher] != NSNotFound) {
#if DEBUG
      NSAssert1(0, @"%@ was already running", fetcher);
#endif
      return YES;
    }

    // We'll save the host that serves as the key for this fetcher's array
    // to avoid any chance of the underlying request changing, stranding
    // the fetcher in the wrong array
    fetcher.serviceHost = host;
    fetcher.thread = [NSThread currentThread];

    if (maxRunningFetchersPerHost_ == 0
        || maxRunningFetchersPerHost_ > [runningForHost count]) {
      [self addRunningFetcher:fetcher forHost:host];
      return YES;
    } else {
      [self addDelayedFetcher:fetcher forHost:host];
      return NO;
    }
  }
  return YES;
}

// Fetcher start and stop methods, invoked on the appropriate thread for
// the fetcher
- (void)startFetcherOnCurrentThread:(GTMHTTPFetcher *)fetcher {
  [fetcher beginFetchMayDelay:NO
                 mayAuthorize:YES];
}

- (void)startFetcher:(GTMHTTPFetcher *)fetcher {
  NSThread *thread = [fetcher thread];
  if ([thread isEqual:[NSThread currentThread]]) {
    // Same thread
    [self startFetcherOnCurrentThread:fetcher];
  } else {
    // Different thread
    [self performSelector:@selector(startFetcherOnCurrentThread:)
                 onThread:thread
               withObject:fetcher
            waitUntilDone:NO];
  }
}

- (void)stopFetcherOnCurrentThread:(GTMHTTPFetcher *)fetcher {
  [fetcher stopFetching];
}

- (void)stopFetcher:(GTMHTTPFetcher *)fetcher {
  NSThread *thread = [fetcher thread];
  if ([thread isEqual:[NSThread currentThread]]) {
    // Same thread
    [self stopFetcherOnCurrentThread:fetcher];
  } else {
    // Different thread
    [self performSelector:@selector(stopFetcherOnCurrentThread:)
                 onThread:thread
               withObject:fetcher
            waitUntilDone:NO];
  }
}



- (void)fetcherDidStop:(GTMHTTPFetcher *)fetcher {
  // Entry point from the fetcher
  @synchronized(self) {
    NSString *host = fetcher.serviceHost;
    if (!host) {
      // fetcher has been stopped previously
      return;
    }

    NSMutableArray *runningForHost = [runningHosts_ objectForKey:host];
    [runningForHost removeObject:fetcher];

    NSMutableArray *delayedForHost = [delayedHosts_ objectForKey:host];
    [delayedForHost removeObject:fetcher];

    while ([delayedForHost count] > 0
           && [runningForHost count] < maxRunningFetchersPerHost_) {
      // Start another delayed fetcher running
      GTMHTTPFetcher *nextFetcher = [delayedForHost objectAtIndex:0];

      [self addRunningFetcher:nextFetcher forHost:host];
      runningForHost = [runningHosts_ objectForKey:host];

      [delayedForHost removeObjectAtIndex:0];
      [self startFetcher:nextFetcher];
    }

    if ([runningForHost count] == 0) {
      // None left; remove the empty array
      [runningHosts_ removeObjectForKey:host];
    }

    if ([delayedForHost count] == 0) {
      [delayedHosts_ removeObjectForKey:host];
    }

    // The fetcher is no longer in the running or the delayed array,
    // so remove its host and thread properties
    fetcher.serviceHost = nil;
    fetcher.thread = nil;
  }
}

- (void)stopAllFetchers {
  // Remove fetchers from the delayed list to avoid fetcherDidStop: from
  // starting more fetchers running as a side effect of stopping one
  NSArray *delayedForHosts = [delayedHosts_ allValues];
  [delayedHosts_ removeAllObjects];

  for (NSArray *delayedForHost in delayedForHosts) {
    for (GTMHTTPFetcher *fetcher in delayedForHost) {
      [self stopFetcher:fetcher];
    }
  }

  NSArray *runningForHosts = [runningHosts_ allValues];
  [runningHosts_ removeAllObjects];

  for (NSArray *runningForHost in runningForHosts) {
    for (GTMHTTPFetcher *fetcher in runningForHost) {
      [self stopFetcher:fetcher];
    }
  }
}

#pragma mark Fetch History Settings

// Turn on data caching to receive a copy of previously-retrieved objects.
// Otherwise, fetches may return status 304 (No Change) rather than actual data
- (void)setShouldCacheETaggedData:(BOOL)flag {
  self.fetchHistory.shouldCacheETaggedData = flag;
}

- (BOOL)shouldCacheETaggedData {
  return self.fetchHistory.shouldCacheETaggedData;
}

- (void)setETaggedDataCacheCapacity:(NSUInteger)totalBytes {
  self.fetchHistory.memoryCapacity = totalBytes;
}

- (NSUInteger)ETaggedDataCacheCapacity {
  return self.fetchHistory.memoryCapacity;
}

- (void)setShouldRememberETags:(BOOL)flag {
  self.fetchHistory.shouldRememberETags = flag;
}

- (BOOL)shouldRememberETags {
  return self.fetchHistory.shouldRememberETags;
}

// reset the ETag cache to avoid getting a Not Modified status
// based on prior queries
- (void)clearETaggedDataCache {
  [self.fetchHistory clearETaggedDataCache];
}

- (void)clearHistory {
  [self clearETaggedDataCache];
  [self.fetchHistory removeAllCookies];
}

#pragma mark Accessors

- (NSDictionary *)runningHosts {
  return runningHosts_;
}

- (void)setRunningHosts:(NSDictionary *)dict {
  [runningHosts_ autorelease];
  runningHosts_ = [dict mutableCopy];
}

- (NSDictionary *)delayedHosts {
  return delayedHosts_;
}

- (void)setDelayedHosts:(NSDictionary *)dict {
  [delayedHosts_ autorelease];
  delayedHosts_ = [dict mutableCopy];
}

- (id <GTMFetcherAuthorizationProtocol>)authorizer {
  return authorizer_;
}

- (void)setAuthorizer:(id <GTMFetcherAuthorizationProtocol>)obj {
  [authorizer_ autorelease];
  authorizer_ = [obj retain];

  // Use the fetcher service for the authorization fetches if the auth
  // object supports fetcher services
  if ([authorizer_ respondsToSelector:@selector(setFetcherService:)]) {
    [authorizer_ setFetcherService:self];
  }
}

@end
