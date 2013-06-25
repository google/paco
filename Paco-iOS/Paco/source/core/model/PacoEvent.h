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

#import <Foundation/Foundation.h>

@interface PacoEvent : NSObject

@property (copy) NSString *who;
@property (retain) NSDate *when;
@property (assign) long long latitude;
@property (assign) long long longitude;
@property (retain) NSDate *responseTime;
@property (retain) NSDate *scheduledTime;
@property (readonly, copy) NSString *appId;
@property (readonly, copy) NSString *pacoVersion;
@property (copy) NSString *experimentId;
@property (copy) NSString *experimentName;
@property (retain) NSArray *responses;  // <NSDictionary>
@property (retain) id jsonObject;
+ (id)pacoEventForIOS;
+ (id)pacoEventFromJSON:(id)jsonObject;
- (id)jsonObject;
- (id)generateJsonObject;

@end
