//
//  PacoNotificationTester.m
//  Paco
//
//  Created by Northrop O'brien on 4/14/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "PacoNotificationTester.h"
#import "PacoAppDelegate.h" 
#import "UILocalNotification+PacoExteded.h"
#import "PacoAppDelegate.h" 
#import "TestUtil.h"
#import "PacoMediator.h"
#import "SignalTime.h" 
#import "ExperimentGroup.h" 
#import "ScheduleTrigger.h"
#import "SchedulePrinter.h" 
#import "Schedule.h" 
#import "PAExperimentDAO+Helper.h"


@interface PacoNotificationTester ()

@end

@implementation PacoNotificationTester




static NSString *dataSource = @"{\r\n  \"title\": \"my new experiment\",\r\n  \"creator\": \"testingpacotoday@gmail.com\",\r\n  \"contactEmail\": \"testingpacotoday@gmail.com\",\r\n  \"id\": 5866608721395712,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2016\/05\/09\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"testingpacotoday@gmail.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 13,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"GroupAd\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1457994166570,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 0,\r\n              \"color\": 0,\r\n              \"dismissible\": true,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1457994166569,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 61200000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"start layer\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 64800000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"hidden layer\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 75600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"hidden layer II\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 79200000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Telos\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1457994166571,\r\n              \"onlyEditableOnJoin\": true,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [\r\n        {\r\n          \"name\": \"input1\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"this is my question\",\r\n          \"likertSteps\": 5,\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"input2\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"list\",\r\n          \"text\": \"this is my list\",\r\n          \"likertSteps\": 5,\r\n          \"listChoices\": [\r\n            \"choice one\",\r\n            \"choice thriee\"\r\n          ],\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        }\r\n      ],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>\\nNo need to do anything else for now.<br\/><br\/>\\nPaco will send you a notification when it is time to participate.<br\/><br\/>\\nBe sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";





- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
}


-(void) startRunningExperiments
{
    PAExperimentDAO* dao =  [TestUtil buildExperiment:dataSource];
   [[PacoMediator sharedInstance] clearRunningExperiments];
   [[PacoMediator sharedInstance] addExperimentToAvailableStore:dao];
   [[PacoMediator sharedInstance] startRunningExperiment:[dao instanceId]];


}


-(void) setSignalTimes:(PAExperimentDAO *) experiment
{
    NSNumber   * numberOfGroups    = [experiment valueForKeyPathEx:@"groups#"];
    int count = [numberOfGroups intValue];
    
    NSMutableArray* signalTimes = [[NSMutableArray alloc] init];
    
    for( int i =0;  i < count; i++)
    {
        
        
        
        NSString* str = [NSString stringWithFormat: @"groups[%i]",i ];
        PAExperimentGroup*  group  =  [experiment valueForKeyPathEx:str];
        
        
        NSNumber*  numberOfActionTriggers =
        [group  valueForKeyEx:@"actionTriggers#"];
        int actionTriggerCount = [numberOfActionTriggers intValue];
        
        for(int ii =0; ii < actionTriggerCount; ii++)
        {
            
            NSString* str = [NSString stringWithFormat: @"actionTriggers[%i]",ii ];
            PAScheduleTrigger  *trigger = [group valueForKeyEx:str];
            
            
            
            NSNumber* numberOfSchedules = [trigger  valueForKeyEx:@"schedules#"];
            int schedulesCount = [numberOfSchedules intValue];
            
            for(int iii=0; iii < schedulesCount; iii++)
            {
                NSString* str = [NSString stringWithFormat: @"schedules[%i]",iii ];
                PASchedule * schedule  = [trigger valueForKeyEx:str];
                
                NSNumber*  numberOfActionTriggers =
                [schedule valueForKeyEx:@"signalTimes#"];
                
                int signalTimesCount = [numberOfActionTriggers intValue];
                
             
                NSDate * date = [NSDate date];
                int count=0;
                int plus_seconds = 10;
               
                
                
                for( int iiii =0;  iiii< signalTimesCount; iiii++)
                {
                    
                    long  milliseconds   = [NSDate  millisecondsSinceMidnight:[NSDate date]];
                    
                    
                    
                  //  let milliseconds:Int   = NSDate.millisecondsSinceMidnight(datePicker.date) as Int
                  //  let millsSinceMidnight:JavaLangInteger  = JavaLangInteger(int :  jint(milliseconds) )
                    
                    
                   // self .signalTime?.setFixedTimeMillisFromMidnightWithJavaLangInteger(millsSinceMidnight)
                    
                    
                    long  millisecondsSinceMidnight  = [NSDate millisecondsSinceMidnight:[NSDate new]];
                    
                    JavaLangInteger * distantce = [JavaLangInteger valueOfWithInt:(int) millisecondsSinceMidnight+ count * 10000];
                    
                    //self .signalTime?.setFixedTimeMillisFromMidnightWithJavaLangInteger(millsSinceMidnight)
                    
                    
                    
                    
                     NSString* str = [NSString stringWithFormat: @"signalTimes[%i]",iiii ];
                    PASignalTime * signalTime =  [schedule valueForKeyEx:str];
                    
                    int seconds = [self secondsSinceMidnight];
                    
                    
                    int adjustedSeceondsSinceMidnight = seconds + count*plus_seconds;
                    count++;
                    
                    [signalTime setFixedTimeMillisFromMidnightWithJavaLangInteger:distantce];
                    
                    
                }
   
            }
 
        }
        
    }
    
    
  
    
}

-(int) secondsSinceMidnight
{
    
 

    NSDate * date = [NSDate date];
    
    NSCalendar *gregorian = [[NSCalendar alloc]
                             initWithCalendarIdentifier:NSGregorianCalendar];
    unsigned unitFlags =  NSHourCalendarUnit | NSMinuteCalendarUnit | NSSecondCalendarUnit;
    NSDateComponents *components = [gregorian components:unitFlags fromDate:date];
    
    return    (60 * [components hour] + [components minute])* 60 + [components second]  ;
}


- (IBAction)test2:(id)sender {
    
      PacoAppDelegate*  appDelegate  = (PacoAppDelegate *) [UIApplication sharedApplication].delegate;
    
    
     PAExperimentDAO * dao =  [TestUtil buildExperiment:dataSource];
    
    NSLog(@" %@", dao);
    
    
     [self setSignalTimes:dao];
    
    
      NSLog(@" %@", dao);
    
     [[PacoMediator sharedInstance] clearRunningExperimentsSynchronous];
     [[PacoMediator sharedInstance] addExperimentToAvailableStore:dao];
    
    [NSThread sleepForTimeInterval:3];
    
     [[PacoMediator sharedInstance] startRunningExperimentRegenerate:[dao instanceId]];
   

 
    /*
    
    NSDate *fireDate = [NSDate dateWithTimeInterval:0.1 sinceDate:[NSDate new]];
    //NSDate *timeOutDate  = [NSDate date :120.0 sinceDate:[NSDate new]];
    
    NSDate *timeoutDate  = [[NSDate new] dateByAddingTimeInterval: 60.0];
    
    
      UILocalNotification* notification =   [UILocalNotification pacoNotificationWithExperimentId:@"6139552436584448" experimentTitle:@"Test Experiment"   fireDate:[NSDate new] timeOutDate:timeoutDate groupId:@"987654321"  groupName:@"Group One"  triggerId:@"triggerId"  notificationActionId:@"1460580085119"   actionTriggerSpecId:@"1460580085118"];
    
    
    
    
    [appDelegate showSurveyForNotification:notification];
     */
  
}



- (IBAction)notifcationSent:(id)sender
{
   

    /*
    
    PacoAppDelegate*  appDelegate  = (PacoAppDelegate *) [UIApplication sharedApplication].delegate;
    
    NSDate *fireDate = [NSDate dateWithTimeInterval:0.1 sinceDate:[NSDate new]];
    //NSDate *timeOutDate  = [NSDate date :120.0 sinceDate:[NSDate new]];
    
    NSDate *timeoutDate  = [[NSDate new] dateByAddingTimeInterval: 60.0];
    
    UILocalNotification* notification =   [UILocalNotification pacoNotificationWithExperimentId:@"5955090469879808" experimentTitle:@"Test Experiment"   fireDate:[NSDate new] timeOutDate:timeoutDate groupId:@"987654321"  groupName:@"Group One"  triggerId:@"triggerId"  notificationActionId:@"1460580085119"   actionTriggerSpecId:@"1460580085118"];
  
    NSLog(@"notfication sent");
     
     */
  
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
