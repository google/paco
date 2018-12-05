/*
 * Copyright 2012-present Pixate, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
//  PXIdSelector.h
//  Pixate
//
//  Created by Kevin Lindsey on 7/9/12.
//  Copyright (c) 2012 Pixate, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PXSelector.h"

/**
 *  A PXIdExpression determines if an element defines an id attribute if it its value matches a specific string.
 */
@interface PXIdSelector : NSObject <PXSelector>

/**
 *  The id value to match
 */
@property (readonly, nonatomic, strong) NSString *idValue;

/**
 *  Initialize a new instance using the specified id name
 *
 *  @param value The id value to match
 */
- (id)initWithIdValue:(NSString *)value;

@end
