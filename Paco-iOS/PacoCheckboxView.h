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

#import <UIKit/UIKit.h>

#import "PacoTableCell.h"

@class PacoCheckboxView;
@protocol PacoCheckboxViewDelegate <NSObject>

 @optional
- (void)onCheckboxChanged:(PacoCheckboxView *)checkbox;

@end

@interface PacoCheckboxView : PacoTableCell
@property (nonatomic, assign) id<PacoCheckboxViewDelegate> delegate;
@property (nonatomic, assign) BOOL radioStyle;
@property (nonatomic, retain) NSNumber *bitFlags;
@property (nonatomic, retain) NSArray *optionLabels;
@property (nonatomic, assign) BOOL vertical;

- (BOOL)hasCheckedBox;

@end
