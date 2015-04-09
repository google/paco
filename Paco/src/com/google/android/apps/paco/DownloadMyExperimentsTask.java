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
package com.google.android.apps.paco;

import android.content.Context;
import android.os.AsyncTask;

public class DownloadMyExperimentsTask extends AsyncTask<Void, Void, String> {
  private final Context enclosingContext;
  private UserPreferences userPrefs;
  private DownloadExperimentsTaskListener listener;
  private String contentAsString;
  private Integer limit;
  private String cursor;

  @SuppressWarnings("unchecked")
  public DownloadMyExperimentsTask(Context activity,
                                 DownloadExperimentsTaskListener listener,
                                 UserPreferences userPrefs, Integer downloadLimit, String experimentCursor) {
    enclosingContext = activity;
    this.listener = listener;
    this.userPrefs = userPrefs;
    this.limit = limit;
    this.cursor = experimentCursor;
  }

  protected String doInBackground(Void... params) {
    DownloadHelper downloadHelper = new DownloadHelper(enclosingContext, userPrefs, limit, cursor);
    String errorCode = downloadHelper.downloadMyExperiments();
    contentAsString = downloadHelper.getContentAsString();
    return errorCode;
  }

  protected void onProgressUpdate() {

  }

  protected void onPostExecute(String errorCode) {
    if (listener != null) {
      listener.done(errorCode);
    }
  }

  public String getContentAsString() {
    return contentAsString;
  }
}