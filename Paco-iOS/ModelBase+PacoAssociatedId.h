//
//  ModelBase+PacoAssociatedId.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/11/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "ModelBase.h"

@interface PAModelBase (PacoAssociatedId)

@property (nonatomic, strong) NSString*  uuid;

- (NSString*) getUuid;
- (void)setUuid:(NSString*) uniqueId;

@end
