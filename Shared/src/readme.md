NOTE: This source folder is used in transpilation to Objective-c and is json-ified and used by all clients.

Do not put dependencies on third parties (outside joda and guava) in the code in this folder unless you
also make transpilations to Objective-C and to Json via the JsonConverter in src_non_j2objc.

For code that needs to use other third party libraries and doesn't need to be available cross-platform, put it in 
src_non_j2objc.