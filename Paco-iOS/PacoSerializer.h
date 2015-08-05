//
//  PacoPacoSerializer.h
//  Paco
//
//  Created by Timothy  Northrop O'Brien on 7/23/15.
//
//  Parses the collection heirarchy created by a json parser to produce a modal tree
//
//
//  Copyright (c) 2015 Paco. All rights reserved.
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
#define PACO_LIST_PARENT  @"LISTPARENTPACPO"
#define PACO_OBJECT_NO_MATCH @"NO MATCH"
#define PACO_OBJECT_PARENT  @"PARENT"

@interface PacoSerializer : NSObject

/*
   used to track where we are in a collection heirarchy. Object tracking should contain
   only classes of NSArray or classes of NSDictionary.
 
 */


/* disable the use of default init so the caller uses the init method with classes */
- (id) init __attribute__((unavailable("init with array of class names")));

/* init with classes. This is the method that shoul be used always*/
- (instancetype)initWithArrayOfClasses:(NSArray*) serializedJson;
/* convert netst foundation collections to json data*/
-(NSData*) foundationCollectionToJSONData:(NSObject*) collection Error:(NSError*) error;
/* create a collection hierarchy of j2obj modals from a collection of Foundation collections*/
-(NSObject* ) toJ2OBJCCollctionsHeirarchy:(NSObject*) parent;
/* create a collection hierarchy of j2obj modals from nested Foundation collections */
-(NSObject*) buildObjectHierarchyFromJSONOBject:(id) data;
/* create a json object from nested Foundation classes */
-(NSObject* ) toJSONobject:(NSObject*) parent;
/* create a json object from nested Foundation classes */
-(NSObject*) buildObjectHierarchyFromCollections:(id) collection;
/* recursively call validate   */ 

-(void) validate:(NSArray *) parentInfo;


@end
