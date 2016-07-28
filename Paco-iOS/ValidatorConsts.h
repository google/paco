//
//  ValidatorConsts.h
//  Paco
//
//  Authored by  Tim N. O'Brien on 9/24/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//



/*
   this file will be merged with a global .h file and added to .pch file
 
 */
#ifndef Paco_ValidatorConsts_h
#define Paco_ValidatorConsts_h


typedef NS_ENUM(NSInteger, ValidatorExecutionStatus)
{
    ValidatorExecutionStatusSuccess =(1 << 0),
    ValidatorExecutionStatusFail =(1 << 1),
    ValidatorExecutionStatusUnableToSave  = (1 << 2),
    ValidatorExecutionStatusExperimentNil =(1 << 3),
    ValidatorExecutionStatusNoApplicableSpecifications =(1 << 4),
    ValidatorExecutionStatusIsSelfReport =(1 << 4)

};


#endif
