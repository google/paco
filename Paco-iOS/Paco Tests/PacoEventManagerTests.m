//
//  PacoEventManagerTests.m
//  Paco
//
//  Created by northropo on 10/8/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "PacoEventManagerExtended.h"
#import "PacoEventExtended.h"
#include "NSDate+PacoTimeZoneHelper.h"
#import "PacoEventPersistenceHelper.h"


@interface PacoEventManagerExtended()

- (void)fetchAllEventsIfNecessary;
- (void)fetchPendingEventsIfNecessary;

@end


@interface PacoEventManagerTests : XCTestCase
@property (nonatomic,strong) PacoEventPersistenceHelper* helper;
@end

@implementation PacoEventManagerTests

- (void)setUp {
    [super setUp];
    
    
    _helper = [PacoEventPersistenceHelper new];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void)testSaveEvent
{
    PacoEventManagerExtended * eventManageer = [PacoEventManagerExtended defaultManager];
    
    [_helper deleteAllEvents];
    
    NSString* schedledTime = [[NSDate date] dateToStringLocalTimezone]  ;
    
    PacoEventExtended * event = [[PacoEventExtended alloc] init];
    event.who =@"me";
    event.when = schedledTime; ;
    event.latitude = [NSNumber numberWithInt:12345];
    event.longitude = [NSNumber numberWithInt:765432];
    event.responseTime= [[NSDate date] dateToStringLocalTimezone];
    event.experimentId =  [NSNumber numberWithInt:1234];
    event.experimentName = @"experimentName";
    event.experimentVersion = [NSNumber numberWithInt:5];
    event.groupName=@"GroupAAA";
    event.scheduleId =[NSNumber numberWithInt:3];
    event.actionTriggerId =[NSNumber numberWithInt:3];
    event.scheduledTime =schedledTime;
    JavaUtilArrayList* arrayList = [[JavaUtilArrayList alloc] init];
    [arrayList addWithId:@"One"];
    [arrayList addWithId:@"Two"];
    [arrayList addWithId:@"Three"];
    event.responses = arrayList;
    [eventManageer saveEvent:event];
    
    NSArray* allEvents = [_helper allEvents];
    
     NSArray* eventsToUpload =  [_helper eventsForUpload];
  
     XCTAssert([allEvents count]==1, @"Pass");
     XCTAssert([eventsToUpload count]==1, @"Pass");
}


- (void)testSaveEvents
{
    PacoEventManagerExtended * eventManageer = [PacoEventManagerExtended defaultManager];
    
    [_helper deleteAllEvents];
    
    NSString* schedledTime = [[NSDate date] dateToStringLocalTimezone]  ;
    
    PacoEventExtended * event = [[PacoEventExtended alloc] init];
    event.who =@"me";
    event.when = schedledTime; ;
    event.latitude = [NSNumber numberWithInt:12345];
    event.longitude = [NSNumber numberWithInt:765432];
    event.responseTime= [[NSDate date] dateToStringLocalTimezone];
    event.experimentId =  [NSNumber numberWithInt:1234];
    event.experimentName = @"experimentName";
    event.experimentVersion = [NSNumber numberWithInt:5];
    event.groupName=@"GroupAAA";
    event.scheduleId =[NSNumber numberWithInt:3];
    event.actionTriggerId =[NSNumber numberWithInt:3];
    event.scheduledTime =schedledTime;
    JavaUtilArrayList* arrayList = [[JavaUtilArrayList alloc] init];
    [arrayList addWithId:@"One"];
    [arrayList addWithId:@"Two"];
    [arrayList addWithId:@"Three"];
    event.responses = arrayList;
    [eventManageer saveEvents:@[event]];
    
    NSArray* allEvents = [_helper allEvents];
    
    NSArray* eventsToUpload =  [_helper eventsForUpload];
    
    XCTAssert([allEvents count]==1, @"Pass");
    XCTAssert([eventsToUpload count]==1, @"Pass");
}


- (void)testSetUploaded
{
 
     PacoEventManagerExtended * eventManageer = [PacoEventManagerExtended defaultManager];
    
    [_helper deleteAllEvents];
    
    NSString* schedledTime = [[NSDate date] dateToStringLocalTimezone]  ;
    PacoEventExtended * event = [[PacoEventExtended alloc] init];
    
    event.who =@"me";
    event.when = schedledTime; ;
    event.latitude = [NSNumber numberWithInt:12345];
    event.longitude = [NSNumber numberWithInt:765432];
    event.responseTime= [[NSDate date] dateToStringLocalTimezone];
    event.experimentId =  [NSNumber numberWithInt:1234];
    event.experimentName = @"experimentName";
    event.experimentVersion = [NSNumber numberWithInt:5];
    event.groupName=@"GroupAAA";
    event.scheduleId =[NSNumber numberWithInt:3];
    event.actionTriggerId =[NSNumber numberWithInt:3];
    event.scheduledTime =schedledTime;
    JavaUtilArrayList* arrayList = [[JavaUtilArrayList alloc] init];
    [arrayList addWithId:@"One"];
    [arrayList addWithId:@"Two"];
    [arrayList addWithId:@"Three"];
    event.responses = arrayList;
    [eventManageer saveEvent:event];
    
    NSArray* allEvents = [_helper allEvents];
    
    [eventManageer fetchPendingEventsIfNecessary];
    [eventManageer  markEventsComplete:@[event]];
    
    NSArray* eventsToUpload =  [_helper eventsForUpload];
    
    XCTAssert([allEvents count]==1,@"Pass");
    XCTAssert([eventsToUpload count]==0,@"Pass");
    
}

@end
