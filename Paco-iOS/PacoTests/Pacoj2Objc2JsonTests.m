//
//  PacoServiceTests.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 7/22/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "PacoClient.h"
#import "PacoService.h"
#import "PacoModel.h"
#import "PacoExperimentDefinition.h"
#import "ExperimentDAO.h"
#import "runtime.h"
#import "PacoSerializer.h"
#import <Foundation/Foundation.h>
#import <objc/runtime.h>
#include "java/util/HashMap.h"
#include "java/util/ArrayList.h"
#include "NSObject+ITNDescription.h"
#import "NSObject+J2objcKVO.h"




static NSString* jsonString  = @"{\r\n  \"title\": \"Drink Water\",\r\n  \"description\": \"tim obrien\",\r\n  \"creator\": \"Timo@google.com\",\r\n  \"organization\": \"Self\",\r\n  \"contactEmail\": \"Timo@google.com\",\r\n  \"id\": 5755617021001728,\r\n  \"recordPhoneDetails\": false,\r\n  \"extraDataCollectionDeclarations\": [],\r\n  \"deleted\": false,\r\n  \"modifyDate\": \"2015\/08\/22\",\r\n  \"published\": false,\r\n  \"admins\": [\r\n    \"Timo@google.com\"\r\n  ],\r\n  \"publishedUsers\": [],\r\n  \"version\": 11,\r\n  \"groups\": [\r\n    {\r\n      \"name\": \"New Group\",\r\n      \"customRendering\": false,\r\n      \"fixedDuration\": true,\r\n      \"startDate\": \"2015\/8\/23\",\r\n      \"endDate\": \"2015\/8\/28\",\r\n      \"logActions\": false,\r\n      \"backgroundListen\": false,\r\n      \"actionTriggers\": [\r\n        {\r\n          \"type\": \"scheduleTrigger\",\r\n          \"actions\": [\r\n            {\r\n              \"actionCode\": 1,\r\n              \"id\": 1440120356423,\r\n              \"type\": \"pacoNotificationAction\",\r\n              \"snoozeCount\": 0,\r\n              \"snoozeTime\": 600000,\r\n              \"timeout\": 15,\r\n              \"delay\": 5000,\r\n              \"msgText\": \"Time to participate\",\r\n              \"snoozeTimeInMinutes\": 10,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.PacoNotificationAction\"\r\n            }\r\n          ],\r\n          \"id\": 1440120356422,\r\n          \"schedules\": [\r\n            {\r\n              \"scheduleType\": 0,\r\n              \"esmFrequency\": 3,\r\n              \"esmPeriodInDays\": 0,\r\n              \"esmStartHour\": 32400000,\r\n              \"esmEndHour\": 61200000,\r\n              \"signalTimes\": [\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 32400000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Nine AM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 54000000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"Three PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                },\r\n                {\r\n                  \"type\": 0,\r\n                  \"fixedTimeMillisFromMidnight\": 57600000,\r\n                  \"missedBasisBehavior\": 1,\r\n                  \"label\": \"4 PM\",\r\n                  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.SignalTime\"\r\n                }\r\n              ],\r\n              \"repeatRate\": 1,\r\n              \"weekDaysScheduled\": 0,\r\n              \"nthOfMonth\": 1,\r\n              \"byDayOfMonth\": true,\r\n              \"dayOfMonth\": 1,\r\n              \"esmWeekends\": false,\r\n              \"minimumBuffer\": 59,\r\n              \"joinDateMillis\": 0,\r\n              \"id\": 1440120356424,\r\n              \"onlyEditableOnJoin\": false,\r\n              \"userEditable\": false,\r\n              \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Schedule\"\r\n            }\r\n          ],\r\n          \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ScheduleTrigger\"\r\n        }\r\n      ],\r\n      \"inputs\": [],\r\n      \"endOfDayGroup\": false,\r\n      \"feedback\": {\r\n        \"text\": \"Thanks for Participating!\",\r\n        \"type\": 0,\r\n        \"nameOfClass\": \"com.pacoapp.paco.shared.model2.Feedback\"\r\n      },\r\n      \"feedbackType\": 0,\r\n      \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentGroup\"\r\n    }\r\n  ],\r\n  \"ringtoneUri\": \"\/assets\/ringtone\/Paco Bark\",\r\n  \"postInstallInstructions\": \"<b>You have successfully joined the experiment!<\/b><br\/><br\/>No need to do anything else for now.<br\/><br\/>Paco will send you a notification when it is time to participate.<br\/><br\/>Be sure your ringer\/buzzer is on so you will hear the notification.\",\r\n  \"nameOfClass\": \"com.pacoapp.paco.shared.model2.ExperimentDAO\"\r\n}";

@interface Pacoj2Objc2JsonTests : XCTestCase

@property(strong, nonatomic) PacoService* pacoService;
@property(strong, nonatomic) PacoClient* pacoClient;

@property(strong, nonatomic) NSArray* classes;

@end

@implementation Pacoj2Objc2JsonTests

- (void)setUp {
  [super setUp];
  _pacoClient = [PacoClient new];
  _pacoService = [PacoService new];
  _classes = [self getClassNames];

  NSLog(@"initialized");
}

- (void)tearDown {
  // Put teardown code here. This method is called after the invocation of each
  // test method in the class.
  [super tearDown];
}

- (void)testPrefetch {
  // This is an example of a functional test case.

  [[PacoClient sharedInstance] loginWithCompletionBlock:^(NSError* error) {

    if (error) {
      XCTAssert(@"NO Pass");
    } else {
      XCTAssert(@"Pass");
    }
  }];
}

- (void)testExperimentDefinition {
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

- (void)logDictionaryKeys:(NSDictionary*)dict {
  for (id key in dict) {
    if ([dict[key] isKindOfClass:[NSDictionary class]]) {
      [self logDictionaryKeys:dict[key]];
    } else {
      NSLog(@"Key: %@", key);
      NSLog(@"Value: %@ (%@)", dict[key], [dict[key] class]);
    }
  }

  return;
}

- (void)parseJsonTree:(id)branch {
  if ([branch isKindOfClass:[NSMutableArray class]]) {
    // Keep drilling to find the leaf dictionaries
    for (id childBranch in branch) {
      [self parseJsonTree:childBranch];
    }
  } else if ([branch isKindOfClass:[NSMutableDictionary class]]) {
    const id nul = [NSNull null];
    const NSString* empty = @"";
    for (NSString* key in [branch allKeys]) {
      const id object = [branch objectForKey:key];
      if (object == nul) {
        [branch setObject:empty forKey:key];
      }
    }
  }
}

- (NSArray*)arrayOfIvarsFromInstance:(id)object {
  NSMutableArray* resultsArray = [NSMutableArray new];

  unsigned int numIvars = 0;
  Ivar* ivars = class_copyIvarList([object class], &numIvars);
  NSLog(@" number of ivars %i", numIvars);

  for (int i = 0; i < numIvars; ++i) {
    Ivar ivar = ivars[i];
    NSString* ivarName = [NSString stringWithCString:ivar_getName(ivar)
                                            encoding:NSUTF8StringEncoding];
    [resultsArray addObject:ivarName];
    NSLog(@"ivarName %@ %s", ivarName, ivar_getTypeEncoding(ivar));
  }

  free(ivars);
  return resultsArray;
}

- (void)enumerateContentsInFolder {
  NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,
                                                       NSUserDomainMask, YES);
  NSString* documentsDirectory = [paths objectAtIndex:0];
  NSError* error;
  NSArray* directoryContents = [[NSFileManager defaultManager]
      contentsOfDirectoryAtPath:documentsDirectory
                          error:&error];

  NSLog(@"directoryContents ====== %@", directoryContents);
}

- (NSArray*)getClassNames {
  NSMutableArray* mutableArray = [NSMutableArray new];
  NSString* path = @"/Users/Timo/Project/paco/Paco-iOS/DerivedData/Paco/"
      @"Build/Intermediates/Paco.build/Debug-iphonesimulator/"
      @"Paco.build/DerivedSources";
  NSArray* dirs =
      [[NSFileManager defaultManager] contentsOfDirectoryAtPath:path error:Nil];
  NSArray* headers =
      [dirs filteredArrayUsingPredicate:
                [NSPredicate predicateWithFormat:@"self ENDSWITH '.h'"]];
  for (NSString* fileName in headers) {
    NSString* trimmedString = [fileName substringToIndex:[fileName length] - 2];
    [mutableArray addObject:trimmedString];
  }
  return mutableArray;
}



- (void)testKVOCategory {
 
  NSData* data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
  PacoSerializer* serializer =
      [[PacoSerializer alloc] initWithArrayOfClasses:_classes
                            withNameOfClassAttribute:@"nameOfClass"];
    
  id object =
 (JavaUtilArrayList*)[serializer buildObjectHierarchyFromJSONOBject:data];
  PAExperimentDAO* dao = [object valueForKeyAndIndex:0 Key:@""];
  [dao setValueEx:@"12/12/20012" forKey:@"modifyDate"];
  NSString* str = [dao valueForKeyEx:@"modifyDate"];
  NSLog(@" string %@", str);
    
    NSNumber * number = [dao valueForKeyPathEx:@"id"];

  NSString* endDay = [dao valueForKeyPathEx:@"groups[0].name"];
  endDay = [dao valueForKeyPathEx:@"groups[5].endDate"];
}



- (void)testBuildModelTree {

  NSData* data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
  PacoSerializer* serializer =
      [[PacoSerializer alloc] initWithArrayOfClasses:nil 
                            withNameOfClassAttribute:@"nameOfClass"];

  NSObject* resultObject = [serializer buildObjectHierarchyFromJSONOBject:data];
  NSError* error2 = nil;
    
    
 NSObject * foundationObject =    [serializer toJSONobject:resultObject];
    
  NSData* newData =
      [serializer foundationCollectionToJSONData:foundationObject Error:error2];
  NSString* string =
      [[NSString alloc] initWithData:newData encoding:NSUTF8StringEncoding];
  NSLog(@"json string %@", string);
}


- (void)testBuildModelTree2 {
    
    NSData* data = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayWithClassAttributeName:@"nameOfClass"];
    
    NSObject* resultObject = [serializer buildObjectHierarchyFromJSONOBject:data];
    NSError* error2 = nil;
    
    
    NSObject * foundationObject =    [serializer toJSONobject:resultObject];
    
    NSData* newData =
    [serializer foundationCollectionToJSONData:foundationObject Error:error2];
    NSString* string =
    [[NSString alloc] initWithData:newData encoding:NSUTF8StringEncoding];
    NSLog(@"json string %@", string);
}




@end
