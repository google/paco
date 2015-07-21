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
#import "GTMHTTPFetcherLogging.h"
#import "GTMOAuth2Authentication.h"
#import "PacoAuthenticator.h"
#import "PacoDateUtility.h"
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
  NSString *converted = @(dst);
  free(dst);
  return converted;
}

- (void)authenticateRequest:(NSMutableURLRequest *)request
                withFetcher:(GTMHTTPFetcher *)fetcher {
  if (self.authenticator.auth) {
    // OAuth2
    [fetcher setAuthorizer:self.authenticator.auth];
    
  } else {
    DDLogError(@"Error authenticating request.");
  }
}

- (void)executePacoServiceCall:(NSMutableURLRequest *)request
             completionHandler:(void (^)(id, NSError *))completionHandler {
  NSString *version = [[NSBundle mainBundle] infoDictionary][(NSString*)kCFBundleVersionKey];
  NSAssert([version length] > 0, @"version number is not valid!");
  [request setValue:@"iOS" forHTTPHeaderField:@"http.useragent"];
  [request setValue:version forHTTPHeaderField:@"paco.version"];
  [request setValue:@"3.0" forHTTPHeaderField:@"pacoProtocol"];

  // Authenticate
    [GTMHTTPFetcher setLoggingEnabled:YES];
  GTMHTTPFetcher *fetcher = [[GTMHTTPFetcher alloc] initWithRequest:request];
  [self authenticateRequest:request withFetcher:fetcher];
  fetcher.delegateQueue = [NSOperationQueue mainQueue];

  // Fetch
  [fetcher beginFetchWithCompletionHandler:^(NSData *data, NSError *error) {
      if (error) {
        DDLogError(@"Service Call Failed [%@]", error);
      }
      // Convert to string and return.
      id jsonObj = nil;
      NSError *jsonError = nil;
      if ([data length]) {
        jsonObj = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:&jsonError];
        if (jsonError) {
          DDLogError(@"JSON PARSE ERROR = %@\n", jsonError);
          DDLogError(@"PROBABLY AN AUTH ERROR");
          
          [[PacoClient sharedInstance] invalidateUserAccount];
        }
      }
      if (completionHandler) {
        completionHandler(jsonObj, error ? error : jsonError);
      }
    }];
}

//http request to load paginated experiment definitions
- (void)sendGetHTTPRequestWithEndPoint:(NSString*)endPointString andBlock:(PacoPaginatedResponseBlock)block {
  NSAssert(endPointString.length > 0, @"endpoint string should be valid!");
  
  NSURL *url = [NSURL URLWithString:
                   [NSString stringWithFormat:@"%@/%@",[PacoClient sharedInstance].serverDomain,endPointString]];
  NSMutableURLRequest *request =
    [NSMutableURLRequest requestWithURL:url
                          cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                          timeoutInterval:120];
  [request setHTTPMethod:@"GET"];
  
  [self executePacoServiceCall:request completionHandler:^(id jsonData, NSError *error) {
    if (!error) {
      NSAssert([jsonData isKindOfClass:[NSDictionary class]], @"paginated response should be a dictionary");
      if (block) {
        NSString* cursor = jsonData[@"cursor"];
        NSArray* results = jsonData[@"results"];
        block(results, cursor, nil);
      }
    } else {
      if (block) {
        block(nil, nil, error);
      }
    }
  }];
}


- (void)loadPublicDefinitionListWithCursor:(NSString*)cursor limit:(NSUInteger)limit block:(PacoPaginatedResponseBlock)block {
  NSString* endPoint = @"/experiments?public";
  if ([cursor length] > 0) {
    endPoint = [endPoint stringByAppendingFormat:@"&cursor=%@", cursor];
  }
  if (limit > 0) {
    endPoint = [endPoint stringByAppendingFormat:@"&limit=%lu", (unsigned long)limit];
  }
    endPoint = [endPoint stringByAppendingFormat:@"&tz=%@", [PacoDateUtility escapedNameForSystemTimeZone]];
  [self sendGetHTTPRequestWithEndPoint:endPoint andBlock:block];
}


- (void)loadMyShortDefinitionListWithBlock:(void (^)(NSArray*, NSError*))completionBlock {
  NSString *endPoint = [@"experiments?mine" stringByAppendingFormat:@"&tz=%@",
                        [PacoDateUtility escapedNameForSystemTimeZone]];
  [self sendGetHTTPRequestWithEndPoint:endPoint andBlock:^(NSArray *items, NSString *cursor, NSError *error) {
    if (completionBlock) {
      completionBlock(items, error);
    }
  }];
}

- (void)loadFullDefinitionListWithIDs:(NSArray*)idList andBlock:(void (^)(NSArray*, NSError*))completionBlock {
  NSAssert([idList count] > 0, @"idList should have more than one id inside!");
  NSString* endPointString = [NSString stringWithFormat:@"experiments?id=%@&tz=%@",[idList componentsJoinedByString:@","], [PacoDateUtility escapedNameForSystemTimeZone]];
  [self sendGetHTTPRequestWithEndPoint:endPointString andBlock:^(NSArray* items, NSString* cursor, NSError* error) {
    if (completionBlock) {
      NSMutableArray* definitionList = [NSMutableArray arrayWithCapacity:[items count]];
      for (id definitionJson in items) {
        NSAssert([definitionJson isKindOfClass:[NSDictionary class]], @"a full definition should be a dictionary ");
        PacoExperimentDefinition* definition = [PacoExperimentDefinition pacoExperimentDefinitionFromJSON:definitionJson];
        NSAssert(definition, @"definition should be valid");
        [definitionList addObject:definition];
      }
      completionBlock([NSArray arrayWithArray:definitionList], error);
    }
  }];
}

- (void)loadFullDefinitionWithID:(NSString*)definitionID andBlock:(void (^)(PacoExperimentDefinition*, NSError*))completionBlock {
  [self loadFullDefinitionListWithIDs:@[definitionID] andBlock:^(NSArray* definitionList, NSError* error) {
    PacoExperimentDefinition* definition = nil;
    if (!error) {
      definition = [definitionList firstObject];
      if (!definition) {
        DDLogWarn(@"Warning: No full definition is found for id=%@, the experiment could expire already", definitionID);
        NSDictionary* userInfo = @{NSLocalizedDescriptionKey : @"No full definition is found on server! "
                                                               @"The experiment could expire already."};
        error = [NSError errorWithDomain:@"paco_error"
                                    code:0
                                userInfo:userInfo];
      }
    }
    if (completionBlock) {
      completionBlock(definition, error);
    }
  }];
}

- (void)loadMyDefinitionIDListWithBlock:(void (^)(NSArray*, NSError*))completionBlock {
  [self loadMyShortDefinitionListWithBlock:^(NSArray* definitionList, NSError* error) {
    if (error == nil) {
      NSMutableArray* result = [NSMutableArray arrayWithCapacity:[definitionList count]];
      for (NSDictionary* dict in definitionList) {
        NSNumber* idNum = dict[@"id"];
        NSAssert(idNum != nil && [idNum isKindOfClass:[NSNumber class]], @"idNum should be valid!");
        NSString* definitionId = [NSString stringWithFormat:@"%lld", [idNum longLongValue]];
        [result addObject:definitionId];
      }
      if (completionBlock) {
        completionBlock(result, error);
      }
    } else {
      if (completionBlock) {
        completionBlock(nil, error);
      }
    }
  }];
}

//YMZ:TODO: there should be a single endpoint for this API
- (void)loadMyFullDefinitionListWithBlock:(void (^)(NSArray*, NSError*))completionBlock {
  NSAssert(completionBlock != nil, @"a completion block has to be passed in");
  
  [self loadMyDefinitionIDListWithBlock:^(NSArray* idList, NSError* error) {
    if (!error) {
      if (0 == [idList count]) {
        completionBlock(idList, nil);
      } else {
        [self loadFullDefinitionListWithIDs:idList andBlock:^(NSArray* fullList, NSError* error) {
          if (!error) {
            completionBlock(fullList, nil);
          } else {
            completionBlock(nil, error);
          }
        }];
      }
    } else {
      completionBlock(nil, error);
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
                      timeoutInterval:25];
  [request setHTTPMethod:@"POST"];
  
  // Serialize to JSON for the request body.
  NSMutableArray* body = [NSMutableArray arrayWithCapacity:[eventList count]];
  for (PacoEvent* event in eventList) {
    id jsonObject = [event payloadJsonWithImageString];
    NSAssert(jsonObject != nil, @"jsonObject should NOT be nil!");
    [body addObject:jsonObject];
  }
  
  //YMZ:TODO: error handling here
  NSError *jsonError = nil;
  NSData *jsonData = [NSJSONSerialization dataWithJSONObject:body
                                                     options:NSJSONWritingPrettyPrinted
                                                       error:&jsonError];
  
  [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
  [request setValue:[NSString stringWithFormat:@"%lu", (unsigned long)[jsonData length]]
 forHTTPHeaderField:@"Content-Length"];
  [request setHTTPBody:jsonData];
  
  // Make the network call.
  [self executePacoServiceCall:request
             completionHandler:^(id jsonData, NSError *error) {
               DDLogInfo(@"Event Upload RESPONSE = %@", jsonData);
               NSMutableArray* successEventIndexes = [NSMutableArray array];
               if (error == nil) {
                 NSAssert([jsonData isKindOfClass:[NSArray class]], @"jsonData should be an array");
                 for (id output in jsonData) {
                   NSAssert([output isKindOfClass:[NSDictionary class]], @"output should be a NSDictionary!");
                   if (output[@"errorMessage"] == nil) {
                     NSNumber* eventIndex = output[@"eventId"];
                     NSAssert([eventIndex isKindOfClass:[NSNumber class]], @"eventIndex should be a NSNumber!");
                     [successEventIndexes addObject:eventIndex];
                   }
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
           [[PacoClient sharedInstance] userEmail]];//self.authenticator.auth.userEmail];
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
