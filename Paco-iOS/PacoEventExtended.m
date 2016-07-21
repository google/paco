//
//  PacoEventExtended.m
//  Paco
//
//  Authored by  Tim N. O'Brien on 8/13/15.
//  Copyright (c) 2015 Paco. All rights reserved.
//

#import "PacoEventExtended.h"
#import "PacoDateUtility.h"
#import "PacoExtendedClient.h"
#import "PacoMediator.h"
#import "JavaUtilArrayList+PacoConversion.h"
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
#import "NSDate+PacoTimeZoneHelper.h" 
#import "JavaUtilArrayList+PacoConversion.h"
#import "SchedulePrinter.h" 
#import "PAExperimentDAO+Helper.h"
#import "JavaUtilArrayList+PacoConversion.h"
#include "java/util/ArrayList.h"





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



/*
 
 
 @property (nonatomic, copy) NSString *who;
 @property (nonatomic, retain) NSString  *when;
 @property (nonatomic, assign) NSNumber* latitude;
 @property (nonatomic, assign) NSNumber*  longitude;
 @property (nonatomic, retain) NSDate   *responseTime;
 @property (nonatomic, retain) NSString  *scheduledTime;
 @property (nonatomic, readonly, copy) NSString *appId;
 @property (nonatomic, readonly, copy) NSString *pacoVersion;
 @property (nonatomic, copy)   NSNumber  *experimentId;
 @property (nonatomic, copy)   NSString *experimentName;
 @property (nonatomic, copy)   NSNumber*   experimentVersion;
 @property (nonatomic, retain) JavaUtilArrayList  *responses;
 @property (nonatomic,strong)  NSNumber* scheduleId;
 @property (nonatomic,strong)  NSNumber* actionTriggerId;
 @property (nonatomic,strong)  NSNumber* actionId;
 @property (nonatomic,strong)  NSNumber* actionTriggerSpecId;
 @property (nonatomic,strong)  NSString* experimentGroupName;
 @property (nonatomic,strong)  NSString* serverExperimentId;
 @property (nonatomic,strong)  NSString* schedule;
 @property (nonatomic,strong)  NSString* guid;
 @property (readwrite)  BOOL  type;
 
 
 
 */



- (void)encodeWithCoder:(NSCoder *)encoder
{
    [encoder encodeObject:_who forKey:@"who"];
    [encoder encodeObject:_when forKey:@"when"];
    [encoder encodeObject:_latitude  forKey:@"latitude"];
    [encoder encodeObject:_longitude forKey:@"longitude"];
    [encoder encodeObject:_responseTime forKey:@"responseTime"];
    [encoder encodeObject:_scheduledTime forKey:@"scheduleTime"];
    [encoder encodeObject:_appId forKey:@"appId"];
    [encoder encodeObject:_pacoVersion forKey:@"pacoVersion"];
    [encoder encodeObject:_experimentId forKey:@"experimentId"];
    [encoder encodeObject:_experimentName forKey:@"experimentName"];
    [encoder encodeObject:_experimentVersion forKey:@"experimentVersion"];
     NSArray * responses = [_responses toNSArray];
    [encoder encodeObject:responses forKey:@"responses"];
    [encoder encodeObject:_scheduleId forKey:@"scheduleId"];
    [encoder encodeObject:_actionTriggerId  forKey:@"actionTriggerId"];
    [encoder encodeObject:_actionId  forKey:@"actionId"];
    [encoder encodeObject:_actionTriggerSpecId  forKey:@"actionTriggerSpecId"];
    [encoder encodeObject:_experimentGroupName  forKey:@"experimentGroupName"];
    [encoder encodeObject:_serverExperimentId  forKey:@"serverExperimentId"];
    [encoder encodeObject:_schedule   forKey:@"schedule"];
    [encoder encodeObject:_guid    forKey:@"guid"];
    [encoder encodeBool:_type forKey:@"type"];
}

- (id)initWithCoder:(NSCoder *)decoder
{
    if (self = [super init])
    {
        
        self.who = [decoder decodeObjectForKey:@"who"];
        self.when = [decoder decodeObjectForKey:@"when"];
        self.latitude  = [decoder decodeObjectForKey:@"latitude"];
        self.longitude  = [decoder decodeObjectForKey:@"longitude"];
        self.responseTime  = [decoder decodeObjectForKey:@"responseTime"];
        self.scheduledTime   = [decoder decodeObjectForKey:@"scheduleTime"];
        self.appId           = [decoder decodeObjectForKey:@"appId"];
        self.pacoVersion           = [decoder decodeObjectForKey:@"pacoVersion"];
        self.experimentId            = [decoder decodeObjectForKey:@"experimentId"];
        self.experimentName           = [decoder decodeObjectForKey:@"experimentName"];
        self.experimentVersion          = [decoder decodeObjectForKey:@"experimentVersion"];
        
        NSArray * responses =  [decoder decodeObjectForKey:@"responses"];
        self.responses  = [JavaUtilArrayList arrayListWithValues:responses];
        
        self.scheduleId                         = [decoder decodeObjectForKey:@"scheduleId"];
        self.actionTriggerId                    = [decoder decodeObjectForKey:@"actionTriggerId"];
        self.actionId                           = [decoder decodeObjectForKey:@"actionId"];
        self.actionTriggerId                    = [decoder decodeObjectForKey:@"actionTriggerId"];
        self.actionTriggerSpecId                = [decoder decodeObjectForKey:@"actionTriggerSpecId"];
        self.experimentGroupName                = [decoder decodeObjectForKey:@"experimentGroupName"];
        self.serverExperimentId                 = [decoder decodeObjectForKey:@"serverExperimentId"];
        self.schedule                           = [decoder decodeObjectForKey:@"schedule"];
        self.guid                               = [decoder decodeObjectForKey:@"guid"];
        self.type                               = [decoder decodeObjectForKey:@"type"];
        
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


- (NSArray*)responseListWithImageString {
    
    NSMutableArray* newReponseList = [[NSMutableArray alloc] initWithArray:[self.responses toNSArray] ];
  
    int size = [self.responses size];

    for (int index=0; index< size; index++) {
        
        id responseDict =[self.responses getWithInt:index];
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
    
    return [NSArray arrayWithArray:newReponseList];
}


- (id)payloadJsonWithImageString
{
    if (0 == [self.responses size]) {
        return [self generateJsonObject];
    }
    
     NSMutableDictionary* jsonPayload =
    [NSMutableDictionary dictionaryWithDictionary:[self generateJsonObject]];
 
    jsonPayload[kPacoEventKeyResponsesExtended] = [self responseListWithImageString];
    return jsonPayload;
}




- (NSString*)description {
    
    /*
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
    */
    
    NSString *responseStr = @" response string " ;
   
 
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
 

 




- (id)payloadJsonWithImageStringInline {
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
    event.guid = [[NSUUID UUID] UUIDString];

   
    
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
    
   // NSString* schedule = PASchedulePrinter toStringWithPASchedule:(PASchedule *)
    
    
    //event.schedule =@"GroupAd:[1457994166569:(1457994166571:Daily at start layer: 05:00PM,hidden layer: 06:00PM,hidden layer II: 09:00PM,Telos: 10:00PM)]" ;
    
    
    event.schedule = [experiment scheduleString];
    
    NSDictionary* joinResponse = @{kPacoResponseKeyNameExtended:kPacoResponseJoinExtended,
                                   kPacoResponseKeyAnswerExtended:@"false",
                                   kPacoResponseKeyInputIdExtended:@"-1"};
    
    [PacoEventExtended populateBasicAttributes:experiment Event:event];
    JavaUtilArrayList * responseList = [[JavaUtilArrayList alloc] init];
    event.type= NO;
    
    [responseList addWithId:joinResponse];
    
    
    NSString * scheduleString =  [experiment scheduleString];
    
    NSDictionary* scheduledResponse = @{kPacoResponseKeyNameExtended:kPacoEventKeyResponsesExtended,
                                        @"schedule":scheduleString,
                                        kPacoResponseKeyInputIdExtended:@"-1"};
    
   // [responseList addWithId:scheduledResponse];
    
    
    NSDictionary* systemInfo = @{kPacoResponseKeyNameExtended:kPacoEventKeyResponsesExtended,
                                 [[UIDevice currentDevice] systemName] :[[UIDevice currentDevice] systemVersion] ,
                                 kPacoResponseKeyInputIdExtended:@"-1"};
    
   // [responseList addWithId:systemInfo];
    event.responses = responseList;

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
    event.guid = [[NSUUID UUID] UUIDString];
    
     // NSString* schedule = PASchedulePrinter toStringWithPASchedule:(PASchedule *)
    //event.schedule =
    
    // @"GroupAd:[1457994166569:(1457994166571:Daily at start layer: 05:00PM,hidden layer: 06:00PM,hidden layer II: 09:00PM,Telos: 10:00PM)]";
    
    
    
    
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
 
      return event;
    
    
}






 
 


+ (PacoEventExtended *) genericEventForDefinition:(PAExperimentDAO*)definition
                             withInputs:(NSArray*)inputs {
    
    
     PacoEventExtended *event = [PacoEventExtended new];
     event.who = [PacoNetwork sharedInstance].userEmail;
     event.experimentId = [definition valueForKeyPathEx:@"id"];
     event.experimentName = [definition valueForKeyPathEx:@"title"];
     event.experimentVersion = [definition valueForKeyPathEx:@"version"];
     event.guid = [[NSUUID UUID] UUIDString];
    
    
    NSMutableArray *responses = [NSMutableArray array];
    for (PacoExperimentInput *input in inputs) {
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
    
    event.responses = [JavaUtilArrayList arrayListWithValues:responses];
    return event;
}

 



+ (PacoEventExtended *)selfReportEventForDefinition:(PAExperimentDAO*) definition
                                withInputs:(NSArray*)inputs {
    NSAssert(inputs != nil, @"inputs should not be nil!");
    PacoEventExtended* event = [PacoEventExtended genericEventForDefinition:definition withInputs:inputs];
    event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];
    event.guid = [[NSUUID UUID] UUIDString];
    event.who = @"useremail";
    event.scheduledTime = nil;
    return event;
}


+ (PacoEventExtended*)surveySubmittedEventForDefinition:(PAExperimentDAO*)definition
                                     withInputs:(NSArray*)inputs
                                     ScheduledTime:(NSDate*)scheduledTime
                                     groupName:(NSString*) groupName
                                     actionTriggerId:(NSString*) actionTriggerId
                                     actionId:(NSString*) actionId
                                     actionTriggerSpecId:(NSString*) actionTriggerSpecId
                                     userEmail:(NSString*)userEmail
{
  
    PacoEventExtended* event = [PacoEventExtended genericEventForDefinition:definition withInputs:inputs];
    event.responseTime =  [NSDate dateWithTimeIntervalSinceNow:0];
    event.scheduledTime = [scheduledTime toPacoFormatedString];
    event.guid = [[NSUUID UUID] UUIDString];
    event.who = userEmail;
    event.experimentGroupName = groupName;
    event.actionTriggerId = @([actionTriggerId intValue]);
    event.actionTriggerSpecId = @([actionTriggerSpecId intValue]);
    event.actionId  =  @([actionId intValue]);
    return event;
}


/*
    
    Survay for missed event,  includes scheduled time in the event
 
 */
/*
+ (PacoEventExtended*)surveyMissedEventForDefinition:(PAExperimentDAO*)definition
                           withScheduledTime:(NSDate*)scheduledTime {
    NSAssert(scheduledTime != nil, @"scheduledTime should be valid!");
    PacoEventExtended* event = [self surveyMissedEventForDefinition:definition
                                          withScheduledTime:scheduledTime
                                                          userEmail:@"email"];// [[PacoExtendedClient sharedInstance] userEmail]];
    return event;
}*/

/*
     
  Creates an experiment definition for survay missed.
 
 */
+ (PacoEventExtended*)surveyMissedEventForDefinition:(PAExperimentDAO*)definition
                                   withScheduledTime:(NSDate*)scheduledTime
                                           groupName:(NSString*) groupName
                                            actionId:(NSString*) actionId
                                     actionTriggerId:(NSString*) actionTriggerId
                                     actionTriggerSpecId:(NSString*) actionTriggerSpecId
                                           userEmail:(NSString*)userEmail

{
    NSAssert(definition, @"definition should be valid");
    NSAssert(scheduledTime != nil, @"scheduledTime should be valid!");
    NSAssert([userEmail length] > 0, @"userEmail should be valid!");
    PacoEventExtended *event = [PacoEventExtended new];
    event.who = userEmail;
    //event.actionId =
    event.actionTriggerId = @([actionTriggerId intValue]);
    event.experimentGroupName = groupName;

    event.experimentId = [definition valueForKeyPathEx:@"id"];
    event.experimentName = [definition valueForKeyPathEx:@"title"];
    event.experimentVersion = [definition valueForKeyPathEx:@"version"];
    event.responseTime = nil;
    event.guid = [[NSUUID UUID] UUIDString];
    
   // NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
   // [dateFormat setDateFormat:@"yyyy/MM/dd HH:mm:ssZ"];
   // NSString *dateString = [dateFormat stringFromDate:scheduledTime];
    
    event.scheduledTime = [scheduledTime toPacoFormatedString];// [scheduledTime dateToStringLocalTimezonePrettyPrint];
    return event;
}





#pragma mark - NSCoder & NSCopy methods 


///
//- (id)initWithCoder:(NSCoder *)decoder
//{
//    
//    /* super does not support  initWithCoder so we don't try to invoke it */
//    
//     NSData* data = [decoder decodeObjectForKey:JsonKey];
//     PacoSerializer* serializer =
//    [[PacoSerializer alloc] initWithArrayOfClasses:nil
//                          withNameOfClassAttribute:@"nameOfClass"];
//    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:data];
//    IOSObjectArray * iosArray = [resultArray toArray];
//    PacoEventExtended * event  =  [iosArray objectAtIndex:0];
//    self =event;
//    return self;
// 
//}
//
//
//- (void) encodeWithCoder:(NSCoder *)encoder
//{
//    
//    NSArray* array = [PacoSerializeUtil getClassNames];
//    PacoSerializer * serializer = [[PacoSerializer alloc] initWithArrayOfClasses:array withNameOfClassAttribute:@"nameOfClass"];
//    NSData* json = [serializer toJSONobject:self];
//    [encoder encodeObject:json  forKey:JsonKey];
//}



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

