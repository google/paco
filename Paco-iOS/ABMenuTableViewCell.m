//
//  ABTableViewCell.m
//  Test
//
//  Created by Alex Bumbu on 06/12/14.
//  Copyright (c) 2014 Alex Bumbu. All rights reserved.
//

#import "ABMenuTableViewCell.h"


typedef NS_ENUM(NSInteger, ABMenuUpdateAction) {
    ABMenuUpdateShowAction = 1,
    ABMenuUpdateHideAction = -1
};

typedef NS_ENUM(NSInteger, ABMenuState) {
    ABMenuStateHidden = 0,
    ABMenuStateShowing,
    ABMenuStateShown,
    ABMenuStateHiding
};


static CGFloat kSpringAnimationDuration = .6;
static CGFloat kAnimationDuration = .26;

@interface ABMenuTableViewCell ()

@property (nonatomic, assign) UITableView *parentTableView;

@property (nonatomic, assign) ABMenuState menuState;
@property (nonatomic, assign) CGPoint lastGestureLocation;
@property (nonatomic, assign) CGPoint startGestureLocation;
@property (nonatomic, assign) CGFloat prevDistance;
@property (nonatomic, assign) BOOL ongoingSelection;

@end


@implementation ABMenuTableViewCell {
    CGRect _rightMenuViewInitialFrame;
    UIPanGestureRecognizer *_swipeGesture;
}

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self commonInit]; 
    }
    
    return self;
}

- (void)awakeFromNib {
    [super awakeFromNib];
    [self commonInit];
}

- (void)didMoveToSuperview {
    [super didMoveToSuperview];
    
    // find out the table view
    UIView *view = self.superview;
    
    while (view && [view isKindOfClass:[UITableView class]] == NO) {
        view = view.superview;
    }
    
    self.parentTableView = (UITableView *)view;
}

- (void)willTransitionToState:(UITableViewCellStateMask)state {
    if (state == UITableViewCellStateShowingEditControlMask) {
        if (self.menuState == ABMenuStateShown || self.menuState == ABMenuStateShowing) {
            [self updateMenuView:ABMenuUpdateHideAction animated:YES];
        }
    }
    
    [super willTransitionToState:state];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    // hide menu if presented
    if (selected && (self.menuState == ABMenuStateShowing || self.menuState == ABMenuStateShown)) {
        [self updateMenuView:ABMenuUpdateHideAction animated:YES];
        
        return;
    }
    
    [super setSelected:selected animated:animated];
    
    // prevent swipeGesture before highlight animation completes
    if (!selected) {
#warning Workaround: 0.45s value is obtained through trial & error
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(.45 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            self.ongoingSelection = NO;
        });
    }
}

- (void)setHighlighted:(BOOL)highlighted animated:(BOOL)animated {
    // iPad special case - highlighting the cell is not prevented when starting swipeGesture
    // fix by checking if swipeGesture is started
    CGFloat gestureVelocity = [_swipeGesture velocityInView:self].x;
    if (gestureVelocity)
        return;
    
    // prevent highlighting when menu is on screen
    if (self.menuState == ABMenuStateShowing || self.menuState == ABMenuStateShown)
        return;
    
    if (highlighted)
        self.ongoingSelection = YES;
    
    [super setHighlighted:highlighted animated:animated];
}

- (void)prepareForReuse {
    [super prepareForReuse];
    
    self.rightMenuView = nil;
}

- (void)layoutSubviews {
    if (self.menuState == ABMenuStateHidden && _rightMenuView) {
        _rightMenuView.frame = CGRectMake(CGRectGetWidth(self.contentView.frame), .0, .0, CGRectGetHeight(self.contentView.frame));
    }
    
    [super layoutSubviews];
}

- (void)setRightMenuView:(UIView *)rightMenuView {
    if (_rightMenuView != rightMenuView) {
        // clean
        [_rightMenuView removeFromSuperview];
        
        // add new
        _rightMenuView = rightMenuView;
        _rightMenuViewInitialFrame = _rightMenuView.frame;
        _rightMenuView.frame = CGRectMake(CGRectGetWidth(self.contentView.frame), .0, .0, CGRectGetHeight(self.contentView.frame));
        [self.contentView addSubview:_rightMenuView];
    }
}


#pragma mark UIGestureRecognizerDelegate Methods

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer {
    return NO;
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer {
    if (gestureRecognizer == _swipeGesture) {
        // prevent swipeGesture before highlight animation completes
        if (self.ongoingSelection)
            return NO;
        
        // prevent swipeGesture when editing control is shown
        if (self.editing)
            return NO;
        
        // enable only horizontal gesture
        CGPoint velocity = [(UIPanGestureRecognizer*)gestureRecognizer velocityInView:self];
        BOOL shouldBegin = fabs(velocity.x) > fabs(velocity.y);
        
        return shouldBegin;
    }
    
    return YES;
}


#pragma mark Actions

- (void)handleSwipeGesture:(UIPanGestureRecognizer *)gesture {
    CGFloat initialWidth = CGRectGetWidth(_rightMenuViewInitialFrame);
    ABMenuUpdateAction direction = [self actionForGesture:gesture];
    CGPoint gestureLocation = [gesture locationInView:self];
    
    switch (gesture.state) {
        case UIGestureRecognizerStateBegan: {
            // reset values
            self.prevDistance = .0;
            self.startGestureLocation = [gesture locationInView:self];
            [self resetLastGestureLocation];
            
            // get new direction value after reseting gesture location values
            direction = [self actionForGesture:gesture];
            
            // update menu state
            if (direction == ABMenuUpdateShowAction) {
                self.menuState = ABMenuStateShowing;
            }
            else {
                self.menuState = ABMenuStateHiding;
            }
            
            break;
        }
        case UIGestureRecognizerStateChanged: {
            // get distance since gesture beginning
            CGFloat totalDistance = self.startGestureLocation.x - gestureLocation.x;
            
            // handle out of bounds case considering menuState and direction
            if (totalDistance <= .0) {
                if (direction == ABMenuUpdateHideAction && self.menuState == ABMenuStateHidden)
                    totalDistance = .0;
                
                self.startGestureLocation = [gesture locationInView:self];
            }
            
            // update UI with distance since previous gesture state
            [self updateMenuView:direction delta:fabs(totalDistance - self.prevDistance) animated:NO];
            
            self.prevDistance = totalDistance;
            
            break;
        }
        case UIGestureRecognizerStateCancelled: {
            break;
        }
        case UIGestureRecognizerStateEnded: {
            // calculate deltaX considering menuState and direction
            CGFloat deltaX = .0;
            switch (self.menuState) {
                case ABMenuStateShowing: {
                    deltaX = (direction == ABMenuUpdateShowAction? initialWidth : 0) - self.prevDistance;
                    break;
                }
                case ABMenuStateHiding: {
                    deltaX = (direction == ABMenuUpdateShowAction? 0 : initialWidth) - self.prevDistance;
                    break;
                }
                case ABMenuStateShown:
                case ABMenuStateHidden:
                    break;
                    
                default:
                    break;
            }
            
            // update UI
            [self updateMenuView:direction delta:fabs(deltaX) animated:YES];
            
            // update menu state
            if (direction == ABMenuUpdateShowAction) {
                self.menuState = ABMenuStateShown;
            }
            else {
                self.menuState = ABMenuStateHidden;
            }
            
            break;
        }
            
        default:
            break;
    }
    
    self.lastGestureLocation = gestureLocation;
}


#pragma mark Private Methods

- (void)commonInit {
    self.ongoingSelection = NO;
    self.menuState = ABMenuStateHidden;
    self.prevDistance = .0;
    self.startGestureLocation = CGPointZero;
    
    [self resetLastGestureLocation];
    
    _swipeGesture = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipeGesture:)];
    _swipeGesture.delegate = self;
    [self addGestureRecognizer:_swipeGesture];
}

- (void)resetLastGestureLocation {
    if (self.menuState == ABMenuStateShown) {
        self.lastGestureLocation = CGPointZero;
    }
    else if (self.menuState == ABMenuStateHidden){
        self.lastGestureLocation = CGPointMake(CGRectGetWidth(self.frame), .0);
    }
}

- (void)updateMenuView:(ABMenuUpdateAction)action animated:(BOOL)animated {
    CGFloat initialWidth = CGRectGetWidth(_rightMenuViewInitialFrame);
    
    [self updateMenuView:action delta:initialWidth animated:animated];
    
    // update menu state
    if (action == ABMenuUpdateShowAction) {
        self.menuState = ABMenuStateShown;
    }
    else {
        self.menuState = ABMenuStateHidden;
    }
}

- (ABMenuUpdateAction)actionForGesture:(UIPanGestureRecognizer *)gesture {
    static ABMenuUpdateAction direction = 0;
    CGPoint gestureLocation = [gesture locationInView:self];
    
    // find swipe direction - not using velocityInView due to incorrect reporting when changing swipe direction
    if (gestureLocation.x == self.lastGestureLocation.x) {
        // do nothing
    }
    else if (gestureLocation.x < self.lastGestureLocation.x) {
        // towards left - show menu view
        direction = ABMenuUpdateShowAction;
    }
    else {
        // towards right - hide menu view
        direction = ABMenuUpdateHideAction;
    }
    
    return direction;
}

- (void)updateMenuView:(ABMenuUpdateAction)action delta:(CGFloat)deltaX animated:(BOOL)animated {
    CGFloat initialWidth = CGRectGetWidth(_rightMenuViewInitialFrame);
    
    // adjust deltaX so it doesn't get out of bounds
    CGFloat newWidth = CGRectGetWidth(_rightMenuView.frame) + action*deltaX;
    CGFloat deltaOffset = initialWidth - newWidth;
    
    if (newWidth < 0) {
        deltaX += newWidth;
    }
    else if (deltaOffset < 0) {
        deltaX += deltaOffset;
    }
    
    // calculate new menu frame
    CGRect menuNewFrame = CGRectMake(CGRectGetMinX(_rightMenuView.frame) - action*deltaX, .0,
                                     CGRectGetWidth(_rightMenuView.frame) + action*deltaX, CGRectGetHeight(self.contentView.frame));
    
    // get list of subviews to update
    NSMutableArray *subviews = [NSMutableArray array];
    for (UIView *subview in self.contentView.subviews) {
        if (subview == _rightMenuView)
            continue;
        
        [subviews addObject:subview];
    }
    
    // animate updates
    [self layoutIfNeeded];
    
    [UIView animateWithDuration:(animated? kAnimationDuration : .0)
                     animations:^{
                         _rightMenuView.frame = menuNewFrame;
                         
                         [self layoutIfNeeded];
                     }];
    
    [UIView animateWithDuration:(animated? kSpringAnimationDuration : .0)
                          delay:.0
         usingSpringWithDamping:(action > 0) ? kSpringAnimationDuration : 1.0
          initialSpringVelocity:(action > 0) ? 1.0 : .0
                        options:0
                     animations:^{
                         for (UIView *subview in subviews) {
                             subview.frame = CGRectMake(CGRectGetMinX(subview.frame) - action*deltaX, CGRectGetMinY(subview.frame),
                                                        CGRectGetWidth(subview.frame), CGRectGetHeight(subview.frame));                             
                         }
                     }
                     completion:nil];
}

@end
