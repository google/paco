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

#import <CoreLocation/CoreLocation.h>

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

- (void)submitEvent:(PacoEvent *)event withCompletionHandler:(void (^)(NSError *))completionHandler {
  // Setup our request.
  NSURL *url = [NSURL URLWithString:[NSString stringWithFormat:@"%@/events", [PacoClient sharedInstance].serverDomain]];
  NSMutableURLRequest *request =
      [NSMutableURLRequest requestWithURL:url
                              cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                          timeoutInterval:120];
  [request setHTTPMethod:@"POST"];

  // Serialize to JSON for the request body.
  NSError *jsonError = nil;
  id jsonObject = [event generateJsonObject];
  
  //YMZ:TODO: error handling here
  NSData *jsonData =
      [NSJSONSerialization dataWithJSONObject:jsonObject
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
      if (completionHandler) {
        completionHandler(error);
      }
  }]; 
}

- (void)submitAnswers:(PacoExperimentDefinition *)experiment
    completionHandler:(void (^)(NSError *))completionHandler {
  // Setup an event for joining the experiement.
  PacoEvent *event = [PacoEvent pacoEventForIOS];
  event.who = [PacoClient sharedInstance].userEmail;
  event.experimentId = experiment.experimentId;
  event.experimentName = experiment.title;
  event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];

  NSMutableArray *responses = [NSMutableArray array];

  for (PacoExperimentInput *input in experiment.inputs) {
    NSMutableDictionary *response = [NSMutableDictionary dictionary];
    id responseObject = input.responseObject;
    if (responseObject == nil) {
      continue;
    }
    NSLog(@"INPUT RESPONSE NAME = %@", input.name);
    [response setObject:input.name forKey:@"name"];
    [response setObject:input.inputIdentifier forKey:@"inputId"];
    if ([input.questionType isEqualToString:@"question"]) {
      if ([input.responseType isEqualToString:@"likert_smileys"]) {
        NSNumber *number = input.responseObject;
        [response setObject:number forKey:@"answer"];
      } else if ([input.responseType isEqualToString:@"likert"]) {
        NSNumber *number = input.responseObject;
        [response setObject:number forKey:@"answer"];
      } else if ([input.responseType isEqualToString:@"open text"]) {
        NSString *string = input.responseObject;
        [response setObject:string forKey:@"answer"];
      } else if ([input.responseType isEqualToString:@"list"]) {
        NSNumber *number = input.responseObject;
        [response setObject:number forKey:@"answer"];
      } else if ([input.responseType isEqualToString:@"number"]) {
        NSNumber *number = input.responseObject;
        [response setObject:number forKey:@"answer"];
      } else if ([input.responseType isEqualToString:@"location"]) {
        CLLocation *location = input.responseObject;
        NSString *locationString = [NSString stringWithFormat:@"(%f,%f)", location.coordinate.latitude, location.coordinate.longitude];
        [response setObject:locationString forKey:@"answer"];
      } else if ([input.responseType isEqualToString:@"photo"]) {
        [response setObject:@"TODO:ImageUploading" forKey:@"answer"];
      }
    }
    [responses addObject:response];
  }

  event.responses = responses;

  [self submitEvent:event withCompletionHandler:^(NSError *error) {
      // If submission was successful then clear the old response objects.
      if (!error) {
        for (PacoExperimentInput *input in experiment.inputs) {
          input.responseObject = nil;
        }
      }
      if (completionHandler) {
        completionHandler(error);
      }
  }];
}

- (void)joinExperiment:(PacoExperimentDefinition *)experiment
              schedule:(PacoExperimentSchedule *)schedule
     completionHandler:(void (^)(PacoEvent *, NSError *))completionHandler {
  PacoEvent* joinEvent = [PacoEvent joinEventForDefinition:experiment withSchedule:schedule];
  [self submitEvent:joinEvent withCompletionHandler:^(NSError *error) {
      if (completionHandler) {
        completionHandler(joinEvent, error);
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
