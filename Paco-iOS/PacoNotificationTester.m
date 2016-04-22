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




@interface PacoNotificationTester ()

@end

@implementation PacoNotificationTester




static NSString *dataSource = @"{\r\n  \"title\": \"Everything Demo New\",\r\n  \"description\": \"\",\r\n  \"creator\": \"bobevans999@gmail.com\",\r\n  \"contactEmail\": \"bobevans999@gmail.com\",\r\n  \"id\": 6139552436584448,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2016\/04\/21\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"bobevans999@gmail.com\",\r\n    \"rbe5000@gmail.com\",\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 5,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"default\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 1640220000,\r\n              \"timeout\": 59,\r\n              \"delay\": 5000,\r\n              \"color\": 0,\r\n              \"dismissible\": true,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 27337,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 43200000,\r\n                  \"basis\": 0,\r\n                  \"offsetTimeMillis\": 1800000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [\r\n        {\r\n          \"name\": \"smiley\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"likert_smileys\",\r\n          \"text\": \"How happy are you?\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"likert\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"likert\",\r\n          \"text\": \"How frequently do you feel this way?\",\r\n          \"likertSteps\": 5,\r\n          \"leftSideLabel\": \"Never\",\r\n          \"rightSideLabel\": \"Always\",\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"color\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"What is your favorite color?\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"slist\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"list\",\r\n          \"text\": \"Choose one\",\r\n          \"listChoices\": [\r\n            \"Almond\",\r\n            \"Macadamia\",\r\n            \"Pecan\"\r\n          ],\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"multi\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"list\",\r\n          \"text\": \"Choose 1 or more\",\r\n          \"listChoices\": [\r\n            \"Vodka\",\r\n            \"Beer\",\r\n            \"Wine\"\r\n          ],\r\n          \"multiselect\": true,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"num\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"number\",\r\n          \"text\": \"Pick a number\",\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"location\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"location\",\r\n          \"text\": \"\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": true,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"photo\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"photo\",\r\n          \"text\": \"Take a photo\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": true,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"scale_for_conditional\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"likert\",\r\n          \"text\": \"This is a scale. Pick a value > 3 to trigger conditional question.\",\r\n          \"likertSteps\": 5,\r\n          \"leftSideLabel\": \"Not at all\",\r\n          \"rightSideLabel\": \"totally\",\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"conditioned_on_scale\",\r\n          \"required\": false,\r\n          \"conditional\": true,\r\n          \"conditionExpression\": \"scale_for_conditional > 3\",\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"Why did you pick a value greater than 3?\",\r\n          \"likertSteps\": 5,\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        }\r\n      ],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 1,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 1,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";





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


- (IBAction)test2:(id)sender {
    
      PacoAppDelegate*  appDelegate  = (PacoAppDelegate *) [UIApplication sharedApplication].delegate;
    
    
     PAExperimentDAO * dao =  [TestUtil buildExperiment:dataSource];
     [[PacoMediator sharedInstance] addExperimentToAvailableStore:dao];
     [[PacoMediator sharedInstance] startRunningExperiment:[dao instanceId]];
   

 
    
    
    NSDate *fireDate = [NSDate dateWithTimeInterval:0.1 sinceDate:[NSDate new]];
    //NSDate *timeOutDate  = [NSDate date :120.0 sinceDate:[NSDate new]];
    
    NSDate *timeoutDate  = [[NSDate new] dateByAddingTimeInterval: 60.0];
    
    
      UILocalNotification* notification =   [UILocalNotification pacoNotificationWithExperimentId:@"6139552436584448" experimentTitle:@"Test Experiment"   fireDate:[NSDate new] timeOutDate:timeoutDate groupId:@"987654321"  groupName:@"Group One"  triggerId:@"triggerId"  notificationActionId:@"1460580085119"   actionTriggerSpecId:@"1460580085118"];
    
    
    
    
    [appDelegate showSurveyForNotification:notification];
  
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
