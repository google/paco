//
//  PAActionSpecification+PacoActionSpecification.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/1/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PAActionSpecification+PacoActionSpecification.h"
#include "org/joda/time/DateTime.h"
#include "ExperimentDAO.h"

@implementation PAActionSpecification (PacoActionSpecification)

-(NSString *)description
{
    return [NSString stringWithFormat:@" time : %@ \n description %@ \n\n  milis: %lld ", [time_ description] ,  [experiment_ getTitle] , [time_ getMillis]];
}

@end
