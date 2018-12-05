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

#import "PacoTableCell.h"

static const long long kPacoStepperMaxValue = 999999999999999;

@class PacoStepperView;
@protocol PacoStepperViewDelegate <NSObject>

@optional
- (void)onStepperValueChanged:(PacoStepperView *)stepper;
- (void)onTextFieldEditBegan:(UITextField *)textField;

@end

@interface PacoStepperView : PacoTableCell
@property (nonatomic, assign) id<PacoStepperViewDelegate> delegate;
@property (nonatomic, retain) NSString *format;
@property (nonatomic, retain) NSNumber *value;
@property (nonatomic, assign) long long minValue;
@property (nonatomic, assign) long long maxValue;

@end
