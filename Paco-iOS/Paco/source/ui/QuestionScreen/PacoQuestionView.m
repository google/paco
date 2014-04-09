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

#import "PacoQuestionView.h"

#import <MapKit/MapKit.h>
#import <MobileCoreServices/MobileCoreServices.h>
#import <QuartzCore/QuartzCore.h>

#import "PacoCheckboxView.h"
#import "UIColor+Paco.h"
#import "UIFont+Paco.h"
#import "PacoLayout.h"
#import "PacoModel.h"
#import "PacoStepperView.h"
#import "PacoExperimentInput.h"
#import "UIImage+Paco.h"
#import "PacoClient.h"

static const int kInvalidIndex = -1;
static NSString* const kPlaceHolderString = @"<type response here>";

@interface PacoQuestionView () <MKMapViewDelegate,
PacoCheckboxViewDelegate,
PacoStepperViewDelegate,
UITextViewDelegate,
UINavigationControllerDelegate,
UIImagePickerControllerDelegate>

@property (nonatomic, retain, readwrite) PacoCheckboxView *checkboxes;
@property (nonatomic, retain, readwrite) UISegmentedControl* photoSegmentControl;
@property (nonatomic, retain, readwrite) UIButton *choosePhotoButton;
@property (nonatomic, retain, readwrite) UIImage *image;
@property (nonatomic, retain, readwrite) UIImagePickerController *imagePicker;
@property (nonatomic, retain, readwrite) MKMapView *map;
@property (nonatomic, retain, readwrite) NSArray *numberButtons;
@property (nonatomic, retain, readwrite) PacoStepperView *numberStepper;
@property (nonatomic, retain, readwrite) UILabel *questionText;
@property (nonatomic, retain, readwrite) NSArray *smileysButtons;
@property (nonatomic, retain, readwrite) UITextView *textView;
@property (nonatomic, retain, readwrite) NSArray* rightLeftLabels;
@property (nonatomic, retain) UILabel* messageLabel;
// TODO(gregvance): add location and photo

- (void)clearUI;
- (void)setupUIForQuestion;

// Likert Smileys
- (void)selectSmiley:(NSUInteger)index;

// Likert
- (void)selectNumberButton:(NSUInteger)index;

@end

@implementation PacoQuestionView

@synthesize question = _question;

- (void)dealloc {
  [self clearUI];
}


- (id)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    self.backgroundColor = [UIColor pacoBackgroundWhite];
  }
  return self;
}

- (PacoExperimentInput *)question {
  return _question;
}

- (void)updateConditionals {
  if (self.question.isADependencyForOthers) {
    [self.tableDelegate reloadTable];
  }
}

- (void)setQuestion:(PacoExperimentInput *)question {
  _question = question;
  [self clearUI];
  [self setupUIForQuestion];
  [self setNeedsLayout];
}

- (void)clearUI {
  [self.photoSegmentControl removeFromSuperview];
  [self.choosePhotoButton removeFromSuperview];
  [self.checkboxes removeFromSuperview];
  for (UIButton *button in self.numberButtons) {
    [button removeFromSuperview];
  }
  for (UILabel* label in self.rightLeftLabels) {
    [label removeFromSuperview];
  }
  [self.imagePicker dismissViewControllerAnimated:NO completion:nil];
  [self.map removeFromSuperview];
  [self.numberStepper removeFromSuperview];
  [self.questionText removeFromSuperview];
  if (self.messageLabel) {
    [self.messageLabel removeFromSuperview];
    self.messageLabel = nil;
  }
  for (UIButton *button in self.smileysButtons) {
    [button removeFromSuperview];
  }
  [self.textView removeFromSuperview];

  self.photoSegmentControl = nil;
  self.choosePhotoButton = nil;
  self.checkboxes = nil;
  self.image = nil;
  self.imagePicker = nil;
  
  //self.map = nil;  // Dont clear the map, it takes too much time to refresh
  //set delegate to nil, otherwise the map updating may set the responseObject to
  //a non-map question, and thus cause crash when trying to update the question view's UI
  //according to responseObject's value(a CLLocation object)
  self.map.delegate = nil;
  
  self.numberButtons = nil;
  self.numberStepper = nil;
  self.questionText = nil;
  self.smileysButtons = nil;
  self.textView = nil;
  self.rightLeftLabels = nil;
}

- (void)selectSmiley:(NSUInteger)index {
  for (int i = 0; i < 5; ++i) {
    UIImage *smileyOn = [UIImage imageNamed:[NSString stringWithFormat:@"smile_icon%d_re.png", i+1]];
    UIImage *smileyOff = [UIImage imageNamed:[NSString stringWithFormat:@"smile_icon%d.png", i+1]];
    assert(smileyOn);
    assert(smileyOff);

    UIButton *button = (self.smileysButtons)[i];
    if (i == index) {
      [button setImage:smileyOn forState:UIControlStateNormal];
      [button setImage:smileyOn forState:UIControlStateHighlighted];
    } else {
      [button setImage:smileyOff forState:UIControlStateNormal];
      [button setImage:smileyOff forState:UIControlStateHighlighted];
    }
  }
  //[self updateConditionals];
}

- (void)selectNumberButton:(NSUInteger)index {
  UIColor *highlightedColor = [UIColor blackColor];
  UIColor *normalColor = [UIColor blackColor];
  for (int i = 0; i < self.question.likertSteps; ++i) {
    UIButton *button = (self.numberButtons)[i];
    if (i == index) {
      UIFont *font = [UIFont pacoTableCellFont];
      UIFont *boldFont = [UIFont boldSystemFontOfSize:(font.pointSize + 1)];
      button.titleLabel.font = boldFont;
      [button setTitleColor:highlightedColor forState:UIControlStateNormal];
      [button setTitleColor:highlightedColor forState:UIControlStateHighlighted];
      [button setBackgroundImage:[UIImage imageNamed:@"uicheckbox_checked"] forState:UIControlStateNormal];
    } else {
      button.titleLabel.font = [UIFont pacoTableCellFont];
      [button setTitleColor:normalColor forState:UIControlStateNormal];
      [button setTitleColor:normalColor forState:UIControlStateHighlighted];
      [button setBackgroundImage:[UIImage imageNamed:@"uicheckbox_unchecked"] forState:UIControlStateNormal];
    }
  }
  //[self updateConditionals];
}

- (void)onSmiley:(UIButton *)button {
  NSUInteger buttonIndex = [self.smileysButtons indexOfObject:button];
  assert(buttonIndex != NSNotFound);
  [self selectSmiley:buttonIndex];
  self.question.responseObject = @(buttonIndex);
  [self updateConditionals];
}

- (void)onNumber:(UIButton *)button {
  NSUInteger buttonIndex = [self.numberButtons indexOfObject:button];
  assert(buttonIndex != NSNotFound);
  [self selectNumberButton:buttonIndex];
  self.question.responseObject = @(buttonIndex);
  [self updateConditionals];
}

- (void)updateChoosePhotoButtonTitle
{
  NSString* title = NSLocalizedString(@"Tap to Take Photo", nil);
  if (self.photoSegmentControl.selectedSegmentIndex == 1) {
    title = NSLocalizedString(@"Tap to Choose Photo", nil);
  }
  [self.choosePhotoButton setTitle:title forState:UIControlStateNormal];
  [self.choosePhotoButton setTitleColor:[UIColor pacoSystemButtonBlue] forState:UIControlStateNormal];
  [self.choosePhotoButton setTitleColor:[UIColor pacoSystemButtonHighlightenBlue]
                               forState:UIControlStateHighlighted];
  if (!self.image) {
    [self.choosePhotoButton setBackgroundColor:[UIColor pacoLightGray]];
  }
}

- (void)updateChoosePhotoButtonImage {
  if (self.image) {
    UIImage* buttonImage = [UIImage scaleImage:self.image toSize:self.choosePhotoButton.frame.size];
    [self.choosePhotoButton setImage:buttonImage forState:UIControlStateNormal];

    CGFloat buttonWidth = self.choosePhotoButton.frame.size.width;
    CGFloat imageMargin = (buttonWidth - buttonImage.size.width) / 2.;
    self.choosePhotoButton.imageEdgeInsets = UIEdgeInsetsMake(0, imageMargin, 0.0, 0.0);
    self.choosePhotoButton.titleEdgeInsets = UIEdgeInsetsMake(0,
                                                              -buttonImage.size.width,
                                                              0.0,
                                                              0.0);
    [self.choosePhotoButton setBackgroundColor:[UIColor clearColor]];
  }
}


- (void)takePhoto
{
  UIImagePickerController* imagePicker = [[UIImagePickerController alloc] init];
  imagePicker.delegate = self;

  switch (self.photoSegmentControl.selectedSegmentIndex) {
    case 0:
      imagePicker.sourceType = UIImagePickerControllerSourceTypeCamera;
      break;
    case 1:
      imagePicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
      break;

    default:
      NSAssert(NO, @"photoSegmentControl receive a wrong selected status!");
      break;
  }
  self.imagePicker = imagePicker;

  [[UIApplication sharedApplication].keyWindow.rootViewController
   presentViewController:self.imagePicker
   animated:YES
   completion:nil];

  [self updateConditionals];
}

- (void)setupUIForQuestion {
  if (self.question == nil) {
    return;
  }

  if (![self.question.questionType isEqualToString:@"question"]) {
    NSLog(@"TODO: implement question type \"%@\" [%@]", self.question.questionType, self.question.text);
    return;
  }

  // Question Label
  self.questionText = [[UILabel alloc] initWithFrame:CGRectZero];
  self.questionText.text = self.question.text;
  self.questionText.backgroundColor = [UIColor clearColor];
  self.questionText.textColor = [UIColor pacoDarkBlue];
  self.questionText.font = [UIFont pacoTableCellFont];
  self.questionText.numberOfLines = 0;  // Number of lines limited to view size
  [self addSubview:self.questionText];
  [self.questionText sizeToFit];

  if (self.question.responseEnumType == ResponseEnumTypeLikertSmileys) {
    // Smiley Buttons
    NSMutableArray *buttons = [NSMutableArray array];
    for (int i = 1; i <= 5; ++i) {
      UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
      [buttons addObject:button];
      [self addSubview:button];
      [button sizeToFit];
      [button addTarget:self action:@selector(onSmiley:) forControlEvents:UIControlEventTouchUpInside];
    }

    self.smileysButtons = buttons;
    if (self.question.responseObject) {
      NSNumber *number = self.question.responseObject;
      [self selectSmiley:[number intValue]];
    } else {
      [self selectSmiley:kInvalidIndex];
    }
  } else if (self.question.responseEnumType == ResponseEnumTypeLikert) {
    //set right left labels
    if (self.question.leftSideLabel != nil && self.question.rightSideLabel != nil) {
      NSMutableArray* labels = [NSMutableArray array];
      UILabel* leftLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, self.frame.size.height)];
      leftLabel.text = self.question.leftSideLabel;
      leftLabel.textColor = [UIColor pacoDarkBlue];
      leftLabel.backgroundColor = [UIColor clearColor];
      leftLabel.font = [UIFont pacoMenuButtonFont];
      [self addSubview:leftLabel];
      [leftLabel sizeToFit];
      [labels addObject:leftLabel];

      UILabel* rightLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.frame.size.width, self.frame.size.height)];
      rightLabel.text = self.question.rightSideLabel;
      rightLabel.textColor = [UIColor pacoDarkBlue];
      rightLabel.backgroundColor = [UIColor clearColor];
      rightLabel.font = [UIFont pacoMenuButtonFont];
      [self addSubview:rightLabel];
      [rightLabel sizeToFit];
      [labels addObject:rightLabel];
      self.rightLeftLabels = labels;
    }
    // Number Steps
    NSMutableArray *buttons = [NSMutableArray array];
    for (NSInteger i = 0; i < self.question.likertSteps; ++i) {
      UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
      [button setBackgroundImage:[UIImage imageNamed:@"radiobtn_off"] forState:UIControlStateNormal];
      [buttons addObject:button];
      [self addSubview:button];
      [button sizeToFit];
      [button addTarget:self action:@selector(onNumber:) forControlEvents:UIControlEventTouchUpInside];
    }
    self.numberButtons = buttons;
    if (self.question.responseObject) {
      NSNumber *number = self.question.responseObject;
      [self selectNumberButton:[number intValue]];
    } else {
      [self selectNumberButton:kInvalidIndex];
    }
  } else if (self.question.responseEnumType == ResponseEnumTypeOpenText) {
    // Open Text Field
    self.textView = [[UITextView alloc] initWithFrame:CGRectZero];
    [self.textView.layer setBorderColor:[[[UIColor lightGrayColor] colorWithAlphaComponent:0.5] CGColor]];
    [self.textView.layer setBorderWidth:1];
    [self.textView.layer setCornerRadius:5];
    self.textView.text = NSLocalizedString(kPlaceHolderString, nil);
    self.textView.textColor = [UIColor lightGrayColor];
    self.textView.editable = YES;
    self.textView.returnKeyType = UIReturnKeyDone;
    [self addSubview:self.textView];
    self.textView.delegate = self;
    if (self.question.responseObject) {
      if (![self.question.responseObject isKindOfClass:[NSString class]]) {
        //NSString *reponseType = NSStringFromClass([self.question.responseObject class]);
        assert(0); // should clear map thing for sure between table instantiations, or sometinng, make sure either way
      }
      self.textView.text = self.question.responseObject;
      self.textView.textColor = [UIColor blackColor];
      self.textView.font = [UIFont systemFontOfSize:15];
    }
  } else if (self.question.responseEnumType == ResponseEnumTypeList) {
    // TODO: radio list UI implementation
    // TODO: modify checkboxes to be vertical
    NSString* listIdentifier = @"question_list";
    if (!self.question.multiSelect) {
      listIdentifier = @"question_list_radio";
    }
    PacoCheckboxView *checkboxes = [[PacoCheckboxView alloc] initWithStyle:UITableViewStylePlain
                                                           reuseIdentifier:listIdentifier];
    checkboxes.optionLabels = self.question.listChoices;
    checkboxes.bitFlags = @0ULL;
    checkboxes.radioStyle = !self.question.multiSelect;
    checkboxes.vertical = YES;
    checkboxes.delegate = self;
    self.checkboxes = checkboxes;
    [self addSubview:checkboxes];
    if (self.question.responseObject) {
      checkboxes.bitFlags = self.question.responseObject;
    }
  } else if (self.question.responseEnumType == ResponseEnumTypeNumber) {
    PacoStepperView* stepper = [[PacoStepperView alloc] initWithStyle:UITableViewStylePlain
                                                      reuseIdentifier:@"question_number"];
    stepper.minValue = 0;
    //need to set the maxValue before setting stepper's value, otherwise it may cause a bug that
    //any value larger than 100 will not be set correctly, since the default max value is 100
    stepper.maxValue = kPacoStepperMaxValue;
    stepper.format = @"%lli";
    if (self.question.responseObject) {
      stepper.value = self.question.responseObject;
    } else {
      stepper.value = @0LL;
    }
    stepper.delegate = self;
    self.numberStepper = stepper;
    [self addSubview:stepper];

  } else if (self.question.responseEnumType == ResponseEnumTypeLocation) {
    if ([self.question.text length] == 0) {
      self.questionText.text = NSLocalizedString(@"Attaching your location ...", nil);
      [self.questionText sizeToFit];
    }
    self.messageLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 300, 100)];
    self.messageLabel.numberOfLines = 0;
    self.messageLabel.backgroundColor = [UIColor clearColor];
    self.messageLabel.textColor = [UIColor darkGrayColor];
    [self.messageLabel setFont:[UIFont fontWithName:@"HelveticaNeue" size:11]];
    [self.messageLabel setText:NSLocalizedString(@"This is the location that will be recorded for your response.", nil)];
    self.messageLabel.textAlignment = NSTextAlignmentCenter;
    [self.messageLabel sizeToFit];
    [self addSubview:self.messageLabel];
    if (!self.map) {
      self.map = [[MKMapView alloc] initWithFrame:CGRectZero];
      self.map.showsUserLocation = YES;
      self.map.zoomEnabled = NO;
      self.map.userInteractionEnabled = NO;
      self.map.userTrackingMode = MKUserTrackingModeFollow;
      self.map.mapType = MKMapTypeHybrid;// MKMapTypeStandard,MKMapTypeSatellite,MKMapTypeHybrid
    }
    self.map.delegate = self;
    [self addSubview:self.map];
  } else if (self.question.responseEnumType == ResponseEnumTypePhoto) {
    if ([self.question.text length] == 0) {
      self.questionText.text = NSLocalizedString(@"Attach a photo.", nil);
      [self.questionText sizeToFit];
    }
    self.photoSegmentControl = [[UISegmentedControl alloc] initWithItems:@[NSLocalizedString(@"Camera", nil), NSLocalizedString(@"Library", nil)]];
    self.photoSegmentControl.selectedSegmentIndex = 0;
    [self.photoSegmentControl addTarget:self action:@selector(updateChoosePhotoButtonTitle) forControlEvents:UIControlEventValueChanged];
    [self addSubview:self.photoSegmentControl];

    self.choosePhotoButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [self updateChoosePhotoButtonTitle];
    [self addSubview:self.choosePhotoButton];
    if (self.question.responseObject) {
      NSAssert([self.question.responseObject isKindOfClass:[UIImage class]],
               @"a non-nil responseObject should be UIImage object");
      UIImage *image = self.question.responseObject;
      self.image = image;
    }
    [self.choosePhotoButton addTarget:self action:@selector(takePhoto) forControlEvents:UIControlEventTouchUpInside];
  } else {

    NSLog(@"TODO: implement response type \"%@\"", self.question.responseType);
  }
}

- (CGSize)sizeThatFits:(CGSize)size {
  NSNumber *height = [self.class heightForData:@[@"", self.question]];
  return CGSizeMake(320, height.integerValue);
}

- (void)sizeToFit {
  [super sizeToFit];
}
//calculate text size in a func that uses .frame then a sizeTOFit
+ (CGSize)textSizeToFitSize:(CGSize)bounds
                       text:(NSString *)text
                       font:(UIFont *)font {
  UIView *parent = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, FLT_MAX)];
  UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 320, FLT_MAX)];
  label.numberOfLines = 0;
  [parent addSubview:label];
  label.text = text;
  if (font) {
    label.font = font;
  }
  [label sizeToFit];
  return label.frame.size;
}

+ (NSNumber *)heightForData:(id)data {
  NSArray *array = (NSArray *)data;
  PacoExperimentInput *question = (PacoExperimentInput *)array[1];
  CGSize textSize = [self textSizeToFitSize:CGSizeMake(320, 10000) text:question.text font:nil];

  if (question == nil) {
    return [NSNumber numberWithInt:140 + (textSize.height)];
  }

  if (![question.questionType isEqualToString:@"question"]) {
    NSLog(@"TODO: implement question type \"%@\" [%@]", question.questionType, question.text);
    return [NSNumber numberWithInt:140 + (textSize.height)];
  }

  if (question.responseEnumType == ResponseEnumTypeLikertSmileys) {
    return [NSNumber numberWithInt:100 + (textSize.height)];
  } else if (question.responseEnumType == ResponseEnumTypeLikert) {
    return [NSNumber numberWithInt:100 + (textSize.height)];
  } else if (question.responseEnumType == ResponseEnumTypeOpenText) {
  } else if (question.responseEnumType == ResponseEnumTypeList) {
    // radio list or multi checkboxes
    return [NSNumber numberWithInt:(question.listChoices.count * 60) + (textSize.height)];
  } else if (question.responseEnumType == ResponseEnumTypeNumber) {
    return [NSNumber numberWithInt:100 + (textSize.height)];
  } else if (question.responseEnumType == ResponseEnumTypeLocation) {
    return [NSNumber numberWithInt:300 + (textSize.height)];
  } else if (question.responseEnumType == ResponseEnumTypePhoto) {
    return [NSNumber numberWithInt:300 + (textSize.height)];
  }

  return [NSNumber numberWithInt:140 + (textSize.height)];
}

- (void)layoutSubviews {
  [super layoutSubviews];

  self.questionText.frame = self.bounds;
  [self.questionText sizeToFit];
  CGSize textsize = [self.class textSizeToFitSize:self.questionText.frame.size
                                             text:self.questionText.text
                                             font:self.questionText.font];

  if (self.question == nil) {
    return;
  }

  if (![self.question.questionType isEqualToString:@"question"]) {
    NSLog(@"TODO: implement question type \"%@\" [%@]", self.question.questionType, self.question.text);
    return;
  }

  if (self.question.responseEnumType == ResponseEnumTypeLikertSmileys) {
    NSUInteger numSmileys = self.smileysButtons.count;
    CGRect bounds = CGRectMake(0, textsize.height + 10, self.frame.size.width, self.frame.size.height - textsize.height - 20);
    NSArray *smileys = [PacoLayout splitRectHorizontally:bounds numSections:numSmileys];
    //for (NSValue *valueRect in smileys) {
    for (int i = 0; i < numSmileys; ++i) {
      UIButton *button = (self.smileysButtons)[i];
      NSValue *valueRect = smileys[i];
      CGRect rect = [valueRect CGRectValue];
      button.frame = rect;
    }
  } else if (self.question.responseEnumType == ResponseEnumTypeLikert) {
    if (self.question.likertSteps <= 4) {
      NSUInteger numOfButtons = [self.numberButtons count];
      CGFloat spaceBetweenButtons = 20;
      CGFloat totalWidth = self.frame.size.width;
      CGFloat btnSize = 25.;
      CGFloat y = self.frame.size.height/2. - btnSize/2.;
      CGFloat btnOffsetToEdge = (totalWidth - spaceBetweenButtons * (numOfButtons - 1) - btnSize * numOfButtons) / 2.0;
      for (int index = 0; index < numOfButtons; ++index) {
        UIButton* button = (self.numberButtons)[index];
        CGFloat btnX = btnOffsetToEdge + (btnSize + spaceBetweenButtons) * index;
        CGRect btnFrame = CGRectMake(btnX, y, btnSize, btnSize);
        button.frame = btnFrame;
      }
      UIButton* leftMostButton = [self.numberButtons firstObject];
      UIButton* rightMostButton = [self.numberButtons lastObject];
      CGFloat spaceBetweenButtonAndLabel = 10.;
      UILabel* lLabel = [self.rightLeftLabels firstObject];
      lLabel.frame = CGRectMake(leftMostButton.frame.origin.x - spaceBetweenButtonAndLabel - lLabel.frame.size.width,
                                y,
                                lLabel.frame.size.width,
                                btnSize);
      UILabel* rLabel = [self.rightLeftLabels lastObject];
      rLabel.frame = CGRectMake(rightMostButton.frame.origin.x + btnSize + spaceBetweenButtonAndLabel,
                                y,
                                rLabel.frame.size.width,
                                btnSize);
    } else if (self.question.likertSteps > 4){
      UILabel* lLabel = (self.rightLeftLabels)[0];
      lLabel.frame = CGRectMake(10, self.questionText.frame.size.height + 10, lLabel.frame.size.width, lLabel.frame.size.height);
      UILabel *rLabel = (self.rightLeftLabels)[1];
      rLabel.frame = CGRectMake(self.frame.size.width - rLabel.frame.size.width - 10, self.questionText.frame.size.height + 10, rLabel.frame.size.width, rLabel.frame.size.height);
      int height = (self.frame.size.height - lLabel.frame.origin.y - 10 - lLabel.frame.size.height);
      height = lLabel.frame.origin.y + 10 + lLabel.frame.size.height+ height / 2 - 25;
      NSUInteger numValues = self.numberButtons.count;
      CGRect bounds = CGRectMake(25, height + 10, self.frame.size.width - 20, height);
      NSArray* numbers = [PacoLayout splitRectHorizontally:bounds numSections:numValues];
      for (int i = 0; i < numValues; ++i) {
        UIButton* button = (self.numberButtons)[i];
        NSValue* valueRect = numbers[i];
        CGRect rect = [valueRect CGRectValue];
        rect.size = CGSizeMake(25, 25);
        button.frame = rect;
      }
    }
  } else if (self.question.responseEnumType == ResponseEnumTypeOpenText) {
    CGRect bounds = CGRectMake(10, textsize.height + 10, self.frame.size.width - 20, self.frame.size.height - textsize.height - 20);
    self.textView.frame = bounds;
  } else if (self.question.responseEnumType == ResponseEnumTypeList) {
    // radio list or multi checkboxes
    CGRect bounds = CGRectMake(10, textsize.height + 10, self.frame.size.width - 20, self.frame.size.height - textsize.height - 20);
    self.checkboxes.frame = bounds;
    //      int numChoices = self.question.listChoices.count;
    //      NSArray *choices = [PacoLayout splitRectVertically:bounds numSections:numChoices];
    //      for (int i = 0; i < numChoices; ++i) {
    //
    //      }
  } else if (self.question.responseEnumType == ResponseEnumTypeNumber) {
    CGRect bounds = CGRectMake(10, textsize.height + 10, self.frame.size.width - 20, self.frame.size.height - textsize.height - 20);

    self.numberStepper.frame = bounds;
  } else if (self.question.responseEnumType == ResponseEnumTypeLocation) {
    CGFloat segmentY = textsize.height + 10;
    self.messageLabel.frame = CGRectMake(self.center.x - self.messageLabel.frame.size.width / 2,
                                         segmentY,
                                         self.messageLabel.frame.size.width,
                                         self.messageLabel.frame.size.height);
    segmentY += self.messageLabel.frame.size.height + 10;
    CGRect bounds = CGRectMake(10, segmentY, self.frame.size.width - 20, self.frame.size.height - segmentY - 20);
    self.map.frame = bounds;
  } else if (self.question.responseEnumType == ResponseEnumTypePhoto) {
    CGFloat segmentY = self.questionText.frame.origin.y + self.questionText.frame.size.height + 10;
    CGFloat marginHorizontal = 10;
    CGRect segmentControlFrame = CGRectMake(marginHorizontal,
                                            segmentY,
                                            self.photoSegmentControl.frame.size.width,
                                            self.photoSegmentControl.frame.size.height);
    self.photoSegmentControl.frame = segmentControlFrame;
    CGFloat space = 5.;
    CGFloat buttonY = segmentY + self.photoSegmentControl.frame.size.height + space;
    CGFloat photoButtonHeight = self.frame.size.height - buttonY - space;
    CGRect photoButtonFrame = CGRectMake(marginHorizontal,
                                         buttonY,
                                         self.frame.size.width - marginHorizontal*2,
                                         photoButtonHeight);
    self.choosePhotoButton.frame = photoButtonFrame;
    [self updateChoosePhotoButtonImage];

  }
}

#pragma mark - UINavigationControllerDelegate
// Called when the navigation controller shows a new top view controller via a push, pop or setting of the view controller stack.
//- (void)navigationController:(UINavigationController *)navigationController willShowViewController:(UIViewController *)viewController animated:(BOOL)animated;
//- (void)navigationController:(UINavigationController *)navigationController didShowViewController:(UIViewController *)viewController animated:(BOOL)animated;

#pragma mark - UIImagePickerControllerDelegate

// The picker does not dismiss itself; the client dismisses it in these callbacks.
// The delegate will receive one or the other, but not both, depending whether the user
// confirms or cancels.
- (void)imagePickerController:(UIImagePickerController *)picker
        didFinishPickingImage:(UIImage *)image
                  editingInfo:(NSDictionary *)editingInfo {
  self.question.responseObject = image;
  self.image = image;
  [self updateConditionals];
  [self updateChoosePhotoButtonImage];
  [self.choosePhotoButton setNeedsLayout];
  [[UIApplication sharedApplication].keyWindow.rootViewController
   dismissViewControllerAnimated:YES
   completion:^{
     self.imagePicker = nil;
   }];
}

- (void)imagePickerController:(UIImagePickerController *)picker
didFinishPickingMediaWithInfo:(NSDictionary *)info {

  NSString *mediaType = info[UIImagePickerControllerMediaType];
  if ([mediaType isEqualToString:(__bridge NSString*)kUTTypeImage]) {
    UIImage *orig = info[UIImagePickerControllerOriginalImage];
    UIImage *edited = info[UIImagePickerControllerEditedImage];
    self.image = edited ? edited : orig;
    self.question.responseObject = self.image;
    [self updateConditionals];
    [self updateChoosePhotoButtonImage];
    [self.choosePhotoButton setNeedsLayout];
  } else if ([mediaType isEqualToString:(__bridge NSString*)kUTTypeMovie]) {
    NSURL *movieURL = info[UIImagePickerControllerMediaURL];
    self.question.responseObject = movieURL;
  }

  [[UIApplication sharedApplication].keyWindow.rootViewController
   dismissViewControllerAnimated:YES
   completion:^{
     self.imagePicker = nil;
   }];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
  self.question.responseObject = nil;
  [[UIApplication sharedApplication].keyWindow.rootViewController
   dismissViewControllerAnimated:YES
   completion:^{
     self.imagePicker = nil;
   }];
}


- (void)moveCellViewToTop {
  UITableView* tableView = [self tableView];
  [tableView setContentOffset:CGPointMake(0, self.frame.origin.y) animated:YES];
}

#pragma mark - UITextViewDelegate

- (void)textViewDidBeginEditing:(UITextView *)textView {
  if ([textView.text isEqualToString:NSLocalizedString(kPlaceHolderString, nil)]) {
    textView.text = @"";
    textView.textColor = [UIColor blackColor];
    textView.font = [UIFont systemFontOfSize:15];
  }
  [self moveCellViewToTop];
}

- (BOOL)textViewShouldEndEditing:(UITextView *)textView {
  return YES;
}

-(void)textViewDidEndEditing:(UITextView *)textView {
  NSString* text = [textView.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
  if (0 == [text length]) {
    self.question.responseObject = nil;
    textView.text = NSLocalizedString(kPlaceHolderString, nil);
    textView.textColor = [UIColor lightGrayColor];
    textView.font = [UIFont systemFontOfSize:12];
  } else {
    self.question.responseObject = text;
    [self updateConditionals];
  }
}

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range
 replacementText:(NSString *)text {
  if ([text rangeOfCharacterFromSet:[NSCharacterSet newlineCharacterSet]].location == NSNotFound) {
    return YES;
  }
  [textView endEditing:YES];
  return NO;
}

#pragma mark - PacoCheckboxViewDelegate

- (void)onCheckboxChanged:(PacoCheckboxView *)checkbox {
  //if nothing is selected, set the responseObject to nil so that
  //this input can be validated correctly.
  if ([checkbox hasCheckedBox]) {
    self.question.responseObject = checkbox.bitFlags;
  }else {
    self.question.responseObject = nil;
  }
  [self updateConditionals];
}

#pragma mark - PacoStepperViewDelegate

- (void)onStepperValueChanged:(PacoStepperView *)stepper {
  long long value = [stepper.value longLongValue];
  self.question.responseObject = @(value);
  [self updateConditionals];
}

- (void)onTextFieldEditBegan:(UITextField *)textField {
  [self moveCellViewToTop];
}

#pragma mark MKMapViewDelegate
- (void)updateLocation:(CLLocation*)currentLocation {
  if (!currentLocation) {
    return;
  }
  CLLocation* prevLocation = self.question.responseObject;
  //avoid updating too often
  if (prevLocation.coordinate.latitude != currentLocation.coordinate.latitude ||
      prevLocation.coordinate.longitude != currentLocation.coordinate.longitude) {
    self.question.responseObject = currentLocation;
  }
}

- (void)mapViewDidFinishLoadingMap:(MKMapView *)mapView {
  [self updateLocation:mapView.userLocation.location];
}

- (void)mapView:(MKMapView *)mapView didUpdateUserLocation:(MKUserLocation *)userLocation {
  [self updateLocation:userLocation.location];
}


- (void)mapView:(MKMapView *)mapView didFailToLocateUserWithError:(NSError *)error {
  DDLogInfo(@"Fail to locate user (%f,%f), error: %@",
            self.map.userLocation.location.coordinate.latitude,
            self.map.userLocation.location.coordinate.longitude,
            [error description]);
}

@end
