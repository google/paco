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

#import "PacoExperimentDetailsViewController.h"

#import "UIColor+Paco.h"
#import "UIFont+Paco.h"
#import "PacoConsentViewController.h"
#import "PacoModel.h"
#import "PacoExperimentDefinition.h"
#import "PacoClient.h"
#import "PacoDateUtility.h"

@interface PacoExperimentDetailsViewController ()<UIAlertViewDelegate>
@property (nonatomic, retain) PacoExperimentDefinition *experiment;
@end

@implementation PacoExperimentDetailsViewController
@synthesize experiment = _experiment;


+ (NSString *)stringFromData:(NSData *)data {
  const char *bytes = [data bytes];
  char *dst = malloc([data length] + 1);
  memset(dst, 0, [data length] + 1);
  memcpy(dst, bytes, [data length]);
  NSString *converted = @(dst);
  free(dst);
  return converted;
}

+(PacoExperimentDetailsViewController*)controllerWithExperiment:(PacoExperimentDefinition *)experiment {
  PacoExperimentDetailsViewController* controller =
      [[PacoExperimentDetailsViewController alloc] initWithNibName:nil bundle:nil];
  controller.experiment = experiment;
  controller.navigationItem.title = experiment.title;
  return controller;
}

- (NSString *)jsonStringFromObj:(id)jsonObject {
  NSError *jsonError = nil;
  NSData *jsonData =
      [NSJSONSerialization dataWithJSONObject:jsonObject
                                      options:NSJSONWritingPrettyPrinted
                                        error:&jsonError];
  if (jsonError) {
    return nil;
  }
  return [[self class] stringFromData:jsonData];
}

- (void)viewDidLoad {
  [super viewDidLoad];

  if ([self respondsToSelector:@selector(edgesForExtendedLayout)]) {
    self.edgesForExtendedLayout = UIRectEdgeNone;
  }

  self.view = [[UIScrollView alloc]initWithFrame:self.view.frame];
  self.view.backgroundColor = [UIColor pacoBackgroundWhite];
  self.automaticallyAdjustsScrollViewInsets = NO;

  UILabel* titleLabel = [[UILabel alloc] initWithFrame:CGRectZero];
  NSString* labelText = self.experiment.title;
  titleLabel.text = labelText;
  titleLabel.font = [UIFont pacoTableCellFont];
  titleLabel.textColor = [UIColor pacoDarkBlue];
  titleLabel.backgroundColor = [UIColor clearColor];
  titleLabel.numberOfLines = 0;
  [self.view addSubview:titleLabel];
  CGRect frame = titleLabel.frame;
  frame.origin.x = 10;
  frame.origin.y = 10;
  frame.size.width = self.view.frame.size.width - 20;
  frame.size.height = self.view.frame.size.height;
  titleLabel.frame = frame;
  [titleLabel sizeToFit];

  CGRect desLabelFrame = CGRectMake(10,
                                    titleLabel.frame.origin.y + titleLabel.frame.size.height + 20,
                                    self.view.frame.size.width - 20,
                                    20);
  UILabel* desLabel = [[UILabel alloc] initWithFrame:desLabelFrame];
  NSString* desText = NSLocalizedString(@"Description:", nil);
  desLabel.text = desText;
  desLabel.font = [UIFont pacoNormalButtonFont];
  desLabel.textColor = [UIColor pacoDarkBlue];
  desLabel.backgroundColor = [UIColor clearColor];
  desLabel.numberOfLines = 0;
  [self.view addSubview:desLabel];
  int yPosition = desLabel.frame.origin.y + desLabel.frame.size.height + 5;

  CGRect descriptionTextFrame = CGRectMake(10, yPosition, self.view.frame.size.width - 20, 0);
  UILabel *descriptionLabel = [[UILabel alloc] initWithFrame:descriptionTextFrame];
  descriptionLabel.backgroundColor=[UIColor clearColor];
  descriptionLabel.font = [UIFont fontWithName:@"HelveticaNeue" size:16];
  descriptionLabel.textColor = [UIColor pacoDarkBlue];
  descriptionLabel.text = self.experiment.experimentDescription;
  descriptionLabel.numberOfLines = 0;
  [descriptionLabel sizeToFit];
  frame = descriptionLabel.frame;
  descriptionLabel.frame = CGRectMake(10,
                                      yPosition,
                                      self.view.frame.size.width - 20,
                                      frame.size.height);
  [self.view addSubview:descriptionLabel];

  yPosition += descriptionLabel.frame.size.height + 20;

  if (self.experiment.startDate) {
    UILabel* dateLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, yPosition, 300, 20)];
    dateLabel.text = [NSString stringWithFormat:@"%@                  %@",
                      NSLocalizedString(@"Start Date:", nil), NSLocalizedString(@"End Date:", nil)];
    dateLabel.font = [UIFont pacoNormalButtonFont];
    dateLabel.textColor = [UIColor pacoDarkBlue];
    dateLabel.backgroundColor = [UIColor clearColor];
    dateLabel.numberOfLines = 0 ;
    [self.view addSubview:dateLabel];
    yPosition = dateLabel.frame.origin.y + dateLabel.frame.size.height + 5;

    NSString* startDate = [PacoDateUtility stringWithYearAndDayFromDate:self.experiment.startDate];
    UILabel* dateText = [[UILabel alloc] initWithFrame:CGRectMake(10, yPosition, 300, 20)];
    dateText.text = [NSString stringWithFormat:@"%@               %@",
                     startDate,
                     self.experiment.inclusiveEndDateString];
    dateText.font = [UIFont fontWithName:@"HelveticaNeue" size:16];
    dateText.textColor = [UIColor pacoDarkBlue];
    dateText.backgroundColor = [UIColor clearColor];
    dateText.numberOfLines = 0 ;
    [self.view addSubview:dateText];
    yPosition = dateText.frame.origin.y + dateText.frame.size.height + 20;
  }

  CGRect creatorLabelFrame = CGRectMake(10, yPosition, self.view.frame.size.width - 20, 20);
  UILabel* creatorLabel = [[UILabel alloc] initWithFrame:creatorLabelFrame];
  NSString* creText = NSLocalizedString(@"Creator:", nil);
  creatorLabel.text = creText;
  creatorLabel.font = [UIFont pacoNormalButtonFont];
  creatorLabel.textColor = [UIColor pacoDarkBlue];
  creatorLabel.backgroundColor = [UIColor clearColor];
  creatorLabel.numberOfLines = 0;
  [self.view addSubview:creatorLabel];
  yPosition += 25;

  UILabel* creatorValueLabel = [[UILabel alloc] initWithFrame:CGRectZero];
  NSString* creatorText = self.experiment.creator;
  creatorValueLabel.text = creatorText;
  creatorValueLabel.font = [UIFont pacoTableCellDetailFont];
  creatorValueLabel.textColor = [UIColor pacoDarkBlue];
  creatorValueLabel.backgroundColor = [UIColor clearColor];
  creatorValueLabel.numberOfLines = 0;
  [self.view addSubview:creatorValueLabel];
  CGRect creatorframe = creatorValueLabel.frame;
  creatorframe.origin.x = 10;
  creatorframe.origin.y = creatorLabel.frame.origin.y + 30;
  creatorframe.size.width = self.view.frame.size.width - 20;
  creatorframe.size.height = self.view.frame.size.height;
  creatorValueLabel.frame = creatorframe;
  [creatorValueLabel sizeToFit];
  yPosition += creatorValueLabel.frame.size.height + 20;

  if (![self.experiment isCompatibleWithIOS]) {
    UIImage* lockImage = [UIImage imageNamed:@"incompatible"];
    CGRect lockViewFrame = CGRectMake(10, yPosition, lockImage.size.width, lockImage.size.height);
    UIImageView* lockView = [[UIImageView alloc] initWithFrame:lockViewFrame];
    [lockView setImage:lockImage];
    [self.view addSubview:lockView];

    UILabel* incompatibilityMsg = [[UILabel alloc] initWithFrame:CGRectZero];
    [incompatibilityMsg setText:NSLocalizedString(@"Incompatible with iOS", nil)];
    incompatibilityMsg.font = [UIFont pacoBoldFont];
    incompatibilityMsg.textColor = [UIColor redColor];
    incompatibilityMsg.backgroundColor = [UIColor clearColor];
    [incompatibilityMsg sizeToFit];
    [self.view addSubview:incompatibilityMsg];
    CGRect textFrame = incompatibilityMsg.frame;
    textFrame.origin.x = lockImage.size.width + 15;
    textFrame.origin.y = yPosition + lockImage.size.height - incompatibilityMsg.frame.size.height;
    textFrame.size = incompatibilityMsg.frame.size;
    incompatibilityMsg.frame = textFrame;
    yPosition += incompatibilityMsg.frame.size.height + 20;
  }

  UIButton* join = [UIButton buttonWithType:UIButtonTypeRoundedRect];
  [join setTitle:NSLocalizedString(@"Join this Experiment", nil) forState:UIControlStateNormal];
  if (IS_IOS_7) {
    join.titleLabel.font = [UIFont pacoNormalButtonFont];
  }
  [join addTarget:self action:@selector(onJoin) forControlEvents:UIControlEventTouchUpInside];
  [self.view addSubview:join];
  [join sizeToFit];
  CGRect  joinframe = join.frame;
  joinframe.origin.x = (self.view.frame.size.width - join.frame.size.width) / 2;
  joinframe.origin.y = yPosition;
  join.frame = joinframe;
  yPosition += join.frame.size.height + 10;
  [(UIScrollView*)self.view setContentSize:CGSizeMake(self.view.frame.size.width, yPosition)];
}

- (void)onJoin {
  BOOL joined = [[PacoClient sharedInstance] hasJoinedExperimentWithId:self.experiment.experimentId];
  if (joined) {
    [[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Congratulations!", nil)
                                message:NSLocalizedString(@"Joined Experiment Alert", nil)
                               delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil] show];
    return;
  }
  if (![self.experiment isCompatibleWithIOS]) {
    [[[UIAlertView alloc] initWithTitle:nil
                               message:NSLocalizedString(@"Experiment Incompatibility alertview message", nil)
                              delegate:self
                     cancelButtonTitle:NSLocalizedString(@"Join anyway", nil)
                     otherButtonTitles:NSLocalizedString(@"Skip joining", nil), nil] show];
    return;
  }
  [self loadConsentViewController];
}

- (void)loadConsentViewController {
  PacoConsentViewController *consent =
      [PacoConsentViewController controllerWithDefinition:self.experiment];
  [self.navigationController pushViewController:consent animated:YES];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
  switch (buttonIndex) {
    case 0:
      [self loadConsentViewController];
      break;
    case 1:
      [self.navigationController popViewControllerAnimated:YES];
      break;
    default:
      break;
  }
}

- (void)didReceiveMemoryWarning {
  [super didReceiveMemoryWarning];
  // Dispose of any resources that can be recreated.
}

@end
