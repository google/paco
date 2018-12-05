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
//  PXUISlider.h
//  Pixate
//
//  Created by Paul Colton on 10/11/12.
//  Copyright (c) 2012 Pixate, Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 *
 *  UISlider supports the following element name:
 *
 *  - slider
 *
 *  UISlider supports the following properties:
 *
 *  - PXTransformStyler
 *  - PXLayoutStyler
 *  - PXOpacityStyler
 *  - PXShapeStyler
 *  - PXFillStyler
 *  - PXBorderStyler
 *  - PXBoxShadowStyler
 *  - PXAnimationStyler
 *
 *  UISlider adds support for the following children:
 *
 *  - min-track
 *  - max-track
 *  - min-value
 *  - max-value
 *  - thumb
 *
 *  UISlider min-track supports the following pseudo-class states:
 *
 *  - normal (default)
 *  - highlighted
 *  - selected
 *  - disabled
 *
 *  UISlider min-track supports the following properties:
 *
 *  - color: <color>
 *  - PXFillStyler
 *  - PXBorderStyler
 *
 *  UISlider max-track supports the following pseudo-class states:
 *
 *  - normal (default)
 *  - highlighted
 *  - selected
 *  - disabled
 *
 *  UISlider max-track supports the following properties:
 *
 *  - color: <color>
 *  - PXFillStyler
 *  - PXBorderStyler
 *
 *  UISlider min-value supports the following properties:
 *
 *  - PXShapeStyler
 *  - PXFillStyler
 *  - PXBorderStyler
 *  - PXBoxShadowStyler
 *
 *  UISlider max-value supports the following properties:
 *
 *  - PXShapeStyler
 *  - PXFillStyler
 *  - PXBorderStyler
 *  - PXBoxShadowStyler
 *
 *  UISlider thumb supports the following pseudo-class states:
 *
 *  - normal (default)
 *  - highlighted
 *  - selected
 *  - disabled
 *
 *  UISlider thumb supports the following properties:
 *
 *  - color: <color>
 *  - PXShapeStyler
 *  - PXFillStyler
 *  - PXBorderStyler
 *  - PXBoxShadowStyler
 *
 */

@interface PXUISlider : UISlider

@end
