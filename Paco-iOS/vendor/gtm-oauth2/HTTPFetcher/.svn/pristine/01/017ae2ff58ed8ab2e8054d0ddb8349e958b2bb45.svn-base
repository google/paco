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

#import <XCTest/XCTest.h>

#import "GTMHTTPFetcherTestServer.h"
#import "GTMHTTPFetcherService.h"

@interface GTMHTTPFetcherServiceTest : XCTestCase {
  GTMHTTPFetcherTestServer *testServer_;
  BOOL isServerRunning_;
}

@end

@implementation GTMHTTPFetcherServiceTest

// file available in Tests folder
static NSString *const kValidFileName = @"gettysburgaddress.txt";

- (NSString *)docRootPath {
  // find a test file
  NSBundle *testBundle = [NSBundle bundleForClass:[self class]];
  XCTAssertNotNil(testBundle);

  // use the directory of the test file as the root directory for our server
  NSString *docFolder = [testBundle resourcePath];
  return docFolder;
}

- (void)setUp {
  NSString *docRoot = [self docRootPath];

  testServer_ = [[GTMHTTPFetcherTestServer alloc] initWithDocRoot:docRoot];
  isServerRunning_ = (testServer_ != nil);

  XCTAssertTrue(isServerRunning_,
               @">>> http test server failed to launch; skipping"
               " service tests\n");
}

- (void)tearDown {
  [testServer_ release];
  testServer_ = nil;

  isServerRunning_ = NO;
}

- (void)testFetcherService {
  if (!isServerRunning_) return;

  // Utility blocks for counting array entries for a specific host
  NSUInteger (^URLsPerHost) (NSArray *, NSString *) = ^(NSArray *URLs,
                                                        NSString *host) {
    NSUInteger counter = 0;
    for (NSURL *url in URLs) {
      if ([host isEqual:[url host]]) counter++;
    }
    return counter;
  };
  
  NSUInteger (^FetchersPerHost) (NSArray *, NSString *) = ^(NSArray *fetchers,
                                                            NSString *host) {
    NSArray *fetcherURLs = [fetchers valueForKeyPath:@"mutableRequest.URL"];
    return URLsPerHost(fetcherURLs, host);
  };

  // Utility block for finding the minimum priority fetcher for a specific host
  NSInteger (^PriorityPerHost) (NSArray *, NSString *) = ^(NSArray *fetchers,
                                                            NSString *host) {
    NSInteger val = NSIntegerMax;
    for (GTMHTTPFetcher *fetcher in fetchers) {
      if ([host isEqual:[[fetcher.mutableRequest URL] host]]) {
        val = MIN(val, fetcher.servicePriority);
      }
    }
    return val;
  };

  // We'll verify we fetched from the server the same data that is on disk
  NSString *gettysburgPath = [testServer_ localPathForFile:kValidFileName];
  NSData *gettysburgAddress = [NSData dataWithContentsOfFile:gettysburgPath];

  NSNotificationCenter *nc = [NSNotificationCenter defaultCenter];

  // We'll create 10 fetchers.  Only 2 should run simultaneously.
  // 1 should fail; the rest should succeeed.
  const NSUInteger kMaxRunningFetchersPerHost = 2;

  NSString *const kUserAgent = @"ServiceTest-UA";
  const NSTimeInterval kTimeout = 55;

  GTMHTTPFetcherService *service = [[[GTMHTTPFetcherService alloc] init] autorelease];
  service.maxRunningFetchersPerHost = kMaxRunningFetchersPerHost;
  service.fetchHistory.shouldRememberETags = NO;
  service.userAgent = kUserAgent;
  service.timeout = kTimeout;
  service.allowLocalhostRequest = YES;

  // Make URLs for a valid fetch, a fetch that returns a status error,
  // and a valid fetch with a different host
  NSURL *validFileURL = [testServer_ localURLForFile:kValidFileName];

  NSString *invalidFile = [kValidFileName stringByAppendingString:@"?status=400"];
  NSURL *invalidFileURL = [testServer_ localURLForFile:invalidFile];

  NSString *validURLStr = [validFileURL absoluteString];
  NSString *altValidURLStr = [validURLStr stringByReplacingOccurrencesOfString:@"localhost"
                                                                    withString:@"127.0.0.1"];
  NSURL *altValidURL = [NSURL URLWithString:altValidURLStr];

  XCTAssertEqualObjects([validFileURL host], @"localhost", @"unexpected host");
  XCTAssertEqualObjects([invalidFileURL host], @"localhost", @"unexpected host");
  XCTAssertEqualObjects([altValidURL host], @"127.0.0.1", @"unexpected host");

  // Make an array with the urls from the different hosts, including one
  // that will fail with a status 400 error
  NSMutableArray *urlArray = [NSMutableArray array];
  for (int idx = 1; idx <= 4; idx++) [urlArray addObject:validFileURL];
  [urlArray addObject:invalidFileURL];
  for (int idx = 1; idx <= 5; idx++) [urlArray addObject:validFileURL];
  for (int idx = 1; idx <= 5; idx++) [urlArray addObject:altValidURL];
  for (int idx = 1; idx <= 5; idx++) [urlArray addObject:validFileURL];
  NSUInteger totalNumberOfFetchers = [urlArray count];

  __block NSMutableArray *pending = [NSMutableArray array];
  __block NSMutableArray *running = [NSMutableArray array];
  __block NSMutableArray *completed = [NSMutableArray array];

  NSUInteger priorityVal = 0;

  // Create all the fetchers
  for (NSURL *fileURL in urlArray) {
    GTMHTTPFetcher *fetcher = [service fetcherWithURL:fileURL];

    // Fetcher start notification
    [nc addObserverForName:kGTMHTTPFetcherStartedNotification
                    object:fetcher
                     queue:nil
                usingBlock:^(NSNotification *note) {
                  // Verify that we have at most two fetchers running for this
                  // fetcher's host
                  [running addObject:fetcher];
                  [pending removeObject:fetcher];

                  NSMutableURLRequest *fetcherReq = [fetcher mutableRequest];
                  NSURL *fetcherReqURL = [fetcherReq URL];
                  NSString *host = [fetcherReqURL host];
                  NSUInteger numberRunning = FetchersPerHost(running, host);
                  XCTAssertTrue(numberRunning > 0, @"count error");
                  XCTAssertTrue(numberRunning <= kMaxRunningFetchersPerHost,
                               @"too many running");

                  NSInteger pendingPriority = PriorityPerHost(pending, host);
                  XCTAssertTrue(fetcher.servicePriority <= pendingPriority,
                               @"a pending fetcher has greater priority");

                  XCTAssertEqual([service numberOfFetchers],
                                 [running count] + [pending count],
                                 @"fetcher count off");
                  XCTAssertEqual([service numberOfRunningFetchers],
                                 [running count], @"running off");
                  XCTAssertEqual([service numberOfDelayedFetchers],
                                 [pending count], @"delayed off");

                  NSArray *matches =
                    [service issuedFetchersWithRequestURL:fetcherReqURL];
                  NSUInteger idx = NSNotFound;
                  if (matches) {
                    idx = [matches indexOfObjectIdenticalTo:fetcher];
                  }
                  XCTAssertTrue(idx != NSNotFound, @"Missing %@ in %@",
                               fetcherReqURL, matches);
                  NSURL *fakeURL =
                    [NSURL URLWithString:@"http://example.com/bad"];
                  matches = [service issuedFetchersWithRequestURL:fakeURL];
                  XCTAssertEqual([matches count], (NSUInteger)0);

                  NSString *agent = [fetcherReq valueForHTTPHeaderField:@"User-Agent"];
                  XCTAssertEqualObjects(agent, kUserAgent);
                  XCTAssertEqual([fetcherReq timeoutInterval], kTimeout);
                }];

    // Fetcher stopped notification
    [nc addObserverForName:kGTMHTTPFetcherStoppedNotification
                    object:fetcher
                     queue:nil
                usingBlock:^(NSNotification *note) {
                  // Verify that we only have two fetchers running
                  [completed addObject:fetcher];
                  [running removeObject:fetcher];

                  NSString *host = [[[fetcher mutableRequest] URL] host];

                  NSUInteger numberRunning = FetchersPerHost(running, host);
                  NSUInteger numberPending = FetchersPerHost(pending, host);
                  NSUInteger numberCompleted = FetchersPerHost(completed, host);

                  XCTAssertTrue(numberRunning <= kMaxRunningFetchersPerHost,
                               @"too many running");
                  XCTAssertTrue(numberPending + numberRunning + numberCompleted <= URLsPerHost(urlArray, host),
                               @"%d issued running (pending:%u running:%u completed:%u)",
                               (unsigned int)totalNumberOfFetchers, (unsigned int)numberPending,
                               (unsigned int)numberRunning, (unsigned int)numberCompleted);

                  XCTAssertEqual([service numberOfFetchers],
                                 [running count] + [pending count] + 1,
                                 @"fetcher count off");
                  XCTAssertEqual([service numberOfRunningFetchers],
                                 [running count] + 1, @"running off");
                  XCTAssertEqual([service numberOfDelayedFetchers],
                                 [pending count], @"delayed off");
                }];

    [pending addObject:fetcher];

    // Set the fetch priority to a value that cycles 0, 1, -1, 0, ...
    priorityVal++;
    if (priorityVal > 1) priorityVal = -1;
    fetcher.servicePriority = priorityVal;

    // Start this fetcher
    [fetcher beginFetchWithCompletionHandler:^(NSData *fetchData, NSError *fetchError) {
      // Callback
      //
      // The query should be empty except for the URL with a status code
      NSString *query = [[[fetcher mutableRequest] URL] query];
      BOOL isValidRequest = ([query length] == 0);
      if (isValidRequest) {
        XCTAssertEqualObjects(fetchData, gettysburgAddress,
                             @"Bad fetch data");
        XCTAssertNil(fetchError, @"unexpected %@ %@",
                    fetchError, [fetchError userInfo]);
      } else {
        // This is the query with ?status=400
        XCTAssertEqual([fetchError code], (NSInteger) 400, @"expected error");
      }
    }];
  }

  [service waitForCompletionOfAllFetchersWithTimeout:10];

  XCTAssertEqual([pending count], (NSUInteger) 0,
                 @"still pending: %@", pending);
  XCTAssertEqual([running count], (NSUInteger) 0,
                 @"still running: %@", running);
  XCTAssertEqual([completed count], (NSUInteger) totalNumberOfFetchers,
                 @"incomplete");

  XCTAssertEqual([service numberOfFetchers], (NSUInteger) 0,
                 @"service non-empty");
}

- (void)testStopAllFetchers {
  if (!isServerRunning_) return;

  GTMHTTPFetcherService *service = [[[GTMHTTPFetcherService alloc] init] autorelease];
  service.maxRunningFetchersPerHost = 2;
  service.fetchHistory.shouldRememberETags = NO;
  service.allowLocalhostRequest = YES;

  // Create three fetchers for each of two URLs, so there should be
  // two running and one delayed for each
  NSURL *validFileURL = [testServer_ localURLForFile:kValidFileName];

  NSString *validURLStr = [validFileURL absoluteString];
  NSString *altValidURLStr = [validURLStr stringByReplacingOccurrencesOfString:@"localhost"
                                                                    withString:@"127.0.0.1"];
  NSURL *altValidURL = [NSURL URLWithString:altValidURLStr];

  // Add three fetches for each URL
  NSMutableArray *urlArray = [NSMutableArray array];
  [urlArray addObject:validFileURL];
  [urlArray addObject:altValidURL];
  [urlArray addObject:validFileURL];
  [urlArray addObject:altValidURL];
  [urlArray addObject:validFileURL];
  [urlArray addObject:altValidURL];

  // Create and start all the fetchers
  for (NSURL *fileURL in urlArray) {
    GTMHTTPFetcher *fetcher = [service fetcherWithURL:fileURL];
    [fetcher beginFetchWithCompletionHandler:^(NSData *fetchData, NSError *fetchError) {
      // We shouldn't reach any of the callbacks
      XCTFail(@"Fetcher completed but should have been stopped");
    }];
  }

  // Two hosts
  XCTAssertEqual([service.runningHosts count], (NSUInteger)2, @"hosts running");
  XCTAssertEqual([service.delayedHosts count], (NSUInteger)2, @"hosts delayed");

  // We should see two fetchers running and one delayed for each host
  NSArray *localhosts = [service.runningHosts objectForKey:@"localhost"];
  XCTAssertEqual([localhosts count], (NSUInteger)2, @"hosts running");

  localhosts = [service.delayedHosts objectForKey:@"localhost"];
  XCTAssertEqual([localhosts count], (NSUInteger)1, @"hosts delayed");

  [service stopAllFetchers];

  XCTAssertEqual([service.runningHosts count], (NSUInteger)0, @"hosts running");
  XCTAssertEqual([service.delayedHosts count], (NSUInteger)0, @"hosts delayed");
}

@end
