//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: joda-time/src/main/java/org/joda/time/convert/LongConverter.java
//

#include "J2ObjC_header.h"

#pragma push_macro("OrgJodaTimeConvertLongConverter_INCLUDE_ALL")
#ifdef OrgJodaTimeConvertLongConverter_RESTRICT
#define OrgJodaTimeConvertLongConverter_INCLUDE_ALL 0
#else
#define OrgJodaTimeConvertLongConverter_INCLUDE_ALL 1
#endif
#undef OrgJodaTimeConvertLongConverter_RESTRICT

#if !defined (OrgJodaTimeConvertLongConverter_) && (OrgJodaTimeConvertLongConverter_INCLUDE_ALL || defined(OrgJodaTimeConvertLongConverter_INCLUDE))
#define OrgJodaTimeConvertLongConverter_

#define OrgJodaTimeConvertAbstractConverter_RESTRICT 1
#define OrgJodaTimeConvertAbstractConverter_INCLUDE 1
#include "org/joda/time/convert/AbstractConverter.h"

#define OrgJodaTimeConvertInstantConverter_RESTRICT 1
#define OrgJodaTimeConvertInstantConverter_INCLUDE 1
#include "org/joda/time/convert/InstantConverter.h"

#define OrgJodaTimeConvertPartialConverter_RESTRICT 1
#define OrgJodaTimeConvertPartialConverter_INCLUDE 1
#include "org/joda/time/convert/PartialConverter.h"

#define OrgJodaTimeConvertDurationConverter_RESTRICT 1
#define OrgJodaTimeConvertDurationConverter_INCLUDE 1
#include "org/joda/time/convert/DurationConverter.h"

@class IOSClass;
@class OrgJodaTimeChronology;

@interface OrgJodaTimeConvertLongConverter : OrgJodaTimeConvertAbstractConverter < OrgJodaTimeConvertInstantConverter, OrgJodaTimeConvertPartialConverter, OrgJodaTimeConvertDurationConverter >

#pragma mark Public

- (jlong)getDurationMillisWithId:(id)object;

- (jlong)getInstantMillisWithId:(id)object
      withOrgJodaTimeChronology:(OrgJodaTimeChronology *)chrono;

- (IOSClass *)getSupportedType;

#pragma mark Protected

- (instancetype)init;

@end

J2OBJC_STATIC_INIT(OrgJodaTimeConvertLongConverter)

inline OrgJodaTimeConvertLongConverter *OrgJodaTimeConvertLongConverter_get_INSTANCE();
/*! INTERNAL ONLY - Use accessor function from above. */
FOUNDATION_EXPORT OrgJodaTimeConvertLongConverter *OrgJodaTimeConvertLongConverter_INSTANCE;
J2OBJC_STATIC_FIELD_OBJ_FINAL(OrgJodaTimeConvertLongConverter, INSTANCE, OrgJodaTimeConvertLongConverter *)

FOUNDATION_EXPORT void OrgJodaTimeConvertLongConverter_init(OrgJodaTimeConvertLongConverter *self);

FOUNDATION_EXPORT OrgJodaTimeConvertLongConverter *new_OrgJodaTimeConvertLongConverter_init() NS_RETURNS_RETAINED;

FOUNDATION_EXPORT OrgJodaTimeConvertLongConverter *create_OrgJodaTimeConvertLongConverter_init();

J2OBJC_TYPE_LITERAL_HEADER(OrgJodaTimeConvertLongConverter)

#endif

#pragma pop_macro("OrgJodaTimeConvertLongConverter_INCLUDE_ALL")