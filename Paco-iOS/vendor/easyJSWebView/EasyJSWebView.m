//
//  EasyJSWebView.m
//  EasyJS
//
//  Created by Lau Alex on 19/1/13.
//  Copyright (c) 2013 Dukeland. All rights reserved.
//

#import "EasyJSWebView.h"

@implementation EasyJSWebView

@synthesize proxyDelegate;

- (id)initWithFrame:(CGRect)frame{
    self = [super initWithFrame:frame];
    if (self) {
		[self initEasyJS];
    }
    return self;
}
- (id)init{
	self = [super init];
    if (self) {
		[self initEasyJS];
    }
    return self;
}
- (id)initWithCoder:(NSCoder *)aDecoder{
	self = [super initWithCoder:aDecoder];
	
	if (self){
		[self initEasyJS];
	}
	
	return self;
}

- (void) initEasyJS{
	self.proxyDelegate = [[EasyJSWebViewProxyDelegate alloc] init];
	self.delegate = self.proxyDelegate;
}

- (void) setDelegate:(id<UIWebViewDelegate>)delegate{
	if (delegate != self.proxyDelegate){
		self.proxyDelegate.realDelegate = delegate;
	}else{
		[super setDelegate:delegate];
	}
}

- (void) addJavascriptInterfaces:(NSObject*) interface WithName:(NSString*) name{
	[self.proxyDelegate addJavascriptInterfaces:interface WithName:name];
}

- (void) dealloc{
	[super dealloc];
	
	[self.proxyDelegate release];
	self.proxyDelegate = nil;
}

@end
