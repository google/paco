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


static NSString* smDefinition = @"[{\"title\":\"ESM Demo\",\"description\":\"This experiment demonstrates an ESM (Experiential Sampling Method) study. It will prompt the user to respond to some questions randomly 5 times per day between 10 and 6. The notification to respond will time out in 15 minutes and record a missed signal in that case. The experiment is ongoing, as opposed to a fixed number of days, in duration. It also uses conditional branching to show some questions only when other questions answers take on certain values.\",\"creator\":\"bobevans@google.com\",\"contactEmail\":\"bobevans@google.com\",\"id\":5754435435233280,\"recordPhoneDetails\":false,\"extraDataCollectionDeclarations\":[],\"deleted\":false,\"published\":false,\"admins\":[\"bobevans@google.com\",\"elasticsearch64@gmail.com\"],\"publishedUsers\":[],\"version\":3,\"groups\":[{\"name\":\"New Group\",\"customRendering\":false,\"fixedDuration\":false,\"logActions\":false,\"backgroundListen\":false,\"actionTriggers\":[{\"type\":\"scheduleTrigger\",\"actions\":[{\"actionCode\":1,\"id\":1436903218335,\"type\":\"pacoNotificationAction\",\"snoozeCount\":0,\"snoozeTime\":600000,\"timeout\":15,\"delay\":5000,\"msgText\":\"Time to participate\",\"snoozeTimeInMinutes\":10}],\"id\":1436903218334,\"schedules\":[{\"scheduleType\":4,\"esmFrequency\":5,\"esmPeriodInDays\":0,\"esmStartHour\":36000000,\"esmEndHour\":64800000,\"signalTimes\":[{\"type\":0,\"fixedTimeMillisFromMidnight\":0}],\"repeatRate\":1,\"weekDaysScheduled\":0,\"nthOfMonth\":1,\"byDayOfMonth\":true,\"dayOfMonth\":1,\"esmWeekends\":true,\"minimumBuffer\":59,\"joinDateMillis\":0,\"id\":1436903218336,\"onlyEditableOnJoin\":false,\"userEditable\":true,\"defaultMinimumBuffer\":59,\"byDayOfWeek\":false}]}],\"inputs\":[{\"name\":\"activity\",\"required\":false,\"conditional\":false,\"responseType\":\"open text\",\"text\":\"What are you doing right now?\",\"multiselect\":false,\"numeric\":false,\"invisible\":false},{\"name\":\"where\",\"required\":false,\"conditional\":false,\"responseType\":\"list\",\"text\":\"Where are you?\",\"listChoices\":[\"Home\",\"Work\",\"Other\"],\"multiselect\":false,\"numeric\":true,\"invisible\":false},{\"name\":\"other_where\",\"required\":false,\"conditional\":true,\"conditionExpression\":\"where == 3\",\"responseType\":\"open text\",\"text\":\"Please enter a name for the place you are\",\"multiselect\":false,\"numeric\":false,\"invisible\":false},{\"name\":\"photo\",\"required\":false,\"conditional\":false,\"responseType\":\"photo\",\"text\":\"Take a photo if you like\",\"multiselect\":false,\"numeric\":false,\"invisible\":true}],\"endOfDayGroup\":false,\"feedback\":{\"text\":\"Thank you for Participating!\",\"type\":0},\"feedbackType\":0}]},{\"title\":\"user present trigger\",\"creator\":\"elasticsearch64@gmail.com\",\"contactEmail\":\"elasticsearch64@gmail.com\",\"id\":5685441885896704,\"recordPhoneDetails\":false,\"extraDataCollectionDeclarations\":[],\"deleted\":false,\"published\":false,\"admins\":[\"elasticsearch64@gmail.com\"],\"publishedUsers\":[],\"version\":2,\"groups\":[{\"name\":\"New Group\",\"customRendering\":false,\"fixedDuration\":false,\"logActions\":false,\"backgroundListen\":false,\"actionTriggers\":[{\"type\":\"interruptTrigger\",\"actions\":[{\"actionCode\":1,\"id\":1437698202506,\"type\":\"pacoNotificationAction\",\"snoozeCount\":0,\"snoozeTime\":600000,\"timeout\":15,\"delay\":5000,\"msgText\":\"Time to participate\",\"snoozeTimeInMinutes\":10}],\"id\":1437698202505,\"cues\":[{\"cueCode\":2}],\"minimumBuffer\":59,\"defaultMinimumBuffer\":15}],\"inputs\":[],\"endOfDayGroup\":false,\"feedback\":{\"text\":\"Thanks for Participating!\",\"type\":0},\"feedbackType\":0}]}]";



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



-(void) populateObjectFromDictionary:(NSDictionary*) dictionary Object:(id) object
{
       NSArray * array = [dictionary allKeys];
       NSMutableArray * noArray = [NSMutableArray new];
       NSMutableArray * yesArray = [NSMutableArray new ];
      NSArray* resultsArray =   [self arrayOfIvarsFromInstance:object];
    
       for(NSString* string  in array)
       {
        NSString * str =  [NSString stringWithFormat:@"_%@",string];
        if([resultsArray  containsObject:str])
        {
            
            [object setValue:[dictionary objectForKey:string]   forKeyPath:string];
            
            [yesArray addObject:str];
        }
        else{
            
            [noArray addObject:str];
            
            
            
        }
        
    }
   
}






-(void)  recurseJason:(id ) recurseObject Level:(int) level
{
   
    
    if( [recurseObject isKindOfClass:[NSDictionary class]]  )
    {
        
        NSLog(@" DICTIONARY -->" );
             NSArray * arrayOfKeys = [recurseObject allKeys];
             for( NSString* key in arrayOfKeys )
             {
                 
                 NSLog(@"%@", key);
                id  newObject = [recurseObject objectForKey:key];
                 
                [self recurseJason:newObject Level:level+1];
                 
            }
        
            NSLog(@" <--DICTIONARY " );
        
    }
    else  if( [recurseObject isKindOfClass:[NSArray class]]  )
    {
        NSLog(@" LIST -->" );
        for( NSObject* obj in recurseObject )
        {
             [self recurseJason:obj Level:level+1];
        }
            NSLog(@" <--LIST " );
        
        
    }
    else{
        
        NSMutableString * m =[NSMutableString new];
        
        for(int i =0 ; i < level; i++)
        {
            [m insertString:@"    " atIndex:0];
            
        }
        
        NSString * string = [NSString stringWithFormat:@"%@%@",@"",recurseObject];
        NSLog(@"=%@", string );
        
    }
    
    
    
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





-(void) testFetchJson
{
    
    NSError* error;
    NSData* data = [smDefinition dataUsingEncoding:NSUTF8StringEncoding];
    id definitionDict = [NSJSONSerialization JSONObjectWithData:data
                                                        options:NSJSONReadingAllowFragments
                                                          error:&error];
    
    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:_classes];
    
    [serializer recurseJason:definitionDict Level:0 Block:^(id data, PacoParserType type) {
        
        NSString* printValue;
        
        switch (type) {
            case kInputTypeArraytEntry:
                printValue = @"arrayEntry";
                break;
            case kInputTypeDictionaryEntry:
                 printValue = @"dicationaryEntry";
                break;
            case kInputTypeNewDictionary:
                printValue = @"new dictioanry";
                break;
            case kInputTypeEndNewDictionary:
                printValue = @"new end new dictionary";
                break;
 
            case kInputTypeStartArray:
                printValue = @"start Array";
                break;
            case kInputTypeEndArray:
                printValue = @"end Array";
                break;
            default:
                break;
        }
        
        
        NSLog(@"%@ %@", printValue, data);
        
        
    }];
    
   

    
}

 



-(void)testLoad
{
    
    PacoModel * modal = [[PacoModel alloc] init];
    NSArray * array =  [modal loadExperimentDefinitionsFromFileWithJson];
    
    NSLog(@" this is %@",array);
    NSDictionary* dict =nil;
    
    Class theClass = NSClassFromString(@"PacoExperimentDefinition");
    id definition = [[theClass alloc] init];
    NSArray* resultsArray =   [self arrayOfIvarsFromInstance:definition];
    
    NSLog(@"%@", resultsArray);
    
    for(dict in array)
    {
        
        NSLog(@"%@", dict);
        [self recurseJason:dict Level:0];
        
       // [self populateObjectFromDictionary:dict Object:definition];
      //  NSArray * array = [dict allKeys];
        
    
     
    }
    
    NSLog(@"done");
    
    
}




-(void) testPrefetchBackkground
{
    
    [[PacoClient sharedInstance]  prefetchInBackground];
    
 
    
    NSLog(@"most likely this one ");
    
}

@end
