//
//  PAExperimentGroup+PacoGroupHelper.h
//  Paco
//
//  Created by Northrop O'brien on 7/6/16.
//  Copyright © 2016 Paco. All rights reserved.
//

#import "ExperimentGroup.h"





@interface PAExperimentGroup (PacoGroupHelper)
/* get the feedback type */ 
-(int) feedbackType;
-(NSString*) jsonStringForJavascript;



@end
