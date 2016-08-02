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

#import "OrgJodaTimeDateTime+PacoDateHelper.h"
#include "DateTime.h"


@implementation OrgJodaTimeDateTime (PacoDateHelper)



-(NSString* ) dateTimeFormatted
{
    NSDate *date = [self nsDateValue];
    NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
    [dateFormat setDateFormat:@"yyyy/MM/dd HH:mm:ssZ"];
    NSString *dateString = [dateFormat stringFromDate:date];
    return dateString;
    
}

-(NSDate*) nsDateValue
{
     long ll = [self getMillis];
     /* divide by one thousand to convert to seconds since epoch*/
     NSDate * date =  [NSDate dateWithTimeIntervalSince1970:[self getMillis]/1000];
     return  date;
}

-(BOOL) isGreaterThan:(OrgJodaTimeDateTime*) otherTime;
{
    
    return ([self getMillis] > [otherTime getMillis]);
}


-(BOOL) isLessThan:(OrgJodaTimeDateTime*) otherTime;
{
    
    return ([self getMillis] < [otherTime getMillis]);
}

@end



