//
//  GoogleClientLogin.m
//  whaleops
//
//  Created by cameron ring on 2/26/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "GoogleClientLogin.h"
#import "GTMNSString+URLArguments.h"


#define CAPTCHA_PREFIX          @"http://www.google.com/accounts/"
#define UNKNOWN_CLIENT_ERROR    @"UnknownClientError"
#define CONNECTION_ERROR        @"ConnetionError"

@interface GoogleClientLogin (private)

-(void)handleAuthResponse:(NSString *)response;
-(NSDictionary *)parseResponseBody:(NSString *)body;

@end


@implementation GoogleClientLogin
@synthesize delegate = m_delegate;

-(id)initWithDelegate:(id<GoogleClientLoginDelegate>)delegate {
    if (!(self = [super init]))
        return self;
    
    // weak pointer to delegate
    m_delegate = delegate;
    
    return self;
}

-(void)dealloc {
    [m_receivedData release];
    [super dealloc];
}

-(void)authWithUsername:(NSString *)username andPassword:(NSString *)password forService:(NSString *)service
              withSource:(NSString *)source {
    [self authWithUsername:username andPassword:password andCaptcha:nil andCaptchaToken:nil forService:service
                withSource:source];
}

-(void)authWithUsername:(NSString *)username andPassword:(NSString *)password andCaptcha:(NSString *)captcha
         andCaptchaToken:(NSString *)captchaToken forService:(NSString *)service withSource:(NSString *)source {
    
    NSString *content = [NSString stringWithFormat:@"accountType=HOSTED_OR_GOOGLE&Email=%@&Passwd=%@&service=%@&source=%@",
                         [username gtm_stringByEscapingForURLArgument], 
                         [password gtm_stringByEscapingForURLArgument],
                         [service gtm_stringByEscapingForURLArgument],
                         [source gtm_stringByEscapingForURLArgument]];

    if (captcha && captchaToken) {
        content = [NSString stringWithFormat:@"%@&logintoken=%@&logincaptcha=%@",
                   content,
                   [captchaToken gtm_stringByEscapingForURLArgument],
                   [captcha gtm_stringByEscapingForURLArgument]];
    }
    
    NSURL *authURL = [NSURL URLWithString:@"https://www.google.com/accounts/ClientLogin"];
    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:authURL];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-type"];
    [request setHTTPBody:[content dataUsingEncoding:NSASCIIStringEncoding]];
    
    [NSURLConnection connectionWithRequest:request delegate:self];
}

+ (NSString *)descriptionForError:(NSString *)error {
    
    if ([error isEqualToString:CONNECTION_ERROR]) {
        return @"There was an error communication with the server";
    } else if ([error isEqualToString:@"BadAuthentication"]) {
        return @"Invalid username or password";
    } else if ([error isEqualToString:@"NotVerified"]) {
        return @"That email address has not been validated. You must verify that address with your Google account before continuing";
    } else if ([error isEqualToString:@"TermsNotAgreed"]) {
        return @"You have not agreed to the terms yet. You must sign in to your Google account on the web before continuing";
    } else if ([error isEqualToString:@"CaptchaRequired"]) {
        return @"A CAPTCHA is required.";
    } else if ([error isEqualToString:@"Unknown"]) {
        return @"There was an unknown error";
    } else if ([error isEqualToString:@"AccountDeleted"]) {
        return @"That account has been deleted";
    } else if ([error isEqualToString:@"AccountDisabled"]) {
        return @"That account has been disabled";
    } else if ([error isEqualToString:@"ServiceDisabled"]) {
        return @"Your access to that service has been disabled";
    } else if ([error isEqualToString:@"ServiceUnavailable"]) {
        return @"That service is currently unavailable. Please try again later";
    }
        
    return @"There was an unknown error (client)";
}

//////////////////////////////////////////////////////////////////////////////////////////////////
// MARK: NSURLConnection delegate methods

-(void)connection:(NSURLConnection *)connection didReceiveResponse:(NSURLResponse *)response {
    
    if (![response isKindOfClass:[NSHTTPURLResponse class]])
        return;
    
    NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
    
    m_statusCode = [httpResponse statusCode];
    
    [m_receivedData release];
    m_receivedData = [[NSMutableData alloc] init];
}

-(void)connection:(NSURLConnection *)connection didReceiveData:(NSData *)data {
    [m_receivedData appendData:data];
}

-(void)connection:(NSURLConnection *)connection didFailWithError:(NSError *)error {
    [m_delegate authFailed:CONNECTION_ERROR];
}

-(void)connectionDidFinishLoading:(NSURLConnection *)connection {

    // process the body
    NSString *body = [[NSString alloc] initWithData:m_receivedData encoding:NSUTF8StringEncoding];
    
    [self handleAuthResponse:body];
    
    [body release];
    [m_receivedData release];
    m_receivedData = nil;
}


//////////////////////////////////////////////////////////////////////////////////////////////////
// MARK: Private method

-(void)handleAuthResponse:(NSString *)response {
    
    NSDictionary *keys = [self parseResponseBody:response];

    if (m_statusCode == 200) {
        NSString *auth = [keys objectForKey:@"Auth"];

        if ([auth length]) {
            [m_delegate authSucceeded:auth];
            return;
        }
    }

    NSString *error = [keys objectForKey:@"Error"];

    if ((m_statusCode != 403) || ![error length]) {
        [m_delegate authFailed:UNKNOWN_CLIENT_ERROR];
        return;        
    }
    
    if (![error isEqualToString:@"CaptchaRequired"]) {
        // a regular error
        [m_delegate authFailed:error];
        return;
    }

    // we need to show a captcha
    NSString *captchaToken = [keys objectForKey:@"CaptchaToken"];
    NSString *captchaPath = [keys objectForKey:@"CaptchaUrl"];
        
    NSURL *captchaURL = [NSURL URLWithString:[NSString stringWithFormat:@"%@%@", CAPTCHA_PREFIX, captchaPath]];
        
    [m_delegate authCaptchaTestNeededFor:captchaToken withCaptchaURL:captchaURL];
}

-(NSDictionary *)parseResponseBody:(NSString *)body {
    
    NSMutableDictionary *keys = [NSMutableDictionary dictionary];
    NSArray *lines = [body componentsSeparatedByString:@"\n"];
    for (NSString *line in lines) {
        
        NSRange separatorRange = [line rangeOfString:@"="];
        
        if (separatorRange.location == NSNotFound)
            break;
        
        NSString *key = [line substringToIndex:separatorRange.location];
        NSString *value = [line substringFromIndex:separatorRange.location + separatorRange.length];
        [keys setObject:value forKey:key];
    }
    
    return keys;
}


@end
