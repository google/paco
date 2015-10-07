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
#import "PacoExperimentExtended.h"

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
#import "PAExperimentDAO+Util.h"
#import "JavaUtilArrayList+PacoConversion.h"




#define JsonKey @"kjsonPrsistanceKey/ForPacoEvent"




//static NSString* const kPacoEventKeyWhoExtended = @"who";
//static NSString* const kPacoEventKeyWhenExtended = @"when";
//static NSString* const kPacoEventKeyLatitudeExtended = @"lat";
//static NSString* const kPacoEventKeyLongitudeExtended = @"long";
//static NSString* const kPacoEventKeyResponseTimeExtended = @"responseTime";
//static NSString* const kPacoEventKeyAppIdExtended = @"appId";
//static NSString* const kPacoEventKeyScheduledTimeExtended = @"scheduledTime";
//static NSString* const kPacoEventKeyPacoVersionExtended = @"pacoVersion";
//static NSString* const kPacoEventKeyExperimentIdExtended = @"experimentId";
//static NSString* const kPacoEventKeyExperimentNameExtended = @"experimentName";
//static NSString* const kPacoEventKeyExperimentVersionExtended = @"experimentVersion";


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

+ (id)pacoEventForIOS {
    return [[PacoEventExtended alloc] init];
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
    NSString* formattedTime = [dateFormatter stringFromDate:self.responseTime];
    return [NSString stringWithFormat:@"<%@, %p: id=%@,name=%@,version=%d,responseTime=%@,"
            "who=%@,when=%@,response=\r%@>",
            NSStringFromClass([self class]),
            self,
            self.experimentId,
            self.experimentName,
            self.experimentVersion,
            formattedTime,
            self.who,
            self.when,
            responseStr];
}

/*
- (id)generateJsonObject {
    NSMutableDictionary *dictionary = [NSMutableDictionary dictionary];
    dictionary[kPacoEventKeyExperimentIdExtended] = self.experimentId;
    dictionary[kPacoEventKeyExperimentNameExtended] = self.experimentName;
    dictionary[kPacoEventKeyExperimentVersionExtended] = [NSString stringWithFormat:@"%d", self.experimentVersion];
    dictionary[kPacoEventKeyWhoExtended] = self.who;
    dictionary[kPacoEventKeyAppIdExtended] = self.appId;
    dictionary[kPacoEventKeyPacoVersionExtended] = self.pacoVersion;
    if (self.when) {
        dictionary[kPacoEventKeyWhenExtended] = [PacoDateUtility pacoStringForDate:self.when];
    }
    if (self.latitude) {
        dictionary[kPacoEventKeyLatitudeExtended] = [NSString stringWithFormat:@"%lld", self.latitude];
    }
    if (self.longitude) {
        dictionary[kPacoEventKeyLongitudeExtended] = [NSString stringWithFormat:@"%lld", self.longitude];
    }
    if (self.responseTime) {
        dictionary[kPacoEventKeyResponseTimeExtended] = [PacoDateUtility pacoStringForDate:self.responseTime];
    }
    if (self.scheduledTime) {
        dictionary[kPacoEventKeyScheduledTimeExtended] = [PacoDateUtility pacoStringForDate:self.scheduledTime];
    }
    if (self.responses) {
        dictionary[kPacoEventKeyResponsesExtended] = self.responses;
    }
    return [NSDictionary dictionaryWithDictionary:dictionary];
}
 
 */




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



+ (PacoEventExtended*)joinEventForActionSpecificaton:(PAActionSpecification*) actionSpecification
{
    // Setup an event for joining the experiement.
    PacoEventExtended *event = [PacoEventExtended pacoEventForIOS];
    
    
    
   // event.who = [[PacoExtendedClient sharedInstance] userEmail];
    event.experimentId = [[actionSpecification valueForKeyPathEx:@"experiment_.id__"] stringValue];
    event.experimentVersion =  [actionSpecification valueForKeyPathEx:@"experiment_.version__"];
    event.experimentName =  [actionSpecification valueForKeyPathEx:@"experiment_.title_"];
    event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];
    
    //Special response values to indicate the user is joining this experiement.
    //For now, we need to indicate inputId=-1 to avoid server exception,
    //in the future, server needs to fix and accept JOIN and STOP events without inputId
    NSDictionary* joinResponse = @{kPacoResponseKeyNameExtended:kPacoResponseJoinExtended,
                                   kPacoResponseKeyAnswerExtended:@"true",
                                   kPacoResponseKeyInputIdExtended:@"-1"};
    
    NSMutableArray* responseList = [NSMutableArray arrayWithObject:joinResponse];
    
    
   
    
 
    if ([actionSpecification ->experiment_ isSelfReport]){
        

        PacoSerializer* serializer =
        [[PacoSerializer alloc] initWithArrayOfClasses:nil
                              withNameOfClassAttribute:@"nameOfClass"];
 
        /* we are going to add all information about the action specification */
        NSData * scheduleData = (NSData*) [serializer toJSONobject:actionSpecification ->experiment_ ];
        NSString* jsonString =
        [[NSString alloc] initWithData:scheduleData encoding:NSUTF8StringEncoding];

        NSDictionary* response  = @{kPacoResponseKeyNameExtended:@"experiment",
                                           kPacoResponseKeyAnswerExtended:jsonString,
                                           kPacoResponseKeyInputIdExtended:@"-1"};
        
        [responseList addObject:response];
  
    }
    event.responses = responseList;
    return event;
}

+ (PacoEventExtended *)stopEventForExperiment:(PacoExperimentExtended*) experiment
{
    //create an event for stopping the experiement.
    
    PacoEventExtended *event = [PacoEventExtended  pacoEventForIOS];
   // event.who = [[PacoClient sharedInstance] userEmail];  ---<><><><>
    event.experimentId = [experiment.definition valueForKeyPathEx:@"id"];
    event.experimentName = [experiment.definition valueForKeyPathEx:@"title"];
    event.experimentVersion = [experiment.definition valueForKeyPathEx:@"version"];
    event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];
    
    
    
    
    
    //For now, we need to indicate inputId=-1 to avoid server exception,
    //in the future, server needs to fix and accept JOIN and STOP events without inputId
    NSDictionary *responsePair = @{kPacoResponseKeyNameExtended:kPacoResponseJoinExtended,
                                   kPacoResponseKeyAnswerExtended:@"false",
                                   kPacoResponseKeyInputIdExtended:@"-1"};
    event.responses = @[responsePair];
    
    return event;
}


/*
     creates and event
 
 */

+ (PacoEventExtended *) genericEventForDefinition:(PAExperimentDAO*)definition
                             withInputs:(NSArray*)inputs {
    PacoEventExtended *event = [PacoEventExtended pacoEventForIOS];
     event.who = [[PacoExtendedClient sharedInstance] userEmail]; 
     event.experimentId = [definition valueForKeyPathEx:@"id"];
     event.experimentName = [definition valueForKeyPathEx:@"title"];
     event.experimentVersion = [definition valueForKeyPathEx:@"version"];
    
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
    
    event.responses = responses;
    return event;
}

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
    event.responseTime = [NSDate dateWithTimeIntervalSinceNow:0];
    event.scheduledTime = scheduledTime;
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
    PacoEventExtended *event = [PacoEventExtended pacoEventForIOS];
    event.who = userEmail;
    event.experimentId = [definition valueForKeyPathEx:@"id"];
    event.experimentName = [definition valueForKeyPathEx:@"title"];
    event.experimentVersion = [definition valueForKeyPathEx:@"version"];
    event.responseTime = nil;
    event.scheduledTime = scheduledTime;
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
    [serializer addNonDomainClass:self];
    NSData* json = [serializer toJSONobject:self];
    
    JavaUtilArrayList  *  resultArray  = (JavaUtilArrayList*) [serializer buildObjectHierarchyFromJSONOBject:json];
    IOSObjectArray * iosArray = [resultArray toArray];
    PacoEventExtended  * event =  [iosArray objectAtIndex:0];
    return event;
    
}

@end

