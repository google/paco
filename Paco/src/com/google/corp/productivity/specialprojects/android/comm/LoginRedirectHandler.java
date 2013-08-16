/*
* Copyright 2011 Google Inc. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance  with the License.  
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package com.google.corp.productivity.specialprojects.android.comm;

import static com.google.corp.productivity.specialprojects.android.comm.Constants.DEFAULT_CHARSET_NAME;
import static com.google.corp.productivity.specialprojects.android.comm.Constants.KEY_APP_CONTEXT;
import static com.google.corp.productivity.specialprojects.android.comm.Constants.KEY_COOKIE_STORE;
import static com.google.corp.productivity.specialprojects.android.comm.Constants.KEY_FAILURE_MESSAGE;
import static com.google.corp.productivity.specialprojects.android.comm.Constants.KEY_LOGIN_OK;
import static com.google.corp.productivity.specialprojects.android.comm.Constants.KEY_ORIGINAL_REQUEST;
import static com.google.corp.productivity.specialprojects.android.comm.Constants.KEY_REQUIRED_ACTION_INTENT;
import static com.google.corp.productivity.specialprojects.android.comm.Constants.KEY_SECOND_ATTEMPT;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.pacoapp.paco.R;
import com.google.android.apps.paco.UserPreferences;

/**
 * An HTTP redirect handler that intercepts redirection to Google appspot login page,
 * and automatically carries out the login with user credentials stored in {@link AccountManager}.
 *
 */
public class LoginRedirectHandler extends DefaultRedirectHandler {

  private static final String LOG_TAG = "login";

  public static final String AUTH_TOKEN_TYPE = "ah";
  private static final String GOOGLE_ACCOUNT = "com.google";
  private static final String[] LOGIN_REDIRECT_PATTERNS = {"/ServiceLogin?", "/login.corp."};

  private static final int LOGIN_STATUS_OK = 0;
  private static final int LOGIN_STATUS_FAILED = 1;
  private static final int LOGIN_STATUS_ERROR = 2;


  private final String accountType;
  /**
   * Used to perform logins only.
   */
  private final DefaultHttpClient httpClient;

  public LoginRedirectHandler() {
    this(GOOGLE_ACCOUNT);
  }

  LoginRedirectHandler(String accountType) {
    this.accountType = accountType;
    // Sets up a no-redirect http client used only for performing the actual login.
    BasicHttpParams params = new BasicHttpParams();
    HttpClientParams.setRedirecting(params, false);
    this.httpClient = new DefaultHttpClient(params);
  }

  @Override
  public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
    boolean redirect = super.isRedirectRequested(response, context);
    if (redirect) {
      try {
        URI locationURI = getLocationURI(response, context);
        if (isLoginRedirectUrl(locationURI.toString()) && isTargetAnAppEngineHost(response)) {
          Log.d(LOG_TAG, "login page? " + locationURI);
          if (Boolean.TRUE.equals(context.getAttribute(KEY_SECOND_ATTEMPT))) {
            // If this is the second attempt and we still get redirected to the
            // login page, the previously attempted login has failed.
            context.setAttribute(KEY_LOGIN_OK, Boolean.FALSE);
            context.setAttribute(KEY_FAILURE_MESSAGE,
                Messages.get(Messages.AH_LOGIN_FAILED, "unknown"));
          } else {
          
            // If this is the login redirection for appspot, try logging in.
            Context appContext = (Context) context.getAttribute(KEY_APP_CONTEXT);
            HttpUriRequest request = (HttpUriRequest) context.getAttribute(KEY_ORIGINAL_REQUEST);
            Log.d(LOG_TAG, "--- original request : " + request);
            Log.d(LOG_TAG, "--- original uri: " + ((request != null) ? request.getURI() : ""));
            login(context, appContext, request.getURI());
          }
          return false;
        } else if (locationURI.getHost().equals("10.0.2.2") && locationURI.getPath().equals("/_ah/login")) {
          HttpUriRequest request = (HttpUriRequest) context.getAttribute(KEY_ORIGINAL_REQUEST);
          Context appContext = (Context) context.getAttribute(KEY_APP_CONTEXT);
          devLogin(context, appContext, request);
          
        }
      } catch (ProtocolException e) {
        // Ignore this, only checking
        Log.w(LOG_TAG, e);
      }
    }
    return redirect;
  }

  private void devLogin(HttpContext context, Context appContext, HttpUriRequest request) {
    String accountName = getSelectedAccountName(appContext);
    String url = "http://"+request.getURI().getHost() + ":" + request.getURI().getPort()+ "/_ah/login?continue=" + request.getURI().toString();
    Object cookieStore = context.getAttribute(KEY_COOKIE_STORE);
    HttpResponse response;
    synchronized (httpClient) {
      if (cookieStore instanceof CookieStore) {
        httpClient.setCookieStore((CookieStore) cookieStore);
      }
      try {
        HttpPost httpPost = new HttpPost(url);
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("email", accountName));
        nvps.add(new BasicNameValuePair("isAdmin", "on"));
        nvps.add(new BasicNameValuePair("continue", request.getURI().toString()));
        nvps.add(new BasicNameValuePair("action", "Log In"));

        httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8)); 
        response = httpClient.execute(httpPost, context);
        switch (getLoginStatus(response)) {
        case LOGIN_STATUS_OK:
          context.setAttribute(KEY_LOGIN_OK, Boolean.TRUE);
        case LOGIN_STATUS_FAILED:
          context.setAttribute(KEY_FAILURE_MESSAGE,
              Messages.get(Messages.AH_LOGIN_CREDENTIAL, "blah"));
          break;
        case LOGIN_STATUS_ERROR:
         context.setAttribute(KEY_FAILURE_MESSAGE,
              Messages.get(Messages.AH_LOGIN_FAILED, response.getStatusLine().getReasonPhrase()));
          break;
      }

      } catch (ClientProtocolException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }


    
  }

  /**
   * Pre checks to see if permission already granted to access credentials. This method only
   * checks if the given context is an activity, so that {@link AccountManager} can initiate
   * appropriate activity if any action is required.
   *
   * @param context
   */
  public void checkCredentialPermission(Context context) {
    if (context instanceof Activity) {
      Activity activity = (Activity) context;
      AccountManager accountManager = AccountManager.get(context);
      String accountName = getSelectedAccountName(context); 
      Account account = findAccount(accountManager, context, accountName);
      
      AccountManagerFuture<Bundle> future = null;
      if (account != null) {
        // when this is gotten, it will just work or throw an error?
        future = accountManager.getAuthToken(account, AUTH_TOKEN_TYPE, null, activity, null, null);
      }
      try {
        Bundle b = future.getResult();
      } catch (Exception e) {
        Log.w(LOG_TAG, e);
      }
    }
  }

  private String getSelectedAccountName(Context context) {
    UserPreferences userPrefs = new UserPreferences(context);
    String accountName = userPrefs.getSelectedAccount();
    return accountName;
  }


  private boolean isTargetAnAppEngineHost(HttpResponse response) {
	  return true;
//    Header header = response.getFirstHeader(APP_ENGINE_ID_HEADER); 
//    if (header != null) {
//      Log.d(LOG_TAG, "Request matches appengine " + header.getValue());
//    }
//    return header != null;
  }

  private void login(final HttpContext httpContext, final Context context, URI uri) {
    if (context == null) {
      httpContext.setAttribute(KEY_LOGIN_OK, Boolean.FALSE);
      httpContext.setAttribute(KEY_FAILURE_MESSAGE, Messages.get(Messages.CONTEXT_NOT_FOUND));
      Log.w(LOG_TAG, "no android context found in this request.");
      return;
    }
    AccountManager accountManager = AccountManager.get(context);
    Account account = checkAccount(httpContext, context, accountManager);
    Log.d(LOG_TAG, "--- account : " + account);
    if (account != null) {
      String token = getToken(httpContext, accountManager, account);
      boolean loggedIn = appspotLogin(httpContext, uri, token);
      if (!loggedIn) {
        // Token might be stale - try again
        accountManager.invalidateAuthToken(accountType, token);
        token = getToken(httpContext, accountManager, account);
        loggedIn = appspotLogin(httpContext, uri, token);
      }
    }
  }

  /**
   * Checks if suitable account exists, otherwise tries to get an intent for
   * creating new account. If no matching account is found, it would request for
   * an Intent and add it to the {@link HttpContext}'s attributes by the
   * name of {@link #KEY_REQUIRED_ACTION_INTENT}.
   *
   * @param httpContext HTTP to store and load attributes.
   * @param context The android application context.
   * @param accountManager The android {@link AccountManager}.
   * @return An appropriate account matching the
   *         {@link LoginRedirectHandler#accountType}, <code>null</code> if no
   *         such account is found.
   */
  private Account checkAccount(final HttpContext httpContext, final Context context,
      final AccountManager accountManager) {
    String accountName = getSelectedAccountName(context);
    Account account = findAccount(accountManager, context, accountName);
//    if (account == null) {
//      // Account not found - ask user to create account
//      AccountManagerFuture<Bundle> future =
//        accountManager.addAccount(accountType, AUTH_TOKEN_TYPE, null, null, null, null, null);
//      try {
//        // This is a blocking call
//        Bundle result = future.getResult();
//        String aName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
//        Log.d(LOG_TAG, "--- account name : " + aName);
//        if (aName == null && result.containsKey(AccountManager.KEY_INTENT)) {
//          String message = Messages.get(Messages.ACCOUNT_NOT_FOUND, accountType);
//          Object intent = result.get(AccountManager.KEY_INTENT);
//          httpContext.setAttribute(KEY_FAILURE_MESSAGE, message);
//          httpContext.setAttribute(KEY_REQUIRED_ACTION_INTENT, intent);
//        }
//      } catch (Exception e) {
//        Log.e(LOG_TAG, "failed to add account", e);
//        httpContext.setAttribute(KEY_LOGIN_OK, Boolean.FALSE);
//        httpContext.setAttribute(KEY_FAILURE_MESSAGE, e.getMessage());
//      }
//      account = findAccount(accountManager, context, null);
//    }
    return account;
  }

  protected Account findAccount(AccountManager am, Context context, String accountName) {
    Account[] accounts = am.getAccountsByType(accountType);
    final List<Account> matchingAccounts = new ArrayList<Account>();
    for (Account account : accounts) {
      if (account.name.equals(accountName)) {
       return account;
      }
    }
    return null;
  }

  /**
   * Performs the actual app hosting (ah) login.
   */
  private boolean appspotLogin(HttpContext httpContext, URI uri, String token) {
    if (token != null) {
      try {
        StringBuilder loginUrlBuilder = new StringBuilder(uri.getScheme())
            .append("://")
            .append(uri.getHost())
            .append("/_ah/login?continue=")
            .append(URLEncoder.encode(uri.toString(), DEFAULT_CHARSET_NAME))
            .append("&auth=")
            .append(URLEncoder.encode(token, DEFAULT_CHARSET_NAME));
        Object cookieStore = httpContext.getAttribute(KEY_COOKIE_STORE);
        HttpResponse response;
        // One login at a time, since the http client is shared, so is the
        // cookie store. This should be ok since we expect login to occur
        // rarely.
        synchronized (httpClient) {
          if (cookieStore instanceof CookieStore) {
            httpClient.setCookieStore((CookieStore) cookieStore);
          }
          response = httpClient.execute(new HttpGet(loginUrlBuilder.toString()), httpContext);
        }
        switch (getLoginStatus(response)) {
          case LOGIN_STATUS_OK:
            httpContext.setAttribute(KEY_LOGIN_OK, Boolean.TRUE);
            return true;
          case LOGIN_STATUS_FAILED:
            httpContext.setAttribute(KEY_FAILURE_MESSAGE,
                Messages.get(Messages.AH_LOGIN_CREDENTIAL, token));
            break;
          case LOGIN_STATUS_ERROR:
            httpContext.setAttribute(KEY_FAILURE_MESSAGE,
                Messages.get(Messages.AH_LOGIN_FAILED, response.getStatusLine().getReasonPhrase()));
            break;
        }
      } catch (Exception e) {
        Log.w(LOG_TAG, e);
        httpContext.setAttribute(KEY_FAILURE_MESSAGE,
            Messages.get(Messages.AH_LOGIN_FAILED, e.getMessage()));
      }
    } else {
      if (httpContext.getAttribute(KEY_FAILURE_MESSAGE) == null) {
        httpContext.setAttribute(KEY_FAILURE_MESSAGE, Messages.get(Messages.NULL_TOKEN));
      }
    }
    httpContext.setAttribute(KEY_LOGIN_OK, Boolean.FALSE);
    return false;
  }

  private int getLoginStatus(HttpResponse response) {
    int code = response.getStatusLine().getStatusCode();
    if (code >= 200 && code < 300) {
      return LOGIN_STATUS_OK;
    }
    if (code == 301 || code == 302) {
      Header location = response.getFirstHeader("Location");
      if (location == null) {
        return LOGIN_STATUS_OK;
      }
      return isLoginRedirectUrl(location.getValue()) ? LOGIN_STATUS_FAILED : LOGIN_STATUS_OK;
    }
    return LOGIN_STATUS_ERROR;
  }

  private String getToken(HttpContext httpContext, AccountManager accountManager, Account account) {
    AccountManagerFuture<Bundle> future = accountManager.getAuthToken
        (account, AUTH_TOKEN_TYPE, false, null, null);
    Bundle result;
    try {
      result = future.getResult();
      String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
      if (authToken != null) {
        return authToken;
      }
      // If auth token is not returned, there might be extra action the user has
      // to perform before getting a valid token.
      httpContext.setAttribute(KEY_REQUIRED_ACTION_INTENT, result.get(AccountManager.KEY_INTENT));
      httpContext.setAttribute(KEY_FAILURE_MESSAGE,
          Messages.get(Messages.INVALID_TOKEN, "Credential changed?"));
    } catch (Exception e) {
      httpContext.setAttribute(KEY_FAILURE_MESSAGE,
          Messages.get(Messages.INVALID_TOKEN, e.getMessage()));
      Log.e(LOG_TAG, "getToken:", e);
    }
    return null;
  }

  private static boolean isLoginRedirectUrl(String url) {
    if (url != null) {
      for (String pattern : LOGIN_REDIRECT_PATTERNS) {
        if (url.indexOf(pattern) >= 0) {
          return true;
        }
      }
    }
    return false;
  }
}
