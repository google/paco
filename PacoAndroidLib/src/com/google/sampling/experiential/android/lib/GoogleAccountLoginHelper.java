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
package com.google.sampling.experiential.android.lib;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

/**
 * Class that manages authorizing user against a Google.com account, and then
 * authorizing them against the Paco AppEngine app.
 * 
 *
 */
public class GoogleAccountLoginHelper {

  public static final String AUTH_TOKEN_PREFERENCE = "PREFS_AUTH";
  public static final String AUTH_TOKEN_PREFERENCE_NAME_KEY = "name";
  public static final String AUTH_TOKEN_PREFERENCE_EXPIRE_KEY = "expiration";

  /** Which hosted domain the server will accept logins for */
  private static final String HOSTED_DOMAIN = "google.com";
  private static String DESIRED_SUFFIX = "@" + HOSTED_DOMAIN;
  /** Account type when querying AccountManager for google accounts */
  private static final String GOOGLE_ACCOUNT_TYPE = "com.google";
  /** Service to use when requesting a token for GAE */
  private static final String GAE_SERVICE = "ah";
  
  private static final String PACO_SERVER_URL = "https://pacoexample.appspot.com/";
  
  private Activity context;
  private SharedPreferences authTokenPreferences;

  /**
   * Create an instance.
   * 
   * @param context Context that has access to Authentication Shared Preferences.
   */
  public GoogleAccountLoginHelper(Activity context) {
    this.context = context;
    authTokenPreferences = this.context.getSharedPreferences(AUTH_TOKEN_PREFERENCE, 
        Context.MODE_PRIVATE);
  }
    
  /**
   * @return
   */
  public synchronized boolean isAuthorized() {
    return isPacoAuthCookieSetAndValid() /*|| authorize()*/;
  }

  /**
   * Validates Paco AppEngine server cookie.
   * 
   * @return whether or not the Paco Appengine server cookie exists and is valid.
   */
  private boolean isPacoAuthCookieSetAndValid() {    
    String key = authTokenPreferences.getString(AUTH_TOKEN_PREFERENCE_NAME_KEY, null);
    String expiry = authTokenPreferences.getString(AUTH_TOKEN_PREFERENCE_EXPIRE_KEY, null);
    return isPacoAuthCookieSetAndValid(key, expiry);
  }

  private boolean isPacoAuthCookieSetAndValid(String key, String expiry) {
    if (key == null || expiry == null) {
      return false;
    }
    try {
      Date expirationDate = new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(expiry);
      return expirationDate.after(new Date());
    } catch (ParseException e) {
      return false;
    }
  }
    
  /**
   * Retrieve the stored Paco appengine server login cookie.
   * 
   * @return Cookie
   */
  public synchronized Cookie retrievePacoAuthCookie() {
    String name = authTokenPreferences.getString(AUTH_TOKEN_PREFERENCE_NAME_KEY, null);
    String expiry = authTokenPreferences.getString(AUTH_TOKEN_PREFERENCE_EXPIRE_KEY, null);
    if (!isPacoAuthCookieSetAndValid(name, expiry)) {
      throw new IllegalStateException("Invalid Auth Cookie! Send back to login!");
    }
    try {
      Date expirationDate = new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(expiry);
      return new MyCookie(name, authTokenPreferences.getString("value", ""),
          expirationDate, authTokenPreferences.getString("domain", "google.com"),
          authTokenPreferences.getString("path", "/"), authTokenPreferences.getInt("version", 0));
    } catch (ParseException e) {
      // this should never happen.
      throw new IllegalStateException("Unparseable date in Auth Cookie! Send back to login!");
    }
  }
  
  /**
   * Authorize against a Google.com account in order to login to the paco server which 
   * is protected by a google.com login at the moment.
   * 
   * @return boolean whether authorization succeeded.
   * @throws OperationCanceledException
   * @throws AuthenticatorException
   * @throws IOException
   */
  public synchronized boolean authorize() {
    AccountManager accountManager = AccountManager.get(context);
    Account account = getGoogleAccount(accountManager, DESIRED_SUFFIX);
    if (account == null) {
      Log.d(Constants.TAG, "No Google.com account in accounts list.");
      return false;
    }
    // String authToken = accountManager.blockingGetAuthToken(account,
    // GAE_SERVICE, false);
    try {
      String googleAuthToken = getGoogleAuthToken(accountManager, account);
      if (googleAuthToken == null) {
        return false;
      }    
      return loginToPacoService(googleAuthToken);
    } catch (OperationCanceledException e) {
      Log.d(Constants.TAG, "Exception getting GoogleAuthToken. ", e);
    } catch (AuthenticatorException e) {
      Log.d(Constants.TAG, "Exception getting GoogleAuthToken. ", e);
    } catch (IOException e) {
      Log.d(Constants.TAG, "Exception getting GoogleAuthToken. ", e);
    }    
    return false;
  }

  private boolean loginToPacoService(String authToken) {
    HttpGet httpget = new HttpGet(PACO_SERVER_URL + "_ah/login?auth=" + authToken + 
        "&continue=" + PACO_SERVER_URL);
    DefaultHttpClient httpclient = new DefaultHttpClient();
    HttpResponse response;
    try {
      response = httpclient.execute(httpget);
      Log.i(Constants.TAG, "Appspot.com Login Response: " + response.getStatusLine());
      // TODO (bobevans): Deal with other responses (redirect, fail, captcha, etc.)
      List<Cookie> cookies = httpclient.getCookieStore().getCookies();

      if (cookies.size() == 0) {
        Log.d(Constants.TAG, "No cookies in httpclient!");
        return false;
      }
      storePacoAuthCookie(cookies.get(0));
      return true;
    } catch (ClientProtocolException e1) {
      Log.e(Constants.TAG, "in service login", e1);
    } catch (IOException e1) {
      Log.e(Constants.TAG, "in service login", e1);
    }
    return false;
  }

  /**
   * Store a new Paco AppEngine server cookie.
   * This will only be used publicly when the other communications with the Paco server send us
   * back a new cookie instead of the existing cookie. 
   * 
   * @param authCookie Cookie from logging into Paco AppEngine instance.
   */
  public synchronized void storePacoAuthCookie(Cookie authCookie) {
    Editor editor = authTokenPreferences.edit();
    editor.putString("comment", authCookie.getComment());
    editor.putString("commentURL", authCookie.getCommentURL());
    editor.putString("domain", authCookie.getDomain());
    editor.putString("name", authCookie.getName());
    editor.putString("path", authCookie.getPath());
    editor.putString("value", authCookie.getValue());
    SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
    editor.putString("expiration", df.format(authCookie.getExpiryDate()));
    if (authCookie.getPorts() != null) {
      editor.putString("ports",stringify(authCookie.getPorts()));
    }
    editor.putInt("version", authCookie.getVersion());
    editor.commit();
  }

  private String getGoogleAuthToken(AccountManager accountManager, Account account) throws 
      OperationCanceledException, IOException, AuthenticatorException {
    String authToken = getNewAuthToken(accountManager, account);
    accountManager.invalidateAuthToken("ah", authToken);
    // There is a bug. Workaround: invalidate the old token, then re-retrieve.
    authToken = getNewAuthToken(accountManager, account);
    return authToken;
  }

  private String getNewAuthToken(AccountManager accountManager, Account account) throws 
      OperationCanceledException, IOException, AuthenticatorException {
    AccountManagerFuture<Bundle> accountManagerFuture = accountManager.getAuthToken(account, "ah", 
        null, context, null, null);

    Bundle authTokenBundle = accountManagerFuture.getResult();
    return authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();
  }

  private Account getGoogleAccount(AccountManager accountManager, String desiredSuffix) {
    for (Account account : accountManager.getAccountsByType(GOOGLE_ACCOUNT_TYPE)) {
      if (account.name.endsWith(desiredSuffix)) {
        return account;
      }
    }
    return null;
  }
  
  /**
   * Creates a GoogleAccountLoginHelper that will return authorization in a state you like.
   * 
   * @param context
   * @param authorized
   * @return
   */
  public static GoogleAccountLoginHelper createMockLoginHelper(final Activity context, 
      final boolean authorized) {
    return new GoogleAccountLoginHelper(context) {
      @Override
      public synchronized boolean isAuthorized() {
        return authorized;
      }      
    };
  }

  private static String stringify(int[] ports) {
    StringBuilder portStr = new StringBuilder();
    boolean first = true;
    for (int i = 0; i < ports.length; i++) {
      if (first) {
        first = false;
      } else {
        portStr.append(",");
      }
      portStr.append(ports[i]);
    }
    return portStr.toString();
  }

  /**
   * We cannot set properties on the BasicClientCookie from Apache httpclient. 
   * This class allows us to access those private methods to recreate our stored cookie.
   * 
   *
   */
  public static class MyCookie extends BasicClientCookie {
    public MyCookie(String name, String value, Date expirationDate, String domain, String path,
        int version) {
      super(name, value);
      setExpiryDate(expirationDate);
      setDomain(domain);
      setPath(path);
      setVersion(version);
    }
  }

}
