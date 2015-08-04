//
//  PacoServiceTests.m
//  Paco
//
//  Created by northropo on 7/22/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "PacoClient.h"
#import "PacoService.h"
#import "PacoModel.h"
#import "PacoExperimentDefinition.h" 

#import "runtime.h" 
#import "PacoSerializer.h"
#import <Foundation/Foundation.h>
#import <objc/runtime.h>
#include "java/util/HashMap.h"
#include "NSObject+ITNDescription.h"



static NSString* smDefinition = @"[{\"title\":\"ESM Demo\",\"description\":\"This experiment demonstrates an ESM (Experiential Sampling Method) study. It will prompt the user to respond to some questions randomly 5 times per day between 10 and 6. The notification to respond will time out in 15 minutes and record a missed signal in that case. The experiment is ongoing, as opposed to a fixed number of days, in duration. It also uses conditional branching to show some questions only when other questions answers take on certain values.\",\"creator\":\"bobevans@google.com\",\"contactEmail\":\"bobevans@google.com\",\"id\":5754435435233280,\"recordPhoneDetails\":false,\"extraDataCollectionDeclarations\":[],\"deleted\":false,\"published\":false,\"admins\":[\"bobevans@google.com\",\"elasticsearch64@gmail.com\"],\"publishedUsers\":[],\"version\":3,\"groups\":[{\"name\":\"New Group\",\"customRendering\":false,\"fixedDuration\":false,\"logActions\":false,\"backgroundListen\":false,\"actionTriggers\":[{\"type\":\"scheduleTrigger\",\"actions\":[{\"actionCode\":1,\"id\":1436903218335,\"type\":\"pacoNotificationAction\",\"snoozeCount\":0,\"snoozeTime\":600000,\"timeout\":15,\"delay\":5000,\"msgText\":\"Time to participate\",\"snoozeTimeInMinutes\":10}],\"id\":1436903218334,\"schedules\":[{\"scheduleType\":4,\"esmFrequency\":5,\"esmPeriodInDays\":0,\"esmStartHour\":36000000,\"esmEndHour\":64800000,\"signalTimes\":[{\"type\":0,\"fixedTimeMillisFromMidnight\":0}],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":true,\"minimumBuffer\":59,\"joinDateMillis\":0,\"id\":1436903218336,\"onlyEditableOnJoin\":false,\"userEditable\":true,\"defaultMinimumBuffer\":59,\"byDayOfWeek\":false}]}],\"inputs\":[{\"name\":\"activity\",\"required\":false,\"conditional\":false,\"responseType\":\"open text\",\"text\":\"What are you doing right now?\",\"multiselect\":false,\"numeric\":false,\"invisible\":false},{\"name\":\"where\",\"required\":false,\"conditional\":false,\"responseType\":\"list\",\"text\":\"Where are you?\",\"listChoices\":[\"Home\",\"Work\",\"Other\"],\"multiselect\":false,\"numeric\":true,\"invisible\":false},{\"name\":\"other_where\",\"required\":false,\"conditional\":true,\"conditionExpression\":\"where == 3\",\"responseType\":\"open text\",\"text\":\"Please enter a name for the place you are\",\"multiselect\":false,\"numeric\":false,\"invisible\":false},{\"name\":\"photo\",\"required\":false,\"conditional\":false,\"responseType\":\"photo\",\"text\":\"Take a photo if you like\",\"multiselect\":false,\"numeric\":false,\"invisible\":true}],\"endOfDayGroup\":false,\"feedback\":{\"text\":\"Thank you for Participating!\",\"type\":0},\"feedbackType\":0}]},{\"title\":\"user present trigger\",\"creator\":\"elasticsearch64@gmail.com\",\"contactEmail\":\"elasticsearch64@gmail.com\",\"id\":5685441885896704,\"recordPhoneDetails\":false,\"extraDataCollectionDeclarations\":[],\"deleted\":false,\"published\":false,\"admins\":[\"elasticsearch64@gmail.com\"],\"publishedUsers\":[],\"version\":2,\"groups\":[{\"name\":\"New Group\",\"customRendering\":false,\"fixedDuration\":false,\"logActions\":false,\"backgroundListen\":false,\"actionTriggers\":[{\"type\":\"interruptTrigger\",\"actions\":[{\"actionCode\":1,\"id\":1437698202506,\"type\":\"pacoNotificationAction\",\"snoozeCount\":0,\"snoozeTime\":600000,\"timeout\":15,\"delay\":5000,\"msgText\":\"Time to participate\",\"snoozeTimeInMinutes\":10}],\"id\":1437698202505,\"cues\":[{\"cueCode\":2}],\"minimumBuffer\":59,\"defaultMinimumBuffer\":15}],\"inputs\":[],\"endOfDayGroup\":false,\"feedback\":{\"text\":\"Thanks for Participating!\",\"type\":0},\"feedbackType\":0}]}]";


static NSString* destDefinition =@"{  \r\n   \"$schema\":\"http://json-schema.org/draft-04/schema#\",\r\n   \"title\":\"Product\",\r\n   \"description\":\"A product from Acme's catalog\",\r\n   \"type\":\"object\",\r\n   \"properties\":{  \r\n      \"id\":{  \r\n         \"description\":\"The unique identifier for a product\",\r\n         \"type\":\"integer\"\r\n      },\r\n      \"name\":{  \r\n         \"description\":\"Name of the product\",\r\n         \"type\":\"string\"\r\n      }\r\n   },\r\n   \"required\":[  \r\n      \"id\",\r\n      \"name\"\r\n   ]\r\n}";


static NSString* newDefinition =@"[{\r\n  \"title\" : \"My Title\",\r\n  \"description\" : \"this is muy description\",\r\n  \"creator\" : \"tim.n.obrien@yahoo.com\",\r\n  \"organization\" : null,\r\n  \"contactEmail\" : null,\r\n  \"contactPhone\" : null,\r\n  \"joinDate\" : \"12\/14\/2014\",\r\n  \"id\" : 12345,\r\n  \"informedConsentForm\" : \"informed consent\",\r\n  \"recordPhoneDetails\" : false,\r\n  \"extraDataCollectionDeclarations\" : [ ],\r\n  \"deleted\" : false,\r\n  \"earliestStartDate\" : null,\r\n  \"latestEndDate\" : null,\r\n  \"modifyDate\" : null,\r\n  \"published\" : true,\r\n  \"admins\" : [ \"tim\", \"jack\", \"john\", \"mike\" ],\r\n  \"publishedUsers\" : [ \"tim\", \"jack\", \"john\", \"mike\" ],\r\n  \"version\" : 1,\r\n  \"groups\" : [ {\r\n    \"name\" : \"test experiment groups\",\r\n    \"customRendering\" : false,\r\n    \"customRenderingCode\" : null,\r\n    \"fixedDuration\" : false,\r\n    \"startDate\" : null,\r\n    \"endDate\" : null,\r\n    \"logActions\" : false,\r\n    \"backgroundListen\" : false,\r\n    \"backgroundListenSourceIdentifier\" : null,\r\n    \"actionTriggers\" : [ {\r\n      \"type\" : \"scheduleTrigger\",\r\n      \"actions\" : [ ],\r\n      \"id\" : null,\r\n      \"schedules\" : [ {\r\n        \"scheduleType\" : 0,\r\n        \"esmFrequency\" : 3,\r\n        \"esmPeriodInDays\" : 0,\r\n        \"esmStartHour\" : 32400000,\r\n        \"esmEndHour\" : 61200000,\r\n        \"signalTimes\" : [ ],\r\n        \"repeatRate\" : 1,\r\n        \"weekDaysScheduled\" : 0,\r\n        \"nthOfMonth\" : 1,\r\n        \"byDayOfMonth\" : true,\r\n        \"dayOfMonth\" : 1,\r\n        \"esmWeekends\" : false,\r\n        \"minimumBuffer\" : 59,\r\n        \"joinDateMillis\" : 0,\r\n        \"beginDate\" : null,\r\n        \"id\" : null,\r\n        \"onlyEditableOnJoin\" : false,\r\n        \"userEditable\" : true,\r\n        \"byDayOfWeek\" : false,\r\n        \"defaultMinimumBuffer\" : 59,\r\n        \"nameOfClass\" : \"com.pacoapp.paco.shared.model2.Schedule\"\r\n      }, {\r\n        \"scheduleType\" : 0,\r\n        \"esmFrequency\" : 3,\r\n        \"esmPeriodInDays\" : 0,\r\n        \"esmStartHour\" : 32400000,\r\n        \"esmEndHour\" : 61200000,\r\n        \"signalTimes\" : [ ],\r\n        \"repeatRate\" : 1,\r\n        \"weekDaysScheduled\" : 0,\r\n        \"nthOfMonth\" : 1,\r\n        \"byDayOfMonth\" : true,\r\n        \"dayOfMonth\" : 1,\r\n        \"esmWeekends\" : false,\r\n        \"minimumBuffer\" : 59,\r\n        \"joinDateMillis\" : 0,\r\n        \"beginDate\" : null,\r\n        \"id\" : null,\r\n        \"onlyEditableOnJoin\" : false,\r\n        \"userEditable\" : true,\r\n        \"byDayOfWeek\" : false,\r\n        \"defaultMinimumBuffer\" : 59,\r\n        \"nameOfClass\" : \"com.pacoapp.paco.shared.model2.Schedule\"\r\n      }, {\r\n        \"scheduleType\" : 0,\r\n        \"esmFrequency\" : 3,\r\n        \"esmPeriodInDays\" : 0,\r\n        \"esmStartHour\" : 32400000,\r\n        \"esmEndHour\" : 61200000,\r\n        \"signalTimes\" : [ ],\r\n        \"repeatRate\" : 1,\r\n        \"weekDaysScheduled\" : 0,\r\n        \"nthOfMonth\" : 1,\r\n        \"byDayOfMonth\" : true,\r\n        \"dayOfMonth\" : 1,\r\n        \"esmWeekends\" : false,\r\n        \"minimumBuffer\" : 59,\r\n        \"joinDateMillis\" : 0,\r\n        \"beginDate\" : null,\r\n        \"id\" : null,\r\n        \"onlyEditableOnJoin\" : false,\r\n        \"userEditable\" : true,\r\n        \"byDayOfWeek\" : false,\r\n        \"defaultMinimumBuffer\" : 59,\r\n        \"nameOfClass\" : \"com.pacoapp.paco.shared.model2.Schedule\"\r\n      } ],\r\n      \"nameOfClass\" : \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n    } ],\r\n    \"inputs\" : [ ],\r\n    \"endOfDayGroup\" : false,\r\n    \"endOfDayReferredGroupName\" : null,\r\n    \"feedback\" : null,\r\n    \"feedbackType\" : 0,\r\n    \"nameOfClass\" : \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n  } ],\r\n  \"nameOfClass\" : \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}]";





@interface PacoServiceTests : XCTestCase

@property (strong, nonatomic) PacoService* pacoService;
@property (strong,nonatomic)  PacoClient * pacoClient;


@property (strong,nonatomic) NSArray *  classes;

@end

@implementation PacoServiceTests

- (void)setUp {
    [super setUp];
    _pacoClient = [PacoClient new];
    _pacoService = [PacoService new];
    _classes =  [self getClassNames];
    

    
    NSLog(@"initialized");
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testPrefetch {
    // This is an example of a functional test case.
    
    
    [[PacoClient sharedInstance] loginWithCompletionBlock:^(NSError *error) {
      
        if (error) {
               XCTAssert( @"NO Pass");
        }
        else
        {
  
            XCTAssert( @"Pass");
        }
           }];

 
    
}

-(void) testExperimentDefinition
{
   /*
    [[PacoClient sharedInstance].service
     loadFullDefinitionWithID:definitionId
     andBlock:^(PacoExperimentDefinition* definition, NSError* error) {
         dispatch_async(dispatch_get_main_queue(), ^{
             if (error) {
                 [PacoAlertView showAlertWithError:error];
             } else {
                 [self goToDefinitionDetailControllerWithDefinition:definition];
             }
         });
     }];*/
    
}



-(void) logDictionaryKeys:(NSDictionary*)dict
{
    for (id key in dict) {
        if ([dict[key] isKindOfClass:[NSDictionary class]]) {
            [self logDictionaryKeys:dict[key]];
        }else {
            NSLog(@"Key: %@", key);
            NSLog(@"Value: %@ (%@)", dict[key], [dict[key] class]);
        }
    }
    
    return;
}



- (void) parseJsonTree:(id) branch
{
    if ([branch isKindOfClass:[NSMutableArray class]])
    {
        //Keep drilling to find the leaf dictionaries
        for (id childBranch in branch)
        {
            [self parseJsonTree:childBranch];
        }
    }
    else if ([branch isKindOfClass:[NSMutableDictionary class]])
    {
        const id nul = [NSNull null];
        const NSString *empty = @"";
        for(NSString *key in [branch allKeys])
        {
            const id object = [branch objectForKey:key];
            if(object == nul)
            {
                [branch setObject:empty forKey:key];
            }
        }
    }
}




-(NSArray*) arrayOfIvarsFromInstance:(id) object
{
    NSMutableArray  * resultsArray = [NSMutableArray  new];
    
     unsigned int numIvars = 0;
     Ivar * ivars = class_copyIvarList([object class], &numIvars);
     NSLog(@" number of ivars %i",numIvars ) ;
    

    for (int i = 0; i < numIvars; ++i) {
        
        Ivar ivar = ivars[i];
        NSString * ivarName = [NSString stringWithCString:ivar_getName(ivar) encoding:NSUTF8StringEncoding];
        [resultsArray addObject:ivarName];
         NSLog(@"ivarName %@ %s", ivarName, ivar_getTypeEncoding(ivar)) ;
        
    }
    
    free(ivars);
    return resultsArray;
}






-(void) enumerateContentsInFolder
{
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    NSError * error;
    NSArray * directoryContents =  [[NSFileManager defaultManager]
                                    contentsOfDirectoryAtPath:documentsDirectory error:&error];
    
    NSLog(@"directoryContents ====== %@",directoryContents);
}
















-(NSArray*) getClassNames
{
    
    NSMutableArray* mutableArray  = [NSMutableArray new];
    NSString* path = @"/Users/northropo/Project/paco/Paco-iOS/DerivedData/Paco/Build/Intermediates/Paco.build/Debug-iphonesimulator/Paco.build/DerivedSources";
    NSArray* dirs = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:path error:Nil];
    
    
    
    
    NSArray* headers = [dirs filteredArrayUsingPredicate:[NSPredicate predicateWithFormat:@"self ENDSWITH '.h'"]];
    
    for(NSString* fileName in headers)
    {
        NSString * trimmedString = [fileName substringToIndex:[fileName length] -2];
        [mutableArray addObject:trimmedString];
        
    }
    
    return mutableArray;
    
}





-(void) testBuildModelTree
{
 
    NSError* error;
    NSData* data = [newDefinition dataUsingEncoding:NSUTF8StringEncoding];
    id definingObject = [NSJSONSerialization JSONObjectWithData:data
                                                        options:NSJSONReadingAllowFragments
                                                          error:&error];
    
    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:_classes];
    NSObject* resultObject  =  [serializer buildObjectHierarchyFromCollections:definingObject];
   // NSObject* resultObject2 = [serializer buildObjectHierarchyFromJSONOBject:data];

   id retObject = [serializer toJ2OBJCCollctionsHeirarchy:resultObject];
    
    NSError* error2 =nil;
    NSData* newData  = [serializer foundationCollectionToJSONData:resultObject Error:error2];
    NSString *string =  [[NSString alloc] initWithData:newData encoding:NSUTF8StringEncoding] ;
    NSLog(@"json string %@", string);
  
}

 









@end
