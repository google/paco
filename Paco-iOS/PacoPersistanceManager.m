//
//  PacoPersistanceManager.m
//  Paco
//
//  Created by northropo on 9/30/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoPersistanceManager.h"
#import "ExperimentDAO.h"
#import "PacoMediator.h"

/*
@interface PacoMediator()

@property (strong,nonatomic ) NSMutableArray* allExperiments;
@property (strong,nonatomic)   NSMutableArray* runningExperiments;

@end
*/


@implementation PacoPersistanceManager
/*
+ (NSString *)getExperimentsDir {
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths objectAtIndex:0];
    documentsDirectory = [documentsDirectory stringByAppendingPathComponent:@"Experiments"];
    
    NSError *error;
    [[NSFileManager defaultManager] createDirectoryAtPath:documentsDirectory withIntermediateDirectories:YES attributes:nil error:&error];
    
    return documentsDirectory;
    
}

+ (NSString *)filePath {
    NSArray *urls = [[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory
                                                           inDomains:NSUserDomainMask];
    NSString *documentDirPath = [[urls objectAtIndex:0] path];
    NSString* experimentsDirectory =  [documentDirPath stringByAppendingPathComponent:@"AllExperiments"];
    NSString* fileDir = [experimentsDirectory stringByAppendingPathComponent:@"AllExperiments"];
    return fileDir;
    
}

+(NSArray*) loadAllAxperiments
{
    
     NSArray* experiments =  [NSKeyedUnarchiver  unarchiveObjectWithFile:[PacoPersistanceManager filePath]];
     return experiments;
}

+(BOOL ) saveAllExperiments
{
    [PacoMediator sharedInstance];
    BOOL  isSaved = [NSKeyedArchiver archiveRootObject:[[PacoMediator sharedInstance]  allExperiments] toFile:[PacoPersistanceManager filePath]];
    return isSaved;
}

*/




@end
