/* Copyright 2015  Google
 
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



#import <Foundation/Foundation.h>
@class PacoExperiment;
@class PacoExperimentInput;
@class PAExperimentGroup;

/*
 NOTE: The evaluation of conditions assumes:
 - The conditianl relationship is linear, the dependency input should always be in the position
 before other questions it decides
 **/

@interface PacoInputEvaluatorEx : NSObject
@property(nonatomic, strong, readonly) PacoExperiment* experiment;
@property(nonatomic, strong, readonly) PAExperimentGroup* group;
@property(nonatomic, strong, readonly) NSArray* visibleInputs;


+ (PacoInputEvaluatorEx*)evaluatorWithExperiment:(PacoExperiment*)experiment andGroup:(PAExperimentGroup*) group;
- (NSError*)validateVisibleInputs;
- (NSArray*)evaluateAllInputs;
- (id)initWithExperimentAndGroup:(PacoExperiment*)experiment group:(PAExperimentGroup*) group;

@end
