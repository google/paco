//
//  PacoEventExtended+PacoCoder.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 10/6/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoEventExtended.h"

@interface PacoEventExtended (PacoCoder)<NSCopying,NSCoding>

-(void) save;

@end
