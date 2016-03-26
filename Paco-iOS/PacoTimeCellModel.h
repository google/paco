//
//  PacoTimeCellModel.h
//  Paco
//
//  Created by Northrop O'brien on 3/17/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SignalTime.h" 




@class DatePickerCell;


@interface PacoTimeCellModel : NSObject

@property (nonatomic,strong)  NSString * groupName;
@property (readwrite)  long   millisecondsSinceMidnight;
@property (nonatomic,strong)  NSString * timeLabelStr;
@property (nonatomic,strong)  DatePickerCell * datePickerCell;
@property (readwrite)         int   * offsetTime;
@property (nonatomic,strong)  PASignalTime * signalTime;





@end
