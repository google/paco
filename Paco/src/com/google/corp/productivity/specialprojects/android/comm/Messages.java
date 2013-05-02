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

import com.pacoapp.paco.R;
import java.text.MessageFormat;

/**
 * This is the central place for a list of login status or failure messages.
 *
 */
public abstract class Messages {
  public static final String INVALID_TOKEN = "Failed to get a valid token from AccountManager: {0}";
  public static final String NULL_TOKEN = "Got null token from AccountManager, if this is the"
      + " first time the application accesses user credentials, please grant permission for the"
      + " application to do so.";
  public static final String AH_LOGIN_CREDENTIAL = "AppSpot login does not accept the authenticated"
      + " token: {0}.";
  public static final String AH_LOGIN_FAILED = "AppSpot login request failed: {0}.";
  public static final String CONTEXT_NOT_FOUND = "Missing android application context.";
  public static final String ACCOUNT_NOT_FOUND = "Missing required account of type <{0}>.";

  public static String get(String message, String... parameters) {
    Object[] args = parameters;
    return MessageFormat.format(message, args);
  }
}
