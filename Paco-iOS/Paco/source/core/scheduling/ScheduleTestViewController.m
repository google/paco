//
//  ScheduleTestViewController.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/10/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "ScheduleTestViewController.h"
#import "PacoNotificationManager.h"
#import "UILocalNotification+Paco.h"
#import "PAActionSpecification+PacoActionSpecification.h"




//
//  PacoScheduleGeneratorj2ObjC.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/19/15.
//  Copyright (c) 2015 Paco. All rights reserved.

#import <UIKit/UIKit.h>
#import "PacoSerializer.h"
#import "PacoExtendedClient.h"
#import "ActionScheduleGenerator.h"
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
#include "java/util/ArrayList.h"
#include "java/util/Arrays.h"
#include "java/util/List.h"
#import "ExperimentDAO.h"
#import <objc/runtime.h>
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
#include "org/joda/time/Hours.h"
#include "org/joda/time/Duration.h"
#include "EsmGenerator2.h"
#include "PacoSerializeUtil.h"
#import  "PacoSignalStore.h"
#import   "PacoEventStore.h"
#import   "DateTime.h"
#import  "NSObject+J2objcKVO.h"
#import  "OrgJodaTimeDateTime+PacoDateHelper.h"
#import "PacoExtendedClient.h"
#import "UILocalNotification+Paco.h"
#import  "PacoSchedulingUtil.h"
#import "PacoMediator.h"
#import "TempStorage.h"
#import "PacoMediator.h"



  


@interface ScheduleTestViewController ()

@property (nonatomic,strong)   NSMutableDictionary* processing;



@end

@implementation ScheduleTestViewController
{
    PacoNotificationManager*  notificationManager;
}



- (IBAction)AddExperiment:(id)sender
{
    
    @try {
        
        if([self.jsonField.text length] !=0)
        {
            
            NSData* data=  [self.jsonField.text  dataUsingEncoding:NSUTF8StringEncoding];
            PacoSerializer* serializer =
            [[PacoSerializer alloc] initWithArrayOfClasses:nil
                                  withNameOfClassAttribute:@"nameOfClass"];
            JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
            IOSObjectArray * iosArray = [resultArray toArray];
            PAExperimentDAO * dao =  [iosArray objectAtIndex:0];
            
            [[PacoMediator sharedInstance] addExperimentToAvailableStore:dao];
           
            
            NSString* strTitle = [NSString stringWithFormat:@"You have successfully added experiment %@", [dao valueForKeyEx:@"title"]];
            
        
            
            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Success"
                                                            message:strTitle
                                                           delegate:nil
                                                  cancelButtonTitle:@"OK"
                                                  otherButtonTitles:nil];
            [alert show];
            
            
            
            
            
        }
    }
    @catch (NSException *exception) {
        
        NSLog(@" not valid json");
        
    }
    @finally {
        
        [self dismissViewControllerAnimated:YES completion:nil];
        self.jsonField.text= @"";
        
    }

    
}

- (void)viewDidLoad {
    [super viewDidLoad];
    _mutableArray = [NSMutableArray new];
    
}






- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



@end
