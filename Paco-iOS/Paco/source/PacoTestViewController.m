//
//  PacoTestViewController.m
//  Paco
//
//  Created by northropo on 7/17/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#include "ExperimentDAO.h"
#include "ExperimentDAOCore.h"
#include "ExperimentGroup.h"
#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "ListMaker.h"
#include "Validator.h"
#include "java/lang/Boolean.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/List.h"
#import "PacoTestViewController.h"
#import "ExperimentDAO.h"


#include "ActionScheduleGenerator.h"
#include "ActionSpecification.h"
#include "ActionTrigger.h"
#include "DateMidnight.h"
#include "DateTime.h"
#include "EsmGenerator2.h"
#include "EsmSignalStore.h"
#include "EventStore.h"
#include "ExperimentDAO.h"
#include "ExperimentGroup.h"
#include "Interval.h"
#include "J2ObjC_source.h"
#include "NonESMSignalGenerator.h"
#include "PacoAction.h"
#include "PacoNotificationAction.h"
#include "Schedule.h"
#include "ScheduleTrigger.h"
#include "SignalTime.h"
#include "TimeUtil.h"
#include "java/lang/Boolean.h"
#include "java/lang/IllegalStateException.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/util/ArrayList.h"
#include "java/util/Collections.h"
#include "java/util/List.h"
#include "java/util/List.h"
#include "org/joda/time/Hours.h"
#include "org/joda/time/Duration.h"
#include "EsmGenerator2.h"


@interface PacoTestViewController ()

@end

@implementation PacoTestViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
}


- (IBAction)testOne:(id)sender
{
    
    OrgJodaTimeDateTime *startDate = [[OrgJodaTimeDateTime alloc] initWithInt:2010 withInt:12 withInt:20 withInt:0 withInt:0 withInt:0 withInt:0];
    
   
  
    
    OrgJodaTimeHours * hours        = [OrgJodaTimeHours hoursWithInt:17];
    OrgJodaTimeDuration * duration  = [hours toStandardDuration];
    long endHourMIlls               = [duration  getStandardSeconds]*1000;
    
    
    OrgJodaTimeHours *      startHours = [OrgJodaTimeHours hoursWithInt:9];
    OrgJodaTimeDuration *   startDuration = [startHours toStandardDuration];
    long                    startHourMIlls = [startDuration  getStandardSeconds]*1000;
    
    
    int     esmFrequency = 1;
    int     esmPeriod = 0;
    BOOL    esmWeekends = false;
    
    
    
    PASchedule* schedule  = [[PASchedule alloc] initWithJavaLangInteger:[JavaLangInteger valueOfWithInt:0]
                                                    withJavaLangBoolean: [JavaLangBoolean valueOfWithBoolean:NO]
                                                    withJavaLangInteger:nil
                                                       withJavaLangLong:[JavaLangLong valueOfWithLong:endHourMIlls]
                                                    withJavaLangInteger:[JavaLangInteger valueOfWithInt:esmFrequency]
                                                    withJavaLangInteger:[JavaLangInteger valueOfWithInt:esmPeriod]
                                                       withJavaLangLong:[JavaLangLong  valueOfWithLong:startHourMIlls]
                                                    withJavaLangInteger:nil
                                                    withJavaLangInteger:nil
                                                       withJavaUtilList:nil
                                                    withJavaLangInteger:[JavaLangInteger     valueOfWithInt:2 ]
                                                    withJavaLangBoolean:[JavaLangBoolean valueOfWithBoolean:esmWeekends]
                                                    withJavaLangInteger:nil
                                                    withJavaLangInteger:[JavaLangInteger valueOfWithInt:59]
                                                    withJavaLangInteger:nil
                                                    withJavaLangInteger:nil] ;
    
    PAEsmGenerator2* generator = [[PAEsmGenerator2 alloc] init];
    
    
    id<JavaUtilList>  list =  [generator generateForScheduleWithOrgJodaTimeDateTime:startDate  withPASchedule:schedule];
    int i =  [list size];
    
    
    NSLog(@" this is the last one %i",i);
    
  /*
- (instancetype)initWithJavaLangInteger:(JavaLangInteger *)scheduleType
withJavaLangBoolean:(JavaLangBoolean *)byDayOfMonth
withJavaLangInteger:(JavaLangInteger *)dayOfMonth
withJavaLangLong:(JavaLangLong *)esmEndHour
withJavaLangInteger:(JavaLangInteger *)esmFrequency
withJavaLangInteger:(JavaLangInteger *)esmPeriodInDays
withJavaLangLong:(JavaLangLong *)esmStartHour
withJavaLangInteger:(JavaLangInteger *)nthOfMonth
withJavaLangInteger:(JavaLangInteger *)repeatRate
withJavaUtilList:(id<JavaUtilList>)times
withJavaLangInteger:(JavaLangInteger *)weekDaysScheduled
withJavaLangBoolean:(JavaLangBoolean *)esmWeekends
withJavaLangInteger:(JavaLangInteger *)timeout
withJavaLangInteger:(JavaLangInteger *)minimumBuffer
withJavaLangInteger:(JavaLangInteger *)snoozeCount
withJavaLangInteger:(JavaLangInteger *)snoozeTime;
   */
    
    
    
    
    
   // long endHourMillis = Hours.hours(17).toStandardDuration().getMillis();

    
    
}




- (IBAction)btnPressed:(id)sender
{
    NSLog(@"Btn Pressed");
    
    
  /*
    - (instancetype)initWithJavaLangLong:(JavaLangLong *)id_
withNSString:(NSString *)title
withNSString:(NSString *)description_
withNSString:(NSString *)informedConsentForm
withNSString:(NSString *)email
withNSString:(NSString *)joinDate
withNSString:(NSString *)modifyDate
withJavaLangBoolean:(JavaLangBoolean *)published
withJavaUtilList:(id<JavaUtilList>)admins
withJavaUtilList:(id<JavaUtilList>)publishedUsers
withJavaLangBoolean:(JavaLangBoolean *)deleted
withJavaLangInteger:(JavaLangInteger *)version_
withJavaLangBoolean:(JavaLangBoolean *)recordPhoneDetails
withJavaUtilList:(id<JavaUtilList>)groups
withJavaUtilList:(id<JavaUtilList>)extraDataDeclarations;
   
   */
    
    
    
    
    PAExperimentDAO* dao = [[PAExperimentDAO alloc] initWithJavaLangLong:[[JavaLangLong alloc  ]initWithLong:4]
                                                                         withNSString:@"hello" withNSString:@"hello" withNSString:@"hello" withNSString:@"hello" withNSString:@"hello" withJavaLangBoolean:0 withJavaLangBoolean:0 withJavaUtilList:nil  withNSString:@"hello"  withNSString:@"hello" withNSString:@"hello" withJavaUtilDate:[NSDate new ] withJavaUtilDate:[NSDate new]];
    
    
  
    
    
    
    
    /*
     
     - (instancetype)initWithJavaLangInteger:(JavaLangInteger *)scheduleType
     withJavaLangBoolean:(JavaLangBoolean *)byDayOfMonth
     withJavaLangInteger:(JavaLangInteger *)dayOfMonth
     withJavaLangLong:(JavaLangLong *)esmEndHour
     withJavaLangInteger:(JavaLangInteger *)esmFrequency
     withJavaLangInteger:(JavaLangInteger *)esmPeriodInDays
     withJavaLangLong:(JavaLangLong *)esmStartHour
     withJavaLangInteger:(JavaLangInteger *)nthOfMonth
     withJavaLangInteger:(JavaLangInteger *)repeatRate
     withJavaUtilList:(id<JavaUtilList>)times
     withJavaLangInteger:(JavaLangInteger *)weekDaysScheduled
     withJavaLangBoolean:(JavaLangBoolean *)esmWeekends
     withJavaLangInteger:(JavaLangInteger *)timeout
     withJavaLangInteger:(JavaLangInteger *)minimumBuffer
     withJavaLangInteger:(JavaLangInteger *)snoozeCount
     withJavaLangInteger:(JavaLangInteger *)snoozeTime;
     
     */
    
    
    JavaUtilArrayList* mylist = [[JavaUtilArrayList alloc] init];
    
    NSDateComponents *today = [[NSCalendar currentCalendar] components: NSCalendarUnitYear | NSCalendarUnitMonth | NSCalendarUnitDay | NSCalendarUnitHour |  NSCalendarUnitMinute|NSCalendarUnitMinute  fromDate:[NSDate date]];
    
    OrgJodaTimeDateTime*  time =  [[OrgJodaTimeDateTime alloc] initWithInt:[today year]   withInt:[today month] withInt:[today day]  withInt:[today hour] withInt:[today minute]  withInt:2];
    
    [mylist addWithId:time];
    
    
    
    
    
    
    
    
    
    PASchedule* schedule  = [[PASchedule alloc] initWithJavaLangInteger:[JavaLangInteger valueOfWithInt:0]
                                                  withJavaLangBoolean: [JavaLangBoolean valueOfWithBoolean:NO]
                                                  withJavaLangInteger:[JavaLangInteger valueOfWithInt:2]
                                                 withJavaLangLong:[JavaLangLong valueOfWithLong:2 ]
                                                  withJavaLangInteger:[JavaLangInteger valueOfWithInt:2]
                                                  withJavaLangInteger:[JavaLangInteger valueOfWithInt:1]
                                                 withJavaLangLong:[JavaLangLong  valueOfWithLong:1]
                                                  withJavaLangInteger:[JavaLangInteger valueOfWithInt:1]
                                                  withJavaLangInteger:[JavaLangInteger valueOfWithInt:1]
                                                  withJavaUtilList:mylist
                                                  withJavaLangInteger:[JavaLangInteger     valueOfWithInt:2 ]
                                                  withJavaLangBoolean:[JavaLangBoolean valueOfWithBoolean:YES]
                                                  withJavaLangInteger:[JavaLangInteger valueOfWithInt:1]
                                                  withJavaLangInteger:[JavaLangInteger valueOfWithInt:10]
                                                  withJavaLangInteger:[JavaLangInteger valueOfWithInt:1]
                                                  withJavaLangInteger:[JavaLangInteger valueOfWithInt:1]] ;
    
    
    
    
    
    
    
  
    
    
    /* (instancetype)initWithInt:(jint)year
withInt:(jint)monthOfYear
withInt:(jint)dayOfMonth
withInt:(jint)hourOfDay
withInt:(jint)minuteOfHour
withInt:(jint)secondOfMinute
    */
    

 
    
    PAEsmGenerator2* generator = [[PAEsmGenerator2 alloc] init];
    
    
   id<JavaUtilList>  list =  [generator generateForScheduleWithOrgJodaTimeDateTime:time  withPASchedule:schedule];
     
    
    
    NSLog(@" this one ");
    
    
    
   // NSLog(@"Btn Pressed %i,%i,%i ",dayOfMonth, scheduleType ,byDayOfMonth );
    
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
