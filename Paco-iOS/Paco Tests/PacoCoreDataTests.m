//
//  PacoCoreDataTests.m
//  Paco
//
//  Created by northropo on 10/1/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import <CoreData/CoreData.h>
#import "PacoSignal.h"
#import "PacoAppDelegate.h" 
#include <stdlib.h>
#include "java/lang/Long.h"
#include "PacoSignal.h"


@interface PacoCoreDataTests : XCTestCase
@property (strong,nonatomic)   PacoAppDelegate* appDelegate;
@property (strong,nonatomic)   NSManagedObjectContext* context;
@property (strong,nonatomic)   PacoSignal *pacoSignal;
@end

@implementation PacoCoreDataTests

- (void)setUp {
    [super setUp];
    
    
    
    self.appDelegate  = (PacoAppDelegate *) [UIApplication sharedApplication].delegate;
    self.context = self.appDelegate.managedObjectContext;
 
    _pacoSignal = [NSEntityDescription
                              insertNewObjectForEntityForName:@"PacoSignal"
                              inManagedObjectContext:[self.appDelegate managedObjectContext]];
    
//    self.pacoSignal.date  = [NSNumber numberWithLongLong:arc4random_uniform(74)];
//    self.pacoSignal.experimentId = [NSNumber numberWithLongLong:arc4random_uniform(74)];
//    self.pacoSignal.groupName = @"groupName";
//    self.pacoSignal.actionTriggerId = [NSNumber numberWithLongLong:9876];
    
    
    self.pacoSignal.date  =   [JavaLangLong valueOfWithLong:12345];
    self.pacoSignal.experimentId = [JavaLangLong valueOfWithLong:12345];
    self.pacoSignal.groupName = @"groupnamethidons";
    self.pacoSignal.actionTriggerId =[JavaLangLong valueOfWithLong:12345];
    self.pacoSignal.scheduleId =  [JavaLangLong valueOfWithLong:12345];
    
    
    
    NSError *error;
    if (![self.appDelegate.managedObjectContext save:&error]) {
        NSLog(@"fail: %@", [error localizedDescription]);
        XCTAssert(NO, @"Fail");
    }


}



- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}







- (void)testExample {
 
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"PacoSignal" inManagedObjectContext:self.context];
    
  
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"(experimentId==%@) AND (date==%@) AND   (groupName LIKE %@)   AND   (actionTriggerId==%@) AND (scheduleId==%@)",[JavaLangLong valueOfWithLong:12345],[JavaLangLong valueOfWithLong:12345],self.pacoSignal.groupName,[JavaLangLong valueOfWithLong:12345],[JavaLangLong valueOfWithLong:12345]];
    

    
    [fetchRequest setEntity:entity];
    [fetchRequest setPredicate:predicate];
    
    NSError *error;
    NSArray *result = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if (error) {
        NSLog(@"Unable to execute fetch request.");
        NSLog(@"%@, %@", error, error.localizedDescription);
          XCTAssert(NO, @"Fail");
    } else {
        
        XCTAssert(YES, @"Fail");
        XCTAssert([result count]==1, @"Success");
        
        
        
        NSLog(@"%@", result);
    }
    
 
    
    XCTAssert(YES, @"Pass");
}



@end
