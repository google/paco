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
//  PXUIImageView.h
//  Pixate
//
//  Created by Paul Colton on 9/18/12.
//  Copyright (c) 2012 Pixate, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 *
 *  UIImageView supports the following element name:
 *
 *  - image-view
 *
 *  UIImageView supports the following pseudo-class states:
 *
 *  - normal (default)
 *  - highlighted
 *
 *  UIImageView supports the following properties:
 *
 *  - PXTransformStyler
 *  - PXLayoutStyler
 *  - PXOpacityStyler
 *  - PXShapeStyler
 *  - PXFillStyler
 *  - PXBorderStyler
 *  - PXBoxShadowStyler
 *  - PXAnimationStyler
 *  - -ios-tint-color: <paint>  // iOS 7 or later
 *  - -ios-rendering-mode: original | template | automatic  // iOS 7 or later
 *
 */
@interface PXUIImageView : UIImageView

@end
