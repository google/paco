//
//  Generated by the J2ObjC translator.  DO NOT EDIT!
//  source: joda-convert/src/main/java/org/joda/convert/ToString.java
//

#include "IOSClass.h"
#include "J2ObjC_source.h"
#include "java/lang/annotation/ElementType.h"
#include "java/lang/annotation/Retention.h"
#include "java/lang/annotation/RetentionPolicy.h"
#include "java/lang/annotation/Target.h"
#include "org/joda/convert/ToString.h"

@implementation OrgJodaConvertToString

- (IOSClass *)annotationType {
  return OrgJodaConvertToString_class_();
}

- (NSString *)description {
  return @"@org.joda.convert.ToString()";
}

+ (IOSObjectArray *)__annotations {
  return [IOSObjectArray arrayWithObjects:(id[]) { [[[JavaLangAnnotationTarget alloc] initWithValue:[IOSObjectArray arrayWithObjects:(id[]) { JavaLangAnnotationElementType_get_METHOD() } count:1 type:NSObject_class_()]] autorelease], [[[JavaLangAnnotationRetention alloc] initWithValue:JavaLangAnnotationRetentionPolicy_get_RUNTIME()] autorelease] } count:2 type:JavaLangAnnotationAnnotation_class_()];
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcClassInfo _OrgJodaConvertToString = { 2, "ToString", "org.joda.convert", NULL, 0x2609, 0, NULL, 0, NULL, 0, NULL, 0, NULL, NULL, NULL };
  return &_OrgJodaConvertToString;
}

@end

J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(OrgJodaConvertToString)