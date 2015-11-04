//
//  ABTableViewCell.h
//
//  Created by Alex Bumbu on 06/12/14.
//  Copyright (c) 2014 Alex Bumbu. All rights reserved.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.


#import <UIKit/UIKit.h>

/**
 The 'ABMenuTableViewCell' is a 'UITableViewCell' subclass that includes properties and methods for setting and presenting a custom 'UIView' object on the right side of the cell. It can be used with predefined cell styles or with custom cells. Also works with InterfaceBuilder instantiated cells.
 
 You are responsible for setting and positioning the 'rightMenuView' content and will be added as subview to the 'contentView' property of the cell when property is set. Presentation will be done using well known iOS horizontal swipe gestures.
 
 @warning Setting the 'accessoryView' property may produce unexpected behaviour due to 'rightMenuView' being a subview of contentView. This behaviour can be avoided by adding the accessory view object as subview to the 'rightMenuView' object.
 @warning Using 'UITableViewDataSource -tableView:canEditRowAtIndexPath:' interferes with the swipe gesture causing unexpected behaviour. If a Delete option is required it can be added as subview to the 'rightMenuView' property of the cell.
 */

@interface ABMenuTableViewCell : UITableViewCell

/**
 Specifies the view to use for the right menu. Default is nil.
 
 @since v1.0.0
 */
@property (nonatomic, assign) UIView *rightMenuView;

@end
