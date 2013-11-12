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

#import "PacoColor.h"
#import "PacoClient.h"
#import "PacoFont.h"
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
    self.navigationItem.title = @"Participate!";
    self.navigationItem.hidesBackButton = NO;
    self.navigationItem.rightBarButtonItem =
    [[UIBarButtonItem alloc] initWithTitle:@"Submit"
                                     style:UIBarButtonItemStyleDone
                                    target:self
                                    action:@selector(onDone)];
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
  
  PacoTableView *table = [[PacoTableView alloc] initWithFrame:CGRectZero];
  table.delegate = self;
  table.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
  [table registerClass:[PacoQuestionView class] forStringKey:kCellIdQuestion dataClass:[PacoExperimentInput class]];
  table.backgroundColor = [PacoColor pacoBackgroundWhite];
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

- (void)showRecommendationAlert {
  NSString* title = [NSString stringWithFormat:@"Hi %@", [[PacoClient sharedInstance] userName]];
  NSString* message = @"It is recommended that you fill this study out on your computer instead.";
  [PacoAlertView showAlertWithTitle:title
                            message:message
                       dismissBlock:^(NSInteger buttonIndex) {
                         if (self.notification) {
                           [[PacoClient sharedInstance].scheduler handleRespondedNotification:self.notification];
                         }
                         [self.navigationController popViewControllerAnimated:YES];
                       }
                  cancelButtonTitle:@"I will respond on the web"
                  otherButtonTitles:nil];
}

- (void)processAttachedNotificationIfNeeded {
  //No need to worry about self-report only experiment
  if ([self.evaluator.experiment isSelfReportExperiment]) {
    return;
  }

  if (self.notification) {
    NSLog(@"Detail: %@", [self.notification description]);
    NSLog(@"Notification Status: %@", [self.notification pacoStatusDescription]);
  }
  BOOL needToDetectActiveNotification = NO;
  if (self.notification == nil ||   //self-report
      (self.notification != nil &&  //non-self-report, but notification is not active any more
       ![[PacoClient sharedInstance].scheduler isNotificationActive:self.notification])) {
        needToDetectActiveNotification = YES;
        NSLog(@"Need to detect active notification.");
  }
  
  if (needToDetectActiveNotification) {
    [UILocalNotification pacoCancelLocalNotification:self.notification];
    NSString* experimentId = self.evaluator.experiment.instanceId;
    NSAssert([experimentId length] > 0, @"experiementId should be valid");
    self.notification =
        [[PacoClient sharedInstance].scheduler activeNotificationForExperiment:experimentId];
    if (self.notification) {
      NSLog(@"Active Notification Detected: %@", [self.notification description]);
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
    [[[UIAlertView alloc] initWithTitle:@"Required Answer Missing:"
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
  
  NSString* title = @"Nice";
  NSString* message = @"Your survey was successfully submitted!";  
  [PacoAlertView showAlertWithTitle:title
                            message:message
                       dismissBlock:^(NSInteger buttonIndex) {
                           [self.navigationController popViewControllerAnimated:YES];
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
  assert([[rowDataArray objectAtIndex:1] isKindOfClass:[PacoExperimentInput class]]);
  PacoExperimentInput *question = (PacoExperimentInput *)[rowDataArray objectAtIndex:1];

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
    NSArray *boxedInput = [NSArray arrayWithObjects:kCellIdQuestion, input, nil];
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
