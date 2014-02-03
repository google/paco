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

#import "PacoColor.h"
#import "PacoFont.h"
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
#import "PacoFont.h"
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

- (id)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    [self setBackgroundColor:[PacoColor pacoBackgroundWhite]];

    _tableView = [[PacoTableView alloc] initWithFrame:CGRectZero];
    _tableView.delegate = self;
    _tableView.backgroundColor = [PacoColor pacoBackgroundWhite];
    [self addSubview:_tableView];

    _joinButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    [_joinButton setTitle:@"Join" forState:UIControlStateNormal];
    if (IS_IOS_7) {
      _joinButton.titleLabel.font = [PacoFont pacoNormalButtonFont];
    }

    _tableView.footer = _joinButton;
    [_joinButton sizeToFit];
    
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
  }
  return self;
}

- (void)setSchedule:(PacoExperimentSchedule *)schedule {
  _schedule = [schedule copy];
  _tableView.data = [[self class] dataFromExperimentSchedule:_schedule];
  [self setNeedsLayout];
}

- (void)layoutSubviews {
  [super layoutSubviews];
  
  CGRect frame = self.frame;
  _tableView.frame = frame;
  _tableView.backgroundColor = [PacoColor pacoBackgroundWhite];
  self.backgroundColor = [PacoColor pacoBackgroundWhite];
}

+ (NSArray *)dataFromExperimentSchedule:(PacoExperimentSchedule *)schedule {
  switch (schedule.scheduleType) {
  case kPacoScheduleTypeDaily:
    return [NSArray arrayWithObjects:
                [NSArray arrayWithObjects:kCellIdSignalTimes, schedule.times, nil],
                nil];
  case kPacoScheduleTypeWeekly:
    return [NSArray arrayWithObjects:
                [NSArray arrayWithObjects:kCellIdRepeat, [NSNumber numberWithInt:(1 << schedule.repeatRate)], nil],
                [NSArray arrayWithObjects:kCellIdDaysOfWeek, [NSNumber numberWithUnsignedInt:schedule.weekDaysScheduled], nil],
                [NSArray arrayWithObjects:kCellIdSignalTimes, schedule.times, nil],
                nil];
  case kPacoScheduleTypeWeekday:
    return [NSArray arrayWithObjects:
                [NSArray arrayWithObjects:kCellIdSignalTimes, schedule.times, nil],
                nil];
  case kPacoScheduleTypeMonthly:
    return [NSArray arrayWithObjects:
                [NSArray arrayWithObjects:kCellIdRepeat, [NSNumber numberWithInt:(1 << schedule.repeatRate)], nil],
                [NSArray arrayWithObjects:kCellIdByDaysOfWeekMonth, [NSNumber numberWithBool:schedule.byDayOfWeek], nil],
                [NSArray arrayWithObjects:((schedule.byDayOfWeek) ? kCellIdWhichFirstDayOfMonth : kCellIdWhichDayOfMonth), [NSNumber numberWithInt:schedule.dayOfMonth], nil],
                [NSArray arrayWithObjects:kCellIdDaysOfWeek, [NSNumber numberWithUnsignedInt:schedule.weekDaysScheduled], nil],
                nil];
  case kPacoScheduleTypeESM:
    return [NSArray arrayWithObjects:
                [NSArray arrayWithObjects:kCellIdESMStartTime, [NSNumber numberWithLongLong:schedule.esmStartHour], nil],
                [NSArray arrayWithObjects:kCellIdESMEndTime, [NSNumber numberWithLongLong:schedule.esmEndHour], nil],
                nil];
  case kPacoScheduleTypeSelfReport:
    return [NSArray arrayWithObjects:
                [NSArray arrayWithObjects:kCellIdText, kCellIdText, nil],
                nil];
  case kPacoScheduleTypeTesting: // TPE special type only used for iOS Notification testing
    return nil;
  }
  return nil;
}


#pragma mark - PacoTableViewDelegate

- (BOOL)isCellType:(NSString *)cellId reuseId:(NSString *)reuseId {
  NSString *testCellId = [[reuseId componentsSeparatedByString:@":"] objectAtIndex:0];
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
  //disable user to modify any schedule for now
  cell.userInteractionEnabled = NO;
  switch (self.schedule.scheduleType) {
  case kPacoScheduleTypeDaily: {
      if ([self isCellType:kCellIdSignalTimes reuseId:reuseId]) {
        PacoTimeSelectionView *cellView = (PacoTimeSelectionView *)cell;
        cellView.times = [self realRowData:rowData];
        cell.userInteractionEnabled = YES;
      } else {
        assert(0);
      }
    }
    break;
  case kPacoScheduleTypeWeekly: {
      if ([self isCellType:kCellIdRepeat reuseId:reuseId]) {
        PacoRepeatRateSelectionView *cellView = (PacoRepeatRateSelectionView *)cell;
        cellView.repeatStyle = kPacoScheduleRepeatWeeks;
        cellView.repeatNumberValue = [self realRowData:rowData];
      } else if ([self isCellType:kCellIdDaysOfWeek reuseId:reuseId]) {
        PacoDayOfWeekSelectionView *cellView = (PacoDayOfWeekSelectionView *)cell;
        cellView.onlyAllowOneDay = NO;
        cellView.daysOfWeek = [self realRowData:rowData];
      } else if ([self isCellType:kCellIdSignalTimes reuseId:reuseId]) {
        PacoTimeSelectionView *cellView = (PacoTimeSelectionView *)cell;
        cellView.times = [self realRowData:rowData];
      } else {
        assert(0);
      }
    }
    break;
  case kPacoScheduleTypeWeekday: {
      if ([self isCellType:kCellIdSignalTimes reuseId:reuseId]) {
        PacoTimeSelectionView *cellView = (PacoTimeSelectionView *)cell;
        cellView.times = [self realRowData:rowData];
      } else {
        assert(0);
      }
    }
    break;
  case kPacoScheduleTypeMonthly: {
      if ([self isCellType:kCellIdRepeat reuseId:reuseId]) {
        PacoRepeatRateSelectionView *cellView = (PacoRepeatRateSelectionView *)cell;
        cellView.repeatStyle = kPacoScheduleRepeatMonths;
        cellView.repeatNumberValue = [self realRowData:rowData];
      } else if ([self isCellType:kCellIdByDaysOfWeekMonth reuseId:reuseId]) {
        PacoByWeekOrMonthSelectionView *cellView = (PacoByWeekOrMonthSelectionView *)cell;
        cellView.byWeek = [[self realRowData:rowData] boolValue];
      } else if ([self isCellType:kCellIdWhichFirstDayOfMonth reuseId:reuseId]) {
        PacoFirstDayOfMonthSelectionView *cellView = (PacoFirstDayOfMonthSelectionView *)cell;
        cellView.firstDayOfMonth = [self realRowData:rowData];
      } else if ([self isCellType:kCellIdWhichDayOfMonth reuseId:reuseId]) {
        PacoDayOfMonthSelectionView *cellView = (PacoDayOfMonthSelectionView *)cell;
        cellView.dayOfMonth = [self realRowData:rowData];
      } else if ([self isCellType:kCellIdDaysOfWeek reuseId:reuseId]) {
        PacoDayOfWeekSelectionView *cellView = (PacoDayOfWeekSelectionView *)cell;
        cellView.onlyAllowOneDay = YES;
        cellView.daysOfWeek = [self realRowData:rowData];
      } else {
        assert(0);
      }
    }
    break;
  case kPacoScheduleTypeESM: {
      if ([self isCellType:kCellIdESMStartTime reuseId:reuseId]) {
        PacoTimeEditView *cellView = (PacoTimeEditView *)cell;
        cellView.time = [self realRowData:rowData];
        cellView.title = @"Start Time: ";
        cell.userInteractionEnabled = YES;
      } else if([self isCellType:kCellIdESMEndTime reuseId:reuseId]) {
        PacoTimeEditView *cellView = (PacoTimeEditView *)cell;
        cellView.time = [self realRowData:rowData];
        cellView.title = @"End Time: ";
        cell.userInteractionEnabled = YES;
      } else {
        assert(0);
      }
    }
    break;
  case kPacoScheduleTypeSelfReport:
    // do nothing
    if ([self isCellType:kCellIdText reuseId:reuseId]) {
      PacoTableTextCell *cellView = (PacoTableTextCell *)cell;
      cellView.textLabel.text = @"Self scheduled.";
      cellView.detailTextLabel.text = @"Submit responses whenever you wish.";
    }
    break;
  case kPacoScheduleTypeTesting: {
    // special type for testing Notification
    }
  break;
  }

}

- (void)handleUserTap {
  NSString* errorMsg = [self.schedule evaluateSchedule];
  if (errorMsg) {
    [[[UIAlertView alloc] initWithTitle:@"Oops"
                                message:errorMsg
                               delegate:nil
                      cancelButtonTitle:@"OK"
                      otherButtonTitles:nil] show];
  }
  [self.tableView dismissDatePicker];
  
  if (self.tableView.footer == nil) {
    self.tableView.footer = self.joinButton;
    [self.joinButton sizeToFit];
    [self setNeedsLayout];
  }
}


- (void)cellSelected:(UITableViewCell *)cell rowData:(id)rowData reuseId:(NSString *)reuseId {
  // When the time picker is active a cell selection triggers dismissal of the time picker.
  if ([reuseId hasPrefix:kCellIdSignalTimes]) {
    PacoTimeSelectionView *timeSelect = (PacoTimeSelectionView *)cell;
    [timeSelect finishTimeSelection];
  }
  [self handleUserTap];
}

- (void)didReceiveTapButNoCellSelected {
  if ([self.schedule isESMSchedule]) {
    [self handleUserTap];
  }
}

- (void)dataUpdated:(UITableViewCell *)cell rowData:(id)rowData reuseId:(NSString *)reuseId {
NSLog(@"TODO: implement schedule editing hookups");
  switch (self.schedule.scheduleType) {
  case kPacoScheduleTypeDaily: {
      if ([self isCellType:kCellIdSignalTimes reuseId:reuseId]) {
        self.schedule.times = rowData;
      } else {
        assert(0);
      }
    }
    break;
  case kPacoScheduleTypeWeekly: {
      if ([self isCellType:kCellIdRepeat reuseId:reuseId]) {
      } else if ([self isCellType:kCellIdDaysOfWeek reuseId:reuseId]) {
      } else if ([self isCellType:kCellIdSignalTimes reuseId:reuseId]) {
      } else {
        assert(0);
      }
    }
    break;
  case kPacoScheduleTypeWeekday: {
      if ([self isCellType:kCellIdSignalTimes reuseId:reuseId]) {
      } else {
        assert(0);
      }
    }
    break;
  case kPacoScheduleTypeMonthly: {
      if ([self isCellType:kCellIdRepeat reuseId:reuseId]) {
      } else if ([self isCellType:kCellIdByDaysOfWeekMonth reuseId:reuseId]) {
      } else if ([self isCellType:kCellIdWhichFirstDayOfMonth reuseId:reuseId]) {
      } else if ([self isCellType:kCellIdWhichDayOfMonth reuseId:reuseId]) {
      } else if ([self isCellType:kCellIdDaysOfWeek reuseId:reuseId]) {
      } else {
        assert(0);
      }
    }
    break;
  case kPacoScheduleTypeESM: {
      if ([self isCellType:kCellIdESMStartTime reuseId:reuseId]) {
        self.schedule.esmStartHour = [rowData longLongValue];
      } else if ([self isCellType:kCellIdESMEndTime reuseId:reuseId]) {
        self.schedule.esmEndHour = [rowData longLongValue];
      }else {
        assert(0);
      }
    }
    break;
  case kPacoScheduleTypeSelfReport:
    // do nothing
    break;
  case kPacoScheduleTypeTesting: {
    // special type for testing Notification
    }
    break;
  }

}

@end
