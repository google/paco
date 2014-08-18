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

#import "PacoQuestionScreenViewController.h"

#import "UIColor+Paco.h"
#import "PacoClient.h"
#import "UIFont+Paco.h"
#import "PacoModel.h"
#import "PacoQuestionView.h"
#import "PacoService.h"
#import "PacoTableView.h"
#import "PacoExperimentInput.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperiment.h"
#import "PacoAlertView.h"
#import "PacoEvent.h"
#import "PacoEventManager.h"
#import "PacoInputEvaluator.h"
#import "PacoScheduler.h"
#import "UILocalNotification+Paco.h"

NSString *kCellIdQuestion = @"question";

@interface PacoQuestionScreenViewController () <PacoTableViewDelegate>

@property(nonatomic, strong) PacoInputEvaluator* evaluator;
@property(nonatomic, strong) UILocalNotification* notification;

@end

@implementation PacoQuestionScreenViewController

- (id)initWithNibName:(NSString *)nibNameOrNil
               bundle:(NSBundle *)nibBundleOrNil
           experiment:(PacoExperiment*)experiment
      andNotification:(UILocalNotification*)notification{
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.navigationItem.title = NSLocalizedString(@"Participate!", nil);
    self.navigationItem.rightBarButtonItem =
    [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Submit", nil)
                                     style:UIBarButtonItemStyleDone
                                    target:self
                                    action:@selector(onDone)];
    self.navigationItem.leftBarButtonItem =
        [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel
                                                      target:self
                                                      action:@selector(onCancel:)];
    _evaluator = [PacoInputEvaluator evaluatorWithExperiment:experiment];
    _notification = notification;
  }
  return self;
}

+ (id)controllerWithExperiment:(PacoExperiment*)experiment {
  return [PacoQuestionScreenViewController controllerWithExperiment:experiment andNotification:nil];
}

+ (id)controllerWithExperiment:(PacoExperiment*)experiment
               andNotification:(UILocalNotification*)notification{
  return [[PacoQuestionScreenViewController alloc] initWithNibName:nil
                                                            bundle:nil
                                                        experiment:experiment
                                                   andNotification:notification];
}

- (void)viewDidLoad {
  [super viewDidLoad];

  if ([self respondsToSelector:@selector(edgesForExtendedLayout)]) {
    self.edgesForExtendedLayout = UIRectEdgeNone;
  }

  PacoTableView *table = [[PacoTableView alloc] initWithFrame:CGRectZero];
  table.delegate = self;
  table.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
  [table registerClass:[PacoQuestionView class] forStringKey:kCellIdQuestion dataClass:[PacoExperimentInput class]];
  table.backgroundColor = [UIColor pacoBackgroundWhite];
  self.view = table;
  [self reloadTable];
}


- (void)viewWillAppear:(BOOL)animated {
  [super viewWillAppear:animated];
  NSLog(@"Survey shows up:");
  [self processAttachedNotificationIfNeeded];

  if (self.evaluator.experiment.definition.webReccommended) {
    [self showRecommendationAlert];
  }
}

- (void)onCancel:(id)sender {
  //clear all inputs' submitted responseObject for the definition when user clicks on back button
  [self.evaluator.experiment.definition clearInputs];
  [self dismiss];
}


- (void)dismiss {
  [self.navigationController popViewControllerAnimated:YES];
}

- (void)showRecommendationAlert {
  NSString* title = [NSString stringWithFormat:@"%@ %@", NSLocalizedString(@"Hi", nil), [[PacoClient sharedInstance] userName]];
  NSString* message = NSLocalizedString(@"It is recommended that you fill this study out on your computer instead.", nil);
  [PacoAlertView showAlertWithTitle:title
                            message:message
                       dismissBlock:^(NSInteger buttonIndex) {
                         if (self.notification) {
                           [[PacoClient sharedInstance].scheduler handleRespondedNotification:self.notification];
                         }
                         [self dismiss];
                       }
                  cancelButtonTitle:NSLocalizedString(@"I will respond on the web", nil)
                  otherButtonTitles:nil];
}

- (void)processAttachedNotificationIfNeeded {
  //No need to worry about self-report only experiment
  if ([self.evaluator.experiment isSelfReportExperiment]) {
    return;
  }

  if (self.notification) {
    NSLog(@"Detail: %@", [self.notification pacoDescription]);
  }
  BOOL needToDetectActiveNotification = NO;
  if (self.notification == nil ||   //self-report
      (self.notification != nil &&  //non-self-report, but notification is not active any more
       ![[PacoClient sharedInstance].scheduler isNotificationActive:self.notification])) {
        needToDetectActiveNotification = YES;
        NSLog(@"Need to detect active notification.");
  }

  if (needToDetectActiveNotification) {
    if (self.notification) {
      NSLog(@"Cancelling current notification from the tray");
      [UILocalNotification pacoCancelLocalNotification:self.notification];
    }
    NSString* experimentId = self.evaluator.experiment.instanceId;
    NSAssert([experimentId length] > 0, @"experiementId should be valid");
    self.notification =
        [[PacoClient sharedInstance].scheduler activeNotificationForExperiment:experimentId];
    if (self.notification) {
      NSLog(@"Active Notification Detected: %@", [self.notification pacoDescription]);
    } else {
      NSLog(@"No Active Notification Detected. ");
    }
  }
  if (self.notification == nil) {
    NSLog(@"Self-report");
  }
}

- (void)onDone {
  NSError* error = [self.evaluator validateVisibleInputs];
  if (error) {
    [[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Required Answer Missing:", nil)
                                message:error.localizedDescription
                               delegate:nil
                      cancelButtonTitle:@"OK"
                      otherButtonTitles:nil] show];
    return;
  }

  [self processAttachedNotificationIfNeeded];

  [[PacoClient sharedInstance] submitSurveyWithDefinition:self.evaluator.experiment.definition
                                             surveyInputs:self.evaluator.visibleInputs
                                             notification:self.notification];

  //clear all inputs' submitted responseObject for the definition
  [self.evaluator.experiment.definition clearInputs];

  NSString* title = NSLocalizedString(@"Nice", nil);
  NSString* message = NSLocalizedString(@"Your survey was successfully submitted!", nil);
  [PacoAlertView showAlertWithTitle:title
                            message:message
                       dismissBlock:^(NSInteger buttonIndex) {
                         [self dismiss];
                       }
                  cancelButtonTitle:@"OK"
                  otherButtonTitles:nil];
}


#pragma mark - PacoTableViewDelegate

- (void)initializeCell:(UITableViewCell *)cell
              withData:(id)rowData
            forReuseId:(NSString *)reuseId {
  assert([cell isKindOfClass:[PacoQuestionView class]]);
  PacoQuestionView *questionCell = (PacoQuestionView *)cell;
  NSArray *rowDataArray = (NSArray *)rowData;
  //NSString *stringKey = [rowDataArray objectAtIndex:0];
  assert([rowDataArray[1] isKindOfClass:[PacoExperimentInput class]]);
  PacoExperimentInput *question = (PacoExperimentInput *)rowDataArray[1];

  // Just assign here, all the magic is in PacoQuestionView.
  questionCell.question = question;
}

- (void)cellSelected:(UITableViewCell *)cell rowData:(id)rowData reuseId:(NSString *)reuseId {

}

- (void)dataUpdated:(UITableViewCell *)cell rowData:(id)rowData reuseId:(NSString *)reuseId {

}

- (NSArray *)boxInputs:(NSArray *)inputs {
  NSMutableArray *boxed = [NSMutableArray array];
  for (id input in inputs) {
    NSArray *boxedInput = @[kCellIdQuestion, input];
    [boxed addObject:boxedInput];
  }
  return boxed;
}

- (void)reloadTable {
  PacoTableView *table = (PacoTableView *)self.view;
  table.data = [self boxInputs:[self.evaluator evaluateAllInputs]];
}



#pragma mark - UITableViewDataSource
//- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
//  return [self.experiment.definition.inputs count];
//}
//
//- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
//  PacoExperimentInput *question = [self.experiment.definition.inputs objectAtIndex:indexPath.row];
//  PacoQuestionView *cell = [tableView dequeueReusableCellWithIdentifier:kCellIdQuestion];
//  if (!cell) {
//    cell = [[PacoQuestionView alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:kCellIdQuestion];
//  }
//  cell.question = question;
//  return cell;
//}
//
#pragma mark - UITableViewDelegate

//- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
//}

@end
