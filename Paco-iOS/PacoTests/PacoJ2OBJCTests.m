//
//  PacoJ2OBJCTests.m
//  Paco
//
//  Created by northropo on 7/20/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>


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

@interface PacoJ2OBJCTests : XCTestCase

@end

@implementation PacoJ2OBJCTests

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

-(void) testRemanemMethod
{
  
  //  PAExperimentGroup   * eGroup = [[PAExperimentGroup alloc] init];
    
    
   
    
}


-(void) testDayIncrement
{
    
    OrgJodaTimeDateTime *startDate = [[OrgJodaTimeDateTime alloc] initWithInt:2012 withInt:3 withInt:23 withInt:0 withInt:0 withInt:0 withInt:0];
 
    OrgJodaTimeDateTime * newTime  = [startDate plusDaysWithInt:1];
    
    long mills = [startDate getMillis];
    long mills2 = [newTime getMillis];
    
    NSLog(@"mills %li", mills2-mills);
    
    
    
}

- (void) test1xPerDay
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
    
    
    
     PASchedule* schedule  = [self setupSchedul:startDate EndHoursMill:endHourMIlls StartHourMill:startHourMIlls ESMFrequencey:esmFrequency EMSPeriod:esmPeriod ESMWeekends:esmWeekends];
    
  
    
    PAEsmGenerator2* generator = [[PAEsmGenerator2 alloc] init];
    id<JavaUtilList>  list =  [generator generateForScheduleWithOrgJodaTimeDateTime:startDate  withPASchedule:schedule];
    int i =  [list size];
    
    XCTAssert(i==1, @"Pass");
}


-(void) test1xPerDayWeekendDayFails
{
    
    OrgJodaTimeDateTime *startDate = [[OrgJodaTimeDateTime alloc] initWithInt:2010 withInt:12 withInt:19 withInt:0 withInt:0 withInt:0 withInt:0];
    
    
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
    
    XCTAssert(0==i, @"Pass");
    
    
    
    
    
}


-(void) testEsmDailyNoWeekendFailsToDoNextWeek
{
    
    OrgJodaTimeDateTime *startDate = [[OrgJodaTimeDateTime alloc] initWithInt:2012 withInt:3 withInt:23 withInt:0 withInt:0 withInt:0 withInt:0];
    OrgJodaTimeHours * hours        = [OrgJodaTimeHours hoursWithInt:17];
    OrgJodaTimeDuration * duration  = [hours toStandardDuration];
    long endHourMIlls               = [duration  getStandardSeconds]*1000;
    
    OrgJodaTimeHours *      startHours = [OrgJodaTimeHours hoursWithInt:9];
    OrgJodaTimeDuration *   startDuration = [startHours toStandardDuration];
    long                    startHourMIlls = [startDuration  getStandardSeconds]*1000;
    
 
    
    
    int     esmFrequency = 8;
    int     esmPeriod = 0;
    BOOL    esmWeekends = false;
    
    PASchedule* schedule  =     [self setupSchedul:startDate EndHoursMill:endHourMIlls StartHourMill:startHourMIlls ESMFrequencey:esmFrequency EMSPeriod:esmPeriod ESMWeekends:esmWeekends];
    
    PAEsmGenerator2* generator = [[PAEsmGenerator2 alloc] init];
    id<JavaUtilList>  list =  [generator generateForScheduleWithOrgJodaTimeDateTime:startDate  withPASchedule:schedule];
    int i =  [list size];
    
    XCTAssert(8==i, @"Pass");
 
    OrgJodaTimeDateTime * nextPeriod  = [startDate plusDaysWithInt:[schedule convertEsmPeriodToDays]];
    list =  [generator generateForScheduleWithOrgJodaTimeDateTime:nextPeriod  withPASchedule:schedule];
    BOOL isWeekend = [PATimeUtil isWeekendWithOrgJodaTimeDateTime:nextPeriod];
    
    XCTAssert(isWeekend, @"Pass");
    XCTAssert(0==[list size], @"Pass");
    
    
     if([schedule convertEsmPeriodToDays] ==1 && ![schedule getEsmWeekends].booleanValue && [PATimeUtil isWeekendWithOrgJodaTimeDateTime:nextPeriod])
     {
        nextPeriod = [PATimeUtil skipWeekendsWithOrgJodaTimeDateTime:nextPeriod];
    }
     isWeekend = [PATimeUtil isWeekendWithOrgJodaTimeDateTime:nextPeriod];
     XCTAssert(!isWeekend, @"Pass");
     list =  [generator generateForScheduleWithOrgJodaTimeDateTime:nextPeriod  withPASchedule:schedule];
    i = [list size];
     XCTAssert(8==i, @"Pass");
}


-(void) testEsmWeeklyNoWeekendFailsToDoNextWeek
{
    OrgJodaTimeDateTime *startDate = [[OrgJodaTimeDateTime alloc] initWithInt:2012 withInt:3 withInt:23 withInt:0 withInt:0 withInt:0 withInt:0];
   
    OrgJodaTimeHours * hours        = [OrgJodaTimeHours hoursWithInt:17];
    OrgJodaTimeDuration * duration  = [hours toStandardDuration];
    long endHourMIlls               = [duration  getStandardSeconds]*1000;
    
    OrgJodaTimeHours *      startHours = [OrgJodaTimeHours hoursWithInt:9];
    OrgJodaTimeDuration *   startDuration = [startHours toStandardDuration];
    long                    startHourMIlls = [startDuration  getStandardSeconds]*1000;
    
    
    int     esmFrequency = 8;
    int     esmPeriod = 1;
    BOOL    esmWeekends = false;
    
    PASchedule* schedule  = [self setupSchedul:startDate EndHoursMill:endHourMIlls StartHourMill:startHourMIlls ESMFrequencey:esmFrequency EMSPeriod:esmPeriod ESMWeekends:esmWeekends];
    
    PAEsmGenerator2* generator = [[PAEsmGenerator2 alloc] init];
    id<JavaUtilList>  list =  [generator generateForScheduleWithOrgJodaTimeDateTime:startDate  withPASchedule:schedule];
    int i =  [list size];
    XCTAssert(i==8, @"Pass");

    OrgJodaTimeDateTime * nextPeriod  = [startDate plusDaysWithInt:[schedule convertEsmPeriodToDays]];
    list =  [generator generateForScheduleWithOrgJodaTimeDateTime:nextPeriod  withPASchedule:schedule];
    i =  [list size];
    
    
    BOOL isWeekend = [PATimeUtil isWeekendWithOrgJodaTimeDateTime:nextPeriod];
    XCTAssert(!isWeekend, @"Pass");
    XCTAssert(8==i, @"Pass");
    
    
}


-(void) test2xPerDay
{
    
    
    OrgJodaTimeDateTime *startDate = [[OrgJodaTimeDateTime alloc] initWithInt:2010 withInt:12 withInt:20 withInt:0 withInt:0 withInt:0 withInt:0];
    
    OrgJodaTimeHours * hours        = [OrgJodaTimeHours hoursWithInt:17];
    OrgJodaTimeDuration * duration  = [hours toStandardDuration];
    long endHourMIlls               = [duration  getStandardSeconds]*1000;
    
    OrgJodaTimeHours *      startHours = [OrgJodaTimeHours hoursWithInt:9];
    OrgJodaTimeDuration *   startDuration = [startHours toStandardDuration];
    long                    startHourMIlls = [startDuration  getStandardSeconds]*1000;
    
    
    int     esmFrequency =2;
    int     esmPeriod = 0;
    BOOL    esmWeekends = false;
    
    PASchedule* schedule  = [self setupSchedul:startDate EndHoursMill:endHourMIlls StartHourMill:startHourMIlls ESMFrequencey:esmFrequency EMSPeriod:esmPeriod ESMWeekends:esmWeekends];
    
    PAEsmGenerator2* generator = [[PAEsmGenerator2 alloc] init];
    id<JavaUtilList>  list =  [generator generateForScheduleWithOrgJodaTimeDateTime:startDate  withPASchedule:schedule];
    int  i =  [list size];
    XCTAssert(2==i, @"Pass");
    
}


-(void) test8xPerDay
{
    
    
    OrgJodaTimeDateTime *startDate = [[OrgJodaTimeDateTime alloc] initWithInt:2010 withInt:12 withInt:20 withInt:0 withInt:0 withInt:0 withInt:0];
    
    OrgJodaTimeHours * hours        = [OrgJodaTimeHours hoursWithInt:17];
    OrgJodaTimeDuration * duration  = [hours toStandardDuration];
    long endHourMIlls               = [duration  getStandardSeconds]*1000;
    
    OrgJodaTimeHours *      startHours = [OrgJodaTimeHours hoursWithInt:9];
    OrgJodaTimeDuration *   startDuration = [startHours toStandardDuration];
    long                    startHourMIlls = [startDuration  getStandardSeconds]*1000;
    
    
    int     esmFrequency =8;
    int     esmPeriod = 0;
    BOOL    esmWeekends = false;
    
    PASchedule* schedule  = [self setupSchedul:startDate EndHoursMill:endHourMIlls StartHourMill:startHourMIlls ESMFrequencey:esmFrequency EMSPeriod:esmPeriod ESMWeekends:esmWeekends];
    
    PAEsmGenerator2* generator = [[PAEsmGenerator2 alloc] init];
    id<JavaUtilList>  list =  [generator generateForScheduleWithOrgJodaTimeDateTime:startDate  withPASchedule:schedule];
    int  i =  [list size];
    XCTAssert(8==i, @"Pass");
    
}











-(void) test1xPerWeekNoWeekends
{
    OrgJodaTimeDateTime *startDate = [[OrgJodaTimeDateTime alloc] initWithInt:2010 withInt:12 withInt:20 withInt:0 withInt:0 withInt:0 withInt:0];
    OrgJodaTimeHours * hours        = [OrgJodaTimeHours hoursWithInt:17];
    OrgJodaTimeDuration * duration  = [hours toStandardDuration];
    long endHourMIlls               = [duration  getStandardSeconds]*1000;
    
    OrgJodaTimeHours *      startHours = [OrgJodaTimeHours hoursWithInt:9];
    OrgJodaTimeDuration *   startDuration = [startHours toStandardDuration];
    long                    startHourMIlls = [startDuration  getStandardSeconds]*1000;
    
    
    int     esmFrequency =1;
    int     esmPeriod = 1;
    BOOL    esmWeekends = false;
    
    PASchedule* schedule  = [self setupSchedul:startDate EndHoursMill:endHourMIlls StartHourMill:startHourMIlls ESMFrequencey:esmFrequency EMSPeriod:esmPeriod ESMWeekends:esmWeekends];
    
    PAEsmGenerator2* generator = [[PAEsmGenerator2 alloc] init];
    id<JavaUtilList>  list =  [generator generateForScheduleWithOrgJodaTimeDateTime:startDate  withPASchedule:schedule];
    int  i =  [list size];
    XCTAssert(1==i, @"Pass");
    
    
}



-(void) test2xPerWeekNoWeekends
{
    
    OrgJodaTimeDateTime *startDate = [[OrgJodaTimeDateTime alloc] initWithInt:2010 withInt:12 withInt:20 withInt:0 withInt:0 withInt:0 withInt:0];
    OrgJodaTimeHours * hours        = [OrgJodaTimeHours hoursWithInt:17];
    OrgJodaTimeDuration * duration  = [hours toStandardDuration];
    long endHourMIlls               = [duration  getStandardSeconds]*1000;
    
    OrgJodaTimeHours *      startHours = [OrgJodaTimeHours hoursWithInt:9];
    OrgJodaTimeDuration *   startDuration = [startHours toStandardDuration];
    long                    startHourMIlls = [startDuration  getStandardSeconds]*1000;
    
    
    int     esmFrequency =2;
    int     esmPeriod = 1;
    BOOL    esmWeekends = false;
    
    PASchedule* schedule  = [self setupSchedul:startDate EndHoursMill:endHourMIlls StartHourMill:startHourMIlls ESMFrequencey:esmFrequency EMSPeriod:esmPeriod ESMWeekends:esmWeekends];
    
    PAEsmGenerator2* generator = [[PAEsmGenerator2 alloc] init];
    id<JavaUtilList>  list =  [generator generateForScheduleWithOrgJodaTimeDateTime:startDate  withPASchedule:schedule];
    int  i =  [list size];
    XCTAssert(2==i, @"Pass");
    
    
    
}



-(void) test23xPerMonthNoWeekends
{
    
    OrgJodaTimeDateTime *startDate = [[OrgJodaTimeDateTime alloc] initWithInt:2010 withInt:12 withInt:1 withInt:0 withInt:0 withInt:0 withInt:0];
    OrgJodaTimeHours * hours        = [OrgJodaTimeHours hoursWithInt:17];
    OrgJodaTimeDuration * duration  = [hours toStandardDuration];
    long endHourMIlls               = [duration  getStandardSeconds]*1000;
    
    OrgJodaTimeHours *      startHours = [OrgJodaTimeHours hoursWithInt:9];
    OrgJodaTimeDuration *   startDuration = [startHours toStandardDuration];
    long                    startHourMIlls = [startDuration  getStandardSeconds]*1000;
    
    
    int     esmFrequency =23;
    int     esmPeriod = 2;
    BOOL    esmWeekends = false;
    
    PASchedule* schedule  = [self setupSchedul:startDate EndHoursMill:endHourMIlls StartHourMill:startHourMIlls ESMFrequencey:esmFrequency EMSPeriod:esmPeriod ESMWeekends:esmWeekends];
    
    PAEsmGenerator2* generator = [[PAEsmGenerator2 alloc] init];
    id<JavaUtilList>  list =  [generator generateForScheduleWithOrgJodaTimeDateTime:startDate  withPASchedule:schedule];
    int  i =  [list size];
    XCTAssert(23==i, @"Pass");
    
    
}


-(PASchedule* ) setupSchedul:(OrgJodaTimeDateTime*) startDate
                        EndHoursMill:(long)endHoursMill
                        StartHourMill:(long) startHourMill
       ESMFrequencey:(int) esmFrequency
           EMSPeriod:(int) esmPeriod
         ESMWeekends:(bool) esmWeekends
{
    
    PASchedule* schedule  = [[PASchedule alloc] initWithJavaLangInteger:[JavaLangInteger valueOfWithInt:0]
                                                    withJavaLangBoolean: [JavaLangBoolean valueOfWithBoolean:NO]
                                                    withJavaLangInteger:nil
                                                       withJavaLangLong:[JavaLangLong valueOfWithLong:endHoursMill]
                                                    withJavaLangInteger:[JavaLangInteger valueOfWithInt:esmFrequency]
                                                    withJavaLangInteger:[JavaLangInteger valueOfWithInt:esmPeriod]
                                                       withJavaLangLong:[JavaLangLong  valueOfWithLong:startHourMill]
                                                    withJavaLangInteger:nil
                                                    withJavaLangInteger:nil
                                                       withJavaUtilList:nil
                                                    withJavaLangInteger:[JavaLangInteger     valueOfWithInt:2 ]
                                                    withJavaLangBoolean:[JavaLangBoolean valueOfWithBoolean:esmWeekends]
                                                    withJavaLangInteger:nil
                                                    withJavaLangInteger:[JavaLangInteger valueOfWithInt:59]
                                                    withJavaLangInteger:nil
                                                    withJavaLangInteger:nil] ;
    
    return schedule;
    
}





//(PASchedule*) getScheduleWith:(OrgJodaTimeDateTime*) startDAte



@end
