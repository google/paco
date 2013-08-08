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
#import "PacoTitleView.h"
#import "PacoExperimentInput.h"
#import "PacoExperimentDefinition.h"
#import "PacoExperiment.h"
#import "PacoAlertView.h"
#import "PacoEvent.h"
#import "PacoEventManager.h"

NSString *kCellIdQuestion = @"question";

@interface PacoQuestionScreenViewController () <PacoTableViewDelegate>

@property(nonatomic, strong) NSArray* visibleInputs;

@end

@implementation PacoQuestionScreenViewController

//validate all the inputs until we find the first invalid input
- (NSError*)validateInputs {
  NSError* error = nil;
  for (PacoExperimentInput* input in self.visibleInputs) {
    if (input.mandatory && input.responseObject == nil) {
      error = [NSError errorWithDomain:@"com.paco.userinput"
                                  code:-1
                              userInfo:@{NSLocalizedDescriptionKey : input.text}];
      break;
    }
  }
  return error;
}

- (void)onDone {
  NSError* error = [self validateInputs];
  if (error) {
    [[[UIAlertView alloc] initWithTitle:@"Required Answer Missing:"
                                message:error.localizedDescription
                               delegate:nil
                      cancelButtonTitle:@"OK"
                      otherButtonTitles:nil] show];
    return;
  }
  
  //create a survey event and save it to cache
  PacoEvent* surveyEvent = [PacoEvent surveyEventForDefinition:self.experiment.definition
                                                    withInputs:self.visibleInputs];
  [[PacoEventManager sharedInstance] saveEvent:surveyEvent];
  
  //clear all inputs' submitted responseObject for the definition 
  [self.experiment.definition clearInputs];
  
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

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
  self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
  if (self) {
    self.navigationItem.titleView = [[PacoTitleView alloc] initText:@"Participate!"];
    self.navigationItem.hidesBackButton = NO;
    self.navigationItem.rightBarButtonItem =
        [[UIBarButtonItem alloc] initWithTitle:@"Submit"
                                         style:UIBarButtonItemStyleDone
                                        target:self
                                        action:@selector(onDone)];
  }
  return self;
}

- (NSArray *)boxInputs:(NSArray *)inputs {
  NSMutableArray *boxed = [NSMutableArray array];
  for (id input in inputs) {
    NSArray *boxedInput = [NSArray arrayWithObjects:kCellIdQuestion, input, nil];
    [boxed addObject:boxedInput];
  }
  return boxed;
}

- (void)viewDidLoad {
  [super viewDidLoad];

  PacoTableView *table = [[PacoTableView alloc] initWithFrame:CGRectZero];
  table.delegate = self;
  table.tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
  [table registerClass:[PacoQuestionView class] forStringKey:kCellIdQuestion dataClass:[PacoExperimentInput class]];
//  table.data = [self boxInputs:self.experiment.definition.inputs];
  table.backgroundColor = [PacoColor pacoLightBlue];
  self.view = table;
  [self reloadTable];
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
/*
- (NSArray *)parseExpression:(NSString *)expr {
  NSArray *ops = [NSArray arrayWithObjects:
                      @">=",
                      @"<=",
                      @"==",
                      @"!=",
                      @">",
                      @"<",
                      @"=",
                      nil];
  for (NSString *op in ops) {
    NSArray *exprArray = [expr componentsSeparatedByString:op];
    if (exprArray.count == 3) {
      return exprArray;
    }
  }
  return nil;
}
*/
- (PacoExperimentInput *)questionByName:(NSString *)name {
  for (PacoExperimentInput *question in self.experiment.definition.inputs) {
    if ([question.name isEqualToString:name]) {
      return question;
    }
  }
  return nil;
}

- (BOOL)checkConditions:(PacoExperimentInput *)question {
  if (!question.conditional) {
    return YES;
  }

  if ([question.conditionalExpression length] == 0) {
    return NO;
  }
  NSArray *expr = [PacoExperimentInput parseExpression:question.conditionalExpression];
  NSString *questionName = [expr objectAtIndex:0];
  questionName = [questionName stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
  NSString *op = [expr objectAtIndex:1];
  op = [op stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
  NSString *value = [expr objectAtIndex:2];
  value = [value stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
  
  PacoExperimentInput *dependantQuestion = [self questionByName:questionName];

  // Apparently we can't find the parent question, so no use for this one.
  if (dependantQuestion == nil) {
    return NO;
  }

  // If the parent isn't answered yet, then hide this question.
  if (dependantQuestion.responseObject == nil) {
    return NO;
  }

  // If the dependent question is conditional, make sure it passes it's conditions
  // before proceeding to check the current ones.
  BOOL parentConditionalsPass = [self checkConditions:dependantQuestion];
  if (!parentConditionalsPass) {
    return NO;
  }

  // Prepare the value for the left hand side of the expression.
  int iValueLHS = 0;
  if ([dependantQuestion.questionType isEqualToString:@"question"]) {
    if ([dependantQuestion.responseType isEqualToString:@"likert_smileys"]) {
      assert(dependantQuestion.responseObject  == nil || [dependantQuestion.responseObject isKindOfClass:[NSNumber class]]);
      iValueLHS = [dependantQuestion.responseObject intValue] + 1;
    } else if ([dependantQuestion.responseType isEqualToString:@"likert"]) {
      assert(dependantQuestion.responseObject  == nil || [dependantQuestion.responseObject isKindOfClass:[NSNumber class]]);
      iValueLHS = [dependantQuestion.responseObject intValue] + 1;
    } else if ([dependantQuestion.responseType isEqualToString:@"open text"]) {
      iValueLHS = dependantQuestion.responseObject ? 1 : 0;
    } else if ([dependantQuestion.responseType isEqualToString:@"list"]) {
      assert(dependantQuestion.responseObject  == nil || [dependantQuestion.responseObject isKindOfClass:[NSNumber class]]);
      iValueLHS = [dependantQuestion.responseObject intValue] + 1;
    } else if ([dependantQuestion.responseType isEqualToString:@"number"]) {
      assert(dependantQuestion.responseObject  == nil || [dependantQuestion.responseObject isKindOfClass:[NSNumber class]]);
      iValueLHS = [dependantQuestion.responseObject intValue];
    } else if ([dependantQuestion.responseType isEqualToString:@"location"]) {
      iValueLHS = dependantQuestion.responseObject ? 1 : 0;
    } else if ([dependantQuestion.responseType isEqualToString:@"photo"]) {
      iValueLHS = dependantQuestion.responseObject ? 1 : 0;
    }
  }
  
  // Prepare the value for the right hand side of the expression.
  int iValueRHS = [value intValue];

  // Evaluate the expression.
  BOOL satisfiesCondition = NO;
  if ([op isEqualToString:@">="]) {
    satisfiesCondition = iValueLHS >= iValueRHS;
  } else if ([op isEqualToString:@"<="]) {
    satisfiesCondition = iValueLHS <= iValueRHS;
  } else if ([op isEqualToString:@"=="]) {
    satisfiesCondition = iValueLHS == iValueRHS;
  } else if ([op isEqualToString:@"!="]) {
    satisfiesCondition = iValueLHS != iValueRHS;
  } else if ([op isEqualToString:@">"]) {
    satisfiesCondition = iValueLHS > iValueRHS;
  } else if ([op isEqualToString:@"<"]) {
    satisfiesCondition = iValueLHS < iValueRHS;
  } else if ([op isEqualToString:@"="]) {
    satisfiesCondition = iValueLHS == iValueRHS;
  } else {
    NSLog(@"Invalid operation [%@]", op);
  }
  return satisfiesCondition;
}

- (void)reloadTable {
  NSMutableArray *questions = [NSMutableArray array];
  for (PacoExperimentInput *question in self.experiment.definition.inputs) {
    if (!question.conditional) {
      [questions addObject:question];
    } else {
      BOOL conditionsSatified = [self checkConditions:question];
      if (conditionsSatified) {
        [questions addObject:question];
      }
    }
  }
  
  self.visibleInputs = questions;
  PacoTableView *table = (PacoTableView *)self.view;
  table.data = [self boxInputs:questions];
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return [self.experiment.definition.inputs count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  PacoExperimentInput *question = [self.experiment.definition.inputs objectAtIndex:indexPath.row];
  PacoQuestionView *cell = [tableView dequeueReusableCellWithIdentifier:kCellIdQuestion];
  if (!cell) {
    cell = [[PacoQuestionView alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:kCellIdQuestion];
  }
  cell.question = question;
  return cell;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
}

@end
