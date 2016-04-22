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

@protocol PacoTableViewDelegate;

@class PacoTableCell;
@class PacoDatePickerView;

@interface PacoTableView : UIView

@property (nonatomic, retain) NSArray *data;
@property (nonatomic, assign) id<PacoTableViewDelegate> delegate;
@property (nonatomic, retain) UITableView *tableView;
@property (nonatomic, retain) UIView *header;
@property (nonatomic, retain) UIView *footer;

// Lasts until next call to set .data
- (void)setLoadingSpinnerEnabledWithLoadingText:(NSString *)loadingText;

- (void)registerClass:(Class)cellClass forStringKey:(NSString *)stringKey dataClass:(Class)dataClass;

- (NSArray *)boxInputs:(NSArray *)inputs withKey:(NSString *)key;

- (void)presentDatePicker:(UIDatePicker*)picker forCell:(PacoTableCell*)cell;
- (void)presentPacoDatePicker:(PacoDatePickerView*)pickerView forCell:(PacoTableCell*)cell;

- (void)dismissAnyDatePicker;
@end
