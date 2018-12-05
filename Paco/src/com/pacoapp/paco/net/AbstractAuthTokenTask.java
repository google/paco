package com.pacoapp.paco.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.pacoapp.paco.UserPreferences;

import android.os.AsyncTask;


/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public abstract class AbstractAuthTokenTask extends AsyncTask<Void, Void, Void> {

  private static Logger Log = LoggerFactory.getLogger(AbstractAuthTokenTask.class);

      protected NetworkClient networkClient;

      protected String oAuthScope = AUTH_TOKEN_TYPE_USERINFO_EMAIL;
      protected UserPreferences userPrefs;
      public static final String AUTH_TOKEN_TYPE_USERINFO_EMAIL = "oauth2:https://www.googleapis.com/auth/userinfo.email";
      private static final String AUTH_TOKEN_TYPE_USERINFO_PROFILE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
      static final String UTF_8 = "UTF-8";

      AbstractAuthTokenTask(NetworkClient networkClient) {
          this.networkClient = networkClient;
          userPrefs = new UserPreferences(networkClient.getContext());
      }

      @Override
      protected Void doInBackground(Void... params) {
        try {
          doRequest();
        } catch (IOException ex) {
          onError("Following Error occured, please try again. " + ex.getMessage(), ex);
        } catch (JSONException e) {
          onError("Bad response: " + e.getMessage(), e);
        }
        return null;
      }

      protected void onError(String msg, Exception e) {
          if (e != null) {
            Log.error("Exception: ", e);
          }
          networkClient.show(msg);  // will be run in UI thread
      }

      /**
       * Get a authentication token if one is not available. If the error is not recoverable then
       * it displays the error message on parent networkClient.
       */
      protected abstract String fetchToken() throws IOException;

      /**
       * Contacts the user info server to get the profile of the user and extracts the first name
       * of the user from the profile. In order to authenticate with the user info server the method
       * first fetches an access token from Google Play services.
       * @throws IOException if communication with user info server failed.
       * @throws JSONException if the response from the server could not be parsed.
       */
      protected void doRequest() throws IOException, JSONException {
          String token = fetchToken();
          if (token == null) {
            // error has already been handled in fetchToken()
            return;
          }
          userPrefs.setAccessToken(token);
          URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
          HttpURLConnection con = ServerAddressBuilder.getConnection(url);

          int sc = 0;
          try {
            sc = con.getResponseCode();
          } catch (ConnectException e) {
            sc = 503;
          }

          if (sc == 200) {
            InputStream is = con.getInputStream();
            String name = "Finished!"; //getFirstName(readResponse(is));
            is.close();
            networkClient.showAndFinish("Hello " + name + "!");
            return;
          } else if (sc == 401) {
              GoogleAuthUtil.invalidateToken(networkClient.getContext(), token);
              onError("Server auth error, please try again.", null);
              Log.info("Server auth error: " + readResponse(con.getErrorStream()));
              return;
          } else {
            onError("Server returned the following error code: " + sc, null);
            return;
          }
      }

      /**
       * Reads the response from the input stream and returns it as a string.
       */
      protected static String readResponse(InputStream is) throws IOException {
          ByteArrayOutputStream bos = new ByteArrayOutputStream();
          byte[] data = new byte[2048];
          int len = 0;
          while ((len = is.read(data, 0, data.length)) >= 0) {
              bos.write(data, 0, len);
          }
          return new String(bos.toByteArray(), UTF_8);
      }

  }
