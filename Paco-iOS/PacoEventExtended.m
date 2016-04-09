//
//  PacoEventExtended.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/13/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoEventExtended.h"

#import "PacoEventExtended.h"
#import "PacoDateUtility.h"
#import "PacoExtendedClient.h"
#import "PacoMediator.h"


#import "Schedule.h"
#import "PacoExperimentInput.h"
#import <CoreLocation/CoreLocation.h>
#import "NSString+Paco.h"
#import "UIImage+Paco.h"
#import "NSObject+J2objcKVO.h" 
#import "PacoSerializer.h" 
#import "PacoSerializeUtil.h" 
#import  "ActionSpecification.h"
#import "java/lang/Long.h"
#import "JavaUtilArrayList+PacoConversion.h"
#import "PacoEventPersistenceHelper.h"
#import "ExperimentDAO.h" 
#import "PAExperimentDAO+Helper.h"
#import "NSDate+PacoTimeZoneHelper.h"
#import "PacoEventExtended+PacoCoder.h"
#import "Input2.h"
#import "PacoNetwork.h"
#import "OrgJodaTimeDateTime+PacoDateHelper.h" 






#define JsonKey @"kjsonPrsistanceKey/ForPacoEvent"



static NSString* const kPacoEventKeyResponsesExtended = @"responses";
NSString* const kPacoResponseKeyNameExtended = @"name";
NSString* const kPacoResponseKeyAnswerExtended = @"answer";
NSString* const kPacoResponseKeyInputIdExtended= @"inputId";
NSString* const kPacoResponseJoinExtended = @"joined";





@interface PacoEventExtended ()
@property (nonatomic, readwrite, copy) NSString *appId;
@property (nonatomic, readwrite, copy) NSString *pacoVersion;
@end


@implementation PacoEventExtended

- (id)init {
    self = [super init];
    if (self) {
        _appId = @"iOS";
        
        NSString *version = [[NSBundle mainBundle] infoDictionary][(NSString*)kCFBundleVersionKey];
        NSAssert([version length] > 0, @"version number is not valid!");
        _pacoVersion = version;
    }
    return self;
}





- (PacoEventTypeExtended)type {
    for (NSDictionary *response in self.responses) {
        if ([response[kPacoResponseKeyNameExtended] isEqualToString:kPacoResponseJoinExtended]) {
            return [response[kPacoResponseKeyAnswerExtended] boolValue] ? PacoEventTypeJoinExtended : PacoEventTypeStopExtended;
        }
    }
    if (self.scheduledTime && self.responseTime) {
        return PacoEventTypeSurveyExtended;
    } else if (self.scheduledTime && !self.responseTime) {
        return PacoEventTypeMissExtended;
    } else {
        NSAssert(self.responseTime, @"responseTime should be valid for self report event");
        return PacoEventTypeSelfReportExtended;
    }
}


- (NSString*)description {
    NSString* responseStr = @"[";
    NSUInteger numOfResponse = [self.responses size];
    int index = 0;
    for (NSDictionary* responseDict in self.responses) {
        responseStr = [responseStr stringByAppendingString:@"{"];
        NSAssert([responseDict isKindOfClass:[NSDictionary class]], @"responseDict should be a dictionary!");
        
        NSUInteger numOfKeyValue = [[responseDict allKeys] count];
        int temp = 0;
        for (NSString* key in responseDict) {
            responseStr = [responseStr stringByAppendingString:key];
            responseStr = [responseStr stringByAppendingString:@":"];
            responseStr = [responseStr stringByAppendingString:[responseDict[key] description]];
            temp++;
            if (temp < numOfKeyValue) {
                responseStr = [responseStr stringByAppendingString:@","];
            }
        }
        responseStr = [responseStr stringByAppendingString:@"}"];
        
        index++;
        if (index < numOfResponse) {
            responseStr = [responseStr stringByAppendingString:@", "];
        }
    }
    responseStr = [responseStr stringByAppendingString:@"]"];
    
    NSDateFormatter* dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy/MM/dd HH:mm:ssZ"];
 
    return [NSString stringWithFormat:@"<%@, %p: id=%@,name=%@,version=%d,responseTime=%@,"
            "who=%@,when=%@,response=\r%@>",
            NSStringFromClass([self class]),
            self,
            self.experimentId,
            self.experimentName,
            [self.experimentVersion intValue],
            self.responseTime,
            self.who,
            self.when,
            responseStr];
}


- (id)generateJsonObject {
    
    
    /* refactor load classes only once on initialization */
    
    NSArray* array = [PacoSerializeUtil getClassNames];
    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:array withNameOfClassAttribute:@"nameOfClass"];
    NSData* json = [serializer toJSONobject:self];
    
     NSError* error;
    id definitionDict =
    [NSJSONSerialization JSONObjectWithData:json
                                    options:NSJSONReadingAllowFragments
                                      error:&error];
    return definitionDict;

}
 

 




- (id)payloadJsonWithImageString {
    if (0 == [self.responses size]) {
        return [self generateJsonObject];
    }
    
    NSArray* localResponses = [self.responses toNSArray];
    NSMutableArray* newReponseList = [NSMutableArray arrayWithArray:localResponses];
    for (int index=0; index<[localResponses count]; index++) {
        id responseDict = (localResponses )[index];
        if (![responseDict isKindOfClass:[NSDictionary class]]) {
            continue;
        }
        id answer = ((NSDictionary*)responseDict)[kPacoResponseKeyAnswerExtended];
        if (![answer isKindOfClass:[NSString class]]) {
            continue;
        }
        NSString* imageName = [UIImage pacoImageNameFromBoxedName:(NSString*)answer];
        if (!imageName) {
            continue;
        }
        NSString* imageString = [UIImage pacoBase64StringWithImageName:imageName];
        if ([imageString length] > 0) {
            NSMutableDictionary* newResponseDict = [NSMutableDictionary dictionaryWithDictionary:responseDict];
            newResponseDict[kPacoResponseKeyAnswerExtended] = imageString;
            newReponseList[index] = newResponseDict;
        }
    }
    
    NSMutableDictionary* jsonPayload =
    [NSMutableDictionary dictionaryWithDictionary:[self generateJsonObject]];
    
    jsonPayload[kPacoEventKeyResponsesExtended] = newReponseList;
    return jsonPayload;
}



+(void)  populateBasicAttributes:(PAExperimentDAO*) experiment Event:(PacoEventExtended*) event
{
    // event.who = [[PacoNetwork  sharedInstance] userEmail];
    event.experimentId =  [experiment valueForKeyPathEx:@"id"] ;
    
    event.experimentVersion =  (NSNumber*)  [experiment getVersion];
    
    event.experimentName =  [experiment valueForKeyPathEx:@"title"];
    

   
    
}

+ (PacoEventExtended*) stopEventForActionSpecificatonWithServerExperimentId:(PAExperimentDAO*) experiment  serverExperimentId:(NSString*) serverExperimentId
{
    
    // Setup an event for joining the experiement.
    PacoEventExtended *event = [PacoEventExtended new];
    
    
    
    event.who = [PacoNetwork sharedInstance].userEmail;
    event.experimentId      =  [experiment valueForKeyEx:@"id"];
    event.experimentVersion =  [experiment valueForKeyEx:@"version"];
    event.experimentName    =  [experiment valueForKeyEx:@"title"];
    event.responseTime = [NSDate new];
    
    
    NSDictionary* joinResponse = @{kPacoResponseKeyNameExtended:kPacoResponseJoinExtended,
                                   kPacoResponseKeyAnswerExtended:@"false",
                                   kPacoResponseKeyInputIdExtended:@"-1"};
    
    [PacoEventExtended populateBasicAttributes:experiment Event:event];
    JavaUtilArrayList * responseList = [[JavaUtilArrayList alloc] init];
    
    
    [responseList addWithId:joinResponse];
    
    
    NSString * scheduleString =  [experiment scheduleString];
    
    NSDictionary* scheduledResponse = @{kPacoResponseKeyNameExtended:kPacoEventKeyResponsesExtended,
                                        @"schedule":scheduleString,
                                        kPacoResponseKeyInputIdExtended:@"-1"};
    
    [responseList addWithId:scheduledResponse];
    
    
    NSDictionary* systemInfo = @{kPacoResponseKeyNameExtended:kPacoEventKeyResponsesExtended,
                                 [[UIDevice currentDevice] systemName] :[[UIDevice currentDevice] systemVersion] ,
                                 kPacoResponseKeyInputIdExtended:@"-1"};
    
    [responseList addWithId:systemInfo];
    event.responses = responseList;
    [event save];
    return event;
    
    
}


+ (PacoEventExtended*)joinEventForActionSpecificatonWithServerExperimentId:(PAExperimentDAO*) experiment  serverExperimentId:(NSString*) serverExperimentId
{
    
    // Setup an event for joining the experiement.
    PacoEventExtended *event = [PacoEventExtended new];
    
    
    
    event.who = [PacoNetwork sharedInstance].userEmail;
    event.experimentId      =  [experiment valueForKeyEx:@"id"];
    event.experimentVersion =  [experiment valueForKeyEx:@"version"];
    event.experimentName    =  [experiment valueForKeyEx:@"title"];
    event.responseTime = [NSDate new];
    
    
    NSDictionary* joinResponse = @{kPacoResponseKeyNameExtended:kPacoResponseJoinExtended,
                                   kPacoResponseKeyAnswerExtended:@"true",
                                   kPacoResponseKeyInputIdExtended:@"-1"};
    
     [PacoEventExtended populateBasicAttributes:experiment Event:event];
      JavaUtilArrayList * responseList = [[JavaUtilArrayList alloc] init];
    
 
    [responseList addWithId:joinResponse];
    
    
      NSString * scheduleString =  [experiment scheduleString];
    
      NSDictionary* scheduledResponse = @{kPacoResponseKeyNameExtended:kPacoEventKeyResponsesExtended,
                                   @"schedule":scheduleString,
                                   kPacoResponseKeyInputIdExtended:@"-1"};
    
      [responseList addWithId:scheduledResponse];
    
    
    NSDictionary* systemInfo = @{kPacoResponseKeyNameExtended:kPacoEventKeyResponsesExtended,
                                    [[UIDevice currentDevice] systemName] :[[UIDevice currentDevice] systemVersion] ,
                                    kPacoResponseKeyInputIdExtended:@"-1"};

     [responseList addWithId:systemInfo];
      event.responses = responseList;
     [event save];
      return event;
    
    
}






 
 
/*

+ (PacoEventExtended *) genericEventForDefinition:(PAExperimentDAO*)definition
                             withInputs:(NSArray*)inputs {
    
    
     PacoEventExtended *event = [PacoEventExtended new];
     event.who = [PacoNetwork sharedInstance].userEmail;
     event.experimentId = [definition valueForKeyPathEx:@"id"];
     event.experimentName = [definition valueForKeyPathEx:@"title"];
     event.experimentVersion = [definition valueForKeyPathEx:@"version"];
   
    
    NSMutableArray *responses = [NSMutableArray array];
    for (PAInput2 *input in inputs) {
        NSMutableDictionary *response = [NSMutableDictionary dictionary];
        
        id payloadObject = [input payloadObject];
        if (payloadObject == nil) {
            continue;
        }
       
        response[@"name"] = input.name;
        response[@"inputId"] = input.inputIdentifier;
        
        if (![payloadObject isKindOfClass:[UIImage class]]) {
            response[@"answer"] = payloadObject;
        } else {
            NSString* imageName = [UIImage pacoSaveImageToDocumentDir:payloadObject
                                                        forDefinition:[definition valueForKeyPathEx:@"id"]
                                                              inputId:input.inputIdentifier];
            if ([imageName length] > 0) {
                NSString* fullName = [UIImage pacoBoxedNameFromImageName:imageName];
                response[@"answer"] = fullName;
            } else {
                response[@"answer"] = @"Failed to save image";
            }
        }
        
        [responses addObject:response];
    }
    
    event.responses = responses;
    return event;
}

*/












+ (PacoEventExtended *)selfReportEventForDefinition:(PAExperimentDAO*) definition
                                withInputs:(NSArray*)inputs {
    NSAssert(inputs != nil, @"inputs should not be nil!");
    PacoEventExtended* event = [PacoEventExtended genericEventForDefinition:definition withInputs:inputs];
    event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];
    event.scheduledTime = nil;
    return event;
}


+ (PacoEventExtended*)surveySubmittedEventForDefinition:(PAExperimentDAO*)definition
                                     withInputs:(NSArray*)inputs
                               andScheduledTime:(NSDate*)scheduledTime {
    NSAssert(scheduledTime != nil, @"scheduledTime should not be nil!");
    PacoEventExtended* event = [PacoEventExtended genericEventForDefinition:definition withInputs:inputs];
    event.responseTime = [[NSDate dateWithTimeIntervalSinceNow:0] dateToStringLocalTimezone];
    event.scheduledTime = [scheduledTime dateToStringLocalTimezone];
    return event;
}


/*
    
    Survay for missed event,  includes scheduled time in the event
 
 */

+ (PacoEventExtended*)surveyMissedEventForDefinition:(PAExperimentDAO*)definition
                           withScheduledTime:(NSDate*)scheduledTime {
    NSAssert(scheduledTime != nil, @"scheduledTime should be valid!");
    PacoEventExtended* event = [self surveyMissedEventForDefinition:definition
                                          withScheduledTime:scheduledTime
                                                          userEmail:@"email"];// [[PacoExtendedClient sharedInstance] userEmail]];
    return event;
}

/*
     
  Creates an experiment definition for survay missed.
 
 */
+ (PacoEventExtended*)surveyMissedEventForDefinition:(PAExperimentDAO*)definition
                           withScheduledTime:(NSDate*)scheduledTime
                                   userEmail:(NSString*)userEmail{
    NSAssert(definition, @"definition should be valid");
    NSAssert(scheduledTime != nil, @"scheduledTime should be valid!");
    NSAssert([userEmail length] > 0, @"userEmail should be valid!");
    PacoEventExtended *event = [PacoEventExtended new];
    event.who = userEmail;
    event.experimentId = [definition valueForKeyPathEx:@"id"];
    event.experimentName = [definition valueForKeyPathEx:@"title"];
    event.experimentVersion = [definition valueForKeyPathEx:@"version"];
    event.responseTime = nil;
    event.scheduledTime = [scheduledTime dateToStringLocalTimezone];
    return event;
}





#pragma mark - NSCoder & NSCopy methods 

- (id)initWithCoder:(NSCoder *)decoder
{
    
    /* super does not support  initWithCoder so we don't try to invoke it */
    
     NSData* data = [decoder decodeObjectForKey:JsonKey];
     PacoSerializer* serializer =
    [[PacoSerializer alloc] initWithArrayOfClasses:nil
                          withNameOfClassAttribute:@"nameOfClass"];
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
    IOSObjectArray * iosArray = [resultArray toArray];
    PacoEventExtended * event  =  [iosArray objectAtIndex:0];
    self =event;
    return self;
 
}


- (void) encodeWithCoder:(NSCoder *)encoder
{
    
    NSArray* array = [PacoSerializeUtil getClassNames];
    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:array withNameOfClassAttribute:@"nameOfClass"];
    NSData* json = [serializer toJSONobject:self];
    [encoder encodeObject:json  forKey:JsonKey];
}



- (id)copyWithZone:(NSZone *)zone {
  
    NSArray* array = [PacoSerializeUtil getClassNames];
    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:array withNameOfClassAttribute:@"nameOfClass"];
    [serializer addNoneDomainClass:self];
    NSData* json = [serializer toJSONobject:self];
    
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:json];
    IOSObjectArray * iosArray = [resultArray toArray];
    PacoEventExtended  * event =  [iosArray objectAtIndex:0];
    return event;
    
}


#pragma mark - PAEventInterface methods

- (id<PAEventInterface>)getEventWithJavaLangLong:(JavaLangLong *)experimentId
                         withOrgJodaTimeDateTime:(OrgJodaTimeDateTime *)scheduledTime
                                    withNSString:(NSString *)groupName
                                withJavaLangLong:(JavaLangLong *)actionTriggerId
                                withJavaLangLong:(JavaLangLong *)scheduleId
{
   PacoEventPersistenceHelper* helper = [[PacoEventPersistenceHelper alloc] init];
    
    id<PAEventInterface> retVal = [helper getEventWithJavaLangLong:experimentId withOrgJodaTimeDateTime:scheduledTime withNSString:groupName withJavaLangLong:actionTriggerId withJavaLangLong:scheduleId];
    return retVal;
}

- (void)updateEventWithPAEventInterface:(id<PAEventInterface>)correspondingEvent
{
      PacoEventPersistenceHelper* helper = [[PacoEventPersistenceHelper alloc] init];
    [helper updateEventWithPAEventInterface:correspondingEvent];
    
}

- (void)insertEventWithPAEventInterface:(id<PAEventInterface>)event
{
      PacoEventPersistenceHelper* helper = [[PacoEventPersistenceHelper alloc] init];
    [helper  insertEventWithPAEventInterface:event];
}

@end

