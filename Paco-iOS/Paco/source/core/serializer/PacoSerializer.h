//
//  PacoPacoSerializer.h
//  Paco
//
//  Created by Timothy  Tim O'Brien on 7/23/15.
//
//
//  Parses the collection heirarchy created by a json parser to produce a model
//  tree
//
//
//
//

#import <Foundation/Foundation.h>

/*
     types of messages.

 */
typedef NS_ENUM(NSInteger, PacoParserType) {
  kInputTypeArraytEntry,
  kInputTypeDictionaryEntry,
  kInputTypeNewDictionary,
  kInputTypeEndNewDictionary,
  kInputTypeStartArray,
  kInputTypeEndArray,
  kInputTypeUnmatchedDictionary,
  kInputTypeUnmatchedArray,
};

typedef NS_ENUM(NSInteger, PacoParentType) {
  kParentTypeList,
  kParentTypeDictionary,
  kParentTypeObject,
};

/* number of matched ivars / total number of ivars */
#define PACO_MATCHER_SUCCESS_RATIO .4
#define PACO_LIST_PARENT @"LISTPARENTPACPO"
#define PACO_OBJECT_NO_MATCH @"NO MATCH"
#define PACO_OBJECT_PARENT @"PARENT"

@interface PacoSerializer : NSObject

/*
   used to track where we are in a collection heirarchy. Object tracking should
   contain
   only classes of NSArray or classes of NSDictionary.

 */

/* disable the use of default init so the caller uses the init method with
 * classes */
- (id)init __attribute__((unavailable("init with array of class names")));


/* timezone used for data conversions*/
@property(nonatomic,strong) NSTimeZone* timeZone;


/* init with name of class only.  */
- (instancetype) initWithArrayWithClassAttributeName: (NSString*) nameOfClass;

/* init with classes and name of class matching fields.  */
- (instancetype)initWithArrayOfClasses:(NSArray*)serializedJson withNameOfClassAttribute:(NSString*) nameOfClass;
/* convert netst foundation collections to json data*/
- (NSData*)foundationCollectionToJSONData:(NSObject*)collection
                                    Error:(NSError*)error;
/* create a collection hierarchy of j2obj models from a collection of Foundation
 * collections*/
- (NSObject*)toJ2OBJCCollctionsHeirarchy:(NSObject*)parent;
/* create a collection hierarchy of j2obj models from nested Foundation
 * collections */
- (NSObject*)buildObjectHierarchyFromJSONOBject:(id)data;
/* create a json object from nested Foundation classes */
- (NSData *)toJSONobject:(NSObject*)parent;
/* create a json object from nested Foundation classes */
- (NSObject*)buildObjectHierarchyFromCollections:(id)collection;

/* get a json object from an NSArray of j2objc objects */
-(NSArray*) experimentToJSonStringFromNSArrayOfDefinitionObjects:(NSArray*) definitions;

- (void)validate:(NSArray*)parentInfo;

/* convert a strong of json to a json heirarchy. */
- (NSObject*)buildObjectHierarchyFromJSONString:(id)json;

/* add class that does not support domain prefix, i.e., 'PA' */
-(void) addNoneDomainClass:(NSObject*) object;

/* return a single object after from json string */
- (NSObject*)buildSingleObjectHierarchyFromJSONString:(id)json;

/* crate a  object from dictionary */
-(NSObject*) buildModelObject:(NSDictionary*) dictionary;



@end
