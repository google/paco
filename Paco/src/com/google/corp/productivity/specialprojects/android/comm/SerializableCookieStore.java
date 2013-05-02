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

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.pacoapp.paco.R;
import com.google.android.apps.paco.UserPreferences;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.List;

/**
 * This class serializes {@link Cookie} objects from a CookieStore into json
 * strings, saves them into Android application's {@link SharedPreferences} and
 * reads them out later. Saving cookies allows persisting cookies across
 * multiple sessions or different HttpClient objects.
 *
 */
public class SerializableCookieStore extends BasicCookieStore {
  private static final String ATTR_SECURE = "secure";
  private static final String ATTR_PORTS = "ports";
  private static final String ATTR_EXPIRY_DATE = "expiryDate";
  private static final String ATTR_VERSION = "version";
  private static final String ATTR_PATH = "path";
  private static final String ATTR_DOMAIN = "domain";
  private static final String ATTR_COMMENT_URL = "commentUrl";
  private static final String ATTR_COMMENT = "comment";
  private static final String ATTR_VALUE = "value";
  private static final String ATTR_NAME = "name";

  /**
   * Flag to indicate whether changes is made to the cookie store.
   */
  private transient boolean dirty;

  /**
   * Saves the entrie cookie store into android's {@link SharedPreferences}.
   * Only serializes if there has been any changes to the store since the last
   * save.
   *
   * @param preferences The preferences store to persist the cookies.
   */
  public synchronized void saveIfDirty(SharedPreferences preferences) {
    if (dirty | clearExpired(new Date())) {
      save(preferences);
    }
  }

  public synchronized void save(SharedPreferences preferences) {
    List<Cookie> cookies = getCookies();
    int n = cookies.size();
    JSONArray array = new JSONArray();
    for (int i = 0; i < n; i++) {
      Cookie cookie = cookies.get(i);
      putJs(array, i, ATTR_NAME, cookie.getName());
      putJs(array, i, ATTR_VALUE, cookie.getValue());
      putJs(array, i, ATTR_COMMENT, cookie.getComment());
      putJs(array, i, ATTR_COMMENT_URL, cookie.getCommentURL());
      putJs(array, i, ATTR_DOMAIN, cookie.getDomain());
      putJs(array, i, ATTR_PATH, cookie.getPath());
      putJs(array, i, ATTR_VERSION, cookie.getVersion());
      putJs(array, i, ATTR_SECURE, cookie.isSecure());
      putJs(array, i, ATTR_PORTS, cookie.getPorts());
      if (cookie.getExpiryDate() != null) {
        putJs(array, i, ATTR_EXPIRY_DATE, cookie.getExpiryDate().getTime());
      }
    }
    Editor editor = preferences.edit();
    editor.putString(UserPreferences.COOKIE_PREFERENCE_KEY, array.toString());
    editor.commit();
    dirty = false;
  }
  
  public SerializableCookieStore(SharedPreferences preferences) {
    String cookiesString = preferences.getString(UserPreferences.COOKIE_PREFERENCE_KEY, null);
    if (cookiesString != null) {
      try {
        JSONArray cookies = new JSONArray(cookiesString);
        int n = cookies.length();
        for (int i = 0; i < n; i++) {
          JSONObject json = cookies.optJSONObject(i);
          if (!JSONObject.NULL.equals(json)) {
            BasicClientCookie2 cookie =
                new BasicClientCookie2(json.optString(ATTR_NAME), json.optString(ATTR_VALUE));
            if (json.has(ATTR_COMMENT)) {
              cookie.setComment(json.optString(ATTR_COMMENT));
            }
            if (json.has(ATTR_COMMENT_URL)) {
              cookie.setCommentURL(json.optString(ATTR_COMMENT_URL));
            }
            if (json.has(ATTR_DOMAIN)) {
              cookie.setDomain(json.optString(ATTR_DOMAIN));
            }
            if (json.has(ATTR_EXPIRY_DATE)) {
              cookie.setExpiryDate(new Date(json.optLong(ATTR_EXPIRY_DATE)));
            }
            if (json.has(ATTR_PATH)) {
              cookie.setPath(json.optString(ATTR_PATH));
            }
            if (json.has(ATTR_SECURE)) {
              cookie.setSecure(json.optBoolean(ATTR_SECURE));
            }
            if (json.has(ATTR_VERSION)) {
              cookie.setVersion(json.optInt(ATTR_VERSION));
            }
            if (json.has(ATTR_PORTS)) {
              JSONArray arr = json.optJSONArray(ATTR_PORTS);
              if (arr != null) {
                int m = arr.length();
                int[] ports = new int[m];
                for (int j = 0; j < m; j++) {
                  ports[j] = arr.optInt(j);
                }
                cookie.setPorts(ports);
              }
            }
            super.addCookie(cookie);
          }
        }
      } catch (JSONException x) {
        // Invalid string format, no cookies can be read.
      }
    }
    dirty = false;
  }

  @Override
  public synchronized void addCookie(Cookie cookie) {
    Date date = new Date();
    super.addCookie(cookie);
    if (cookie != null && !cookie.isExpired(date)) {
      dirty = true;
    }
  }

  private static void putJs(JSONArray array, int i, String key, Object value) {
    if (value != null) {
      JSONObject object = array.optJSONObject(i);
      try {
        if (object == null) {
          object = new JSONObject();
          array.put(i, object);
        }
        if (value.getClass().isArray()) {
          // android's version of org.json doesn't instantiate array
          // automatically.
          JSONArray newArray = new JSONArray();
          int n = Array.getLength(value);
          for (int j = 0; j < n; j++) {
            newArray.put(Array.get(value, j));
          }
          value = newArray;
        }
        object.put(key, value);
      } catch (JSONException e) {
        // Ignore.
      }
    }
  }
}
