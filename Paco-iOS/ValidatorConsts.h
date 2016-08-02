/* Copyright 2015  Google
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */


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
