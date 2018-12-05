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

#import "PacoScheduleEditView.h"

#import "PacoModel.h"

#import "UIColor+Paco.h"
#import "PacoTimeSelectionView.h"
#import "PacoDayOfMonthSelectionView.h"
#import "PacoDayOfWeekSelectionView.h"
#import "PacoRepeatRateSelectionView.h"
#import "PacoFirstDayOfMonthSelectionView.h"
#import "PacoESMFrequencySelectionView.h"
#import "PacoESMPeriodSelectionView.h"
#import "PacoESMIncludeWeekendsSelectionView.h"
#import "PacoByWeekOrMonthSelectionView.h"
#import "PacoTableTextCell.h"
#import "PacoTableView.h"
#import "PacoExperimentSchedule.h"
#import "UIFont+Paco.h"
#import "PacoClient.h"
#import "PacoTimeEditView.h"

NSString *kCellIdRepeat = @"repeat";
NSString *kCellIdSignalTimes = @"times";
NSString *kCellIdDaysOfWeek = @"days";
NSString *kCellIdByDaysOfWeekMonth = @"byDayOfWeek?";
NSString *kCellIdWhichFirstDayOfMonth = @"1st;2nd;3rd;4th;5th";
NSString *kCellIdWhichDayOfMonth = @"1-31";
NSString *kCellIdESMStartTime = @"esm start time";
NSString *kCellIdESMEndTime = @"esm end time";
NSString *kCellIdIncludeWeekends = @"include weekends";
NSString *kCellIdText = @"text";

@interface PacoScheduleEditView () <PacoTableViewDelegate>
@end

@implementation PacoScheduleEditView
@synthesize schedule = _schedule;

- (id)initWithFrame:(CGRect)frame schedule:(PacoExperimentSchedule*)schedule {
  self = [super initWithFrame:frame];
  if (self) {
    _schedule = [schedule copy];

    [self setBackgroundColor:[UIColor pacoBackgroundWhite]];

    _tableView = [[PacoTableView alloc] initWithFrame:CGRectZero];
    _tableView.delegate = self;
    _tableView.backgroundColor = [UIColor pacoBackgroundWhite];

    [_tableView registerClass:[PacoTimeSelectionView class] forStringKey:kCellIdSignalTimes dataClass:[NSArray class]];
    [_tableView registerClass:[PacoRepeatRateSelectionView class] forStringKey:kCellIdRepeat dataClass:[NSNumber class]];
    [_tableView registerClass:[PacoDayOfWeekSelectionView class] forStringKey:kCellIdDaysOfWeek dataClass:[NSNumber class]];
    [_tableView registerClass:[PacoByWeekOrMonthSelectionView class] forStringKey:kCellIdByDaysOfWeekMonth dataClass:[NSNumber class]];
    [_tableView registerClass:[PacoFirstDayOfMonthSelectionView class] forStringKey:kCellIdWhichFirstDayOfMonth dataClass:[NSNumber class]];
    [_tableView registerClass:[PacoDayOfMonthSelectionView class] forStringKey:kCellIdWhichDayOfMonth dataClass:[NSNumber class]];
    [_tableView registerClass:[PacoESMIncludeWeekendsSelectionView class] forStringKey:kCellIdIncludeWeekends dataClass:[NSNumber class]];
    [_tableView registerClass:[PacoTimeEditView class] forStringKey:kCellIdESMStartTime dataClass:[NSNumber class]];
    [_tableView registerClass:[PacoTimeEditView class] forStringKey:kCellIdESMEndTime dataClass:[NSNumber class]];
    [_tableView registerClass:[PacoTableTextCell class] forStringKey:kCellIdText dataClass:[NSString class]];

    [self addSubview:_tableView];
    _tableView.data = [[self class] dataFromExperimentSchedule:_schedule];
  }
  return self;
}

+ (instancetype)viewWithFrame:(CGRect)frame schedule:(PacoExperimentSchedule*)schedule {
  return [[[self class] alloc] initWithFrame:frame schedule:schedule];
}

- (void)layoutSubviews {
  [super layoutSubviews];

  CGRect frame = self.frame;
  _tableView.frame = frame;
  _tableView.backgroundColor = [UIColor pacoBackgroundWhite];
  self.backgroundColor = [UIColor pacoBackgroundWhite];
}

+ (NSArray *)dataFromExperimentSchedule:(PacoExperimentSchedule *)schedule {
  switch (schedule.scheduleType) {
    case kPacoScheduleTypeDaily:
    case kPacoScheduleTypeWeekly:
    case kPacoScheduleTypeWeekday:
    case kPacoScheduleTypeMonthly:
      return @[@[kCellIdSignalTimes, schedule.times]];
    case kPacoScheduleTypeESM:
      return @[@[kCellIdESMStartTime, @(schedule.esmStartHour)],
              @[kCellIdESMEndTime, @(schedule.esmEndHour)]];
    case kPacoScheduleTypeSelfReport:
      return @[@[kCellIdText, kCellIdText]];
    case kPacoScheduleTypeTesting: // TPE special type only used for iOS Notification testing
      return nil;
  }
  return nil;
}


#pragma mark - PacoTableViewDelegate

- (BOOL)isCellType:(NSString *)cellId reuseId:(NSString *)reuseId {
  NSString *testCellId = [reuseId componentsSeparatedByString:@":"][0];
  return [testCellId isEqualToString:cellId];
}

- (id)realRowData:(id)rowData {
  if([rowData isKindOfClass:[NSArray class]]) {
    return ((NSArray *)rowData).lastObject;
  }
  return rowData;
}

- (void)initializeCell:(UITableViewCell *)cell
              withData:(id)rowData
            forReuseId:(NSString *)reuseId {
  cell.userInteractionEnabled = YES;

  switch (self.schedule.scheduleType) {
    case kPacoScheduleTypeDaily:
    case kPacoScheduleTypeWeekly:
    case kPacoScheduleTypeWeekday:
    case kPacoScheduleTypeMonthly: {
      NSAssert([self isCellType:kCellIdSignalTimes reuseId:reuseId], @"cellType should be signalTimes");
      PacoTimeSelectionView *cellView = (PacoTimeSelectionView *)cell;
      cellView.completionBlock = ^{
        [self onDoneEditing];
      };
      cellView.times = [self realRowData:rowData];
      break;
    }
      
    case kPacoScheduleTypeESM: {
      if ([self isCellType:kCellIdESMStartTime reuseId:reuseId]) {
        PacoTimeEditView *cellView = (PacoTimeEditView *)cell;
        cellView.completionBlock = ^{
          [self onDoneEditing];
        };
        cellView.time = [self realRowData:rowData];
        cellView.title = NSLocalizedString(@"Start Time", nil);
      } else if([self isCellType:kCellIdESMEndTime reuseId:reuseId]) {
        PacoTimeEditView *cellView = (PacoTimeEditView *)cell;
        cellView.completionBlock = ^{
          [self onDoneEditing];
        };
        cellView.time = [self realRowData:rowData];
        cellView.title = NSLocalizedString(@"End Time", nil);
      } else {
        NSAssert(NO, @"cellType should either be esmStartTime or esmEndTime");
      }
      break;
    }
      
    case kPacoScheduleTypeSelfReport:
      cell.userInteractionEnabled = NO;
      if ([self isCellType:kCellIdText reuseId:reuseId]) {
        PacoTableTextCell *cellView = (PacoTableTextCell *)cell;
        cellView.textLabel.text = NSLocalizedString(@"Self scheduled.", nil);
        cellView.detailTextLabel.text = NSLocalizedString(@"Submit responses whenever you wish.", nil);
      }
      break;
      
    case kPacoScheduleTypeTesting:
      cell.userInteractionEnabled = NO;
      break;
      
    default:
      NSAssert(NO, @"scheduleType is not correct!");
      break;
  }
}

- (void)onDoneEditing {
  NSString* errorMsg = [self.schedule validate];
  if (errorMsg) {
    [[[UIAlertView alloc] initWithTitle:NSLocalizedString(@"Oops", nil)
                                message:errorMsg
                               delegate:nil
                      cancelButtonTitle:@"OK"
                      otherButtonTitles:nil] show];
  }
  [self.tableView dismissAnyDatePicker];
}


- (void)cellSelected:(UITableViewCell *)cell rowData:(id)rowData reuseId:(NSString *)reuseId {
  // When the time picker is active a cell selection triggers dismissal of the time picker.
  if ([reuseId hasPrefix:kCellIdSignalTimes]) {
    PacoTimeSelectionView *timeSelect = (PacoTimeSelectionView *)cell;
    [timeSelect cancelDateEdit];
    return;
  }
  [self onDoneEditing];
}

- (void)didReceiveTapButNoCellSelected {
  if ([self.schedule isESMSchedule]) {
    [self onDoneEditing];
  }
}

- (void)dataUpdated:(UITableViewCell *)cell rowData:(id)rowData reuseId:(NSString *)reuseId {
  switch (self.schedule.scheduleType) {
    case kPacoScheduleTypeDaily:
    case kPacoScheduleTypeWeekly:
    case kPacoScheduleTypeWeekday:
    case kPacoScheduleTypeMonthly: {
      NSAssert([self isCellType:kCellIdSignalTimes reuseId:reuseId], @"cellType should be signalTimes");
      self.schedule.times = rowData;
      break;
    }
    case kPacoScheduleTypeESM: {
      if ([self isCellType:kCellIdESMStartTime reuseId:reuseId]) {
        self.schedule.esmStartHour = [rowData longLongValue];
      } else if ([self isCellType:kCellIdESMEndTime reuseId:reuseId]) {
        self.schedule.esmEndHour = [rowData longLongValue];
      }else {
        NSAssert(NO, @"cellType should either be esmStartTime or esmEndTime");
      }
      break;
    }
    case kPacoScheduleTypeSelfReport:
      break;
    case kPacoScheduleTypeTesting:
      break;
    default:
      NSAssert(NO, @"scheduleType is not correct!");
      break;
  }
}

@end
