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

#import "PacoService.h"


#import "GTMHTTPFetcher.h"
#import "GTMOAuth2Authentication.h"
#import "PacoAuthenticator.h"
#import "PacoDate.h"
#import "PacoModel.h"
#import "PacoClient.h"
#import "PacoExperimentInput.h"
#import "PacoExperimentSchedule.h"
#import "PacoExperimentDefinition.h"
#import "PacoEvent.h"

@implementation PacoService


+ (NSString *)stringFromData:(NSData *)data {
  const char *bytes = [data bytes];
  char *dst = malloc([data length] + 1);
  memset(dst, 0, [data length] + 1);
  memcpy(dst, bytes, [data length]);
  NSString *converted = [NSString stringWithUTF8String:dst];
  free(dst);
  return converted;
}

- (void)authenticateRequest:(NSMutableURLRequest *)request
                withFetcher:(GTMHTTPFetcher *)fetcher {
  if (self.authenticator.auth) {
    // OAuth2
    [fetcher setAuthorizer:self.authenticator.auth];
    
  } else if (self.authenticator.cookie) {
    // Client Login
    [request setValue:self.authenticator.cookie forHTTPHeaderField:@"Cookie"];
  } else {
    NSLog(@"Error authenticating request.");
  }
}

- (void)executePacoServiceCall:(NSMutableURLRequest *)request
             completionHandler:(void (^)(id, NSError *))completionHandler {
  // Authenticate
  GTMHTTPFetcher *fetcher = [[GTMHTTPFetcher alloc] initWithRequest:request];
  [self authenticateRequest:request withFetcher:fetcher];
  //Set delegateQueue so that fetcher can work in a background thread
  fetcher.delegateQueue = [[NSOperationQueue alloc] init];
  
  // Fetch
  [fetcher beginFetchWithCompletionHandler:^(NSData *data, NSError *error) {
      if (error) {
        NSLog(@"Service Call Failed [%@]", error);
      }
      // Convert to string and return.
      id jsonObj = nil;
      NSError *jsonError = nil;
      if ([data length]) {
        jsonObj = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:&jsonError];
        if (jsonError) {
          //char* bla = malloc([data length] + 1);
          //memset(bla,0,[data length] + 1);
          //memcpy(bla, [data bytes], [data length]);
          NSLog(@"JSON PARSE ERROR = %@\n", jsonError);//, bla);
          NSLog(@"PROBABLY AN AUTH ERROR");
        }
      }
      if (completionHandler) {
        completionHandler(jsonObj, error ? error : jsonError);
      }
    }];
}

- (void)loadAllExperimentsWithCompletionHandler:(void (^)(NSArray *, NSError *))completionHandler {
  // Setup our request.
  NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"%@/experiments", [PacoClient sharedInstance].serverDomain]];
  NSMutableURLRequest *request =
      [NSMutableURLRequest requestWithURL:url
                              cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                          timeoutInterval:120];
  [request setHTTPMethod:@"GET"];

  // Make the network call.
  [self executePacoServiceCall:request
             completionHandler:^(id jsonData, NSError *error) {
      if (completionHandler) {
        completionHandler(jsonData, error);
      }
  }];
}


- (void)submitEventList:(NSArray*)eventList withCompletionBlock:(void (^)(NSArray*, NSError*))completionBlock {
  NSAssert([eventList count] > 0, @"eventList should have more than one item!");
  
  // Setup our request.
  NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"%@/events", [PacoClient sharedInstance].serverDomain]];
  NSMutableURLRequest *request =
  [NSMutableURLRequest requestWithURL:url
                          cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                      timeoutInterval:120];
  [request setHTTPMethod:@"POST"];
  
  // Serialize to JSON for the request body.
  NSMutableArray* body = [NSMutableArray arrayWithCapacity:[eventList count]];
  for (PacoEvent* event in eventList) {
    id jsonObject = [event generateJsonObject];
    NSAssert(jsonObject != nil, @"jsonObject should NOT be nil!");
    [body addObject:jsonObject];
  }
  
  //YMZ:TODO: error handling here
  NSError *jsonError = nil;
  NSData *jsonData =
  [NSJSONSerialization dataWithJSONObject:body
                                  options:NSJSONWritingPrettyPrinted
                                    error:&jsonError];
  
  [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
  [request setValue:[NSString stringWithFormat:@"%d", [jsonData length]]
 forHTTPHeaderField:@"Content-Length"];
  [request setHTTPBody:jsonData];
  
  // Make the network call.
  [self executePacoServiceCall:request
             completionHandler:^(id jsonData, NSError *error) {
               NSLog(@"JOIN RESPONSE = %@", jsonData);

               NSAssert([jsonData isKindOfClass:[NSArray class]], @"jsonData should be an array");
               NSMutableArray* successEventIndexes = [NSMutableArray array];
               for (id output in jsonData) {
                 NSAssert([output isKindOfClass:[NSDictionary class]], @"output should be a NSDictionary!");
                 if ([output objectForKey:@"errorMessage"] == nil) {
                   NSNumber* eventIndex = [output objectForKey:@"eventId"];
                   NSAssert([eventIndex isKindOfClass:[NSNumber class]], @"eventIndex should be a NSNumber!");
                   [successEventIndexes addObject:eventIndex];
                 }
               }
               
               if (completionBlock) {
                 completionBlock(successEventIndexes, error);
               }
             }];
}

- (void)loadEventsForExperiment:(PacoExperimentDefinition *)experiment
    withCompletionHandler:(void (^)(NSArray *, NSError *))completionHandler {
  // Setup our request.
  NSString *urlString =
      [NSString stringWithFormat:@"%@/events?json&q='experimentId=%@:who=%@'",
           [PacoClient sharedInstance].serverDomain,
           experiment.experimentId,
           [PacoClient sharedInstance].userEmail];//self.authenticator.auth.userEmail];
  NSLog(@"******\n\t%@\n******", urlString);
  NSURL *url = [NSURL URLWithString:urlString];
  NSMutableURLRequest *request =
      [NSMutableURLRequest requestWithURL:url
                              cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                          timeoutInterval:120];
  [request setHTTPMethod:@"GET"];

  // Make the network call.
  [self executePacoServiceCall:request
             completionHandler:^(id jsonData, NSError *error) {
      if (completionHandler) {
        NSLog(@"_+_+_+_EVENT RESPONSE _+_+_+_\n%@", jsonData);

        completionHandler(jsonData, error);
      }
  }];
}


@end
