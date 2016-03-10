//
//  PacoNetworkTests.m
//  Paco
//
//  Created by Timo on 10/16/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "PacoNetwork.h"

@interface PacoNetworkTests : XCTestCase

@end

@implementation PacoNetworkTests

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)tearDown {
    // Put teardown code here. This method is called after the invocation of each test method in the class.
    [super tearDown];
}

- (void) testNetworkConnection
{
    PacoNetwork * network = [PacoNetwork sharedInstance];
    [network loginWithCompletionBlock:^(NSError* error) {
        
        if (error) {
            XCTAssert(@"NO Pass");
        } else {
            XCTAssert(@"Pass");
        }
    }];
}
    


   


@end
