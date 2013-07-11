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

import com.pacoapp.paco.R;
import com.google.android.apps.paco.UserPreferences;
import com.google.corp.productivity.specialprojects.android.comm.Response.Status;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A url content manager that wraps around the {@link DefaultHttpClient} class.
 * This class also handles required logins to appspot urls, it automatically
 * stores login credential cookies across multiple sessions, and uses the
 * AccountManager's stored credential when login is required.
 *
 * <p>
 * Example usage:
 * <p>
 *
 * <pre>Context context; // android activity/application context
 * ...
 * URLContentManager manager = new URLContentManager(context);
 * Response response = manager.createRequest()
 *     .setUri("http://quantifiedself.appspot.com/events")
 *     .setPostData("{\"data\":42}", "text/json")
 *     .execute();
 * </pre>
 *
 * @see Request
 * @see Response
 *
 */
public class UrlContentManager {
  /*
   * Client connection pool properties.
   */
  private static final long CONNPOOL_DEFAULT_TIMEOUT = 5000L;
  private static final int CONNPOOL_DEFAULT_MAX_TOTAL = 10;
  private static final int CONNPOOL_DEFAULT_MAX_PER_ROUTE = 5;

  private static final String LOG_TAG = UrlContentManager.class.getSimpleName();

  private final LoginRedirectHandler loginHandler;
  private final SerializableCookieStore cookieStore;
  /**
   * Used in case we need to persist anything under the application context.
   */
  private final Context applicationContext;
  /**
   * Note : do not use this member directly, use {@link #getHttpClient()}.
   */
  private AbstractHttpClient httpClient;

  /**
   * Constructs a {@link UrlContentManager} that would handle url request (post
   * or get) via a {@link Request} object obtained by calling
   * {@link UrlContentManager#createRequest()}.
   *
   * @param context Android's application {@link Context}.
   */
  public UrlContentManager(Context context) {
    this.applicationContext = context.getApplicationContext();
    this.loginHandler = new LoginRedirectHandler();
    SharedPreferences preferences = context.getSharedPreferences(UserPreferences.PREFERENCE_KEY, Context.MODE_PRIVATE);
    this.cookieStore = new SerializableCookieStore(preferences);
    loginHandler.checkCredentialPermission(context);
  }

  protected Response execute(HttpUriRequest request) {
    HttpResponse response;
    HttpContext context = createContext(request);
    String errorMessage = null;
    try {
      response = getHttpClient().execute(request, context);
      // redo the request if the previous execution detected that a login
      // was required and only attempted a successful "GET" login request.
      if (Boolean.TRUE.equals(context.getAttribute(KEY_LOGIN_OK))) {
        context = createContext(request);
        context.setAttribute(KEY_SECOND_ATTEMPT, Boolean.TRUE);
        response = getHttpClient().execute(request, context);
      }
    } catch (IOException e) {
      response = null;
      errorMessage = e.getMessage();
      Log.w(LOG_TAG, e);
    } finally {
      SharedPreferences preferences =
          applicationContext.getSharedPreferences(UserPreferences.PREFERENCE_KEY, Context.MODE_PRIVATE);
      cookieStore.saveIfDirty(preferences);
    }
    return getResponse(response, context, errorMessage);
  }

  /**
   * Creates a {@link Request} object that would execute under the current
   * {@link UrlContentManager} context.
   */
  public Request createRequest() {
    return new RequestImpl();
  }

  /**
   * Resets all connections that are currently opened and reclaims resources
   * used in making connections.
   */
  public synchronized void cleanUp() {
    // Clear the instance first before clearing its contents.
    AbstractHttpClient tmp = httpClient;
    httpClient = null;
    if (tmp != null) {
      Log.d(LOG_TAG, "cleaning up");
      SharedPreferences preferences = applicationContext.getSharedPreferences(UserPreferences.PREFERENCE_KEY, Context.MODE_PRIVATE);
      cookieStore.saveIfDirty(preferences);
      tmp.clearRequestInterceptors();
      tmp.clearResponseInterceptors();
      tmp.getConnectionManager().shutdown();
    }
  }

  protected AbstractHttpClient getHttpClient() {
    if (httpClient == null) {
      synchronized (this) {
        // Double checks in case of race condition on the first
        // "httpClient == null"
        if (httpClient == null) {
          Log.d(LOG_TAG, "creating new httpclient");
          // Registers schemes to handle.
          SchemeRegistry registry = new SchemeRegistry();
          registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
          registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
          // Always allows redirecting.
          BasicHttpParams params = new BasicHttpParams();
          params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
          HttpClientParams.setRedirecting(params, true);
         
          // Use a thread-safe connection manager.
          ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(params, registry);
          ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRoute() {
            @Override
            public int getMaxForRoute(HttpRoute route) {
              return CONNPOOL_DEFAULT_MAX_PER_ROUTE;
            }
          });
          ConnManagerParams.setMaxTotalConnections(params, CONNPOOL_DEFAULT_MAX_TOTAL);
          ConnManagerParams.setTimeout(params, CONNPOOL_DEFAULT_TIMEOUT);
          httpClient = new DefaultHttpClient(connManager, params);
          // Uses the LoginRedirectHandler to intercept and handle appspot
          // logins.
          httpClient.setRedirectHandler(loginHandler);
          // Sets cookie store.
          httpClient.setCookieStore(cookieStore);
          // for local appengine testing, allow circular rediirects
          httpClient.getParams().setParameter("http.protocol.allow-circular-redirects", true);
        }
      }
    }
    return httpClient;
  }

  protected static String getCharSetEncoding(String contentType) {
    if (contentType != null) {
      String[] pairs = contentType.split(";");
      for (String pair : pairs) {
        int splitter = pair.indexOf('=');
        if (splitter > 0) {
          String key = pair.substring(0, splitter).trim().toLowerCase();
          if ("charset".equals(key) && pair.length() > splitter) {
            String charset = pair.substring(splitter + 1).trim();
            if (charset.length() > 0) {
              return charset;
            }
            break;
          }
        }
      }
    }
    return DEFAULT_CHARSET_NAME;
  }

  private HttpContext createContext(HttpRequest request) {
    BasicHttpContext context = new BasicHttpContext();
    context.setAttribute(KEY_APP_CONTEXT, applicationContext);
    context.setAttribute(KEY_COOKIE_STORE, cookieStore);
    context.setAttribute(KEY_ORIGINAL_REQUEST, request);
    return context;
  }

  private Response getResponse(HttpResponse response, HttpContext context, String errorMessage) {
    ResponseImpl finalResponse;
    if (context.getAttribute(KEY_REQUIRED_ACTION_INTENT) instanceof Intent) {
      // case 1: If login processor registers an intent to create account,
      // propagate that intent to the caller.
      Intent actionIntent = (Intent) context.getAttribute(KEY_REQUIRED_ACTION_INTENT);
      finalResponse = new ResponseImpl(Status.ACCOUNT_REQUIRED).setRequiredAction(actionIntent);
    } else if (Boolean.FALSE.equals(context.getAttribute(KEY_LOGIN_OK))) {
      // case 2: If login processor explicitly registers a FALSE flag,
      // tell the caller that login fails after we tried everything.
      finalResponse = new ResponseImpl(Status.AUTH_FAILED);
    } else if (response == null) {
      // case 3: If no response is obtained, something is wrong with network.
      finalResponse = new ResponseImpl(Status.NETWORK_ERROR);
    } else {
      // case 4: Normal response.
      finalResponse = new ResponseImpl(response);
    }
    // sets status message if there is any.
    finalResponse.addStatusMessage(errorMessage);
    Object message = context.getAttribute(KEY_FAILURE_MESSAGE);
    if (message instanceof String) {
      finalResponse.addStatusMessage((String) message);
    }
    return finalResponse;
  }

  private class RequestImpl implements Request {
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private List<Pair<String, String>> headers;
    private AbstractHttpEntity postData;
    private URI uri;

    @Override
    public Request addHeader(String headerKey, String headerValue) {
      if (headers == null) {
        headers = new ArrayList<Pair<String, String>>();
      }
      headers.add(new Pair<String, String>(headerKey, headerValue));
      return this;
    }
    
    @Override
    public String getHeaderValue(String headerKey) {
      for (Pair<String, String> keyVal : headers) {
        if (keyVal.first.equals(headerKey)) {
          return keyVal.second;
        }
      }
      return null;
    }

    @Override
    public Response execute() {
      if (uri == null) {
        throw new IllegalStateException("Uri must be set.");
      }
      HttpUriRequest request = (postData == null) ? toHttpGet() : toHttpPost();
      return UrlContentManager.this.execute(request);
    }

    @Override
    public Request setPostData(String postData) {
      return setPostData(postData, null);
    }

    @Override
    public Request setPostData(String postData, String contentType)
        throws UnsupportedCharsetException {
      if (postData != null) {
        String charset = getCharSetEncoding(contentType);
        try {
          this.postData = new StringEntity(postData, charset);
          if (contentType != null) {
            this.postData.setContentType(new BasicHeader(CONTENT_TYPE_HEADER, contentType));
          }
        } catch (UnsupportedEncodingException e) {
          UnsupportedCharsetException charsetException = new UnsupportedCharsetException(charset);
          charsetException.initCause(e);
          throw charsetException;
        }
      } else {
        this.postData = null;
      }
      return this;
    }

    @Override
    public Request setPostData(byte[] postData, String contentType) {
      if (postData != null) {
        this.postData = new ByteArrayEntity(postData);
        if (contentType != null) {
          this.postData.setContentType(new BasicHeader(CONTENT_TYPE_HEADER, contentType));
        }
      } else {
        this.postData = null;
      }
      return this;
    }

    @Override
    public Request setUrl(String url) throws IllegalArgumentException {
      try {
        this.uri = new URI(url);
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(e);
      }
      return this;
    }

    @Override
    public String getUrl() {
      return uri == null ? null : uri.toString();
    }

    private HttpPost toHttpPost() {
      HttpPost post = new HttpPost(uri);
      setHeaders(post);
      if (postData != null) {
        post.setEntity(postData);
      }
      return post;
    }

    private HttpGet toHttpGet() {
      HttpGet get = new HttpGet(uri);
      setHeaders(get);
      return get;
    }

    private void setHeaders(HttpRequest request) {
      if (headers != null) {
        for (Pair<String, String> header : headers) {
          request.setHeader(header.first, header.second);
        }
      }
    }

    @Override
    public String removeFirstHeader(String headerKey) {
      String value = null;
      if (headers != null) {
        for(Pair<String, String> header : headers) {
          if (header.first.equalsIgnoreCase(headerKey)) {
            value = header.second;
            headers.remove(header);
            break;
          }
        }
      }
      return value;
    }
  }
}
