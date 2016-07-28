//
//  EasyJSDataFunction.m
//  EasyJSWebViewSample
//
//  Created by Alex Lau on 21/1/13.
//  Copyright (c) 2013 Dukeland. All rights reserved.
//

#import "EasyJSDataFunction.h"

@implementation EasyJSDataFunction

@synthesize funcID;
@synthesize webView;
@synthesize removeAfterExecute;

- (id) initWithWebView:(EasyJSWebView *)_webView{
	self = [super init];
    if (self) {
		self.webView = _webView;
    }
    return self;
}

- (NSString*) execute{
	return [self executeWithParams:nil];
}

- (NSString*) executeWithParam: (NSString*) param{
	NSMutableArray* params = [[NSMutableArray alloc] initWithObjects:param, nil];
	return [self executeWithParams:params];
}

- (NSString*) executeWithParams: (NSArray*) params{
	NSMutableString* injection = [[NSMutableString alloc] init];
	
	[injection appendFormat:@"EasyJS.invokeCallback(\"%@\", %@", self.funcID, self.removeAfterExecute ? @"true" : @"false"];
	
	if (params){
		for (int i = 0, l = params.count; i < l; i++){
			NSString* arg = [params objectAtIndex:i];
			NSString* encodedArg = (NSString*) CFURLCreateStringByAddingPercentEscapes(NULL, (CFStringRef)arg, NULL, (CFStringRef) @"!*'();:@&=+$,/?%#[]", kCFStringEncodingUTF8);
			
			[injection appendFormat:@", \"%@\"", encodedArg];
		}
	}
	
	[injection appendString:@");"];
	
	if (self.webView){
		return [self.webView stringByEvaluatingJavaScriptFromString:injection];
	}else{
		return nil;
	}
}

@end