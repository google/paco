//
//  GoogleAppEngineAuth.m
//  whaleops
//
//  Created by cameron ring on 2/26/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "GoogleAppEngineAuth.h"
#import "GTMNSString+URLArguments.h"
#import <CommonCrypto/CommonDigest.h>

@interface GoogleAppEngineAuth (private)

-(void)authForDevServerWith:(NSString *)username;
-(NSString *)userIdForUsername:(NSString *)username;

@end


@implementation GoogleAppEngineAuth
@synthesize delegate = m_delegate;

-(id)initWithDelegate:(id<GoogleClientLoginDelegate>)delegate andAppURL:(NSURL *)appURL {
    
    if (!(self = [super init]))
        return self;
    
    m_googleClientLogin = [[GoogleClientLogin alloc] initWithDelegate:self];
    m_delegate = delegate;
    
    m_appURL = [appURL retain];
    
    m_useDevServer = [[appURL host] isEqualToString:@"localhost"];
    
    return self;
}

-(void)dealloc {
    [m_appURL release];
    [m_googleClientLogin setDelegate:nil];
    [m_googleClientLogin release];
    [super dealloc];
}

-(void)authWithUsername:(NSString *)username andPassword:(NSString *)password withSource:(NSString *)source {
    
    [self authWithUsername:username andPassword:password andCaptcha:nil andCaptchaToken:nil withSource:source];
}

-(void)authWithUsername:(NSString *)username andPassword:(NSString *)password andCaptcha:(NSString *)captcha
         andCaptchaToken:(NSString *)captchaToken withSource:(NSString *)source {

    if (m_useDevServer) {
        [self authForDevServerWith:username];
        return;
    }
    
    // ah is the service for app engine, apparently
    [m_googleClientLogin authWithUsername:username andPassword:password andCaptcha:captcha andCaptchaToken:captchaToken
                               forService:@"ah" withSource:source];
    
}


//////////////////////////////////////////////////////////////////////////////////////////////////
// MARK: GoogleClientLoginDelegate methods

-(void)authSucceeded:(NSString *)authKey {

    // request correct cookie
    NSURL *cookieURL = [NSURL URLWithString:[NSString stringWithFormat:@"%@/_ah/login?continue=%@/&auth=%@", 
                                             m_appURL,
                                             m_appURL,
                                             [authKey gtm_stringByEscapingForURLArgument]
                                             ]
                        ];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:cookieURL];
    [request setHTTPMethod:@"GET"];
    
    [NSURLConnection connectionWithRequest:request delegate:self];
}

-(void)authFailed:(NSString *)error {
    [m_delegate authFailed:error];
}

-(void)authCaptchaTestNeededFor:(NSString *)captchaToken withCaptchaURL:(NSURL *)captchaURL {
    [m_delegate authCaptchaTestNeededFor:captchaToken withCaptchaURL:captchaURL];    
}

//////////////////////////////////////////////////////////////////////////////////////////////////
// MARK: NSURLConnection delegate methods

-(void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
    
    if (![response isKindOfClass:[NSHTTPURLResponse class]]) {
        [m_delegate authFailed:@"UnknownClientError"];
        [connection cancel];
        return;
    }

    // no need to read body
    [connection cancel];

    NSArray *cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookiesForURL:[response URL]];
    
    // iterate over cookies looking for ACSID
    for (NSHTTPCookie *cookie in cookies) {
        if (!([[cookie name] isEqualToString:@"ACSID"] || [[cookie name] isEqualToString:@"SACSID"]))
            continue;
        
        [m_delegate authSucceeded:[cookie value]];
        return;
    }
    
    [m_delegate authFailed:@"UnknownAppEngineClientError"];
}

-(void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {

}

-(void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error {
    NSString *msg = [NSString stringWithFormat:@"Connection Error (%@)", [error localizedDescription]];
    [m_delegate authFailed:msg];
}

-(void)connectionDidFinishLoading:(NSURLConnection *)connection {
    
}

//////////////////////////////////////////////////////////////////////////////////////////////////
// MARK: Private methods

// only used when generating a cookie for authing against the dev server
-(NSString *)userIdForUsername:(NSString *)username {
	const char *cStr = [username UTF8String];
	unsigned char result[CC_MD5_DIGEST_LENGTH];
    
	CC_MD5(cStr, strlen(cStr), result);
	return [NSString 
			stringWithFormat: @"1%02d%02d%02d%02d%02d%02d%02d%02d",
			result[0], result[1],
			result[2], result[3],
			result[4], result[5],
			result[6], result[7],
			result[8], result[9],
			result[10], result[11],
			result[12], result[13],
			result[14], result[15]
			];
    
}

// if we're authing against the dev server, just set the right cookie
-(void)authForDevServerWith:(NSString *)username {

    NSHTTPCookieStorage *cookieJar = [NSHTTPCookieStorage sharedHTTPCookieStorage];
    
    NSString *value = [NSString stringWithFormat:@"%@:False:%@", username, [self userIdForUsername:username]];
    
    NSDictionary *properties = [NSDictionary dictionaryWithObjectsAndKeys:
                                @"dev_appserver_login", NSHTTPCookieName,
                                value, NSHTTPCookieValue,
                                @"/", NSHTTPCookiePath,
                                @"localhost", NSHTTPCookieDomain,
                                nil];
    
    [m_delegate authSucceeded:value];
    
    [cookieJar setCookie:[NSHTTPCookie cookieWithProperties:properties]];
}

@end
