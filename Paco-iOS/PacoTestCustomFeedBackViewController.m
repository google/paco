
//
//  PacoTestCustomFeedBackViewController.m
//  Paco
//
//  Created by Northrop O'brien on 7/10/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "PacoTestCustomFeedBackViewController.h"
#import "TestUtil.h"
#import "ExperimentDAO.h" 
#import "PacoMediator.h" 
#import "PacoFeedbackWebViewController.h"
#import "ExperimentGroup.h"
#import "NSObject+J2objcKVO.h"



static NSString* dataSource = @"{\r\n  \"title\": \"Sneezy 3\",\r\n  \"description\": \"Keep track of what foods make me sneeze after I eat them.\",\r\n  \"creator\": \"rbe5000@gmail.com\",\r\n  \"contactEmail\": \"rbe5000@gmail.com\",\r\n  \"id\": 6455693512015872,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2016\/06\/30\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"rbe5000@gmail.com\",\r\n    \"testingpacotoday@gmail.com\",\r\n    \"northropo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 2,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"default\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": false,\r\n      \"logActions\": false,\r\n      \"logShutdown\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 1640220000,\r\n              \"timeout\": 59,\r\n              \"delay\": 5000,\r\n              \"color\": 0,\r\n              \"dismissible\": true,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 27337,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 34200000,\r\n                  \"basis\": 0,\r\n                  \"offsetTimeMillis\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 46800000,\r\n                  \"basis\": 0,\r\n                  \"offsetTimeMillis\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 72000000,\r\n                  \"basis\": 0,\r\n                  \"offsetTimeMillis\": 0,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": true,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [\r\n        {\r\n          \"name\": \"Sneeze\",\r\n          \"required\": true,\r\n          \"conditional\": false,\r\n          \"responseType\": \"likert\",\r\n          \"text\": \"Did you sneeze after your \\\"last\\\" meal?\",\r\n          \"likertSteps\": 2,\r\n          \"leftSideLabel\": \"No\",\r\n          \"rightSideLabel\": \"Yes\",\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"Severity\",\r\n          \"required\": false,\r\n          \"conditional\": true,\r\n          \"conditionExpression\": \"Sneeze = 2\",\r\n          \"responseType\": \"list\",\r\n          \"text\": \"How severe was the sneezing?\",\r\n          \"listChoices\": [\r\n            \"Mild\",\r\n            \"Moderate\",\r\n            \"Horrible\"\r\n          ],\r\n          \"multiselect\": false,\r\n          \"numeric\": true,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        },\r\n        {\r\n          \"name\": \"Food\",\r\n          \"required\": false,\r\n          \"conditional\": false,\r\n          \"responseType\": \"open text\",\r\n          \"text\": \"Which foods did you eat?\",\r\n          \"multiselect\": false,\r\n          \"numeric\": false,\r\n          \"invisible\": false,\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Input2\"\r\n        }\r\n      ],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"  <script>\\n  $(function() {\\n\\n     function getData() {\\n        return paco.db.getAllEvents();\\n     };\\n\\n    String.prototype.trim = function() {\\n      return this.replace(\/^\\\\s+|\\\\s+$\/g,\\\"\\\");\\n    }\\n\\n    function dateString(d){\\n      function pad(n){\\n        return n<10 ? \'0\'+n : n;\\n      }\\n\\n      return pad(d.getMonth()+1)+\'\/\'\\n        + pad(d.getDate()) + \'\/\'\\n        + String(d.getFullYear()).slice(2)\\n     }\\n\\n     var expdata = getData();\\n\\n      if (!expdata) {\\n        alert(\\\"No Data\\\");\\n        return;\\n      }\\n    \/\/   expdata = expdata.sort(function(a,b) {\\n\\t\\t\\t\\t\/\/ \\t\\t\\t   return a.responseTime - b.responseTime;\\n\\t\\t\\t\\t\/\/ \\t\\t\\t });\\n\\t  \/\/alert(\\\"expdata = \\\" + JSON.stringify(expdata, null, 2));\\t\\t\\t\\n\\t  var foods = {};\\n\\t  for (var j=0;j < expdata.length;j++) {\\n            var sneezed = false;\\n            var food;\\n            var severity = 1;\\n            for (var i=0;i < expdata[j].responses.length;i++) {\\n              var response = expdata[j].responses[i];\\n              if (response.name == \'Sneeze\' && response.answer == 2) {\\n                sneezed = true;\\n              }\\n              if (response.name == \'Food\') {\\n                food = response.answer.trim();\\n              }\\n              if (response.name == \'Severity\' && response.name != null && response.name.length > 0) {\\n                severity = response.answer;\\n              }\\n            }\\n            if (sneezed) {\\n              foods[food] = (foods[food] || parseInt(0)) + parseInt(severity);\\n            }\\n\\t  }\\n\\t  \/\/alert(\\\"food = \\\" + JSON.stringify(foods, null, 2));\\n\\t  var foodlist = \\\"<h1>Sneezy Foods<\/h1><center><table><tr><th>Food<\/th><th>Severity count<\/th><\/tr>\\\";\\n\\t \\n\\t  for (var f in foods) {\\n\\t    foodlist += \\\"<tr><td>\\\" + f + \\\"<\/td><td>\\\" + foods[f] + \\\"<\/td><\/tr>\\\";\\n\\t  }\\n\\t  foodlist += \\\"<\/table><\/center>\\\";\\n\\t  $(\\\"#placeholder\\\").html(foodlist);\\n    });\\n    \\n<\/script>\\n  <h2>\\n  Thanks for Participating!\\n<\/h2>\\n  <div id=\\\"placeholder\\\" ><\/div>\\n  \\n  <br>\\n\",\r\n        \"type\": 3,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 3,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";

@interface PacoTestCustomFeedBackViewController ()

@end

@implementation PacoTestCustomFeedBackViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (IBAction)customFeedback:(id)sender {
    
     PAExperimentDAO * experiment =  [TestUtil buildExperiment:dataSource];
    [[PacoMediator sharedInstance] replaceAllExperiments:@[experiment]];
    
    PAExperimentGroup* group =  [experiment valueForKeyPathEx:@"groups[0]"];
    
    _feedback  = [PacoFeedbackWebViewController controllerWithExperimentGroup:group  withExperiment:experiment  htmlName:@"skeleton" dismissBlock:^{
        
        NSLog(@"skeleton");
        
    }];

    
    self.navigation = [[UINavigationController alloc] initWithRootViewController:_feedback];
    [self.view addSubview:self.navigation.view];
    
  
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
