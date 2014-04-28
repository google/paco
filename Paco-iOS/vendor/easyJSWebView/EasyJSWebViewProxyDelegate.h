//
//  EasyJSWebViewDelegate.h
//  EasyJS
//
//  Created by Lau Alex on 19/1/13.
//  Copyright (c) 2013 Dukeland. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface EasyJSWebViewProxyDelegate : NSObject<UIWebViewDelegate>

@property (nonatomic, retain) NSMutableDictionary* javascriptInterfaces;
@property (nonatomic, retain) id<UIWebViewDelegate> realDelegate;

- (void) addJavascriptInterfaces:(NSObject*) interface WithName:(NSString*) name;

@end
